package com.chamaflow.ui.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chamaflow.data.models.MarketplaceCategory
import com.chamaflow.data.models.MarketplaceListing
import com.chamaflow.data.repository.AuthResult
import com.chamaflow.data.repository.MarketplaceRepository
import com.chamaflow.data.repository.StorageRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class MarketplaceUiState(
    val listings: List<MarketplaceListing> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val selectedCategory: MarketplaceCategory? = null,
    val isUploading: Boolean = false
)

@HiltViewModel
class MarketplaceViewModel @Inject constructor(
    private val repo: MarketplaceRepository,
    private val storageRepo: StorageRepository
) : ViewModel() {
    private val _ui = MutableStateFlow(MarketplaceUiState())
    val uiState = _ui.asStateFlow()

    fun loadListings(chamaId: String) {
        viewModelScope.launch {
            _ui.update { it.copy(isLoading = true) }
            repo.getListingsFlow(chamaId)
                .catch { e -> _ui.update { it.copy(isLoading = false, errorMessage = e.message) } }
                .collect { list -> _ui.update { it.copy(listings = list, isLoading = false) } }
        }
    }

    fun setCategory(category: MarketplaceCategory?) {
        _ui.update { it.copy(selectedCategory = category) }
    }

    fun createListing(chamaId: String, listing: MarketplaceListing, imageUris: List<Uri>) {
        viewModelScope.launch {
            _ui.update { it.copy(isUploading = true) }
            
            val uploadedUrls = mutableListOf<String>()
            var uploadError: String? = null

            for (uri in imageUris) {
                val path = "marketplace/${chamaId}/${UUID.randomUUID()}"
                when (val uploadResult = storageRepo.uploadImage(path, uri)) {
                    is AuthResult.Success -> {
                        uploadedUrls.add(uploadResult.data)
                    }
                    is AuthResult.Error -> {
                        uploadError = uploadResult.message
                        break
                    }
                    else -> {}
                }
            }

            if (uploadError != null) {
                _ui.update { it.copy(isUploading = false, errorMessage = uploadError) }
                return@launch
            }

            val finalListing = listing.copy(imageUrls = uploadedUrls)

            when (val r = repo.createListing(chamaId, finalListing)) {
                is AuthResult.Success -> _ui.update { it.copy(isUploading = false, successMessage = "Listing posted successfully!") }
                is AuthResult.Error -> _ui.update { it.copy(isUploading = false, errorMessage = r.message) }
                else -> {}
            }
        }
    }

    fun markAsSold(chamaId: String, listingId: String) {
        viewModelScope.launch {
            repo.markAsSold(chamaId, listingId)
        }
    }

    fun deleteListing(chamaId: String, listingId: String) {
        viewModelScope.launch {
            repo.deleteListing(chamaId, listingId)
        }
    }

    fun clearMessages() {
        _ui.update { it.copy(errorMessage = null, successMessage = null) }
    }
}
