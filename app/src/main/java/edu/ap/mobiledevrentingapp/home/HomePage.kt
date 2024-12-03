package edu.ap.mobiledevrentingapp.home

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import edu.ap.mobiledevrentingapp.firebase.Device
import edu.ap.mobiledevrentingapp.firebase.FirebaseService

@Composable
fun HomePage(navController: NavController) {
    var rentedDevices by remember { mutableStateOf<List<Pair<Device, String>>>(emptyList()) }
    var myRentedDevices by remember { mutableStateOf<List<Device>>(emptyList()) }

    LaunchedEffect(Unit) {
        val currentUser = FirebaseService.getCurrentUserId()
        if (currentUser != null) {
            FirebaseService.getDevicesRentedByUser(currentUser) { devicesWithNames ->
                rentedDevices = devicesWithNames
            }
            FirebaseService.getMyDevicesBeingRented(currentUser) { devices ->
                myRentedDevices = devices
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Devices I'm Renting
        Text(
            text = "Devices I'm Renting",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            modifier = Modifier.padding(bottom = 8.dp)
        )
        if (rentedDevices.isEmpty()) {
            Text("You're not renting any devices", color = MaterialTheme.colorScheme.onSurfaceVariant)
        } else {
            for ((device, renterName) in rentedDevices) {
                DeviceCard(device = device, renterName = renterName)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Devices I'm Renting Out
        Text(
            text = "Devices I'm Renting Out",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            modifier = Modifier.padding(bottom = 8.dp)
        )
        if (myRentedDevices.isEmpty()) {
            Text("You are not renting out any devices", color = MaterialTheme.colorScheme.onSurfaceVariant)
        } else {
            for (device in myRentedDevices) {
                DeviceCard(device = device, isRentedOut = true)
            }
        }
    }
}

@Composable
fun DeviceCard(device: Device, renterName: String? = null, isRentedOut: Boolean = false) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
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