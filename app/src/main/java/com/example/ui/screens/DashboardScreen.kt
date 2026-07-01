package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.ui.res.painterResource
import com.example.R
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
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.BillReminderEntity
import com.example.data.NotificationEntity
import com.example.data.TransactionEntity
import com.example.ui.components.*
import com.example.ui.viewmodel.FinanceViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: FinanceViewModel,
    modifier: Modifier = Modifier
) {
    val currency by viewModel.currency.collectAsState()
    val isDarkMode by viewModel.isDarkMode.collectAsState()
    val userName by viewModel.userName.collectAsState()

    val activeBudget by viewModel.activeBudget.collectAsState()
    val spentThisMonth by viewModel.spentThisMonth.collectAsState()
    val incomeThisMonth by viewModel.incomeThisMonth.collectAsState()
    val remainingBudget by viewModel.remainingBudget.collectAsState()
    val totalSavings by viewModel.totalSavings.collectAsState()
    val score by viewModel.financialHealthScore.collectAsState()
    val streak by viewModel.budgetStreak.collectAsState()

    val transactions by viewModel.transactions.collectAsState()
    val billReminders by viewModel.billReminders.collectAsState()
    val notifications by viewModel.notifications.collectAsState()

    var showNotificationsSheet by remember { mutableStateOf(false) }

    val bgColors = if (isDarkMode) {
        listOf(Color(0xFF0F172A), Color(0xFF020617))
    } else {
        listOf(Color(0xFFF8FAFC), Color(0xFFE2E8F0))
    }
    
    val textColor = if (isDarkMode) Color.White else Color(0xFF0F172A)
    val subTextColor = if (isDarkMode) Color(0xFF94A3B8) else Color(0xFF475569)

    // Calculate details for charts
    val categorySummary = remember(transactions) {
        val currentMonth = SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(Date())
        transactions.filter { it.type == "EXPENSE" && it.date.startsWith(currentMonth) }
            .groupBy { it.category }
            .map { Pair(it.key, it.value.sumOf { item -> item.amount }) }
            .sortedByDescending { it.second }
    }

    val recentTx = remember(transactions) {
        transactions.take(5)
    }

    val unreadNotificationsCount = remember(notifications) {
        notifications.count { !it.isRead }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(bgColors))
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 100.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header Bar
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.img_logo),
                            contentDescription = "Expense Tracker Logo",
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(10.dp))
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Hello, $userName",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 22.sp
                                ),
                                color = textColor,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "Manage your finances, build habits",
                                style = MaterialTheme.typography.bodyMedium,
                                color = subTextColor,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    Box {
                        IconButton(
                            onClick = { showNotificationsSheet = true },
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(if (isDarkMode) Color(0xFF1E293B) else Color.White)
                                .testTag("notifications_icon_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = "Notifications",
                                tint = if (unreadNotificationsCount > 0) Color(0xFFEF4444) else textColor
                            )
                        }

                        if (unreadNotificationsCount > 0) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFEF4444))
                                    .align(Alignment.TopEnd)
                            )
                        }
                    }
                }
            }

            // High Fidelity Summary Cards Grid
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Budget Card
                    FinanceCard(
                        modifier = Modifier.weight(1f),
                        isDark = isDarkMode
                    ) {
                        Text("Active Budget", style = MaterialTheme.typography.labelMedium, color = Color.Gray)
                        Text(
                            "$currency${String.format("%,.0f", activeBudget)}",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = textColor,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    // Remaining Card
                    val remainingColor = if (remainingBudget < 0) Color(0xFFEF4444) else if (remainingBudget < 1500) Color(0xFFF59E0B) else Color(0xFF10B981)
                    FinanceCard(
                        modifier = Modifier.weight(1f),
                        isDark = isDarkMode
                    ) {
                        Text("Remaining", style = MaterialTheme.typography.labelMedium, color = Color.Gray)
                        Text(
                            "$currency${String.format("%,.0f", remainingBudget)}",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = remainingColor,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Spent Card
                    FinanceCard(
                        modifier = Modifier.weight(1f),
                        isDark = isDarkMode
                    ) {
                        Text("Spent Month", style = MaterialTheme.typography.labelMedium, color = Color.Gray)
                        Text(
                            "$currency${String.format("%,.0f", spentThisMonth)}",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (spentThisMonth > activeBudget) Color(0xFFEF4444) else textColor,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    // Savings Card
                    FinanceCard(
                        modifier = Modifier.weight(1f),
                        isDark = isDarkMode
                    ) {
                        Text("Total Savings", style = MaterialTheme.typography.labelMedium, color = Color.Gray)
                        Text(
                            "$currency${String.format("%,.0f", totalSavings)}",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF10B981),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            // Budget Progress Bar
            item {
                val ratio = if (activeBudget > 0) (spentThisMonth / activeBudget).toFloat() else 0f
                val progressColor = if (ratio > 1.0f) Color(0xFFEF4444) else if (ratio > 0.8f) Color(0xFFF59E0B) else Color(0xFF3B82F6)
                
                FinanceCard(isDark = isDarkMode) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Monthly Budget Used",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = textColor
                        )
                        Text(
                            String.format("%.1f%%", ratio * 100f),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = progressColor
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    FinanceProgressBar(progress = ratio, color = progressColor)
                    
                    if (ratio > 1.0f) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = "Alert",
                                tint = Color(0xFFEF4444),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                "Crossed budget limit by $currency${String.format("%,.0f", spentThisMonth - activeBudget)}!",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFFEF4444),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Gamification: Streak & Badges
            item {
                FinanceCard(isDark = isDarkMode) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFFEF3C7)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("🔥", fontSize = 24.sp)
                        }
                        
                        Spacer(modifier = Modifier.width(16.dp))
                        
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "$streak-Day Saving Streak",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = textColor
                            )
                            Text(
                                "Keep daily spending below ${currency}500. Keep it up!",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Gray
                            )
                        }
                        
                        // Badge indicators
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            BadgeItem(emoji = "🌱", label = "Saver", unlocked = streak >= 3)
                            BadgeItem(emoji = "🏆", label = "Master", unlocked = streak >= 7)
                            BadgeItem(emoji = "🛡️", label = "Ninja", unlocked = streak >= 12)
                        }
                    }
                }
            }

            // Financial Health Score Circular Widget
            item {
                FinanceCard(isDark = isDarkMode) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier.size(80.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            // Circular health bar
                            val animatedScore by animateFloatAsState(
                                targetValue = score.toFloat() / 100f,
                                animationSpec = tween(1000),
                                label = "healthScore"
                            )
                            val ringColor = when {
                                score >= 80 -> Color(0xFF10B981)
                                score >= 60 -> Color(0xFFF59E0B)
                                else -> Color(0xFFEF4444)
                            }

                            Canvas(modifier = Modifier.fillMaxSize()) {
                                drawCircle(
                                    color = Color.Gray.copy(alpha = 0.2f),
                                    style = Stroke(width = 8.dp.toPx())
                                )
                                drawArc(
                                    color = ringColor,
                                    startAngle = -90f,
                                    sweepAngle = animatedScore * 360f,
                                    useCenter = false,
                                    style = Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round)
                                )
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    "$score",
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = textColor
                                )
                                Text(
                                    "Score",
                                    fontSize = 10.sp,
                                    color = Color.Gray
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(20.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "Financial Health",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = textColor
                            )
                            val recommendation = when {
                                score >= 80 -> "Excellent habits! Continue monitoring goals and invest surplus savings."
                                score >= 60 -> "Good stability. Avoid non-essential shopping to raise score further."
                                else -> "Warning: High budget spending or unpaid reminders. Delay deliveries!"
                            }
                            Text(
                                recommendation,
                                style = MaterialTheme.typography.bodySmall,
                                color = subTextColor
                            )
                        }
                    }
                }
            }

            // Category Wise Spending Chart Section
            item {
                FinanceCard(isDark = isDarkMode) {
                    Text(
                        "Category Breakdown",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = textColor
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    if (categorySummary.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(140.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "No expenses logged this month yet.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.Gray
                            )
                        }
                    } else {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            val chartColors = listOf(
                                Color(0xFF3B82F6), Color(0xFF10B981), Color(0xFFF59E0B),
                                Color(0xFFEC4899), Color(0xFF8B5CF6), Color(0xFFEF4444),
                                Color(0xFF06B6D4), Color(0xFF14B8A6), Color(0xFF84CC16)
                            )

                            SimplePieChart(
                                data = categorySummary.take(5),
                                colors = chartColors,
                                modifier = Modifier.size(140.dp)
                            )

                            Column(
                                verticalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier.padding(start = 12.dp)
                            ) {
                                categorySummary.take(4).forEachIndexed { index, pair ->
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(10.dp)
                                                .clip(CircleShape)
                                                .background(chartColors[index % chartColors.size])
                                        )
                                        Text(
                                            "${pair.first}: $currency${String.format("%.0f", pair.second)}",
                                            style = MaterialTheme.typography.bodySmall,
                                            fontWeight = FontWeight.SemiBold,
                                            color = textColor,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Upcoming Bills
            item {
                val unpaidReminders = billReminders.filter { !it.isPaid }
                if (unpaidReminders.isNotEmpty()) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Upcoming Bill Reminders",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = textColor
                            )
                            Text(
                                "${unpaidReminders.size} Unpaid",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFFF59E0B),
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // Horizontal list of bill reminders
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            unpaidReminders.take(2).forEach { bill ->
                                FinanceCard(
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("bill_reminder_card_${bill.title}"),
                                    isDark = isDarkMode
                                ) {
                                    Text(
                                        bill.title,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = textColor,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        "Due: ${bill.dueDate}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.Gray
                                    )
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            "$currency${String.format("%,.0f", bill.amount)}",
                                            fontWeight = FontWeight.ExtraBold,
                                            color = Color(0xFFF59E0B),
                                            style = MaterialTheme.typography.titleMedium
                                        )
                                        Button(
                                            onClick = { viewModel.payBill(bill) },
                                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 2.dp),
                                            modifier = Modifier
                                                .height(30.dp)
                                                .testTag("pay_bill_${bill.id}"),
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Text("Mark Paid", fontSize = 11.sp, color = Color.White)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Recent Transactions List
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Recent Transactions",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = textColor
                    )
                }
            }

            if (recentTx.isEmpty()) {
                item {
                    FinanceCard(isDark = isDarkMode) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(80.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "No transactions recorded yet.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.Gray
                            )
                        }
                    }
                }
            } else {
                items(recentTx) { tx ->
                    TransactionRow(
                        transaction = tx,
                        currency = currency,
                        isDark = isDarkMode,
                        onDelete = { viewModel.deleteTransaction(tx) }
                    )
                }
            }
        }

        // Custom Notification Sheet Dialog
        if (showNotificationsSheet) {
            AlertDialog(
                onDismissRequest = { showNotificationsSheet = false },
                modifier = Modifier.testTag("notifications_dialog"),
                title = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Limit Alerts & Actions", fontWeight = FontWeight.Bold)
                        IconButton(onClick = { viewModel.clearNotifications() }) {
                            Icon(Icons.Default.Delete, contentDescription = "Clear notifications", tint = Color.Gray)
                        }
                    }
                },
                text = {
                    Box(modifier = Modifier.heightIn(max = 350.dp)) {
                        if (notifications.isEmpty()) {
                            Column(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(Icons.Default.NotificationsNone, "Empty", tint = Color.Gray, modifier = Modifier.size(48.dp))
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("No alerts or notifications recorded.", color = Color.Gray)
                            }
                        } else {
                            LazyColumn(
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                items(notifications) { item ->
                                    NotificationRow(notification = item, isDark = isDarkMode)
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = { showNotificationsSheet = false },
                        modifier = Modifier.testTag("dismiss_notifications_button")
                    ) {
                        Text("Dismiss", color = Color(0xFF3B82F6))
                    }
                },
                shape = RoundedCornerShape(24.dp),
                containerColor = if (isDarkMode) Color(0xFF1E293B) else Color.White
            )
        }
    }
}

