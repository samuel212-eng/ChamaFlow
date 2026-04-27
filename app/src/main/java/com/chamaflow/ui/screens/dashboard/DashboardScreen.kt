package com.chamaflow.ui.screens.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.hilt.navigation.compose.hiltViewModel
import com.chamaflow.data.models.*
import com.chamaflow.ui.components.*
import com.chamaflow.ui.theme.*
import com.chamaflow.ui.viewmodel.DashboardViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    chamaId: String,
    chamaName: String,
    userId: String,
    userRole: String,
    adminName: String,
    onNavigateToMembers: () -> Unit = {},
    onNavigateToContributions: () -> Unit = {},
    onNavigateToLoans: () -> Unit = {},
    onNavigateToMeetings: () -> Unit = {},
    onNavigateToReports: () -> Unit = {},
    onNavigateToNotifications: () -> Unit = {},
    onNavigateToProfile: () -> Unit = {},
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(chamaId) {
        viewModel.loadDashboard(chamaId, chamaName, userId, userRole)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "ChamaFlow",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            "Welcome back, $adminName",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateToNotifications) {
                        BadgedBox(badge = {
                            if (uiState.stats.overdueLoans > 0) Badge(containerColor = ChamaGold) {
                                Text("!", style = MaterialTheme.typography.labelSmall)
                            }
                        }) {
                            Icon(Icons.Outlined.Notifications, "Notifications", tint = Color.White)
                        }
                    }
                    IconButton(onClick = onNavigateToProfile) {
                        MemberAvatar(name = adminName, size = 34.dp, backgroundColor = Color.White.copy(alpha = 0.2f), textColor = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = ChamaBlue)
            )
        },
        containerColor = ChamaBackground
    ) { padding ->

        if (uiState.isLoading && uiState.stats.totalMembers == 0) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = ChamaBlue)
            }
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            item {
                BalanceBanner(
                    chamaName = uiState.chamaName,
                    balance = uiState.stats.currentGroupBalance,
                    memberCount = uiState.stats.totalMembers
                )
            }

            item {
                Spacer(modifier = Modifier.height(20.dp))
                QuickActionsRow(
                    onContribute = onNavigateToContributions,
                    onLoan = onNavigateToLoans,
                    onReport = onNavigateToReports,
                    onMeeting = onNavigateToMeetings
                )
            }

            item {
                Spacer(modifier = Modifier.height(8.dp))
                SectionHeader(title = "Overview", actionLabel = "Reports", onAction = onNavigateToReports)
                StatsGrid(stats = uiState.stats)
            }

            uiState.stats.upcomingMeeting?.let { meeting ->
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    SectionHeader(title = "Next Meeting", actionLabel = "All meetings", onAction = onNavigateToMeetings)
                    UpcomingMeetingCard(meeting = meeting, onClick = onNavigateToMeetings)
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }

            if (uiState.stats.overdueLoans > 0) {
                item {
                    OverdueAlertBanner(overdueLoans = uiState.stats.overdueLoans, onClick = onNavigateToLoans)
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }

            if (uiState.stats.recentContributions.isNotEmpty()) {
                item {
                    SectionHeader(title = "Recent Contributions", actionLabel = "See all", onAction = onNavigateToContributions)
                }
                items(uiState.stats.recentContributions.take(4)) { contribution ->
                    Box(modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)) {
                        ContributionRow(contribution = contribution)
                    }
                }
            } else {
                item {
                    SectionHeader(title = "Recent Contributions", actionLabel = "Record", onAction = onNavigateToContributions)
                    EmptyState(
                        icon = Icons.Outlined.Savings,
                        title = "No contributions yet",
                        subtitle = "Start recording monthly contributions",
                        actionLabel = "Record First Payment",
                        onAction = onNavigateToContributions
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(20.dp)) }
        }
    }
}

@Composable
private fun QuickActionsRow(onContribute: () -> Unit, onLoan: () -> Unit, onReport: () -> Unit, onMeeting: () -> Unit) {
    val actions = listOf(
        Triple(Icons.Filled.Add, "Contribute", onContribute),
        Triple(Icons.Filled.RequestPage, "Loan", onLoan),
        Triple(Icons.Filled.BarChart, "Reports", onReport),
        Triple(Icons.Filled.EventNote, "Meeting", onMeeting),
    )
    LazyRow(contentPadding = PaddingValues(horizontal = 20.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        items(actions) { (icon, label, action) ->
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.clickable { action() }
            ) {
                Box(
                    modifier = Modifier.size(56.dp).clip(RoundedCornerShape(16.dp)).background(ChamaBlueLight),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, label, tint = ChamaBlue, modifier = Modifier.size(24.dp))
                }
                Text(label, style = MaterialTheme.typography.labelSmall, color = ChamaTextSecondary, fontWeight = FontWeight.Medium)
            }
        }
    }
}

@Composable
private fun StatsGrid(stats: DashboardStats) {
    data class Item(val title: String, val value: String, val icon: ImageVector, val bg: Color, val tint: Color, val sub: String?)
    val items = listOf(
        Item("Total Members", "${stats.totalMembers}", Icons.Filled.Group, ChamaBlueLight, ChamaBlue, "Active"),
        Item("Total Savings", "KES ${fmt(stats.totalContributions)}", Icons.Filled.Savings, ChamaGreenLight, ChamaGreen, "This year"),
        Item("Loans Issued", "KES ${fmt(stats.totalLoansIssued)}", Icons.Filled.AccountBalance, ChamaGoldLight, ChamaGold, "${stats.activeLoans} active"),
        Item("Penalties", "KES ${fmt(stats.totalPenaltiesCollected)}", Icons.Filled.Warning, ChamaRedLight, ChamaRed, "Collected"),
    )
    Column(modifier = Modifier.padding(horizontal = 20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        items.chunked(2).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                row.forEach { item ->
                    StatCard(title = item.title, value = item.value, icon = item.icon, containerColor = item.bg, iconColor = item.tint, subtitle = item.sub, modifier = Modifier.weight(1f))
                }
                if (row.size == 1) Spacer(modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun UpcomingMeetingCard(meeting: Meeting, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp).clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = ChamaBlue),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth().padding(20.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Box(modifier = Modifier.size(52.dp).clip(RoundedCornerShape(12.dp)).background(Color.White.copy(alpha = 0.2f)), contentAlignment = Alignment.Center) {
                Icon(Icons.Filled.EventNote, null, tint = Color.White, modifier = Modifier.size(28.dp))
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(meeting.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = Color.White)
                Spacer(modifier = Modifier.height(2.dp))
                Text("${meeting.date} · ${meeting.time}", style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.85f))
                Text(meeting.venue, style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.7f))
            }
            Icon(Icons.Filled.ChevronRight, null, tint = Color.White.copy(alpha = 0.7f))
        }
    }
}

@Composable
private fun OverdueAlertBanner(overdueLoans: Int, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp).clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = ChamaRedLight)
    ) {
        Row(modifier = Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Icon(Icons.Filled.Warning, null, tint = ChamaRed)
            Text("$overdueLoans loan${if (overdueLoans > 1) "s" else ""} overdue — tap to review", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold, color = ChamaRed, modifier = Modifier.weight(1f))
            Icon(Icons.Filled.ChevronRight, null, tint = ChamaRed)
        }
    }
}

private fun fmt(amount: Double): String = when {
    amount >= 1_000_000 -> "${"%.1f".format(amount / 1_000_000)}M"
    amount >= 1_000 -> "${"%.0f".format(amount / 1_000)}K"
    else -> "%,.0f".format(amount)
}
