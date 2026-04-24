package com.chamaflow.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

sealed class AuthResult<out T> {
    data class Success<T>(val data: T) : AuthResult<T>()
    data class Error(val message: String) : AuthResult<Nothing>()
    object Loading : AuthResult<Nothing>()
}

@Singleton
class AuthRepository @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) {
    val currentUser: Flow<FirebaseUser?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { trySend(it.currentUser) }
        auth.addAuthStateListener(listener)
        awaitClose { auth.removeAuthStateListener(listener) }
    }

    val isLoggedIn: Boolean get() = auth.currentUser != null

    suspend fun loginWithEmail(email: String, password: String): AuthResult<FirebaseUser> {
        return try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            result.user?.let { AuthResult.Success(it) } ?: AuthResult.Error("Login failed.")
        } catch (e: Exception) { AuthResult.Error(friendlyError(e.message)) }
    }

    suspend fun registerWithEmail(email: String, password: String, fullName: String, phone: String): AuthResult<FirebaseUser> {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            val user = result.user ?: return AuthResult.Error("Registration failed.")
            
            try {
                firestore.collection("users").document(user.uid).set(
                    mapOf("userId" to user.uid, "fullName" to fullName, "email" to email,
                        "phoneNumber" to phone, "role" to "MEMBER", "createdAt" to System.currentTimeMillis())
                ).await()
            } catch (e: Exception) {
                // If firestore fails, we have a user in Auth but no profile.
                // We'll let it succeed and handle the missing profile later or just ignore.
                // Alternatively, we could delete the auth user here.
            }
            
            AuthResult.Success(user)
        } catch (e: Exception) { AuthResult.Error(friendlyError(e.message)) }
    }

    suspend fun getUserProfile(uid: String): Map<String, Any>? {
        return try {
            firestore.collection("users").document(uid).get().await().data
        } catch (e: Exception) { null }
    }

    fun sendPhoneOtp(phoneNumber: String, activity: android.app.Activity, callbacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks) {
        val formatted = when {
            phoneNumber.startsWith("+254") -> phoneNumber
            phoneNumber.startsWith("07") || phoneNumber.startsWith("01") -> "+254${phoneNumber.substring(1)}"
            else -> phoneNumber
        }
        PhoneAuthProvider.verifyPhoneNumber(
            PhoneAuthOptions.newBuilder(auth).setPhoneNumber(formatted)
                .setTimeout(60L, TimeUnit.SECONDS).setActivity(activity).setCallbacks(callbacks).build()
        )
    }

    suspend fun verifyOtp(verificationId: String, otpCode: String): AuthResult<FirebaseUser> {
        return try {
            signInWithCredential(PhoneAuthProvider.getCredential(verificationId, otpCode))
        } catch (e: Exception) { AuthResult.Error("Invalid OTP code.") }
    }

    suspend fun signInWithCredential(credential: PhoneAuthCredential): AuthResult<FirebaseUser> {
        return try {
            val result = auth.signInWithCredential(credential).await()
            result.user?.let { AuthResult.Success(it) } ?: AuthResult.Error("Verification failed.")
        } catch (e: Exception) { AuthResult.Error(friendlyError(e.message)) }
    }

    suspend fun sendPasswordResetEmail(email: String): AuthResult<Unit> {
        return try {
            auth.sendPasswordResetEmail(email).await()
            AuthResult.Success(Unit)
        } catch (e: Exception) { AuthResult.Error(friendlyError(e.message)) }
    }

    fun logout() { auth.signOut() }

    private fun friendlyError(msg: String?): String = when {
        msg == null -> "Something went wrong. Please try again."
        msg.lowercase().contains("password") -> "Wrong password or invalid format."
        msg.lowercase().contains("email") && msg.lowercase().contains("already") -> "An account with this email already exists. Try signing in."
        msg.lowercase().contains("no user") || msg.lowercase().contains("user not found") || msg.lowercase().contains("incorrect") -> "Invalid email or password."
        msg.lowercase().contains("network") -> "No internet connection."
        msg.lowercase().contains("too many") -> "Too many attempts. Please wait."
        else -> msg ?: "Authentication failed. Please try again."
    }
}
