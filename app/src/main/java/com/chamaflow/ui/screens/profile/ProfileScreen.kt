package com.chamaflow.ui.screens.profile

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.chamaflow.ui.components.MemberAvatar
import com.chamaflow.ui.screens.members.ChamaTextField
import com.chamaflow.ui.theme.*
import com.chamaflow.ui.viewmodel.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onBack: () -> Unit = {},
    onLogout: () -> Unit = {},
    userName: String = "User",
    userEmail: String = "",
    userPhone: String = "",
    userRole: String = "Member",
    chamName: String = "ChamaFlow Group",
    authViewModel: AuthViewModel = hiltViewModel()
) {
    var isEditing by remember { mutableStateOf(false) }
    var editName by remember { mutableStateOf(userName) }
    var editPhone by remember { mutableStateOf(userPhone) }
    var showPasswordSheet by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        selectedImageUri = uri
        // TODO: Upload to Firebase Storage and update profile
    }

    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profile", fontWeight = FontWeight.Bold, color = Color.White) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.White) } },
                actions = {
                    TextButton(onClick = {
                        if (isEditing) {
                            // TODO: save via ViewModel
                            isEditing = false
                        } else {
                            isEditing = true
                        }
                    }) {
                        Text(if (isEditing) "Save" else "Edit", color = Color.White, fontWeight = FontWeight.SemiBold)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = ChamaBlue)
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = ChamaBackground
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            // Hero
            Box(
                modifier = Modifier.fillMaxWidth().background(ChamaBlue).padding(horizontal = 24.dp, vertical = 28.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Box(contentAlignment = Alignment.BottomEnd) {
                        if (selectedImageUri != null) {
                            AsyncImage(
                                model = selectedImageUri,
                                contentDescription = "Profile Picture",
                                modifier = Modifier.size(100.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.2f)),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            MemberAvatar(name = editName, size = 100.dp, backgroundColor = Color.White.copy(alpha = 0.2f), textColor = Color.White)
                        }
                        
                        IconButton(
                            onClick = { photoPickerLauncher.launch("image/*") },
                            modifier = Modifier.size(32.dp).clip(CircleShape).background(ChamaBlueLight)
                        ) {
                            Icon(Icons.Filled.CameraAlt, "Change Photo", tint = ChamaBlue, modifier = Modifier.size(18.dp))
                        }
                    }
                    
                    Text(editName, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Color.White)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Surface(shape = RoundedCornerShape(20.dp), color = Color.White.copy(alpha = 0.2f)) {
                            Text(userRole, modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp), style = MaterialTheme.typography.labelSmall, color = Color.White, fontWeight = FontWeight.SemiBold)
                        }
                        Surface(shape = RoundedCornerShape(20.dp), color = Color.White.copy(alpha = 0.2f)) {
                            Text(chamName, modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp), style = MaterialTheme.typography.labelSmall, color = Color.White)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Info card
            ProfileSection(title = "Personal Information") {
                if (isEditing) {
                    ChamaTextField(value = editName, onValueChange = { editName = it }, label = "Full Name", placeholder = "Your name", leadingIcon = Icons.Filled.Person)
                    Spacer(modifier = Modifier.height(8.dp))
                    ChamaTextField(value = editPhone, onValueChange = { editPhone = it }, label = "Phone Number", placeholder = "+254...", leadingIcon = Icons.Filled.Phone, keyboardType = KeyboardType.Phone)
                } else {
                    ProfileRow(Icons.Filled.Person, "Full Name", editName)
                    HorizontalDivider(color = ChamaOutline)
                    ProfileRow(Icons.Filled.Email, "Email", userEmail.ifEmpty { "Not set" })
                    HorizontalDivider(color = ChamaOutline)
                    ProfileRow(Icons.Filled.Phone, "Phone", editPhone.ifEmpty { "Not set" })
                    HorizontalDivider(color = ChamaOutline)
                    ProfileRow(Icons.Filled.Groups, "Chama", chamName)
                    HorizontalDivider(color = ChamaOutline)
                    ProfileRow(Icons.Filled.ManageAccounts, "Role", userRole)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Account actions
            ProfileSection(title = "Account") {
                ProfileActionRow(
                    icon = Icons.Filled.Lock,
                    label = "Change Password",
                    onClick = { showPasswordSheet = true }
                )
                HorizontalDivider(color = ChamaOutline)
                ProfileActionRow(
                    icon = Icons.Filled.Notifications,
                    label = "Notification Preferences",
                    onClick = {}
                )
                HorizontalDivider(color = ChamaOutline)
                ProfileActionRow(
                    icon = Icons.Filled.Share,
                    label = "Invite Members",
                    subtitle = "Share chama invite code",
                    onClick = {}
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // App info
            ProfileSection(title = "App") {
                ProfileRow(Icons.Filled.Info, "Version", "1.0.0")
                HorizontalDivider(color = ChamaOutline)
                ProfileRow(Icons.Filled.Policy, "Terms & Privacy", "View")
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Logout
            Card(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = ChamaSurface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp).clickable { showLogoutDialog = true },
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(Icons.AutoMirrored.Filled.Logout, null, tint = ChamaRed)
                    Text("Sign Out", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = ChamaRed, modifier = Modifier.weight(1f))
                    Icon(Icons.Filled.ChevronRight, null, tint = ChamaRed)
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }

    // Change password sheet
    if (showPasswordSheet) {
        ChangePasswordSheet(
            onDismiss = { showPasswordSheet = false },
            onSave = { showPasswordSheet = false }
        )
    }

    // Logout dialog
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            icon = { Icon(Icons.AutoMirrored.Filled.Logout, null, tint = ChamaRed) },
            title = { Text("Sign Out") },
            text = { Text("Are you sure you want to sign out of ChamaFlow?") },
            confirmButton = {
                Button(onClick = { showLogoutDialog = false; onLogout() }, colors = ButtonDefaults.buttonColors(containerColor = ChamaRed)) { Text("Sign Out") }
            },
            dismissButton = { TextButton(onClick = { showLogoutDialog = false }) { Text("Cancel") } }
        )
    }
}

// ─── Change Password Sheet ────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ChangePasswordSheet(onDismiss: () -> Unit, onSave: () -> Unit) {
    var current by remember { mutableStateOf("") }
    var newPass by remember { mutableStateOf("") }
    var confirm by remember { mutableStateOf("") }
    val mismatch = confirm.isNotEmpty() && newPass != confirm
    val valid = current.isNotEmpty() && newPass.length >= 6 && !mismatch

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = ChamaSurface
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp).padding(bottom = 32.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Change Password", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                IconButton(onClick = onDismiss) { Icon(Icons.Filled.Close, null, tint = ChamaTextSecondary) }
            }
            HorizontalDivider(color = ChamaOutline)
            OutlinedTextField(value = current, onValueChange = { current = it }, label = { Text("Current Password") }, leadingIcon = { Icon(Icons.Filled.Lock, null, tint = ChamaTextSecondary) }, visualTransformation = PasswordVisualTransformation(), modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), singleLine = true, colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = ChamaBlue, unfocusedBorderColor = ChamaOutline))
            OutlinedTextField(value = newPass, onValueChange = { newPass = it }, label = { Text("New Password") }, leadingIcon = { Icon(Icons.Filled.LockOpen, null, tint = ChamaTextSecondary) }, visualTransformation = PasswordVisualTransformation(), modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), singleLine = true, colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = ChamaBlue, unfocusedBorderColor = ChamaOutline))
            OutlinedTextField(value = confirm, onValueChange = { confirm = it }, label = { Text("Confirm New Password") }, leadingIcon = { Icon(Icons.Filled.LockOpen, null, tint = ChamaTextSecondary) }, visualTransformation = PasswordVisualTransformation(), isError = mismatch, supportingText = if (mismatch) { { Text("Passwords do not match", color = ChamaRed) } } else null, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), singleLine = true, colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = ChamaBlue, unfocusedBorderColor = ChamaOutline))
            Button(onClick = { if (valid) onSave() }, enabled = valid, modifier = Modifier.fillMaxWidth().height(52.dp), shape = RoundedCornerShape(12.dp)) {
                Text("Update Password", fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

// ─── Helpers ──────────────────────────────────────────────────────────────────

@Composable
private fun ProfileSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(title, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = ChamaTextSecondary)
        Card(shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = ChamaSurface), elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)) {
            Column(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                content()
            }
        }
    }
}

@Composable
private fun ProfileRow(icon: ImageVector, label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Icon(icon, null, tint = ChamaTextSecondary, modifier = Modifier.size(18.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(label, style = MaterialTheme.typography.labelSmall, color = ChamaTextSecondary)
            Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
private fun ProfileActionRow(icon: ImageVector, label: String, subtitle: String? = null, onClick: () -> Unit) {
    Row(modifier = Modifier.fillMaxWidth().clickable { onClick() }, verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Icon(icon, null, tint = ChamaBlue, modifier = Modifier.size(18.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(label, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
            subtitle?.let { Text(it, style = MaterialTheme.typography.labelSmall, color = ChamaTextSecondary) }
        }
        Icon(Icons.Filled.ChevronRight, null, tint = ChamaTextSecondary)
    }
}

private fun Modifier.clickable(onClick: () -> Unit): Modifier =
    this.then(Modifier.clickable { onClick() })
