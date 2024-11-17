package edu.ap.mobiledevrentingapp

import android.graphics.Bitmap
import android.widget.Toast
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import edu.ap.mobiledevrentingapp.ui.theme.MobileDevRentingAppTheme

@Composable
fun DevicesPage(navController: NavController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(2.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            Button(
                onClick = { navController.navigate("map") },
                modifier = Modifier.padding(end = 8.dp).width(180.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = "Map Icon",
                    modifier = Modifier.size(20.dp).padding(end = 4.dp)
                )
                Text("Map")
            }
            Button(
                onClick = { navController.navigate("add_device") },
                modifier = Modifier.width(180.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Device Icon",
                    modifier = Modifier.size(20.dp).padding(end = 4.dp)
                )
                Text("Add Device")
            }
        }

        DisplayDevicesWithImagesScreen()
    }
}

@Composable
fun DisplayDevicesWithImagesScreen() {
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

    if (isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
    }
    else if (devicesWithImages.isEmpty()) {
        Text(
            text = "No devices found.",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray
        )
    }
        else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            items(devicesWithImages) { (device, images) ->
                DeviceWithImagesCard(device, images)
            }
        }
    }
}

@Composable
fun DeviceWithImagesCard(device: Device, images: List<Pair<String, Bitmap>>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .border(1.dp, Color.Black, RoundedCornerShape(8.dp)),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Device Name: ${device.deviceName}",
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(modifier = Modifier.height(8.dp))

        if (images.isEmpty()) {
            Text(
                text = "No images available.",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
        } else {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                items(images) { (_, bitmap) ->
                    Image(
                        bitmap = bitmap.asImageBitmap(),
                        contentDescription = "Device image",
                        modifier = Modifier
                            .size(100.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .border(1.dp, Color.White, RoundedCornerShape(8.dp))
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DevicesPagePreview() {
    MobileDevRentingAppTheme {
        DevicesPage(navController = rememberNavController())
    }
}