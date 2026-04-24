package com.chamaflow.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.chamaflow.MainActivity
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class ChamaMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(msg: RemoteMessage) {
        val title = msg.notification?.title ?: msg.data["title"] ?: "ChamaFlow"
        val body  = msg.notification?.body  ?: msg.data["body"]  ?: ""
        val type  = msg.data["type"] ?: "general"
        showNotification(title, body, type)
    }

    override fun onNewToken(token: String) {
        // Save token to Firestore: db.collection("users").document(uid).update("fcmToken", token)
    }

    private fun showNotification(title: String, body: String, type: String) {
        val channelId = when (type) {
            "contribution" -> CHANNEL_CONTRIBUTIONS
            "loan"         -> CHANNEL_LOANS
            "meeting"      -> CHANNEL_MEETINGS
            "penalty"      -> CHANNEL_PENALTIES
            else           -> CHANNEL_GENERAL
        }
        createChannel(channelId)
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("notification_type", type)
        }
        val pending = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        val notif = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title).setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pending).setAutoCancel(true).build()
        (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
            .notify(System.currentTimeMillis().toInt(), notif)
    }

    private fun createChannel(channelId: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = when (channelId) {
                CHANNEL_CONTRIBUTIONS -> "Contributions"
                CHANNEL_LOANS         -> "Loans"
                CHANNEL_MEETINGS      -> "Meetings"
                CHANNEL_PENALTIES     -> "Penalties"
                else                  -> "General"
            }
            (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
                .createNotificationChannel(NotificationChannel(channelId, name, NotificationManager.IMPORTANCE_HIGH))
        }
    }

    companion object {
        const val CHANNEL_GENERAL       = "chamaflow_general"
        const val CHANNEL_CONTRIBUTIONS = "chamaflow_contributions"
        const val CHANNEL_LOANS         = "chamaflow_loans"
        const val CHANNEL_MEETINGS      = "chamaflow_meetings"
        const val CHANNEL_PENALTIES     = "chamaflow_penalties"
    }
}
