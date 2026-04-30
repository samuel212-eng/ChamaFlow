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

data class MerryGoRoundUiState(
    val merryGoRounds: List<MerryGoRound> = emptyList(),
    val payouts: List<MerryGoRoundPayout> = emptyList(),
    val members: List<Member> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null
)

@HiltViewModel
class MerryGoRoundViewModel @Inject constructor(
    private val repo: MerryGoRoundRepository,
    private val membersRepo: MembersRepository
) : ViewModel() {
    private val _ui = MutableStateFlow(MerryGoRoundUiState()); val uiState = _ui.asStateFlow()

    fun loadMerryGoRoundData(chamaId: String) {
        viewModelScope.launch {
            _ui.update { it.copy(isLoading = true) }
            repo.getMerryGoRoundsFlow(chamaId).flatMapLatest { mgrs ->
                if (mgrs.isEmpty()) {
                    membersRepo.getMembersFlow(chamaId).map { members ->
                        _ui.update { it.copy(merryGoRounds = emptyList(), members = members, payouts = emptyList(), isLoading = false) }
                    }
                } else {
                    val mgr = mgrs.first()
                    combine(
                        repo.getPayoutsFlow(chamaId, mgr.id),
                        membersRepo.getMembersFlow(chamaId)
                    ) { payouts, members ->
                        _ui.update { it.copy(
                            merryGoRounds = mgrs,
                            members = members,
                            payouts = payouts,
                            isLoading = false
                        ) }
                    }
                }
            }.catch { e -> _ui.update { it.copy(isLoading = false, errorMessage = e.message) } }.collect()
        }
    }

    fun createMerryGoRound(chamaId: String, amount: Double, order: List<String>) {
        viewModelScope.launch {
            _ui.update { it.copy(isLoading = true) }
            val mgr = MerryGoRound(
                chamaId = chamaId,
                amountPerMember = amount,
                totalPool = amount * order.size,
                startDate = LocalDate.now().toString(),
                rotationOrder = order,
                status = MerryGoRoundStatus.ACTIVE
            )
            when (val r = repo.createMerryGoRound(chamaId, mgr)) {
                is AuthResult.Success -> _ui.update { it.copy(isLoading = false, successMessage = "Merry Go Round started!") }
                is AuthResult.Error -> _ui.update { it.copy(isLoading = false, errorMessage = r.message) }
                else -> {}
            }
        }
    }

    fun recordPayout(chamaId: String, mgrId: String, memberId: String, memberName: String, amount: Double) {
        viewModelScope.launch {
            _ui.update { it.copy(isLoading = true) }
            val payout = MerryGoRoundPayout(
                merryGoRoundId = mgrId,
                memberId = memberId,
                memberName = memberName,
                amount = amount,
                datePaid = LocalDate.now().toString(),
                month = LocalDate.now().month.name
            )
            when (val r = repo.recordPayout(chamaId, payout)) {
                is AuthResult.Success -> _ui.update { it.copy(isLoading = false, successMessage = "Payout recorded for $memberName") }
                is AuthResult.Error -> _ui.update { it.copy(isLoading = false, errorMessage = r.message) }
                else -> {}
            }
        }
    }

    fun clearMessages() { _ui.update { it.copy(errorMessage = null, successMessage = null) } }
}
