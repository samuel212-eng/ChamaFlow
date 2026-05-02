package com.chamaflow.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chamaflow.data.models.*
import com.chamaflow.ui.theme.*

@Composable
fun StatCard(title: String, value: String, icon: ImageVector, containerColor: Color, iconColor: Color, modifier: Modifier = Modifier, subtitle: String? = null, onClick: (() -> Unit)? = null) {
    Card(
        modifier = modifier
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier)
            .shadow(elevation = 8.dp, shape = RoundedCornerShape(20.dp), spotColor = iconColor.copy(alpha = 0.2f)),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(containerColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, title, tint = iconColor, modifier = Modifier.size(24.dp))
            }
            Column {
                Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold)
                Text(title, style = MaterialTheme.typography.labelMedium, color = TextSecondary, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            subtitle?.let { 
                Text(
                    it, 
                    style = MaterialTheme.typography.labelSmall, 
                    color = Accent,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.background(Accent.copy(alpha = 0.1f), RoundedCornerShape(4.dp)).padding(horizontal = 6.dp, vertical = 2.dp)
                ) 
            }
        }
    }
}

@Composable
fun SectionHeader(title: String, actionLabel: String = "See all", onAction: () -> Unit = {}) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = Primary
        )
        TextButton(
            onClick = onAction,
            contentPadding = PaddingValues(horizontal = 8.dp)
        ) {
            Text(
                actionLabel,
                color = Secondary,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun MemberAvatar(name: String, size: Dp = 44.dp, backgroundColor: Color = Color(0xFFE8F0FE), textColor: Color = Color(0xFF1A56DB)) {
    val initials = name.split(" ").filter { it.isNotEmpty() }.take(2).joinToString("") { it.firstOrNull()?.uppercase() ?: "" }
    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(Brush.linearGradient(listOf(backgroundColor, backgroundColor.copy(alpha = 0.7f)))),
        contentAlignment = Alignment.Center
    ) {
        Text(initials, color = textColor, fontWeight = FontWeight.ExtraBold, fontSize = (size.value * 0.38).sp)
    }
}

@Composable
fun MemberListRow(member: Member, onClick: () -> Unit = {}) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 20.dp, vertical = 4.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            MemberAvatar(name = member.fullName, backgroundColor = Secondary.copy(alpha = 0.1f), textColor = Secondary)
            Column(modifier = Modifier.weight(1f)) {
                Text(member.fullName, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                Text(member.phoneNumber, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
            }
            Column(horizontalAlignment = Alignment.End) {
                StatusChip(status = member.status.name)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "KES ${"%,.0f".format(member.totalContributions)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Accent,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }
    }
}

@Composable
fun StatusChip(status: String) {
    val (bg, text) = when (status.uppercase()) {
        "ACTIVE", "PAID", "REPAID"        -> Pair(Color(0xFFDCFCE7), Color(0xFF059669))
        "INACTIVE", "UNPAID", "REJECTED"  -> Pair(Color(0xFFFEE2E2), Color(0xFFDC2626))
        "OVERDUE"                          -> Pair(Color(0xFFFEF2F2), Color(0xFFB91C1C))
        "PARTIAL"                          -> Pair(Color(0xFFFFEDD5), Color(0xFFD97706))
        "PENDING"                          -> Pair(Color(0xFFFEF3C7), Color(0xFF92400E))
        "UPCOMING"                         -> Pair(Color(0xFFDBEAFE), Color(0xFF2563EB))
        "COMPLETED"                        -> Pair(Color(0xFFDCFCE7), Color(0xFF059669))
        else                               -> Pair(Color(0xFFF1F5F9), Color(0xFF64748B))
    }
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = bg
    ) {
        Text(
            status.replace("_", " ").lowercase().replaceFirstChar { it.uppercase() },
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            color = text,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun ContributionRow(contribution: Contribution, onClick: () -> Unit = {}) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 20.dp, vertical = 4.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (contribution.status == ContributionStatus.PAID) Color(0xFFDCFCE7) else Color(0xFFFEE2E2)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    if (contribution.status == ContributionStatus.PAID) Icons.Filled.CheckCircle else Icons.Filled.History,
                    null,
                    tint = if (contribution.status == ContributionStatus.PAID) Color(0xFF059669) else Color(0xFFDC2626),
                    modifier = Modifier.size(24.dp)
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(contribution.memberName, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                Text("${contribution.month} · ${contribution.paymentMethod.name.replace("_", " ")}", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    "KES ${"%,.0f".format(contribution.amount)}",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = Primary
                )
                StatusChip(status = contribution.status.name)
            }
        }
    }
}

@Composable
fun LoanProgressCard(loan: Loan, onClick: () -> Unit = {}, onRepay: (() -> Unit)? = null) {
    val progress = if (loan.totalRepayable > 0) (loan.amountPaid / loan.totalRepayable).toFloat().coerceIn(0f, 1f) else 0f
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .shadow(elevation = 10.dp, shape = RoundedCornerShape(24.dp), spotColor = Secondary.copy(alpha = 0.1f)),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Surface)
    ) {
        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    MemberAvatar(name = loan.memberName, size = 44.dp, backgroundColor = Secondary.copy(alpha = 0.1f), textColor = Secondary)
                    Column {
                        Text(loan.memberName, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                        Text("Due: ${loan.dueDate}", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                    }
                }
                StatusChip(status = loan.status.name)
            }
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text("Total Payable", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
                    Text("KES ${"%,.0f".format(loan.totalRepayable)}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Remaining Balance", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
                    Text("KES ${"%,.0f".format(loan.remainingBalance)}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold, color = if (loan.status == LoanStatus.OVERDUE) Error else Primary)
                }
            }
            
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp)
                        .clip(CircleShape),
                    color = if (loan.status == LoanStatus.OVERDUE) Error else Secondary,
                    trackColor = Color(0xFFF1F5F9)
                )
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("${(progress * 100).toInt()}% repaid", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = TextSecondary)
                    Text("KES ${"%,.0f".format(loan.amountPaid)} paid", style = MaterialTheme.typography.labelSmall, color = Accent, fontWeight = FontWeight.Bold)
                }
            }
            
            if (onRepay != null && loan.status != LoanStatus.REPAID) {
                Button(
                    onClick = onRepay,
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Primary)
                ) {
                    Icon(Icons.Filled.Payments, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Make Repayment", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun EmptyState(icon: ImageVector, title: String, subtitle: String, actionLabel: String? = null, onAction: () -> Unit = {}) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 48.dp, horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
                .background(Brush.radialGradient(listOf(Secondary.copy(alpha = 0.15f), Color.Transparent))),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, tint = Secondary, modifier = Modifier.size(48.dp))
        }
        Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
        Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = TextSecondary, textAlign = TextAlign.Center)
        actionLabel?.let { 
            Button(
                onClick = onAction,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.height(48.dp).padding(top = 8.dp)
            ) { 
                Text(it, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
            } 
        }
    }
}

