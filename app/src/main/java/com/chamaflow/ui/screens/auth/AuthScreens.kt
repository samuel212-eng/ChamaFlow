package com.chamaflow.ui.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chamaflow.ui.theme.*
import kotlinx.coroutines.delay

// ─── Gradient background ──────────────────────────────────────────────────────
private val authGradient @Composable get() = Brush.verticalGradient(listOf(ChamaBlue, ChamaBlueDark, Color(0xFF0C1A4E)))

@Composable
fun LoginScreen(
    onLoginSuccess: (String, String) -> Unit,
    onNavigateToRegister: () -> Unit = {},
    onNavigateToForgotPassword: () -> Unit = {},
    onContinueWithPhone: (String) -> Unit = {},
    isLoading: Boolean = false,
    errorMessage: String? = null
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    val formValid = email.isNotEmpty() && password.length >= 6

    Box(modifier = Modifier.fillMaxSize().background(authGradient)) {
        Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()), horizontalAlignment = Alignment.CenterHorizontally) {
            Spacer(Modifier.height(64.dp))
            AuthLogo()
            Spacer(Modifier.height(12.dp))
            Text("ChamaFlow", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = Color.White)
            Text("Smart savings for your chama", style = MaterialTheme.typography.bodyMedium, color = Color.White.copy(alpha = 0.75f))
            Spacer(Modifier.height(48.dp))
            Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp), shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = ChamaSurface), elevation = CardDefaults.cardElevation(8.dp)) {
                Column(modifier = Modifier.fillMaxWidth().padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text("Sign In", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    errorMessage?.let { AuthErrorBanner(it) }
                    AuthField(email, { email = it.trim() }, "Email Address", Icons.Filled.Email, keyboardType = KeyboardType.Email)
                    OutlinedTextField(value = password, onValueChange = { password = it }, label = { Text("Password") }, leadingIcon = { Icon(Icons.Filled.Lock, null, tint = ChamaTextSecondary) }, trailingIcon = { IconButton(onClick = { passwordVisible = !passwordVisible }) { Icon(if (passwordVisible) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility, null, tint = ChamaTextSecondary) } }, visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password), modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), singleLine = true, colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = ChamaBlue, unfocusedBorderColor = ChamaOutline))
                    Text("Forgot password?", style = MaterialTheme.typography.labelMedium, color = ChamaBlue, fontWeight = FontWeight.SemiBold, modifier = Modifier.align(Alignment.End).clickable { onNavigateToForgotPassword() })
                    AuthButton("Sign In", isLoading, formValid) { onLoginSuccess(email, password) }
                    AuthDivider()
                    // If phone is entered in the email field or just a shortcut to register with phone
                    OutlinedButton(onClick = onNavigateToRegister, modifier = Modifier.fillMaxWidth().height(52.dp), shape = RoundedCornerShape(12.dp)) {
                        Icon(Icons.Filled.Phone, null, tint = ChamaTextSecondary, modifier = Modifier.size(18.dp)); Spacer(Modifier.width(8.dp)); Text("Register with Email & Phone", color = ChamaTextSecondary)
                    }
                }
            }
            Spacer(Modifier.height(24.dp))
            AuthFooterLink("Don't have an account?", "Sign Up", onNavigateToRegister)
            Spacer(Modifier.height(48.dp))
        }
    }
}

