package edu.ap.mobiledevrentingapp.osm

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import org.osmdroid.util.GeoPoint
import edu.ap.mobiledevrentingapp.FirebaseService

object Coordinates {
    fun fetchAllDevices(callback: (List<GeoPoint>) -> Unit) {
        FirebaseService.getAllDevices { success, devices, error ->
            if (success) {
                val geoPoints = devices?.mapNotNull { document ->
                    val lat = document.getDouble("lat")
                    val lon = document.getDouble("lon")
                    if (lat != null && lon != null) {
                        GeoPoint(lat, lon)
                    } else {
                        null
                    }
                } ?: emptyList()

                callback(geoPoints)
            } else {
                println("Failed to fetch devices: $error")
                callback(emptyList())
            }
        }
    }
}