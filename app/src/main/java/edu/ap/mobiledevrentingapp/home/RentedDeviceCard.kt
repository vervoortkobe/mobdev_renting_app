package edu.ap.mobiledevrentingapp.home

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import edu.ap.mobiledevrentingapp.R
import edu.ap.mobiledevrentingapp.firebase.AppUtil
import edu.ap.mobiledevrentingapp.firebase.Device
import edu.ap.mobiledevrentingapp.firebase.Rental
import edu.ap.mobiledevrentingapp.firebase.User
import edu.ap.mobiledevrentingapp.ui.theme.Yellow40
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun RentedDeviceCard(
    rentedDevice: Device,
    rentalPeriod: Rental,
    renterData: User,
    onClick: () -> Unit
) {
    val deviceImage = AppUtil.decode(rentedDevice.images.firstOrNull() ?: "")?.asImageBitmap()
    val context = LocalContext.current
    val startDateString = rentalPeriod.startDate
    val endDateString = rentalPeriod.endDate

    val startDate: Date? = if (startDateString.isNotEmpty()) {
        try {
            SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(startDateString)
        } catch (e: ParseException) {
            null
        }
    } else {
        null
    }

    val endDate: Date? = if (endDateString.isNotEmpty()) {
        try {
            SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(endDateString)
        } catch (e: ParseException) {
            null
        }
    } else {
        null
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
            if (deviceImage != null) {
                Image(
                    bitmap = deviceImage,
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
                text = rentedDevice.deviceName.replaceFirstChar { it.uppercase() },
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.padding(bottom = 4.dp)
            )

            Text(
                text = AppUtil.convertUppercaseToTitleCase(rentedDevice.category),
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                color = Yellow40,
                modifier = Modifier.padding(end = 8.dp)
            )



            renterData.profileImage.let { profileImage ->
                AppUtil.decode(profileImage)?.let {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            bitmap = it.asImageBitmap(),
                            contentDescription = context.getString(R.string.home_renters_profile_image),
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(20.dp))
                        )
                        Text(
                            text = renterData.fullName,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }

            if (startDate != null && endDate != null) {
                Text(
                    text = "${AppUtil.formatDate(startDate)} - ${AppUtil.formatDate(endDate)}",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 4.dp).weight(1f)
                )
            }

            Text(
                text = "€ ${
                    startDate?.let { start ->
                        endDate?.let { end ->
                            String.format(
                                Locale.getDefault(),
                                "%.2f",
                                AppUtil.calculateTotalPrice(rentedDevice.price.toDouble(), start, end).toDouble()
                            )
                        }
                    } ?: "0.00"
                }",
                style = MaterialTheme.typography.bodyMedium,
                color = Yellow40
            )
        }
    }
}