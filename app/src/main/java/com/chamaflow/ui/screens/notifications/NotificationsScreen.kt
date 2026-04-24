package com.chamaflow.ui.screens.notifications

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.chamaflow.ui.theme.*

// ─── Model ────────────────────────────────────────────────────────────────────

data class AppNotification(
    val id: String,
    val type: NotificationType,
    val title: String,
    val body: String,
    val timeAgo: String,
    val isRead: Boolean = false
)

enum class NotificationType { CONTRIBUTION, LOAN, MEETING, PENALTY, GENERAL }

// ─── Sample notifications ─────────────────────────────────────────────────────

private val sampleNotifications = listOf(
    AppNotification("1", NotificationType.LOAN, "Loan Request Pending", "Isaiah Waweru has applied for a KES 50,000 loan. Review required.", "2 min ago", false),
    AppNotification("2", NotificationType.CONTRIBUTION, "Payment Received", "Brian Otieno paid KES 5,000 contribution for June 2025. Ref: QHX2Y3ABCD", "1 hr ago", false),
    AppNotification("3", NotificationType.MEETING, "Meeting Reminder", "June Monthly Meeting is tomorrow at 2:00 PM — Grace Hall, Westlands.", "3 hrs ago", false),
    AppNotification("4", NotificationType.PENALTY, "Overdue Penalty", "David Kipchoge has KES 1,500 in unpaid penalties. Follow up required.", "Yesterday", true),
    AppNotification("5", NotificationType.CONTRIBUTION, "Overdue Contribution", "Felix Odhiambo has not paid the June contribution. Due date was June 5.", "Yesterday", true),
    AppNotification("6", NotificationType.LOAN, "Loan Approved", "Grace Njoroge's loan of KES 15,000 was approved and disbursed.", "2 days ago", true),
    AppNotification("7", NotificationType.GENERAL, "New Member Joined", "Hassan Abdi has joined Pamoja Savings Group via invite code.", "3 days ago", true),
    AppNotification("8", NotificationType.CONTRIBUTION, "Monthly Reminder", "June contributions are due by June 5. 3 members yet to pay.", "4 days ago", true),
)

// ─── Screen ───────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(
    onBack: () -> Unit = {}
) {
    var notifications by remember { mutableStateOf(sampleNotifications) }
    val unreadCount = notifications.count { !it.isRead }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Notifications", fontWeight = FontWeight.Bold, color = Color.White)
                        if (unreadCount > 0) {
                            Badge(containerColor = ChamaGold) {
                                Text("$unreadCount", style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }
                },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, null, tint = Color.White) } },
                actions = {
                    if (unreadCount > 0) {
                        TextButton(onClick = {
                            notifications = notifications.map { it.copy(isRead = true) }
                        }) {
                            Text("Mark all read", color = Color.White.copy(alpha = 0.9f), style = MaterialTheme.typography.labelMedium)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = ChamaBlue)
            )
        },
        containerColor = ChamaBackground
    ) { padding ->
        if (notifications.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(modifier = Modifier.size(80.dp).clip(CircleShape).background(ChamaBlueLight), contentAlignment = Alignment.Center) {
                        Icon(Icons.Outlined.Notifications, null, tint = ChamaBlue, modifier = Modifier.size(40.dp))
                    }
                    Text("All caught up!", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Text("No new notifications", style = MaterialTheme.typography.bodySmall, color = ChamaTextSecondary)
                }
            }
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            // Unread section
            val unread = notifications.filter { !it.isRead }
            if (unread.isNotEmpty()) {
                item {
                    Text("New", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = ChamaTextSecondary,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp))
                }
                items(unread, key = { it.id }) { notif ->
                    NotificationItem(
                        notification = notif,
                        onClick = {
                            notifications = notifications.map { if (it.id == notif.id) it.copy(isRead = true) else it }
                        }
                    )
                }
            }

            // Read section
            val read = notifications.filter { it.isRead }
            if (read.isNotEmpty()) {
                item {
                    Text("Earlier", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = ChamaTextSecondary,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp))
                }
                items(read, key = { it.id }) { notif ->
                    NotificationItem(notification = notif, onClick = {})
                }
            }

            item { Spacer(modifier = Modifier.height(20.dp)) }
        }
    }
}

// ─── Notification Item ────────────────────────────────────────────────────────

@Composable
private fun NotificationItem(notification: AppNotification, onClick: () -> Unit) {
    val (icon, iconBg, iconTint) = notifIconStyle(notification.type)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(if (!notification.isRead) ChamaBlueLight.copy(alpha = 0.3f) else ChamaSurface)
            .clickable { onClick() }
            .padding(horizontal = 20.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalAlignment = Alignment.Top
    ) {
        // Icon
        Box(
            modifier = Modifier.size(44.dp).clip(RoundedCornerShape(12.dp)).background(iconBg),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, tint = iconTint, modifier = Modifier.size(22.dp))
        }

        // Content
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
                Text(notification.title, style = MaterialTheme.typography.bodyMedium, fontWeight = if (!notification.isRead) FontWeight.Bold else FontWeight.SemiBold, modifier = Modifier.weight(1f))
                Text(notification.timeAgo, style = MaterialTheme.typography.labelSmall, color = ChamaTextMuted)
            }
            Text(notification.body, style = MaterialTheme.typography.bodySmall, color = ChamaTextSecondary, maxLines = 2, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
        }

        // Unread dot
        if (!notification.isRead) {
            Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(ChamaBlue).align(Alignment.Top))
        }
    }

    HorizontalDivider(color = ChamaOutline.copy(alpha = 0.5f), modifier = Modifier.padding(horizontal = 20.dp))
}

private fun notifIconStyle(type: NotificationType): Triple<ImageVector, Color, Color> = when (type) {
    NotificationType.CONTRIBUTION -> Triple(Icons.Filled.Savings, ChamaGreenLight, ChamaGreen)
    NotificationType.LOAN         -> Triple(Icons.Filled.AccountBalance, Color(0xFFEDE9FE), Color(0xFF6D28D9))
    NotificationType.MEETING      -> Triple(Icons.Filled.EventNote, Color(0xFFE0F2FE), Color(0xFF0369A1))
    NotificationType.PENALTY      -> Triple(Icons.Filled.Warning, ChamaRedLight, ChamaRed)
    NotificationType.GENERAL      -> Triple(Icons.Filled.Info, ChamaBlueLight, ChamaBlue)
}
