package edu.ap.mobiledevrentingapp.home

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import edu.ap.mobiledevrentingapp.firebase.Device

@Composable
fun DeviceCard(device: Device, renterName: String? = null, isRentedOut: Boolean = false) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = device.deviceName,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, fontSize = 18.sp)
            )
            Text(
                text = "Price: €${device.price}/day",
                style = MaterialTheme.typography.bodyMedium
            )
            if (isRentedOut && renterName != null) {
                Text(
                    text = "Rented by: $renterName",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}