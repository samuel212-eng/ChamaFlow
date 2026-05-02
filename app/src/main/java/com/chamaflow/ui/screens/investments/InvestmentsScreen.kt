package com.chamaflow.ui.screens.investments

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.automirrored.outlined.*
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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import com.chamaflow.data.models.*
import com.chamaflow.ui.components.*
import com.chamaflow.ui.theme.*
import com.chamaflow.ui.viewmodel.InvestmentsViewModel
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InvestmentsScreen(
    chamaId: String,
    userRole: String,
    onBack: () -> Unit = {},
    viewModel: InvestmentsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var showAddSheet by remember { mutableStateOf(false) }
    var showPayoutSheet by remember { mutableStateOf<Investment?>(null) }
    var selectedDistribution by remember { mutableStateOf<DividendDistribution?>(null) }

    LaunchedEffect(chamaId) { viewModel.loadInvestments(chamaId) }

    LaunchedEffect(uiState.latestDistribution) {
        if (uiState.latestDistribution != null) {
            selectedDistribution = uiState.latestDistribution
        }
    }

    LaunchedEffect(uiState.successMessage, uiState.errorMessage) {
        val msg = uiState.successMessage ?: uiState.errorMessage
        msg?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessages()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Group Investments", fontWeight = FontWeight.Bold, color = Color.White) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.White) } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Primary)
            )
        },
        floatingActionButton = {
            if (userRole == "ADMIN") {
                ExtendedFloatingActionButton(
                    onClick = { showAddSheet = true },
                    icon = { Icon(Icons.Filled.Add, null) },
                    text = { Text("New Investment") },
                    containerColor = Secondary,
                    contentColor = Color.White
                )
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = Background
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            // Portfolio Summary
            val totalInvested = uiState.investments.sumOf { it.amountInvested }
            val totalValuation = uiState.investments.sumOf { it.currentValuation }
            val totalProfit = totalValuation - totalInvested

            Card(
                modifier = Modifier.fillMaxWidth().padding(20.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Primary),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text("Portfolio Value", style = MaterialTheme.typography.labelMedium, color = Color.White.copy(alpha = 0.7f))
                    Text("KES ${"%,.0f".format(totalValuation)}", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.ExtraBold, color = Color.White)
                    
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column {
                            Text("Total Invested", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.7f))
                            Text("KES ${"%,.0f".format(totalInvested)}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Total Profit", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.7f))
                            Text("KES ${"%,.0f".format(totalProfit)}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = if (totalProfit >= 0) Accent else Error)
                        }
                    }
                }
            }

            if (uiState.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Secondary)
                }
            } else if (uiState.investments.isEmpty()) {
                EmptyState(
                    icon = Icons.AutoMirrored.Outlined.TrendingUp,
                    title = "No investments yet",
                    subtitle = "Group investments will appear here"
                )
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(uiState.investments) { investment ->
                        InvestmentCard(
                            investment = investment,
                            canManage = userRole == "ADMIN",
                            onDistribute = { showPayoutSheet = investment }
                        )
                    }
                    item { Spacer(modifier = Modifier.height(80.dp)) }
                }
            }
        }
    }

    if (showAddSheet) {
        AddInvestmentSheet(
            onDismiss = { showAddSheet = false },
            onSave = { viewModel.addInvestment(chamaId, it) }
        )
    }

    if (showPayoutSheet != null) {
        DividendDistributionSheet(
            investment = showPayoutSheet!!,
            onDismiss = { showPayoutSheet = null },
            onDistribute = { amount -> 
                viewModel.distributeDividends(chamaId, showPayoutSheet!!.id, showPayoutSheet!!.title, amount)
            }
        )
    }

    if (selectedDistribution != null) {
        PayoutStatementDialog(
            distribution = selectedDistribution!!,
            onDismiss = { selectedDistribution = null }
        )
    }
}

