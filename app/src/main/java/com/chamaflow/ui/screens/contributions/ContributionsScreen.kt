package com.chamaflow.ui.screens.contributions

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.chamaflow.data.models.*
import com.chamaflow.ui.components.*
import com.chamaflow.ui.theme.*
import com.chamaflow.ui.viewmodel.ContributionsViewModel
import com.chamaflow.ui.viewmodel.MembersViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContributionsScreen(
    chamaId: String,
    defaultContributionAmount: Double = 5_000.0,
    viewModel: ContributionsViewModel = hiltViewModel(),
    membersViewModel: MembersViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val membersState by membersViewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var showSheet by remember { mutableStateOf(false) }

    val months = remember {
        (5 downTo 0).map { offset ->
            LocalDate.now().minusMonths(offset.toLong())
                .format(DateTimeFormatter.ofPattern("MMM yyyy"))
        }
    }
    val tabs = listOf("All", "Paid", "Unpaid", "Overdue")

    LaunchedEffect(chamaId) { 
        viewModel.loadContributions(chamaId)
        membersViewModel.loadMembers(chamaId)
    }

    LaunchedEffect(uiState.successMessage, uiState.errorMessage) {
        val msg = uiState.successMessage ?: uiState.errorMessage
        msg?.let { snackbarHostState.showSnackbar(it); viewModel.clearMessages() }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Savings & Contributions", fontWeight = FontWeight.ExtraBold, color = Color.White) },
                actions = {
                    IconButton(onClick = {}) { Icon(Icons.Outlined.FileDownload, "Export", tint = Color.White) }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Primary)
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showSheet = true },
                icon = { Icon(Icons.Filled.Add, null) },
                text = { Text("Record Payment", fontWeight = FontWeight.Bold) },
                containerColor = Secondary,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp)
            )
        },
        snackbarHost = {
            SnackbarHost(snackbarHostState) { data ->
                Snackbar(
                    snackbarData = data, 
                    containerColor = if (uiState.errorMessage != null) Error else Accent, 
                    contentColor = Color.White,
                    shape = RoundedCornerShape(12.dp)
                )
            }
        },
        containerColor = Background
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {

            // Month selector
            Box(modifier = Modifier.fillMaxWidth().background(Primary)) {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp), 
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(months) { month ->
                        val isSelected = month == uiState.selectedMonth
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) Color.White else Color.White.copy(alpha = 0.1f),
                            modifier = Modifier.clickable { viewModel.changeMonth(chamaId, month) }
                        ) {
                            Text(
                                month,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
                                color = if (isSelected) Primary else Color.White.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
            }

            // Summary card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .shadow(elevation = 12.dp, shape = RoundedCornerShape(24.dp), spotColor = Accent.copy(alpha = 0.3f)),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Surface),
            ) {
                Column(modifier = Modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    val progress = if (uiState.totalCount > 0) uiState.paidCount.toFloat() / uiState.totalCount else 0f
                    val expectedTotal = uiState.totalCount * defaultContributionAmount

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Column {
                            Text("Total Collected", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
                            Text("KES ${"%,.0f".format(uiState.amountCollected)}", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold, color = Accent)
                        }
                        Surface(color = Color(0xFFF1F5F9), shape = RoundedCornerShape(8.dp)) {
                            Text(
                                "Goal: KES ${"%,.0f".format(expectedTotal)}", 
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelSmall, 
                                fontWeight = FontWeight.Bold,
                                color = Primary
                            )
                        }
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("${uiState.paidCount} of ${uiState.totalCount} members paid", style = MaterialTheme.typography.bodySmall, color = TextSecondary, fontWeight = FontWeight.Medium)
                            Text("${(progress * 100).toInt()}%", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.ExtraBold, color = Accent)
                        }
                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier.fillMaxWidth().height(10.dp).clip(CircleShape),
                            color = Accent,
                            trackColor = Color(0xFFF1F5F9)
                        )
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        SummaryPill("Paid", uiState.paidCount, Color(0xFFDCFCE7), Color(0xFF059669), Modifier.weight(1f))
                        SummaryPill("Partial", uiState.partialCount, Color(0xFFFFEDD5), Color(0xFFD97706), Modifier.weight(1f))
                        SummaryPill("Overdue", uiState.overdueCount, Color(0xFFFEE2E2), Color(0xFFDC2626), Modifier.weight(1f))
                    }
                }
            }

            // Tabs
            TabRow(
                selectedTabIndex = uiState.selectedTabIndex,
                containerColor = Color(0xFFF1F5F9),
                contentColor = Secondary,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[uiState.selectedTabIndex]),
                        color = Secondary,
                        height = 3.dp
                    )
                },
                modifier = Modifier.padding(horizontal = 20.dp).clip(RoundedCornerShape(12.dp)),
                divider = {}
            ) {
                tabs.forEachIndexed { index, tab ->
                    Tab(
                        selected = uiState.selectedTabIndex == index,
                        onClick = { viewModel.setTab(index) },
                        text = { 
                            Text(
                                tab, 
                                style = MaterialTheme.typography.labelLarge, 
                                fontWeight = if (uiState.selectedTabIndex == index) FontWeight.ExtraBold else FontWeight.Bold,
                                color = if (uiState.selectedTabIndex == index) Secondary else TextSecondary
                            ) 
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            when {
                uiState.isLoading -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Secondary)
                }
                uiState.filteredContributions.isEmpty() -> EmptyState(
                    icon = Icons.Outlined.Savings,
                    title = "No records found",
                    subtitle = "There are no contribution records for this selection"
                )
                else -> LazyColumn(
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(uiState.filteredContributions, key = { it.id }) { contribution ->
                        ContributionRow(contribution = contribution)
                    }
                    item { Spacer(modifier = Modifier.height(100.dp)) }
                }
            }
        }
    }

    if (showSheet) {
        RecordContributionSheet(
            onDismiss = { showSheet = false },
            onSave = { contribution -> viewModel.recordContribution(chamaId, contribution) },
            members = membersState.members,
            defaultAmount = defaultContributionAmount,
            currentMonth = uiState.selectedMonth
        )
    }
}

@Composable
private fun SummaryPill(label: String, count: Int, bgColor: Color, textColor: Color, modifier: Modifier = Modifier) {
    Surface(shape = RoundedCornerShape(12.dp), color = bgColor, modifier = modifier) {
        Column(modifier = Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(count.toString(), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold, color = textColor)
            Text(label, style = MaterialTheme.typography.labelSmall, color = textColor, fontWeight = FontWeight.Bold)
        }
    }
}
