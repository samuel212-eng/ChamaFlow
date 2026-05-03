package com.chamaflow.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chamaflow.data.models.*
import com.chamaflow.data.repository.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ReportsUiState(
    val totalSavings: Double = 0.0,
    val loansIssued: Double = 0.0,
    val loansRepaid: Double = 0.0,
    val penaltiesCollected: Double = 0.0,
    val groupBalance: Double = 0.0,
    val monthlySavings: Map<String, Double> = emptyMap(),
    val monthlyRepayments: Map<String, Double> = emptyMap(),
    val topContributors: List<Pair<String, Double>> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class ReportsViewModel @Inject constructor(
    private val membersRepo: MembersRepository,
    private val loansRepo: LoansRepository,
    private val penaltiesRepo: PenaltiesRepository,
    private val contribRepo: ContributionsRepository
) : ViewModel() {
    private val _ui = MutableStateFlow(ReportsUiState())
    val uiState = _ui.asStateFlow()

    fun loadReports(chamaId: String) {
        viewModelScope.launch {
            _ui.update { it.copy(isLoading = true) }
            combine(
                membersRepo.getMembersFlow(chamaId),
                loansRepo.getLoansFlow(chamaId),
                penaltiesRepo.getPenaltiesFlow(chamaId)
            ) { members, loans, penalties ->
                val totalSavings = members.sumOf { it.totalContributions }
                val loansIssued = loans.sumOf { it.amount }
                val loansRepaid = loans.sumOf { it.amountPaid }
                val penaltiesCollected = penalties.filter { it.status == PenaltyStatus.PAID }.sumOf { it.amount }
                val activeLoansBalance = loans.filter { it.status == LoanStatus.ACTIVE || it.status == LoanStatus.OVERDUE }.sumOf { it.remainingBalance }
                
                // Simplified monthly data (assuming we have date info)
                // In a real app, we'd group contributions and repayments by month
                
                ReportsUiState(
                    totalSavings = totalSavings,
                    loansIssued = loansIssued,
                    loansRepaid = loansRepaid,
                    penaltiesCollected = penaltiesCollected,
                    groupBalance = totalSavings - activeLoansBalance, // Simplified
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
