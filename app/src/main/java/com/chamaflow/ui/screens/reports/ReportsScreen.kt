package com.chamaflow.ui.screens.reports

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.chamaflow.ui.components.StatCard
import com.chamaflow.ui.theme.*
import com.chamaflow.ui.viewmodel.ReportsViewModel
import com.chamaflow.util.ExportUtils

private val reportTeal = Color(0xFF0F766E)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen(
    chamaId: String,
    onBack: () -> Unit = {},
    viewModel: ReportsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var selectedPeriod by remember { mutableStateOf("This Year") }
    val periods = listOf("This Month", "This Quarter", "This Year", "All Time")
    var periodDropdownOpen by remember { mutableStateOf(false) }

    LaunchedEffect(chamaId) {
        viewModel.loadReports(chamaId)
    }

    fun exportFinancialStatement() {
        val csvContent = buildString {
            append("Chama Financial Report - $selectedPeriod\n")
            append("Metric,Value\n")
            append("Total Savings,${uiState.totalSavings}\n")
            append("Loans Issued,${uiState.loansIssued}\n")
            append("Loans Repaid,${uiState.loansRepaid}\n")
            append("Penalties Collected,${uiState.penaltiesCollected}\n")
            append("Welfare Fund Balance,${uiState.welfareBalance}\n")
            append("Total Chama Worth,${uiState.totalSavings + uiState.welfareBalance + uiState.penaltiesCollected}\n")
            append("\nTop Contributors\n")
            append("Name,Amount\n")
            uiState.topContributors.forEach { (name, amount) ->
                append("$name,$amount\n")
            }
        }
        ExportUtils.shareCsv(context, "Chama_Report_${System.currentTimeMillis()}.csv", csvContent)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Reports", fontWeight = FontWeight.Bold, color = Color.White) },
                navigationIcon = { 
                    IconButton(onClick = onBack) { 
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.White) 
                    } 
                },
                actions = { 
                    IconButton(onClick = { exportFinancialStatement() }) { 
                        Icon(Icons.Outlined.FileDownload, "Export", tint = Color.White) 
                    } 
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = reportTeal)
            )
        },
        containerColor = Background
    ) { padding ->
        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = reportTeal)
            }
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize().padding(padding)) {

                // Period selector
                item {
                    Box(modifier = Modifier.fillMaxWidth().background(reportTeal).padding(horizontal = 24.dp, vertical = 12.dp)) {
                        ExposedDropdownMenuBox(expanded = periodDropdownOpen, onExpandedChange = { periodDropdownOpen = it }) {
                            Surface(shape = RoundedCornerShape(20.dp), color = Color.White.copy(alpha = 0.2f), modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable)) {
                                Row(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Icon(Icons.Filled.DateRange, null, tint = Color.White, modifier = Modifier.size(16.dp))
                                    Text(selectedPeriod, style = MaterialTheme.typography.labelMedium, color = Color.White, fontWeight = FontWeight.SemiBold)
                                    Icon(Icons.Filled.ExpandMore, null, tint = Color.White, modifier = Modifier.size(16.dp))
                                }
                            }
                            ExposedDropdownMenu(expanded = periodDropdownOpen, onDismissRequest = { periodDropdownOpen = false }) {
                                periods.forEach { p -> DropdownMenuItem(text = { Text(p) }, onClick = { selectedPeriod = p; periodDropdownOpen = false }) }
                            }
                        }
                    }
                }

                // Financial summary grid
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Financial Summary", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(horizontal = 20.dp))
                    Spacer(modifier = Modifier.height(12.dp))
                    Column(modifier = Modifier.padding(horizontal = 20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            StatCard(title = "Total Savings", value = "KES ${fmt(uiState.totalSavings)}", icon = Icons.Filled.Savings, containerColor = ChamaGreenLight, iconColor = ChamaGreen, modifier = Modifier.weight(1f))
                            StatCard(title = "Loans Issued", value = "KES ${fmt(uiState.loansIssued)}", icon = Icons.Filled.AccountBalance, containerColor = Color(0xFFEDE9FE), iconColor = Color(0xFF6D28D9), modifier = Modifier.weight(1f))
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            StatCard(title = "Welfare Fund", value = "KES ${fmt(uiState.welfareBalance)}", icon = Icons.Filled.VolunteerActivism, containerColor = ChamaOrangeLight, iconColor = ChamaOrange, modifier = Modifier.weight(1f))
                            StatCard(title = "Penalties", value = "KES ${fmt(uiState.penaltiesCollected)}", icon = Icons.Filled.Warning, containerColor = ChamaRedLight, iconColor = ChamaRed, modifier = Modifier.weight(1f))
                        }
                    }
                }

                // Group balance card
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = reportTeal), elevation = CardDefaults.cardElevation(4.dp)) {
                        Column(modifier = Modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("Net Chama Worth", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.8f))
                            Text("KES ${fmt(uiState.totalSavings + uiState.welfareBalance + uiState.penaltiesCollected)}", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = Color.White)
                            HorizontalDivider(color = Color.White.copy(alpha = 0.2f))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                BalanceItem("Cash at Hand", "KES ${fmt(uiState.groupBalance)}")
                                BalanceItem("Loans Owed", "KES ${fmt(uiState.loansIssued - uiState.loansRepaid)}")
                                BalanceItem("Welfare", "KES ${fmt(uiState.welfareBalance)}")
                            }
                        }
                    }
                }

                // Charts
                if (uiState.monthlySavingsLabels.isNotEmpty()) {
                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                        BarChartCard(
                            title = "Monthly Contributions",
                            subtitle = "Savings collection trend",
                            icon = Icons.AutoMirrored.Filled.TrendingUp,
                            months = uiState.monthlySavingsLabels,
                            values = uiState.monthlySavingsValues,
                            barColor = ChamaGreen
                        )
                    }
                }

                // Member performance
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Member Performance", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(horizontal = 20.dp))
                    Spacer(modifier = Modifier.height(12.dp))
                    Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Surface), elevation = CardDefaults.cardElevation(2.dp)) {
                        Column(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text("Top Savers", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                            uiState.topContributors.forEachIndexed { index, (name, amount) ->
                                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    Text("${index + 1}", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = Color.Gray, modifier = Modifier.width(20.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                            Text(name, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium)
                                            Text("KES ${fmt(amount)}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold, color = ChamaGreen)
                                        }
                                        Spacer(modifier = Modifier.height(3.dp))
                                        val progress = if (uiState.totalSavings > 0) (amount / uiState.totalSavings).toFloat() else 0f
                                        LinearProgressIndicator(progress = { progress }, modifier = Modifier.fillMaxWidth().height(5.dp).clip(RoundedCornerShape(3.dp)), color = ChamaGreen, trackColor = ChamaOutline)
                                    }
                                }
                            }
                        }
                    }
                }

                // Export
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Surface), elevation = CardDefaults.cardElevation(2.dp)) {
                        Column(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text("Export Financial Statements", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                            Text("Download full audited reports for your records or annual general meetings.", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedButton(onClick = { exportFinancialStatement() }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(10.dp)) { Icon(Icons.Filled.PictureAsPdf, null, modifier = Modifier.size(16.dp)); Spacer(Modifier.width(4.dp)); Text("PDF") }
                                OutlinedButton(onClick = { exportFinancialStatement() }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(10.dp)) { Icon(Icons.Filled.TableChart, null, modifier = Modifier.size(16.dp)); Spacer(Modifier.width(4.dp)); Text("Excel") }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }
}

@Composable
private fun BalanceItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.7f))
        Text(value, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold, color = Color.White)
    }
}

@Composable
private fun BarChartCard(title: String, subtitle: String, icon: ImageVector, months: List<String>, values: List<Double>, barColor: Color) {
    val maxVal = values.maxOrNull()?.takeIf { it > 0 } ?: 1.0
    Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Surface), elevation = CardDefaults.cardElevation(2.dp)) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(icon, null, tint = barColor, modifier = Modifier.size(18.dp))
                Column { Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold); Text(subtitle, style = MaterialTheme.typography.labelSmall, color = TextSecondary) }
            }
            Row(modifier = Modifier.fillMaxWidth().height(100.dp), verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.SpaceAround) {
                months.zip(values).forEach { (month, value) ->
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Bottom, modifier = Modifier.weight(1f)) {
                        Box(modifier = Modifier.width(24.dp).fillMaxHeight((value / maxVal).toFloat().coerceAtLeast(0.05f)).clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp)).background(barColor))
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(month, style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                    }
                }
            }
        }
    }
}

private fun fmt(amount: Double): String = when {
    amount >= 1_000_000 -> "${"%.1f".format(amount / 1_000_000)}M"
    amount >= 1_000 -> "${"%.1f".format(amount / 1_000)}K"
    else -> "%,.0f".format(amount)
}
