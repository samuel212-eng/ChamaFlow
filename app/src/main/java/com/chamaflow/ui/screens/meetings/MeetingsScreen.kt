package com.chamaflow.ui.screens.meetings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import com.chamaflow.ui.viewmodel.MeetingsViewModel

private val meetingBlue = Color(0xFF0369A1)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MeetingsScreen(
    chamaId: String,
    onMeetingClick: (String) -> Unit = {},
    viewModel: MeetingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val tabs = listOf("Upcoming", "Past")

    LaunchedEffect(chamaId) { viewModel.loadMeetings(chamaId) }

    LaunchedEffect(uiState.successMessage, uiState.errorMessage) {
        val msg = uiState.successMessage ?: uiState.errorMessage
        msg?.let { snackbarHostState.showSnackbar(it); viewModel.clearMessages() }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Meetings", fontWeight = FontWeight.Bold, color = Color.White) },
                actions = { IconButton(onClick = {}) { Icon(Icons.Outlined.CalendarMonth, null, tint = Color.White) } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = meetingBlue)
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { viewModel.showSheet() },
                icon = { Icon(Icons.Filled.Add, null) },
                text = { Text("Schedule") },
                containerColor = meetingBlue,
                contentColor = Color.White
            )
        },
        snackbarHost = {
            SnackbarHost(snackbarHostState) { data ->
                Snackbar(snackbarData = data, containerColor = if (uiState.errorMessage != null) ChamaRed else ChamaGreen, contentColor = Color.White)
            }
        },
        containerColor = ChamaBackground
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {

            // Next meeting hero
            uiState.nextMeeting?.let { NextMeetingHero(meeting = it) }

            // Tabs
            TabRow(
                selectedTabIndex = uiState.selectedTabIndex,
                containerColor = ChamaSurface,
                contentColor = meetingBlue,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp).clip(RoundedCornerShape(12.dp))
            ) {
                tabs.forEachIndexed { index, tab ->
                    Tab(selected = uiState.selectedTabIndex == index, onClick = { viewModel.setTab(index) },
                        text = { Text(tab, style = MaterialTheme.typography.labelMedium, fontWeight = if (uiState.selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal) })
                }
            }

            when {
                uiState.isLoading -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = meetingBlue)
                }
                uiState.displayedMeetings.isEmpty() -> EmptyState(
                    icon = Icons.Outlined.EventNote,
                    title = if (uiState.selectedTabIndex == 0) "No upcoming meetings" else "No past meetings",
                    subtitle = if (uiState.selectedTabIndex == 0) "Schedule your next meeting" else "Past meeting records will appear here",
                    actionLabel = if (uiState.selectedTabIndex == 0) "Schedule Now" else null,
                    onAction = { viewModel.showSheet() }
                )
                else -> LazyColumn(
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(uiState.displayedMeetings, key = { it.id }) { meeting ->
                        MeetingCard(meeting = meeting, onClick = { onMeetingClick(meeting.id) })
                    }
                    item { Spacer(modifier = Modifier.height(80.dp)) }
                }
            }
        }
    }

    if (uiState.showScheduleSheet) {
        ScheduleMeetingSheet(
            onDismiss = { viewModel.hideSheet() },
            onSave = { meeting -> viewModel.scheduleMeeting(chamaId, meeting) }
        )
    }
}

@Composable
private fun NextMeetingHero(meeting: Meeting) {
    Box(modifier = Modifier.fillMaxWidth().background(meetingBlue).padding(horizontal = 24.dp, vertical = 20.dp)) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("Next Meeting", style = MaterialTheme.typography.labelMedium, color = Color.White.copy(alpha = 0.75f), fontWeight = FontWeight.SemiBold)
            Text(meeting.title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Color.White)
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically) {
                MeetingInfoRow(Icons.Filled.CalendarToday, meeting.date)
                MeetingInfoRow(Icons.Filled.Schedule, meeting.time)
            }
            MeetingInfoRow(Icons.Filled.LocationOn, meeting.venue)
        }
    }
}

@Composable
private fun MeetingCard(meeting: Meeting, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = ChamaSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(meeting.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                StatusChip(status = meeting.status.name)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically) {
                MeetingInfoRow(Icons.Filled.CalendarToday, meeting.date)
                MeetingInfoRow(Icons.Filled.Schedule, meeting.time)
            }
            MeetingInfoRow(Icons.Filled.LocationOn, meeting.venue)
            if (meeting.agenda.isNotEmpty()) {
                HorizontalDivider(color = ChamaOutline)
                Text("Agenda", style = MaterialTheme.typography.labelSmall, color = ChamaTextSecondary, fontWeight = FontWeight.SemiBold)
                Text(meeting.agenda.lines().take(2).joinToString("\n"), style = MaterialTheme.typography.bodySmall, color = ChamaTextSecondary)
            }
            if (meeting.status == MeetingStatus.COMPLETED && meeting.decisions.isNotEmpty()) {
                HorizontalDivider(color = ChamaOutline)
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Icon(Icons.Filled.Gavel, null, tint = ChamaGreen, modifier = Modifier.size(14.dp))
                    Text("Decisions recorded", style = MaterialTheme.typography.labelSmall, color = ChamaGreen, fontWeight = FontWeight.SemiBold)
                }
            }
            if (meeting.attendees.isNotEmpty()) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Icon(Icons.Filled.Group, null, tint = ChamaTextSecondary, modifier = Modifier.size(14.dp))
                    Text("${meeting.attendees.size} members attended", style = MaterialTheme.typography.labelSmall, color = ChamaTextSecondary)
                }
            }
        }
    }
}

@Composable
private fun MeetingInfoRow(icon: ImageVector, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        Icon(icon, null, tint = ChamaTextSecondary, modifier = Modifier.size(14.dp))
        Text(text, style = MaterialTheme.typography.bodySmall, color = ChamaTextSecondary)
    }
}
