package edu.ap.mobiledevrentingapp.map

import edu.ap.mobiledevrentingapp.firebase.FirebaseService
import org.osmdroid.util.GeoPoint

object Coordinates {
    /*fun fetchAllDevices(callback: (List<GeoPoint>) -> Unit) {
        FirebaseService.getAllDevices { success, devices, error ->
            if (success) {
                val geoPoints = devices?.mapNotNull { document ->
                    val lat = document.getDouble("latitude")
                    val lon = document.getDouble("longitude")
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
    }*/
}