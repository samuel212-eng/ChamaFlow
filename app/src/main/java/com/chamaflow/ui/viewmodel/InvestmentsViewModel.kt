package com.chamaflow.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chamaflow.data.models.*
import com.chamaflow.data.repository.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class InvestmentsUiState(
    val investments: List<Investment> = emptyList(),
    val dividendDistributions: List<DividendDistribution> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val latestDistribution: DividendDistribution? = null
)

@HiltViewModel
class InvestmentsViewModel @Inject constructor(
    private val repo: InvestmentsRepository,
    private val membersRepo: MembersRepository
) : ViewModel() {
    private val _ui = MutableStateFlow(InvestmentsUiState())
    val uiState = _ui.asStateFlow()

    fun loadInvestments(chamaId: String) {
        viewModelScope.launch {
            _ui.update { it.copy(isLoading = true) }
            repo.getInvestmentsFlow(chamaId)
                .catch { e -> _ui.update { it.copy(isLoading = false, errorMessage = e.message) } }
                .collect { list -> _ui.update { it.copy(investments = list, isLoading = false) } }
        }
    }

    fun addInvestment(chamaId: String, investment: Investment) {
        viewModelScope.launch {
            _ui.update { it.copy(isLoading = true) }
            when (val r = repo.addInvestment(chamaId, investment)) {
                is AuthResult.Success -> _ui.update { it.copy(isLoading = false, successMessage = "Investment recorded") }
                is AuthResult.Error -> _ui.update { it.copy(isLoading = false, errorMessage = r.message) }
                else -> {}
            }
        }
    }

    fun updateInvestment(chamaId: String, investment: Investment) {
        viewModelScope.launch {
            repo.updateInvestment(chamaId, investment)
        }
    }

    fun deleteInvestment(chamaId: String, investmentId: String) {
        viewModelScope.launch {
            repo.deleteInvestment(chamaId, investmentId)
        }
    }

    fun distributeDividends(chamaId: String, investmentId: String, investmentTitle: String, totalAmount: Double) {
        viewModelScope.launch {
            _ui.update { it.copy(isLoading = true) }
            val members = membersRepo.getMembersOnce(chamaId)
            if (members.isEmpty()) {
                _ui.update { it.copy(isLoading = false, errorMessage = "No members found to distribute to.") }
                return@launch
            }

            val totalGroupContributions = members.sumOf { it.totalContributions }
            if (totalGroupContributions == 0.0) {
                _ui.update { it.copy(isLoading = false, errorMessage = "No contributions found to calculate shares.") }
                return@launch
            }

            val payouts = members.map { m ->
                val percentage = (m.totalContributions / totalGroupContributions)
                MemberPayout(
                    memberId = m.id,
                    memberName = m.fullName,
                    contributionPercentage = percentage * 100,
                    amount = totalAmount * percentage
                )
            }

            val distribution = DividendDistribution(
                chamaId = chamaId,
                investmentId = investmentId,
                investmentTitle = investmentTitle,
                totalAmount = totalAmount,
                dateDistributed = LocalDate.now().toString(),
                memberPayouts = payouts
            )

            when (val r = repo.distributeDividends(chamaId, distribution)) {
                is AuthResult.Success -> _ui.update { it.copy(
                    isLoading = false, 
                    successMessage = "Dividends distributed successfully",
                    latestDistribution = distribution
                ) }
                is AuthResult.Error -> _ui.update { it.copy(isLoading = false, errorMessage = r.message) }
                else -> {}
            }
        }
    }

    fun clearMessages() { 
        _ui.update { it.copy(errorMessage = null, successMessage = null, latestDistribution = null) } 
    }
}
