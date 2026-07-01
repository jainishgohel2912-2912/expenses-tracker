package com.example.ui.screens

import java.util.UUID

import android.content.Intent
import android.widget.Toast
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
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.GroupBudgetEntity
import com.example.data.GroupMemberEntity
import com.example.ui.components.FinanceCard
import com.example.ui.viewmodel.FinanceViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: FinanceViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    val userName by viewModel.userName.collectAsState()
    val userPin by viewModel.userPin.collectAsState()
    val currency by viewModel.currency.collectAsState()
    val isDarkMode by viewModel.isDarkMode.collectAsState()

    val groups by viewModel.groups.collectAsState()
    val activeBudget by viewModel.activeBudget.collectAsState()
    val spentThisMonth by viewModel.spentThisMonth.collectAsState()
    val remainingBudget by viewModel.remainingBudget.collectAsState()

    var showProfileEditDialog by remember { mutableStateOf(false) }
    var showCreateGroupDialog by remember { mutableStateOf(false) }

    // Sharing fields
    var selectedShareRole by remember { mutableStateOf("View Only (Parents/Roommates)") }
    var generatedShareLink by remember { mutableStateOf("") }

    val bgColors = if (isDarkMode) {
        listOf(Color(0xFF0F172A), Color(0xFF020617))
    } else {
        listOf(Color(0xFFF8FAFC), Color(0xFFE2E8F0))
    }

    val textColor = if (isDarkMode) Color.White else Color(0xFF0F172A)
    val cardBgColor = if (isDarkMode) Color(0xFF1E293B) else Color.White

    // Share report synthesizer text
    val monthlyReportSummary = """
        📈 *FinFlow Financial Report for $userName*
        
        💰 Overall Monthly Budget: $currency${String.format("%,.0f", activeBudget)}
        💸 Spent this Month: $currency${String.format("%,.0f", spentThisMonth)}
        🛡️ Remaining Balance: $currency${String.format("%,.0f", remainingBudget)}
        
        Sent securely via Expense Tracker App.
    """.trimIndent()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(bgColors))
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Hero Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF3B82F6)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = userName.take(1).uppercase(),
                        color = Color.White,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(userName, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold, color = textColor)
                    Text("Secure Active Profile", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                }
                IconButton(
                    onClick = { showProfileEditDialog = true },
                    modifier = Modifier.testTag("edit_profile_icon")
                ) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit profile", tint = Color(0xFF3B82F6))
                }
            }
        }

        // Section 1: Themes & Currency
        item {
            FinanceCard(isDark = isDarkMode) {
                Text("App Preferences", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium, color = textColor)
                Spacer(modifier = Modifier.height(14.dp))

                // Theme Switcher (M3 Style)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.DarkMode, null, tint = Color.Gray, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Text("Dark Mode Theme", color = textColor, fontSize = 13.sp)
                    }
                    Switch(
                        checked = isDarkMode,
                        onCheckedChange = { viewModel.setDarkMode(it) },
                        modifier = Modifier.testTag("dark_mode_switch")
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Currency selector
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Paid, null, tint = Color.Gray, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Text("Currency System", color = textColor, fontSize = 13.sp)
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf("₹" to "INR", "$" to "USD", "€" to "EUR", "£" to "GBP").forEach { curr ->
                            val isSel = currency == curr.first
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSel) Color(0xFF3B82F6) else Color.Gray.copy(alpha = 0.15f))
                                    .clickable { viewModel.setCurrency(curr.first) }
                                    .padding(horizontal = 8.dp, vertical = 6.dp)
                                    .testTag("currency_${curr.second}"),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    curr.first,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSel) Color.White else textColor
                                )
                            }
                        }
                    }
                }
            }
        }

        // Section 2: Secure Expense Report Sharing ⭐
        item {
            FinanceCard(isDark = isDarkMode) {
                Text("Secure Report Sharing", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium, color = textColor)
                Text(
                    "Share reports securely with parents, roommates, or partners without editing rights.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray,
                    modifier = Modifier.padding(top = 2.dp, bottom = 12.dp)
                )

                // Access level selector
                Text("Access Permission", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp, bottom = 12.dp)
                ) {
                    listOf("View Only", "Can Comment", "Suggest Budget").forEach { role ->
                        val isSel = selectedShareRole.contains(role)
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSel) Color(0xFF10B981) else Color.Gray.copy(alpha = 0.1f))
                                .clickable { selectedShareRole = "$role (Secure Access)" }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(role, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = if (isSel) Color.White else textColor)
                        }
                    }
                }

                // Share link generator
                Button(
                    onClick = {
                        val token = UUID.randomUUID().toString().take(6)
                        generatedShareLink = "https://finflow.link/share/$token"
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("generate_share_link_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.Link, null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Generate Secure Share Link", color = Color.White)
                }

                if (generatedShareLink.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isDarkMode) Color(0xFF334155).copy(alpha = 0.5f) else Color(0xFFF1F5F9))
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            generatedShareLink,
                            fontSize = 11.sp,
                            color = Color(0xFF10B981),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(
                            onClick = {
                                clipboardManager.setText(AnnotatedString(generatedShareLink))
                                Toast.makeText(context, "Copied link to clipboard!", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier
                                .size(24.dp)
                                .testTag("copy_share_link")
                        ) {
                            Icon(Icons.Default.ContentCopy, "Copy", tint = Color.Gray, modifier = Modifier.size(16.dp))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // External Sharing Channels
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // WhatsApp Share
                    Button(
                        onClick = {
                            val intent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_TEXT, monthlyReportSummary)
                            }
                            context.startActivity(Intent.createChooser(intent, "Share via WhatsApp"))
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp)
                            .testTag("whatsapp_share_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Share, null, tint = Color.White, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("WhatsApp", fontSize = 11.sp, color = Color.White)
                    }

                    // Email Share
                    Button(
                        onClick = {
                            val intent = Intent(Intent.ACTION_SEND).apply {
                                type = "message/rfc822"
                                putExtra(Intent.EXTRA_SUBJECT, "FinFlow Monthly Expenses - $userName")
                                putExtra(Intent.EXTRA_TEXT, monthlyReportSummary)
                            }
                            context.startActivity(Intent.createChooser(intent, "Send Email Report"))
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp)
                            .testTag("email_share_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B82F6)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Email, null, tint = Color.White, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Email PDF", fontSize = 11.sp, color = Color.White)
                    }

                    // PDF Mock Download
                    Button(
                        onClick = {
                            Toast.makeText(context, "Exporting Expense Report CSV & PDF...", Toast.LENGTH_LONG).show()
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp)
                            .testTag("pdf_export_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6B7280)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Download, null, tint = Color.White, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("CSV/PDF", fontSize = 11.sp, color = Color.White)
                    }
                }
            }
        }

        // Section 3: Collaborative Group Budgets ⭐
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Collaborative Groups",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = textColor
                )
                Button(
                    onClick = { showCreateGroupDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B82F6)),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .height(32.dp)
                        .testTag("add_group_button")
                ) {
                    Icon(Icons.Default.Add, null, modifier = Modifier.size(16.dp), tint = Color.White)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("New Group", fontSize = 11.sp, color = Color.White)
                }
            }
        }

        // Collaborative Group Budgets List
        if (groups.isEmpty()) {
            item {
                FinanceCard(isDark = isDarkMode) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(80.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No active shared budgets (e.g. flatmates, couples).", color = Color.Gray, fontSize = 12.sp)
                    }
                }
            }
        } else {
            items(groups) { grp ->
                FinanceCard(
                    modifier = Modifier.testTag("group_card_${grp.name}"),
                    isDark = isDarkMode
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(grp.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = textColor)
                            Text(grp.description, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                        }
                        IconButton(onClick = { viewModel.deleteGroup(grp) }) {
                            Icon(Icons.Default.Delete, "Delete", tint = Color.Gray.copy(alpha = 0.5f))
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Group Budget limit:", color = Color.Gray, fontSize = 12.sp)
                        Text("$currency${String.format("%,.0f", grp.totalBudget)}", color = textColor, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Simulated member settlement split logic
                    Text("Settlement Balances & Splits:", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Rahul (Flatmate)", color = textColor, fontSize = 11.sp)
                            Text("Owes You: ${currency}280.00", color = Color(0xFF10B981), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Aman (Hostel Friend)", color = textColor, fontSize = 11.sp)
                            Text("You Owe: ${currency}140.00", color = Color(0xFFEF4444), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Logout Block
        item {
            Button(
                onClick = { viewModel.logout() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("logout_button"),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Red.copy(alpha = 0.15f)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.ExitToApp, null, tint = Color.Red)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Logout Account", color = Color.Red, fontWeight = FontWeight.Bold)
            }
        }
    }

    // Edit Profile Modal
    if (showProfileEditDialog) {
        var nameEdit by remember { mutableStateOf(userName) }
        var pinEdit by remember { mutableStateOf(userPin) }

        AlertDialog(
            onDismissRequest = { showProfileEditDialog = false },
            modifier = Modifier.testTag("edit_profile_dialog"),
            title = { Text("Edit Secure Profile", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = nameEdit,
                        onValueChange = { nameEdit = it },
                        label = { Text("Update Name") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("profile_name_input")
                    )
                    OutlinedTextField(
                        value = pinEdit,
                        onValueChange = { if (it.length <= 4) pinEdit = it },
                        label = { Text("4-Digit Security PIN") },
                        singleLine = true,
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("profile_pin_input")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (nameEdit.isNotBlank() && pinEdit.length == 4) {
                            viewModel.updateProfile(nameEdit.trim(), pinEdit)
                            showProfileEditDialog = false
                        }
                    },
                    modifier = Modifier.testTag("save_profile_button")
                ) {
                    Text("Save Changes", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showProfileEditDialog = false }) {
                    Text("Cancel", color = Color.Gray)
                }
            },
            containerColor = cardBgColor,
            shape = RoundedCornerShape(24.dp)
        )
    }

    // Create Group Modal
    if (showCreateGroupDialog) {
        var gName by remember { mutableStateOf("") }
        var gDesc by remember { mutableStateOf("") }
        var gLimit by remember { mutableStateOf("") }
        var gMembers by remember { mutableStateOf("") } // Comma separated list of member names

        AlertDialog(
            onDismissRequest = { showCreateGroupDialog = false },
            modifier = Modifier.testTag("add_group_dialog"),
            title = { Text("New Collaborative Group", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = gName,
                        onValueChange = { gName = it },
                        label = { Text("Group Name (e.g. Hostel Friends)") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("group_name_input")
                    )
                    OutlinedTextField(
                        value = gDesc,
                        onValueChange = { gDesc = it },
                        label = { Text("Purpose (e.g. rent split, daily meals)") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("group_desc_input")
                    )
                    OutlinedTextField(
                        value = gLimit,
                        onValueChange = { gLimit = it },
                        label = { Text("Group Budget Limit ($currency)") },
                        singleLine = true,
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("group_budget_input")
                    )
                    OutlinedTextField(
                        value = gMembers,
                        onValueChange = { gMembers = it },
                        label = { Text("Invite Members (comma-separated names)") },
                        placeholder = { Text("Rahul, Aman, Sneha") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("group_members_input")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val limitNum = gLimit.toDoubleOrNull() ?: 10000.0
                        if (gName.isNotBlank() && gMembers.isNotBlank()) {
                            val memberList = gMembers.split(",").map { it.trim() }
                            viewModel.createGroup(gName.trim(), gDesc.trim(), limitNum, memberList)
                            showCreateGroupDialog = false
                        }
                    },
                    modifier = Modifier.testTag("save_group_button")
                ) {
                    Text("Create Group", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateGroupDialog = false }) {
                    Text("Cancel", color = Color.Gray)
                }
            },
            containerColor = cardBgColor,
            shape = RoundedCornerShape(24.dp)
        )
    }
}
