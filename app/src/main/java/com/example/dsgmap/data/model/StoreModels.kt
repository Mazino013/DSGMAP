package com.example.dsgmap.data.model

import com.google.gson.annotations.SerializedName
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class StoreResponse(
    @SerializedName("results")
    val results: List<StoreResult> = emptyList(),
    
    // Add a fallback field in case the API returns data in a different structure
    @SerializedName("stores")
    private val _stores: List<StoreResult>? = null
) : Parcelable {
    // This getter ensures we always get a list, even if the API structure changes
    val allResults: List<StoreResult>
        get() = results.ifEmpty { _stores ?: emptyList() }
}

@Parcelize
data class StoreResult(
    @SerializedName("store")
    val store: Store = Store(),
    
    @SerializedName("distance")
    val distance: Double = 0.0,
    
    @SerializedName("units")
    val units: String = ""
) : Parcelable

@Parcelize
data class Store(
    @SerializedName("location")
    val location: String? = "",
    
    @SerializedName("chain")
    val chain: String? = "",
    
    @SerializedName("name")
    val name: String? = "",
    
    @SerializedName("street1")
    val street1: String? = "",
    
    @SerializedName("street2")
    val street2: String? = null,
    
    @SerializedName("phone")
    val phone: String? = "",
    
    @SerializedName("city")
    val city: String? = "",
    
    @SerializedName("state")
    val state: String? = "",
    
    @SerializedName("zip")
    val zip: String? = "",
    
    @SerializedName("lat")
    val lat: String? = "",
    
    @SerializedName("lng")
    val lng: String? = ""

) : Parcelable

// Domain models for UI display
data class StoreUiModel(
    val id: String,
    val name: String,
    val distance: Double,
    val location: String,
    val address: String
)