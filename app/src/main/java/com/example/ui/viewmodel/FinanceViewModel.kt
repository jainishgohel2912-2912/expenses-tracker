package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.api.GeminiClient
import com.example.data.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class FinanceViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getDatabase(application)
    private val repository = FinanceRepository(database.financeDao())

    // UI Preferences stored in SharedPreferences
    private val sharedPrefs = application.getSharedPreferences("finance_prefs", Application.MODE_PRIVATE)

    private val _currency = MutableStateFlow(sharedPrefs.getString("currency", "₹") ?: "₹")
    val currency: StateFlow<String> = _currency.asStateFlow()

    private val _isDarkMode = MutableStateFlow(sharedPrefs.getBoolean("dark_mode", true))
    val isDarkMode: StateFlow<Boolean> = _isDarkMode.asStateFlow()

    private val _userName = MutableStateFlow(sharedPrefs.getString("user_name", "Student") ?: "Student")
    val userName: StateFlow<String> = _userName.asStateFlow()

    private val _userPin = MutableStateFlow(sharedPrefs.getString("user_pin", "") ?: "")
    val userPin: StateFlow<String> = _userPin.asStateFlow()

    private val _isUserLoggedIn = MutableStateFlow(sharedPrefs.getBoolean("is_logged_in", false))
    val isUserLoggedIn: StateFlow<Boolean> = _isUserLoggedIn.asStateFlow()

    // Room Database Flows
    val transactions: StateFlow<List<TransactionEntity>> = repository.allTransactions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val budgets: StateFlow<List<BudgetEntity>> = repository.allBudgets
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val savingsGoals: StateFlow<List<SavingsGoalEntity>> = repository.allSavingsGoals
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val billReminders: StateFlow<List<BillReminderEntity>> = repository.allBillReminders
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val groups: StateFlow<List<GroupBudgetEntity>> = repository.allGroups
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val notifications: StateFlow<List<NotificationEntity>> = repository.allNotifications
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val chatMessages: StateFlow<List<ChatMessageEntity>> = repository.allChatMessages
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // AI Assist loading state
    private val _aiLoading = MutableStateFlow(false)
    val aiLoading: StateFlow<Boolean> = _aiLoading.asStateFlow()

    // Voice log result toast-like state
    private val _voiceLogResult = MutableSharedFlow<String>()
    val voiceLogResult = _voiceLogResult.asSharedFlow()

    init {
        // Pre-populate data if transactions are empty to avoid blank initial states
        viewModelScope.launch(Dispatchers.IO) {
            repository.allTransactions.first().let { list ->
                if (list.isEmpty()) {
                    prepopulateSampleData()
                }
            }
        }
    }

    // --- Dynamic Computations ---

    val activeBudget: StateFlow<Double> = budgets.map { list ->
        list.firstOrNull { it.period == "MONTHLY" && it.category == "ALL" }?.amount ?: 10000.0
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 10000.0)

    val spentThisMonth: StateFlow<Double> = transactions.map { list ->
        val currentMonth = SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(Date())
        list.filter { it.type == "EXPENSE" && it.date.startsWith(currentMonth) }
            .sumOf { it.amount }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val incomeThisMonth: StateFlow<Double> = transactions.map { list ->
        val currentMonth = SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(Date())
        list.filter { it.type == "INCOME" && it.date.startsWith(currentMonth) }
            .sumOf { it.amount }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val remainingBudget: StateFlow<Double> = combine(activeBudget, spentThisMonth, incomeThisMonth) { budget, spent, income ->
        // Total available is budget + any income, minus spending
        budget + income - spent
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 10000.0)

    val totalSavings: StateFlow<Double> = savingsGoals.map { list ->
        list.sumOf { it.currentAmount }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val financialHealthScore: StateFlow<Int> = combine(activeBudget, spentThisMonth, savingsGoals, billReminders) { budget, spent, goals, bills ->
        if (budget <= 0.0) return@combine 50
        
        var score = 100
        
        // 1. Budget Adherence (max deduction: 40)
        val spentRatio = spent / budget
        when {
            spentRatio > 1.2 -> score -= 40 // crossed budget by 20%+
            spentRatio > 1.0 -> score -= 30 // crossed budget
            spentRatio > 0.9 -> score -= 15 // close to budget
            spentRatio > 0.4 && spentRatio <= 0.8 -> score -= 0 // perfect spending
            spentRatio <= 0.4 -> score -= 5 // too little spending or low-activity
        }

        // 2. Savings goals progress (max bonus/deduction: 30)
        val goalCount = goals.size
        if (goalCount > 0) {
            val totalTarget = goals.sumOf { it.targetAmount }
            val totalSaved = goals.sumOf { it.currentAmount }
            val progress = if (totalTarget > 0) totalSaved / totalTarget else 0.0
            when {
                progress >= 0.8 -> score += 10
                progress < 0.3 -> score -= 15
                else -> score -= 5
            }
        } else {
            score -= 10 // no savings goals set
        }

        // 3. Bill payment behavior (max deduction: 30)
        val unpaidBills = bills.filter { !it.isPaid }
        if (unpaidBills.isNotEmpty()) {
            score -= (unpaidBills.size * 8).coerceAtMost(30)
        }

        score.coerceIn(0, 100)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 75)

    val budgetStreak: StateFlow<Int> = transactions.map { list ->
        // Number of consecutive days in the last 30 days where daily spending did not exceed a daily threshold (e.g. 500)
        val dailyLimit = 500.0
        val expensesByDay = list.filter { it.type == "EXPENSE" }
            .groupBy { it.date }
            .mapValues { entry -> entry.value.sumOf { it.amount } }

        var streak = 0
        val cal = Calendar.getInstance()
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        
        for (i in 0 until 30) {
            val dateStr = sdf.format(cal.time)
            val spentOnDay = expensesByDay[dateStr] ?: 0.0
            if (spentOnDay <= dailyLimit && spentOnDay > 0.0) {
                streak++
            } else if (spentOnDay > dailyLimit) {
                break
            }
            cal.add(Calendar.DAY_OF_YEAR, -1)
        }
        streak
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 5)

    // --- Action Methods ---

    fun setCurrency(symbol: String) {
        _currency.value = symbol
        sharedPrefs.edit().putString("currency", symbol).apply()
    }

    fun setDarkMode(enabled: Boolean) {
        _isDarkMode.value = enabled
        sharedPrefs.edit().putBoolean("dark_mode", enabled).apply()
    }

    fun updateProfile(name: String, pin: String) {
        _userName.value = name
        _userPin.value = pin
        sharedPrefs.edit()
            .putString("user_name", name)
            .putString("user_pin", pin)
            .putBoolean("is_logged_in", true)
            .apply()
        _isUserLoggedIn.value = true
    }

    fun completeLogin(name: String, emailOrPhone: String) {
        val finalName = if (name.isNotBlank()) name.trim() else "Student"
        _userName.value = finalName
        sharedPrefs.edit()
            .putString("user_name", finalName)
            .putString("user_contact", emailOrPhone.trim())
            .putBoolean("is_logged_in", true)
            .apply()
        _isUserLoggedIn.value = true
    }

    fun logout() {
        _isUserLoggedIn.value = false
        sharedPrefs.edit().putBoolean("is_logged_in", false).apply()
    }

    fun login(pin: String): Boolean {
        return if (pin == _userPin.value || _userPin.value.isEmpty()) {
            _isUserLoggedIn.value = true
            sharedPrefs.edit().putBoolean("is_logged_in", true).apply()
            true
        } else {
            false
        }
    }

    // Transactions Actions
    fun addTransaction(
        amount: Double,
        category: String,
        description: String,
        type: String,
        paymentMethod: String,
        date: String = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()),
        time: String = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date()),
        location: String? = null,
        notes: String? = null,
        receiptPath: String? = null,
        groupId: Int? = null
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val tx = TransactionEntity(
                amount = amount,
                category = category,
                description = description,
                date = date,
                time = time,
                type = type,
                paymentMethod = paymentMethod,
                location = location,
                notes = notes,
                receiptPath = receiptPath,
                groupId = groupId
            )
            repository.insertTransaction(tx)

            // Trigger Budget Limits checks on Expenses
            if (type == "EXPENSE") {
                checkBudgetAlerts(amount, category)
            }
        }
    }

    fun deleteTransaction(tx: TransactionEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteTransaction(tx)
        }
    }

    // Budgets Actions
    fun setBudget(amount: Double, period: String = "MONTHLY", category: String = "ALL", thresholds: String = "500,1000,2000,5000") {
        viewModelScope.launch(Dispatchers.IO) {
            val existing = repository.allBudgets.first().firstOrNull { it.period == period && it.category == category }
            val budget = BudgetEntity(
                id = existing?.id ?: 0,
                amount = amount,
                period = period,
                category = category,
                alertThresholds = thresholds
            )
            repository.insertBudget(budget)
        }
    }

    // Savings Goals Actions
    fun addSavingsGoal(name: String, target: Double, current: Double, expectedDate: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val goal = SavingsGoalEntity(
                name = name,
                targetAmount = target,
                currentAmount = current,
                expectedDate = expectedDate,
                isCompleted = current >= target
            )
            repository.insertSavingsGoal(goal)
        }
    }

    fun updateSavingsProgress(goal: SavingsGoalEntity, addAmount: Double) {
        viewModelScope.launch(Dispatchers.IO) {
            val newAmount = (goal.currentAmount + addAmount).coerceIn(0.0, goal.targetAmount)
            val updated = goal.copy(currentAmount = newAmount, isCompleted = newAmount >= goal.targetAmount)
            repository.insertSavingsGoal(updated)

            if (updated.isCompleted && !goal.isCompleted) {
                // Generate a notification for completing a savings goal!
                repository.insertNotification(
                    NotificationEntity(
                        title = "🎯 Savings Goal Achieved!",
                        message = "Congratulations! You have successfully saved ${currency.value}${goal.targetAmount} for '${goal.name}'!",
                        date = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date()),
                        type = "SAVING"
                    )
                )
            }
        }
    }

    fun deleteSavingsGoal(goal: SavingsGoalEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteSavingsGoal(goal)
        }
    }

    // Bill Reminders
    fun addBillReminder(title: String, amount: Double, dueDate: String, category: String, isRecurring: Boolean = false, period: String? = null) {
        viewModelScope.launch(Dispatchers.IO) {
            val bill = BillReminderEntity(
                title = title,
                amount = amount,
                dueDate = dueDate,
                isPaid = false,
                category = category,
                isRecurring = isRecurring,
                recurringPeriod = period
            )
            repository.insertBillReminder(bill)
        }
    }

    fun payBill(bill: BillReminderEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            // Mark bill as paid
            repository.insertBillReminder(bill.copy(isPaid = true))

            // Auto-add paid bill as an EXPENSE!
            addTransaction(
                amount = bill.amount,
                category = bill.category,
                description = "Paid Bill: ${bill.title}",
                type = "EXPENSE",
                paymentMethod = "Cash"
            )

            // Auto-schedule next bill if recurring
            if (bill.isRecurring && bill.recurringPeriod != null) {
                val cal = Calendar.getInstance()
                val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val currentDueDate = sdf.parse(bill.dueDate) ?: Date()
                cal.time = currentDueDate
                when (bill.recurringPeriod) {
                    "WEEKLY" -> cal.add(Calendar.WEEK_OF_YEAR, 1)
                    "MONTHLY" -> cal.add(Calendar.MONTH, 1)
                    "YEARLY" -> cal.add(Calendar.YEAR, 1)
                }
                repository.insertBillReminder(
                    BillReminderEntity(
                        title = bill.title,
                        amount = bill.amount,
                        dueDate = sdf.format(cal.time),
                        isPaid = false,
                        category = bill.category,
                        isRecurring = true,
                        recurringPeriod = bill.recurringPeriod
                    )
                )
            }
        }
    }

    fun deleteBillReminder(bill: BillReminderEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteBillReminder(bill)
        }
    }

    fun deleteGroup(group: GroupBudgetEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteGroup(group)
        }
    }

    // Group Budgets Actions
    fun createGroup(name: String, description: String, totalBudget: Double, members: List<String>) {
        viewModelScope.launch(Dispatchers.IO) {
            val groupId = repository.insertGroup(GroupBudgetEntity(name = name, description = description, totalBudget = totalBudget))
            
            // Add Self as a member
            repository.insertGroupMember(GroupMemberEntity(groupId = groupId.toInt(), memberName = "You"))
            
            // Add other members
            for (m in members) {
                if (m.isNotBlank()) {
                    repository.insertGroupMember(GroupMemberEntity(groupId = groupId.toInt(), memberName = m.trim()))
                }
            }

            // Generate notification
            repository.insertNotification(
                NotificationEntity(
                    title = "👥 Shared Group Created",
                    message = "Group budget for '$name' is set up with ${members.size + 1} members.",
                    date = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date()),
                    type = "GROUP"
                )
            )
        }
    }

    fun addGroupExpense(groupId: Int, amount: Double, description: String, paidBy: String, category: String, groupName: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val exp = GroupExpenseEntity(
                groupId = groupId,
                amount = amount,
                description = description,
                paidBy = paidBy,
                category = category,
                date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            )
            repository.insertGroupExpense(exp)

            // Notify group
            repository.insertNotification(
                NotificationEntity(
                    title = "💸 Group Expense added in $groupName",
                    message = "$paidBy paid ${currency.value}$amount for '$description'.",
                    date = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date()),
                    type = "GROUP"
                )
            )
        }
    }

    // Delete notifications
    fun clearNotifications() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.clearAllNotifications()
        }
    }

    // Chat with AI Spending Assistant
    fun sendChatMessage(text: String) {
        if (text.trim().isEmpty()) return

        viewModelScope.launch(Dispatchers.IO) {
            // Save user message
            repository.insertChatMessage(ChatMessageEntity(text = text, isUser = true))

            _aiLoading.value = true

            // Gather context to make the AI extremely smart and context-aware!
            val recentTx = repository.allTransactions.first().take(10).joinToString("\n") {
                "${it.date} | ${it.type} | ${currency.value}${it.amount} | ${it.category} | ${it.description}"
            }
            val activeBudgets = repository.allBudgets.first().joinToString("\n") {
                "${it.period} budget for ${it.category}: ${currency.value}${it.amount}"
            }
            val activeGoals = repository.allSavingsGoals.first().joinToString("\n") {
                "${it.name}: Target ${currency.value}${it.targetAmount}, saved ${currency.value}${it.currentAmount} (Expected: ${it.expectedDate})"
            }
            val unpaidBills = repository.allBillReminders.first().filter { !it.isPaid }.joinToString("\n") {
                "${it.title}: ${currency.value}${it.amount} due on ${it.dueDate}"
            }

            val systemPrompt = """
                You are a smart personal finance AI assistant and budgeting coach for college students and individuals.
                You have access to the user's real financial status:
                - User Name: ${userName.value}
                - Selected Currency: ${currency.value}
                - Total Active Budget: ${currency.value}${activeBudget.value}
                - Spent this month: ${currency.value}${spentThisMonth.value}
                - Income this month: ${currency.value}${incomeThisMonth.value}
                - Remaining budget: ${currency.value}${remainingBudget.value}
                - Financial Health Score: ${financialHealthScore.value}/100
                - Budget Streak: ${budgetStreak.value} days
                
                Active Budgets:
                $activeBudgets
                
                Savings Goals:
                $activeGoals
                
                Upcoming Unpaid Bills:
                $unpaidBills
                
                Recent Transactions:
                $recentTx

                Give the user tailored, highly relevant financial tips. Answer their questions about savings, split bills, and daily costs. Do not output raw markdown lists if they look cluttered. Keep your answers conversational, concise (max 3-4 paragraphs), supportive, and extremely helpful.
            """.trimIndent()

            val aiResponse = GeminiClient.askGemini(prompt = text, systemPrompt = systemPrompt)
            
            _aiLoading.value = false
            repository.insertChatMessage(ChatMessageEntity(text = aiResponse, isUser = false))
        }
    }

    fun clearChat() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.clearChatHistory()
            // Add standard welcoming message
            repository.insertChatMessage(
                ChatMessageEntity(
                    text = "Hello! I am your AI Finance Companion. Ask me to analyze your spending, suggest saving strategies, or tell me 'I spent ${currency.value}250 on books' to quick log expenses!",
                    isUser = false
                )
            )
        }
    }

    // Voice / Quick Input parsing using Gemini REST API
    fun logTransactionViaVoiceText(naturalInput: String) {
        viewModelScope.launch(Dispatchers.IO) {
            _aiLoading.value = true
            val parsed = GeminiClient.parseNaturalLanguageTransaction(naturalInput)
            _aiLoading.value = false

            if (parsed != null) {
                // Insert transaction
                addTransaction(
                    amount = parsed.amount,
                    category = parsed.category,
                    description = parsed.description,
                    type = parsed.type,
                    paymentMethod = parsed.paymentMethod
                )
                
                val typeWord = if (parsed.type == "EXPENSE") "Expense" else "Income"
                val successMessage = "Successfully logged $typeWord of ${currency.value}${parsed.amount} under Category '${parsed.category}' (${parsed.description})!"
                _voiceLogResult.emit(successMessage)

                // Add to chatbot history as logged transaction update
                repository.insertChatMessage(ChatMessageEntity(text = "Quick log: \"$naturalInput\"", isUser = true))
                repository.insertChatMessage(ChatMessageEntity(text = "Awesome! I have processed that and logged a ${parsed.type.lowercase()} of ${currency.value}${parsed.amount} for '${parsed.description}' in your '${parsed.category}' category. Let me know if you need anything else!", isUser = false))
            } else {
                _voiceLogResult.emit("Sorry, I couldn't parse the transaction from that description. Try again like: 'I spent 200 on lunch'.")
            }
        }
    }

    // Helper method to check budget alerts when an expense is recorded
    private suspend fun checkBudgetAlerts(amount: Double, category: String) {
        val budgetVal = activeBudget.value
        val spentVal = spentThisMonth.value + amount
        val remainingVal = budgetVal - spentVal

        // Retrieve alert thresholds
        val existingBudget = repository.allBudgets.first().firstOrNull { it.period == "MONTHLY" && it.category == "ALL" }
        val thresholds = existingBudget?.alertThresholds?.split(",")?.mapNotNull { it.trim().toDoubleOrNull() } 
            ?: listOf(500.0, 1000.0, 2000.0, 5000.0)

        val dateStr = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date())

        // 1. Check if budget exceeded completely
        if (remainingVal < 0.0) {
            val crossedBy = -remainingVal
            repository.insertNotification(
                NotificationEntity(
                    title = "🚨 Budget Exceeded!",
                    message = "Warning! You have crossed your monthly budget by ${currency.value}$crossedBy. Try to reduce food delivery, unnecessary shopping, and postpone non-essential purchases.",
                    date = dateStr,
                    type = "ALERT"
                )
            )
            return
        }

        // 2. Check individual thresholds (remaining limits)
        for (t in thresholds) {
            // If the transaction just crossed this remaining limit
            val previousRemaining = remainingVal + amount
            if (previousRemaining >= t && remainingVal < t) {
                repository.insertNotification(
                    NotificationEntity(
                        title = "⚠️ Warning: Budget Alert",
                        message = "You only have ${currency.value}$t left for this month. Focus on reducing shopping and avoid unnecessary dining out to stay in bounds.",
                        date = dateStr,
                        type = "ALERT"
                    )
                )
                break // Only trigger one notification per transaction
            }
        }
    }

    // Seed initial records so the app immediately looks beautiful and highly complete on first launch
    private suspend fun prepopulateSampleData() {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val cal = Calendar.getInstance()

        // 1. Seed Monthly budget
        repository.insertBudget(
            BudgetEntity(
                amount = 10000.0,
                period = "MONTHLY",
                category = "ALL",
                alertThresholds = "500,1000,2000,5000"
            )
        )

        // 2. Seed Transactions (Expense & Income)
        val today = sdf.format(cal.time)
        
        cal.add(Calendar.DAY_OF_YEAR, -1)
        val yesterday = sdf.format(cal.time)

        cal.add(Calendar.DAY_OF_YEAR, -2)
        val twoDaysAgo = sdf.format(cal.time)

        cal.add(Calendar.DAY_OF_YEAR, -5)
        val fiveDaysAgo = sdf.format(cal.time)

        // Re-set calendar
        cal.time = Date()

        val sampleTransactions = listOf(
            TransactionEntity(amount = 8000.0, category = "Pocket Money", description = "Monthly Pocket Money", date = fiveDaysAgo, time = "10:00", type = "INCOME", paymentMethod = "Bank Transfer"),
            TransactionEntity(amount = 5000.0, category = "Scholarship", description = "Merit Scholarship Stipend", date = twoDaysAgo, time = "11:30", type = "INCOME", paymentMethod = "Bank Transfer"),
            
            TransactionEntity(amount = 2500.0, category = "Hostel", description = "Hostel Rent Deposit", date = fiveDaysAgo, time = "14:00", type = "EXPENSE", paymentMethod = "Bank Transfer"),
            TransactionEntity(amount = 1200.0, category = "Books", description = "Semester Textbooks Purchase", date = twoDaysAgo, time = "15:00", type = "EXPENSE", paymentMethod = "UPI"),
            TransactionEntity(amount = 650.0, category = "Food", description = "Dinner delivery at hostel", date = yesterday, time = "20:30", type = "EXPENSE", paymentMethod = "UPI"),
            TransactionEntity(amount = 450.0, category = "Entertainment", description = "Cinema Movie Ticket", date = yesterday, time = "18:15", type = "EXPENSE", paymentMethod = "Card"),
            TransactionEntity(amount = 350.0, category = "Travel", description = "Train travel ticket", date = today, time = "09:00", type = "EXPENSE", paymentMethod = "UPI"),
            TransactionEntity(amount = 120.0, category = "Food", description = "College Cafeteria lunch", date = today, time = "13:10", type = "EXPENSE", paymentMethod = "Cash"),
            TransactionEntity(amount = 520.0, category = "Shopping", description = "Running shoes discount sale", date = today, time = "16:45", type = "EXPENSE", paymentMethod = "Card")
        )

        for (tx in sampleTransactions) {
            repository.insertTransaction(tx)
        }

        // 3. Seed Savings Goal
        cal.add(Calendar.MONTH, 6)
        repository.insertSavingsGoal(
            SavingsGoalEntity(
                name = "🎯 New Laptop",
                targetAmount = 80000.0,
                currentAmount = 42000.0,
                expectedDate = sdf.format(cal.time),
                isCompleted = false
            )
        )
        
        cal.add(Calendar.MONTH, -6) // Reset calendar

        // 4. Seed Bill Reminders
        cal.add(Calendar.DAY_OF_YEAR, 3)
        repository.insertBillReminder(
            BillReminderEntity(
                title = "Netflix Premium",
                amount = 199.0,
                dueDate = sdf.format(cal.time),
                isPaid = false,
                category = "Entertainment",
                isRecurring = true,
                recurringPeriod = "MONTHLY"
            )
        )
        
        cal.add(Calendar.DAY_OF_YEAR, 7) // 10 days from now
        repository.insertBillReminder(
            BillReminderEntity(
                title = "College Tuition installment",
                amount = 15000.0,
                dueDate = sdf.format(cal.time),
                isPaid = false,
                category = "College",
                isRecurring = false
            )
        )

        // 5. Seed Chat welcoming message
        repository.insertChatMessage(
            ChatMessageEntity(
                text = "Hello! I am your AI Finance Companion. Ask me to analyze your spending, suggest saving strategies, or tell me 'I spent ${currency.value}250 on books' to quick log expenses!",
                isUser = false
            )
        )

        // 6. Seed default notifications
        val nowStr = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date())
        repository.insertNotification(
            NotificationEntity(
                title = "🔔 Welcome to Smart Expense Tracker",
                message = "Let's manage your finance! Monthly budget initialized at ₹10,000. Try setting up custom saving goals and alerts.",
                date = nowStr,
                type = "SYSTEM"
            )
        )
    }
}
