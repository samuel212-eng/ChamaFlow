package com.chamaflow.ui.screens.contributions

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.chamaflow.data.models.*
import com.chamaflow.ui.components.*
import com.chamaflow.ui.theme.*
import com.chamaflow.ui.viewmodel.ContributionsViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContributionsScreen(
    chamaId: String,
    defaultContributionAmount: Double = 5_000.0,
    viewModel: ContributionsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var showSheet by remember { mutableStateOf(false) }

    // Build last 6 months for selector
    val months = remember {
        (5 downTo 0).map { offset ->
            LocalDate.now().minusMonths(offset.toLong())
                .format(DateTimeFormatter.ofPattern("MMM yyyy"))
        }
    }
    val tabs = listOf("All", "Paid", "Unpaid", "Overdue")

    LaunchedEffect(chamaId) { viewModel.loadContributions(chamaId) }

    LaunchedEffect(uiState.successMessage, uiState.errorMessage) {
        val msg = uiState.successMessage ?: uiState.errorMessage
        msg?.let { snackbarHostState.showSnackbar(it); viewModel.clearMessages() }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Contributions", fontWeight = FontWeight.Bold, color = Color.White) },
                actions = {
                    IconButton(onClick = {}) { Icon(Icons.Outlined.FileDownload, "Export", tint = Color.White) }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = ChamaGreen)
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showSheet = true },
                icon = { Icon(Icons.Filled.Add, null) },
                text = { Text("Record Payment") },
                containerColor = ChamaGreen,
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

            // Month selector
            Box(modifier = Modifier.fillMaxWidth().background(ChamaGreen)) {
                LazyRow(contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(months) { month ->
                        val isSelected = month == uiState.selectedMonth
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = if (isSelected) Color.White else Color.White.copy(alpha = 0.2f),
                            modifier = if (!isSelected) Modifier.clickable { viewModel.changeMonth(chamaId, month) } else Modifier
                        ) {
                            Text(
                                month,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) ChamaGreen else Color.White
                            )
                        }
                    }
                }
            }

            // Summary card
            Card(
                modifier = Modifier.fillMaxWidth().padding(20.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = ChamaSurface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    val progress = if (uiState.totalCount > 0) uiState.paidCount.toFloat() / uiState.totalCount else 0f
                    val expectedTotal = uiState.totalCount * defaultContributionAmount

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Column {
                            Text("Amount Collected", style = MaterialTheme.typography.labelSmall, color = ChamaTextSecondary)
                            Text("KES ${"%,.0f".format(uiState.amountCollected)}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = ChamaGreen)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Expected", style = MaterialTheme.typography.labelSmall, color = ChamaTextSecondary)
                            Text("KES ${"%,.0f".format(expectedTotal)}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                        }
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("${uiState.paidCount} of ${uiState.totalCount} paid", style = MaterialTheme.typography.labelSmall, color = ChamaTextSecondary)
                            Text("${(progress * 100).toInt()}%", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold, color = ChamaGreen)
                        }
                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier.fillMaxWidth().height(10.dp).clip(RoundedCornerShape(5.dp)),
                            color = ChamaGreen,
                            trackColor = ChamaOutline
                        )
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        SummaryPill("Paid", uiState.paidCount, ChamaGreen, Modifier.weight(1f))
                        SummaryPill("Partial", uiState.partialCount, ChamaOrange, Modifier.weight(1f))
                        SummaryPill("Overdue", uiState.overdueCount, ChamaRed, Modifier.weight(1f))
                    }
                }
            }

            // Tabs
            TabRow(
                selectedTabIndex = uiState.selectedTabIndex,
                containerColor = ChamaSurface,
                contentColor = ChamaGreen,
                modifier = Modifier.padding(horizontal = 20.dp).clip(RoundedCornerShape(12.dp))
            ) {
                tabs.forEachIndexed { index, tab ->
                    Tab(
                        selected = uiState.selectedTabIndex == index,
                        onClick = { viewModel.setTab(index) },
                        text = { Text(tab, style = MaterialTheme.typography.labelMedium, fontWeight = if (uiState.selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            when {
                uiState.isLoading -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = ChamaGreen)
                }
                uiState.filteredContributions.isEmpty() -> EmptyState(
                    icon = Icons.Outlined.Savings,
                    title = "No contributions",
                    subtitle = "No records for this filter"
                )
                else -> LazyColumn(
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(uiState.filteredContributions, key = { it.id }) { contribution ->
                        ContributionRow(contribution = contribution)
                    }
                    item { Spacer(modifier = Modifier.height(80.dp)) }
                }
            }
        }
    }

    if (showSheet) {
        RecordContributionSheet(
            onDismiss = { showSheet = false },
            onSave = { contribution -> viewModel.recordContribution(chamaId, contribution) },
            defaultAmount = defaultContributionAmount,
            currentMonth = uiState.selectedMonth
        )
    }
}

@Composable
private fun SummaryPill(label: String, count: Int, color: Color, modifier: Modifier = Modifier) {
    Surface(shape = RoundedCornerShape(8.dp), color = color.copy(alpha = 0.1f), modifier = modifier) {
        Column(modifier = Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(count.toString(), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = color)
            Text(label, style = MaterialTheme.typography.labelSmall, color = color)
        }
    }
}

private fun Modifier.clickable(onClick: () -> Unit): Modifier =
    this.then(Modifier.clickable { onClick() })
