package com.example.dsgmap.ui

import android.location.Location
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dsgmap.data.model.StoreUiModel
import com.example.dsgmap.data.repository.StoreRepository
import com.example.dsgmap.util.LocationProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StoreSearchViewModel @Inject constructor(
    private val storeRepository: StoreRepository,
    private val locationProvider: LocationProvider
) : ViewModel(), DefaultLifecycleObserver {

    sealed class StoreSearchUiState {
        data object Initial : StoreSearchUiState()
        data object Loading : StoreSearchUiState()
        data class Success(val stores: List<StoreUiModel>) : StoreSearchUiState()
        data class Empty(val message: String = "No stores found") : StoreSearchUiState()
        data class Error(val message: String) : StoreSearchUiState()
    }

    private val _uiState = MutableStateFlow<StoreSearchUiState>(StoreSearchUiState.Initial)
    val uiState: StateFlow<StoreSearchUiState> = _uiState.asStateFlow()

    override fun onPause(owner: LifecycleOwner) {
        clearStoreList()
        super.onPause(owner)
    }

    private fun clearStoreList() {
        _uiState.value = StoreSearchUiState.Initial
    }

    fun searchStoresByZipCode(zipCode: String) {
        if (zipCode.isBlank()) return
        
        _uiState.value = StoreSearchUiState.Loading
        
        viewModelScope.launch {
            try {
                storeRepository.searchStoresByZipCode(zipCode)
                    .catch { e ->
                        _uiState.value = StoreSearchUiState.Error(
                            "Failed to load stores: ${e.message ?: "Unknown error"}"
                        )
                    }
                    .collectLatest { result ->
                        result.fold(
                            onSuccess = { stores ->
                                if (stores.isEmpty()) {
                                    _uiState.value = StoreSearchUiState.Empty()
                                } else {
                                    _uiState.value = StoreSearchUiState.Success(stores)
                                }
                            },
                            onFailure = { e ->
                                _uiState.value = StoreSearchUiState.Error(
                                    "Failed to load stores: ${e.message ?: "Unknown error"}"
                                )
                            }
                        )
                    }
            } catch (e: Exception) {
                _uiState.value = StoreSearchUiState.Error(
                    "Failed to load stores: ${e.message ?: "Unknown error"}"
                )
            }
        }
    }

    fun searchStoresByCurrentLocation() {
        _uiState.value = StoreSearchUiState.Loading
        
        viewModelScope.launch {
            val location = locationProvider.getCurrentLocation()
            if (location == null) {
                _uiState.value = StoreSearchUiState.Error(
                    "Could not determine your location. Please try again or search by ZIP code."
                )
                return@launch
            }
            
            searchStoresByLocation(location)
        }
    }
    
    private suspend fun searchStoresByLocation(location: Location) {
        storeRepository.searchStoresByLocation(location.latitude, location.longitude)
            .catch { e ->
                _uiState.value = StoreSearchUiState.Error(
                    "Failed to load stores: ${e.localizedMessage}"
                )
            }
            .collectLatest { result ->
                result.fold(
                    onSuccess = { stores ->
                        if (stores.isEmpty()) {
                            _uiState.value = StoreSearchUiState.Empty()
                        } else {
                            _uiState.value = StoreSearchUiState.Success(stores)
                        }
                    },
                    onFailure = { e ->
                        _uiState.value = StoreSearchUiState.Error(
                            "Failed to load stores: ${e.localizedMessage}"
                        )
                    }
                )
            }
    }
    
    fun hasLocationPermission(): Boolean {
        return locationProvider.hasLocationPermission()
    }
}