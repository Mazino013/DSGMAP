package com.example.dsgmap.data.repository

import com.example.dsgmap.data.model.Store
import com.example.dsgmap.data.model.StoreResponse
import com.example.dsgmap.data.model.StoreUiModel
import com.example.dsgmap.data.remote.StoreApiService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import retrofit2.HttpException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StoreRepository @Inject constructor(
    private val storeApiService: StoreApiService
) {

    fun searchStoresByZipCode(zipCode: String): Flow<Result<List<StoreUiModel>>> = flow {
        val response = storeApiService.searchStoresByZipCode(zipCode = zipCode)
        
        if (response.allResults.isNotEmpty()) {
            emit(Result.success(mapStoreResponseToUiModel(response)))
        } else {
            emit(Result.success(emptyList()))
        }
    }.handleStoreApiErrors()

    fun searchStoresByLocation(latitude: Double, longitude: Double): Flow<Result<List<StoreUiModel>>> = flow {
        val latLng = "$latitude,$longitude"
        val response = storeApiService.searchStoresByLocation(latLong = latLng)
        
        if (response.allResults.isNotEmpty()) {
            emit(Result.success(mapStoreResponseToUiModel(response)))
        } else {
            emit(Result.success(emptyList()))
        }
    }.handleStoreApiErrors()

    private fun Flow<Result<List<StoreUiModel>>>.handleStoreApiErrors(): Flow<Result<List<StoreUiModel>>> =
        this.catch { e ->
            when (e) {
                is HttpException -> {
                    when (e.code()) {
                        404 -> emit(Result.success(emptyList()))
                        else -> emit(Result.failure(Exception("API error: ${e.code()} ${e.message()}")))
                    }
                }
                else -> {
                    emit(Result.failure(e))
                }
            }
        }

    private fun mapStoreResponseToUiModel(response: StoreResponse): List<StoreUiModel> {
        return response.allResults.mapNotNull { result ->
            try {
                mapStoreToUiModel(result.store, result.distance)
            } catch (e: Exception) {
                null
            }
        }.sortedBy { 
            // Sort by distance (closest first)
            it.distance 
        }
    }
    
    private fun mapStoreToUiModel(store: Store, distance: Double): StoreUiModel {
        val storeName = store.name.orEmpty()
        val storeCity = store.city.orEmpty() 
        val storeState = store.state.orEmpty()
        val location = if (storeCity.isNotEmpty() && storeState.isNotEmpty()) {
            "$storeCity, $storeState"
        } else {
            "Unknown Location"
        }
        
        // Parse latitude and longitude from strings to doubles
        val latitude = store.lat?.toDoubleOrNull() ?: 0.0
        val longitude = store.lng?.toDoubleOrNull() ?: 0.0
    
        return StoreUiModel(
            id = store.location.orEmpty(),
            name = storeName,
            distance = distance,
            location = location,
            address = buildAddress(store),
            latitude = latitude,
            longitude = longitude
        )
    }
    
    private fun buildAddress(store: Store): String {
        // Create address parts for each segment
        val streetPart = listOfNotNull(
            store.street1?.takeIf { it.isNotBlank() },
            store.street2?.takeIf { it.isNotBlank() }
        ).joinToString(", ").takeIf { it.isNotEmpty() }

        // handling for state and zip
        val stateZipPart = when {
            !store.state.isNullOrBlank() && !store.zip.isNullOrBlank() -> 
                "${store.state} ${store.zip}"
            !store.state.isNullOrBlank() -> store.state
            !store.zip.isNullOrBlank() -> store.zip
            else -> null
        }

        // Combine parts
        val parts = listOfNotNull(
            streetPart,
            store.city?.takeIf { it.isNotBlank() },
            stateZipPart
        )
        
        return parts.joinToString(", ").ifEmpty { "Unknown Address" }
    }
}