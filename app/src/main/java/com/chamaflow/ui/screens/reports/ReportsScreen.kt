package com.chamaflow.ui.screens.reports

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.chamaflow.ui.components.StatCard
import com.chamaflow.ui.theme.*

private val reportTeal = Color(0xFF0F766E)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen(onBack: () -> Unit = {}) {
    var selectedPeriod by remember { mutableStateOf("This Year") }
    val periods = listOf("This Month", "This Quarter", "This Year", "All Time")
    var periodDropdownOpen by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Reports", fontWeight = FontWeight.Bold, color = Color.White) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, null, tint = Color.White) } },
                actions = { IconButton(onClick = {}) { Icon(Icons.Outlined.FileDownload, "Export", tint = Color.White) } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = reportTeal)
            )
        },
        containerColor = ChamaBackground
    ) { padding ->
        LazyColumn(modifier = Modifier.fillMaxSize().padding(padding)) {

            // Period selector
            item {
                Box(modifier = Modifier.fillMaxWidth().background(reportTeal).padding(horizontal = 24.dp, vertical = 12.dp)) {
                    ExposedDropdownMenuBox(expanded = periodDropdownOpen, onExpandedChange = { periodDropdownOpen = it }) {
                        Surface(shape = RoundedCornerShape(20.dp), color = Color.White.copy(alpha = 0.2f), modifier = Modifier.menuAnchor()) {
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
                val rows = listOf(
                    listOf(Triple(Icons.Filled.Savings, "Total Savings", "KES 480K") to Pair(ChamaGreenLight, ChamaGreen), Triple(Icons.Filled.AccountBalance, "Loans Issued", "KES 150K") to Pair(Color(0xFFEDE9FE), Color(0xFF6D28D9))),
                    listOf(Triple(Icons.Filled.TrendingDown, "Loan Repaid", "KES 90K") to Pair(ChamaBlueLight, ChamaBlue), Triple(Icons.Filled.Warning, "Penalties", "KES 8.5K") to Pair(ChamaRedLight, ChamaRed))
                )
                Column(modifier = Modifier.padding(horizontal = 20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    rows.forEach { row ->
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            row.forEach { (triple, colors) ->
                                StatCard(title = triple.second, value = triple.third, icon = triple.first, containerColor = colors.first, iconColor = colors.second, modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }

            // Group balance card
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = reportTeal), elevation = CardDefaults.cardElevation(4.dp)) {
                    Column(modifier = Modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Current Group Balance", style = MaterialTheme.typography.labelMedium, color = Color.White.copy(alpha = 0.75f))
                        Text("KES 238,500", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = Color.White)
                        HorizontalDivider(color = Color.White.copy(alpha = 0.2f))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            BalanceItem("Contributions", "KES 480K")
                            BalanceItem("Less: Loans", "- KES 150K")
                            BalanceItem("Less: Expenses", "- KES 91.5K")
                        }
                    }
                }
            }

            // Bar charts
            item {
                Spacer(modifier = Modifier.height(16.dp))
                BarChartCard("Monthly Savings", "Contributions collected per month", Icons.Outlined.TrendingUp, listOf("Jan","Feb","Mar","Apr","May","Jun"), listOf(40_000.0,45_000.0,50_000.0,48_000.0,55_000.0,60_000.0), ChamaGreen)
            }
            item {
                Spacer(modifier = Modifier.height(12.dp))
                BarChartCard("Loan Repayments", "Amount repaid per month", Icons.Outlined.AccountBalance, listOf("Jan","Feb","Mar","Apr","May","Jun"), listOf(0.0,10_000.0,15_000.0,20_000.0,22_000.0,23_000.0), Color(0xFF6D28D9))
            }

            // Member performance
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Text("Member Performance", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(horizontal = 20.dp))
                Spacer(modifier = Modifier.height(12.dp))
                Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = ChamaSurface), elevation = CardDefaults.cardElevation(2.dp)) {
                    Column(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("Top Contributors", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        listOf("Amina Wanjiku" to 100, "Brian Otieno" to 100, "Catherine Mwangi" to 75, "David Kipchoge" to 50).forEachIndexed { index, (name, pct) ->
                            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                Text("${index + 1}", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = ChamaTextMuted, modifier = Modifier.width(20.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text(name, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium)
                                        Text("KES ${pct / 100 * 60}K", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold, color = ChamaGreen)
                                    }
                                    Spacer(modifier = Modifier.height(3.dp))
                                    LinearProgressIndicator(progress = { pct / 100f }, modifier = Modifier.fillMaxWidth().height(5.dp).clip(RoundedCornerShape(3.dp)), color = ChamaGreen, trackColor = ChamaOutline)
                                }
                            }
                        }
                    }
                }
            }

            // Export
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = ChamaSurface), elevation = CardDefaults.cardElevation(2.dp)) {
                    Column(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("Export Reports", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        Text("Download full financial reports for your records or meetings.", style = MaterialTheme.typography.bodySmall, color = ChamaTextSecondary)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(onClick = {}, modifier = Modifier.weight(1f), shape = RoundedCornerShape(10.dp)) { Icon(Icons.Filled.PictureAsPdf, null, modifier = Modifier.size(16.dp)); Spacer(Modifier.width(4.dp)); Text("PDF") }
                            OutlinedButton(onClick = {}, modifier = Modifier.weight(1f), shape = RoundedCornerShape(10.dp)) { Icon(Icons.Filled.TableChart, null, modifier = Modifier.size(16.dp)); Spacer(Modifier.width(4.dp)); Text("Excel") }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(32.dp))
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
    val maxVal = values.maxOrNull() ?: 1.0
    Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = ChamaSurface), elevation = CardDefaults.cardElevation(2.dp)) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(icon, null, tint = barColor, modifier = Modifier.size(18.dp))
                Column { Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold); Text(subtitle, style = MaterialTheme.typography.labelSmall, color = ChamaTextSecondary) }
            }
            Row(modifier = Modifier.fillMaxWidth().height(100.dp), verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.SpaceAround) {
                months.zip(values).forEach { (month, value) ->
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Bottom, modifier = Modifier.weight(1f)) {
                        Box(modifier = Modifier.width(24.dp).fillMaxHeight((value / maxVal).toFloat().coerceAtLeast(0.05f)).clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp)).background(barColor))
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(month, style = MaterialTheme.typography.labelSmall, color = ChamaTextSecondary)
                    }
                }
            }
        }
    }
}
