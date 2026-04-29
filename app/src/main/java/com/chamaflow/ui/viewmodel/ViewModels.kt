package com.chamaflow.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chamaflow.data.models.*
import com.chamaflow.data.repository.*
import com.google.firebase.auth.FirebaseUser
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

// ─── Auth ─────────────────────────────────────────────────────────────────────

data class AuthUiState(
    val isLoading: Boolean = true, 
    val isLoggedIn: Boolean = false, 
    val user: FirebaseUser? = null, 
    val errorMessage: String? = null, 
    val successMessage: String? = null,
    val verificationId: String? = null,
    val phoneNumber: String? = null
)

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val repo: AuthRepository,
    private val prefs: com.chamaflow.data.preferences.UserPreferencesRepository
) : ViewModel() {
    private val _ui = MutableStateFlow(AuthUiState()); val uiState = _ui.asStateFlow()
    
    init { 
        viewModelScope.launch { 
            repo.currentUser.collect { user -> 
                _ui.update { it.copy(user = user, isLoggedIn = user != null, isLoading = false) }
                if (user != null) {
                    val profile = repo.getUserProfile(user.uid)
                    if (profile != null) {
                        prefs.saveUserInfo(
                            userId = user.uid,
                            name = profile["fullName"] as? String ?: "",
                            role = profile["role"] as? String ?: "MEMBER"
                        )
                    }
                }
            } 
        } 
    }

    fun login(email: String, password: String) { viewModelScope.launch { _ui.update { it.copy(isLoading = true, errorMessage = null) }; when (val r = repo.loginWithEmail(email, password)) { is AuthResult.Success -> _ui.update { it.copy(isLoading = false, user = r.data, isLoggedIn = true) }; is AuthResult.Error -> _ui.update { it.copy(isLoading = false, errorMessage = r.message) }; else -> {} } } }
    fun register(email: String, password: String, name: String, phone: String) { viewModelScope.launch { _ui.update { it.copy(isLoading = true, errorMessage = null) }; when (val r = repo.registerWithEmail(email, password, name, phone)) { is AuthResult.Success -> _ui.update { it.copy(isLoading = false, user = r.data, isLoggedIn = true) }; is AuthResult.Error -> _ui.update { it.copy(isLoading = false, errorMessage = r.message) }; else -> {} } } }
    
    fun sendOtp(phone: String, activity: android.app.Activity) {
        viewModelScope.launch {
            _ui.update { it.copy(isLoading = true, errorMessage = null, phoneNumber = phone) }
            repo.sendPhoneOtp(phone, activity, object : com.google.firebase.auth.PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                override fun onVerificationCompleted(credential: com.google.firebase.auth.PhoneAuthCredential) {
                    viewModelScope.launch {
                        val r = repo.signInWithCredential(credential)
                        if (r is AuthResult.Success) _ui.update { it.copy(isLoading = false, isLoggedIn = true) }
                        else if (r is AuthResult.Error) _ui.update { it.copy(isLoading = false, errorMessage = r.message) }
                    }
                }
                override fun onVerificationFailed(e: com.google.firebase.FirebaseException) {
                    _ui.update { it.copy(isLoading = false, errorMessage = e.message) }
                }
                override fun onCodeSent(verificationId: String, token: com.google.firebase.auth.PhoneAuthProvider.ForceResendingToken) {
                    _ui.update { it.copy(isLoading = false, successMessage = "OTP sent!", verificationId = verificationId) }
                }
            })
        }
    }

    fun verifyOtp(otpCode: String) {
        val vid = _ui.value.verificationId ?: return
        viewModelScope.launch {
            _ui.update { it.copy(isLoading = true, errorMessage = null) }
            when (val r = repo.verifyOtp(vid, otpCode)) {
                is AuthResult.Success -> _ui.update { it.copy(isLoading = false, isLoggedIn = true) }
                is AuthResult.Error -> _ui.update { it.copy(isLoading = false, errorMessage = r.message) }
                else -> {}
            }
        }
    }

    fun sendPasswordReset(email: String) { viewModelScope.launch { _ui.update { it.copy(isLoading = true) }; when (val r = repo.sendPasswordResetEmail(email)) { is AuthResult.Success -> _ui.update { it.copy(isLoading = false, successMessage = "Reset link sent to $email") }; is AuthResult.Error -> _ui.update { it.copy(isLoading = false, errorMessage = r.message) }; else -> {} } } }
    fun logout() { viewModelScope.launch { prefs.clearAll(); repo.logout(); _ui.update { AuthUiState(isLoading = false) } } }
    fun clearError() { _ui.update { it.copy(errorMessage = null) } }
    fun clearMessages() { _ui.update { it.copy(errorMessage = null, successMessage = null) } }
}

