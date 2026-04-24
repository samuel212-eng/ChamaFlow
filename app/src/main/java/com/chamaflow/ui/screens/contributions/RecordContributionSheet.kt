package com.chamaflow.ui.screens.contributions

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import com.chamaflow.data.models.*
import com.chamaflow.ui.components.MemberAvatar
import com.chamaflow.ui.theme.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecordContributionSheet(onDismiss: () -> Unit, onSave: (Contribution) -> Unit, members: List<Member> = emptyList(), defaultAmount: Double = 5_000.0, currentMonth: String = "") {
    var selectedMember by remember { mutableStateOf<Member?>(null) }
    var memberDropdownOpen by remember { mutableStateOf(false) }
    var amount by remember { mutableStateOf(defaultAmount.toInt().toString()) }
    var paymentMethod by remember { mutableStateOf(PaymentMethod.MPESA) }
    var mpesaRef by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    val isMpesa = paymentMethod == PaymentMethod.MPESA
    val mpesaRefError = isMpesa && mpesaRef.isNotEmpty() && !mpesaRef.matches(Regex("[A-Z0-9]{10}"))
    val formValid = selectedMember != null && (amount.toDoubleOrNull() ?: 0.0) > 0 && (!isMpesa || mpesaRef.isNotEmpty())

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true), containerColor = ChamaSurface, shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)) {
        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp).padding(bottom = 32.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column { Text("Record Payment", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold); Text(currentMonth, style = MaterialTheme.typography.bodySmall, color = ChamaTextSecondary) }
                IconButton(onClick = onDismiss) { Icon(Icons.Filled.Close, null, tint = ChamaTextSecondary) }
            }
            HorizontalDivider(color = ChamaOutline)
            ExposedDropdownMenuBox(expanded = memberDropdownOpen, onExpandedChange = { memberDropdownOpen = it }) {
                OutlinedTextField(value = selectedMember?.fullName ?: "", onValueChange = {}, readOnly = true, label = { Text("Select Member *") }, leadingIcon = { if (selectedMember != null) MemberAvatar(name = selectedMember!!.fullName, size = 28.dp) else Icon(Icons.Filled.Person, null, tint = ChamaTextSecondary) }, trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = memberDropdownOpen) }, placeholder = { Text("Choose a member", color = ChamaTextMuted) }, modifier = Modifier.fillMaxWidth().menuAnchor(), shape = RoundedCornerShape(12.dp), colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = ChamaGreen, unfocusedBorderColor = ChamaOutline))
                ExposedDropdownMenu(expanded = memberDropdownOpen, onDismissRequest = { memberDropdownOpen = false }) {
                    members.forEach { m -> DropdownMenuItem(text = { Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) { MemberAvatar(name = m.fullName, size = 28.dp); Column { Text(m.fullName, style = MaterialTheme.typography.bodyMedium); Text(m.phoneNumber, style = MaterialTheme.typography.labelSmall, color = ChamaTextSecondary) } } }, onClick = { selectedMember = m; memberDropdownOpen = false }) }
                }
            }
            OutlinedTextField(value = amount, onValueChange = { amount = it.filter { c -> c.isDigit() } }, label = { Text("Amount (KES) *") }, leadingIcon = { Icon(Icons.Filled.AttachMoney, null, tint = ChamaTextSecondary) }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), singleLine = true, colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = ChamaGreen, unfocusedBorderColor = ChamaOutline))
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Payment Method *", style = MaterialTheme.typography.labelMedium, color = ChamaTextSecondary, fontWeight = FontWeight.SemiBold)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    PaymentMethod.entries.forEach { method ->
                        FilterChip(selected = paymentMethod == method, onClick = { paymentMethod = method }, label = { Text(method.name.replace("_", " "), style = MaterialTheme.typography.labelSmall) }, colors = FilterChipDefaults.filterChipColors(selectedContainerColor = ChamaGreen, selectedLabelColor = Color.White))
                    }
                }
            }
            if (isMpesa) {
                OutlinedTextField(value = mpesaRef, onValueChange = { mpesaRef = it.uppercase().filter { c -> c.isLetterOrDigit() }.take(10) }, label = { Text("M-Pesa Reference *") }, placeholder = { Text("e.g. QHX2Y3ABCD", color = ChamaTextMuted) }, leadingIcon = { Box(modifier = Modifier.size(28.dp).background(Color(0xFF4CAF50), RoundedCornerShape(6.dp)), contentAlignment = Alignment.Center) { Text("M", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = Color.White) } }, trailingIcon = { if (mpesaRef.length == 10) Icon(Icons.Filled.CheckCircle, null, tint = ChamaGreen) }, isError = mpesaRefError, supportingText = { Text(if (mpesaRefError) "M-Pesa refs are 10 characters" else "Found in the M-Pesa confirmation SMS", color = if (mpesaRefError) ChamaRed else ChamaTextMuted) }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), singleLine = true, colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = ChamaGreen, unfocusedBorderColor = ChamaOutline))
                Surface(shape = RoundedCornerShape(10.dp), color = ChamaGreenLight) {
                    Row(modifier = Modifier.fillMaxWidth().padding(12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.Top) {
                        Icon(Icons.Filled.Info, null, tint = ChamaGreen, modifier = Modifier.size(16.dp))
                        Text("The M-Pesa reference is the code in the confirmation SMS. Example: QHX2Y3ABCD", style = MaterialTheme.typography.bodySmall, color = ChamaGreenDark)
                    }
                }
            }
            Button(onClick = {
                if (formValid) {
                    val today = LocalDate.now().format(DateTimeFormatter.ofPattern("MMM d, yyyy"))
                    val amt = amount.toDoubleOrNull() ?: 0.0
                    onSave(Contribution(memberId = selectedMember!!.id, memberName = selectedMember!!.fullName, amount = amt, date = today, month = currentMonth, paymentMethod = paymentMethod, notes = if (isMpesa) "Ref: $mpesaRef${if (notes.isNotEmpty()) " · $notes" else ""}" else notes, status = if (amt >= defaultAmount) ContributionStatus.PAID else ContributionStatus.PARTIAL))
                    onDismiss()
                }
            }, enabled = formValid, modifier = Modifier.fillMaxWidth().height(52.dp), shape = RoundedCornerShape(12.dp), colors = ButtonDefaults.buttonColors(containerColor = ChamaGreen, disabledContainerColor = ChamaOutline)) {
                Icon(Icons.Filled.CheckCircle, null, modifier = Modifier.size(18.dp)); Spacer(Modifier.width(8.dp)); Text("Save Contribution", fontWeight = FontWeight.SemiBold)
            }
        }
    }
}
