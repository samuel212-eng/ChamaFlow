package com.chamaflow.data.repository

import com.chamaflow.data.models.*
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

typealias FirestoreResult<T> = AuthResult<T>

// ── Members ───────────────────────────────────────────────────────────────────

@Singleton
class MembersRepository @Inject constructor(private val db: FirebaseFirestore) {
    private fun col(chamaId: String) = db.collection("chamas").document(chamaId).collection("members")

    fun getMembersFlow(chamaId: String): Flow<List<Member>> = callbackFlow {
        val l = col(chamaId).orderBy("fullName").addSnapshotListener { snap, e ->
            if (e != null) { close(e); return@addSnapshotListener }
            trySend(snap?.documents?.mapNotNull { it.toObject(Member::class.java)?.copy(id = it.id) } ?: emptyList())
        }
        awaitClose { l.remove() }
    }

    suspend fun addMember(chamaId: String, member: Member): FirestoreResult<String> = try {
        val ref = col(chamaId).document()
        ref.set(member.copy(id = ref.id, chamaId = chamaId)).await()
        AuthResult.Success(ref.id)
    } catch (e: Exception) { AuthResult.Error("Failed to add member: ${e.message}") }

    suspend fun updateMember(chamaId: String, member: Member): FirestoreResult<Unit> = try {
        col(chamaId).document(member.id).set(member).await(); AuthResult.Success(Unit)
    } catch (e: Exception) { AuthResult.Error("Failed to update: ${e.message}") }

    suspend fun deleteMember(chamaId: String, memberId: String): FirestoreResult<Unit> = try {
        col(chamaId).document(memberId).delete().await(); AuthResult.Success(Unit)
    } catch (e: Exception) { AuthResult.Error("Failed to remove: ${e.message}") }

    suspend fun getMemberById(chamaId: String, memberId: String): FirestoreResult<Member> = try {
        val doc = col(chamaId).document(memberId).get().await()
        doc.toObject(Member::class.java)?.copy(id = doc.id)?.let { AuthResult.Success(it) }
            ?: AuthResult.Error("Member not found")
    } catch (e: Exception) { AuthResult.Error("Failed to load: ${e.message}") }
}

// ── Contributions ─────────────────────────────────────────────────────────────

@Singleton
class ContributionsRepository @Inject constructor(private val db: FirebaseFirestore) {
    private fun col(chamaId: String) = db.collection("chamas").document(chamaId).collection("contributions")

    fun getContributionsForMonth(chamaId: String, month: String): Flow<List<Contribution>> = callbackFlow {
        val l = col(chamaId).whereEqualTo("month", month)
            .orderBy("date", Query.Direction.DESCENDING)
            .addSnapshotListener { snap, e ->
                if (e != null) { close(e); return@addSnapshotListener }
                trySend(snap?.documents?.mapNotNull { it.toObject(Contribution::class.java)?.copy(id = it.id) } ?: emptyList())
            }
        awaitClose { l.remove() }
    }

    fun getMemberContributions(chamaId: String, memberId: String): Flow<List<Contribution>> = callbackFlow {
        val l = col(chamaId).whereEqualTo("memberId", memberId)
            .orderBy("date", Query.Direction.DESCENDING)
            .addSnapshotListener { snap, e ->
                if (e != null) { close(e); return@addSnapshotListener }
                trySend(snap?.documents?.mapNotNull { it.toObject(Contribution::class.java)?.copy(id = it.id) } ?: emptyList())
            }
        awaitClose { l.remove() }
    }

    suspend fun recordContribution(chamaId: String, contribution: Contribution): FirestoreResult<String> = try {
        val ref = col(chamaId).document()
        ref.set(contribution.copy(id = ref.id)).await()
        updateMemberTotal(chamaId, contribution.memberId, contribution.amount)
        AuthResult.Success(ref.id)
    } catch (e: Exception) { AuthResult.Error("Failed to record: ${e.message}") }

    private suspend fun updateMemberTotal(chamaId: String, memberId: String, amount: Double) {
        try {
            val memberRef = db.collection("chamas").document(chamaId).collection("members").document(memberId)
            db.runTransaction { tx ->
                val current = tx.get(memberRef).getDouble("totalContributions") ?: 0.0
                tx.update(memberRef, "totalContributions", current + amount)
            }.await()
        } catch (_: Exception) {}
    }

