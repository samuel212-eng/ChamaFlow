package com.chamaflow.ui.screens.merrygoround

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
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
import com.chamaflow.ui.viewmodel.MerryGoRoundViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MerryGoRoundScreen(
    chamaId: String,
    userRole: String,
    onBack: () -> Unit = {},
    viewModel: MerryGoRoundViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var showCreateSheet by remember { mutableStateOf(false) }

    LaunchedEffect(chamaId) { viewModel.loadMerryGoRoundData(chamaId) }

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
                title = { Text("Merry Go Round", fontWeight = FontWeight.Bold, color = Color.White) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.White) } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = ChamaBlue)
            )
        },
        floatingActionButton = {
            if (userRole == "ADMIN" && uiState.merryGoRounds.isEmpty()) {
                ExtendedFloatingActionButton(
                    onClick = { showCreateSheet = true },
                    icon = { Icon(Icons.Filled.Autorenew, null) },
                    text = { Text("Start Rotation") },
                    containerColor = ChamaBlue,
                    contentColor = Color.White
                )
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = ChamaBackground
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            if (uiState.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = ChamaBlue)
                }
            } else if (uiState.merryGoRounds.isEmpty()) {
                EmptyState(
                    icon = Icons.Outlined.Cached,
                    title = "No Merry Go Round",
                    subtitle = "Set up a rotating fund for your members"
                )
            } else {
                val mgr = uiState.merryGoRounds.first()
                val currentReceiverId = mgr.rotationOrder.getOrNull(mgr.currentIndex)
                val currentReceiver = uiState.members.find { it.id == currentReceiverId }

                // Status Card
                Card(
                    modifier = Modifier.fillMaxWidth().padding(20.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = ChamaBlue),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("Current Receiver", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.7f))
                        Text(currentReceiver?.fullName ?: "Unknown", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = Color.White)
                        
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column {
                                Text("Pool Amount", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.7f))
                                Text("KES ${"%,.0f".format(mgr.totalPool)}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, color = Color.White)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("Contribution", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.7f))
                                Text("KES ${"%,.0f".format(mgr.amountPerMember)}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, color = Color.White)
                            }
                        }

                        if (userRole == "ADMIN") {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(
                                    onClick = { 
                                        // TODO: Send notification to the receiver
                                        viewModel.clearMessages() // Temporary use
                                    },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.2f)),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Icon(Icons.Filled.Notifications, null, tint = Color.White)
                                    Spacer(Modifier.width(4.dp))
                                    Text("Remind", color = Color.White)
                                }
                                
                                Button(
                                    onClick = { 
                                        if (currentReceiver != null) {
                                            viewModel.recordPayout(chamaId, mgr.id, currentReceiver.id, currentReceiver.fullName, mgr.totalPool)
                                        }
                                    },
                                    modifier = Modifier.weight(1.5f),
                                    colors = ButtonDefaults.buttonColors(containerColor = ChamaGreen),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Icon(Icons.Filled.CheckCircle, null)
                                    Spacer(Modifier.width(8.dp))
                                    Text("Confirm Payout")
                                }
                            }
                        }
                    }
                }

                Text(
                    "Rotation Schedule",
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    itemsIndexed(mgr.rotationOrder) { index, memberId ->
                        val member = uiState.members.find { it.id == memberId }
                        val isCurrent = index == mgr.currentIndex
                        val isPast = index < mgr.currentIndex
                        
                        RotationItem(
                            name = member?.fullName ?: "Unknown",
                            index = index + 1,
                            isCurrent = isCurrent,
                            isPast = isPast
                        )
                    }
                }
            }
        }
    }

    if (showCreateSheet) {
        CreateMerryGoRoundSheet(
            members = uiState.members,
            onDismiss = { showCreateSheet = false },
            onSave = { amount, order -> 
                viewModel.createMerryGoRound(chamaId, amount, order)
            }
        )
    }
}

@Composable
fun RotationItem(name: String, index: Int, isCurrent: Boolean, isPast: Boolean) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isCurrent) ChamaBlueLight else ChamaSurface
        ),
        border = if (isCurrent) androidx.compose.foundation.BorderStroke(1.dp, ChamaBlue) else null
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(if (isPast) ChamaGreen else if (isCurrent) ChamaBlue else ChamaOutline),
                contentAlignment = Alignment.Center
            ) {
                if (isPast) Icon(Icons.Filled.Check, null, tint = Color.White, modifier = Modifier.size(16.dp))
                else Text("$index", color = if (isCurrent) Color.White else ChamaTextSecondary, fontWeight = FontWeight.Bold)
            }
            
            Column(modifier = Modifier.weight(1f)) {
                Text(name, style = MaterialTheme.typography.bodyMedium, fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal)
                if (isCurrent) Text("Receiving this month", style = MaterialTheme.typography.labelSmall, color = ChamaBlue)
                else if (isPast) Text("Received", style = MaterialTheme.typography.labelSmall, color = ChamaGreenDark)
            }
            
            if (isCurrent) {
                Icon(Icons.Filled.NotificationsActive, null, tint = ChamaBlue, modifier = Modifier.size(20.dp))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateMerryGoRoundSheet(members: List<Member>, onDismiss: () -> Unit, onSave: (Double, List<String>) -> Unit) {
    var amount by remember { mutableStateOf("1000") }
    val rotationOrder = remember { mutableStateListOf<Member>().apply { addAll(members) } }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(modifier = Modifier.padding(24.dp).padding(bottom = 32.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text("Set Up Merry Go Round", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            
            OutlinedTextField(
                value = amount,
                onValueChange = { amount = it },
                label = { Text("Contribution per member") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number)
            )

            Text("Rotation Order (Members will receive in this order)", style = MaterialTheme.typography.labelMedium, color = ChamaTextSecondary)
            
            LazyColumn(modifier = Modifier.heightIn(max = 300.dp)) {
                itemsIndexed(rotationOrder) { index, member ->
                    ListItem(
                        headlineContent = { Text(member.fullName) },
                        leadingContent = { 
                            Box(modifier = Modifier.size(24.dp).clip(CircleShape).background(ChamaBlueLight), contentAlignment = Alignment.Center) {
                                Text("${index + 1}", style = MaterialTheme.typography.labelSmall, color = ChamaBlue)
                            }
                        },
                        trailingContent = {
                            Row {
                                if (index > 0) {
                                    IconButton(onClick = { 
                                        val m = rotationOrder.removeAt(index)
                                        rotationOrder.add(index - 1, m)
                                    }) { Icon(Icons.Filled.ArrowUpward, null) }
                                }
                                if (index < rotationOrder.size - 1) {
                                    IconButton(onClick = { 
                                        val m = rotationOrder.removeAt(index)
                                        rotationOrder.add(index + 1, m)
                                    }) { Icon(Icons.Filled.ArrowDownward, null) }
                                }
                            }
                        }
                    )
                }
            }

            Button(
                onClick = {
                    onSave(amount.toDoubleOrNull() ?: 0.0, rotationOrder.map { it.id })
                    onDismiss()
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = amount.isNotEmpty() && rotationOrder.isNotEmpty()
            ) {
                Text("Start Merry Go Round")
            }
        }
    }
}
