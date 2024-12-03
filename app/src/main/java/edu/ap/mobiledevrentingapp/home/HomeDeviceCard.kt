package edu.ap.mobiledevrentingapp.home

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
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
import edu.ap.mobiledevrentingapp.firebase.Rental
import edu.ap.mobiledevrentingapp.firebase.User
import kotlinx.coroutines.suspendCancellableCoroutine
import java.text.SimpleDateFormat
import java.util.*
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

@Composable
fun HomeDeviceCard(
    device: Device,
    rentalPeriods: List<Rental>,
    userLocation: android.location.Location,
    onClick: () -> Unit
) {
    var ownerData by remember { mutableStateOf(User("", "")) }
    var ownerName by remember { mutableStateOf("Loading...") }
    var ownerProfileImage by remember { mutableStateOf<Bitmap?>(null) }

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
        ownerProfileImage = AppUtil.decode(ownerData.profileImage)
    }

    val deviceImages = remember(device) {
        device.images.mapNotNull { AppUtil.decode(it) }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .border(1.dp, Color.Gray, RoundedCornerShape(8.dp))
            .clickable { onClick() }
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
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Text(
                text = device.deviceName.replaceFirstChar { it.uppercase() },
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.padding(bottom = 4.dp)
            )
            Text(
                text = AppUtil.convertUppercaseToTitleCase(device.category),
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                color = Color.Yellow,
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
                        text = "${"%.1f".format(distance)} km",
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

            // Display rental periods
            rentalPeriods.forEach { rental ->
                val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val startDate = dateFormat.parse(rental.startDate)
                val endDate = dateFormat.parse(rental.endDate)

                if (startDate != null && endDate != null) {
                    Text(
                        text = "${AppUtil.formatDate(startDate)} - ${AppUtil.formatDate(endDate)}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

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