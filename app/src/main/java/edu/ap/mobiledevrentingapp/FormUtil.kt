package edu.ap.mobiledevrentingapp

import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.location.Geocoder
import android.location.Location
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import java.util.Locale

object FormUtil {
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
}