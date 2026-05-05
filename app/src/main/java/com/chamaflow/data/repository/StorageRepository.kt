package com.chamaflow.data.repository

import android.net.Uri
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StorageRepository @Inject constructor(private val storage: FirebaseStorage) {

    suspend fun uploadImage(path: String, uri: Uri): FirestoreResult<String> = try {
        val ref = storage.reference.child(path)
        ref.putFile(uri).await()
        val downloadUrl = ref.downloadUrl.await()
        AuthResult.Success(downloadUrl.toString())
    } catch (e: Exception) {
        AuthResult.Error("Failed to upload image: ${e.message}")
    }
}
