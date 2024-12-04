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

    LaunchedEffect(Unit) {
        isLoading = true
        val currentUserId = FirebaseService.getCurrentUserId()
        if (currentUserId != null) {
            FirebaseService.getUserById(currentUserId) { success, document, _ ->
                if (success && document != null) {
                    userLocation = Location("").apply {
                        latitude = document.getDouble("latitude") ?: 0.0
                        longitude = document.getDouble("longitude") ?: 0.0
                    }
                }
            }

            FirebaseService.getDevicesRentedByUser(currentUserId) { devicesWithNames ->
                rentedDevices = devicesWithNames
            }

            FirebaseService.getMyRentedOutDevices(currentUserId) { rentedOutDevices ->
                myRentedOutDevices = rentedOutDevices
                Log.d("HomePage", "My Rented Out Devices: $myRentedOutDevices")
            }

            FirebaseService.getUserRentals(currentUserId) { rentals ->
                userRentals = rentals
            }
        }
        isLoading = false
    }

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
                    items(rentedDevices) { (device, _) ->
                        userLocation?.let { location ->
                            val currentDate = LocalDate.now()

                            val rentalPeriods = userRentals.filter { rental ->
                                rental.deviceId == device.deviceId &&
                                rental.ownerId == FirebaseService.getCurrentUserId() &&
                                LocalDate.parse(rental.startDate) <= currentDate &&
                                LocalDate.parse(rental.endDate) >= currentDate
                            }

                            HomeDeviceCard(
                                device = device,
                                rentalPeriods = rentalPeriods,
                                userLocation = location,
                                onClick = {
                                    navController.navigate("device_details/${device.deviceId}")
                                }
                            )
                        }
                    }
                }

                item {
                    Text(
                        text = "Devices I'm Currently Renting Out",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                    )
                }

                val currentDate = LocalDate.now()

                val ongoingRentals = myRentedOutDevices.filter { (_, _, rental) ->
                    rental.ownerId == FirebaseService.getCurrentUserId() &&
                    LocalDate.parse(rental.startDate) <= currentDate &&
                    LocalDate.parse(rental.endDate) >= currentDate
                }

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
