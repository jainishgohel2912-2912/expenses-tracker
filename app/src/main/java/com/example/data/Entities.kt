package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val amount: Double,
    val category: String,
    val description: String,
    val date: String, // YYYY-MM-DD
    val time: String, // HH:MM
    val type: String, // "EXPENSE" or "INCOME"
    val paymentMethod: String, // "Cash", "Card", "UPI", "Bank Transfer"
    val location: String? = null,
    val notes: String? = null,
    val receiptPath: String? = null,
    val groupId: Int? = null // null for personal, non-null for group split
)

@Entity(tableName = "budgets")
data class BudgetEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val amount: Double,
    val period: String, // "DAILY", "WEEKLY", "MONTHLY", "YEARLY"
    val category: String = "ALL", // "ALL" means overall, or specific category name
    val alertThresholds: String = "1000,2000,5000" // remaining amounts to trigger alert
)

@Entity(tableName = "savings_goals")
data class SavingsGoalEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val targetAmount: Double,
    val currentAmount: Double,
    val expectedDate: String, // YYYY-MM-DD
    val isCompleted: Boolean = false
)

@Entity(tableName = "bill_reminders")
data class BillReminderEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val amount: Double,
    val dueDate: String, // YYYY-MM-DD
    val isPaid: Boolean = false,
    val category: String,
    val isRecurring: Boolean = false,
    val recurringPeriod: String? = null // "WEEKLY", "MONTHLY", "YEARLY"
)

@Entity(tableName = "groups")
data class GroupBudgetEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val description: String,
    val totalBudget: Double
)

@Entity(tableName = "group_members")
data class GroupMemberEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val groupId: Int,
    val memberName: String,
    val shareRatio: Double = 1.0 // for custom weight split, default 1.0 (equal)
)

@Entity(tableName = "group_expenses")
data class GroupExpenseEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val groupId: Int,
    val amount: Double,
    val description: String,
    val paidBy: String, // member name who paid
    val category: String,
    val date: String // YYYY-MM-DD
)

@Entity(tableName = "notifications")
data class NotificationEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val message: String,
    val date: String, // YYYY-MM-DD HH:MM
    val isRead: Boolean = false,
    val type: String // "ALERT", "BILL", "SAVING", "GROUP", "SYSTEM"
)

@Entity(tableName = "chat_messages")
data class ChatMessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val text: String,
    val isUser: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)
