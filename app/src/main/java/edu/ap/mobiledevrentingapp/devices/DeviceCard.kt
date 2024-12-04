package edu.ap.mobiledevrentingapp.devices

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import edu.ap.mobiledevrentingapp.R
import edu.ap.mobiledevrentingapp.firebase.AppUtil
import edu.ap.mobiledevrentingapp.firebase.AppUtil.decode
import edu.ap.mobiledevrentingapp.firebase.Device
import edu.ap.mobiledevrentingapp.firebase.FirebaseService
import edu.ap.mobiledevrentingapp.firebase.User
import edu.ap.mobiledevrentingapp.ui.theme.Yellow40
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

@Composable
fun DeviceCard(
    device: Device,
    userLocation: android.location.Location,
    navController: androidx.navigation.NavController
) {
    var ownerData by remember { mutableStateOf(User("", "")) }
    var ownerName by remember { mutableStateOf("Loading...") }
    var ownerProfileImage by remember { mutableStateOf<Bitmap?>(null) }
    val context = LocalContext.current

    val distance = remember(userLocation, device) {
        AppUtil.calculateDistanceUsingLocation(
            userLocation.latitude,
            userLocation.longitude,
            device.latitude,
            device.longitude
        )
    }

    LaunchedEffect(device.ownerId) {
        ownerData = getOwnerData(device.ownerId)
        ownerName = ownerData.fullName
        ownerProfileImage = decode(ownerData.profileImage)
    }

    val deviceImages = remember(device) {
        device.images.mapNotNull { imageString ->
            decode(imageString)
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 8.dp, end = 8.dp, top = 2.dp, bottom = 4.dp)
            .border(1.dp, Color.Gray, RoundedCornerShape(8.dp))
            .clickable {
                navController.navigate("device_details/${device.deviceId}")
            }
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(135.dp)
                .clip(RoundedCornerShape(8.dp))
                .border(1.dp, Color.Transparent, RoundedCornerShape(8.dp))
        ) {
            if (deviceImages.isNotEmpty()) {
                Image(
                    bitmap = deviceImages.first().asImageBitmap(),
                    contentDescription = context.getString(R.string.device_image),
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
                        text = context.getString(R.string.no_image),
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White
                    )
                }
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Text(
                text = if (device.deviceName.length > 15) {
                    device.deviceName.replaceFirstChar { it.uppercase() }.take(12) + "..."
                } else {
                    device.deviceName.replaceFirstChar { it.uppercase() }
                },
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.weight(1f)
            )
            Text(
                text = AppUtil.convertUppercaseToTitleCase(device.category),
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                color = Yellow40,
                textAlign = TextAlign.End,
                modifier = Modifier.padding(end = 8.dp)
            )

            Spacer(modifier = Modifier.height(1.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.LocationOn, contentDescription = "location", tint = Color.Gray)
                Column {
                    Text(
                        text = "${"%.1f".format(distance)}km",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = ownerData.city.replaceFirstChar { it.uppercase() },
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
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
                            contentDescription = ownerName.replaceFirstChar { it.uppercase() },
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                }
                Text(
                    text = "${ownerData.fullName.split(" ").first().replaceFirstChar { it.uppercase() }} ${ownerData.fullName.split(" ").last().replaceFirstChar { it.uppercase() }}",
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
                val ownerData = User(
                    fullName = document.getString("fullName") ?: "Unknown",
                    phoneNumber = document.getString("phoneNumber") ?: "Unknown",
                    streetName = document.getString("streetName") ?: "Unknown",
                    zipCode = document.getString("zipCode") ?: "Unknown",
                    city = document.getString("city") ?: "Unknown",
                    addressNr = document.getString("addressNr") ?: "Unknown",
                    ibanNumber = document.getString("ibanNumber") ?: "Unknown",
                    country = document.getString("country") ?: "Unknown",
                    userId = document.getString("userId") ?: "Unknown",
                    profileImage = document.getString("profileImage").toString()
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