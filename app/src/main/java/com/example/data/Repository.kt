package com.example.data

import kotlinx.coroutines.flow.Flow

class FinanceRepository(private val dao: FinanceDao) {

    // Transactions
    val allTransactions: Flow<List<TransactionEntity>> = dao.getAllTransactions()

    suspend fun insertTransaction(transaction: TransactionEntity) {
        dao.insertTransaction(transaction)
    }

    suspend fun updateTransaction(transaction: TransactionEntity) {
        dao.updateTransaction(transaction)
    }

    suspend fun deleteTransaction(transaction: TransactionEntity) {
        dao.deleteTransaction(transaction)
    }

    suspend fun deleteTransactionById(id: Int) {
        dao.deleteTransactionById(id)
    }

    // Budgets
    val allBudgets: Flow<List<BudgetEntity>> = dao.getAllBudgets()

    suspend fun insertBudget(budget: BudgetEntity) {
        dao.insertBudget(budget)
    }

    suspend fun updateBudget(budget: BudgetEntity) {
        dao.updateBudget(budget)
    }

    suspend fun deleteBudget(budget: BudgetEntity) {
        dao.deleteBudget(budget)
    }

    suspend fun deleteBudgetById(id: Int) {
        dao.deleteBudgetById(id)
    }

    // Savings Goals
    val allSavingsGoals: Flow<List<SavingsGoalEntity>> = dao.getAllSavingsGoals()

    suspend fun insertSavingsGoal(goal: SavingsGoalEntity) {
        dao.insertSavingsGoal(goal)
    }

    suspend fun updateSavingsGoal(goal: SavingsGoalEntity) {
        dao.updateSavingsGoal(goal)
    }

    suspend fun deleteSavingsGoal(goal: SavingsGoalEntity) {
        dao.deleteSavingsGoal(goal)
    }

    // Bill Reminders
    val allBillReminders: Flow<List<BillReminderEntity>> = dao.getAllBillReminders()

    suspend fun insertBillReminder(reminder: BillReminderEntity) {
        dao.insertBillReminder(reminder)
    }

    suspend fun updateBillReminder(reminder: BillReminderEntity) {
        dao.updateBillReminder(reminder)
    }

    suspend fun deleteBillReminder(reminder: BillReminderEntity) {
        dao.deleteBillReminder(reminder)
    }

    // Group Budgets
    val allGroups: Flow<List<GroupBudgetEntity>> = dao.getAllGroups()

    suspend fun insertGroup(group: GroupBudgetEntity): Long {
        return dao.insertGroup(group)
    }

    suspend fun deleteGroup(group: GroupBudgetEntity) {
        dao.deleteGroup(group)
    }

    // Group Members
    fun getGroupMembers(groupId: Int): Flow<List<GroupMemberEntity>> {
        return dao.getGroupMembers(groupId)
    }

    suspend fun insertGroupMember(member: GroupMemberEntity) {
        dao.insertGroupMember(member)
    }

    suspend fun deleteGroupMember(member: GroupMemberEntity) {
        dao.deleteGroupMember(member)
    }

    // Group Expenses
    fun getGroupExpenses(groupId: Int): Flow<List<GroupExpenseEntity>> {
        return dao.getGroupExpenses(groupId)
    }

    suspend fun insertGroupExpense(expense: GroupExpenseEntity) {
        dao.insertGroupExpense(expense)
    }

    suspend fun deleteGroupExpense(expense: GroupExpenseEntity) {
        dao.deleteGroupExpense(expense)
    }

    // Notifications
    val allNotifications: Flow<List<NotificationEntity>> = dao.getAllNotifications()

    suspend fun insertNotification(notification: NotificationEntity) {
        dao.insertNotification(notification)
    }

    suspend fun markNotificationAsRead(id: Int) {
        dao.markNotificationAsRead(id)
    }

    suspend fun clearAllNotifications() {
        dao.clearAllNotifications()
    }

    // Chat History
    val allChatMessages: Flow<List<ChatMessageEntity>> = dao.getAllChatMessages()

    suspend fun insertChatMessage(message: ChatMessageEntity) {
        dao.insertChatMessage(message)
    }

    suspend fun clearChatHistory() {
        dao.clearChatHistory()
    }
}
