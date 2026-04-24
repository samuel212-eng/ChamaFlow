package com.chamaflow.data.repository

import com.chamaflow.data.models.Chama
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChamaRepository @Inject constructor(
    private val db: FirebaseFirestore,
    private val auth: FirebaseAuth
) {
    private val col = db.collection("chamas")

    fun getUserChamasFlow(): Flow<List<Chama>> = callbackFlow {
        val uid = auth.currentUser?.uid ?: run { trySend(emptyList()); close(); return@callbackFlow }
        val l = col.whereArrayContains("memberIds", uid).addSnapshotListener { snap, e ->
            if (e != null) { close(e); return@addSnapshotListener }
            trySend(snap?.documents?.mapNotNull { it.toObject(Chama::class.java)?.copy(id = it.id) } ?: emptyList())
        }
        awaitClose { l.remove() }
    }

    suspend fun createChama(chama: Chama): AuthResult<String> {
        val uid = auth.currentUser?.uid ?: return AuthResult.Error("Not logged in.")
        return try {
            val ref = col.document()
            ref.set(mapOf(
                "id" to ref.id, "name" to chama.name, "description" to chama.description,
                "goal" to chama.goal, "contributionAmount" to chama.contributionAmount,
                "penaltyAmount" to chama.penaltyAmount, "joiningFee" to chama.joiningFee,
                "loanInterestRate" to chama.loanInterestRate,
                "meetingFrequency" to chama.meetingFrequency.name,
                "memberIds" to listOf(uid), "createdBy" to uid,
                "createdAt" to System.currentTimeMillis(), "totalBalance" to 0.0, "memberCount" to 1
            )).await()
            AuthResult.Success(ref.id)
        } catch (e: Exception) { AuthResult.Error("Failed to create: ${e.message}") }
    }

    suspend fun joinChamaWithCode(inviteCode: String): AuthResult<String> {
        val uid = auth.currentUser?.uid ?: return AuthResult.Error("Not logged in.")
        return try {
            val snap = col.whereEqualTo("inviteCode", inviteCode).get().await()
            val doc = snap.documents.firstOrNull() ?: return AuthResult.Error("No chama found with that code.")
            col.document(doc.id).update("memberIds", FieldValue.arrayUnion(uid)).await()
            AuthResult.Success(doc.id)
        } catch (e: Exception) { AuthResult.Error("Failed to join: ${e.message}") }
    }

    suspend fun getChamaById(chamaId: String): AuthResult<Chama> = try {
        val doc = col.document(chamaId).get().await()
        doc.toObject(Chama::class.java)?.copy(id = doc.id)?.let { AuthResult.Success(it) }
            ?: AuthResult.Error("Chama not found.")
    } catch (e: Exception) { AuthResult.Error("Failed to load: ${e.message}") }
}
