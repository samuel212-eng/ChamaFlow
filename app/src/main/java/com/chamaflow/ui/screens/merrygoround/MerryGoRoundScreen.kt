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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
                title = { Text("Merry Go Round", fontWeight = FontWeight.ExtraBold, color = Color.White) },
                navigationIcon = { 
                    IconButton(onClick = onBack) { 
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.White) 
                    } 
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Primary)
            )
        },
        floatingActionButton = {
            if (userRole == "ADMIN" && uiState.merryGoRounds.isEmpty()) {
                ExtendedFloatingActionButton(
                    onClick = { showCreateSheet = true },
                    icon = { Icon(Icons.Filled.Autorenew, null) },
                    text = { Text("Start Rotation", fontWeight = FontWeight.Bold) },
                    containerColor = Secondary,
                    contentColor = Color.White,
                    shape = RoundedCornerShape(16.dp)
                )
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = Background
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            if (uiState.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Secondary)
                }
            } else if (uiState.merryGoRounds.isEmpty()) {
                EmptyState(
                    icon = Icons.Outlined.Cached,
                    title = "No Merry Go Round",
                    subtitle = "Set up a rotating fund for your members to receive a large pool of funds periodically."
                )
            } else {
                val mgr = uiState.merryGoRounds.first()
                val currentReceiverId = mgr.rotationOrder.getOrNull(mgr.currentIndex)
                val currentReceiver = uiState.members.find { it.id == currentReceiverId }

                // Premium Status Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                        .shadow(elevation = 16.dp, shape = RoundedCornerShape(28.dp), spotColor = Primary.copy(alpha = 0.4f)),
                    shape = RoundedCornerShape(28.dp),
                ) {
                    Box(modifier = Modifier.background(Brush.linearGradient(PremiumGradient))) {
                        Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                Icon(Icons.Filled.Stars, null, tint = Warning, modifier = Modifier.size(20.dp))
                                Text("NEXT RECIPIENT", style = MaterialTheme.typography.labelLarge, color = Color.White.copy(alpha = 0.7f), fontWeight = FontWeight.Bold)
                            }
                            
                            Column {
                                Text(
                                    currentReceiver?.fullName ?: "Member Not Assigned", 
                                    style = MaterialTheme.typography.headlineSmall, 
                                    fontWeight = FontWeight.ExtraBold, 
                                    color = Color.White,
                                    letterSpacing = 0.5.sp
                                )
                                Text(
                                    "Is scheduled to receive the pool", 
                                    style = MaterialTheme.typography.bodySmall, 
                                    color = Color.White.copy(alpha = 0.6f)
                                )
                            }
                            
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Column {
                                    Text("Pool Total", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.6f))
                                    Text("KES ${"%,.0f".format(mgr.totalPool)}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold, color = Accent)
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text("Each Contributes", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.6f))
                                    Text("KES ${"%,.0f".format(mgr.amountPerMember)}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold, color = Color.White)
                                }
                            }

                            if (userRole == "ADMIN") {
                                Button(
                                    onClick = { 
                                        if (currentReceiver != null) {
                                            viewModel.recordPayout(chamaId, mgr.id, currentReceiver.id, currentReceiver.fullName, mgr.totalPool)
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth().height(52.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.2f)),
                                    shape = RoundedCornerShape(12.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.3f))
                                ) {
                                    Icon(Icons.Filled.CheckCircle, null, tint = Color.White)
                                    Spacer(Modifier.width(8.dp))
                                    Text("Confirm Payout", fontWeight = FontWeight.Bold, color = Color.White)
                                }
                            }
                        }
                    }
                }

                SectionHeader(title = "Rotation Schedule", actionLabel = "Order", onAction = { /* Show order info */ })

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(bottom = 100.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
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
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = if (isCurrent) Secondary.copy(alpha = 0.05f) else Color.Transparent
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(
                        if (isPast) Color(0xFFDCFCE7) 
                        else if (isCurrent) Secondary 
                        else Color(0xFFF1F5F9)
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (isPast) Icon(Icons.Filled.Check, null, tint = Color(0xFF059669), modifier = Modifier.size(20.dp))
                else Text("$index", color = if (isCurrent) Color.White else TextSecondary, fontWeight = FontWeight.ExtraBold, fontSize = 14.sp)
            }
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    name, 
                    style = MaterialTheme.typography.bodyLarge, 
                    fontWeight = if (isCurrent) FontWeight.ExtraBold else FontWeight.Bold, 
                    color = if (isCurrent) Primary else TextPrimary
                )
                if (isCurrent) {
                    Text("Currently receiving pool funds", style = MaterialTheme.typography.labelSmall, color = Secondary, fontWeight = FontWeight.Bold)
                } else if (isPast) {
                    Text("Fund payout completed", style = MaterialTheme.typography.labelSmall, color = Color(0xFF059669), fontWeight = FontWeight.Bold)
                } else {
                    Text("Waiting for turn", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                }
            }
            
            if (isCurrent) {
                Icon(Icons.Filled.NotificationsActive, null, tint = Secondary, modifier = Modifier.size(22.dp))
            } else if (!isPast) {
                Icon(Icons.Outlined.Schedule, null, tint = TextMuted, modifier = Modifier.size(20.dp))
            }
        }
    }
    HorizontalDivider(color = Color(0xFFF1F5F9), modifier = Modifier.padding(start = 76.dp))
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateMerryGoRoundSheet(members: List<Member>, onDismiss: () -> Unit, onSave: (Double, List<String>) -> Unit) {
    var amount by remember { mutableStateOf("1000") }
    val rotationOrder = remember { mutableStateListOf<Member>().apply { addAll(members) } }

    ModalBottomSheet(onDismissRequest = onDismiss, containerColor = Surface) {
        Column(modifier = Modifier.padding(24.dp).padding(bottom = 32.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
            Text("Initialize Merry Go Round", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold, color = Primary)
            
            OutlinedTextField(
                value = amount,
                onValueChange = { amount = it },
                label = { Text("Amount Each Member Contributes") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                prefix = { Text("KES ", fontWeight = FontWeight.Bold) },
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number)
            )

            Text("Set Payout Order", style = MaterialTheme.typography.labelLarge, color = Primary, fontWeight = FontWeight.ExtraBold)
            
            LazyColumn(
                modifier = Modifier.heightIn(max = 300.dp).background(Color(0xFFF8FAFC), RoundedCornerShape(16.dp)).padding(4.dp)
            ) {
                itemsIndexed(rotationOrder) { index, member ->
                    ListItem(
                        headlineContent = { Text(member.fullName, fontWeight = FontWeight.Bold, color = Primary) },
                        leadingContent = { 
                            Box(modifier = Modifier.size(28.dp).clip(CircleShape).background(Secondary.copy(alpha = 0.1f)), contentAlignment = Alignment.Center) {
                                Text("${index + 1}", style = MaterialTheme.typography.labelSmall, color = Secondary, fontWeight = FontWeight.ExtraBold)
                            }
                        },
                        trailingContent = {
                            Row {
                                if (index > 0) {
                                    IconButton(onClick = { 
                                        val m = rotationOrder.removeAt(index)
                                        rotationOrder.add(index - 1, m)
                                    }) { Icon(Icons.Filled.ArrowUpward, null, tint = Secondary) }
                                }
                                if (index < rotationOrder.size - 1) {
                                    IconButton(onClick = { 
                                        val m = rotationOrder.removeAt(index)
                                        rotationOrder.add(index + 1, m)
                                    }) { Icon(Icons.Filled.ArrowDownward, null, tint = Secondary) }
                                }
                            }
                        },
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                    )
                    if (index < rotationOrder.size - 1) HorizontalDivider(color = Color.White, thickness = 2.dp)
                }
            }

            Button(
                onClick = {
                    onSave(amount.toDoubleOrNull() ?: 0.0, rotationOrder.map { it.id })
                    onDismiss()
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(12.dp),
                enabled = amount.isNotEmpty() && rotationOrder.isNotEmpty(),
                colors = ButtonDefaults.buttonColors(containerColor = Primary)
            ) {
                Text("Launch Merry Go Round", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }
    }
}
