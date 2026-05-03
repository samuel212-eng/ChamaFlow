package com.chamaflow

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
import com.chamaflow.ui.screens.chat.ChatScreen
import com.chamaflow.ui.screens.contributions.ContributionsScreen
import com.chamaflow.ui.screens.dashboard.DashboardScreen
import com.chamaflow.ui.screens.investments.InvestmentsScreen
import com.chamaflow.ui.screens.loans.LoanApplicationScreen
import com.chamaflow.ui.screens.loans.LoansScreen
import com.chamaflow.ui.screens.marketplace.MarketplaceDetailScreen
import com.chamaflow.ui.screens.marketplace.MarketplaceScreen
import com.chamaflow.ui.screens.meetings.MeetingsScreen
import com.chamaflow.ui.screens.members.AddMemberScreen
import com.chamaflow.ui.screens.members.MemberProfileScreen
import com.chamaflow.ui.screens.members.MembersScreen
import com.chamaflow.ui.screens.merrygoround.MerryGoRoundScreen
import com.chamaflow.ui.screens.notifications.NotificationsScreen
import com.chamaflow.ui.screens.penalties.PenaltiesScreen
import com.chamaflow.ui.screens.profile.ProfileScreen
import com.chamaflow.ui.screens.reports.ReportsScreen
import com.chamaflow.ui.screens.welfare.WelfareScreen
import com.chamaflow.ui.theme.*
import com.chamaflow.ui.viewmodel.AuthViewModel
import com.chamaflow.ui.viewmodel.ChamaViewModel
import com.chamaflow.ui.viewmodel.MembersViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
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

// ── Root: auth gate ──────────────────────────────────────────────────────────

