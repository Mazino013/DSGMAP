package com.example.dsgmap.data.remote

import com.example.dsgmap.BuildConfig
import com.example.dsgmap.data.model.StoreResponse
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Query

interface StoreApiService {

    @Headers(API_KEY_HEADER)
    @GET("api/v4/stores/search")
    suspend fun searchStoresByZipCode(
        @Query("lob") lob: String = "dsg",
        @Query("radius") radius: Int = 100,
        @Query("addr") zipCode: String
    ): StoreResponse

    @Headers(API_KEY_HEADER)
    @GET("api/v4/stores/search")
    suspend fun searchStoresByLocation(
        @Query("lob") lob: String = "dsg",
        @Query("radius") radius: Int = 100,
        @Query("addr") latLong: String
    ): StoreResponse

    companion object {
        private const val API_KEY_HEADER = "x-api-key: ${BuildConfig.API_KEY}"
    }

}