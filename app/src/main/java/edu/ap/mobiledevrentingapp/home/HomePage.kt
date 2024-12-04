package edu.ap.mobiledevrentingapp.home

import android.location.Location
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import edu.ap.mobiledevrentingapp.R
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
    val context = LocalContext.current

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
                        text = context.getString(R.string.home_currently_renting),
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }

                if (rentedDevices.isEmpty()) {
                    item {
                        Text(
                            text = context.getString(R.string.home_no_devices_currently_renting),
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
                        text = context.getString(R.string.home_devices_currently_renting_out),
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
                            text = context.getString(R.string.home_no_devices_currently_renting_out),
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
