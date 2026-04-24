package com.chamaflow.ui.screens.chama

import androidx.compose.animation.*
import androidx.compose.foundation.background
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.chamaflow.data.models.Chama
import com.chamaflow.data.models.MeetingFrequency
import com.chamaflow.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateChamaScreen(
    onBack: () -> Unit = {},
    onSave: (Chama) -> Unit = {},
    isLoading: Boolean = false,
    errorMessage: String? = null
) {
    var step by remember { mutableIntStateOf(0) }

    // Step 1
    var chamaName by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var goal by remember { mutableStateOf("") }

    // Step 2
    var contributionAmount by remember { mutableStateOf("5000") }
    var penaltyAmount by remember { mutableStateOf("500") }
    var joiningFee by remember { mutableStateOf("0") }
    var interestRate by remember { mutableStateOf("10") }
    var meetingFrequency by remember { mutableStateOf(MeetingFrequency.MONTHLY) }
    var freqDropdownOpen by remember { mutableStateOf(false) }

    val canProceed = when (step) { 0 -> chamaName.length >= 3; 1 -> (contributionAmount.toDoubleOrNull() ?: 0.0) > 0; else -> true }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(listOf("Create Chama", "Financial Rules", "Review & Create")[step], fontWeight = FontWeight.Bold, color = Color.White) },
                navigationIcon = { IconButton(onClick = { if (step > 0) step-- else onBack() }) { Icon(Icons.Filled.ArrowBack, null, tint = Color.White) } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = ChamaBlue)
            )
        },
        containerColor = ChamaBackground
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {

            // Step progress bar
            Column(modifier = Modifier.fillMaxWidth().background(ChamaBlue).padding(horizontal = 24.dp, vertical = 16.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    listOf("Identity", "Rules", "Review").forEachIndexed { index, label ->
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                            Box(modifier = Modifier.size(32.dp).clip(CircleShape).background(when { index < step -> ChamaGreen; index == step -> Color.White; else -> Color.White.copy(alpha = 0.3f) }), contentAlignment = Alignment.Center) {
                                if (index < step) Icon(Icons.Filled.Check, null, tint = Color.White, modifier = Modifier.size(16.dp))
                                else Text("${index + 1}", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = if (index == step) ChamaBlue else Color.White.copy(alpha = 0.6f))
                            }
                            Text(label, style = MaterialTheme.typography.labelSmall, color = if (index <= step) Color.White else Color.White.copy(alpha = 0.5f), fontWeight = if (index == step) FontWeight.Bold else FontWeight.Normal)
                        }
                        if (index < 2) HorizontalDivider(modifier = Modifier.weight(0.5f), color = if (step > index) ChamaGreen else Color.White.copy(alpha = 0.3f), thickness = 2.dp)
                    }
                }
            }

            Column(modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 24.dp, vertical = 16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                errorMessage?.let {
                    Surface(shape = RoundedCornerShape(10.dp), color = ChamaRedLight) {
                        Row(modifier = Modifier.fillMaxWidth().padding(12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) { Icon(Icons.Filled.ErrorOutline, null, tint = ChamaRed, modifier = Modifier.size(18.dp)); Text(it, style = MaterialTheme.typography.bodySmall, color = ChamaRed) }
                    }
                }

                AnimatedContent(targetState = step, transitionSpec = { if (targetState > initialState) slideInHorizontally { it } + fadeIn() togetherWith slideOutHorizontally { -it } + fadeOut() else slideInHorizontally { -it } + fadeIn() togetherWith slideOutHorizontally { it } + fadeOut() }, label = "step") { s ->
                    when (s) {
                        0 -> Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            StepCard("Group Identity", Icons.Filled.Groups) {
                                SetupField(chamaName, { chamaName = it }, "Chama Name *", "e.g. Pamoja Savings Group", Icons.Filled.Groups, isError = chamaName.isNotEmpty() && chamaName.length < 3, errorMsg = "Name must be at least 3 characters")
                                SetupField(description, { description = it }, "Description", "What is this chama about?", Icons.Filled.Description, singleLine = false)
                                SetupField(goal, { goal = it }, "Group Goal", "e.g. Buy land together by 2027", Icons.Filled.Flag)
                            }
                            Surface(shape = RoundedCornerShape(12.dp), color = ChamaBlueLight) {
                                Row(modifier = Modifier.fillMaxWidth().padding(14.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) { Icon(Icons.Filled.Lightbulb, null, tint = ChamaBlue, modifier = Modifier.size(18.dp)); Text("A clear group goal keeps members motivated. You can change these details later in Settings.", style = MaterialTheme.typography.bodySmall, color = ChamaBlueDark) }
                            }
                        }
                        1 -> Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            StepCard("Contributions", Icons.Filled.Savings) {
                                SetupField(contributionAmount, { contributionAmount = it.filter { c -> c.isDigit() } }, "Monthly Contribution (KES) *", "e.g. 5000", Icons.Filled.AttachMoney, keyboardType = KeyboardType.Number)
                                SetupField(joiningFee, { joiningFee = it.filter { c -> c.isDigit() } }, "Joining Fee (KES)", "0 if none", Icons.Filled.PersonAdd, keyboardType = KeyboardType.Number)
                            }
                            StepCard("Penalties & Loans", Icons.Filled.Gavel) {
                                SetupField(penaltyAmount, { penaltyAmount = it.filter { c -> c.isDigit() } }, "Penalty Amount (KES)", "e.g. 500", Icons.Filled.Warning, keyboardType = KeyboardType.Number)
                                SetupField(interestRate, { interestRate = it.filter { c -> c.isDigit() } }, "Loan Interest Rate (%)", "10", Icons.Filled.Percent, keyboardType = KeyboardType.Number)
                            }
                            StepCard("Meeting Frequency", Icons.Filled.EventNote) {
                                ExposedDropdownMenuBox(expanded = freqDropdownOpen, onExpandedChange = { freqDropdownOpen = it }) {
                                    OutlinedTextField(value = meetingFrequency.name.lowercase().replaceFirstChar { it.uppercase() }, onValueChange = {}, readOnly = true, label = { Text("Meeting Frequency") }, leadingIcon = { Icon(Icons.Filled.CalendarMonth, null, tint = ChamaTextSecondary) }, trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = freqDropdownOpen) }, modifier = Modifier.fillMaxWidth().menuAnchor(), shape = RoundedCornerShape(10.dp), colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = ChamaBlue, unfocusedBorderColor = ChamaOutline))
                                    ExposedDropdownMenu(expanded = freqDropdownOpen, onDismissRequest = { freqDropdownOpen = false }) {
                                        MeetingFrequency.entries.forEach { freq -> DropdownMenuItem(text = { Text(freq.name.lowercase().replaceFirstChar { it.uppercase() }) }, onClick = { meetingFrequency = freq; freqDropdownOpen = false }) }
                                    }
                                }
                            }
                        }
                        else -> Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            // Preview card
                            Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = ChamaBlue)) {
                                Column(modifier = Modifier.fillMaxWidth().padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Box(modifier = Modifier.size(64.dp).background(Color.White.copy(alpha = 0.2f), RoundedCornerShape(16.dp)), contentAlignment = Alignment.Center) { Text(chamaName.take(2).uppercase(), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = Color.White) }
                                    Text(chamaName, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Color.White)
                                    if (description.isNotEmpty()) Text(description, style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.8f), textAlign = TextAlign.Center)
                                    if (goal.isNotEmpty()) Surface(shape = RoundedCornerShape(20.dp), color = Color.White.copy(alpha = 0.2f)) { Row(modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp), horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Filled.Flag, null, tint = ChamaGold, modifier = Modifier.size(14.dp)); Text(goal, style = MaterialTheme.typography.labelSmall, color = Color.White) } }
                                }
                            }
                            StepCard("Rules Summary", Icons.Filled.Rule) {
                                listOf("Monthly Contribution" to "KES ${"%,.0f".format(contributionAmount.toDoubleOrNull() ?: 0.0)}", "Penalty Amount" to "KES ${"%,.0f".format(penaltyAmount.toDoubleOrNull() ?: 0.0)}", "Joining Fee" to if ((joiningFee.toDoubleOrNull() ?: 0.0) == 0.0) "None" else "KES ${joiningFee}", "Loan Interest" to "${interestRate}% flat rate", "Meeting Frequency" to meetingFrequency.name.lowercase().replaceFirstChar { it.uppercase() }).forEachIndexed { i, (label, value) ->
                                    if (i > 0) HorizontalDivider(color = ChamaOutline)
                                    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text(label, style = MaterialTheme.typography.bodySmall, color = ChamaTextSecondary)
                                        Text(value, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                                    }
                                }
                            }
                            Surface(shape = RoundedCornerShape(12.dp), color = ChamaGreenLight) {
                                Row(modifier = Modifier.fillMaxWidth().padding(14.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) { Icon(Icons.Filled.CheckCircle, null, tint = ChamaGreen, modifier = Modifier.size(18.dp)); Text("Once created, you'll get an invite code to share with members.", style = MaterialTheme.typography.bodySmall, color = ChamaGreenDark) }
                            }
                        }
                    }
                }
            }

            // Bottom buttons
            Surface(shadowElevation = 8.dp, color = ChamaSurface) {
                Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    if (step > 0) OutlinedButton(onClick = { step-- }, modifier = Modifier.weight(1f).height(52.dp), shape = RoundedCornerShape(12.dp)) { Text("Back") }
                    Button(onClick = { 
                        if (step < 2) step++ 
                        else {
                            val chama = Chama(
                                name = chamaName,
                                description = description,
                                goal = goal,
                                contributionAmount = contributionAmount.toDoubleOrNull() ?: 0.0,
                                penaltyAmount = penaltyAmount.toDoubleOrNull() ?: 0.0,
                                joiningFee = joiningFee.toDoubleOrNull() ?: 0.0,
                                loanInterestRate = (interestRate.toDoubleOrNull() ?: 0.0) / 100.0,
                                meetingFrequency = meetingFrequency
                            )
                            onSave(chama)
                        }
                    }, enabled = canProceed && !isLoading, modifier = Modifier.weight(1f).height(52.dp), shape = RoundedCornerShape(12.dp), colors = ButtonDefaults.buttonColors(containerColor = ChamaBlue)) {
                        if (isLoading) CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                        else { Text(if (step < 2) "Continue" else "Create Chama", fontWeight = FontWeight.SemiBold); if (step < 2) { Spacer(Modifier.width(4.dp)); Icon(Icons.Filled.ArrowForward, null, modifier = Modifier.size(16.dp)) } }
                    }
                }
            }
        }
    }
}

@Composable
private fun StepCard(title: String, icon: ImageVector, content: @Composable ColumnScope.() -> Unit) {
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = ChamaSurface), elevation = CardDefaults.cardElevation(2.dp)) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) { Icon(icon, null, tint = ChamaBlue, modifier = Modifier.size(18.dp)); Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold) }
            content()
        }
    }
}

@Composable
private fun SetupField(value: String, onValueChange: (String) -> Unit, label: String, placeholder: String, icon: ImageVector, keyboardType: KeyboardType = KeyboardType.Text, isError: Boolean = false, errorMsg: String = "", singleLine: Boolean = true) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        OutlinedTextField(value = value, onValueChange = onValueChange, label = { Text(label) }, placeholder = { Text(placeholder, color = ChamaTextMuted) }, leadingIcon = { Icon(icon, null, tint = ChamaTextSecondary) }, keyboardOptions = KeyboardOptions(keyboardType = keyboardType), isError = isError, singleLine = singleLine, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp), colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = ChamaBlue, unfocusedBorderColor = ChamaOutline))
        if (isError && errorMsg.isNotEmpty()) Text(errorMsg, style = MaterialTheme.typography.labelSmall, color = ChamaRed, modifier = Modifier.padding(start = 4.dp))
    }
}
