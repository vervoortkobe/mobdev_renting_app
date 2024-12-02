package edu.ap.mobiledevrentingapp.home

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import com.google.android.gms.location.LocationServices
import edu.ap.mobiledevrentingapp.devices.DeviceCard
import edu.ap.mobiledevrentingapp.firebase.Device
import edu.ap.mobiledevrentingapp.firebase.FirebaseService
import kotlinx.coroutines.tasks.await

@Composable
fun HomePage(onLogout: () -> Unit, navController: NavController) {
    val context = LocalContext.current
    var userLocation by remember { mutableStateOf<Location?>(null) }
    var rentedDevices by remember { mutableStateOf<List<Device>>(emptyList()) }
    var myRentedDevices by remember { mutableStateOf<List<Device>>(emptyList()) }
    
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            // Get location if permission is granted
            val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
            if (ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                    userLocation = location
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        // Fetch current user
        val currentUser = FirebaseService.getCurrentUser()
        if (currentUser != null) {
            // Fetch rented devices for the current user
            rentedDevices = FirebaseService.getDevicesRentedByUser(currentUser)
            myRentedDevices = FirebaseService.getMyDevicesBeingRented(currentUser.uid)
        } else {
            // Handle case where user is not logged in
            rentedDevices = emptyList()
            myRentedDevices = emptyList()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Home",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            OutlinedButton(
                onClick = onLogout,
                content = {
                    Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "Logout")
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Log out")
                }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Devices I'm renting
        Text(
            text = "Devices I'm Renting",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            fontWeight = FontWeight.Bold
        )
        
        if (rentedDevices.isEmpty()) {
            Text(
                text = "You're not renting any devices",
                color = Color.Gray,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                items(rentedDevices) { device ->
                    userLocation?.let {
                        DeviceCard(
                            device = device,
                            userLocation = it,
                            navController = navController
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // My devices being rented
        Text(
            text = "My Devices Being Rented",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            fontWeight = FontWeight.Bold
        )

        if (myRentedDevices.isEmpty()) {
            Text(
                text = "None of your devices are being rented",
                color = Color.Gray
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                items(myRentedDevices) { device ->
                    userLocation?.let {
                        DeviceCard(
                            device = device,
                            userLocation = it,
                            navController = navController
                        )
                    }
                }
            }
        }
    }
}