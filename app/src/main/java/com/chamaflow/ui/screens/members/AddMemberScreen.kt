package com.chamaflow.ui.screens.members

import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.chamaflow.data.models.Member
import com.chamaflow.data.models.MemberRole
import com.chamaflow.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddMemberScreen(onBack: () -> Unit = {}, onSave: (Member) -> Unit = {}) {
    var fullName by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var nationalId by remember { mutableStateOf("") }
    var role by remember { mutableStateOf(MemberRole.MEMBER) }
    var roleDropdownOpen by remember { mutableStateOf(false) }
    val phoneError = phone.isNotEmpty() && !phone.matches(Regex("^(\\+254|07|01)\\d{8,9}$"))
    val emailError = email.isNotEmpty() && !email.contains("@")
    val formValid = fullName.length >= 3 && phone.isNotEmpty() && !phoneError && !emailError

    Scaffold(
        topBar = { TopAppBar(title = { Text("Add Member", fontWeight = FontWeight.Bold, color = Color.White) }, navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, null, tint = Color.White) } }, colors = TopAppBarDefaults.topAppBarColors(containerColor = ChamaBlue)) },
        containerColor = ChamaBackground
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState())) {
            Column(modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Surface(shape = CircleShape, color = ChamaBlueLight, modifier = Modifier.size(80.dp)) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                        if (fullName.isNotBlank()) {
                            val initials = fullName.trim().split("\\s+".toRegex())
                                .take(2)
                                .mapNotNull { it.firstOrNull()?.toString()?.uppercase() }
                                .joinToString("")
                            Text(initials, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = ChamaBlue)
                        } else {
                            Icon(Icons.Filled.Person, null, tint = ChamaBlue, modifier = Modifier.size(40.dp))
                        }
                    }
                }
            }
            Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = ChamaSurface), elevation = CardDefaults.cardElevation(2.dp)) {
                Column(modifier = Modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    FormSectionLabel("Personal Information")
                    ChamaTextField(fullName, { fullName = it }, "Full Name *", "e.g. Amina Wanjiku", Icons.Filled.Person, isError = fullName.isNotEmpty() && fullName.length < 3, errorMessage = "Name must be at least 3 characters")
                    ChamaTextField(phone, { phone = it }, "Phone Number *", "e.g. 0712 345 678", Icons.Filled.Phone, keyboardType = KeyboardType.Phone, isError = phoneError, errorMessage = "Enter a valid Kenyan phone number")
                    ChamaTextField(email, { email = it }, "Email Address", "e.g. amina@email.com", Icons.Filled.Email, keyboardType = KeyboardType.Email, isError = emailError, errorMessage = "Enter a valid email")
                    ChamaTextField(nationalId, { nationalId = it }, "National ID (optional)", "e.g. 12345678", Icons.Filled.Badge, keyboardType = KeyboardType.Number)
                    HorizontalDivider(color = ChamaOutline)
                    FormSectionLabel("Role in Group")
                    ExposedDropdownMenuBox(expanded = roleDropdownOpen, onExpandedChange = { roleDropdownOpen = it }) {
                        OutlinedTextField(value = role.name.lowercase().replaceFirstChar { it.uppercase() }, onValueChange = {}, readOnly = true, label = { Text("Role") }, leadingIcon = { Icon(Icons.Filled.ManageAccounts, null, tint = ChamaTextSecondary) }, trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = roleDropdownOpen) }, modifier = Modifier.fillMaxWidth().menuAnchor(), shape = RoundedCornerShape(10.dp), colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = ChamaBlue, unfocusedBorderColor = ChamaOutline))
                        ExposedDropdownMenu(expanded = roleDropdownOpen, onDismissRequest = { roleDropdownOpen = false }) {
                            MemberRole.entries.forEach { r -> DropdownMenuItem(text = { Text(r.name.lowercase().replaceFirstChar { it.uppercase() }) }, onClick = { role = r; roleDropdownOpen = false }) }
                        }
                    }
                }
            }
            Spacer(Modifier.height(24.dp))
            Button(
                onClick = { 
                    if (formValid) {
                        val member = Member(
                            fullName = fullName,
                            phoneNumber = phone,
                            email = email,
                            nationalId = nationalId,
                            role = role,
                            joinDate = java.time.LocalDate.now().toString()
                        )
                        onSave(member)
                    } 
                }, 
                enabled = formValid, 
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp).height(52.dp), 
                shape = RoundedCornerShape(12.dp), 
                colors = ButtonDefaults.buttonColors(containerColor = ChamaBlue, disabledContainerColor = ChamaOutline)
            ) {
                Icon(Icons.Filled.PersonAdd, null, modifier = Modifier.size(18.dp)); Spacer(Modifier.width(8.dp)); Text("Add Member", fontWeight = FontWeight.SemiBold)
            }
            Spacer(Modifier.height(32.dp))
        }
    }
}

// ─── Shared form helpers used across screens ──────────────────────────────────

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
