package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.viewmodel.FinanceViewModel
import kotlin.random.Random

// CubicBezier easing that works beautifully
val EasyInOutEasing = CubicBezierEasing(0.42f, 0f, 0.58f, 1f)

@Composable
fun AnimatedFloatingParticles(isDarkMode: Boolean) {
    val infiniteTransition = rememberInfiniteTransition(label = "particles")

    // Slow organic floating animations
    val offset1Y by infiniteTransition.animateFloat(
        initialValue = -60f,
        targetValue = 60f,
        animationSpec = infiniteRepeatable(
            animation = tween(6500, easing = EasyInOutEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "p1_y"
    )
    val offset1X by infiniteTransition.animateFloat(
        initialValue = -40f,
        targetValue = 40f,
        animationSpec = infiniteRepeatable(
            animation = tween(8000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "p1_x"
    )

    val offset2Y by infiniteTransition.animateFloat(
        initialValue = 80f,
        targetValue = -80f,
        animationSpec = infiniteRepeatable(
            animation = tween(7500, easing = EasyInOutEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "p2_y"
    )
    val offset2X by infiniteTransition.animateFloat(
        initialValue = 50f,
        targetValue = -50f,
        animationSpec = infiniteRepeatable(
            animation = tween(9000, easing = EasyInOutEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "p2_x"
    )

    val offset3Y by infiniteTransition.animateFloat(
        initialValue = -30f,
        targetValue = 90f,
        animationSpec = infiniteRepeatable(
            animation = tween(5000, easing = EasyInOutEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "p3_y"
    )

    val p1Color = if (isDarkMode) Color(0xFF3B82F6).copy(alpha = 0.22f) else Color(0xFF3B82F6).copy(alpha = 0.12f)
    val p2Color = if (isDarkMode) Color(0xFFEC4899).copy(alpha = 0.18f) else Color(0xFFEC4899).copy(alpha = 0.08f)
    val p3Color = if (isDarkMode) Color(0xFF10B981).copy(alpha = 0.15f) else Color(0xFF10B981).copy(alpha = 0.08f)

    Canvas(modifier = Modifier.fillMaxSize()) {
        // Draw 3 beautiful ambient dynamic blobs with different radii
        drawCircle(
            color = p1Color,
            radius = 220f,
            center = androidx.compose.ui.geometry.Offset(
                x = size.width * 0.15f + offset1X,
                y = size.height * 0.2f + offset1Y
            )
        )
        drawCircle(
            color = p2Color,
            radius = 280f,
            center = androidx.compose.ui.geometry.Offset(
                x = size.width * 0.85f + offset2X,
                y = size.height * 0.75f + offset2Y
            )
        )
        drawCircle(
            color = p3Color,
            radius = 160f,
            center = androidx.compose.ui.geometry.Offset(
                x = size.width * 0.5f - offset2X,
                y = size.height * 0.45f + offset3Y
            )
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthScreen(
    viewModel: FinanceViewModel,
    modifier: Modifier = Modifier
) {
    val isDarkMode by viewModel.isDarkMode.collectAsState()
    val context = LocalContext.current

    // Infinite transitions for dynamic UI polish
    val infiniteTransition = rememberInfiniteTransition(label = "auth_effects")
    
    // Logo pulsing
    val logoScale by infiniteTransition.animateFloat(
        initialValue = 0.96f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = EasyInOutEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "logo_pulse"
    )

    // Breathing glow intensity
    val glowPulse by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = EasyInOutEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_pulse"
    )

    // Login Form State
    var isEmailMode by remember { mutableStateOf(true) }
    var nameInput by remember { mutableStateOf("") }
    var emailInput by remember { mutableStateOf("") }
    var passwordInput by remember { mutableStateOf("") }
    var phoneInput by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    // OTP Flow State
    var isOtpSent by remember { mutableStateOf(false) }
    var generatedOtp by remember { mutableStateOf("") }
    var otpInput by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }

    val bgColors = if (isDarkMode) {
        listOf(Color(0xFF0F172A), Color(0xFF020617))
    } else {
        listOf(Color(0xFFF8FAFC), Color(0xFFE2E8F0))
    }

    val textColor = if (isDarkMode) Color.White else Color(0xFF0F172A)
    val cardBgColor = if (isDarkMode) Color(0xFF1E293B) else Color.White

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(bgColors))
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        // Floating animated backdrops
        AnimatedFloatingParticles(isDarkMode = isDarkMode)

        // Ambient color glow behind card
        Box(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .height(480.dp)
                .graphicsLayer {
                    alpha = glowPulse * 0.15f
                    scaleX = 1.05f
                    scaleY = 1.05f
                }
                .background(
                    Brush.radialGradient(
                        colors = listOf(Color(0xFF3B82F6), Color(0xFFEC4899), Color.Transparent),
                    ),
                    shape = RoundedCornerShape(36.dp)
                )
        )

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            colors = CardDefaults.cardColors(containerColor = cardBgColor),
            shape = RoundedCornerShape(28.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Animated Logo
                Image(
                    painter = painterResource(id = R.drawable.img_logo),
                    contentDescription = "Expense Tracker Logo",
                    modifier = Modifier
                        .size(140.dp)
                        .graphicsLayer {
                            scaleX = logoScale
                            scaleY = logoScale
                        }
                        .clip(RoundedCornerShape(16.dp))
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Welcome to Expense Tracker",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 22.sp
                    ),
                    color = textColor,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = if (isOtpSent) "Verification Required" else "Secure Login / Registration",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 4.dp, bottom = 20.dp)
                )

                AnimatedContent(
                    targetState = isOtpSent,
                    transitionSpec = {
                        slideInHorizontally { width -> width } + fadeIn() togetherWith
                                slideOutHorizontally { width -> -width } + fadeOut()
                    },
                    label = "AuthFlow"
                ) { otpSent ->
                    if (!otpSent) {
                        // INPUT FORM MODE
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            // Name Input (for registration)
                            OutlinedTextField(
                                value = nameInput,
                                onValueChange = { nameInput = it },
                                label = { Text("Your Name") },
                                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("auth_name_input"),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(0xFF3B82F6),
                                    unfocusedBorderColor = Color.Gray.copy(alpha = 0.5f)
                                )
                            )

                            // Segmented Controls (Email vs Phone Tab)
                            TabRow(
                                selectedTabIndex = if (isEmailMode) 0 else 1,
                                containerColor = if (isDarkMode) Color(0xFF334155) else Color(0xFFF1F5F9),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(42.dp)
                                    .clip(RoundedCornerShape(8.dp)),
                                indicator = @Composable { Box {} },
                                divider = @Composable {}
                            ) {
                                Tab(
                                    selected = isEmailMode,
                                    onClick = { isEmailMode = true },
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isEmailMode) Color(0xFF3B82F6) else Color.Transparent)
                                ) {
                                    Text(
                                        "Email Address",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = if (isEmailMode) Color.White else (if (isDarkMode) Color.LightGray else Color.Gray)
                                    )
                                }
                                Tab(
                                    selected = !isEmailMode,
                                    onClick = { isEmailMode = false },
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (!isEmailMode) Color(0xFF3B82F6) else Color.Transparent)
                                ) {
                                    Text(
                                        "Phone Number",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = if (!isEmailMode) Color.White else (if (isDarkMode) Color.LightGray else Color.Gray)
                                    )
                                }
                            }

                            if (isEmailMode) {
                                // Email Fields
                                OutlinedTextField(
                                    value = emailInput,
                                    onValueChange = { emailInput = it },
                                    label = { Text("Email Address") },
                                    leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("auth_email_input"),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = Color(0xFF3B82F6),
                                        unfocusedBorderColor = Color.Gray.copy(alpha = 0.5f)
                                    )
                                )

                                OutlinedTextField(
                                    value = passwordInput,
                                    onValueChange = { passwordInput = it },
                                    label = { Text("Password") },
                                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                                    trailingIcon = {
                                        val image = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff
                                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                            Icon(image, "Toggle Password")
                                        }
                                    },
                                    singleLine = true,
                                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("auth_password_input"),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = Color(0xFF3B82F6),
                                        unfocusedBorderColor = Color.Gray.copy(alpha = 0.5f)
                                    )
                                )
                            } else {
                                // Phone Fields
                                OutlinedTextField(
                                    value = phoneInput,
                                    onValueChange = { phoneInput = it },
                                    label = { Text("Phone Number") },
                                    leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("auth_phone_input"),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = Color(0xFF3B82F6),
                                        unfocusedBorderColor = Color.Gray.copy(alpha = 0.5f)
                                    )
                                )
                            }

                            if (errorMessage.isNotEmpty()) {
                                Text(
                                    text = errorMessage,
                                    color = Color.Red,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Send OTP Action Button
                            Button(
                                onClick = {
                                    if (nameInput.trim().isEmpty()) {
                                        errorMessage = "Please enter your Name"
                                        return@Button
                                    }
                                    if (isEmailMode) {
                                        if (emailInput.trim().isEmpty() || !emailInput.contains("@")) {
                                            errorMessage = "Please enter a valid Email Address"
                                            return@Button
                                        }
                                        if (passwordInput.trim().length < 6) {
                                            errorMessage = "Password must be at least 6 characters"
                                            return@Button
                                        }
                                        // Simulate sending email OTP
                                        generatedOtp = String.format("%06d", Random.nextInt(100000, 999999))
                                        isOtpSent = true
                                        errorMessage = ""
                                        Toast.makeText(context, "OTP Sent to Email!", Toast.LENGTH_SHORT).show()
                                    } else {
                                        if (phoneInput.trim().length < 8) {
                                            errorMessage = "Please enter a valid Phone Number"
                                            return@Button
                                        }
                                        // Simulate sending SMS OTP
                                        generatedOtp = String.format("%06d", Random.nextInt(100000, 999999))
                                        isOtpSent = true
                                        errorMessage = ""
                                        Toast.makeText(context, "OTP Sent via SMS!", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .testTag("send_otp_button"),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B82F6)),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.Send, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Send OTP & Login", fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }
                    } else {
                        // OTP VERIFICATION MODE
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            // Beautiful simulation info card containing the sent OTP code
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = Color(0xFF10B981).copy(alpha = 0.12f)
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = if (isEmailMode) Icons.Default.Email else Icons.Default.Sms,
                                        contentDescription = null,
                                        tint = Color(0xFF10B981)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = if (isEmailMode) "Simulating Email Delivery" else "Simulating Mobile Message",
                                            fontSize = 11.sp,
                                            color = Color.Gray,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "Your OTP Verification Code is: $generatedOtp",
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 13.sp,
                                            color = Color(0xFF10B981)
                                        )
                                    }
                                }
                            }

                            Text(
                                text = "Enter the 6-digit verification code sent to ${if (isEmailMode) emailInput else phoneInput}",
                                fontSize = 13.sp,
                                color = Color.Gray,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 12.dp)
                            )

                            OutlinedTextField(
                                value = otpInput,
                                onValueChange = { if (it.length <= 6) otpInput = it },
                                label = { Text("Enter 6-Digit OTP") },
                                singleLine = true,
                                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("otp_code_input"),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(0xFF10B981),
                                    unfocusedBorderColor = Color.Gray.copy(alpha = 0.5f)
                                )
                            )

                            if (errorMessage.isNotEmpty()) {
                                Text(
                                    text = errorMessage,
                                    color = Color.Red,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                // Back Button
                                OutlinedButton(
                                    onClick = {
                                        isOtpSent = false
                                        otpInput = ""
                                        errorMessage = ""
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(48.dp),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(Icons.Default.ArrowBack, contentDescription = null)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Back")
                                }

                                // Verify & Login Button
                                Button(
                                    onClick = {
                                        if (otpInput == generatedOtp) {
                                            viewModel.completeLogin(
                                                name = nameInput,
                                                emailOrPhone = if (isEmailMode) emailInput else phoneInput
                                            )
                                            Toast.makeText(context, "Verification Successful!", Toast.LENGTH_LONG).show()
                                        } else {
                                            errorMessage = "Incorrect OTP. Please enter the code displayed above."
                                        }
                                    },
                                    modifier = Modifier
                                        .weight(1.5f)
                                        .height(48.dp)
                                        .testTag("verify_otp_button"),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text("Verify & Login", fontWeight = FontWeight.Bold, color = Color.White)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
