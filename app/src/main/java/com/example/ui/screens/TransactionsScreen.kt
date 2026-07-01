package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.TransactionEntity
import com.example.ui.components.FilterChipGroup
import com.example.ui.components.FinanceCard
import com.example.ui.viewmodel.FinanceViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionsScreen(
    viewModel: FinanceViewModel,
    modifier: Modifier = Modifier
) {
    val currency by viewModel.currency.collectAsState()
    val isDarkMode by viewModel.isDarkMode.collectAsState()
    val transactions by viewModel.transactions.collectAsState()
    val groups by viewModel.groups.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedTypeFilter by remember { mutableStateOf("ALL") } // "ALL", "EXPENSE", "INCOME"
    var selectedCategoryFilter by remember { mutableStateOf("ALL") }
    var sortBy by remember { mutableStateOf("DATE_DESC") } // "DATE_DESC", "AMOUNT_DESC", "AMOUNT_ASC"

    var showAddDialog by remember { mutableStateOf(false) }

    val bgColors = if (isDarkMode) {
        listOf(Color(0xFF0F172A), Color(0xFF020617))
    } else {
        listOf(Color(0xFFF8FAFC), Color(0xFFE2E8F0))
    }

    val textColor = if (isDarkMode) Color.White else Color(0xFF0F172A)
    val cardBgColor = if (isDarkMode) Color(0xFF1E293B) else Color.White

    // Predefined default categories
    val categories = listOf(
        "ALL", "Food", "Travel", "Shopping", "College", "Books", 
        "Hostel", "Rent", "Entertainment", "Fuel", "Healthcare", 
        "Gym", "Bills", "Investment", "Others"
    )

    // Filter and Sort transactions
    val filteredTransactions = remember(transactions, searchQuery, selectedTypeFilter, selectedCategoryFilter, sortBy) {
        transactions.filter { tx ->
            val matchesSearch = tx.description.contains(searchQuery, ignoreCase = true) || 
                                tx.category.contains(searchQuery, ignoreCase = true) || 
                                (tx.notes?.contains(searchQuery, ignoreCase = true) ?: false)
            
            val matchesType = selectedTypeFilter == "ALL" || tx.type == selectedTypeFilter
            val matchesCategory = selectedCategoryFilter == "ALL" || tx.category == selectedCategoryFilter

            matchesSearch && matchesType && matchesCategory
        }.sortedWith { a, b ->
            when (sortBy) {
                "DATE_DESC" -> compareValuesBy(b, a) { it.date + it.time }
                "AMOUNT_DESC" -> compareValuesBy(b, a) { it.amount }
                "AMOUNT_ASC" -> compareValuesBy(a, b) { it.amount }
                else -> compareValuesBy(b, a) { it.date + it.time }
            }
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = Color.Transparent,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = Color(0xFF3B82F6),
                contentColor = Color.White,
                shape = CircleShape,
                modifier = Modifier
                    .padding(bottom = 80.dp) // Offset bottom bar height
                    .testTag("add_transaction_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Transaction", modifier = Modifier.size(28.dp))
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(bgColors))
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Transaction Center",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold,
                color = textColor
            )
            Text(
                "Filter, search, or scan receipts with OCR",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search by description, note...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color.Gray) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear search", tint = Color.Gray)
                        }
                    }
                },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("search_field"),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF3B82F6),
                    unfocusedBorderColor = Color.Gray.copy(alpha = 0.5f),
                    focusedContainerColor = cardBgColor,
                    unfocusedContainerColor = cardBgColor
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Filter Row (Income/Expense/All)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("ALL", "EXPENSE", "INCOME").forEach { type ->
                    val isSelected = selectedTypeFilter == type
                    val activeBg = if (type == "EXPENSE") Color(0xFFEF4444) else if (type == "INCOME") Color(0xFF10B981) else Color(0xFF3B82F6)
                    val bg = if (isSelected) activeBg else cardBgColor
                    val fg = if (isSelected) Color.White else textColor

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(bg)
                            .clickable { selectedTypeFilter = type }
                            .padding(vertical = 10.dp)
                            .testTag("filter_type_$type"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(type, fontWeight = FontWeight.Bold, color = fg, fontSize = 12.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Sort & Category Filtering Peeks
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Sorting: ", color = Color.Gray, style = MaterialTheme.typography.bodySmall)
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    SortChip(label = "Latest", active = sortBy == "DATE_DESC", onClick = { sortBy = "DATE_DESC" })
                    SortChip(label = "Highest", active = sortBy == "AMOUNT_DESC", onClick = { sortBy = "AMOUNT_DESC" })
                    SortChip(label = "Lowest", active = sortBy == "AMOUNT_ASC", onClick = { sortBy = "AMOUNT_ASC" })
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Horizontal Categories Scroll
            FilterChipGroup(
                items = listOf("ALL", "Food", "Shopping", "College", "Hostel", "Bills"),
                selectedItem = selectedCategoryFilter,
                onSelected = { selectedCategoryFilter = it },
                isDark = isDarkMode,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            // Transactions List
            if (filteredTransactions.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Inbox, "No logs", tint = Color.Gray, modifier = Modifier.size(56.dp))
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("No transactions match filters.", color = Color.Gray, fontWeight = FontWeight.SemiBold)
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 100.dp)
                ) {
                    items(filteredTransactions) { tx ->
                        TransactionRow(
                            transaction = tx,
                            currency = currency,
                            isDark = isDarkMode,
                            onDelete = { viewModel.deleteTransaction(tx) }
                        )
                    }
                }
            }
        }
    }

    // Interactive Add Transaction Dialog (Full field set + OCR simulation!)
    if (showAddDialog) {
        var txAmount by remember { mutableStateOf("") }
        var txDescription by remember { mutableStateOf("") }
        var txType by remember { mutableStateOf("EXPENSE") } // "EXPENSE" or "INCOME"
        var txCategory by remember { mutableStateOf("Food") }
        var txPaymentMethod by remember { mutableStateOf("Cash") }
        var txLocation by remember { mutableStateOf("") }
        var txNotes by remember { mutableStateOf("") }
        var txSelectedGroupId by remember { mutableStateOf<Int?>(null) }

        var showOcrScanning by remember { mutableStateOf(false) }
        val coroutineScope = rememberCoroutineScope()

        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            modifier = Modifier.testTag("add_transaction_dialog"),
            title = { Text("Log Transaction", fontWeight = FontWeight.Bold, color = textColor) },
            text = {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.heightIn(max = 400.dp)
                ) {
                    // Quick OCR Receipt Scanning Simulation Button
                    item {
                        Button(
                            onClick = {
                                coroutineScope.launch {
                                    showOcrScanning = true
                                    delay(2000) // simulated ocr scanning duration
                                    showOcrScanning = false
                                    // Seed fields with random extracted receipt content
                                    val receipts = listOf(
                                        Pair("Walmart Store", 1450.0 to "Shopping"),
                                        Pair("Starbucks Coffee", 280.0 to "Food"),
                                        Pair("College Library Desk", 850.0 to "Books"),
                                        Pair("Petrol Fueling", 500.0 to "Fuel"),
                                        Pair("Apollo Pharmacy", 320.0 to "Healthcare")
                                    )
                                    val chosen = receipts.random()
                                    txDescription = "Extracted: " + chosen.first
                                    txAmount = chosen.second.first.toString()
                                    txCategory = chosen.second.second
                                    txPaymentMethod = "Card"
                                    txNotes = "Extracted via Smart AI Receipt OCR."
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("ocr_receipt_scan_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8B5CF6)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            if (showOcrScanning) {
                                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(10.dp))
                                Text("Analyzing Receipt OCR...", color = Color.White, fontSize = 12.sp)
                            } else {
                                Icon(Icons.Default.QrCodeScanner, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(10.dp))
                                Text("OCR Receipt Scanner (Simulation)", color = Color.White, fontSize = 12.sp)
                            }
                        }
                    }

                    // Income / Expense selector
                    item {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = { txType = "EXPENSE"; txCategory = "Food" },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = if (txType == "EXPENSE") Color(0xFFEF4444) else Color.Gray.copy(alpha = 0.2f)),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("Expense", color = if (txType == "EXPENSE") Color.White else textColor)
                            }
                            Button(
                                onClick = { txType = "INCOME"; txCategory = "Salary" },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = if (txType == "INCOME") Color(0xFF10B981) else Color.Gray.copy(alpha = 0.2f)),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("Income", color = if (txType == "INCOME") Color.White else textColor)
                            }
                        }
                    }

                    // Amount & Description fields
                    item {
                        OutlinedTextField(
                            value = txAmount,
                            onValueChange = { txAmount = it },
                            label = { Text("Amount ($currency)") },
                            singleLine = true,
                            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("tx_amount_input")
                        )
                    }

                    item {
                        OutlinedTextField(
                            value = txDescription,
                            onValueChange = { txDescription = it },
                            label = { Text("Description (e.g. Lunch)") },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("tx_description_input")
                        )
                    }

                    // Category Selector
                    item {
                        val availableCategories = if (txType == "EXPENSE") {
                            listOf("Food", "Travel", "Shopping", "College", "Books", "Hostel", "Rent", "Entertainment", "Fuel", "Healthcare", "Gym", "Bills", "Investment", "Others")
                        } else {
                            listOf("Salary", "Pocket Money", "Scholarship", "Freelancing", "Business Income", "Other Income")
                        }

                        Text("Category", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(modifier = Modifier.fillMaxWidth()) {
                                var expandedCat by remember { mutableStateOf(false) }
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isDarkMode) Color(0xFF334155) else Color(0xFFF1F5F9))
                                        .clickable { expandedCat = true }
                                        .padding(14.dp)
                                        .testTag("tx_category_selector")
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(txCategory, color = textColor, fontWeight = FontWeight.Bold)
                                        Icon(Icons.Default.ArrowDropDown, null, tint = Color.Gray)
                                    }
                                }
                                DropdownMenu(
                                    expanded = expandedCat,
                                    onDismissRequest = { expandedCat = false },
                                    modifier = Modifier.background(cardBgColor)
                                ) {
                                    availableCategories.forEach { cat ->
                                        DropdownMenuItem(
                                            text = { Text(cat, color = textColor) },
                                            onClick = {
                                                txCategory = cat
                                                expandedCat = false
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Payment Method selector
                    item {
                        val paymentMethods = listOf("Cash")
                        Text("Payment Method", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            paymentMethods.forEach { method ->
                                val isSel = txPaymentMethod == method
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isSel) Color(0xFF3B82F6) else (if (isDarkMode) Color(0xFF334155) else Color(0xFFF1F5F9)))
                                        .clickable { txPaymentMethod = method }
                                        .padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(method, color = if (isSel) Color.White else textColor, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    // Collaborative Group Selection
                    if (groups.isNotEmpty()) {
                        item {
                            Text("Split Expense in Group (Optional)", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                            var expandedGrp by remember { mutableStateOf(false) }
                            Box(modifier = Modifier.fillMaxWidth()) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isDarkMode) Color(0xFF334155) else Color(0xFFF1F5F9))
                                        .clickable { expandedGrp = true }
                                        .padding(14.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        val currentGroupText = groups.firstOrNull { it.id == txSelectedGroupId }?.name ?: "None (Personal Expense)"
                                        Text(currentGroupText, color = textColor)
                                        Icon(Icons.Default.Group, null, tint = Color.Gray)
                                    }
                                }
                                DropdownMenu(
                                    expanded = expandedGrp,
                                    onDismissRequest = { expandedGrp = false },
                                    modifier = Modifier.background(cardBgColor)
                                ) {
                                    DropdownMenuItem(
                                        text = { Text("None (Personal Expense)", color = textColor) },
                                        onClick = {
                                            txSelectedGroupId = null
                                            expandedGrp = false
                                        }
                                    )
                                    groups.forEach { grp ->
                                        DropdownMenuItem(
                                            text = { Text(grp.name, color = textColor) },
                                            onClick = {
                                                txSelectedGroupId = grp.id
                                                expandedGrp = false
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Location & Notes optional fields
                    item {
                        OutlinedTextField(
                            value = txLocation,
                            onValueChange = { txLocation = it },
                            label = { Text("Location (Optional)") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    item {
                        OutlinedTextField(
                            value = txNotes,
                            onValueChange = { txNotes = it },
                            label = { Text("Notes (Optional)") },
                            singleLine = false,
                            maxLines = 3,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val amountNum = txAmount.toDoubleOrNull()
                        if (amountNum != null && txDescription.isNotBlank()) {
                            viewModel.addTransaction(
                                amount = amountNum,
                                category = txCategory,
                                description = txDescription.trim(),
                                type = txType,
                                paymentMethod = txPaymentMethod,
                                location = txLocation.trim().ifEmpty { null },
                                notes = txNotes.trim().ifEmpty { null },
                                groupId = txSelectedGroupId
                            )
                            
                            // If Split Group is selected, auto-log split expense
                            if (txType == "EXPENSE" && txSelectedGroupId != null) {
                                val activeGrp = groups.firstOrNull { it.id == txSelectedGroupId }
                                if (activeGrp != null) {
                                    viewModel.addGroupExpense(
                                        groupId = txSelectedGroupId!!,
                                        amount = amountNum,
                                        description = txDescription.trim(),
                                        paidBy = "You",
                                        category = txCategory,
                                        groupName = activeGrp.name
                                    )
                                }
                            }

                            showAddDialog = false
                        }
                    },
                    modifier = Modifier.testTag("save_transaction_button")
                ) {
                    Text("Save Log", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showAddDialog = false },
                    modifier = Modifier.testTag("dismiss_add_transaction_button")
                ) {
                    Text("Cancel", color = Color.Gray)
                }
            },
            containerColor = cardBgColor,
            shape = RoundedCornerShape(24.dp)
        )
    }
}

@Composable
fun SortChip(
    label: String,
    active: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (active) Color(0xFF3B82F6).copy(alpha = 0.15f) else Color.Transparent)
            .clickable { onClick() }
            .padding(horizontal = 8.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            label,
            color = if (active) Color(0xFF3B82F6) else Color.Gray,
            fontWeight = FontWeight.Bold,
            fontSize = 10.sp
        )
    }
}
