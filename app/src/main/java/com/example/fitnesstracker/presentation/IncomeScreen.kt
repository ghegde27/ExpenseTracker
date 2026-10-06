package com.example.fitnesstracker.presentation

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.fitnesstracker.data.local.ClientEntity
import com.example.fitnesstracker.data.local.IncomeEntity
import com.example.fitnesstracker.data.local.PaymentMethod
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun IncomeScreen(
    incomeViewModel: IncomeViewModel = hiltViewModel()
) {
    var showDialog by remember { mutableStateOf(false) }
    var selectedMethodFilter by remember { mutableStateOf("All") }

    val dbIncomeList by incomeViewModel.incomeList.collectAsState()
    val totalIncomeDb by incomeViewModel.totalIncome.collectAsState()
    val clientSearchResults by incomeViewModel.clientSearchResults.collectAsState()

    val filteredList = remember(dbIncomeList, selectedMethodFilter) {
        if (selectedMethodFilter == "All") dbIncomeList
        else dbIncomeList.filter { it.paymentMethod.name.equals(selectedMethodFilter, ignoreCase = true) }
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showDialog = true },
                containerColor = Color(0xFF00C853),
                contentColor = Color.White,
                shape = RoundedCornerShape(18.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "Add Income", fontWeight = FontWeight.Bold)
                }
            }
        }
    ) { paddingValues ->

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF8F9FA))
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Hero Total Income Banner
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
                                    colors = listOf(Color(0xFF00B0FF), Color(0xFF00E676))
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
                                        text = "Total Recorded Income 💰",
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            color = Color.White.copy(alpha = 0.9f),
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                    Text(
                                        text = "₹$totalIncomeDb",
                                        style = MaterialTheme.typography.headlineMedium.copy(
                                            color = Color.White,
                                            fontWeight = FontWeight.ExtraBold
                                        )
                                    )
                                }

                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(CircleShape)
                                        .background(Color.White.copy(alpha = 0.25f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.TrendingUp,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(28.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Payment Method Stats
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                PaymentStatPill(
                                    label = "UPI",
                                    amount = dbIncomeList.filter { it.paymentMethod == PaymentMethod.UPI }.sumOf { it.amount }
                                )
                                PaymentStatPill(
                                    label = "Cash",
                                    amount = dbIncomeList.filter { it.paymentMethod == PaymentMethod.CASH }.sumOf { it.amount }
                                )
                                PaymentStatPill(
                                    label = "Bank",
                                    amount = dbIncomeList.filter { it.paymentMethod == PaymentMethod.BANK_TRANSFER }.sumOf { it.amount }
                                )
                            }
                        }
                    }
                }
            }

            // Payment Method Filter Chips
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("All", "UPI", "CASH", "BANK_TRANSFER").forEach { filter ->
                        FilterChip(
                            selected = selectedMethodFilter == filter,
                            onClick = { selectedMethodFilter = filter },
                            label = { Text(if (filter == "BANK_TRANSFER") "Bank Transfer" else filter) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFF00C853),
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }
            }

            // Income List or Placeholder
            if (filteredList.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.AttachMoney,
                                contentDescription = null,
                                tint = Color.LightGray,
                                modifier = Modifier.size(56.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "No income records found",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = Color.Gray)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Tap '+ Add Income' to log your payments",
                                style = MaterialTheme.typography.bodySmall.copy(color = Color.LightGray)
                            )
                        }
                    }
                }
            } else {
                items(filteredList, key = { it.id }) { item ->
                    IncomeCardItem(
                        item = item,
                        onDelete = { incomeViewModel.delete(item.id) }
                    )
                }
            }
        }
    }

    if (showDialog) {
        AddIncomeDialog(
            clientSearchResults = clientSearchResults,
            onSearchClient = { incomeViewModel.searchClients(it) },
            onDismiss = { showDialog = false },
            onSave = { clientId, clientName, amount, method, notes ->
                incomeViewModel.add(clientId, clientName, amount.toLongOrNull() ?: 0L, method, notes)
                showDialog = false
            }
        )
    }
}

@Composable
private fun PaymentStatPill(label: String, amount: Long) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color.White.copy(alpha = 0.2f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "$label: ",
                style = MaterialTheme.typography.labelSmall.copy(color = Color.White.copy(alpha = 0.8f))
            )
            Text(
                text = "₹$amount",
                style = MaterialTheme.typography.labelSmall.copy(
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            )
        }
    }
}

