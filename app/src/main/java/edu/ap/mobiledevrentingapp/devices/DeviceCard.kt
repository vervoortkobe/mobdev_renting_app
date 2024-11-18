package edu.ap.mobiledevrentingapp.devices

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import edu.ap.mobiledevrentingapp.firebase.AppUtil
import edu.ap.mobiledevrentingapp.firebase.Device
import edu.ap.mobiledevrentingapp.firebase.FirebaseService
import edu.ap.mobiledevrentingapp.firebase.User
import edu.ap.mobiledevrentingapp.profile.decode
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

@Composable
fun DeviceCard(device: Device, images: List<Pair<String, Bitmap>>) {
    var ownerData by remember { mutableStateOf(User("", "")) }
    var ownerName by remember { mutableStateOf("Loading...") }
    var ownerProfileImage by remember { mutableStateOf<Bitmap?>(null) }

    LaunchedEffect(device.ownerId) {
        ownerData = getOwnerData(device.ownerId)
        ownerName = ownerData.fullName
        ownerProfileImage = decode(ownerData.profileImage)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 8.dp, end = 8.dp, top = 2.dp, bottom = 4.dp)
            .border(1.dp, Color.Gray, RoundedCornerShape(8.dp))
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(135.dp)
                .clip(RoundedCornerShape(8.dp))
                .border(1.dp, Color.Transparent, RoundedCornerShape(8.dp))
        ) {
            if (images.isNotEmpty()) {
                Image(
                    bitmap = images.first().second.asImageBitmap(),
                    contentDescription = "Device image",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Gray),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No image",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White
                    )
                }
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = device.deviceName,
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = AppUtil.convertUppercaseToTitleCase(device.category),
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.End,
                    modifier = Modifier.padding(end = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(1.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.LocationOn, contentDescription = "location", tint = Color.Gray)
                Text(text = "5km Antwerp", style = MaterialTheme.typography.bodyMedium)

                Spacer(modifier = Modifier.width(0.dp))

                Icon(Icons.Default.ShoppingCart, contentDescription = "price", tint = Color.Gray)
                Text(text = "€ ${device.price} / day", style = MaterialTheme.typography.bodyMedium)
            }

            Spacer(modifier = Modifier.height(0.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .clip(CircleShape)
                        .background(Color.LightGray)
                ) {
                    if (ownerProfileImage != null) {
                        Image(
                            bitmap = ownerProfileImage!!.asImageBitmap(),
                            contentDescription = "Owner profile image",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                }
                Text(
                    text = ownerName,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

suspend fun getOwnerData(ownerId: String): User {
    return suspendCancellableCoroutine { continuation ->
        FirebaseService.getUserById(ownerId) { success, document, error ->
            if (success && document != null) {
                val fullName = document.getString("fullName") ?: "Unknown"
                val phoneNumber = document.getString("phoneNumber")
                val streetName = document.getString("streetName")
                val zipCode = document.getString("zipCode")
                val city = document.getString("city")
                val addressNr = document.getString("addressNr")
                val ibanNumber = document.getString("ibanNumber")
                val country = document.getString("country")
                val userId = document.getString("userId")
                val profileImage = document.getString("profileImage")

                val ownerData = User(
                    fullName = fullName,
                    profileImage = profileImage.toString()
                )

                continuation.resume(ownerData)
            } else {
                continuation.resumeWithException(
                    Exception(error ?: "Failed to fetch owner data.")
                )
            }
        }
    }
}