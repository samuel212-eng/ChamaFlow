package com.chamaflow.ui.screens.members

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.chamaflow.data.models.*
import com.chamaflow.ui.components.*
import com.chamaflow.ui.theme.*
import com.chamaflow.ui.viewmodel.MemberDetailViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MemberProfileScreen(
    chamaId: String,
    memberId: String, 
    onBack: () -> Unit = {}, 
    viewModel: MemberDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Overview", "Transactions", "Loans")

    LaunchedEffect(chamaId, memberId) {
        viewModel.loadMemberDetails(chamaId, memberId)
    }

    Scaffold(
        topBar = { 
            TopAppBar(
                title = { Text("Member Profile", fontWeight = FontWeight.ExtraBold, color = Color.White) }, 
                navigationIcon = { 
                    IconButton(onClick = onBack) { 
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.White) 
                    } 
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Primary)
            ) 
        },
        containerColor = Background
    ) { padding ->
        val member = uiState.member
        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Secondary)
            }
        } else if (member == null) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                EmptyState(Icons.Outlined.PersonOff, "Member not found", "We couldn't retrieve this member's details.")
            }
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize().padding(padding)) {
                item {
                    // Modern Profile Header
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Brush.verticalGradient(listOf(Primary, Color(0xFF1E293B))))
                            .padding(horizontal = 24.dp, vertical = 32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            Box(contentAlignment = Alignment.BottomEnd) {
                                MemberAvatar(
                                    name = member.fullName, 
                                    size = 100.dp, 
                                    backgroundColor = Color.White.copy(alpha = 0.2f), 
                                    textColor = Color.White
                                )
                                Box(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .clip(CircleShape)
                                        .background(Accent)
                                        .padding(4.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Filled.Verified, null, tint = Color.White, modifier = Modifier.size(16.dp))
                                }
                            }
                            
                            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    member.fullName, 
                                    style = MaterialTheme.typography.headlineSmall, 
                                    fontWeight = FontWeight.ExtraBold, 
                                    color = Color.White
                                )
                                Text(
                                    member.email.ifEmpty { member.phoneNumber }, 
                                    style = MaterialTheme.typography.bodyMedium, 
                                    color = Color.White.copy(alpha = 0.6f)
                                )
                            }
                            
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                StatusChip(status = member.role.name)
                                StatusChip(status = member.status.name)
                            }
                        }
                    }
                }

                item {
                    // Modern Stats Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 20.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        ProfileMiniStat("Total Saved", "KES ${"%,.0f".format(member.totalContributions)}", Accent, Modifier.weight(1f))
                        ProfileMiniStat("Loans", "KES ${"%,.0f".format(member.loanBalance)}", Warning, Modifier.weight(1f))
                        ProfileMiniStat("Welfare", "KES ${"%,.0f".format(member.welfareBalance)}", Secondary, Modifier.weight(1f))
                    }
                }

                item {
                    TabRow(
                        selectedTabIndex = selectedTab,
                        containerColor = Color.White,
                        contentColor = Secondary,
                        modifier = Modifier.padding(horizontal = 20.dp).clip(RoundedCornerShape(12.dp)),
                        divider = {},
                        indicator = { tabPositions ->
                            TabRowDefaults.SecondaryIndicator(
                                Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                                color = Secondary,
                                height = 3.dp
                            )
                        }
                    ) {
                        tabs.forEachIndexed { i, tab -> 
                            Tab(
                                selected = selectedTab == i, 
                                onClick = { selectedTab = i }, 
                                text = { 
                                    Text(
                                        tab, 
                                        style = MaterialTheme.typography.labelLarge, 
                                        fontWeight = if (selectedTab == i) FontWeight.ExtraBold else FontWeight.Bold,
                                        color = if (selectedTab == i) Secondary else TextSecondary
                                    ) 
                                }
                            ) 
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                }

                when (selectedTab) {
                    0 -> item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp)
                                .shadow(8.dp, RoundedCornerShape(24.dp), spotColor = Color.Black.copy(alpha = 0.1f)),
                            shape = RoundedCornerShape(24.dp),
                            colors = CardDefaults.cardColors(containerColor = Surface)
                        ) {
                            Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
                                Text("Personal Information", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold, color = Primary)
                                DetailItem(Icons.Filled.Phone, "Mobile Number", member.phoneNumber)
                                DetailItem(Icons.Filled.Email, "Email Address", member.email.ifEmpty { "Not provided" })
                                DetailItem(Icons.Filled.Badge, "National ID", member.nationalId.ifEmpty { "Not provided" })
                                DetailItem(Icons.Filled.CalendarMonth, "Member Since", member.joinDate)
                            }
                        }
                    }
                    1 -> {
                        if (uiState.contributions.isEmpty()) {
                            item { EmptyTabContent(Icons.Outlined.History, "No transactions found") }
                        } else {
                            items(uiState.contributions) { contribution ->
                                Box(modifier = Modifier.padding(bottom = 8.dp)) {
                                    ContributionRow(contribution = contribution)
                                }
                            }
                        }
                    }
                    2 -> {
                        if (uiState.loans.isEmpty()) {
                            item { EmptyTabContent(Icons.Outlined.AccountBalanceWallet, "No active loans") }
                        } else {
                            items(uiState.loans) { loan ->
                                Box(modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)) {
                                    LoanProgressCard(loan = loan)
                                }
                            }
                        }
                    }
                }
                item { Spacer(Modifier.height(100.dp)) }
            }
        }
    }
}

@Composable
private fun ProfileMiniStat(label: String, value: String, color: Color, modifier: Modifier) {
    Card(
        modifier = modifier.shadow(4.dp, RoundedCornerShape(16.dp), spotColor = color.copy(alpha = 0.1f)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Surface)
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(label, style = MaterialTheme.typography.labelSmall, color = TextSecondary, fontWeight = FontWeight.Bold)
            Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.ExtraBold, color = color)
        }
    }
}

@Composable
private fun DetailItem(icon: ImageVector, label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        Box(
            modifier = Modifier.size(40.dp).background(Color(0xFFF1F5F9), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, tint = Primary, modifier = Modifier.size(18.dp))
        }
        Column {
            Text(label, style = MaterialTheme.typography.labelSmall, color = TextSecondary, fontWeight = FontWeight.Bold)
            Text(value, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold, color = Primary)
        }
    }
}

@Composable
private fun EmptyTabContent(icon: ImageVector, message: String) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(icon, null, tint = TextMuted, modifier = Modifier.size(40.dp))
        Text(message, style = MaterialTheme.typography.bodyMedium, color = TextSecondary, fontWeight = FontWeight.Bold)
    }
}