@Composable
fun BadgeItem(
    emoji: String,
    label: String,
    unlocked: Boolean
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .padding(2.dp)
            .width(50.dp)
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(if (unlocked) Color(0xFFD1FAE5) else Color.Gray.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = emoji,
                fontSize = 18.sp,
                modifier = Modifier.padding(2.dp),
                color = if (unlocked) Color.Unspecified else Color.Gray
            )
        }
        Text(
            label,
            fontSize = 9.sp,
            color = if (unlocked) Color.Unspecified else Color.Gray,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun TransactionRow(
    transaction: TransactionEntity,
    currency: String,
    isDark: Boolean,
    onDelete: () -> Unit
) {
    val rowBg = if (isDark) Color(0xFF1E293B) else Color.White
    val textThemeColor = if (isDark) Color.White else Color(0xFF0F172A)
    val isExpense = transaction.type == "EXPENSE"

    val categoryIcon = getCategoryIcon(transaction.category)
    val categoryColor = getCategoryColor(transaction.category)

    var showDeleteConfirm by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(rowBg)
            .clickable { showDeleteConfirm = true }
            .padding(14.dp)
            .testTag("transaction_row_${transaction.id}"),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            // Category Icon with Colored Circle
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(categoryColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = categoryIcon,
                    contentDescription = transaction.category,
                    tint = categoryColor,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column {
                Text(
                    text = transaction.description,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = textThemeColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = transaction.category,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(modifier = Modifier.size(3.dp).clip(CircleShape).background(Color.Gray))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = transaction.date,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
            }
        }

        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = "${if (isExpense) "-" else "+"}$currency${String.format("%,.0f", transaction.amount)}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
                color = if (isExpense) Color(0xFFEF4444) else Color(0xFF10B981)
            )
            Text(
                text = transaction.paymentMethod,
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
        }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Delete Transaction?", fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure you want to delete this log: '${transaction.description}' ($currency${transaction.amount})?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDelete()
                        showDeleteConfirm = false
                    },
                    modifier = Modifier.testTag("confirm_delete_tx")
                ) {
                    Text("Delete", color = Color.Red, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDeleteConfirm = false },
                    modifier = Modifier.testTag("cancel_delete_tx")
                ) {
                    Text("Cancel", color = Color.Gray)
                }
            },
            containerColor = if (isDark) Color(0xFF1E293B) else Color.White
        )
    }
}

