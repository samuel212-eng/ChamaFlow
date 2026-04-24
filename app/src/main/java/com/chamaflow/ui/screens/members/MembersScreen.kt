package com.chamaflow.ui.screens.members

import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.chamaflow.ui.components.*
import com.chamaflow.ui.theme.*
import com.chamaflow.ui.viewmodel.MembersViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MembersScreen(
    chamaId: String,
    onAddMember: () -> Unit = {},
    onMemberClick: (String) -> Unit = {},
    viewModel: MembersViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val filters = listOf("All", "Admin", "Treasurer", "Member", "Inactive")
    var snackbarMessage by remember { mutableStateOf<String?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(chamaId) { viewModel.loadMembers(chamaId) }

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
                title = { Text("Members", fontWeight = FontWeight.Bold, color = Color.White) },
                actions = {
                    IconButton(onClick = {}) {
                        Icon(Icons.Outlined.FilterList, "Filter", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = ChamaBlue)
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onAddMember,
                icon = { Icon(Icons.Filled.PersonAdd, null) },
                text = { Text("Add Member") },
                containerColor = ChamaBlue,
                contentColor = Color.White
            )
        },
        snackbarHost = {
            SnackbarHost(snackbarHostState) { data ->
                Snackbar(
                    snackbarData = data,
                    containerColor = if (uiState.errorMessage != null) ChamaRed else ChamaGreen,
                    contentColor = Color.White
                )
            }
        },
        containerColor = ChamaBackground
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {

            // Summary banner
            Row(
                modifier = Modifier.fillMaxWidth().background(ChamaBlue).padding(horizontal = 24.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                BannerStat("Active", "${uiState.activeCount}")
                VerticalDivider(color = Color.White.copy(alpha = 0.3f), modifier = Modifier.height(36.dp))
                BannerStat("With Loans", "${uiState.withLoans}")
                VerticalDivider(color = Color.White.copy(alpha = 0.3f), modifier = Modifier.height(36.dp))
                BannerStat("Penalties", "${uiState.withPenalties}")
            }

            // Search
            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = { viewModel.setSearchQuery(it) },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp),
                placeholder = { Text("Search by name or phone", color = ChamaTextMuted) },
                leadingIcon = { Icon(Icons.Filled.Search, null, tint = ChamaTextSecondary) },
                trailingIcon = {
                    if (uiState.searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.setSearchQuery("") }) {
                            Icon(Icons.Filled.Close, null, tint = ChamaTextSecondary)
                        }
                    }
                },
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = ChamaBlue, unfocusedBorderColor = ChamaOutline, focusedContainerColor = ChamaSurface, unfocusedContainerColor = ChamaSurface)
            )

            // Filter chips
            LazyRow(contentPadding = PaddingValues(horizontal = 20.dp, vertical = 4.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(filters) { filter ->
                    FilterChip(
                        selected = uiState.selectedFilter == filter,
                        onClick = { viewModel.setFilter(filter) },
                        label = { Text(filter) },
                        colors = FilterChipDefaults.filterChipColors(selectedContainerColor = ChamaBlue, selectedLabelColor = Color.White)
                    )
                }
            }

            // Count
            Text(
                "${uiState.filteredMembers.size} member${if (uiState.filteredMembers.size != 1) "s" else ""}",
                style = MaterialTheme.typography.labelMedium,
                color = ChamaTextSecondary,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
            )

            // Loading / empty / list
            when {
                uiState.isLoading -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = ChamaBlue)
                }
                uiState.filteredMembers.isEmpty() -> EmptyState(
                    icon = Icons.Outlined.Group,
                    title = "No members found",
                    subtitle = if (uiState.searchQuery.isNotEmpty()) "Try a different search term" else "Add your first member to get started",
                    actionLabel = if (uiState.searchQuery.isEmpty()) "Add Member" else null,
                    onAction = onAddMember
                )
                else -> LazyColumn(
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(uiState.filteredMembers, key = { it.id }) { member ->
                        MemberListRow(member = member, onClick = { onMemberClick(member.id) })
                    }
                    item { Spacer(modifier = Modifier.height(80.dp)) }
                }
            }
        }
    }
}

@Composable
private fun BannerStat(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Color.White)
        Text(label, style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.75f))
    }
}
