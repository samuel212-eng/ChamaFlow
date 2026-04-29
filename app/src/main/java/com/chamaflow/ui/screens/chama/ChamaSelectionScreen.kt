package com.chamaflow.ui.screens.chama

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.chamaflow.data.models.Chama
import com.chamaflow.ui.theme.*
import com.chamaflow.ui.viewmodel.ChamaViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChamaSelectionScreen(
    onCreateNew: () -> Unit,
    onJoinExisting: (String) -> Unit,
    onLogout: () -> Unit,
    onSkip: () -> Unit = {},
    viewModel: ChamaViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var showJoinDialog by remember { mutableStateOf(false) }
    var showSearchDialog by remember { mutableStateOf(false) }
    var inviteCode by remember { mutableStateOf("") }
    var searchQuery by remember { mutableStateOf("") }
    val requestedChamas = remember { mutableStateListOf<String>() }

    LaunchedEffect(uiState.successMessage) {
        uiState.successMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessages()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("ChamaFlow", fontWeight = FontWeight.Bold, color = Color.White) },
                actions = {
                    TextButton(onClick = onSkip) {
                        Text("Skip", color = Color.White)
                    }
                    IconButton(onClick = onLogout) {
                        Icon(Icons.AutoMirrored.Filled.Logout, "Logout", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = ChamaBlue)
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = ChamaBackground
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                Icons.Filled.Groups,
                null,
                modifier = Modifier.size(80.dp),
                tint = ChamaBlue
            )
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                "Welcome to ChamaFlow",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Text(
                "Create a group, join your friends, or start saving individually.",
                style = MaterialTheme.typography.bodyMedium,
                color = ChamaTextSecondary,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(vertical = 16.dp)
            )

            uiState.errorMessage?.let {
                Text(
                    it,
                    color = ChamaRed,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }

            Button(
                onClick = onCreateNew,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = ChamaBlue)
            ) {
                Icon(Icons.Filled.Add, null)
                Spacer(Modifier.width(8.dp))
                Text("Create New Chama", fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedButton(
                onClick = { showJoinDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Filled.VpnKey, null)
                Spacer(Modifier.width(8.dp))
                Text("Join with Invite Code", fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedButton(
                onClick = { showSearchDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Filled.Search, null)
                Spacer(Modifier.width(8.dp))
                Text("Search for a Chama", fontWeight = FontWeight.Bold)
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            TextButton(onClick = onSkip) {
                Text("I'll join later, let me start saving", color = ChamaBlue, fontWeight = FontWeight.SemiBold)
            }
        }
    }

    if (showJoinDialog) {
        AlertDialog(
            onDismissRequest = { showJoinDialog = false },
            title = { Text("Join with Invite Code") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Enter the 8-character code sent by your admin.")
                    OutlinedTextField(
                        value = inviteCode,
                        onValueChange = { inviteCode = it.uppercase().take(8) },
                        label = { Text("Invite Code") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { 
                        onJoinExisting(inviteCode)
                        showJoinDialog = false
                    },
                    enabled = inviteCode.length == 8 && !uiState.isLoading
                ) {
                    if (uiState.isLoading) CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White)
                    else Text("Join")
                }
            },
            dismissButton = {
                TextButton(onClick = { showJoinDialog = false }) { Text("Cancel") }
            }
        )
    }

    if (showSearchDialog) {
        AlertDialog(
            onDismissRequest = { showSearchDialog = false },
            title = { Text("Search Chama") },
            text = {
                Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { 
                            searchQuery = it
                            viewModel.searchChamas(it)
                        },
                        label = { Text("Group Name") },
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = { Icon(Icons.Filled.Search, null) },
                        singleLine = true
                    )
                    
                    if (uiState.isSearching) {
                        LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                    }
                    
                    LazyColumn(modifier = Modifier.heightIn(max = 300.dp)) {
                        items(uiState.searchResults) { chama ->
                            val isRequested = requestedChamas.contains(chama.id)
                            ListItem(
                                headlineContent = { Text(chama.name) },
                                supportingContent = { Text(chama.description) },
                                trailingContent = {
                                    Button(
                                        onClick = { 
                                            viewModel.requestToJoin(chama.id)
                                            requestedChamas.add(chama.id)
                                        },
                                        enabled = !isRequested
                                    ) {
                                        Text(if (isRequested) "Sent" else "Request")
                                    }
                                }
                            )
                        }
                        if (!uiState.isSearching && uiState.searchResults.isEmpty() && searchQuery.length >= 2) {
                            item {
                                Text("No groups found", modifier = Modifier.fillMaxWidth().padding(16.dp), textAlign = TextAlign.Center, color = ChamaTextSecondary)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showSearchDialog = false }) { Text("Close") }
            }
        )
    }
}