@Composable
fun NotificationRow(
    notification: NotificationEntity,
    isDark: Boolean
) {
    val isAlert = notification.type == "ALERT"
    val isSystem = notification.type == "SYSTEM"
    val isSaving = notification.type == "SAVING"
    val isGroup = notification.type == "GROUP"

    val indicatorColor = when {
        isAlert -> Color(0xFFEF4444)
        isSaving -> Color(0xFF10B981)
        isGroup -> Color(0xFF3B82F6)
        else -> Color(0xFFF59E0B)
    }

    val icon = when {
        isAlert -> Icons.Default.Warning
        isSaving -> Icons.Default.Star
        isGroup -> Icons.Default.Group
        else -> Icons.Default.Notifications
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(if (isDark) Color(0xFF334155).copy(alpha = 0.5f) else Color(0xFFF1F5F9))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(indicatorColor.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, tint = indicatorColor, modifier = Modifier.size(18.dp))
        }
        
        Spacer(modifier = Modifier.width(12.dp))

        Column {
            Text(
                notification.title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = if (isDark) Color.White else Color(0xFF0F172A)
            )
            Text(
                notification.message,
                style = MaterialTheme.typography.bodySmall,
                color = if (isDark) Color(0xFFCBD5E1) else Color(0xFF475569)
            )
            Text(
                notification.date,
                style = MaterialTheme.typography.labelSmall,
                color = Color.Gray,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
    }
}

// Global visual configuration helpers for Transaction Icons
fun getCategoryIcon(category: String): ImageVector {
    return when (category) {
        "Food" -> Icons.Default.Restaurant
        "Travel" -> Icons.Default.DirectionsCar
        "Shopping" -> Icons.Default.ShoppingBag
        "College", "Books" -> Icons.Default.Book
        "Hostel", "Rent" -> Icons.Default.Home
        "Entertainment" -> Icons.Default.LocalPlay
        "Fuel" -> Icons.Default.LocalGasStation
        "Healthcare" -> Icons.Default.LocalHospital
        "Gym" -> Icons.Default.FitnessCenter
        "Bills" -> Icons.Default.ReceiptLong
        "Investment" -> Icons.Default.TrendingUp
        "Pocket Money", "Salary" -> Icons.Default.AttachMoney
        "Freelancing", "Business" -> Icons.Default.Work
        "Scholarship" -> Icons.Default.School
        else -> Icons.Default.AccountBalanceWallet
    }
}

fun getCategoryColor(category: String): Color {
    return when (category) {
        "Food" -> Color(0xFFEF4444) // Red
        "Travel" -> Color(0xFF3B82F6) // Blue
        "Shopping" -> Color(0xFFEC4899) // Pink
        "College", "Books" -> Color(0xFF8B5CF6) // Purple
        "Hostel", "Rent" -> Color(0xFFF59E0B) // Orange/Yellow
        "Entertainment" -> Color(0xFF14B8A6) // Teal
        "Fuel" -> Color(0xFF06B6D4) // Cyan
        "Healthcare" -> Color(0xFF10B981) // Green
        "Gym" -> Color(0xFF6B7280) // Gray
        "Bills" -> Color(0xFF84CC16) // Lime
        "Investment" -> Color(0xFF6366F1) // Indigo
        "Pocket Money", "Salary" -> Color(0xFF10B981)
        "Freelancing", "Business" -> Color(0xFF14B8A6)
        "Scholarship" -> Color(0xFF8B5CF6)
        else -> Color(0xFF3B82F6)
    }
}
