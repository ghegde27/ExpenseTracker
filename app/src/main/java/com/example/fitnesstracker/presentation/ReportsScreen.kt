package com.example.fitnesstracker.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.fitnesstracker.data.local.ExpenseCategory

data class CategoryBreakdown(
    val category: String,
    val amount: Double,
    val percentage: Float,
    val color: Color
)

private val categoryColors = mapOf(
    ExpenseCategory.EQUIPMENT to Color(0xFF6200EA),
    ExpenseCategory.FUEL to Color(0xFFFF6D00),
    ExpenseCategory.FOOD to Color(0xFF00C853),
    ExpenseCategory.SUBSCRIPTION to Color(0xFF29B6F6),
    ExpenseCategory.OTHER to Color(0xFFAB47BC)
)

@Composable
fun ReportsScreen(
    incomeViewModel: IncomeViewModel = hiltViewModel(),
    expenseViewModel: ExpenseViewModel = hiltViewModel()
) {
    var selectedPeriod by remember { mutableStateOf("All Time") }

    val totalIncomeDb by incomeViewModel.totalIncome.collectAsState()
    val totalExpensesDb by expenseViewModel.totalExpenses.collectAsState()
    val dbExpenses by expenseViewModel.expenseList.collectAsState()

    val totalIncome = totalIncomeDb.toDouble()
    val totalExpenses = totalExpensesDb.toDouble()
    val netProfit = totalIncome - totalExpenses
    val profitMargin = if (totalIncome > 0) ((netProfit / totalIncome) * 100).toInt() else 0

    // Compute Expense Category Breakdown from DB
    val categoryBreakdownList = remember(dbExpenses, totalExpenses) {
        if (dbExpenses.isEmpty() || totalExpenses == 0.0) {
            emptyList()
        } else {
            dbExpenses
                .groupBy { it.category }
                .map { (category, list) ->
                    val catTotal = list.sumOf { it.amount.toDouble() }
                    val ratio = (catTotal / totalExpenses).toFloat()
                    val color = categoryColors[category] ?: Color(0xFF757575)
                    CategoryBreakdown(category.name, catTotal, ratio, color)
                }
                .sortedByDescending { it.amount }
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8F9FA))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Hero Financial Overview
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(Color(0xFF4A148C), Color(0xFF7B1FA2))
                            )
                        )
                        .padding(20.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Analytics & Reports 📊",
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                                Text(
                                    text = "Financial performance overview",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = Color.White.copy(alpha = 0.8f)
                                    )
                                )
                            }

                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color.White.copy(alpha = 0.2f)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Assessment,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "$profitMargin% Margin",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        // Metric Triad from DB
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            MetricSummaryBox(title = "Total Revenue", value = "₹${totalIncome.toInt()}", color = Color(0xFF00E676))
                            MetricSummaryBox(title = "Total Expenses", value = "₹${totalExpenses.toInt()}", color = Color(0xFFFF5252))
                            MetricSummaryBox(title = "Net Savings", value = "₹${netProfit.toInt()}", color = Color(0xFFE040FB))
                        }
                    }
                }
            }
        }

        // Period Filters
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("All Time", "This Month", "This Week").forEach { period ->
                    FilterChip(
                        selected = selectedPeriod == period,
                        onClick = { selectedPeriod = period },
                        label = { Text(period) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFF7B1FA2),
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }
        }

        // Category Breakdown Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.PieChart,
                                contentDescription = null,
                                tint = Color(0xFF7B1FA2)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Expense Breakdown by Category",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    if (categoryBreakdownList.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "No expenses logged yet",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, color = Color.Gray)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Log business expenses to view category breakdown",
                                    style = MaterialTheme.typography.bodySmall.copy(color = Color.LightGray)
                                )
                            }
                        }
                    } else {
                        categoryBreakdownList.forEach { category ->
                            Column(modifier = Modifier.padding(vertical = 6.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = category.category,
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)
                                    )
                                    Text(
                                        text = "₹${category.amount.toInt()} (${(category.percentage * 100).toInt()}%)",
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = category.color
                                        )
                                    )
                                }

                                Spacer(modifier = Modifier.height(4.dp))

                                LinearProgressIndicator(
                                    progress = { category.percentage },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(8.dp)
                                        .clip(RoundedCornerShape(4.dp)),
                                    color = category.color,
                                    trackColor = category.color.copy(alpha = 0.15f)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Export Actions
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = { },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7B1FA2))
                ) {
                    Icon(imageVector = Icons.Default.FileDownload, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "Export PDF", fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = { },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(imageVector = Icons.Default.Share, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "Share Report", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun MetricSummaryBox(title: String, value: String, color: Color) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.labelSmall.copy(color = Color.White.copy(alpha = 0.75f))
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium.copy(
                color = color,
                fontWeight = FontWeight.ExtraBold
            )
        )
    }
}