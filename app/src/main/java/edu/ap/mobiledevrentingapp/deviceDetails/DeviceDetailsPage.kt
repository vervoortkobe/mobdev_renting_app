package edu.ap.mobiledevrentingapp.deviceDetails

import android.graphics.Bitmap
import android.widget.Toast
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import edu.ap.mobiledevrentingapp.firebase.*
import edu.ap.mobiledevrentingapp.ui.theme.Yellow40
import kotlinx.coroutines.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateRangePickerModal(
    onDateRangeSelected: (Pair<Long?, Long?>) -> Unit,
    onDismiss: () -> Unit,
    disabledDates: List<Date>
) {
    val dateRangePickerState = rememberDateRangePickerState(
        initialSelectedStartDateMillis = null,
        initialSelectedEndDateMillis = null,
        yearRange = IntRange(Calendar.getInstance().get(Calendar.YEAR), 
                           Calendar.getInstance().get(Calendar.YEAR) + 1)
    )

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    onDateRangeSelected(
                        Pair(
                            dateRangePickerState.selectedStartDateMillis,
                            dateRangePickerState.selectedEndDateMillis
                        )
                    )
                    onDismiss()
                }
            ) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    ) {
        DateRangePicker(
            state = dateRangePickerState,
            title = { Text("Select rental period") },
            headline = { Text("Select start and end date") },
            showModeToggle = false,
            modifier = Modifier
                .fillMaxWidth()
                .height(500.dp)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeviceDetailsPage(
    navController: NavController,
    deviceId: String
) {
    var device by remember { mutableStateOf<Device?>(null) }
    var images by remember { mutableStateOf<List<Pair<String, Bitmap>>>(emptyList()) }
    var owner by remember { mutableStateOf<User?>(null) }
    var showFullScreenImage by remember { mutableStateOf(false) }
    var currentImageIndex by remember { mutableIntStateOf(0) }
    var startDate by remember { mutableStateOf<Date?>(null) }
    var endDate by remember { mutableStateOf<Date?>(null) }
    var existingRentals by remember { mutableStateOf<List<Rental>>(emptyList()) }
    var showPaymentDialog by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var userRental by remember { mutableStateOf<Rental?>(null) }
    var showDatePicker by remember { mutableStateOf(false) }
    var currentUser by remember { mutableStateOf<User?>(null) }
    val context = LocalContext.current
    val pagerState = rememberPagerState(pageCount = { images.size })
    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    var userLocation by remember { mutableStateOf<Pair<Double, Double>?>(null) }

    val distance = remember(userLocation, device) {
        userLocation?.let { (lat, lon) ->
            device?.let { dev ->
                calculateDistance(
                    lat,
                    lon,
                    dev.latitude,
                    dev.longitude
                )
            }
        } ?: 0f
    }

    LaunchedEffect(Unit) {
        FirebaseService.getCurrentUser { success, document, _ ->
            if (success && document != null) {
                val lat = document.getDouble("latitude") ?: 0.0
                val lon = document.getDouble("longitude") ?: 0.0
                userLocation = Pair(lat, lon)
            }
        }
    }

    // Load current user data
    LaunchedEffect(Unit) {
        FirebaseService.getCurrentUser { success, document, _ ->
            if (success && document != null) {
                currentUser = document.toObject(User::class.java)
            } else {
                Toast.makeText(
                    context,
                    "Your user data couldn't be loaded.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    LaunchedEffect(deviceId) {
        try {
            coroutineScope {
                // Load device details
                val deviceDeferred = async {
                    FirebaseService.getDeviceById(deviceId) { success, document, _ ->
                        if (success && document != null) {
                            device = document.toObject(Device::class.java)
                        }
                    }
                }
                
                deviceDeferred.await()
                
                device?.let { dev ->
                    // Load owner details
                    launch {
                        FirebaseService.getUserById(dev.ownerId) { success, doc, _ ->
                            if (success && doc != null) {
                                owner = doc.toObject(User::class.java)
                            }
                        }
                    }

                    // Load device images
                    val imageResults = dev.imageIds.map { imageId ->
                        async {
                            FirebaseService.getImageById(imageId)
                        }
                    }.awaitAll()
                    images = imageResults.filterNotNull()

                    // Load existing rentals
                    launch {
                        FirebaseService.getRentalsByDeviceId(deviceId) { rentals ->
                            existingRentals = rentals
                            // Find if current user has rented this device
                            userRental = rentals.find { it.renterId == currentUser?.userId }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            println("Error loading device details: ${e.message}")
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = { navController.popBackStack() }) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                            }
                            Text(device?.deviceName ?: "", fontWeight = FontWeight.Bold)
                        }
                        Text(
                            text = device?.category?.let { AppUtil.convertUppercaseToTitleCase(it) } ?: "",
                            color = Yellow40,
                            modifier = Modifier.padding(end = 16.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            // Image Slider
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
                    .clickable { showFullScreenImage = true }
            ) {
                if (images.isNotEmpty()) {
                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier.fillMaxSize()
                    ) { page ->
                        Image(
                            bitmap = images[page].second.asImageBitmap(),
                            contentDescription = "Device image",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                }
            }

            // Price per day
            Text(
                text = "€${device?.price ?: 0}/day",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(16.dp)
            )

            // Description
            Text(
                text = device?.description ?: "",
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Owner details
            owner?.let { ownerData ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ownerData.profileImage.let { profileImageString ->
                        AppUtil.decode(profileImageString)?.let { bitmap ->
                            Image(
                                bitmap = bitmap.asImageBitmap(),
                                contentDescription = "Owner profile",
                                modifier = Modifier
                                    .size(50.dp)
                                    .clip(CircleShape)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = ownerData.fullName,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Location
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.LocationOn, contentDescription = "Location")
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "${distance.toInt()}km • ${owner?.city ?: ""}")
            }

            // User's existing rental
            userRental?.let { rental ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                ) {
                    Text(
                        text = "You already rented this item from ${rental.startDate} to ${rental.endDate}!",
                        modifier = Modifier.padding(16.dp),
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Calendar and Booking Section
            if (userRental == null) {
                if (startDate != null && endDate != null) {
                    val formatter = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                    Text(
                        text = "Selected period: ${formatter.format(startDate!!)} - ${formatter.format(endDate!!)}",
                        modifier = Modifier.padding(horizontal = 16.dp),
                        fontWeight = FontWeight.Bold
                    )
                }

                Button(
                    onClick = {
                        if (currentUser != null) {
                            showDatePicker = true
                        } else {
                            Toast.makeText(
                                context,
                                "Please wait while we load your user data.",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        if (startDate != null && endDate != null)
                            "Pay €${calculateTotalPrice(device?.price?.toDoubleOrNull() ?: 0.0, startDate!!, endDate!!)}"
                        else
                            "Select duration"
                    )
                }
            }
        }
    }

    if (showDatePicker) {
        DateRangePickerModal(
            onDateRangeSelected = { (start, end) ->
                if (start != null && end != null) {
                    startDate = Date(start)
                    endDate = Date(end)
                    showPaymentDialog = true
                }
            },
            onDismiss = { showDatePicker = false },
            disabledDates = getDisabledDates(existingRentals)
        )
    }

    // Payment Confirmation Dialog
    if (showPaymentDialog) {
        AlertDialog(
            onDismissRequest = { showPaymentDialog = false },
            title = { Text("Confirm Rental") },
            text = {
                Text(
                    "Total amount: €${calculateTotalPrice(device?.price?.toDoubleOrNull() ?: 0.0, startDate!!, endDate!!)}",
                    fontSize = 18.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showPaymentDialog = false
                        isLoading = true
                        processRental(
                            deviceId = deviceId,
                            ownerId = device?.ownerId ?: "",
                            renterId = currentUser?.userId ?: "",
                            startDate = startDate!!,
                            endDate = endDate!!,
                            onComplete = {
                                isLoading = false
                                navController.popBackStack()
                            }
                        )
                    }
                ) {
                    Text("Confirm Payment")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPaymentDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Loading Dialog
    if (isLoading) {
        Dialog(onDismissRequest = {}) {
            CircularProgressIndicator()
        }
    }

    // Full screen image viewer
    if (showFullScreenImage) {
        Dialog(
            onDismissRequest = { showFullScreenImage = false }
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black)
            ) {
                HorizontalPager(
                    state = rememberPagerState(
                        initialPage = pagerState.currentPage,
                        pageCount = { images.size }
                    )
                ) { page ->
                    Image(
                        bitmap = images[page].second.asImageBitmap(),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Fit
                    )
                }
            }
        }
    }
}

private fun calculateTotalPrice(pricePerDay: Double, startDate: Date, endDate: Date): Double {
    val diffInMillis = endDate.time - startDate.time
    val days = (diffInMillis / (1000 * 60 * 60 * 24)) + 1
    return pricePerDay * days
}

private fun processRental(
    deviceId: String,
    ownerId: String,
    renterId: String,
    startDate: Date,
    endDate: Date,
    onComplete: () -> Unit
) {
    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val rental = Rental(
        rentalId = UUID.randomUUID().toString(),
        deviceId = deviceId,
        ownerId = ownerId,
        renterId = renterId,
        startDate = dateFormat.format(startDate),
        endDate = dateFormat.format(endDate)
    )

    FirebaseService.createRental(rental) { success ->
        if (success) {
            onComplete()
        }
    }
}

private fun getDisabledDates(rentals: List<Rental>): List<Date> {
    val disabledDates = mutableListOf<Date>()
    val calendar = Calendar.getInstance()
    val today = calendar.time
    
    // Add past dates
    calendar.add(Calendar.YEAR, -1)
    val startDate = calendar.time
    calendar.time = today
    
    var currentDate = startDate
    while (currentDate.before(today)) {
        disabledDates.add(currentDate.clone() as Date)  // Clone to avoid modifying the same instance
        calendar.time = currentDate
        calendar.add(Calendar.DAY_OF_MONTH, 1)
        currentDate = calendar.time
    }

    // Add rented dates
    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    rentals.forEach { rental ->
        val rentalStart = dateFormat.parse(rental.startDate)
        val rentalEnd = dateFormat.parse(rental.endDate)
        if (rentalStart != null && rentalEnd != null) {
            calendar.time = rentalStart
            while (!calendar.time.after(rentalEnd)) {
                disabledDates.add(calendar.time.clone() as Date)  // Clone to avoid modifying the same instance
                calendar.add(Calendar.DAY_OF_MONTH, 1)
            }
        }
    }
    
    return disabledDates
}

private fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Float {
    val results = FloatArray(1)
    android.location.Location.distanceBetween(lat1, lon1, lat2, lon2, results)
    return results[0] / 1000 // Convert meters to kilometers
}