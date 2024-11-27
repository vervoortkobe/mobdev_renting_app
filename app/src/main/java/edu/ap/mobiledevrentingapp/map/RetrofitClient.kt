package edu.ap.mobiledevrentingapp.map

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    private const val BASE_URL = "https://nominatim.openstreetmap.org/"

    private val client: OkHttpClient by lazy {
        OkHttpClient.Builder()
        .addInterceptor { chain ->
            val request = chain.request().newBuilder()
                .header("User-Agent", "BorrowBee/1.0 (kobe.vervoort@student.ap.be)")
                .build()
            chain.proceed(request)
        }
        .build()
    }

    val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build()
    }
}