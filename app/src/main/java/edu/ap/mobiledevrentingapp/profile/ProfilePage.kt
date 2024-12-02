package edu.ap.mobiledevrentingapp.profile

import android.graphics.Bitmap
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import edu.ap.mobiledevrentingapp.firebase.AppUtil.decode
import edu.ap.mobiledevrentingapp.firebase.FirebaseService
import edu.ap.mobiledevrentingapp.ui.theme.Yellow40

@Composable
fun ProfilePage(navController: NavController) {
    val context = LocalContext.current
    var name by remember { mutableStateOf<String?>(null) }
    var phoneNumber by remember { mutableStateOf<String?>(null) }
    var streetName by remember { mutableStateOf<String?>(null) }
    var zipCode by remember { mutableStateOf<String?>(null) }
    var city by remember { mutableStateOf<String?>(null) }
    var adressNr by remember { mutableStateOf<String?>(null) }
    var ibanNumber by remember { mutableStateOf<String?>(null) }
    var totalAdress by remember { mutableStateOf<String?>(null) }
    var email by remember { mutableStateOf<String?>(null) }
    var country by remember { mutableStateOf<String?>(null) }
    var encodedImage by remember { mutableStateOf<String?>(null) }
    var profileBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var devices by remember { mutableStateOf<List<Map<String, Any>>?>(null) }

    LaunchedEffect(Unit) {
        FirebaseService.getCurrentUser { success, document, _ ->
            if (success && document != null) {
                name = document.getString("fullName")
                phoneNumber = document.getString("phoneNumber")
                streetName = document.getString("streetName")
                zipCode = document.getString("zipCode")
                city = document.getString("city")
                adressNr = document.getString("addressNr")
                ibanNumber = document.getString("ibanNumber")
                country = document.getString("country")
                totalAdress = "${streetName} ${adressNr} ${city} ${zipCode} ${country}"
                email = FirebaseService.getCurrentUserEmail()
                encodedImage = document.getString("profileImage")
                profileBitmap = if (!encodedImage.isNullOrEmpty()) decode(encodedImage!!) else null

                // Fetch devices
                val userId = document.getString("userId")
                if (!userId.isNullOrEmpty()) {
                    FirebaseService.getDevicesByUserId(userId) { success, documents, _ ->
                        if (success) {
                            devices = documents?.map { it.data ?: emptyMap() }
                        } else {
                            devices = emptyList()
                        }
                    }
                }
            } else {
                profileBitmap = null
                Toast.makeText(
                    context,
                    "Your user data couldn't be loaded.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 72.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                ProfileHeader(
                    profileBitmap = profileBitmap,
                    name = name,
                    email = email,
                    phoneNumber = phoneNumber,
                    totalAdress = totalAdress,
                    ibanNumber = ibanNumber
                )
            }

            item {
                Text(
                    text = "Your Devices",
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Start
                )
            }

            devices?.let {
                items(it) { device ->
                    DeviceCard(device) { deviceId ->
                        // Handle delete action
                        FirebaseService.deleteDeviceById(deviceId) { success, error ->
                            if (success) {
                                Toast.makeText(context, "Device deleted successfully.", Toast.LENGTH_SHORT).show()
                                // Refresh the device list
                                devices = devices?.filterNot { it["deviceId"] == deviceId }
                            } else {
                                Toast.makeText(context, "Failed to delete device: $error", Toast.LENGTH_LONG).show()
                            }
                        }
                    }
                }
            }
        }

        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp)
                .size(50.dp)
                .clip(CircleShape)
                .background(Yellow40)
                .clickable {
                    navController.navigate("profileSettings")
                }
        ) {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = "Settings",
                tint = Color.Black,
                modifier = Modifier.align(Alignment.Center)
            )
        }
    }
}

@Composable
fun ProfileHeader(
    profileBitmap: Bitmap?,
    name: String?,
    email: String?,
    phoneNumber: String?,
    totalAdress: String?,
    ibanNumber: String?
) {
    Box(
        modifier = Modifier
            .size(100.dp)
            .clip(RoundedCornerShape(50.dp))
            .border(2.dp, Color.Gray, RoundedCornerShape(50.dp))
    ) {
        if (profileBitmap != null) {
            Image(
                bitmap = profileBitmap.asImageBitmap(),
                contentDescription = "Profile Image",
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(50.dp)),
                contentScale = ContentScale.Crop
            )
        } else {
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(RoundedCornerShape(50.dp))
                    .background(Color.Gray)
                    .border(2.dp, Color.LightGray, RoundedCornerShape(50.dp))
            )
        }
    }

    Spacer(modifier = Modifier.height(16.dp))

    Text(
        text = name ?: "N/A",
        modifier = Modifier.fillMaxWidth(),
        style = MaterialTheme.typography.bodyMedium,
        textAlign = TextAlign.Center
    )
    Text(
        text = email ?: "N/A",
        modifier = Modifier.fillMaxWidth(),
        style = MaterialTheme.typography.bodyMedium,
        textAlign = TextAlign.Center
    )
    Text(
        text = phoneNumber ?: "N/A",
        modifier = Modifier.fillMaxWidth(),
        style = MaterialTheme.typography.bodyMedium,
        textAlign = TextAlign.Center
    )
    Text(
        text = totalAdress ?: "N/A",
        modifier = Modifier.fillMaxWidth(),
        style = MaterialTheme.typography.bodyMedium,
        textAlign = TextAlign.Center
    )
    Text(
        text = ibanNumber ?: "N/A",
        modifier = Modifier.fillMaxWidth(),
        style = MaterialTheme.typography.bodyMedium,
        textAlign = TextAlign.Center
    )
}

@Composable
fun DeviceCard(device: Map<String, Any>, onDelete: (String) -> Unit) {
    val context = LocalContext.current
    var renterName by remember { mutableStateOf<String>("Loading...") }

    val deviceId = device["deviceId"]?.toString()
    LaunchedEffect(deviceId) {
        if (!deviceId.isNullOrEmpty()) {
            FirebaseService.getRentalsByDeviceId(deviceId) { rentals ->
                if (rentals.isNotEmpty()) {
                    val renterId = rentals.firstOrNull()?.renterId
                    if (!renterId.isNullOrEmpty()) {
                        FirebaseService.getUserById(renterId) { success, document, error ->
                            renterName = if (success && document != null) {
                                document.getString("fullName") ?: "Unknown Renter"
                            } else {
                                error ?: "Failed to load renter details"
                            }
                        }
                    } else {
                        renterName = "No renter assigned"
                    }
                } else {
                    renterName = "Not rented"
                }
            }
        } else {
            renterName = "Invalid Device ID"
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .border(1.dp, Color.Gray, RoundedCornerShape(8.dp)),
        shape = RoundedCornerShape(8.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = device["deviceName"]?.toString() ?: "Unknown Device",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Category: ${device["category"]?.toString() ?: "N/A"}",
                style = MaterialTheme.typography.bodySmall
            )
            Text(
                text = "Currently being rented by: ${renterName}",
                style = MaterialTheme.typography.bodySmall
            )
            Spacer(modifier = Modifier.height(8.dp))

            androidx.compose.material3.Button(
                onClick = {
                    if (!deviceId.isNullOrEmpty()) {
                        onDelete(deviceId)
                    } else {
                        Toast.makeText(context, "Invalid Device ID", Toast.LENGTH_SHORT).show()
                    }
                }
            ) {
                Text(text = "Delete Device")
            }
        }
    }
}