@Composable
private fun IncomeCardItem(
    item: IncomeEntity,
    onDelete: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    val formattedDate = remember(item.sessionDate) {
        SimpleDateFormat("dd MMM yyyy • hh:mm a", Locale.getDefault()).format(Date(item.sessionDate))
    }

    val (methodColor, methodIcon) = when (item.paymentMethod) {
        PaymentMethod.UPI -> Color(0xFF7B1FA2) to Icons.Default.QrCode
        PaymentMethod.CASH -> Color(0xFF2E7D32) to Icons.Default.AttachMoney
        PaymentMethod.BANK_TRANSFER -> Color(0xFF1565C0) to Icons.Default.AccountBalance
        else -> Color(0xFFE65100) to Icons.Default.CreditCard
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize()
            .clickable { expanded = !expanded },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(methodColor.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = methodIcon,
                        contentDescription = null,
                        tint = methodColor,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.clientName,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF212121)
                        )
                    )
                    Text(
                        text = formattedDate,
                        style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray)
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "+₹${item.amount}",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF00C853)
                        )
                    )

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = methodColor.copy(alpha = 0.12f)
                    ) {
                        Text(
                            text = item.paymentMethod.name,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = methodColor,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }
            }

            if (expanded) {
                Spacer(modifier = Modifier.height(10.dp))
                if (item.notes.isNotBlank()) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFFF5F5F5),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Notes: ${item.notes}",
                            modifier = Modifier.padding(10.dp),
                            style = MaterialTheme.typography.bodySmall.copy(color = Color.DarkGray)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    IconButton(onClick = onDelete) {
                        Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFFF3D00))
                    }
                }
            }
        }
    }
}

@Composable
private fun AddIncomeDialog(
    clientSearchResults: List<ClientEntity>,
    onSearchClient: (String) -> Unit,
    onDismiss: () -> Unit,
    onSave: (clientId: Long?, clientName: String, amount: String, method: PaymentMethod, notes: String) -> Unit
) {
    var clientNameInput by remember { mutableStateOf("") }
    var selectedClient by remember { mutableStateOf<ClientEntity?>(null) }
    var amountInput by remember { mutableStateOf("") }
    var selectedMethod by remember { mutableStateOf(PaymentMethod.UPI) }
    var notesInput by remember { mutableStateOf("") }

    val presetAmounts = listOf("500", "1000", "2000", "5000")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Record Income 💵",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = clientNameInput,
                    onValueChange = { value ->
                        clientNameInput = value
                        selectedClient = null
                        onSearchClient(value)
                    },
                    label = { Text("Client Name") },
                    placeholder = { Text("Type client name...") },
                    leadingIcon = { Icon(imageVector = Icons.Default.Person, contentDescription = null) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                if (clientNameInput.isNotBlank() && selectedClient == null && clientSearchResults.isNotEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF0F4F8))
                    ) {
                        Column {
                            clientSearchResults.take(3).forEach { client ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            selectedClient = client
                                            clientNameInput = client.name
                                            onSearchClient("")
                                        }
                                        .padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = Color(0xFF00C853))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(text = client.name, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                OutlinedTextField(
                    value = amountInput,
                    onValueChange = { amountInput = it },
                    label = { Text("Amount (₹)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Quick:", style = MaterialTheme.typography.labelSmall)
                    presetAmounts.forEach { preset ->
                        FilterChip(
                            selected = amountInput == preset,
                            onClick = { amountInput = preset },
                            label = { Text("₹$preset", fontSize = 11.sp) }
                        )
                    }
                }

                Text(text = "Payment Method:", style = MaterialTheme.typography.labelMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf(PaymentMethod.UPI, PaymentMethod.CASH, PaymentMethod.BANK_TRANSFER).forEach { method ->
                        FilterChip(
                            selected = selectedMethod == method,
                            onClick = { selectedMethod = method },
                            label = { Text(method.name, fontSize = 11.sp) }
                        )
                    }
                }

                OutlinedTextField(
                    value = notesInput,
                    onValueChange = { notesInput = it },
                    label = { Text("Notes / Session Details") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val finalName = selectedClient?.name ?: clientNameInput.ifBlank { "Walk-in Client" }
                    val clientId = selectedClient?.id
                    onSave(clientId, finalName, amountInput.trim(), selectedMethod, notesInput.trim())
                },
                enabled = amountInput.toDoubleOrNull() != null && amountInput.toDouble() > 0,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00C853))
            ) {
                Text("Save Income")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}