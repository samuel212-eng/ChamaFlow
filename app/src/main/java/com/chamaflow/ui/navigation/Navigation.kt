package com.chamaflow.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String) {
    object Dashboard     : Screen("dashboard")
    object Members       : Screen("members")
    object Contributions : Screen("contributions")
    object Loans         : Screen("loans")
    object Meetings      : Screen("meetings")
    object Reports       : Screen("reports")
    object Penalties     : Screen("penalties")
    object Notifications : Screen("notifications")
    object Settings      : Screen("settings")
    object Profile       : Screen("profile")

    object MemberDetail : Screen("member_detail/{memberId}") {
        fun createRoute(id: String) = "member_detail/$id"
    }
    object AddMember : Screen("add_member")
    object LoanDetail : Screen("loan_detail/{loanId}") {
        fun createRoute(id: String) = "loan_detail/$id"
    }
    object AddLoan : Screen("add_loan")
}

data class BottomNavItem(val screen: Screen, val label: String, val selectedIcon: ImageVector, val unselectedIcon: ImageVector)

val bottomNavItems = listOf(
    BottomNavItem(Screen.Dashboard,     "Home",     Icons.Filled.Home,           Icons.Outlined.Home),
    BottomNavItem(Screen.Members,       "Members",  Icons.Filled.Group,          Icons.Outlined.Group),
    BottomNavItem(Screen.Contributions, "Savings",  Icons.Filled.Savings,        Icons.Outlined.Savings),
    BottomNavItem(Screen.Loans,         "Loans",    Icons.Filled.AccountBalance,  Icons.Outlined.AccountBalance),
    BottomNavItem(Screen.Meetings,      "Meetings", Icons.Filled.EventNote,       Icons.Outlined.EventNote),
)