// ─── Chama ────────────────────────────────────────────────────────────────────

data class ChamaUiState(
    val isLoading: Boolean = false, 
    val successMessage: String? = null, 
    val errorMessage: String? = null, 
    val userChamas: List<Chama> = emptyList(), 
    val searchResults: List<Chama> = emptyList(), 
    val isSearching: Boolean = false
)

@HiltViewModel
class ChamaViewModel @Inject constructor(
    private val repo: ChamaRepository,
    private val prefs: com.chamaflow.data.preferences.UserPreferencesRepository
) : ViewModel() {
    private val _ui = MutableStateFlow(ChamaUiState(isLoading = true)); val uiState = _ui.asStateFlow()

    init {
        viewModelScope.launch {
            repo.getUserChamasFlow().collect { chamas ->
                _ui.update { it.copy(userChamas = chamas, isLoading = false) }
            }
        }
    }

    fun createChama(chama: Chama) {
        viewModelScope.launch {
            _ui.update { it.copy(isLoading = true, errorMessage = null) }
            when (val r = repo.createChama(chama)) {
                is AuthResult.Success -> {
                    prefs.saveActiveChamaId(r.data, chama.name)
                    _ui.update { it.copy(isLoading = false, successMessage = "Chama created!") }
                }
                is AuthResult.Error -> _ui.update { it.copy(isLoading = false, errorMessage = r.message) }
                else -> {}
            }
        }
    }

    fun searchChamas(query: String) {
        if (query.length < 2) {
            _ui.update { it.copy(searchResults = emptyList()) }
            return
        }
        viewModelScope.launch {
            _ui.update { it.copy(isSearching = true) }
            when (val r = repo.searchChamas(query)) {
                is AuthResult.Success -> _ui.update { it.copy(isSearching = false, searchResults = r.data) }
                is AuthResult.Error -> _ui.update { it.copy(isSearching = false, errorMessage = r.message) }
                else -> {}
            }
        }
    }

    fun requestToJoin(chamaId: String) {
        viewModelScope.launch {
            _ui.update { it.copy(isLoading = true, errorMessage = null) }
            when (val r = repo.requestToJoin(chamaId)) {
                is AuthResult.Success -> _ui.update { it.copy(isLoading = false, successMessage = "Join request sent!") }
                is AuthResult.Error -> _ui.update { it.copy(isLoading = false, errorMessage = r.message) }
                else -> {}
            }
        }
    }

    fun joinChama(inviteCode: String) {
        viewModelScope.launch {
            _ui.update { it.copy(isLoading = true, errorMessage = null) }
            when (val r = repo.joinChamaWithCode(inviteCode)) {
                is AuthResult.Success -> {
                    val chamaResult = repo.getChamaById(r.data)
                    if (chamaResult is AuthResult.Success) {
                        prefs.saveActiveChamaId(r.data, chamaResult.data.name)
                        _ui.update { it.copy(isLoading = false, successMessage = "Joined ${chamaResult.data.name}!") }
                    } else {
                        _ui.update { it.copy(isLoading = false, errorMessage = "Joined but failed to load details.") }
                    }
                }
                is AuthResult.Error -> _ui.update { it.copy(isLoading = false, errorMessage = r.message) }
                else -> {}
            }
        }
    }

    fun selectChama(chamaId: String, chamaName: String) {
        viewModelScope.launch {
            prefs.saveActiveChamaId(chamaId, chamaName)
        }
    }

    fun skipChamaSelection() {
        viewModelScope.launch {
            prefs.saveActiveChamaId("PERSONAL", "My Personal Savings")
        }
    }

    fun clearMessages() { _ui.update { it.copy(errorMessage = null, successMessage = null) } }
}

// ─── Dashboard ────────────────────────────────────────────────────────────────

data class DashboardUiState(val stats: DashboardStats = DashboardStats(), val chamaName: String = "", val inviteCode: String = "", val isLoading: Boolean = false, val errorMessage: String? = null)