@Composable
fun RegisterScreen(
    onRegisterSuccess: (String, String, String, String) -> Unit,
    onNavigateToLogin: () -> Unit = {},
    isLoading: Boolean = false,
    errorMessage: String? = null
) {
    var fullName by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirm by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    val phoneError = phone.isNotEmpty() && !phone.matches(Regex("^(\\+254|07|01)\\d{8,9}$"))
    val emailError = email.isNotEmpty() && !email.contains("@")
    val mismatch = confirm.isNotEmpty() && password != confirm
    val formValid = fullName.length >= 3 && phone.isNotEmpty() && !phoneError && email.isNotEmpty() && !emailError && password.length >= 6 && !mismatch

    Box(modifier = Modifier.fillMaxSize().background(authGradient)) {
        Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()), horizontalAlignment = Alignment.CenterHorizontally) {
            Spacer(Modifier.height(48.dp))
            AuthLogo(size = 64.dp)
            Spacer(Modifier.height(12.dp))
            Text("Create Account", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = Color.White)
            Spacer(Modifier.height(32.dp))
            Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp), shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = ChamaSurface), elevation = CardDefaults.cardElevation(8.dp)) {
                Column(modifier = Modifier.fillMaxWidth().padding(24.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Text("Personal Details", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    errorMessage?.let { AuthErrorBanner(it) }
                    AuthField(fullName, { fullName = it }, "Full Name", Icons.Filled.Person)
                    AuthField(phone, { phone = it }, "Phone Number", Icons.Filled.Phone, keyboardType = KeyboardType.Phone, isError = phoneError, errorMsg = "Valid Kenyan number required (07xx or +254)")
                    AuthField(email, { email = it.trim() }, "Email Address", Icons.Filled.Email, keyboardType = KeyboardType.Email, isError = emailError, errorMsg = "Enter a valid email")
                    HorizontalDivider(color = ChamaOutline)
                    OutlinedTextField(value = password, onValueChange = { password = it }, label = { Text("Password") }, leadingIcon = { Icon(Icons.Filled.Lock, null, tint = ChamaTextSecondary) }, trailingIcon = { IconButton(onClick = { passwordVisible = !passwordVisible }) { Icon(if (passwordVisible) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility, null, tint = ChamaTextSecondary) } }, visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password), isError = password.isNotEmpty() && password.length < 6, supportingText = if (password.isNotEmpty() && password.length < 6) { { Text("Min 6 characters", color = ChamaRed) } } else null, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), singleLine = true, colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = ChamaBlue, unfocusedBorderColor = ChamaOutline))
                    OutlinedTextField(value = confirm, onValueChange = { confirm = it }, label = { Text("Confirm Password") }, leadingIcon = { Icon(Icons.Filled.LockOpen, null, tint = ChamaTextSecondary) }, visualTransformation = PasswordVisualTransformation(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password), isError = mismatch, supportingText = if (mismatch) { { Text("Passwords do not match", color = ChamaRed) } } else null, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), singleLine = true, colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = ChamaBlue, unfocusedBorderColor = ChamaOutline))
                    AuthButton("Create Account", isLoading, formValid) { onRegisterSuccess(fullName, phone, email, password) }
                }
            }
            Spacer(Modifier.height(24.dp))
            AuthFooterLink("Already have an account?", "Sign In", onNavigateToLogin)
            Spacer(Modifier.height(48.dp))
        }
    }
}

