package edu.ap.mobiledevrentingapp.home

import android.location.Location
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
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
import java.time.LocalDate

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun HomePage(navController: NavController) {
    var rentedDevices by remember { mutableStateOf<List<Pair<Device, String>>>(emptyList()) }
    var myRentedOutDevices by remember { mutableStateOf<List<Triple<Device, User, Rental>>>(emptyList()) }
    var userLocation by remember { mutableStateOf<Location?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var userRentals by remember { mutableStateOf<List<Rental>>(emptyList()) }

    // Fetch data on first composition
    LaunchedEffect(Unit) {
        isLoading = true
        val currentUserId = FirebaseService.getCurrentUserId()
        if (currentUserId != null) {
            // Fetch user location
            FirebaseService.getUserById(currentUserId) { success, document, _ ->
                if (success && document != null) {
                    userLocation = Location("").apply {
                        latitude = document.getDouble("latitude") ?: 0.0
                        longitude = document.getDouble("longitude") ?: 0.0
                    }
                }
            }

            // Fetch rented devices
            FirebaseService.getDevicesRentedByUser(currentUserId) { devicesWithNames ->
                rentedDevices = devicesWithNames
            }

            // Fetch my rented out devices
            FirebaseService.getMyRentedOutDevices(currentUserId) { rentedOutDevices ->
                myRentedOutDevices = rentedOutDevices // Populate myRentedOutDevices
                Log.d("HomePage", "My Rented Out Devices: $myRentedOutDevices") // Log rented out devices
            }

            // Fetch all rentals for the current user
            FirebaseService.getUserRentals(currentUserId) { rentals ->
                userRentals = rentals // Assuming rentals is a list of Rental objects
            }
        }
        isLoading = false
    }

    // UI Content
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(42.dp),
                color = Yellow40
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp)
            ) {
                // Devices currently rented by the user
                item {
                    Text(
                        text = "Devices I'm Currently Renting",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }

                if (rentedDevices.isEmpty()) {
                    item {
                        Text(
                            text = "You're not renting any devices",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    items(rentedDevices) { (device, rentalId) ->
                        userLocation?.let { location ->
                            // Get the current date
                            val currentDate = LocalDate.now()

                            // Filter rentals for the current device where the owner is the current user and the rental period is ongoing
                            val rentalPeriods = userRentals.filter { rental ->
                                rental.deviceId == device.deviceId &&
                                rental.ownerId == FirebaseService.getCurrentUserId() && // Assuming Rental has an ownerId field
                                LocalDate.parse(rental.startDate) <= currentDate && // Check if rental has started
                                LocalDate.parse(rental.endDate) >= currentDate // Check if rental has not ended
                            }

                            HomeDeviceCard(
                                device = device,
                                rentalPeriods = rentalPeriods, // Pass the filtered list of rental periods
                                userLocation = location,
                                onClick = {
                                    navController.navigate("device_details/${device.deviceId}")
                                }
                            )
                        }
                    }
                }

                // Devices rented out by the user
                item {
                    Text(
                        text = "Devices I'm Currently Renting Out",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                    )
                }

                // Get the current date
                val currentDate = LocalDate.now()

                // Filter my rented out devices to show only those with ongoing rentals
                val ongoingRentals = myRentedOutDevices.filter { (device, renter, rental) ->
                    rental.ownerId == FirebaseService.getCurrentUserId() && // Ensure the current user is the owner
                    LocalDate.parse(rental.startDate) <= currentDate && // Check if rental has started
                    LocalDate.parse(rental.endDate) >= currentDate // Check if rental has not ended
                }

                Log.d("HomePage", "Ongoing Rentals: $ongoingRentals")
                Log.d("HomePage", "My Rented Out Devices: $myRentedOutDevices")

                if (ongoingRentals.isEmpty()) {
                    item {
                        Text(
                            text = "You're not renting out any devices",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    items(ongoingRentals) { (device, renter, rental) ->
                        RentedDeviceCard(
                            rentedDevice = device,
                            rentalPeriod = rental,
                            renterData = renter,
                            onClick = {
                                navController.navigate("device_details/${device.deviceId}")
                            }
                        )
                    }
                }
            }
        }
    }
}
