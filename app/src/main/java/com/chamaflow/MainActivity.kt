package com.chamaflow

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.chamaflow.data.preferences.UserPreferencesRepository
import com.chamaflow.ui.navigation.Screen
import com.chamaflow.ui.navigation.bottomNavItems
import com.chamaflow.ui.screens.auth.LoginScreen
import com.chamaflow.ui.screens.auth.OtpVerificationScreen
import com.chamaflow.ui.screens.auth.RegisterScreen
import com.chamaflow.ui.screens.chama.ChamaSelectionScreen
import com.chamaflow.ui.screens.chama.CreateChamaScreen
import com.chamaflow.ui.screens.contributions.ContributionsScreen
import com.chamaflow.ui.screens.dashboard.DashboardScreen
import com.chamaflow.ui.screens.loans.LoanApplicationScreen
import com.chamaflow.ui.screens.loans.LoansScreen
import com.chamaflow.ui.screens.meetings.MeetingsScreen
import com.chamaflow.ui.screens.members.AddMemberScreen
import com.chamaflow.ui.screens.members.MemberProfileScreen
import com.chamaflow.ui.screens.members.MembersScreen
import com.chamaflow.ui.screens.notifications.NotificationsScreen
import com.chamaflow.ui.screens.penalties.PenaltiesScreen
import com.chamaflow.ui.screens.profile.ProfileScreen
import com.chamaflow.ui.screens.reports.ReportsScreen
import com.chamaflow.ui.theme.*
import com.chamaflow.ui.viewmodel.AuthViewModel
import com.chamaflow.ui.viewmodel.ChamaViewModel
import com.chamaflow.ui.viewmodel.MembersViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var prefsRepository: UserPreferencesRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ChamaFlowTheme {
                RootApp(prefsRepository, this)
            }
        }
    }
}

// ─── Root: auth gate ──────────────────────────────────────────────────────────

@Composable
fun RootApp(prefsRepository: UserPreferencesRepository, activity: android.app.Activity) {
    val authViewModel: AuthViewModel = hiltViewModel()
    val authState by authViewModel.uiState.collectAsState()
    val prefs by prefsRepository.userPreferences.collectAsState(
        initial = com.chamaflow.data.preferences.UserPreferences()
    )

    if (authState.isLoading && !authState.isLoggedIn && authState.verificationId == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = ChamaBlue)
        }
        return
    }

    when {
        !authState.isLoggedIn -> AuthFlow(authViewModel, activity)
        prefs.activeChamaId.isEmpty() -> {
            ChamaOnboardingFlow(authViewModel)
        }
        else -> MainApp(
            chamaId   = prefs.activeChamaId,
            chamaName = prefs.activeChamaName,
            userId    = prefs.userId,
            userName  = prefs.userName,
            userRole  = prefs.userRole,
            onLogout  = {
                authViewModel.logout()
            }
        )
    }
}

@Composable
fun ChamaOnboardingFlow(authViewModel: AuthViewModel) {
    val chamaViewModel: ChamaViewModel = hiltViewModel()
    val chamaState by chamaViewModel.uiState.collectAsState()
    var showCreateScreen by remember { mutableStateOf(false) }

    LaunchedEffect(chamaState.userChamas) {
        // Automatically enter the first chama if the user is already a member
        if (chamaState.userChamas.isNotEmpty()) {
            val first = chamaState.userChamas.first()
            chamaViewModel.selectChama(first.id, first.name)
        }
    }

    if (chamaState.isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = ChamaBlue)
        }
    } else if (showCreateScreen) {
        CreateChamaScreen(
            onBack = { showCreateScreen = false },
            onSave = { chama -> chamaViewModel.createChama(chama) },
            isLoading = chamaState.isLoading,
            errorMessage = chamaState.errorMessage
        )
    } else {
        ChamaSelectionScreen(
            onCreateNew = { showCreateScreen = true },
            onJoinExisting = { code -> chamaViewModel.joinChama(code) },
            onLogout = { authViewModel.logout() },
            onSkip = { chamaViewModel.skipChamaSelection() },
            viewModel = chamaViewModel
        )
    }
}

