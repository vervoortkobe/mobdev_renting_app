package edu.ap.mobiledevrentingapp.devices

import android.graphics.Bitmap
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import edu.ap.mobiledevrentingapp.firebase.Device
import edu.ap.mobiledevrentingapp.firebase.FirebaseService

@Composable
fun DisplayDevicesWithImages(searchQuery: String) {
    val context = LocalContext.current
    var devicesWithImages by remember { mutableStateOf<List<Pair<Device, List<Pair<String, Bitmap>>>>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        FirebaseService.getAllDevicesWithImages { success, devicesList, error ->
            if (success) {
                isLoading = false
                devicesWithImages = devicesList
            } else {
                Toast.makeText(
                    context,
                    error ?: "An error occurred while fetching devices.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    // Filter devices based on the search query
    val filteredDevices = if (searchQuery.isNotBlank()) {
        devicesWithImages.filter { it.first.deviceName.contains(searchQuery, ignoreCase = true) }
    } else {
        devicesWithImages
    }

    if (isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
    } else if (filteredDevices.isEmpty()) {
        Text(
            text = "No devices found.",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray
        )
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            items(filteredDevices) { (device, images) ->
                DeviceCard(device, images)
            }
        }
    }
}