package edu.ap.mobiledevrentingapp.map

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
