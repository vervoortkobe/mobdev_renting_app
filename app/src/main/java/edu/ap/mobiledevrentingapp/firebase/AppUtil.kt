package edu.ap.mobiledevrentingapp.firebase

import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import java.util.Calendar
import java.util.Date
import java.util.Locale

object AppUtil {
    fun loadBitmapFromUri(context: Context, uri: Uri): Bitmap? {
        return try {
            if (Build.VERSION.SDK_INT < 28) {
                MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
            } else {
                val source = ImageDecoder.createSource(context.contentResolver, uri)
                ImageDecoder.decodeBitmap(source)
            }
        } catch (e: Exception) {
            Log.e("FormUtil", "Error loading bitmap", e)
            null
        }
    }

    fun isValidPhoneNumber(phoneNumber: String): Boolean {
        return phoneNumber.startsWith("+") && phoneNumber.length in 8..14
    }

    fun isValidIbanNumber(ibanNumber: String): Boolean {
        return ibanNumber.length in 16..34
    }

    fun convertUppercaseToTitleCase(input: String): String {
        return input.split(" ").joinToString { it.lowercase(Locale.ROOT).capitalize(Locale.ROOT).replace("_", " & ") }
    }

    fun calculateDistanceUsingLocation(latitude1: Double, longitude1: Double, latitude2: Double, longitude2: Double): Float {
        val results = FloatArray(1)
        android.location.Location.distanceBetween(latitude1, longitude1, latitude2, longitude2, results)
        return results[0] / 1000 // Convert meters to kilometers
    }

    fun decode(base64String: String): Bitmap? {
        return try {
            val imageBytes = android.util.Base64.decode(base64String, android.util.Base64.DEFAULT)
            android.graphics.BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
        } catch (e: Exception) {
            Log.e("AppUtil", "Error decoding base64 string", e)
            null
        }
    }

    fun formatDate(date: Date): String {
        val cal = Calendar.getInstance()
        cal.time = date
        return "${cal.get(Calendar.DAY_OF_MONTH)}/${cal.get(Calendar.MONTH) + 1}/${cal.get(Calendar.YEAR)}"
    }

    fun calculateTotalPrice(pricePerDay: Double, startDate: Date, endDate: Date): String {
        val diffInMillis = endDate.time - startDate.time
        val days = (diffInMillis / (1000 * 60 * 60 * 24)) + 1
        val total = pricePerDay * days
        return String.format(Locale.US, "%.2f", total)
    }
}