package com.chamaflow.ui.screens.meetings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.chamaflow.data.models.Meeting
import com.chamaflow.data.models.MeetingStatus
import com.chamaflow.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleMeetingSheet(onDismiss: () -> Unit, onSave: (Meeting) -> Unit, existingMeeting: Meeting? = null) {
    val isEditing = existingMeeting != null
    var title by remember { mutableStateOf(existingMeeting?.title ?: "") }
    var date by remember { mutableStateOf(existingMeeting?.date ?: "") }
    var time by remember { mutableStateOf(existingMeeting?.time ?: "") }
    var venue by remember { mutableStateOf(existingMeeting?.venue ?: "") }
    var agenda by remember { mutableStateOf(existingMeeting?.agenda ?: "") }
    val timeOptions = listOf("9:00 AM","10:00 AM","11:00 AM","12:00 PM","1:00 PM","2:00 PM","3:00 PM","4:00 PM","5:00 PM","6:00 PM")
    var timeDropdownOpen by remember { mutableStateOf(false) }
    val formValid = title.isNotEmpty() && date.isNotEmpty() && time.isNotEmpty() && venue.isNotEmpty()
    val meetingBlue = Color(0xFF0369A1)

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true), containerColor = ChamaSurface, shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)) {
        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp).padding(bottom = 32.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column { Text(if (isEditing) "Edit Meeting" else "Schedule Meeting", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold); Text("All members will be notified", style = MaterialTheme.typography.bodySmall, color = ChamaTextSecondary) }
                IconButton(onClick = onDismiss) { Icon(Icons.Filled.Close, null, tint = ChamaTextSecondary) }
            }
            HorizontalDivider(color = ChamaOutline)
            OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Meeting Title *") }, placeholder = { Text("e.g. June Monthly Meeting", color = ChamaTextMuted) }, leadingIcon = { Icon(Icons.Filled.EventNote, null, tint = ChamaTextSecondary) }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), singleLine = true, colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = meetingBlue, unfocusedBorderColor = ChamaOutline))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(value = date, onValueChange = { date = it }, label = { Text("Date *") }, placeholder = { Text("e.g. June 28, 2025", color = ChamaTextMuted) }, leadingIcon = { Icon(Icons.Filled.CalendarToday, null, tint = ChamaTextSecondary, modifier = Modifier.size(18.dp)) }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp), singleLine = true, colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = meetingBlue, unfocusedBorderColor = ChamaOutline))
                ExposedDropdownMenuBox(expanded = timeDropdownOpen, onExpandedChange = { timeDropdownOpen = it }, modifier = Modifier.weight(1f)) {
                    OutlinedTextField(value = time, onValueChange = { time = it }, label = { Text("Time *") }, leadingIcon = { Icon(Icons.Filled.Schedule, null, tint = ChamaTextSecondary, modifier = Modifier.size(18.dp)) }, trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = timeDropdownOpen) }, modifier = Modifier.fillMaxWidth().menuAnchor(), shape = RoundedCornerShape(12.dp), singleLine = true, colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = meetingBlue, unfocusedBorderColor = ChamaOutline))
                    ExposedDropdownMenu(expanded = timeDropdownOpen, onDismissRequest = { timeDropdownOpen = false }) {
                        timeOptions.forEach { opt -> DropdownMenuItem(text = { Text(opt) }, onClick = { time = opt; timeDropdownOpen = false }) }
                    }
                }
            }
            OutlinedTextField(value = venue, onValueChange = { venue = it }, label = { Text("Venue *") }, placeholder = { Text("e.g. Grace Hall, Westlands", color = ChamaTextMuted) }, leadingIcon = { Icon(Icons.Filled.LocationOn, null, tint = ChamaTextSecondary) }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), singleLine = true, colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = meetingBlue, unfocusedBorderColor = ChamaOutline))
            OutlinedTextField(value = agenda, onValueChange = { agenda = it }, label = { Text("Agenda (optional)") }, placeholder = { Text("1. Financial review\n2. Loan approvals\n3. AOB", color = ChamaTextMuted) }, leadingIcon = { Icon(Icons.Filled.List, null, tint = ChamaTextSecondary) }, modifier = Modifier.fillMaxWidth().heightIn(min = 100.dp), shape = RoundedCornerShape(12.dp), singleLine = false, minLines = 3, colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = meetingBlue, unfocusedBorderColor = ChamaOutline))
            Surface(shape = RoundedCornerShape(10.dp), color = Color(0xFFE0F2FE)) {
                Row(modifier = Modifier.fillMaxWidth().padding(12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.Top) {
                    Icon(Icons.Filled.Notifications, null, tint = meetingBlue, modifier = Modifier.size(16.dp))
                    Text("A push notification will be sent to all members when saved.", style = MaterialTheme.typography.bodySmall, color = Color(0xFF0C4A6E))
                }
            }
            Button(onClick = { if (formValid) { onSave(Meeting(id = existingMeeting?.id ?: "", chamaId = existingMeeting?.chamaId ?: "", title = title, date = date, time = time, venue = venue, agenda = agenda, status = MeetingStatus.UPCOMING)); onDismiss() } }, enabled = formValid, modifier = Modifier.fillMaxWidth().height(52.dp), shape = RoundedCornerShape(12.dp), colors = ButtonDefaults.buttonColors(containerColor = meetingBlue, disabledContainerColor = ChamaOutline)) {
                Icon(if (isEditing) Icons.Filled.Save else Icons.Filled.EventAvailable, null, modifier = Modifier.size(18.dp)); Spacer(Modifier.width(8.dp)); Text(if (isEditing) "Save Changes" else "Schedule Meeting", fontWeight = FontWeight.SemiBold)
            }
        }
    }
}
