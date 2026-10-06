package com.example.fitnesstracker.voice

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fitnesstracker.data.local.ExpenseCategory
import com.example.fitnesstracker.data.local.PaymentMethod

@Composable
fun VoiceConfirmationDialog(
    rawText: String,
    suggestion: VoiceTransaction,
    onDismiss: () -> Unit,
    onConfirmExpense: (amount: Long, category: ExpenseCategory, description: String, source: String) -> Unit,
    onConfirmIncome: (clientName: String, amount: Long, method: PaymentMethod, notes: String) -> Unit
) {
    var isExpense by remember {
        mutableStateOf(suggestion.type == VoiceTransaction.Type.EXPENSE || suggestion.type == VoiceTransaction.Type.UNKNOWN)
    }

    var amountInput by remember { mutableStateOf(suggestion.amount?.toString() ?: "") }
    var descriptionInput by remember { mutableStateOf(suggestion.description ?: suggestion.source ?: "") }
    var clientNameInput by remember { mutableStateOf(suggestion.clientName ?: suggestion.description ?: "") }
    var selectedCategory by remember { mutableStateOf(suggestion.category) }
    var selectedPaymentMethod by remember { mutableStateOf(PaymentMethod.UPI) }

    val isAmountValid = amountInput.toLongOrNull()?.let { it > 0 } == true

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Mic,
                    contentDescription = null,
                    tint = if (isExpense) Color(0xFFFF3D00) else Color(0xFF00C853),
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Confirm Voice Entry 🎤",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                // Recognized Speech Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F5F9))
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(
                            text = "Recognized Speech:",
                            style = MaterialTheme.typography.labelSmall.copy(color = Color.Gray)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "\"$rawText\"",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF0F172A)
                            )
                        )
                    }
                }

                // Transaction Type Selector
                Text(text = "Transaction Type:", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = isExpense,
                        onClick = { isExpense = true },
                        label = { Text("Expense 📉") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFFFF3D00),
                            selectedLabelColor = Color.White
                        )
                    )
                    FilterChip(
                        selected = !isExpense,
                        onClick = { isExpense = false },
                        label = { Text("Income 📈") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFF00C853),
                            selectedLabelColor = Color.White
                        )
                    )
                }

                // Amount Field
                OutlinedTextField(
                    value = amountInput,
                    onValueChange = { amountInput = it },
                    label = { Text("Amount (₹)") },
                    singleLine = true,
                    isError = !isAmountValid && amountInput.isNotBlank(),
                    modifier = Modifier.fillMaxWidth()
                )

                if (suggestion.amount == null && amountInput.isBlank()) {
                    Text(
                        text = "⚠️ Could not understand the amount. Please enter amount above.",
                        style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFFFF3D00))
                    )
                }

                if (isExpense) {
                    // Category Selection for Expense
                    Text(text = "Category:", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf(
                            ExpenseCategory.FOOD,
                            ExpenseCategory.FUEL,
                            ExpenseCategory.EQUIPMENT,
                            ExpenseCategory.SUBSCRIPTION,
                            ExpenseCategory.OTHER
                        ).forEach { cat ->
                            FilterChip(
                                selected = selectedCategory == cat,
                                onClick = { selectedCategory = cat },
                                label = { Text(cat.name, fontSize = 10.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Color(0xFFFF3D00),
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }

                    OutlinedTextField(
                        value = descriptionInput,
                        onValueChange = { descriptionInput = it },
                        label = { Text("Description / Merchant") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    // Client / Notes for Income
                    OutlinedTextField(
                        value = clientNameInput,
                        onValueChange = { clientNameInput = it },
                        label = { Text("Client Name / Source") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text(text = "Payment Method:", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf(PaymentMethod.UPI, PaymentMethod.CASH, PaymentMethod.BANK_TRANSFER).forEach { method ->
                            FilterChip(
                                selected = selectedPaymentMethod == method,
                                onClick = { selectedPaymentMethod = method },
                                label = { Text(method.name, fontSize = 10.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Color(0xFF00C853),
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amount = amountInput.toLongOrNull() ?: 0L
                    if (amount > 0) {
                        if (isExpense) {
                            onConfirmExpense(
                                amount,
                                selectedCategory,
                                descriptionInput.ifBlank { selectedCategory.name },
                                descriptionInput.ifBlank { "Voice Entry" }
                            )
                        } else {
                            onConfirmIncome(
                                clientNameInput.ifBlank { "Voice Income" },
                                amount,
                                selectedPaymentMethod,
                                "Added via Voice Command"
                            )
                        }
                    }
                },
                enabled = isAmountValid,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isExpense) Color(0xFFFF3D00) else Color(0xFF00C853)
                )
            ) {
                Text(if (isExpense) "Add Expense" else "Add Income")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
