package com.chamaflow.ui.screens.members

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.chamaflow.data.models.Member
import com.chamaflow.data.models.MemberRole
import com.chamaflow.ui.components.*
import com.chamaflow.ui.theme.*
import com.chamaflow.ui.viewmodel.ChamaViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddMemberScreen(
    chamaId: String,
    onBack: () -> Unit = {}, 
    onSave: (Member) -> Unit = {},
    chamaViewModel: ChamaViewModel = hiltViewModel()
) {
    val chamaState by chamaViewModel.uiState.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var showResults by remember { mutableStateOf(false) }

    Scaffold(
        topBar = { 
            TopAppBar(
                title = { Text("Add Member", fontWeight = FontWeight.Bold, color = Color.White) }, 
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, null, tint = Color.White) } }, 
                colors = TopAppBarDefaults.topAppBarColors(containerColor = ChamaBlue)
            ) 
        },
        containerColor = ChamaBackground
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(20.dp)) {
            Text(
                "Add existing app users to your Chama. Search by their name or email.",
                style = MaterialTheme.typography.bodyMedium,
                color = ChamaTextSecondary,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { 
                    searchQuery = it
                    if (it.length >= 3) {
                        chamaViewModel.searchChamas(it) // Reusing search logic for users
                        showResults = true
                    } else {
                        showResults = false
                    }
                },
                label = { Text("Search User") },
                placeholder = { Text("Enter name or email") },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = { Icon(Icons.Filled.Search, null) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = ""; showResults = false }) {
                            Icon(Icons.Filled.Close, null)
                        }
                    }
                },
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(20.dp))

            if (showResults) {
                if (chamaState.isSearching) {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = ChamaBlue)
                    }
                } else if (chamaState.searchResults.isEmpty() && searchQuery.length >= 3) {
                    EmptyState(
                        icon = Icons.Filled.PersonSearch,
                        title = "No users found",
                        subtitle = "Try a different name or invite them to the app"
                    )
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        items(chamaState.searchResults) { userChama -> // Mocking user result with chama for now
                            UserResultRow(
                                name = userChama.name,
                                email = userChama.description,
                                onAdd = {
                                    val newMember = Member(
                                        fullName = userChama.name,
                                        email = userChama.description,
                                        role = MemberRole.MEMBER,
                                        joinDate = java.time.LocalDate.now().toString()
                                    )
                                    onSave(newMember)
                                }
                            )
                        }
                    }
                }
            } else {
                EmptyState(
                    icon = Icons.Filled.GroupAdd,
                    title = "Start searching",
                    subtitle = "Search results will appear here"
                )
            }
        }
    }
}

@Composable
private fun UserResultRow(name: String, email: String, onAdd: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = ChamaSurface),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            MemberAvatar(name = name)
            Column(modifier = Modifier.weight(1f)) {
                Text(name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                Text(email, style = MaterialTheme.typography.bodySmall, color = ChamaTextSecondary)
            }
            Button(
                onClick = onAdd,
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
            ) {
                Text("Add", style = MaterialTheme.typography.labelMedium)
            }
        }
    }
}

@Composable
fun FormSectionLabel(label: String) {
    Text(label, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold, color = ChamaTextSecondary)
}

@Composable
fun ChamaTextField(value: String, onValueChange: (String) -> Unit, label: String, placeholder: String = "", leadingIcon: ImageVector, keyboardType: KeyboardType = KeyboardType.Text, isError: Boolean = false, errorMessage: String = "", singleLine: Boolean = true, modifier: Modifier = Modifier.fillMaxWidth()) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        OutlinedTextField(value = value, onValueChange = onValueChange, label = { Text(label) }, placeholder = { Text(placeholder, color = ChamaTextMuted) }, leadingIcon = { Icon(leadingIcon, null, tint = if (isError) ChamaRed else ChamaTextSecondary) }, isError = isError, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp), keyboardOptions = KeyboardOptions(keyboardType = keyboardType), singleLine = singleLine, colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = ChamaBlue, unfocusedBorderColor = ChamaOutline, errorBorderColor = ChamaRed))
        if (isError && errorMessage.isNotEmpty()) Text(errorMessage, style = MaterialTheme.typography.labelSmall, color = ChamaRed, modifier = Modifier.padding(start = 4.dp))
    }
}
