package com.chamaflow.ui.screens.members

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import com.chamaflow.data.models.Member
import com.chamaflow.ui.components.*
import com.chamaflow.ui.theme.*
import com.chamaflow.ui.viewmodel.MembersViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MemberProfileScreen(
    chamaId: String = "",
    memberId: String = "", 
    onBack: () -> Unit = {}, 
    viewModel: MembersViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val member = remember(uiState.members, memberId) { 
        uiState.members.find { it.id == memberId } 
    }
    
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Overview", "Contributions", "Loans")

    LaunchedEffect(chamaId) {
        if (chamaId.isNotEmpty()) viewModel.loadMembers(chamaId)
    }

    Scaffold(
        topBar = { 
            TopAppBar(
                title = { Text("Member Profile", fontWeight = FontWeight.Bold, color = Color.White) }, 
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, null, tint = Color.White) } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = ChamaBlue)
            ) 
        },
        containerColor = ChamaBackground
    ) { padding ->
        if (member == null) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                if (uiState.isLoading) CircularProgressIndicator(color = ChamaBlue)
                else Text("Member not found", color = ChamaTextSecondary)
            }
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize().padding(padding)) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().background(ChamaBlue).padding(horizontal = 24.dp, vertical = 28.dp), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            MemberAvatar(name = member.fullName, size = 80.dp, backgroundColor = Color.White.copy(alpha = 0.2f), textColor = Color.White)
                            Text(member.fullName, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Color.White)
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                StatusChip(status = member.role.name)
                                StatusChip(status = member.status.name)
                            }
                        }
                    }
                }
                item {
                    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        MiniStat("Total Saved", "KES ${"%,.0f".format(member.totalContributions)}", ChamaGreen, Modifier.weight(1f))
                        MiniStat("Loan Balance", if (member.loanBalance > 0) "KES ${"%,.0f".format(member.loanBalance)}" else "None", if (member.loanBalance > 0) ChamaRed else ChamaGreen, Modifier.weight(1f))
                        MiniStat("Penalties", if (member.penaltiesOwed > 0) "KES ${"%,.0f".format(member.penaltiesOwed)}" else "None", if (member.penaltiesOwed > 0) ChamaRed else ChamaGreen, Modifier.weight(1f))
                    }
                }
                item {
                    TabRow(selectedTabIndex = selectedTab, containerColor = ChamaSurface, contentColor = ChamaBlue, modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp).clip(RoundedCornerShape(12.dp))) {
                        tabs.forEachIndexed { i, tab -> Tab(selected = selectedTab == i, onClick = { selectedTab = i }, text = { Text(tab, style = MaterialTheme.typography.labelMedium, fontWeight = if (selectedTab == i) FontWeight.Bold else FontWeight.Normal) }) }
                    }
                    Spacer(Modifier.height(8.dp))
                    Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = ChamaSurface), elevation = CardDefaults.cardElevation(2.dp)) {
                        Column(modifier = Modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            Text("Contact Details", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                            DetailRow(Icons.Filled.Phone, "Phone", member.phoneNumber)
                            HorizontalDivider(color = ChamaOutline)
                            DetailRow(Icons.Filled.Email, "Email", member.email.ifEmpty { "No email provided" })
                            HorizontalDivider(color = ChamaOutline)
                            DetailRow(Icons.Filled.CalendarToday, "Join Date", member.joinDate.ifEmpty { "N/A" })
                        }
                    }
                    Spacer(Modifier.height(32.dp))
                }
            }
        }
    }
}

@Composable
private fun MiniStat(label: String, value: String, color: Color, modifier: Modifier) {
    Card(modifier = modifier, shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = ChamaSurface), elevation = CardDefaults.cardElevation(1.dp)) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(label, style = MaterialTheme.typography.labelSmall, color = ChamaTextSecondary)
            Text(value, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = color)
        }
    }
}

@Composable
private fun DetailRow(icon: ImageVector, label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Icon(icon, null, tint = ChamaTextSecondary, modifier = Modifier.size(18.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(label, style = MaterialTheme.typography.labelSmall, color = ChamaTextSecondary)
            Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
        }
    }
}
