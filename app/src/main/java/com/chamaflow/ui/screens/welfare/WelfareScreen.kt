package com.chamaflow.ui.screens.welfare

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import com.chamaflow.ui.viewmodel.WelfareViewModel
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WelfareScreen(
    chamaId: String,
    userRole: String,
    onBack: () -> Unit = {},
    viewModel: WelfareViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var selectedTab by remember { mutableIntStateOf(0) }
    var showAddContribution by remember { mutableStateOf(false) }
    var showAddDisbursement by remember { mutableStateOf(false) }
    var showEditRules by remember { mutableStateOf(false) }

    LaunchedEffect(chamaId) { viewModel.loadWelfareData(chamaId) }

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
                title = { Text("Welfare Fund", fontWeight = FontWeight.Bold, color = Color.White) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.White) } },
                actions = {
                    if (userRole == "ADMIN") {
                        IconButton(onClick = { showEditRules = true }) {
                            Icon(Icons.Outlined.Settings, "Rules", tint = Color.White)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = ChamaOrange)
            )
        },
        floatingActionButton = {
            if (userRole == "ADMIN" || userRole == "TREASURER") {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp), horizontalAlignment = Alignment.End) {
                    SmallFloatingActionButton(
                        onClick = { showAddDisbursement = true },
                        containerColor = ChamaRed,
                        contentColor = Color.White
                    ) { Icon(Icons.Filled.Remove, "Disburse") }
                    
                    ExtendedFloatingActionButton(
                        onClick = { showAddContribution = true },
                        icon = { Icon(Icons.Filled.Add, null) },
                        text = { Text("Record Contribution") },
                        containerColor = ChamaOrange,
                        contentColor = Color.White
                    )
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = ChamaBackground
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            // Welfare Balance Card
            Card(
                modifier = Modifier.fillMaxWidth().padding(20.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = ChamaOrange),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Available Welfare Fund", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.8f))
                    Text("KES ${"%,.0f".format(uiState.fund?.totalBalance ?: 0.0)}", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = Color.White)
                    
                    if (uiState.fund?.rules?.isNotEmpty() == true) {
                        Surface(color = Color.White.copy(alpha = 0.15f), shape = RoundedCornerShape(8.dp)) {
                            Text(
                                uiState.fund!!.rules,
                                modifier = Modifier.padding(12.dp),
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White
                            )
                        }
                    }
                }
            }

            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = ChamaSurface,
                contentColor = ChamaOrange,
                modifier = Modifier.padding(horizontal = 20.dp).clip(RoundedCornerShape(12.dp))
            ) {
                Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }, text = { Text("Contributions") })
                Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }, text = { Text("Disbursements") })
            }

            if (uiState.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = ChamaOrange)
                }
            } else {
                when (selectedTab) {
                    0 -> WelfareContributionsList(uiState.contributions)
                    1 -> WelfareDisbursementsList(uiState.disbursements)
                }
            }
        }
    }

    if (showAddContribution) {
        WelfareContributionSheet(
            members = uiState.members,
            defaultAmount = uiState.fund?.contributionAmount ?: 0.0,
            onDismiss = { showAddContribution = false },
            onSave = { viewModel.recordContribution(chamaId, it) }
        )
    }

    if (showAddDisbursement) {
        WelfareDisbursementSheet(
            members = uiState.members,
            onDismiss = { showAddDisbursement = false },
            onSave = { viewModel.recordDisbursement(chamaId, it) }
        )
    }

    if (showEditRules) {
        EditWelfareRulesSheet(
            currentAmount = uiState.fund?.contributionAmount ?: 0.0,
            currentRules = uiState.fund?.rules ?: "",
            onDismiss = { showEditRules = false },
            onSave = { amount, rules -> viewModel.updateRules(chamaId, amount, rules) }
        )
    }
}

@Composable
fun WelfareContributionsList(contributions: List<WelfareContribution>) {
    if (contributions.isEmpty()) {
        EmptyState(Icons.Outlined.History, "No contributions", "Recent welfare payments will appear here")
    } else {
        LazyColumn(contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(contributions) { item ->
                WelfareItemRow(
                    title = item.memberName,
                    subtitle = "${item.date} · ${item.month}",
                    amount = item.amount,
                    isPositive = true
                )
            }
        }
    }
}

@Composable
fun WelfareDisbursementsList(disbursements: List<WelfareDisbursement>) {
    if (disbursements.isEmpty()) {
        EmptyState(Icons.Outlined.History, "No disbursements", "Welfare payouts will appear here")
    } else {
        LazyColumn(contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(disbursements) { item ->
                WelfareItemRow(
                    title = item.memberName,
                    subtitle = "${item.category.name} · ${item.reason}",
                    amount = item.amount,
                    isPositive = false
                )
            }
        }
    }
}