@Composable
fun InvestmentCard(investment: Investment, canManage: Boolean, onDistribute: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(investment.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold, color = Primary)
                    Text(investment.dateInvested, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                }
                StatusChip(status = investment.status.name)
            }
            
            Text(investment.description, style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
            
            HorizontalDivider(color = ChamaOutline.copy(alpha = 0.5f))
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text("Invested", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                    Text("KES ${"%,.0f".format(investment.amountInvested)}", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Current Value", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                    Text("KES ${"%,.0f".format(investment.currentValuation)}", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold, color = Secondary)
                }
            }
            
            if (investment.totalDividendsDistributed > 0) {
                Surface(color = Accent.copy(alpha = 0.1f), shape = RoundedCornerShape(12.dp)) {
                    Row(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Dividends Paid", style = MaterialTheme.typography.labelSmall, color = Accent, fontWeight = FontWeight.Bold)
                        Text("KES ${"%,.0f".format(investment.totalDividendsDistributed)}", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.ExtraBold, color = Accent)
                    }
                }
            }

            if (canManage && investment.status == InvestmentStatus.ACTIVE) {
                Button(
                    onClick = onDistribute,
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Primary)
                ) {
                    Icon(Icons.Filled.Payments, null, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Distribute Dividends", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddInvestmentSheet(onDismiss: () -> Unit, onSave: (Investment) -> Unit) {
    var title by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    val formValid = title.isNotEmpty() && amount.toDoubleOrNull() != null

    ModalBottomSheet(onDismissRequest = onDismiss, containerColor = Surface) {
        Column(modifier = Modifier.padding(24.dp).padding(bottom = 32.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
            Text("Add New Investment", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold, color = Primary)
            OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Investment Title") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
            OutlinedTextField(value = desc, onValueChange = { desc = it }, label = { Text("Description") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
            OutlinedTextField(value = amount, onValueChange = { amount = it }, label = { Text("Amount Invested (KES)") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
            Button(
                onClick = {
                    onSave(Investment(
                        title = title,
                        description = desc,
                        amountInvested = amount.toDoubleOrNull() ?: 0.0,
                        currentValuation = amount.toDoubleOrNull() ?: 0.0,
                        dateInvested = LocalDate.now().toString()
                    ))
                    onDismiss()
                },
                enabled = formValid,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Primary)
            ) {
                Text("Save Investment", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DividendDistributionSheet(investment: Investment, onDismiss: () -> Unit, onDistribute: (Double) -> Unit) {
    var amount by remember { mutableStateOf("") }
    val distAmount = amount.toDoubleOrNull() ?: 0.0

    ModalBottomSheet(onDismissRequest = onDismiss, containerColor = Surface) {
        Column(modifier = Modifier.padding(24.dp).padding(bottom = 32.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
            Text("Distribute Dividends", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold, color = Primary)
            Text("From: ${investment.title}", style = MaterialTheme.typography.bodyMedium, color = TextSecondary, fontWeight = FontWeight.Bold)
            
            OutlinedTextField(
                value = amount,
                onValueChange = { amount = it },
                label = { Text("Total Dividend Amount (KES)") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )
            
            if (distAmount > 0) {
                Surface(color = Secondary.copy(alpha = 0.1f), shape = RoundedCornerShape(12.dp)) {
                    Text(
                        "This will be shared among members automatically based on their contribution percentage.", 
                        style = MaterialTheme.typography.bodySmall, 
                        color = Secondary,
                        modifier = Modifier.padding(12.dp),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            Button(
                onClick = {
                    onDistribute(distAmount)
                    onDismiss()
                },
                enabled = distAmount > 0,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Accent)
            ) {
                Text("Process Payouts", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun PayoutStatementDialog(distribution: DividendDistribution, onDismiss: () -> Unit) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(0.92f).padding(16.dp),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = Surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 24.dp)
        ) {
            Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Payout Statement", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold, color = Primary)
                    IconButton(onClick = onDismiss) { Icon(Icons.Filled.Close, null, tint = TextSecondary) }
                }
                
                Column(modifier = Modifier.fillMaxWidth().background(Primary, RoundedCornerShape(20.dp)).padding(20.dp)) {
                    Text(distribution.investmentTitle, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold, color = Color.White)
                    Text("Date: ${distribution.dateDistributed}", style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.7f))
                    Spacer(Modifier.height(16.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom) {
                        Text("Total Distributed", style = MaterialTheme.typography.bodyMedium, color = Color.White.copy(alpha = 0.8f))
                        Text("KES ${"%,.0f".format(distribution.totalAmount)}", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold, color = Color.White)
                    }
                }
                
                Text("Member Breakdown", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.ExtraBold, color = Primary)
                
                LazyColumn(modifier = Modifier.weight(1f, fill = false), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(distribution.memberPayouts) { payout ->
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(payout.memberName, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = Primary)
                                Text("Share: ${"%.2f".format(payout.contributionPercentage)}%", style = MaterialTheme.typography.labelSmall, color = Secondary, fontWeight = FontWeight.Bold)
                            }
                            Text("KES ${"%,.0f".format(payout.amount)}", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.ExtraBold, color = Accent)
                        }
                        HorizontalDivider(color = ChamaOutline.copy(alpha = 0.5f), thickness = 0.5.dp)
                    }
                }
                
                Button(
                    onClick = onDismiss, 
                    modifier = Modifier.fillMaxWidth().height(52.dp), 
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Primary)
                ) {
                    Text("Close Statement", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
