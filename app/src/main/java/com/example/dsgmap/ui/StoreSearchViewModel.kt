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
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class StoreSearchViewModel @Inject constructor(
    private val storeRepository: StoreRepository,
    private val locationProvider: LocationProvider
) : ViewModel(), DefaultLifecycleObserver {

    data class StoreSearchUiState(
        val isLoading: Boolean = false,
        val stores: List<StoreUiModel> = emptyList(),
        val error: String? = null,
        val isEmpty: Boolean = false
    )

    private val _uiState = MutableStateFlow(StoreSearchUiState())
    val uiState: StateFlow<StoreSearchUiState> = _uiState.asStateFlow()

    override fun onPause(owner: LifecycleOwner) {
        clearStoreList()
        super.onPause(owner)
    }

    private fun clearStoreList() {
        _uiState.update {
            // Reset to initial state
            StoreSearchUiState()
        }
    }

    fun searchStoresByZipCode(zipCode: String) {
        if (zipCode.isBlank()) return
        
        _uiState.update { it.copy(isLoading = true, error = null) }
        
        viewModelScope.launch {
            try {
                storeRepository.searchStoresByZipCode(zipCode)
                    .catch { e ->
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                error = "Failed to load stores: ${e.message ?: "Unknown error"}"
                            )
                        }
                    }
                    .collectLatest { result ->
                        result.fold(
                            onSuccess = { stores ->
                                _uiState.update {
                                    it.copy(
                                        isLoading = false,
                                        stores = stores,
                                        isEmpty = stores.isEmpty(),
                                        error = null
                                    )
                                }
                            },
                            onFailure = { e ->
                                _uiState.update {
                                    it.copy(
                                        isLoading = false,
                                        error = "Failed to load stores: ${e.message ?: "Unknown error"}"
                                    )
                                }
                            }
                        )
                    }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = "Failed to load stores: ${e.message ?: "Unknown error"}"
                    )
                }
            }
        }
    }

    fun searchStoresByCurrentLocation() {
        _uiState.update { it.copy(isLoading = true, error = null) }
        
        viewModelScope.launch {
            val location = locationProvider.getCurrentLocation()
            if (location == null) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = "Could not determine your location. Please try again or search by ZIP code."
                    )
                }
                return@launch
            }
            
            searchStoresByLocation(location)
        }
    }
    
    private suspend fun searchStoresByLocation(location: Location) {
        storeRepository.searchStoresByLocation(location.latitude, location.longitude)
            .catch { e ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = "Failed to load stores: ${e.localizedMessage}"
                    )
                }
            }
            .collectLatest { result ->
                result.fold(
                    onSuccess = { stores ->
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                stores = stores,
                                //Store not found
                                isEmpty = stores.isEmpty(),
                                error = null
                            )
                        }
                    },
                    onFailure = { e ->
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                error = "Failed to load stores: ${e.localizedMessage}"
                            )
                        }
                    }
                )
            }
    }
    
    fun hasLocationPermission(): Boolean {
        return locationProvider.hasLocationPermission()
    }
}