@Composable
fun WelfareItemRow(title: String, subtitle: String, amount: Double, isPositive: Boolean) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = ChamaSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = ChamaTextSecondary)
            }
            Text(
                "${if (isPositive) "+" else "-"} KES ${"%,.0f".format(amount)}",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = if (isPositive) ChamaGreen else ChamaRed
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WelfareContributionSheet(members: List<Member>, defaultAmount: Double, onDismiss: () -> Unit, onSave: (WelfareContribution) -> Unit) {
    var selectedMember by remember { mutableStateOf<Member?>(null) }
    var amount by remember { mutableStateOf(defaultAmount.toInt().toString()) }
    var dropdownOpen by remember { mutableStateOf(false) }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(modifier = Modifier.padding(24.dp).padding(bottom = 32.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text("Record Welfare Payment", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            
            ExposedDropdownMenuBox(expanded = dropdownOpen, onExpandedChange = { dropdownOpen = it }) {
                OutlinedTextField(
                    value = selectedMember?.fullName ?: "",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Select Member") },
                    modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable),
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = dropdownOpen) }
                )
                ExposedDropdownMenu(expanded = dropdownOpen, onDismissRequest = { dropdownOpen = false }) {
                    members.forEach { m ->
                        DropdownMenuItem(text = { Text(m.fullName) }, onClick = { selectedMember = m; dropdownOpen = false })
                    }
                }
            }

            OutlinedTextField(value = amount, onValueChange = { amount = it }, label = { Text("Amount (KES)") }, modifier = Modifier.fillMaxWidth())
            
            Button(
                onClick = {
                    if (selectedMember != null) {
                        onSave(WelfareContribution(
                            memberId = selectedMember!!.id,
                            memberName = selectedMember!!.fullName,
                            amount = amount.toDoubleOrNull() ?: 0.0,
                            date = LocalDate.now().toString(),
                            month = LocalDate.now().month.name
                        ))
                        onDismiss()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = selectedMember != null && amount.isNotEmpty()
            ) { Text("Save Contribution") }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WelfareDisbursementSheet(members: List<Member>, onDismiss: () -> Unit, onSave: (WelfareDisbursement) -> Unit) {
    var selectedMember by remember { mutableStateOf<Member?>(null) }
    var amount by remember { mutableStateOf("") }
    var reason by remember { mutableStateOf("") }
    var category by remember { mutableStateOf(WelfareCategory.EMERGENCY) }
    var dropdownOpen by remember { mutableStateOf(false) }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(modifier = Modifier.padding(24.dp).padding(bottom = 32.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text("Record Disbursement", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            
            ExposedDropdownMenuBox(expanded = dropdownOpen, onExpandedChange = { dropdownOpen = it }) {
                OutlinedTextField(
                    value = selectedMember?.fullName ?: "",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Recipient Member") },
                    modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable),
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = dropdownOpen) }
                )
                ExposedDropdownMenu(expanded = dropdownOpen, onDismissRequest = { dropdownOpen = false }) {
                    members.forEach { m ->
                        DropdownMenuItem(text = { Text(m.fullName) }, onClick = { selectedMember = m; dropdownOpen = false })
                    }
                }
            }

            OutlinedTextField(value = amount, onValueChange = { amount = it }, label = { Text("Amount (KES)") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = reason, onValueChange = { reason = it }, label = { Text("Reason/Details") }, modifier = Modifier.fillMaxWidth())
            
            Text("Category", style = MaterialTheme.typography.labelMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                WelfareCategory.entries.forEach { cat ->
                    FilterChip(
                        selected = category == cat,
                        onClick = { category = cat },
                        label = { Text(cat.name.lowercase().replaceFirstChar { it.uppercase() }) }
                    )
                }
            }

            Button(
                onClick = {
                    if (selectedMember != null) {
                        onSave(WelfareDisbursement(
                            memberId = selectedMember!!.id,
                            memberName = selectedMember!!.fullName,
                            amount = amount.toDoubleOrNull() ?: 0.0,
                            reason = reason,
                            category = category,
                            date = LocalDate.now().toString()
                        ))
                        onDismiss()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = selectedMember != null && amount.isNotEmpty(),
                colors = ButtonDefaults.buttonColors(containerColor = ChamaRed)
            ) { Text("Confirm Disbursement") }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditWelfareRulesSheet(currentAmount: Double, currentRules: String, onDismiss: () -> Unit, onSave: (Double, String) -> Unit) {
    var amount by remember { mutableStateOf(currentAmount.toInt().toString()) }
    var rules by remember { mutableStateOf(currentRules) }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(modifier = Modifier.padding(24.dp).padding(bottom = 32.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text("Welfare Settings", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            OutlinedTextField(value = amount, onValueChange = { amount = it }, label = { Text("Monthly Contribution (KES)") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = rules, onValueChange = { rules = it }, label = { Text("Rules & Guidelines") }, modifier = Modifier.fillMaxWidth(), minLines = 3)
            Button(
                onClick = {
                    onSave(amount.toDoubleOrNull() ?: 0.0, rules)
                    onDismiss()
                },
                modifier = Modifier.fillMaxWidth()
            ) { Text("Save Settings") }
        }
    }
}
