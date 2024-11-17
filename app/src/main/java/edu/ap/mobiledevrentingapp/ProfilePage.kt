@file:Suppress("DEPRECATION")

package edu.ap.mobiledevrentingapp

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.MediaStore
import android.provider.MediaStore.Images.Media.getBitmap
import android.util.Base64
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

@Composable
fun ProfilePage() {
    val context = LocalContext.current
    var name by remember { mutableStateOf<String?>(null) }
    var phoneNumber by remember { mutableStateOf<String?>(null) }
    var streetName by remember { mutableStateOf<String?>(null) }
    var zipCode by remember { mutableStateOf<String?>(null) }
    var city by remember { mutableStateOf<String?>(null) }
    var adressNr by remember { mutableStateOf<String?>(null) }
    var ibanNumber by remember { mutableStateOf<String?>(null) }
    var totalAdress by remember { mutableStateOf<String?>(null) }
    var email by remember { mutableStateOf<String?>(null) }
    var encodedImage by remember { mutableStateOf<String?>(null) }
    var id by remember { mutableStateOf<String?>(null) }
    var profileBitmap by remember { mutableStateOf<Bitmap?>(null) }

    LaunchedEffect(Unit) {
        FirebaseService.getCurrentUser { success, document, _ ->
            if (success && document != null) {
                name = document.getString("fullName")
                phoneNumber = document.getString("phoneNumber")
                streetName = document.getString("streetName")
                zipCode = document.getString("zipCode")
                city = document.getString("city")
                adressNr = document.getString("addressNr")
                ibanNumber = document.getString("ibanNumber")
                totalAdress = "${streetName} ${adressNr} ${city} ${zipCode}"
                email = FirebaseService.getCurrentUserEmail()
                id = document.getString("userId")
                encodedImage = document.getString("profileImage")
                encodedImage?.let { Log.e("ErrorImage", it) }
                profileBitmap = if (!encodedImage.isNullOrEmpty()) decode(encodedImage) else null
            } else {
                profileBitmap = null
                Toast.makeText(
                    context,
                    "Your user data couldn't be loaded.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            val bitmap = getBitmap(context.contentResolver, it)
            profileBitmap = bitmap
            id?.let { userId ->
                FirebaseService.uploadUserProfileImage(userId, bitmap) { success, _, _ ->
                    if (success) {
                        Toast.makeText(context, "Profile image uploaded successfully!", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, "Failed to upload image", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box() {
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(RoundedCornerShape(50.dp))
                        .border(2.dp, Color.Gray, RoundedCornerShape(50.dp))
                ) {
                    if (profileBitmap != null) {
                        Image(
                            bitmap = profileBitmap!!.asImageBitmap(),
                            contentDescription = "Profile Image",
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(50.dp)),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .size(100.dp)
                                .clip(RoundedCornerShape(50.dp))
                                .background(Color.Gray)
                                .border(2.dp, Color.LightGray, RoundedCornerShape(50.dp))
                        )
                    }
                }
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .clip(RoundedCornerShape(50.dp))
                        .background(Color.White)
                        .border(2.dp, Color.Gray, RoundedCornerShape(50.dp))
                        .clickable {
                            launcher.launch("image/*")
                        }
                        .align(Alignment.TopEnd)
                        .offset(x = 18.dp, y = (-18).dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Home,
                        contentDescription = "Edit Profile Image",
                        modifier = Modifier
                            .size(18.dp)
                            .align(Alignment.Center),
                        tint = Color.Gray
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            name?.let { Text(text = it, color = Color.Black) }
            Spacer(modifier = Modifier.height(4.dp))
            email?.let { Text(text = it, color = Color.Black) }
            Spacer(modifier = Modifier.height(4.dp))
            phoneNumber?.let { Text(text = it, color = Color.Black) }
            Spacer(modifier = Modifier.height(4.dp))
            ibanNumber?.let { Text(text = it, color = Color.Black) }
            Spacer(modifier = Modifier.height(4.dp))
            totalAdress?.let { Text(text = it, color = Color.Black) }
        }
    }
}

fun decode(toDecodeString: String?): Bitmap? {
    if (toDecodeString.isNullOrEmpty()) return null
    return try {
        val byteArray = Base64.decode(toDecodeString, Base64.DEFAULT)
        BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
    } catch (e: IllegalArgumentException) {
        null
    }
}





