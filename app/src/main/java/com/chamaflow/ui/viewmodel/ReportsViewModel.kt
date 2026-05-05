package com.chamaflow.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chamaflow.data.models.*
import com.chamaflow.data.repository.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*
import javax.inject.Inject

data class ReportsUiState(
    val totalSavings: Double = 0.0,
    val loansIssued: Double = 0.0,
    val loansRepaid: Double = 0.0,
    val penaltiesCollected: Double = 0.0,
    val welfareBalance: Double = 0.0,
    val welfareDisbursed: Double = 0.0,
    val groupBalance: Double = 0.0,
    val monthlySavingsLabels: List<String> = emptyList(),
    val monthlySavingsValues: List<Double> = emptyList(),
    val topContributors: List<Pair<String, Double>> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class ReportsViewModel @Inject constructor(
    private val membersRepo: MembersRepository,
    private val loansRepo: LoansRepository,
    private val penaltiesRepo: PenaltiesRepository,
    private val contribRepo: ContributionsRepository,
    private val welfareRepo: WelfareRepository
) : ViewModel() {
    private val _ui = MutableStateFlow(ReportsUiState())
    val uiState = _ui.asStateFlow()

    fun loadReports(chamaId: String) {
        viewModelScope.launch {
            _ui.update { it.copy(isLoading = true) }
            
            val membersFlow = membersRepo.getMembersFlow(chamaId)
            val loansFlow = loansRepo.getLoansFlow(chamaId)
            val penaltiesFlow = penaltiesRepo.getPenaltiesFlow(chamaId)
            val contribsFlow = contribRepo.getAllContributionsFlow(chamaId)
            val welfareFundFlow = welfareRepo.getWelfareFundFlow(chamaId)
            val disbursementsFlow = welfareRepo.getWelfareDisbursementsFlow(chamaId)

            combine(
                membersFlow,
                loansFlow,
                penaltiesFlow,
                contribsFlow,
                welfareFundFlow,
                disbursementsFlow
            ) { args: Array<Any?> ->
                val members = args[0] as List<Member>
                val loans = args[1] as List<Loan>
                val penalties = args[2] as List<Penalty>
                val contributions = args[3] as List<Contribution>
                val welfareFund = args[4] as WelfareFund?
                val disbursements = args[5] as List<WelfareDisbursement>

                val totalSavings = members.sumOf { it.totalContributions }
                val loansIssued = loans.sumOf { it.amount }
                val loansRepaid = loans.sumOf { it.amountPaid }
                val penaltiesCollected = penalties.filter { it.status == PenaltyStatus.PAID }.sumOf { it.amount }
                val activeLoansBalance = loans.filter { it.status == LoanStatus.ACTIVE || it.status == LoanStatus.OVERDUE }.sumOf { it.remainingBalance }
                val welfareDisbursed = disbursements.sumOf { it.amount }
                
                val current = LocalDate.now()
                val last6Months = (0..5).map { current.minusMonths(it.toLong()).format(DateTimeFormatter.ofPattern("MMM yyyy")) }.reversed()
                
                val savingsByMonth = contributions
                    .filter { it.status == ContributionStatus.PAID || it.status == ContributionStatus.PARTIAL }
                    .groupBy { it.month }
                
                val savingsValues = last6Months.map { month ->
                    savingsByMonth[month]?.sumOf { it.amount } ?: 0.0
                }

                ReportsUiState(
                    totalSavings = totalSavings,
                    loansIssued = loansIssued,
                    loansRepaid = loansRepaid,
                    penaltiesCollected = penaltiesCollected,
                    welfareBalance = welfareFund?.totalBalance ?: 0.0,
                    welfareDisbursed = welfareDisbursed,
                    groupBalance = totalSavings - activeLoansBalance,
                    monthlySavingsLabels = last6Months.map { it.split(" ")[0] },
                    monthlySavingsValues = savingsValues,
                    topContributors = members.sortedByDescending { it.totalContributions }
                        .take(5)
                        .map { it.fullName to it.totalContributions },
                    isLoading = false
                )
            }.catch { e -> 
                _ui.update { it.copy(isLoading = false, errorMessage = e.message) } 
            }.collect { state ->
                _ui.update { state }
            }
        }
    }
}
