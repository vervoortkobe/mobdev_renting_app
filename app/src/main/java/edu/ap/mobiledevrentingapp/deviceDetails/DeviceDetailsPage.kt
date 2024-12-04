package edu.ap.mobiledevrentingapp.deviceDetails

import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.util.Log
import android.widget.Toast
import androidx.appcompat.content.res.AppCompatResources
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.navigation.NavController
import com.utsman.osmandcompose.rememberCameraState
import edu.ap.mobiledevrentingapp.R
import edu.ap.mobiledevrentingapp.firebase.AppUtil
import edu.ap.mobiledevrentingapp.firebase.Device
import edu.ap.mobiledevrentingapp.firebase.FirebaseService
import edu.ap.mobiledevrentingapp.firebase.Rental
import edu.ap.mobiledevrentingapp.firebase.User
import edu.ap.mobiledevrentingapp.ui.theme.Yellow40
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.osmdroid.util.GeoPoint
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeviceDetailsPage(
    navController: NavController,
    deviceId: String
) {
    var device by remember { mutableStateOf<Device?>(null) }
    var images by remember { mutableStateOf<List<Bitmap>>(emptyList()) }
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
    var isLoadingOwner by remember { mutableStateOf(true) }

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
        FirebaseService.getRentalsByDeviceId(deviceId) { rentals ->
            existingRentals = rentals
            userRental = rentals.find { it.renterId == currentUser?.userId }
        }

        FirebaseService.getDeviceById(deviceId) { success, document, _ ->
            if (success && document != null) {
                device = document.toObject(Device::class.java)
                
                device?.let { dev ->
                    FirebaseService.getUserById(dev.ownerId) { ownerSuccess, ownerDoc, ownerError ->
                        if (ownerSuccess && ownerDoc != null) {
                            owner = ownerDoc.toObject(User::class.java)
                        } else {
                            Log.e("DeviceDetailsPage", "Failed to load owner: $ownerError")
                        }
                        isLoadingOwner = false
                    }
                }

                device?.let { dev ->
                    images = dev.images.mapNotNull { imageString ->
                        AppUtil.decode(imageString)
                    }
                }
            }
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
                            Spacer(modifier = Modifier.width(2.dp))
                            IconButton(onClick = { navController.popBackStack() }) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                device?.deviceName ?: "",
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Text(
                            text = device?.category?.let { AppUtil.convertUppercaseToTitleCase(it) } ?: "",
                            fontSize = 18.sp,
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
            userRental?.let { rental ->
                val currentDate = Date()
                val rentalStartDate = dateFormat.parse(rental.startDate)
                val rentalEndDate = dateFormat.parse(rental.endDate)
                
                if (rentalStartDate != null && rentalEndDate != null &&
                    currentDate.after(rentalStartDate) && currentDate.before(rentalEndDate)) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                            .border(
                                width = 1.dp,
                                color = MaterialTheme.colorScheme.outline,
                                shape = RoundedCornerShape(8.dp)
                            ),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
                        )
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Text(
                                    text = "Current Ongoing Rental Period",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = "${AppUtil.formatDate(dateFormat.parse(userRental!!.startDate)!!)} - ${AppUtil.formatDate(dateFormat.parse(userRental!!.endDate)!!)}",
                                            style = MaterialTheme.typography.bodyLarge,
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    Text(
                                        text = "€${AppUtil.calculateTotalPrice(device?.price?.toDoubleOrNull() ?: 0.0,
                                            dateFormat.parse(userRental!!.startDate)!!,
                                            dateFormat.parse(userRental!!.endDate)!!
                                        )}",
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Yellow40
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(350.dp)
                    .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 16.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outline,
                        shape = RoundedCornerShape(8.dp)
                    )
            ) {
                if (images.isNotEmpty()) {
                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    ) { page ->
                        Image(
                            bitmap = images[page].asImageBitmap(),
                            contentDescription = "Device image",
                            modifier = Modifier
                                .fillMaxSize()
                                .clickable { 
                                    currentImageIndex = page
                                    showFullScreenImage = true 
                                },
                            contentScale = ContentScale.Fit
                        )
                    }

                    Text(
                        text = "${pagerState.currentPage + 1} / ${images.size}",
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 24.dp)
                            .background(
                                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
                                shape = RoundedCornerShape(4.dp)
                            )
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 16.dp)
            ) {
                Icon(
                    Icons.Default.ShoppingCart,
                    contentDescription = "Price",
                    tint = Yellow40,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "€${device?.price ?: 0}/day",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Yellow40
                )
            }

            Text(
                text = device?.description ?: "",
                modifier = Modifier.padding(16.dp)
            )

            if (existingRentals.any { it.renterId == currentUser?.userId }) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .border(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.outline,
                            shape = RoundedCornerShape(8.dp)
                        ),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Your Rental Periods",
                            style = MaterialTheme.typography.titleMedium,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        existingRentals.filter { it.renterId == currentUser?.userId }.forEach { rental ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row {
                                    Text(
                                        text = "${AppUtil.formatDate(dateFormat.parse(rental.startDate)!!)} - ${AppUtil.formatDate(dateFormat.parse(rental.endDate)!!)}",
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                }
                                Text(
                                    text = "€${AppUtil.calculateTotalPrice(
                                        device?.price?.toDoubleOrNull() ?: 0.0,
                                        dateFormat.parse(rental.startDate)!!,
                                        dateFormat.parse(rental.endDate)!!
                                    )}",
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Yellow40
                                )
                            }
                            if (existingRentals.last { it.renterId == currentUser?.userId } != rental) {
                                HorizontalDivider(
                                    modifier = Modifier.padding(vertical = 8.dp),
                                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                                )
                            }
                        }
                    }
                }
            }

            if (isLoadingOwner) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .padding(16.dp)
                        .align(Alignment.CenterHorizontally),
                    color = Yellow40
                )
            } else {
                owner?.let { ownerData ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .border(
                                width = 1.dp,
                                color = MaterialTheme.colorScheme.outline,
                                shape = RoundedCornerShape(8.dp)
                            )
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(70.dp)
                                    .clip(CircleShape)
                                    .background(Color.LightGray)
                            ) {
                                ownerData.profileImage.takeIf { it.isNotEmpty() }?.let { profileImageString ->
                                    AppUtil.decode(profileImageString)?.let { bitmap ->
                                        Image(
                                            bitmap = bitmap.asImageBitmap(),
                                            contentDescription = "Owner profile",
                                            modifier = Modifier.fillMaxSize(),
                                            contentScale = ContentScale.Crop
                                        )
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text(
                                    text = ownerData.fullName,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 20.sp
                                )
                                if (existingRentals.any { it.renterId == currentUser?.userId }) {
                                    Text(
                                        text = ownerData.phoneNumber,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontSize = 18.sp
                                    )
                                }
                                Text(
                                    text = if (existingRentals.any { it.renterId == currentUser?.userId }) {
                                        "${ownerData.streetName} ${ownerData.addressNr}"
                                    } else {
                                        ownerData.city
                                    },
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 18.sp
                                )
                                if (existingRentals.any { it.renterId == currentUser?.userId }) {
                                    Text(
                                        text = "${ownerData.zipCode} ${ownerData.city}",
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontSize = 18.sp
                                    )
                                }
                            }
                        }
                    }
                } ?: run {
                    Text(
                        text = "Owner information unavailable",
                        modifier = Modifier.padding(16.dp),
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.LocationOn, contentDescription = "Location")
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "${distance.toInt()}km • ${owner?.city  ?: ""}", fontSize = 18.sp, fontWeight = FontWeight.Normal)
            }

            device?.let { dev ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                        .padding(start = 16.dp, end = 16.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .border(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.outline,
                            shape = RoundedCornerShape(8.dp)
                        )
                ) {
                    val cameraState = rememberCameraState {
                        geoPoint = GeoPoint(dev.latitude, dev.longitude)
                        zoom = 15.0
                    }

                    val deviceIcon: Drawable? by remember {
                        mutableStateOf(AppCompatResources.getDrawable(context, R.drawable.custom_marker_icon))
                    }

                    val userIcon: Drawable? by remember {
                        mutableStateOf(AppCompatResources.getDrawable(context, R.drawable.user_marker_icon))
                    }

                    DeviceDetailsMap(
                        cameraState = cameraState,
                        device = dev,
                        userLocation = userLocation,
                        deviceIcon = deviceIcon,
                        userIcon = userIcon
                    )
                }
            }

            if (showPaymentDialog) {
                AlertDialog(
                    onDismissRequest = { showPaymentDialog = false },
                    title = { 
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Confirm Rental", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        }
                    },
                    text = {
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "Total amount: €${AppUtil.calculateTotalPrice(device?.price?.toDoubleOrNull() ?: 0.0, startDate!!, endDate!!)}",
                                fontSize = 18.sp,
                                textAlign = TextAlign.Center
                            )
                        }
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
                                        CoroutineScope(Dispatchers.Main).launch {
                                            delay(3000)
                                            isLoading = false
                                            // Reset date selection
                                            startDate = null
                                            endDate = null
                                            showDatePicker = false
                                            // Refresh rentals instead of navigating
                                            FirebaseService.getRentalsByDeviceId(deviceId) { rentals ->
                                                existingRentals = rentals
                                                userRental = rentals.find { it.renterId == currentUser?.userId }
                                            }
                                        }
                                    }
                                )
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Yellow40
                            )
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

            if (isLoading) {
                Dialog(
                    onDismissRequest = { },
                    properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false)
                ) {
                    Box(
                        modifier = Modifier
                            .background(
                                MaterialTheme.colorScheme.surface,
                                shape = RoundedCornerShape(16.dp)
                            )
                            .padding(24.dp)
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            CircularProgressIndicator(color = Yellow40)
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                "Processing payment...",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            if (showFullScreenImage) {
                Dialog(
                    onDismissRequest = { showFullScreenImage = false },
                    properties = DialogProperties(
                        dismissOnBackPress = true,
                        dismissOnClickOutside = true,
                        usePlatformDefaultWidth = false
                    )
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.9f))
                            .clickable { showFullScreenImage = false }
                    ) {
                        val fullScreenPagerState = rememberPagerState(
                            initialPage = currentImageIndex,
                            pageCount = { images.size }
                        )
                        
                        HorizontalPager(
                            state = fullScreenPagerState,
                            modifier = Modifier.fillMaxSize()
                        ) { page ->
                            Image(
                                bitmap = images[page].asImageBitmap(),
                                contentDescription = null,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(16.dp),
                                contentScale = ContentScale.Fit
                            )
                        }
                        
                        // Image counter for full screen view
                        Text(
                            text = "${fullScreenPagerState.currentPage + 1} / ${images.size}",
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(bottom = 32.dp)
                                .background(
                                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
                                    shape = RoundedCornerShape(4.dp)
                                )
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                            color = Color.White
                        )
                    }
                }
            }

            if (startDate != null && endDate != null) {
                val formatter = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Selected period: ${formatter.format(startDate!!)} - ${formatter.format(endDate!!)}",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
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
                    .padding(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Yellow40
                )
            ) {
                Text(
                    text = if (startDate != null && endDate != null) {
                        "Pay €${
                            AppUtil.calculateTotalPrice(
                                device?.price?.toDoubleOrNull() ?: 0.0,
                                startDate!!,
                                endDate!!
                            )
                        }"
                    } else {
                        "Select duration to rent"
                    },
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )

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
    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val disabledDates = mutableListOf<Date>()
    
    rentals.forEach { rental ->
        val startDate = dateFormat.parse(rental.startDate)
        val endDate = dateFormat.parse(rental.endDate)
        
        if (startDate != null && endDate != null) {
            val calendar = Calendar.getInstance()
            calendar.time = startDate
            
            while (!calendar.time.after(endDate)) {
                disabledDates.add(calendar.time)
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