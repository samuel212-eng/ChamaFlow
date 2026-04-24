package com.chamaflow.ui.screens.loans

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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.chamaflow.data.models.*
import com.chamaflow.ui.components.*
import com.chamaflow.ui.theme.*
import com.chamaflow.ui.viewmodel.LoansViewModel

private val loanPurple = Color(0xFF6D28D9)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoansScreen(
    chamaId: String,
    onApplyLoan: () -> Unit = {},
    onLoanClick: (String) -> Unit = {},
    viewModel: LoansViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val tabs = listOf("Active", "Pending", "Repaid")

    LaunchedEffect(chamaId) { viewModel.loadLoans(chamaId) }

    LaunchedEffect(uiState.successMessage, uiState.errorMessage) {
        val msg = uiState.successMessage ?: uiState.errorMessage
        msg?.let { snackbarHostState.showSnackbar(it); viewModel.clearMessages() }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Loans", fontWeight = FontWeight.Bold, color = Color.White) },
                actions = { IconButton(onClick = {}) { Icon(Icons.Outlined.FileDownload, null, tint = Color.White) } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = loanPurple)
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onApplyLoan,
                icon = { Icon(Icons.Filled.Add, null) },
                text = { Text("New Loan") },
                containerColor = loanPurple,
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

            // Summary banner
            Row(
                modifier = Modifier.fillMaxWidth().background(loanPurple).padding(horizontal = 24.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                BannerStat("Issued", "KES ${fmtK(uiState.totalIssued)}")
                VerticalDivider(color = Color.White.copy(alpha = 0.3f), modifier = Modifier.height(36.dp))
                BannerStat("Repaid", "KES ${fmtK(uiState.totalRepaid)}")
                VerticalDivider(color = Color.White.copy(alpha = 0.3f), modifier = Modifier.height(36.dp))
                BannerStat("Remaining", "KES ${fmtK(uiState.totalRemaining)}")
            }

            // Pending alert
            if (uiState.pendingLoans.isNotEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp).clickable { viewModel.setTab(1) },
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = ChamaGoldLight)
                ) {
                    Row(modifier = Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Icon(Icons.Filled.Pending, null, tint = ChamaGold)
                        Text("${uiState.pendingLoans.size} loan request${if (uiState.pendingLoans.size > 1) "s" else ""} awaiting approval", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold, color = Color(0xFF92400E), modifier = Modifier.weight(1f))
                        Icon(Icons.Filled.ChevronRight, null, tint = ChamaGold)
                    }
                }
            }

            // Tabs
            TabRow(
                selectedTabIndex = uiState.selectedTabIndex,
                containerColor = ChamaSurface,
                contentColor = loanPurple,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp).clip(RoundedCornerShape(12.dp))
            ) {
                tabs.forEachIndexed { index, tab ->
                    Tab(selected = uiState.selectedTabIndex == index, onClick = { viewModel.setTab(index) },
                        text = { Text(tab, style = MaterialTheme.typography.labelMedium, fontWeight = if (uiState.selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal) })
                }
            }

            when {
                uiState.isLoading -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = loanPurple)
                }
                uiState.displayedLoans.isEmpty() -> EmptyState(icon = Icons.Outlined.AccountBalance, title = "No loans here", subtitle = "No loan records in this category")
                else -> LazyColumn(
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(uiState.displayedLoans, key = { it.id }) { loan ->
                        when (loan.status) {
                            LoanStatus.PENDING -> PendingLoanCard(
                                loan = loan,
                                onApprove = { viewModel.approveLoan(chamaId, loan.id) },
                                onReject  = { viewModel.rejectLoan(chamaId, loan.id) }
                            )
                            else -> LoanProgressCard(loan = loan, onClick = { onLoanClick(loan.id) })
                        }
                    }
                    item { Spacer(modifier = Modifier.height(80.dp)) }
                }
            }
        }
    }
}

@Composable
private fun PendingLoanCard(loan: Loan, onApprove: () -> Unit, onReject: () -> Unit) {
    var showApproveDialog by remember { mutableStateOf(false) }
    var showRejectDialog  by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = ChamaSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    MemberAvatar(name = loan.memberName, size = 38.dp, backgroundColor = ChamaGoldLight, textColor = ChamaGold)
                    Column {
                        Text(loan.memberName, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                        Text("Requested: ${loan.disbursedDate}", style = MaterialTheme.typography.bodySmall, color = ChamaTextSecondary)
                    }
                }
                StatusChip(status = "PENDING")
            }
            HorizontalDivider(color = ChamaOutline)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                LoanDetailItem("Amount", "KES ${"%,.0f".format(loan.amount)}")
                LoanDetailItem("Interest", "${(loan.interestRate * 100).toInt()}%")
                LoanDetailItem("Period", "${loan.repaymentPeriodMonths} months")
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = { showRejectDialog = true }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(10.dp), colors = ButtonDefaults.outlinedButtonColors(contentColor = ChamaRed), border = ButtonDefaults.outlinedButtonBorder.copy(brush = SolidColor(ChamaRed))) {
                    Icon(Icons.Filled.Close, null, modifier = Modifier.size(16.dp)); Spacer(Modifier.width(4.dp)); Text("Reject")
                }
                Button(onClick = { showApproveDialog = true }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(10.dp), colors = ButtonDefaults.buttonColors(containerColor = ChamaGreen)) {
                    Icon(Icons.Filled.Check, null, modifier = Modifier.size(16.dp)); Spacer(Modifier.width(4.dp)); Text("Approve")
                }
            }
        }
    }

    if (showApproveDialog) {
        AlertDialog(
            onDismissRequest = { showApproveDialog = false },
            title = { Text("Approve Loan") },
            text = { Text("Approve KES ${"%,.0f".format(loan.amount)} for ${loan.memberName}?\nMonthly installment: KES ${"%,.0f".format(loan.monthlyInstallment)}") },
            confirmButton = { Button(onClick = { onApprove(); showApproveDialog = false }, colors = ButtonDefaults.buttonColors(containerColor = ChamaGreen)) { Text("Approve") } },
            dismissButton = { TextButton(onClick = { showApproveDialog = false }) { Text("Cancel") } }
        )
    }
    if (showRejectDialog) {
        AlertDialog(
            onDismissRequest = { showRejectDialog = false },
            title = { Text("Reject Loan") },
            text = { Text("Reject loan request from ${loan.memberName}?") },
            confirmButton = { Button(onClick = { onReject(); showRejectDialog = false }, colors = ButtonDefaults.buttonColors(containerColor = ChamaRed)) { Text("Reject") } },
            dismissButton = { TextButton(onClick = { showRejectDialog = false }) { Text("Cancel") } }
        )
    }
}

@Composable
private fun LoanDetailItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = ChamaTextSecondary)
        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun BannerStat(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.White)
        Text(label, style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.75f))
    }
}

private fun fmtK(amount: Double): String = when {
    amount >= 1_000_000 -> "${"%.1f".format(amount / 1_000_000)}M"
    amount >= 1_000 -> "${"%.0f".format(amount / 1_000)}K"
    else -> "%,.0f".format(amount)
}
