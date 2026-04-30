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

data class WelfareUiState(
    val fund: WelfareFund? = null,
    val contributions: List<WelfareContribution> = emptyList(),
    val disbursements: List<WelfareDisbursement> = emptyList(),
    val members: List<Member> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null
)

@HiltViewModel
class WelfareViewModel @Inject constructor(
    private val repo: WelfareRepository,
    private val membersRepo: MembersRepository
) : ViewModel() {
    private val _ui = MutableStateFlow(WelfareUiState()); val uiState = _ui.asStateFlow()

    fun loadWelfareData(chamaId: String) {
        viewModelScope.launch {
            _ui.update { it.copy(isLoading = true) }
            combine(
                repo.getWelfareFundFlow(chamaId),
                repo.getWelfareContributionsFlow(chamaId),
                repo.getWelfareDisbursementsFlow(chamaId),
                membersRepo.getMembersFlow(chamaId)
            ) { fund, contribs, disbs, members ->
                _ui.update { it.copy(
                    fund = fund,
                    contributions = contribs,
                    disbursements = disbs,
                    members = members,
                    isLoading = false
                ) }
            }.catch { e -> _ui.update { it.copy(isLoading = false, errorMessage = e.message) } }.collect()
        }
    }

    fun recordContribution(chamaId: String, contribution: WelfareContribution) {
        viewModelScope.launch {
            _ui.update { it.copy(isLoading = true) }
            when (val r = repo.recordWelfareContribution(chamaId, contribution)) {
                is AuthResult.Success -> _ui.update { it.copy(isLoading = false, successMessage = "Contribution recorded") }
                is AuthResult.Error -> _ui.update { it.copy(isLoading = false, errorMessage = r.message) }
                else -> {}
            }
        }
    }

    fun recordDisbursement(chamaId: String, disbursement: WelfareDisbursement) {
        viewModelScope.launch {
            _ui.update { it.copy(isLoading = true) }
            when (val r = repo.recordWelfareDisbursement(chamaId, disbursement)) {
                is AuthResult.Success -> _ui.update { it.copy(isLoading = false, successMessage = "Disbursement recorded") }
                is AuthResult.Error -> _ui.update { it.copy(isLoading = false, errorMessage = r.message) }
                else -> {}
            }
        }
    }

    fun updateRules(chamaId: String, amount: Double, rules: String) {
        viewModelScope.launch {
            _ui.update { it.copy(isLoading = true) }
            when (val r = repo.updateWelfareRules(chamaId, amount, rules)) {
                is AuthResult.Success -> _ui.update { it.copy(isLoading = false, successMessage = "Rules updated") }
                is AuthResult.Error -> _ui.update { it.copy(isLoading = false, errorMessage = r.message) }
                else -> {}
            }
        }
    }

    fun clearMessages() { _ui.update { it.copy(errorMessage = null, successMessage = null) } }
}
