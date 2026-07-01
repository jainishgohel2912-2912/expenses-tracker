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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.BillReminderEntity
import com.example.data.SavingsGoalEntity
import com.example.ui.components.FinanceCard
import com.example.ui.components.FinanceProgressBar
import com.example.ui.viewmodel.FinanceViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetsScreen(
    viewModel: FinanceViewModel,
    modifier: Modifier = Modifier
) {
    val currency by viewModel.currency.collectAsState()
    val isDarkMode by viewModel.isDarkMode.collectAsState()

    val activeBudget by viewModel.activeBudget.collectAsState()
    val savingsGoals by viewModel.savingsGoals.collectAsState()
    val billReminders by viewModel.billReminders.collectAsState()

    var showBudgetDialog by remember { mutableStateOf(false) }
    var showGoalDialog by remember { mutableStateOf(false) }
    var showBillDialog by remember { mutableStateOf(false) }

    val bgColors = if (isDarkMode) {
        listOf(Color(0xFF0F172A), Color(0xFF020617))
    } else {
        listOf(Color(0xFFF8FAFC), Color(0xFFE2E8F0))
    }

    val textColor = if (isDarkMode) Color.White else Color(0xFF0F172A)
    val cardBgColor = if (isDarkMode) Color(0xFF1E293B) else Color.White

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(bgColors))
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Title block
        item {
            Text(
                text = "Budgets & Savings Goals",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold,
                color = textColor
            )
            Text(
                "Configure active limits, goals, and track regular bills",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        // Active Monthly Budget Card with thresholds controls
        item {
            FinanceCard(isDark = isDarkMode) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            "Monthly Overall Budget",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = textColor
                        )
                        Text(
                            "Alerting when budget goes lower than threshold limits",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                    }

                    IconButton(
                        onClick = { showBudgetDialog = true },
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(Color(0xFF3B82F6).copy(alpha = 0.1f))
                            .testTag("edit_budget_button")
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit Budget", tint = Color(0xFF3B82F6))
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "$currency${String.format("%,.0f", activeBudget)}",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF3B82F6)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        "/ Month",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.Gray,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Threshold visualization list
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    listOf(5000, 2000, 1000, 500).forEach { limit ->
                        if (limit < activeBudget) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFFF59E0B).copy(alpha = 0.1f))
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    "Alert at $currency$limit",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFF59E0B)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Savings Goals Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Savings Goals",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = textColor
                )
                Button(
                    onClick = { showGoalDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .height(32.dp)
                        .testTag("add_savings_goal_button")
                ) {
                    Icon(Icons.Default.Add, null, modifier = Modifier.size(16.dp), tint = Color.White)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("New Goal", fontSize = 11.sp, color = Color.White)
                }
            }
        }

        // Savings Goals List
        if (savingsGoals.isEmpty()) {
            item {
                FinanceCard(isDark = isDarkMode) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(80.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No active savings goals set up.", color = Color.Gray)
                    }
                }
            }
        } else {
            items(savingsGoals) { goal ->
                var showAddMoneyDialog by remember { mutableStateOf(false) }
                var contributionAmount by remember { mutableStateOf("") }

                val ratio = if (goal.targetAmount > 0) (goal.currentAmount / goal.targetAmount).toFloat() else 0f

                FinanceCard(
                    modifier = Modifier.testTag("savings_card_${goal.name}"),
                    isDark = isDarkMode
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                goal.name,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = textColor,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                "Target Date: ${goal.expectedDate}",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Gray
                            )
                        }

                        IconButton(onClick = { viewModel.deleteSavingsGoal(goal) }) {
                            Icon(Icons.Default.Delete, "Delete goal", tint = Color.Gray.copy(alpha = 0.5f))
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "$currency${String.format("%,.0f", goal.currentAmount)} Saved",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF10B981)
                        )
                        Text(
                            "Goal: $currency${String.format("%,.0f", goal.targetAmount)}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = textColor
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    FinanceProgressBar(progress = ratio, color = Color(0xFF10B981))
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            String.format("%.1f%% Completed", ratio * 100f),
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = if (goal.isCompleted) Color(0xFF10B981) else Color.Gray
                        )

                        if (!goal.isCompleted) {
                            Button(
                                onClick = { showAddMoneyDialog = true },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 2.dp),
                                modifier = Modifier
                                    .height(32.dp)
                                    .testTag("add_money_to_${goal.id}")
                            ) {
                                Text("Add Money", fontSize = 11.sp, color = Color.White)
                            }
                        } else {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.CheckCircle, null, tint = Color(0xFF10B981), modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Completed!", color = Color(0xFF10B981), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                // Inner Dialog for Adding Money to Goal
                if (showAddMoneyDialog) {
                    AlertDialog(
                        onDismissRequest = { showAddMoneyDialog = false },
                        title = { Text("Contribute Savings", fontWeight = FontWeight.Bold) },
                        text = {
                            OutlinedTextField(
                                value = contributionAmount,
                                onValueChange = { contributionAmount = it },
                                label = { Text("Amount to Save ($currency)") },
                                singleLine = true,
                                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("contribution_amount_input")
                            )
                        },
                        confirmButton = {
                            Button(
                                onClick = {
                                    val contribution = contributionAmount.toDoubleOrNull()
                                    if (contribution != null && contribution > 0.0) {
                                        viewModel.updateSavingsProgress(goal, contribution)
                                        showAddMoneyDialog = false
                                        contributionAmount = ""
                                    }
                                },
                                modifier = Modifier.testTag("save_contribution_button")
                            ) {
                                Text("Save Savings", color = Color.White)
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { showAddMoneyDialog = false }) {
                                Text("Cancel", color = Color.Gray)
                            }
                        },
                        containerColor = cardBgColor,
                        shape = RoundedCornerShape(24.dp)
                    )
                }
            }
        }

        // Subscriptions & Recurring Bills Section Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Subscriptions & Bills",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = textColor
                )
                Button(
                    onClick = { showBillDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF59E0B)),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .height(32.dp)
                        .testTag("add_bill_button")
                ) {
                    Icon(Icons.Default.Add, null, modifier = Modifier.size(16.dp), tint = Color.White)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add Bill", fontSize = 11.sp, color = Color.White)
                }
            }
        }

        // Bills List
        if (billReminders.isEmpty()) {
            item {
                FinanceCard(isDark = isDarkMode) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(80.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No active bills scheduled.", color = Color.Gray)
                    }
                }
            }
        } else {
            items(billReminders) { bill ->
                FinanceCard(
                    modifier = Modifier.testTag("bill_row_${bill.id}"),
                    isDark = isDarkMode
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFF59E0B).copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.ReceiptLong, null, tint = Color(0xFFF59E0B), modifier = Modifier.size(18.dp))
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    bill.title,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = textColor
                                )
                                Text(
                                    "Due: ${bill.dueDate}${if (bill.isRecurring) " (${bill.recurringPeriod})" else ""}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.Gray
                                )
                            }
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                "$currency${String.format("%,.2f", bill.amount)}",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (bill.isPaid) Color(0xFF10B981) else Color(0xFFF59E0B)
                            )
                            
                            Spacer(modifier = Modifier.height(4.dp))

                            if (bill.isPaid) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Check, null, tint = Color(0xFF10B981), modifier = Modifier.size(12.dp))
                                    Spacer(modifier = Modifier.width(2.dp))
                                    Text("Paid", fontSize = 10.sp, color = Color(0xFF10B981), fontWeight = FontWeight.Bold)
                                }
                            } else {
                                Button(
                                    onClick = { viewModel.payBill(bill) },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                                    shape = RoundedCornerShape(6.dp),
                                    modifier = Modifier
                                        .height(24.dp)
                                        .testTag("pay_row_${bill.id}")
                                ) {
                                    Text("Mark Paid", fontSize = 10.sp, color = Color.White)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Interactive Dialog to Edit Budget
    if (showBudgetDialog) {
        var budgetInput by remember { mutableStateOf(activeBudget.toString()) }
        AlertDialog(
            onDismissRequest = { showBudgetDialog = false },
            modifier = Modifier.testTag("edit_budget_dialog"),
            title = { Text("Update Monthly Budget", fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = budgetInput,
                    onValueChange = { budgetInput = it },
                    label = { Text("Target Amount ($currency)") },
                    singleLine = true,
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("budget_amount_input")
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val amount = budgetInput.toDoubleOrNull()
                        if (amount != null && amount > 0.0) {
                            viewModel.setBudget(amount)
                            showBudgetDialog = false
                        }
                    },
                    modifier = Modifier.testTag("save_budget_button")
                ) {
                    Text("Save Budget", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showBudgetDialog = false }) {
                    Text("Cancel", color = Color.Gray)
                }
            },
            containerColor = cardBgColor,
            shape = RoundedCornerShape(24.dp)
        )
    }

    // Interactive Dialog to Create Saving Goal
    if (showGoalDialog) {
        var goalName by remember { mutableStateOf("") }
        var goalTarget by remember { mutableStateOf("") }
        var goalCurrent by remember { mutableStateOf("") }
        var goalDate by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showGoalDialog = false },
            modifier = Modifier.testTag("add_goal_dialog"),
            title = { Text("New Savings Goal", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = goalName,
                        onValueChange = { goalName = it },
                        label = { Text("Goal Name (e.g., Laptop)") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("goal_name_input")
                    )
                    OutlinedTextField(
                        value = goalTarget,
                        onValueChange = { goalTarget = it },
                        label = { Text("Target Amount ($currency)") },
                        singleLine = true,
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("goal_target_input")
                    )
                    OutlinedTextField(
                        value = goalCurrent,
                        onValueChange = { goalCurrent = it },
                        label = { Text("Initial Saved ($currency)") },
                        singleLine = true,
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("goal_current_input")
                    )
                    OutlinedTextField(
                        value = goalDate,
                        onValueChange = { goalDate = it },
                        label = { Text("Expected Date (YYYY-MM-DD)") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("goal_date_input")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val targetNum = goalTarget.toDoubleOrNull()
                        val currentNum = goalCurrent.toDoubleOrNull() ?: 0.0
                        if (targetNum != null && goalName.isNotBlank() && goalDate.isNotBlank()) {
                            viewModel.addSavingsGoal(goalName.trim(), targetNum, currentNum, goalDate.trim())
                            showGoalDialog = false
                        }
                    },
                    modifier = Modifier.testTag("save_goal_button")
                ) {
                    Text("Save Goal", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showGoalDialog = false }) {
                    Text("Cancel", color = Color.Gray)
                }
            },
            containerColor = cardBgColor,
            shape = RoundedCornerShape(24.dp)
        )
    }

    // Interactive Dialog to Add Bill / Subscription
    if (showBillDialog) {
        var billTitle by remember { mutableStateOf("") }
        var billAmount by remember { mutableStateOf("") }
        var billDate by remember { mutableStateOf("") }
        var billCategory by remember { mutableStateOf("Bills") }
        var isRecurring by remember { mutableStateOf(false) }
        var recurringPeriod by remember { mutableStateOf("MONTHLY") }

        AlertDialog(
            onDismissRequest = { showBillDialog = false },
            modifier = Modifier.testTag("add_bill_dialog"),
            title = { Text("New Scheduled Bill / Subscription", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = billTitle,
                        onValueChange = { billTitle = it },
                        label = { Text("Bill Title (e.g. Rent, Netflix)") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("bill_title_input")
                    )
                    OutlinedTextField(
                        value = billAmount,
                        onValueChange = { billAmount = it },
                        label = { Text("Amount ($currency)") },
                        singleLine = true,
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("bill_amount_input")
                    )
                    OutlinedTextField(
                        value = billDate,
                        onValueChange = { billDate = it },
                        label = { Text("Due Date (YYYY-MM-DD)") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("bill_date_input")
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Is Recurring Subscription?", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium, color = textColor)
                        Switch(
                            checked = isRecurring,
                            onCheckedChange = { isRecurring = it },
                            modifier = Modifier.testTag("bill_recurring_switch")
                        )
                    }

                    if (isRecurring) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            listOf("WEEKLY", "MONTHLY", "YEARLY").forEach { period ->
                                val active = recurringPeriod == period
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (active) Color(0xFFF59E0B) else Color.Gray.copy(alpha = 0.2f))
                                        .clickable { recurringPeriod = period }
                                        .padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(period, color = if (active) Color.White else textColor, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val amountNum = billAmount.toDoubleOrNull()
                        if (amountNum != null && billTitle.isNotBlank() && billDate.isNotBlank()) {
                            viewModel.addBillReminder(
                                title = billTitle.trim(),
                                amount = amountNum,
                                dueDate = billDate.trim(),
                                category = billCategory,
                                isRecurring = isRecurring,
                                period = if (isRecurring) recurringPeriod else null
                            )
                            showBillDialog = false
                        }
                    },
                    modifier = Modifier.testTag("save_bill_button")
                ) {
                    Text("Save Bill", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showBillDialog = false }) {
                    Text("Cancel", color = Color.Gray)
                }
            },
            containerColor = cardBgColor,
            shape = RoundedCornerShape(24.dp)
        )
    }
}
