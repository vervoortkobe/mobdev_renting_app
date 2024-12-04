package edu.ap.mobiledevrentingapp.devices

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import edu.ap.mobiledevrentingapp.firebase.Device
import edu.ap.mobiledevrentingapp.firebase.DeviceCategory
import edu.ap.mobiledevrentingapp.firebase.FirebaseService
import android.location.Location
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.navigation.NavController
import edu.ap.mobiledevrentingapp.R
import edu.ap.mobiledevrentingapp.ui.theme.Yellow40

@Composable
fun DisplayDevicesWithImages(navController: NavController) {
    val context = LocalContext.current
    var devicesList by remember { mutableStateOf<List<Device>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategoryIndex by remember { mutableIntStateOf(0) }
    var showFilters by remember { mutableStateOf(false) }
    var maxDistance by remember { mutableFloatStateOf(100f) }
    var userLocation by remember { mutableStateOf<Pair<Double, Double>?>(null) }
    var userLocationObject by remember { mutableStateOf<Location?>(null) }
    var currentUserId by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        FirebaseService.getCurrentUser { success, document, _ ->
            if (success && document != null) {
                val lat = document.getDouble("latitude") ?: 0.0
                val lon = document.getDouble("longitude") ?: 0.0
                userLocation = Pair(lat, lon)
                
                currentUserId = document.getString("userId")
                
                val location = Location("").apply {
                    latitude = lat
                    longitude = lon
                }
                userLocationObject = location
            }
        }
    }

    LaunchedEffect(Unit) {
        FirebaseService.getAllDevices { success, devices, error ->
            if (success) {
                isLoading = false
                devicesList = devices
            } else {
                Toast.makeText(
                    context,
                    error ?: context.getString(R.string.devices_error_loading_devices),
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 8.dp, end = 8.dp, top = 2.dp, bottom = 2.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            SearchBar(
                searchQuery = searchQuery,
                onSearchQueryChange = { searchQuery = it },
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 4.dp)
                    .height(52.dp)
                    .background(Color.White, RoundedCornerShape(8.dp))
                    .border(1.dp, Color.Gray, RoundedCornerShape(8.dp))
            )

            IconButton(
                onClick = { showFilters = !showFilters },
                Modifier
                    .border(1.dp, Color.Gray, RoundedCornerShape(8.dp))
                    .size(52.dp),
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = context.getString(R.string.devices_filter_button),
                    tint = Yellow40,
                    modifier = Modifier.size(32.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        AnimatedVisibility(
            visible = showFilters,
            enter = expandVertically(),
            exit = shrinkVertically()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 8.dp, end = 8.dp)
            ) {
                DropdownListDevices(
                    categories = DeviceCategory.entries,
                    selectedCategoryIndex = selectedCategoryIndex,
                    onCategorySelected = { selectedCategoryIndex = it },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(0.dp))

                Text(
                    text = if (maxDistance < 100f) 
                        "Maximum distance: ${maxDistance.toInt()} km"
                    else 
                        context.getString(R.string.devices_no_distance_limit),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(start = 4.dp)
                )

                Spacer(modifier = Modifier.height(0.dp))

                Slider(
                    value = maxDistance,
                    onValueChange = { maxDistance = it },
                    valueRange = 0f..100f,
                    colors = SliderDefaults.colors(
                        thumbColor = Yellow40,
                        activeTrackColor = Yellow40
                    )
                )

                Spacer(modifier = Modifier.height(4.dp))
            }
        }

        val filteredDevices = devicesList.filter { device ->
            val matchesCategory = selectedCategoryIndex == 0 || 
                device.category == DeviceCategory.entries[selectedCategoryIndex - 1].name
            val matchesQuery = device.deviceName.contains(searchQuery, ignoreCase = true)
            val matchesDistance = if (maxDistance < 100f) {
                userLocation?.let { (userLat, userLon) ->
                    calculateDistance(
                        userLat, userLon,
                        device.latitude, device.longitude
                    ) <= maxDistance
                } ?: true
            } else {
                true
            }
            val isNotOwnDevice = device.ownerId != currentUserId

            matchesCategory && matchesQuery && matchesDistance && isNotOwnDevice
        }.sortedBy { device ->
            userLocation?.let { (userLat, userLon) ->
                calculateDistance(
                    userLat, userLon,
                    device.latitude, device.longitude
                )
            } ?: Float.MAX_VALUE
        }

        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Yellow40)
            }
        } else if (filteredDevices.isEmpty()) {
            Text(
                text = context.getString(R.string.devices_no_devices_found),
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(0.dp)
            ) {
                items(filteredDevices) { device ->
                    userLocationObject?.let { location ->
                        DeviceCard(
                            device = device,
                            userLocation = location,
                            navController = navController
                        )
                    }
                }
            }
        }
    }
}

private fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Float {
    val results = FloatArray(1)
    Location.distanceBetween(lat1, lon1, lat2, lon2, results)
    return results[0] / 1000
}