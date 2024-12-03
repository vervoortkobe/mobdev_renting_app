package edu.ap.mobiledevrentingapp.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
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
fun RentedDeviceCard(
    rentedDevice: Device,
    rentalPeriod: Rental,
    renterData: User,
    onClick: () -> Unit
) {
    val deviceImage = AppUtil.decode(rentedDevice.images.firstOrNull() ?: "")?.asImageBitmap()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .border(1.dp, Color.Gray, RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Device Image
        Box(
            modifier = Modifier
                .size(135.dp)
                .clip(RoundedCornerShape(8.dp))
                .border(1.dp, Color.Transparent, RoundedCornerShape(8.dp))
        ) {
            if (deviceImage != null) {
                Image(
                    bitmap = deviceImage,
                    contentDescription = "Rented device image",
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
            // Device Name
            Text(
                text = rentedDevice.deviceName.replaceFirstChar { it.uppercase() },
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.padding(bottom = 4.dp)
            )

            // Device Category
            Text(
                text = AppUtil.convertUppercaseToTitleCase(rentedDevice.category),
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                color = Yellow40,
                modifier = Modifier.padding(end = 8.dp)
            )

            // Renter's Name
            Text(
                text = "Renter: ${renterData.fullName}",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 4.dp)
            )

            // Renter's Profile Image (if available)
            renterData.profileImage.let { profileImage ->
                AppUtil.decode(profileImage)?.let {
                    Image(
                        bitmap = it.asImageBitmap(),
                        contentDescription = "Renter's profile image",
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(20.dp))
                    )
                }
            }

            // Rental Period
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val startDate = dateFormat.parse(rentalPeriod.startDate)
            val endDate = dateFormat.parse(rentalPeriod.endDate)

            if (startDate != null && endDate != null) {
                Text(
                    text = "${AppUtil.formatDate(startDate)} - ${AppUtil.formatDate(endDate)}",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            // Rental Price
            val rentalPrice = startDate?.let {
                if (endDate != null) {
                    AppUtil.calculateTotalPrice(rentedDevice.price.toDouble(),
                        it, endDate)
                }
            }
            Text(
                text = "€ $rentalPrice",
                style = MaterialTheme.typography.bodyMedium,
                color = Yellow40
            )
        }
    }
}