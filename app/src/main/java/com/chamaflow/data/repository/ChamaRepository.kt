package com.chamaflow.data.repository

import com.chamaflow.data.models.Chama
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton
import java.util.UUID

@Singleton
class ChamaRepository @Inject constructor(
    private val db: FirebaseFirestore,
    private val auth: FirebaseAuth
) {
    private val col = db.collection("chamas")

    fun getUserChamasFlow(): Flow<List<Chama>> = callbackFlow {
        var registration: ListenerRegistration? = null
        
        val authListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            registration?.remove()
            val uid = firebaseAuth.currentUser?.uid
            if (uid != null) {
                registration = col.whereArrayContains("memberIds", uid)
                    .addSnapshotListener { snap, e ->
                        if (e != null) return@addSnapshotListener
                        val chamas = snap?.documents?.mapNotNull { 
                            it.toObject(Chama::class.java)?.copy(id = it.id) 
                        } ?: emptyList()
                        trySend(chamas)
                    }
            } else {
                trySend(emptyList())
            }
        }
        
        auth.addAuthStateListener(authListener)
        
        awaitClose {
            auth.removeAuthStateListener(authListener)
            registration?.remove()
        }
    }

    fun getChamaFlow(chamaId: String): Flow<Chama?> = callbackFlow {
        if (chamaId == "PERSONAL" || chamaId.isEmpty()) {
            trySend(null)
            awaitClose { }
            return@callbackFlow
        }
        val l = col.document(chamaId).addSnapshotListener { snap, e ->
            if (e != null) return@addSnapshotListener
            trySend(snap?.toObject(Chama::class.java)?.copy(id = snap.id))
        }
        awaitClose { l.remove() }
    }

    suspend fun createChama(chama: Chama): AuthResult<String> {
        val uid = auth.currentUser?.uid ?: return AuthResult.Error("Not logged in.")
        return try {
            val inviteCode = UUID.randomUUID().toString().take(8).uppercase()
            val ref = col.document()
            val chamaData = mapOf(
                "id" to ref.id, "name" to chama.name, "description" to chama.description,
                "goal" to chama.goal, "contributionAmount" to chama.contributionAmount,
                "penaltyAmount" to chama.penaltyAmount, "joiningFee" to chama.joiningFee,
                "loanInterestRate" to chama.loanInterestRate,
                "meetingFrequency" to chama.meetingFrequency.name,
                "memberIds" to listOf(uid), "createdBy" to uid,
                "inviteCode" to inviteCode,
                "createdAt" to System.currentTimeMillis(), "totalBalance" to 0.0, "memberCount" to 1,
                "joinRequests" to emptyList<String>()
            )
            ref.set(chamaData).await()
            
            db.collection("users").document(uid).get().await().data?.let { userProfile ->
                val memberRef = ref.collection("members").document(uid)
                memberRef.set(mapOf(
                    "id" to uid,
                    "chamaId" to ref.id,
                    "userId" to uid,
                    "fullName" to (userProfile["fullName"] ?: "Admin"),
                    "phoneNumber" to (userProfile["phoneNumber"] ?: ""),
                    "email" to (userProfile["email"] ?: ""),
                    "role" to "ADMIN",
                    "status" to "ACTIVE",
                    "joinDate" to java.time.LocalDate.now().toString()
                )).await()
            }
            
            AuthResult.Success(ref.id)
        } catch (e: Exception) { AuthResult.Error("Failed to create: ${e.message}") }
    }

    suspend fun searchChamas(query: String): AuthResult<List<Chama>> = try {
        val snap = col.orderBy("name")
            .startAt(query)
            .endAt(query + "\uf8ff")
            .limit(10)
            .get().await()
        val list = snap.documents.mapNotNull { it.toObject(Chama::class.java)?.copy(id = it.id) }
        AuthResult.Success(list)
    } catch (e: Exception) { AuthResult.Error("Search failed: ${e.message}") }

    suspend fun joinChamaWithCode(inviteCode: String): AuthResult<String> {
        val uid = auth.currentUser?.uid ?: return AuthResult.Error("Not logged in.")
        return try {
            val snap = col.whereEqualTo("inviteCode", inviteCode.uppercase()).get().await()
            val doc = snap.documents.firstOrNull() ?: return AuthResult.Error("No chama found with that code.")
            val chamaId = doc.id
            
            db.runTransaction { tx ->
                tx.update(doc.reference, "memberIds", FieldValue.arrayUnion(uid))
                tx.update(doc.reference, "memberCount", (doc.getLong("memberCount") ?: 0) + 1)
            }.await()
            
            db.collection("users").document(uid).get().await().data?.let { userProfile ->
                doc.reference.collection("members").document(uid).set(mapOf(
                    "id" to uid,
                    "chamaId" to chamaId,
                    "userId" to uid,
                    "fullName" to (userProfile["fullName"] ?: "Member"),
                    "phoneNumber" to (userProfile["phoneNumber"] ?: ""),
                    "email" to (userProfile["email"] ?: ""),
                    "role" to "MEMBER",
                    "status" to "ACTIVE",
                    "joinDate" to java.time.LocalDate.now().toString()
                )).await()
            }
            
            AuthResult.Success(chamaId)
        } catch (e: Exception) { AuthResult.Error("Failed to join: ${e.message}") }
    }

    suspend fun requestToJoin(chamaId: String): AuthResult<Unit> {
        val uid = auth.currentUser?.uid ?: return AuthResult.Error("Not logged in.")
        return try {
            col.document(chamaId).update("joinRequests", FieldValue.arrayUnion(uid)).await()
            AuthResult.Success(Unit)
        } catch (e: Exception) { AuthResult.Error("Request failed: ${e.message}") }
    }

    suspend fun getChamaById(chamaId: String): AuthResult<Chama> = try {
        val doc = col.document(chamaId).get().await()
        doc.toObject(Chama::class.java)?.copy(id = doc.id)?.let { AuthResult.Success(it) }
            ?: AuthResult.Error("Chama not found.")
    } catch (e: Exception) { AuthResult.Error("Failed to load: ${e.message}") }
    
    @Suppress("UNCHECKED_CAST")
    fun getJoinRequestsFlow(chamaId: String): Flow<List<Map<String, Any>>> = callbackFlow {
        if (chamaId == "PERSONAL" || chamaId.isEmpty()) {
            trySend(emptyList())
            awaitClose { }
            return@callbackFlow
        }
        val l = col.document(chamaId).addSnapshotListener { snap, e ->
            if (e != null) return@addSnapshotListener
            val uids = snap?.get("joinRequests") as? List<String> ?: emptyList()
            if (uids.isEmpty()) {
                trySend(emptyList())
            } else {
                // We can't easily listen to multiple user docs in one go reactively without separate listeners
                // For now, we'll just fetch them whenever the Chama doc's joinRequests array changes
                db.collection("users").whereIn("userId", uids).get().addOnSuccessListener { users ->
                    trySend(users.documents.map { it.data!! })
                }
            }
        }
        awaitClose { l.remove() }
    }

    suspend fun acceptJoinRequest(chamaId: String, userId: String): AuthResult<Unit> = try {
        val chamaRef = col.document(chamaId)
        
        // 1. Update the Chama document
        db.runTransaction { tx ->
            val snap = tx.get(chamaRef)
            tx.update(chamaRef, "joinRequests", FieldValue.arrayRemove(userId))
            tx.update(chamaRef, "memberIds", FieldValue.arrayUnion(userId))
            val currentCount = snap.getLong("memberCount") ?: 0
            tx.update(chamaRef, "memberCount", currentCount + 1)
        }.await()
        
        // 2. Add the user to the members subcollection
        db.collection("users").document(userId).get().await().data?.let { userProfile ->
            chamaRef.collection("members").document(userId).set(mapOf(
                "id" to userId,
                "chamaId" to chamaId,
                "userId" to userId,
                "fullName" to (userProfile["fullName"] ?: "Member"),
                "phoneNumber" to (userProfile["phoneNumber"] ?: ""),
                "email" to (userProfile["email"] ?: ""),
                "role" to "MEMBER",
                "status" to "ACTIVE",
                "joinDate" to java.time.LocalDate.now().toString()
            )).await()
        }
        AuthResult.Success(Unit)
    } catch (e: Exception) { AuthResult.Error("Action failed: ${e.message}") }
}
