package edu.ap.mobiledevrentingapp

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.ShoppingCart
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.google.firebase.firestore.DocumentSnapshot
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

        DisplayDevicesWithImages()
    }
}

@Composable
fun DisplayDevicesWithImages() {
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
                DeviceCard(device, images)
            }
        }
    }
}

@Composable
fun DeviceCard(device: Device, images: List<Pair<String, Bitmap>>) {
    var ownerData by remember { mutableStateOf<DocumentSnapshot?>(null) }

    // Fetch owner data
    LaunchedEffect(device.ownerId) {
        fetchOwnerData(device.ownerId) { success, document, _ ->
            if (success) {
                ownerData = document
            }
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .border(1.dp, Color.Gray, RoundedCornerShape(8.dp))
            .padding(8.dp),
        verticalAlignment = Alignment.Top
    ) {
        // Left Side: Title and Owner Info
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Title and Owner Info Row
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Owner Profile Image
                val profileImageBase64 = ownerData?.getString("profileImage")
                if (profileImageBase64 != null) {
                    val imageBitmap = profileImageBase64.decodeBase64ToBitmap()
                    if (imageBitmap != null) {
                        Image(
                            bitmap = imageBitmap.asImageBitmap(),
                            contentDescription = "Owner profile",
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color.Gray)
                        )
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color.Gray)
                    )
                }

                // Owner Name
                val ownerName = ownerData?.getString("fullName") ?: "Unknown Owner"
                Text(
                    text = ownerName,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                )
            }

            // Device Title
            Text(
                text = device.deviceName,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Center: Images Section
        Box(
            modifier = Modifier
                .weight(2f)
                .aspectRatio(1f)
        ) {
            if (images.isNotEmpty()) {
                // Stack images
                images.forEachIndexed { index, (_, bitmap) ->
                    Image(
                        bitmap = bitmap.asImageBitmap(),
                        contentDescription = "Device image",
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(start = (index * 8).dp, top = (index * 8).dp)
                            .clip(RoundedCornerShape(8.dp))
                            .border(
                                1.dp,
                                Color.Gray,
                                RoundedCornerShape(8.dp)
                            )
                            .graphicsLayer { scaleX = 1.1f; scaleY = 1.1f } // Slight zoom
                            .zIndex((images.size - index).toFloat())
                    )
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.Gray),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No images",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White
                    )
                }
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Right Side: Device Details
        Column(
            modifier = Modifier.weight(3f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Location and Price
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.LocationOn, contentDescription = null, tint = Color.Gray)
                Text(
                    text = ownerData?.getString("city") ?: "Unknown City",
                    style = MaterialTheme.typography.bodySmall
                )

                Spacer(modifier = Modifier.width(16.dp))

                Icon(Icons.Default.ShoppingCart, contentDescription = null, tint = Color.Gray)
                Text(
                    text = "${device.price} €",
                    style = MaterialTheme.typography.bodySmall
                )
            }

            // Category
            Text(
                text = AppUtil.convertUppercaseToTitleCase(device.category),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary
            )

            // Description
            Text(
                text = device.description.take(500) + if (device.description.length > 500) "..." else "",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
        }
    }
}

// Extension function to decode Base64 to Bitmap
fun String.decodeBase64ToBitmap(): Bitmap? {
    return try {
        val decodedBytes = Base64.decode(this, Base64.DEFAULT)
        BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
    } catch (e: Exception) {
        null
    }
}

// Owner Data Fetch Wrapper
fun fetchOwnerData(userId: String, callback: (Boolean, DocumentSnapshot?, String?) -> Unit) {
    FirebaseService.getUserById(userId, callback)
}


@Preview(showBackground = true)
@Composable
fun DevicesPagePreview() {
    MobileDevRentingAppTheme {
        DevicesPage(navController = rememberNavController())
    }
}