@Composable
fun BalanceBanner(chamaName: String, balance: Double, memberCount: Int) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp)
            .height(200.dp)
            .shadow(elevation = 20.dp, shape = RoundedCornerShape(28.dp), spotColor = Primary.copy(alpha = 0.5f)),
        shape = RoundedCornerShape(28.dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.linearGradient(PremiumGradient))
        ) {
            // Decorative shapes
            Box(
                modifier = Modifier
                    .size(150.dp)
                    .offset(x = 180.dp, y = (-50).dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.05f))
            )
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .offset(x = (-30).dp, y = 140.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.05f))
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Filled.Security, null, tint = Accent, modifier = Modifier.size(16.dp))
                        Text(chamaName, style = MaterialTheme.typography.labelLarge, color = Color.White.copy(alpha = 0.9f), fontWeight = FontWeight.Bold)
                    }
                    Text("Premium Member", style = MaterialTheme.typography.labelSmall, color = Warning, fontWeight = FontWeight.Bold, modifier = Modifier.background(Warning.copy(alpha = 0.1f), RoundedCornerShape(4.dp)).padding(horizontal = 6.dp, vertical = 2.dp))
                }

                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Total Available Balance", style = MaterialTheme.typography.labelMedium, color = Color.White.copy(alpha = 0.6f))
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("KES", style = MaterialTheme.typography.titleLarge, color = Color.White.copy(alpha = 0.7f), fontWeight = FontWeight.Normal)
                        Text(
                            "%,.2f".format(balance),
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White,
                            letterSpacing = (-1).sp
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Icon(Icons.Filled.Groups, null, tint = Color.White.copy(alpha = 0.7f), modifier = Modifier.size(14.dp))
                        Text("$memberCount Active Members", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.7f))
                    }
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, null, tint = Color.White.copy(alpha = 0.5f), modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}
