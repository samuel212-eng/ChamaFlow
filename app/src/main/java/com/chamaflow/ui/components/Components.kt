package com.chamaflow.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chamaflow.data.models.*
import com.chamaflow.ui.theme.*

@Composable
fun StatCard(title: String, value: String, icon: ImageVector, containerColor: Color, iconColor: Color, modifier: Modifier = Modifier, subtitle: String? = null, onClick: (() -> Unit)? = null) {
    Card(modifier = modifier.then(if (onClick != null) Modifier.clickable { onClick() } else Modifier), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Box(modifier = Modifier.size(40.dp).clip(RoundedCornerShape(10.dp)).background(containerColor), contentAlignment = Alignment.Center) {
                Icon(icon, title, tint = iconColor, modifier = Modifier.size(20.dp))
            }
            Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text(title, style = MaterialTheme.typography.bodySmall, color = ChamaTextSecondary, maxLines = 1, overflow = TextOverflow.Ellipsis)
            subtitle?.let { Text(it, style = MaterialTheme.typography.labelSmall, color = ChamaGreen) }
        }
    }
}

@Composable
fun SectionHeader(title: String, actionLabel: String = "See all", onAction: () -> Unit = {}) {
    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        TextButton(onClick = onAction) { Text(actionLabel, color = ChamaBlue, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold) }
    }
}

@Composable
fun MemberAvatar(name: String, size: Dp = 44.dp, backgroundColor: Color = ChamaBlueLight, textColor: Color = ChamaBlue) {
    val initials = name.split(" ").take(2).joinToString("") { it.firstOrNull()?.uppercase() ?: "" }
    Box(modifier = Modifier.size(size).clip(CircleShape).background(backgroundColor), contentAlignment = Alignment.Center) {
        Text(initials, color = textColor, fontWeight = FontWeight.Bold, fontSize = (size.value * 0.35).sp)
    }
}

