package com.chamaflow.ui.screens.penalties

import androidx.compose.foundation.background
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.chamaflow.data.models.*
import com.chamaflow.ui.components.*
import com.chamaflow.ui.theme.*

private val samplePenalties = listOf(
    Penalty("1","m4","David Kipchoge", PenaltyReason.LATE_CONTRIBUTION, 500.0,"Jun 8, 2025", PenaltyStatus.UNPAID),
    Penalty("2","m4","David Kipchoge", PenaltyReason.LATE_LOAN_REPAYMENT, 1_000.0,"May 31, 2025", PenaltyStatus.UNPAID),
    Penalty("3","m6","Felix Odhiambo", PenaltyReason.MISSED_MEETING, 300.0,"May 31, 2025", PenaltyStatus.UNPAID),
    Penalty("4","m3","Catherine Mwangi", PenaltyReason.LATE_CONTRIBUTION, 500.0,"May 8, 2025", PenaltyStatus.PAID),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PenaltiesScreen(onBack: () -> Unit = {}) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Unpaid", "Paid", "All")
    val displayed = when (selectedTab) { 0 -> samplePenalties.filter { it.status == PenaltyStatus.UNPAID }; 1 -> samplePenalties.filter { it.status == PenaltyStatus.PAID }; else -> samplePenalties }
    val totalUnpaid = samplePenalties.filter { it.status == PenaltyStatus.UNPAID }.sumOf { it.amount }
    val totalCollected = samplePenalties.filter { it.status == PenaltyStatus.PAID }.sumOf { it.amount }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Penalties", fontWeight = FontWeight.Bold, color = Color.White) }, navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, null, tint = Color.White) } }, colors = TopAppBarDefaults.topAppBarColors(containerColor = ChamaRed)) },
        containerColor = ChamaBackground
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            Row(modifier = Modifier.fillMaxWidth().background(ChamaRed).padding(horizontal = 24.dp, vertical = 16.dp), horizontalArrangement = Arrangement.SpaceAround) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) { Text("${samplePenalties.count { it.status == PenaltyStatus.UNPAID }}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Color.White); Text("Unpaid", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.75f)) }
                VerticalDivider(color = Color.White.copy(alpha = 0.3f), modifier = Modifier.height(36.dp))
                Column(horizontalAlignment = Alignment.CenterHorizontally) { Text("${samplePenalties.count { it.status == PenaltyStatus.PAID }}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Color.White); Text("Cleared", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.75f)) }
            }
            Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatCard("Outstanding", "KES ${"%,.0f".format(totalUnpaid)}", Icons.Filled.Warning, ChamaRedLight, ChamaRed, modifier = Modifier.weight(1f))
                StatCard("Collected", "KES ${"%,.0f".format(totalCollected)}", Icons.Filled.CheckCircle, ChamaGreenLight, ChamaGreen, modifier = Modifier.weight(1f))
            }
            TabRow(selectedTabIndex = selectedTab, containerColor = ChamaSurface, contentColor = ChamaRed, modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp).clip(RoundedCornerShape(12.dp))) {
                tabs.forEachIndexed { i, tab -> Tab(selected = selectedTab == i, onClick = { selectedTab = i }, text = { Text(tab, style = MaterialTheme.typography.labelMedium, fontWeight = if (selectedTab == i) FontWeight.Bold else FontWeight.Normal) }) }
            }
            Spacer(Modifier.height(4.dp))
            if (displayed.isEmpty()) EmptyState(Icons.Outlined.CheckCircle, "No penalties", "All members are in good standing")
            else LazyColumn(contentPadding = PaddingValues(horizontal = 20.dp, vertical = 4.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(displayed, key = { it.id }) { penalty ->
                    var showDialog by remember { mutableStateOf(false) }
                    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = if (penalty.status == PenaltyStatus.UNPAID) ChamaRedLight else ChamaSurface), elevation = CardDefaults.cardElevation(1.dp)) {
                        Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Box(modifier = Modifier.size(40.dp).clip(RoundedCornerShape(10.dp)).background(if (penalty.status == PenaltyStatus.UNPAID) ChamaRed.copy(alpha = 0.15f) else ChamaGreenLight), contentAlignment = Alignment.Center) { Icon(if (penalty.status == PenaltyStatus.UNPAID) Icons.Filled.Warning else Icons.Filled.CheckCircle, null, tint = if (penalty.status == PenaltyStatus.UNPAID) ChamaRed else ChamaGreen, modifier = Modifier.size(20.dp)) }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(penalty.memberName, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                                Text(penalty.reason.name.replace("_", " ").lowercase().replaceFirstChar { it.uppercase() }, style = MaterialTheme.typography.bodySmall, color = ChamaTextSecondary)
                                Text(penalty.dateIssued, style = MaterialTheme.typography.labelSmall, color = ChamaTextMuted)
                            }
                            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text("KES ${"%,.0f".format(penalty.amount)}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = if (penalty.status == PenaltyStatus.UNPAID) ChamaRed else ChamaTextSecondary)
                                if (penalty.status == PenaltyStatus.UNPAID) TextButton(onClick = { showDialog = true }, contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp), modifier = Modifier.height(28.dp)) { Text("Mark Paid", style = MaterialTheme.typography.labelSmall, color = ChamaGreen) }
                                else StatusChip(status = "PAID")
                            }
                        }
                    }
                    if (showDialog) AlertDialog(onDismissRequest = { showDialog = false }, icon = { Icon(Icons.Filled.CheckCircle, null, tint = ChamaGreen) }, title = { Text("Mark as Paid") }, text = { Text("Confirm ${penalty.memberName} paid KES ${"%,.0f".format(penalty.amount)} penalty?") }, confirmButton = { Button(onClick = { showDialog = false }, colors = ButtonDefaults.buttonColors(containerColor = ChamaGreen)) { Text("Confirm") } }, dismissButton = { TextButton(onClick = { showDialog = false }) { Text("Cancel") } })
                }
                item { Spacer(Modifier.height(24.dp)) }
            }
        }
    }
}