@HiltViewModel
class DashboardViewModel @Inject constructor(private val chamaRepo: ChamaRepository, private val membersRepo: MembersRepository, private val contribRepo: ContributionsRepository, private val loansRepo: LoansRepository, private val penaltiesRepo: PenaltiesRepository) : ViewModel() {
    private val _ui = MutableStateFlow(DashboardUiState()); val uiState = _ui.asStateFlow()
    fun loadDashboard(chamaId: String, chamaName: String, userId: String, userRole: String) {
        _ui.update { it.copy(chamaName = chamaName, isLoading = true) }
        viewModelScope.launch {
            if (chamaId != "PERSONAL") {
                val chamaInfo = chamaRepo.getChamaById(chamaId)
                if (chamaInfo is AuthResult.Success) {
                    _ui.update { it.copy(inviteCode = chamaInfo.data.inviteCode) }
                }
            }

            combine(
                membersRepo.getMembersFlow(chamaId),
                contribRepo.getContributionsForMonth(chamaId, currentMonth()),
                loansRepo.getLoansFlow(chamaId),
                penaltiesRepo.getPenaltiesFlow(chamaId)
            ) { members, contributions, loans, penalties ->
                if (userRole == "ADMIN" || userRole == "TREASURER") {
                    val activeLoans = loans.filter { it.status == LoanStatus.ACTIVE || it.status == LoanStatus.OVERDUE }
                    DashboardStats(
                        totalMembers = members.count { it.status == MemberStatus.ACTIVE },
                        totalContributions = members.sumOf { it.totalContributions },
                        totalLoansIssued = loans.sumOf { it.amount },
                        totalLoanRepayments = loans.sumOf { it.amountPaid },
                        totalPenaltiesCollected = penalties.filter { it.status == PenaltyStatus.PAID }.sumOf { it.amount },
                        currentGroupBalance = members.sumOf { it.totalContributions } - activeLoans.sumOf { it.remainingBalance },
                        activeLoans = activeLoans.size,
                        overdueLoans = loans.count { it.status == LoanStatus.OVERDUE },
                        recentContributions = contributions.take(5)
                    )
                } else {
                    // Member personal stats
                    val myMember = members.find { it.userId == userId || it.id == userId }
                    val myLoans = loans.filter { it.memberId == userId }
                    val myActiveLoans = myLoans.filter { it.status == LoanStatus.ACTIVE || it.status == LoanStatus.OVERDUE }
                    val myContribs = contributions.filter { it.memberId == userId }
                    
                    DashboardStats(
                        totalMembers = if (chamaId == "PERSONAL") 1 else members.count { it.status == MemberStatus.ACTIVE },
                        totalContributions = myMember?.totalContributions ?: 0.0,
                        totalLoansIssued = myLoans.sumOf { it.amount },
                        totalLoanRepayments = myLoans.sumOf { it.amountPaid },
                        totalPenaltiesCollected = myMember?.penaltiesOwed ?: 0.0,
                        currentGroupBalance = myMember?.totalContributions ?: 0.0,
                        activeLoans = myActiveLoans.size,
                        overdueLoans = myLoans.count { it.status == LoanStatus.OVERDUE },
                        recentContributions = myContribs
                    )
                }
            }.catch { e -> _ui.update { it.copy(isLoading = false, errorMessage = e.message) } }
             .collect { stats -> _ui.update { it.copy(stats = stats, isLoading = false) } }
        }
    }
}

// ─── Members ──────────────────────────────────────────────────────────────────

data class MembersUiState(val members: List<Member> = emptyList(), val isLoading: Boolean = false, val errorMessage: String? = null, val successMessage: String? = null, val searchQuery: String = "", val selectedFilter: String = "All", val appUsers: List<Map<String, Any>> = emptyList()) {
    val filteredMembers get() = members.filter { m ->
        val s = m.fullName.contains(searchQuery, true) || m.phoneNumber.contains(searchQuery, true)
        val f = when (selectedFilter) { "Admin" -> m.role == MemberRole.ADMIN; "Treasurer" -> m.role == MemberRole.TREASURER; "Member" -> m.role == MemberRole.MEMBER && m.status == MemberStatus.ACTIVE; "Inactive" -> m.status == MemberStatus.INACTIVE; else -> true }
        s && f
    }
    val activeCount get() = members.count { it.status == MemberStatus.ACTIVE }
    val withLoans get() = members.count { it.loanBalance > 0 }
    val withPenalties get() = members.count { it.penaltiesOwed > 0 }
}