@Composable
fun OtpVerificationScreen(phoneNumber: String = "", onVerified: (String) -> Unit = {}, onBack: () -> Unit = {}, isLoading: Boolean = false, isError: Boolean = false) {
    var otp by remember { mutableStateOf("") }
    var timer by remember { mutableIntStateOf(60) }
    var canResend by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) { focusRequester.requestFocus(); while (timer > 0) { delay(1000); timer-- }; canResend = true }

    Box(modifier = Modifier.fillMaxSize().background(authGradient)) {
        Column(modifier = Modifier.fillMaxSize().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Spacer(Modifier.height(56.dp))
            Row(modifier = Modifier.fillMaxWidth()) { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.White) } }
            Spacer(Modifier.height(24.dp))
            AuthLogo(); Spacer(Modifier.height(16.dp))
            Text("Verify Your Number", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = Color.White)
            Text("6-digit code sent to $phoneNumber", style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.8f))
            Spacer(Modifier.height(48.dp))
            Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = ChamaSurface), elevation = CardDefaults.cardElevation(8.dp)) {
                Column(modifier = Modifier.fillMaxWidth().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(24.dp)) {
                    Text("Enter OTP Code", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    BasicTextField(value = otp, onValueChange = { if (it.length <= 6) otp = it }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword), modifier = Modifier.focusRequester(focusRequester).size(0.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        repeat(6) { i ->
                            val char = otp.getOrNull(i)
                            Box(modifier = Modifier.size(48.dp).background(when { isError -> ChamaRedLight; i == otp.length -> ChamaBlueLight; char != null -> ChamaGreenLight; else -> ChamaBackground }, RoundedCornerShape(10.dp)).border(2.dp, when { isError -> ChamaRed; i == otp.length -> ChamaBlue; char != null -> ChamaGreen; else -> ChamaOutline }, RoundedCornerShape(10.dp)), contentAlignment = Alignment.Center) {
                                Text(char?.toString() ?: "", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = if (isError) ChamaRed else ChamaTextPrimary)
                            }
                        }
                    }
                    if (isError) Text("Invalid code. Try again.", style = MaterialTheme.typography.bodySmall, color = ChamaRed)
                    AuthButton("Verify", isLoading, otp.length == 6) { onVerified(otp) }
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text("Didn't receive code?", style = MaterialTheme.typography.bodySmall, color = ChamaTextSecondary)
                        if (canResend) TextButton(onClick = { otp = ""; timer = 60; canResend = false }) { Text("Resend OTP", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = ChamaBlue) }
                        else Text("Resend in ${timer}s", style = MaterialTheme.typography.bodySmall, color = ChamaTextMuted)
                    }
                }
            }
        }
    }
}

// ─── Shared auth helpers ──────────────────────────────────────────────────────

@Composable
private fun AuthLogo(size: androidx.compose.ui.unit.Dp = 80.dp) {
    Box(modifier = Modifier.size(size).background(Color.White.copy(alpha = 0.15f), RoundedCornerShape(size * 0.3f)), contentAlignment = Alignment.Center) {
        Icon(Icons.Filled.AccountBalance, null, tint = Color.White, modifier = Modifier.size(size * 0.55f))
    }
}

@Composable
private fun AuthErrorBanner(message: String) {
    Surface(shape = RoundedCornerShape(10.dp), color = ChamaRedLight) {
        Row(modifier = Modifier.fillMaxWidth().padding(12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.ErrorOutline, null, tint = ChamaRed, modifier = Modifier.size(18.dp))
            Text(message, style = MaterialTheme.typography.bodySmall, color = ChamaRed)
        }
    }
}

@Composable
private fun AuthField(value: String, onValueChange: (String) -> Unit, label: String, icon: ImageVector, keyboardType: KeyboardType = KeyboardType.Text, isError: Boolean = false, errorMsg: String = "") {
    OutlinedTextField(value = value, onValueChange = onValueChange, label = { Text(label) }, leadingIcon = { Icon(icon, null, tint = ChamaTextSecondary) }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text), isError = isError, supportingText = if (isError && errorMsg.isNotEmpty()) { { Text(errorMsg, color = ChamaRed) } } else null, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), singleLine = true, colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = ChamaBlue, unfocusedBorderColor = ChamaOutline))
}

@Composable
private fun AuthButton(label: String, isLoading: Boolean, enabled: Boolean, onClick: () -> Unit) {
    Button(onClick = onClick, enabled = enabled && !isLoading, modifier = Modifier.fillMaxWidth().height(52.dp), shape = RoundedCornerShape(12.dp), colors = ButtonDefaults.buttonColors(containerColor = ChamaBlue, disabledContainerColor = ChamaOutline)) {
        if (isLoading) CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
        else Text(label, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
    }
}

@Composable
private fun AuthDivider() {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        HorizontalDivider(modifier = Modifier.weight(1f), color = ChamaOutline)
        Text("or", style = MaterialTheme.typography.labelSmall, color = ChamaTextSecondary)
        HorizontalDivider(modifier = Modifier.weight(1f), color = ChamaOutline)
    }
}

@Composable
private fun AuthFooterLink(prefix: String, linkText: String, onClick: () -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(prefix, style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.8f))
        Text(linkText, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.clickable { onClick() })
    }
}
