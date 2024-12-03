package edu.ap.mobiledevrentingapp.home

import android.location.Location
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import edu.ap.mobiledevrentingapp.firebase.Device
import edu.ap.mobiledevrentingapp.firebase.FirebaseService
import edu.ap.mobiledevrentingapp.firebase.Rental
import edu.ap.mobiledevrentingapp.firebase.User
import edu.ap.mobiledevrentingapp.ui.theme.Yellow40

@Composable
fun HomePage(navController: NavController) {
    var rentedDevices by remember { mutableStateOf<List<Pair<Device, String>>>(emptyList()) }
    var myRentedDevices by remember { mutableStateOf<List<Device>>(emptyList()) }
    var userRentals by remember { mutableStateOf<List<Rental>>(emptyList()) }
    var userLocation by remember { mutableStateOf<Location?>(null) }
    var isLoading by remember { mutableStateOf(true) } // Loading state

    LaunchedEffect(Unit) {
        val currentUserId = FirebaseService.getCurrentUserId()
        if (currentUserId != null) {
            // Fetch user data
            FirebaseService.getUserById(currentUserId) { success, document, _ ->
                if (success && document != null) {
                    val user = User(
                        fullName = document.getString("fullName") ?: "Unknown",
                        phoneNumber = document.getString("phoneNumber") ?: "Unknown",
                        latitude = document.getDouble("latitude") ?: 0.0,
                        longitude = document.getDouble("longitude") ?: 0.0,
                    )
                    // Create a Location object
                    userLocation = Location("").apply {
                        latitude = user.latitude
                        longitude = user.longitude
                    }
                }
                isLoading = false // Set loading to false after fetching user data
            }

            // Fetch rented devices
            FirebaseService.getDevicesRentedByUser(currentUserId) { devicesWithNames ->
                rentedDevices = devicesWithNames
            }
            FirebaseService.getMyDevicesBeingRented(currentUserId) { devices ->
                myRentedDevices = devices
            }
            // Fetch all rentals for the current user
            FirebaseService.getDevicesRentedByUser(currentUserId) { devicesWithNames ->
                userRentals = devicesWithNames.map { (device, _) ->
                    Rental(deviceId = device.deviceId, renterId = currentUserId, startDate = "2023-01-01", endDate = "2023-01-10") // Example rental data
                }
            }
        }
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                color = Yellow40, // Use your theme color
                modifier = Modifier.size(48.dp) // Adjust size as needed
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp)
            ) {
                item {
                    Text(
                        text = "Devices I'm Currently Renting",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }

                if (rentedDevices.isEmpty()) {
                    item {
                        Text("You're not renting any devices", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                } else {
                    items(rentedDevices) { (device, _) ->
                        val rentalPeriods = userRentals.filter { it.deviceId == device.deviceId }
                        userLocation?.let { location ->
                            HomeDeviceCard(
                                device = device,
                                rentalPeriods = rentalPeriods,
                                userLocation = location, // Pass userLocation here
                                onClick = {
                                    navController.navigate("device_details/${device.deviceId}")
                                }
                            )
                        }
                    }
                }

                // New section for devices currently being rented out
                item {
                    Text(
                        text = "Devices I'm Currently Renting Out",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                    )
                }

                if (myRentedDevices.isEmpty()) {
                    item {
                        Text("You're not renting out any devices", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                } else {
                    items(myRentedDevices) { rentedDevice ->
                        val rentalPeriod = userRentals.find { it.deviceId == rentedDevice.deviceId }
                        rentalPeriod?.let { period ->
                            RentedDeviceCard(
                                rentedDevice = rentedDevice,
                                rentalPeriod = period,
                                renterData = User("Renter Name", "Renter Phone"), // Replace with actual renter data
                                onClick = {
                                    navController.navigate("device_details/${rentedDevice.deviceId}")
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}