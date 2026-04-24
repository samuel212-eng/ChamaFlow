package com.chamaflow.data.repository

import com.chamaflow.data.models.Meeting
import com.chamaflow.data.models.MeetingStatus
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MeetingsRepository @Inject constructor(private val db: FirebaseFirestore) {
    private fun col(chamaId: String) = db.collection("chamas").document(chamaId).collection("meetings")

    fun getMeetingsFlow(chamaId: String): Flow<List<Meeting>> = callbackFlow {
        val l = col(chamaId).orderBy("date", Query.Direction.DESCENDING)
            .addSnapshotListener { snap, e ->
                if (e != null) { close(e); return@addSnapshotListener }
                trySend(snap?.documents?.mapNotNull { it.toObject(Meeting::class.java)?.copy(id = it.id) } ?: emptyList())
            }
        awaitClose { l.remove() }
    }

    suspend fun scheduleMeeting(chamaId: String, meeting: Meeting): AuthResult<String> = try {
        val ref = col(chamaId).document()
        ref.set(meeting.copy(id = ref.id, chamaId = chamaId)).await()
        AuthResult.Success(ref.id)
    } catch (e: Exception) { AuthResult.Error("Failed to schedule: ${e.message}") }

    suspend fun updateMeeting(chamaId: String, meeting: Meeting): AuthResult<Unit> = try {
        col(chamaId).document(meeting.id).set(meeting).await(); AuthResult.Success(Unit)
    } catch (e: Exception) { AuthResult.Error("Failed to update: ${e.message}") }

    suspend fun markAttendance(chamaId: String, meetingId: String, memberId: String, attended: Boolean): AuthResult<Unit> = try {
        val field = if (attended) FieldValue.arrayUnion(memberId) else FieldValue.arrayRemove(memberId)
        col(chamaId).document(meetingId).update("attendees", field).await()
        AuthResult.Success(Unit)
    } catch (e: Exception) { AuthResult.Error("Failed to update attendance: ${e.message}") }

    suspend fun completeMeeting(chamaId: String, meetingId: String, notes: String, decisions: String): AuthResult<Unit> = try {
        col(chamaId).document(meetingId).update(
            mapOf("status" to MeetingStatus.COMPLETED.name, "notes" to notes, "decisions" to decisions)
        ).await()
        AuthResult.Success(Unit)
    } catch (e: Exception) { AuthResult.Error("Failed to complete: ${e.message}") }
}
