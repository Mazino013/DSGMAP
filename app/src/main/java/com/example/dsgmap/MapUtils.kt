package com.example.dsgmap

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.net.toUri

/**
 * Utility functions for map-related operations
 */
object MapUtils {

    /**
     * Opens a location in the default map app or falls back to browser if no map app is available
     *
     * @param context Android context for launching the intent
     * @param locationName Name of the location to display in map
     * @param lat Latitude coordinate
     * @param lng Longitude coordinate
     */
    fun openInMaps(context: Context, locationName: String, lat: Double, lng: Double) {
        try {
            // Format: geo:latitude,longitude?q=latitude,longitude(label)
            val geoUri = "geo:$lat,$lng?q=$lat,$lng(${Uri.encode(locationName)})".toUri()
            val mapIntent = Intent(Intent.ACTION_VIEW, geoUri)
            context.startActivity(mapIntent)
        } catch (e: Exception) {
            // Fallback for devices without map apps
            try {
                val fallbackUri =
                    "https://www.google.com/maps/search/?api=1&query=$lat,$lng".toUri()
                val browserIntent = Intent(Intent.ACTION_VIEW, fallbackUri)
                context.startActivity(browserIntent)
            } catch (e2: Exception) {
                // Unable to open any map application
            }
        }
    }
}