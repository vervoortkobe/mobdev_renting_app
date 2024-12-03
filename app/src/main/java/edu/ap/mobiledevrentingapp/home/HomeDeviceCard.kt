package edu.ap.mobiledevrentingapp.home

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.unit.dp
import edu.ap.mobiledevrentingapp.firebase.AppUtil
import edu.ap.mobiledevrentingapp.firebase.Device
import edu.ap.mobiledevrentingapp.firebase.Rental
import edu.ap.mobiledevrentingapp.firebase.User
import edu.ap.mobiledevrentingapp.ui.theme.Yellow40
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun HomeDeviceCard(
    device: Device,
    rentalPeriods: List<Rental>,
    userLocation: android.location.Location,
    onClick: () -> Unit
) {
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
            // Display device name at the top
            Text(
                text = device.deviceName.replaceFirstChar { it.uppercase() },
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.padding(bottom = 4.dp) // Add some padding for spacing
            )

            // Display device category below the name
            Text(
                text = AppUtil.convertUppercaseToTitleCase(device.category),
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                color = Yellow40,
                modifier = Modifier.padding(end = 8.dp)
            )

            // Additional content...
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.LocationOn, contentDescription = "location", tint = Color.Gray)
                Text(
                    text = "${"%.1f".format(AppUtil.calculateDistanceUsingLocation(userLocation.latitude, userLocation.longitude, device.latitude, device.longitude))} km",
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.ShoppingCart, contentDescription = "price", tint = Color.Gray)
                Text(text = "€ ${device.price} / day", style = MaterialTheme.typography.bodyMedium)
            }

            // Display rental periods with prices
            rentalPeriods.forEach { rental ->
                val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val startDate = dateFormat.parse(rental.startDate)
                val endDate = dateFormat.parse(rental.endDate)

                if (startDate != null && endDate != null) {
                    val rentalPrice = AppUtil.calculateTotalPrice(device.price.toDouble(), startDate, endDate)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${AppUtil.formatDate(startDate)} - ${AppUtil.formatDate(endDate)}",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = "€ $rentalPrice",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.weight(1f),
                            color = Yellow40
                        )
                    }
                }
            }
        }
    }
}