// ─── Auth flow ────────────────────────────────────────────────────────────────

@Composable
fun AuthFlow(authViewModel: AuthViewModel, activity: android.app.Activity) {
    val authState by authViewModel.uiState.collectAsState()
    val navController = rememberNavController()

    LaunchedEffect(authState.verificationId) {
        if (authState.verificationId != null) {
            navController.navigate("otp")
        }
    }

    NavHost(navController = navController, startDestination = "login") {
        composable("login") {
            LoginScreen(
                onLoginSuccess = { email, password -> authViewModel.login(email, password) },
                onNavigateToRegister = { navController.navigate("register") },
                onNavigateToForgotPassword = { navController.navigate("forgot") },
                onContinueWithPhone = { phone: String -> authViewModel.sendOtp(phone, activity) },
                isLoading = authState.isLoading,
                errorMessage = authState.errorMessage
            )
        }
        composable("register") {
            RegisterScreen(
                onRegisterSuccess = { name, phone, email, password -> 
                    authViewModel.register(email, password, name, phone) 
                },
                onNavigateToLogin = { navController.popBackStack() },
                isLoading = authState.isLoading,
                errorMessage = authState.errorMessage
            )
        }
        composable("otp") {
            OtpVerificationScreen(
                phoneNumber = authState.phoneNumber ?: "",
                onVerified = { otp: String -> authViewModel.verifyOtp(otp) },
                onBack = { navController.popBackStack() },
                isLoading = authState.isLoading,
                isError = authState.errorMessage != null
            )
        }
        composable("forgot") {
            ForgotPasswordScreen(
                onBack = { navController.popBackStack() },
                onSend = { authViewModel.sendPasswordReset(it) },
                isLoading = authState.isLoading,
                successMessage = authState.successMessage
            )
        }
    }
}

