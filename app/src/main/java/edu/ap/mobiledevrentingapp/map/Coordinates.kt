package edu.ap.mobiledevrentingapp.map

import edu.ap.mobiledevrentingapp.firebase.FirebaseService
import org.osmdroid.util.GeoPoint

data class MarkerInfo(
    val geoPoint: GeoPoint,
    val deviceId: String,
    val imageUrl: String
)

object Coordinates {
    fun fetchAllDevices(callback: (List<MarkerInfo>) -> Unit) {
        FirebaseService.getAllDevices { success, devices, error ->
            if (success) {
                val markerInfoList = devices.map { device ->
                    val lat = device.latitude
                    val lon = device.longitude
                    MarkerInfo(
                        geoPoint = GeoPoint(lat, lon),
                        deviceId = device.deviceId,
                        imageUrl = device.images.first()
                    )
                }

                callback(markerInfoList)
            } else {
                println("Failed to fetch devices: $error")
                callback(emptyList())
            }
        }
    }
}