package edu.ap.mobiledevrentingapp.map

import edu.ap.mobiledevrentingapp.firebase.FirebaseService
import org.osmdroid.util.GeoPoint

object Coordinates {
    fun fetchAllDevices(callback: (List<GeoPoint>) -> Unit) {
        FirebaseService.getAllDevices { success, devices, error ->
            if (success) {
                val geoPoints = devices.map { device ->
                    val lat = device.latitude
                    val lon = device.longitude
                    GeoPoint(lat, lon)
                }

                callback(geoPoints)
            } else {
                println("Failed to fetch devices: $error")
                callback(emptyList())
            }
        }
    }
}