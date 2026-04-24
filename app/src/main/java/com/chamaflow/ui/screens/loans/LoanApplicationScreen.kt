package com.chamaflow.ui.screens.loans

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.chamaflow.ui.screens.members.ChamaTextField
import com.chamaflow.ui.screens.members.FormSectionLabel
import com.chamaflow.ui.theme.*

private val loanPurple = Color(0xFF6D28D9)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoanApplicationScreen(onBack: () -> Unit = {}, onSubmit: () -> Unit = {}) {
    var selectedMember by remember { mutableStateOf("") }
    var memberDropdownOpen by remember { mutableStateOf(false) }
    var loanAmount by remember { mutableStateOf("") }
    var repaymentPeriod by remember { mutableStateOf("3") }
    var periodDropdownOpen by remember { mutableStateOf(false) }
    var purpose by remember { mutableStateOf("") }
    val members = listOf("Select from your members list")
    val periods = listOf("1", "2", "3", "4", "6", "9", "12")
    val interestRate = 0.10
    val amount = loanAmount.toDoubleOrNull() ?: 0.0
    val months = repaymentPeriod.toIntOrNull() ?: 3
    val totalRepayable = amount + (amount * interestRate)
    val monthlyInstallment = if (months > 0) totalRepayable / months else 0.0
    val formValid = selectedMember.isNotEmpty() && amount > 0 && purpose.isNotEmpty()

    Scaffold(
        topBar = { TopAppBar(title = { Text("Loan Application", fontWeight = FontWeight.Bold, color = Color.White) }, navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, null, tint = Color.White) } }, colors = TopAppBarDefaults.topAppBarColors(containerColor = loanPurple)) },
        containerColor = ChamaBackground
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState())) {
            Box(modifier = Modifier.fillMaxWidth().background(loanPurple).padding(horizontal = 24.dp, vertical = 16.dp)) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Interest Rate: 10% flat", style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.85f))
                    Text("Max Loan: KES 100,000", style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.85f))
                }
            }
            Spacer(Modifier.height(20.dp))
            Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = ChamaSurface), elevation = CardDefaults.cardElevation(2.dp)) {
                Column(modifier = Modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    FormSectionLabel("Loan Details")
                    ExposedDropdownMenuBox(expanded = memberDropdownOpen, onExpandedChange = { memberDropdownOpen = it }) {
                        OutlinedTextField(value = selectedMember, onValueChange = {}, readOnly = true, label = { Text("Select Member *") }, leadingIcon = { Icon(Icons.Filled.Person, null, tint = ChamaTextSecondary) }, trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = memberDropdownOpen) }, placeholder = { Text("Choose a member", color = ChamaTextMuted) }, modifier = Modifier.fillMaxWidth().menuAnchor(), shape = RoundedCornerShape(10.dp), colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = loanPurple, unfocusedBorderColor = ChamaOutline))
                        ExposedDropdownMenu(expanded = memberDropdownOpen, onDismissRequest = { memberDropdownOpen = false }) {
                            members.forEach { DropdownMenuItem(text = { Text(it) }, onClick = { selectedMember = it; memberDropdownOpen = false }) }
                        }
                    }
                    ChamaTextField(loanAmount, { loanAmount = it.filter { c -> c.isDigit() } }, "Loan Amount (KES) *", "e.g. 20000", Icons.Filled.AttachMoney, keyboardType = KeyboardType.Number, isError = amount > 100_000, errorMessage = "Cannot exceed KES 100,000")
                    ExposedDropdownMenuBox(expanded = periodDropdownOpen, onExpandedChange = { periodDropdownOpen = it }) {
                        OutlinedTextField(value = "$repaymentPeriod month${if (repaymentPeriod != "1") "s" else ""}", onValueChange = {}, readOnly = true, label = { Text("Repayment Period *") }, leadingIcon = { Icon(Icons.Filled.Schedule, null, tint = ChamaTextSecondary) }, trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = periodDropdownOpen) }, modifier = Modifier.fillMaxWidth().menuAnchor(), shape = RoundedCornerShape(10.dp), colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = loanPurple, unfocusedBorderColor = ChamaOutline))
                        ExposedDropdownMenu(expanded = periodDropdownOpen, onDismissRequest = { periodDropdownOpen = false }) {
                            periods.forEach { p -> DropdownMenuItem(text = { Text("$p month${if (p != "1") "s" else ""}") }, onClick = { repaymentPeriod = p; periodDropdownOpen = false }) }
                        }
                    }
                    ChamaTextField(purpose, { purpose = it }, "Loan Purpose *", "e.g. School fees, business stock", Icons.Filled.Description, singleLine = false)
                    if (amount > 0) {
                        HorizontalDivider(color = ChamaOutline)
                        FormSectionLabel("Repayment Summary")
                        Surface(shape = RoundedCornerShape(10.dp), color = Color(0xFFEDE9FE)) {
                            Column(modifier = Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                SummaryRow("Loan Amount", "KES ${"%,.0f".format(amount)}")
                                SummaryRow("Interest (10%)", "KES ${"%,.0f".format(amount * interestRate)}")
                                HorizontalDivider(color = loanPurple.copy(alpha = 0.2f))
                                SummaryRow("Total Repayable", "KES ${"%,.0f".format(totalRepayable)}", bold = true, color = loanPurple)
                                SummaryRow("Monthly Installment", "KES ${"%,.0f".format(monthlyInstallment)}", bold = true, color = loanPurple)
                                SummaryRow("Period", "$repaymentPeriod months")
                            }
                        }
                    }
                }
            }
            Spacer(Modifier.height(24.dp))
            Button(onClick = { if (formValid) onSubmit() }, enabled = formValid && amount <= 100_000, modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp).height(52.dp), shape = RoundedCornerShape(12.dp), colors = ButtonDefaults.buttonColors(containerColor = loanPurple, disabledContainerColor = ChamaOutline)) {
                Icon(Icons.Filled.Send, null, modifier = Modifier.size(18.dp)); Spacer(Modifier.width(8.dp)); Text("Submit Application", fontWeight = FontWeight.SemiBold)
            }
            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
private fun SummaryRow(label: String, value: String, bold: Boolean = false, color: Color = ChamaTextPrimary) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = MaterialTheme.typography.bodySmall, color = ChamaTextSecondary, fontWeight = if (bold) FontWeight.SemiBold else FontWeight.Normal)
        Text(value, style = MaterialTheme.typography.bodySmall, fontWeight = if (bold) FontWeight.Bold else FontWeight.SemiBold, color = color)
    }
}