@Composable
fun MemberListRow(member: Member, onClick: () -> Unit = {}) {
    Card(modifier = Modifier.fillMaxWidth().clickable { onClick() }, shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)) {
        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            MemberAvatar(name = member.fullName)
            Column(modifier = Modifier.weight(1f)) {
                Text(member.fullName, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                Text(member.phoneNumber, style = MaterialTheme.typography.bodySmall, color = ChamaTextSecondary)
            }
            Column(horizontalAlignment = Alignment.End) {
                StatusChip(status = member.status.name)
                Spacer(modifier = Modifier.height(4.dp))
                Text("KES ${"%,.0f".format(member.totalContributions)}", style = MaterialTheme.typography.labelSmall, color = ChamaGreen, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
fun StatusChip(status: String) {
    val (bg, text) = when (status.uppercase()) {
        "ACTIVE", "PAID", "REPAID"        -> Pair(ChamaGreenLight, ChamaGreenDark)
        "INACTIVE", "UNPAID", "REJECTED"  -> Pair(ChamaRedLight, ChamaRed)
        "OVERDUE"                          -> Pair(ChamaRedLight, ChamaRed)
        "PARTIAL"                          -> Pair(ChamaOrangeLight, ChamaOrange)
        "PENDING"                          -> Pair(ChamaGoldLight, Color(0xFF92400E))
        "UPCOMING"                         -> Pair(ChamaBlueLight, ChamaBlue)
        "COMPLETED"                        -> Pair(ChamaGreenLight, ChamaGreenDark)
        else                               -> Pair(ChamaOutline, ChamaTextSecondary)
    }
    Surface(shape = RoundedCornerShape(20.dp), color = bg) {
        Text(status.replace("_", " ").lowercase().replaceFirstChar { it.uppercase() }, modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp), style = MaterialTheme.typography.labelSmall, color = text, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
fun ContributionRow(contribution: Contribution, onClick: () -> Unit = {}) {
    Card(modifier = Modifier.fillMaxWidth().clickable { onClick() }, shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)) {
        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(modifier = Modifier.size(40.dp).clip(RoundedCornerShape(10.dp)).background(if (contribution.status == ContributionStatus.PAID) ChamaGreenLight else ChamaRedLight), contentAlignment = Alignment.Center) {
                Icon(if (contribution.status == ContributionStatus.PAID) Icons.Filled.CheckCircle else Icons.Filled.Warning, null, tint = if (contribution.status == ContributionStatus.PAID) ChamaGreen else ChamaRed, modifier = Modifier.size(20.dp))
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(contribution.memberName, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                Text("${contribution.month} · ${contribution.paymentMethod.name.replace("_", " ")}", style = MaterialTheme.typography.bodySmall, color = ChamaTextSecondary)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("KES ${"%,.0f".format(contribution.amount)}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                StatusChip(status = contribution.status.name)
            }
        }
    }
}

@Composable
fun LoanProgressCard(loan: Loan, onClick: () -> Unit = {}, onRepay: (() -> Unit)? = null) {
    val progress = if (loan.totalRepayable > 0) (loan.amountPaid / loan.totalRepayable).toFloat().coerceIn(0f, 1f) else 0f
    Card(modifier = Modifier.fillMaxWidth().clickable { onClick() }, shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    MemberAvatar(name = loan.memberName, size = 36.dp)
                    Column {
                        Text(loan.memberName, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                        Text("Due: ${loan.dueDate}", style = MaterialTheme.typography.bodySmall, color = ChamaTextSecondary)
                    }
                }
                StatusChip(status = loan.status.name)
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text("Loan Amount", style = MaterialTheme.typography.labelSmall, color = ChamaTextSecondary)
                    Text("KES ${"%,.0f".format(loan.amount)}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Remaining", style = MaterialTheme.typography.labelSmall, color = ChamaTextSecondary)
                    Text("KES ${"%,.0f".format(loan.remainingBalance)}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = if (loan.status == LoanStatus.OVERDUE) ChamaRed else ChamaTextPrimary)
                }
            }
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("${(progress * 100).toInt()}% repaid", style = MaterialTheme.typography.labelSmall, color = ChamaTextSecondary)
                    Text("KES ${"%,.0f".format(loan.amountPaid)} paid", style = MaterialTheme.typography.labelSmall, color = ChamaGreen, fontWeight = FontWeight.SemiBold)
                }
                LinearProgressIndicator(progress = { progress }, modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)), color = if (loan.status == LoanStatus.OVERDUE) ChamaRed else ChamaGreen, trackColor = ChamaOutline)
            }
            
            if (onRepay != null && loan.status != LoanStatus.REPAID) {
                Button(
                    onClick = onRepay,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = ChamaGreen)
                ) {
                    Icon(Icons.Filled.Payments, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Repay Loan")
                }
            }
        }
    }
}

@Composable
fun EmptyState(icon: ImageVector, title: String, subtitle: String, actionLabel: String? = null, onAction: () -> Unit = {}) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 48.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Box(modifier = Modifier.size(80.dp).clip(CircleShape).background(ChamaBlueLight), contentAlignment = Alignment.Center) {
            Icon(icon, null, tint = ChamaBlue, modifier = Modifier.size(40.dp))
        }
        Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        Text(subtitle, style = MaterialTheme.typography.bodySmall, color = ChamaTextSecondary)
        actionLabel?.let { Button(onClick = onAction, shape = RoundedCornerShape(12.dp)) { Text(it) } }
    }
}

@Composable
fun BalanceBanner(chamaName: String, balance: Double, memberCount: Int) {
    Box(modifier = Modifier.fillMaxWidth().background(brush = Brush.linearGradient(listOf(ChamaBlue, ChamaBlueDark)), shape = RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp)).padding(horizontal = 24.dp, vertical = 28.dp)) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Filled.Groups, null, tint = Color.White.copy(alpha = 0.8f), modifier = Modifier.size(18.dp))
                Text(chamaName, style = MaterialTheme.typography.titleMedium, color = Color.White.copy(alpha = 0.9f), fontWeight = FontWeight.SemiBold)
            }
            Text("Group Balance", style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.7f))
            Text("KES ${"%,.0f".format(balance)}", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold, color = Color.White)
            Text("$memberCount active members", style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.7f))
        }
    }
}