@Composable
fun RootApp(prefsRepository: UserPreferencesRepository, activity: android.app.Activity) {
    val authViewModel: AuthViewModel = hiltViewModel()
    val authState by authViewModel.uiState.collectAsState()
    val prefs by prefsRepository.userPreferences.collectAsState(
        initial = com.chamaflow.data.preferences.UserPreferences()
    )

    if (authState.isLoading && !authState.isLoggedIn && authState.verificationId == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = Secondary)
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
        if (chamaState.userChamas.isNotEmpty()) {
            val first = chamaState.userChamas.first()
            chamaViewModel.selectChama(first.id, first.name)
        }
    }

    if (chamaState.isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = Secondary)
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForgotPasswordScreen(onBack: () -> Unit, onSend: (String) -> Unit, isLoading: Boolean, successMessage: String?) {
    var email by remember { mutableStateOf("") }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Reset Password", fontWeight = FontWeight.Bold, color = Color.White) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.White) } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Primary)
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text("Enter your email and we'll send a reset link.", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
            successMessage?.let {
                Surface(shape = androidx.compose.foundation.shape.RoundedCornerShape(10.dp), color = Color(0xFFDCFCE7)) {
                    Row(modifier = Modifier.fillMaxWidth().padding(12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.CheckCircle, null, tint = Accent)
                        Text(it, style = MaterialTheme.typography.bodySmall, color = Color(0xFF057A55))
                    }
                }
            }
            OutlinedTextField(
                value = email, onValueChange = { email = it.trim() },
                label = { Text("Email Address") },
                leadingIcon = { Icon(Icons.Filled.Email, null, tint = TextSecondary) },
                modifier = Modifier.fillMaxWidth(),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Secondary, unfocusedBorderColor = ChamaOutline)
            )
            Button(
                onClick = { if (email.contains("@")) onSend(email) },
                enabled = email.contains("@") && !isLoading,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Primary)
            ) {
                if (isLoading) CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                else Text("Send Reset Link", fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

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
    
    val pagerState = rememberPagerState(pageCount = { bottomNavItems.size })
    val scope = rememberCoroutineScope()
    
    val isTopLevel = currentDestination?.route == "main_tabs" || currentDestination == null

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            if (isTopLevel) {
                NavigationBar(containerColor = Surface) {
                    bottomNavItems.forEachIndexed { index, item ->
                        val isSelected = pagerState.currentPage == index
                        NavigationBarItem(
                            icon = { Icon(if (isSelected) item.selectedIcon else item.unselectedIcon, item.label) },
                            label = { Text(item.label, style = MaterialTheme.typography.labelSmall, fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal) },
                            selected = isSelected,
                            onClick = {
                                scope.launch { pagerState.animateScrollToPage(index) }
                            },
                            colors = NavigationBarItemDefaults.colors(selectedIconColor = Secondary, selectedTextColor = Secondary, unselectedIconColor = TextSecondary, unselectedTextColor = TextSecondary, indicatorColor = Secondary.copy(alpha = 0.1f))
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(navController = navController, startDestination = "main_tabs") {

            composable("main_tabs") {
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxSize().padding(innerPadding),
                    beyondViewportPageCount = 1
                ) { page ->
                    when (bottomNavItems[page].screen) {
                        Screen.Dashboard -> DashboardScreen(
                            chamaId = chamaId,
                            chamaName = chamaName,
                            userId = userId,
                            userRole = userRole,
                            adminName = userName,
                            onNavigateToMembers = { scope.launch { pagerState.animateScrollToPage(1) } },
                            onNavigateToContributions = { scope.launch { pagerState.animateScrollToPage(2) } },
                            onNavigateToLoans = { scope.launch { pagerState.animateScrollToPage(3) } },
                            onNavigateToMeetings = { scope.launch { pagerState.animateScrollToPage(4) } },
                            onNavigateToReports = { navController.navigate(Screen.Reports.route) },
                            onNavigateToNotifications = { navController.navigate(Screen.Notifications.route) },
                            onNavigateToProfile = { navController.navigate(Screen.Profile.route) },
                            onNavigateToInvestments = { navController.navigate(Screen.Investments.route) },
                            onNavigateToMerryGoRound = { navController.navigate(Screen.MerryGoRound.route) },
                            onNavigateToWelfare = { navController.navigate(Screen.Welfare.route) },
                            onNavigateToChat = { navController.navigate(Screen.Chat.route) },
                            onNavigateToMarketplace = { navController.navigate(Screen.Marketplace.route) }
                        )
                        Screen.Members -> MembersScreen(
                            chamaId = chamaId,
                            onAddMember = { navController.navigate(Screen.AddMember.route) },
                            onMemberClick = { navController.navigate(Screen.MemberDetail.createRoute(it)) }
                        )
                        Screen.Contributions -> ContributionsScreen(chamaId = chamaId)
                        Screen.Loans -> LoansScreen(
                            chamaId = chamaId,
                            onApplyLoan = { navController.navigate(Screen.AddLoan.route) },
                            onLoanClick = { navController.navigate(Screen.LoanDetail.createRoute(it)) }
                        )
                        Screen.Meetings -> MeetingsScreen(chamaId = chamaId)
                        else -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("Screen ${bottomNavItems[page].label}") }
                    }
                }
            }

            composable(Screen.AddMember.route) {
                AddMemberScreen(
                    chamaId = chamaId,
                    onBack = { navController.popBackStack() }, 
                    onSave = { member -> 
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
            composable(Screen.Investments.route) {
                InvestmentsScreen(
                    chamaId = chamaId,
                    userRole = userRole,
                    onBack = { navController.popBackStack() }
                )
            }
            composable(Screen.MerryGoRound.route) {
                MerryGoRoundScreen(
                    chamaId = chamaId,
                    userRole = userRole,
                    onBack = { navController.popBackStack() }
                )
            }
            composable(Screen.Welfare.route) {
                WelfareScreen(
                    chamaId = chamaId,
                    userRole = userRole,
                    onBack = { navController.popBackStack() }
                )
            }
            composable(Screen.Chat.route) {
                ChatScreen(
                    chamaId = chamaId,
                    userId = userId,
                    userName = userName,
                    onBack = { navController.popBackStack() }
                )
            }
            composable(Screen.Marketplace.route) {
                MarketplaceScreen(
                    chamaId = chamaId,
                    userId = userId,
                    userName = userName,
                    onBack = { navController.popBackStack() },
                    onListingClick = { navController.navigate(Screen.MarketplaceDetail.createRoute(it)) }
                )
            }
            composable(Screen.MarketplaceDetail.route) { back ->
                MarketplaceDetailScreen(
                    listingId = back.arguments?.getString("listingId") ?: "",
                    onBack = { navController.popBackStack() },
                    onContactSeller = { sellerId, sellerName ->
                        // In a real app, this would open a direct chat
                        navController.navigate(Screen.Chat.route)
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PlaceholderScreen(title: String, onBack: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title, fontWeight = FontWeight.Bold, color = Color.White) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.White) } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Primary)
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
            Text("$title — coming soon", color = TextSecondary, textAlign = TextAlign.Center)
        }
    }
}