@HiltViewModel
class MembersViewModel @Inject constructor(private val repo: MembersRepository, private val authRepo: AuthRepository) : ViewModel() {
    private val _ui = MutableStateFlow(MembersUiState()); val uiState = _ui.asStateFlow()
    fun loadMembers(chamaId: String) { viewModelScope.launch { _ui.update { it.copy(isLoading = true) }; repo.getMembersFlow(chamaId).catch { e -> _ui.update { it.copy(isLoading = false, errorMessage = e.message) } }.collect { members -> _ui.update { it.copy(members = members, isLoading = false) } } } }
    fun setSearchQuery(q: String) { _ui.update { it.copy(searchQuery = q) } }
    fun setFilter(f: String) { _ui.update { it.copy(selectedFilter = f) } }
    fun searchAppUsers(query: String) {
        if (query.length < 3) return
        viewModelScope.launch {
            // This is a mock search for now, ideally you'd search the 'users' collection
            // but for safety/privacy reasons we might just search by specific email or phone
        }
    }
    fun addMember(chamaId: String, member: Member) { viewModelScope.launch { _ui.update { it.copy(isLoading = true) }; when (val r = repo.addMember(chamaId, member)) { is AuthResult.Success -> _ui.update { it.copy(isLoading = false, successMessage = "Member added") }; is AuthResult.Error -> _ui.update { it.copy(isLoading = false, errorMessage = r.message) }; else -> {} } } }
    fun deleteMember(chamaId: String, memberId: String) { viewModelScope.launch { when (val r = repo.deleteMember(chamaId, memberId)) { is AuthResult.Success -> _ui.update { it.copy(successMessage = "Member removed") }; is AuthResult.Error -> _ui.update { it.copy(errorMessage = r.message) }; else -> {} } } }
    fun clearMessages() { _ui.update { it.copy(errorMessage = null, successMessage = null) } }
}

// ─── Contributions ────────────────────────────────────────────────────────────

fun currentMonth(): String = LocalDate.now().format(DateTimeFormatter.ofPattern("MMM yyyy"))

data class ContributionsUiState(val contributions: List<Contribution> = emptyList(), val isLoading: Boolean = false, val errorMessage: String? = null, val successMessage: String? = null, val selectedMonth: String = currentMonth(), val selectedTabIndex: Int = 0) {
    val filteredContributions get() = contributions.filter { c -> when (selectedTabIndex) { 1 -> c.status == ContributionStatus.PAID; 2 -> c.status == ContributionStatus.UNPAID; 3 -> c.status == ContributionStatus.OVERDUE; else -> true } }
    val paidCount get() = contributions.count { it.status == ContributionStatus.PAID }
    val totalCount get() = contributions.size
    val amountCollected get() = contributions.filter { it.status == ContributionStatus.PAID || it.status == ContributionStatus.PARTIAL }.sumOf { it.amount }
    val partialCount get() = contributions.count { it.status == ContributionStatus.PARTIAL }
    val overdueCount get() = contributions.count { it.status == ContributionStatus.OVERDUE }
}

@HiltViewModel
class ContributionsViewModel @Inject constructor(private val repo: ContributionsRepository) : ViewModel() {
    private val _ui = MutableStateFlow(ContributionsUiState()); val uiState = _ui.asStateFlow()
    fun loadContributions(chamaId: String, month: String = currentMonth()) { viewModelScope.launch { _ui.update { it.copy(isLoading = true, selectedMonth = month) }; repo.getContributionsForMonth(chamaId, month).catch { e -> _ui.update { it.copy(isLoading = false, errorMessage = e.message) } }.collect { list -> _ui.update { it.copy(contributions = list, isLoading = false) } } } }
    fun changeMonth(chamaId: String, month: String) { loadContributions(chamaId, month) }
    fun setTab(i: Int) { _ui.update { it.copy(selectedTabIndex = i) } }
    fun recordContribution(chamaId: String, contribution: Contribution) { viewModelScope.launch { _ui.update { it.copy(isLoading = true) }; when (val r = repo.recordContribution(chamaId, contribution)) { is AuthResult.Success -> _ui.update { it.copy(isLoading = false, successMessage = "Payment recorded for ${contribution.memberName}") }; is AuthResult.Error -> _ui.update { it.copy(isLoading = false, errorMessage = r.message) }; else -> {} } } }
    fun clearMessages() { _ui.update { it.copy(errorMessage = null, successMessage = null) } }
}

// ─── Loans ────────────────────────────────────────────────────────────────────

data class LoansUiState(val loans: List<Loan> = emptyList(), val isLoading: Boolean = false, val errorMessage: String? = null, val successMessage: String? = null, val selectedTabIndex: Int = 0) {
    val activeLoans  get() = loans.filter { it.status == LoanStatus.ACTIVE || it.status == LoanStatus.OVERDUE }
    val pendingLoans get() = loans.filter { it.status == LoanStatus.PENDING }
    val repaidLoans  get() = loans.filter { it.status == LoanStatus.REPAID }
    val displayedLoans get() = when (selectedTabIndex) { 0 -> activeLoans; 1 -> pendingLoans; 2 -> repaidLoans; else -> emptyList() }
    val totalIssued    get() = activeLoans.sumOf { it.amount }
    val totalRepaid    get() = activeLoans.sumOf { it.amountPaid }
    val totalRemaining get() = activeLoans.sumOf { it.remainingBalance }
    val overdueCount   get() = loans.count { it.status == LoanStatus.OVERDUE }
}