    suspend fun updateContribution(chamaId: String, contribution: Contribution): FirestoreResult<Unit> = try {
        col(chamaId).document(contribution.id).set(contribution).await(); AuthResult.Success(Unit)
    } catch (e: Exception) { AuthResult.Error("Failed to update: ${e.message}") }
}

// ── Loans ─────────────────────────────────────────────────────────────────────

@Singleton
class LoansRepository @Inject constructor(private val db: FirebaseFirestore) {
    private fun col(chamaId: String) = db.collection("chamas").document(chamaId).collection("loans")

    fun getLoansFlow(chamaId: String): Flow<List<Loan>> = callbackFlow {
        val l = col(chamaId).orderBy("disbursedDate", Query.Direction.DESCENDING)
            .addSnapshotListener { snap, e ->
                if (e != null) { close(e); return@addSnapshotListener }
                trySend(snap?.documents?.mapNotNull { it.toObject(Loan::class.java)?.copy(id = it.id) } ?: emptyList())
            }
        awaitClose { l.remove() }
    }

    suspend fun applyForLoan(chamaId: String, loan: Loan): FirestoreResult<String> = try {
        val ref = col(chamaId).document()
        ref.set(loan.copy(id = ref.id, status = LoanStatus.PENDING)).await()
        AuthResult.Success(ref.id)
    } catch (e: Exception) { AuthResult.Error("Failed to submit: ${e.message}") }

    suspend fun approveLoan(chamaId: String, loanId: String): FirestoreResult<Unit> = try {
        col(chamaId).document(loanId).update("status", LoanStatus.ACTIVE.name).await()
        AuthResult.Success(Unit)
    } catch (e: Exception) { AuthResult.Error("Failed to approve: ${e.message}") }

    suspend fun rejectLoan(chamaId: String, loanId: String): FirestoreResult<Unit> = try {
        col(chamaId).document(loanId).update("status", LoanStatus.REJECTED.name).await()
        AuthResult.Success(Unit)
    } catch (e: Exception) { AuthResult.Error("Failed to reject: ${e.message}") }

    suspend fun recordRepayment(chamaId: String, loanId: String, amount: Double): FirestoreResult<Unit> = try {
        val ref = col(chamaId).document(loanId)
        db.runTransaction { tx ->
            val snap = tx.get(ref)
            val paid = (snap.getDouble("amountPaid") ?: 0.0) + amount
            val remaining = ((snap.getDouble("totalRepayable") ?: 0.0) - paid).coerceAtLeast(0.0)
            val status = if (remaining <= 0) LoanStatus.REPAID.name else snap.getString("status")
            tx.update(ref, mapOf("amountPaid" to paid, "remainingBalance" to remaining, "status" to status))
        }.await()
        AuthResult.Success(Unit)
    } catch (e: Exception) { AuthResult.Error("Failed to record repayment: ${e.message}") }
}

// ── Penalties ─────────────────────────────────────────────────────────────────

@Singleton
class PenaltiesRepository @Inject constructor(private val db: FirebaseFirestore) {
    private fun col(chamaId: String) = db.collection("chamas").document(chamaId).collection("penalties")

    fun getPenaltiesFlow(chamaId: String): Flow<List<Penalty>> = callbackFlow {
        val l = col(chamaId).orderBy("dateIssued", Query.Direction.DESCENDING)
            .addSnapshotListener { snap, e ->
                if (e != null) { close(e); return@addSnapshotListener }
                trySend(snap?.documents?.mapNotNull { it.toObject(Penalty::class.java)?.copy(id = it.id) } ?: emptyList())
            }
        awaitClose { l.remove() }
    }

    suspend fun issuePenalty(chamaId: String, penalty: Penalty): FirestoreResult<String> = try {
        val ref = col(chamaId).document()
        ref.set(penalty.copy(id = ref.id)).await(); AuthResult.Success(ref.id)
    } catch (e: Exception) { AuthResult.Error("Failed to issue: ${e.message}") }

    suspend fun markPenaltyPaid(chamaId: String, penaltyId: String): FirestoreResult<Unit> = try {
        col(chamaId).document(penaltyId).update("status", PenaltyStatus.PAID.name).await()
        AuthResult.Success(Unit)
    } catch (e: Exception) { AuthResult.Error("Failed to mark paid: ${e.message}") }
}