// ─── Forgot password ──────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForgotPasswordScreen(onBack: () -> Unit, onSend: (String) -> Unit, isLoading: Boolean, successMessage: String?) {
    var email by remember { mutableStateOf("") }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Reset Password", fontWeight = FontWeight.Bold, color = Color.White) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.White) } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = ChamaBlue)
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text("Enter your email and we'll send a reset link.", style = MaterialTheme.typography.bodyMedium, color = ChamaTextSecondary)
            successMessage?.let {
                Surface(shape = androidx.compose.foundation.shape.RoundedCornerShape(10.dp), color = ChamaGreenLight) {
                    Row(modifier = Modifier.fillMaxWidth().padding(12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                        Icon(Icons.Filled.CheckCircle, null, tint = ChamaGreen)
                        Text(it, style = MaterialTheme.typography.bodySmall, color = ChamaGreenDark)
                    }
                }
            }
            OutlinedTextField(
                value = email, onValueChange = { email = it.trim() },
                label = { Text("Email Address") },
                leadingIcon = { Icon(Icons.Filled.Email, null, tint = ChamaTextSecondary) },
                modifier = Modifier.fillMaxWidth(),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = ChamaBlue, unfocusedBorderColor = ChamaOutline)
            )
            Button(
                onClick = { if (email.contains("@")) onSend(email) },
                enabled = email.contains("@") && !isLoading,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
            ) {
                if (isLoading) CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                else Text("Send Reset Link", fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

// ─── Main app (post-login) ────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainApp(
    chamaId: String,
    chamaName: String,
    userId: String,
    userName: String,
    userRole: String,
    onLogout: () -> Unit
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val bottomNavRoutes = bottomNavItems.map { it.screen.route }
    val showBottomNav = currentDestination?.route in bottomNavRoutes

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            if (showBottomNav) {
                NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                    bottomNavItems.forEach { item ->
                        val isSelected = currentDestination?.hierarchy?.any { it.route == item.screen.route } == true
                        NavigationBarItem(
                            icon = { Icon(if (isSelected) item.selectedIcon else item.unselectedIcon, item.label) },
                            label = { Text(item.label, style = MaterialTheme.typography.labelSmall, fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal) },
                            selected = isSelected,
                            onClick = {
                                navController.navigate(item.screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true; restoreState = true
                                }
                            },
                            colors = NavigationBarItemDefaults.colors(selectedIconColor = ChamaBlue, selectedTextColor = ChamaBlue, unselectedIconColor = ChamaTextSecondary, unselectedTextColor = ChamaTextSecondary, indicatorColor = ChamaBlueLight)
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(navController = navController, startDestination = Screen.Dashboard.route, modifier = Modifier.padding(innerPadding)) {

            composable(Screen.Dashboard.route) {
                DashboardScreen(
                    chamaId = chamaId,
                    chamaName = chamaName,
                    userId = userId,
                    userRole = userRole,
                    adminName = userName,
                    onNavigateToMembers = { navController.navigate(Screen.Members.route) },
                    onNavigateToContributions = { navController.navigate(Screen.Contributions.route) },
                    onNavigateToLoans = { navController.navigate(Screen.Loans.route) },
                    onNavigateToMeetings = { navController.navigate(Screen.Meetings.route) },
                    onNavigateToReports = { navController.navigate(Screen.Reports.route) },
                    onNavigateToNotifications = { navController.navigate(Screen.Notifications.route) },
                    onNavigateToProfile = { navController.navigate(Screen.Profile.route) }
                )
            }
            composable(Screen.Members.route) {
                MembersScreen(
                    chamaId = chamaId,
                    onAddMember = { navController.navigate(Screen.AddMember.route) },
                    onMemberClick = { navController.navigate(Screen.MemberDetail.createRoute(it)) }
                )
            }
            composable(Screen.Contributions.route) {
                ContributionsScreen(chamaId = chamaId)
            }
            composable(Screen.Loans.route) {
                LoansScreen(
                    chamaId = chamaId,
                    onApplyLoan = { navController.navigate(Screen.AddLoan.route) },
                    onLoanClick = { navController.navigate(Screen.LoanDetail.createRoute(it)) }
                )
            }
            composable(Screen.Meetings.route) {
                MeetingsScreen(chamaId = chamaId)
            }

            // ── Detail screens ────────────────────────────────────────────────
            composable(Screen.AddMember.route) {
                val membersViewModel: MembersViewModel = hiltViewModel()
                AddMemberScreen(
                    chamaId = chamaId,
                    onBack = { navController.popBackStack() }, 
                    onSave = { member -> 
                        membersViewModel.addMember(chamaId, member)
                        navController.popBackStack()
                    }
                )
            }
            composable(Screen.MemberDetail.route) { back ->
                MemberProfileScreen(
                    chamaId = chamaId,
                    memberId = back.arguments?.getString("memberId") ?: "", 
                    onBack = { navController.popBackStack() }
                )
            }
            composable(Screen.AddLoan.route) {
                LoanApplicationScreen(
                    chamaId = chamaId,
                    onBack = { navController.popBackStack() },
                    onSubmit = { navController.popBackStack() }
                )
            }
            composable(Screen.LoanDetail.route) {
                PlaceholderScreen("Loan Details") { navController.popBackStack() }
            }
            composable(Screen.Reports.route) {
                ReportsScreen(onBack = { navController.popBackStack() })
            }
            composable(Screen.Penalties.route) {
                PenaltiesScreen(onBack = { navController.popBackStack() })
            }
            composable(Screen.Notifications.route) {
                NotificationsScreen(onBack = { navController.popBackStack() })
            }
            composable(Screen.Profile.route) {
                ProfileScreen(
                    onBack = { navController.popBackStack() },
                    onLogout = onLogout,
                    userName = userName,
                    userRole = userRole,
                    chamName = chamaName
                )
            }
            composable(Screen.Settings.route) {
                ProfileScreen(
                    onBack = { navController.popBackStack() },
                    onLogout = onLogout,
                    userName = userName,
                    userRole = userRole,
                    chamName = chamaName
                )
            }
        }
    }
}

// ─── Placeholder (only LoanDetail left) ──────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PlaceholderScreen(title: String, onBack: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title, fontWeight = FontWeight.Bold, color = Color.White) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.White) } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = ChamaBlue)
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
            Text("$title — coming soon", color = ChamaTextSecondary)
        }
    }
}