@HiltViewModel
class LoansViewModel @Inject constructor(private val repo: LoansRepository) : ViewModel() {
    private val _ui = MutableStateFlow(LoansUiState()); val uiState = _ui.asStateFlow()
    fun loadLoans(chamaId: String) { viewModelScope.launch { _ui.update { it.copy(isLoading = true) }; repo.getLoansFlow(chamaId).catch { e -> _ui.update { it.copy(isLoading = false, errorMessage = e.message) } }.collect { loans -> _ui.update { it.copy(loans = loans, isLoading = false) } } } }
    fun setTab(i: Int) { _ui.update { it.copy(selectedTabIndex = i) } }
    fun approveLoan(chamaId: String, loanId: String) { viewModelScope.launch { when (val r = repo.approveLoan(chamaId, loanId)) { is AuthResult.Success -> _ui.update { it.copy(successMessage = "Loan approved") }; is AuthResult.Error -> _ui.update { it.copy(errorMessage = r.message) }; else -> {} } } }
    fun rejectLoan(chamaId: String, loanId: String) { viewModelScope.launch { when (val r = repo.rejectLoan(chamaId, loanId)) { is AuthResult.Success -> _ui.update { it.copy(successMessage = "Loan rejected") }; is AuthResult.Error -> _ui.update { it.copy(errorMessage = r.message) }; else -> {} } } }
    fun applyForLoan(chamaId: String, loan: Loan) { viewModelScope.launch { when (val r = repo.applyForLoan(chamaId, loan)) { is AuthResult.Success -> _ui.update { it.copy(successMessage = "Application submitted") }; is AuthResult.Error -> _ui.update { it.copy(errorMessage = r.message) }; else -> {} } } }
    fun recordRepayment(chamaId: String, loanId: String, amount: Double) {
        viewModelScope.launch {
            _ui.update { it.copy(isLoading = true) }
            when (val r = repo.recordRepayment(chamaId, loanId, amount)) {
                is AuthResult.Success -> _ui.update { it.copy(isLoading = false, successMessage = "Repayment recorded!") }
                is AuthResult.Error -> _ui.update { it.copy(isLoading = false, errorMessage = r.message) }
                else -> {}
            }
        }
    }
    fun clearMessages() { _ui.update { it.copy(errorMessage = null, successMessage = null) } }
}

// ─── Meetings ─────────────────────────────────────────────────────────────────

data class MeetingsUiState(val meetings: List<Meeting> = emptyList(), val isLoading: Boolean = false, val errorMessage: String? = null, val successMessage: String? = null, val selectedTabIndex: Int = 0, val showScheduleSheet: Boolean = false) {
    val upcomingMeetings get() = meetings.filter { it.status == MeetingStatus.UPCOMING }
    val pastMeetings     get() = meetings.filter { it.status != MeetingStatus.UPCOMING }
    val nextMeeting      get() = upcomingMeetings.firstOrNull()
    val displayedMeetings get() = if (selectedTabIndex == 0) upcomingMeetings else pastMeetings
}

@HiltViewModel
class MeetingsViewModel @Inject constructor(private val repo: MeetingsRepository) : ViewModel() {
    private val _ui = MutableStateFlow(MeetingsUiState()); val uiState = _ui.asStateFlow()
    fun loadMeetings(chamaId: String) { viewModelScope.launch { _ui.update { it.copy(isLoading = true) }; repo.getMeetingsFlow(chamaId).catch { e -> _ui.update { it.copy(isLoading = false, errorMessage = e.message) } }.collect { meetings -> _ui.update { it.copy(meetings = meetings, isLoading = false) } } } }
    fun scheduleMeeting(chamaId: String, meeting: Meeting) { viewModelScope.launch { _ui.update { it.copy(isLoading = true) }; when (val r = repo.scheduleMeeting(chamaId, meeting)) { is AuthResult.Success -> _ui.update { it.copy(isLoading = false, successMessage = "Meeting scheduled: ${meeting.title}") }; is AuthResult.Error -> _ui.update { it.copy(isLoading = false, errorMessage = r.message) }; else -> {} } } }
    fun setTab(i: Int) { _ui.update { it.copy(selectedTabIndex = i) } }
    fun showSheet() { _ui.update { it.copy(showScheduleSheet = true) } }
    fun hideSheet() { _ui.update { it.copy(showScheduleSheet = false) } }
    fun clearMessages() { _ui.update { it.copy(errorMessage = null, successMessage = null) } }
}
