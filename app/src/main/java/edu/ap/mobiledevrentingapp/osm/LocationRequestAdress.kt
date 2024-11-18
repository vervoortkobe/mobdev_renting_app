package edu.ap.mobiledevrentingapp.osm

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import org.osmdroid.util.GeoPoint
import edu.ap.mobiledevrentingapp.FirebaseService
import retrofit2.http.GET
import retrofit2.http.Query

interface GeocodingService {
    @GET("search")
    suspend fun getCoordinates(
        @Query("q") address: String,
        @Query("format") format: String = "json"
    ): List<GeocodingResult>
}

data class GeocodingResult(
    val lat: String,
    val lon: String
)
