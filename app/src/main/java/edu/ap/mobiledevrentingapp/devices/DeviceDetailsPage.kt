package edu.ap.mobiledevrentingapp.devices

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
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
import androidx.navigation.NavController
import edu.ap.mobiledevrentingapp.firebase.AppUtil
import edu.ap.mobiledevrentingapp.firebase.Device
import edu.ap.mobiledevrentingapp.firebase.FirebaseService
import edu.ap.mobiledevrentingapp.firebase.User
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.awaitAll

@Composable
fun DeviceDetailsPage(navController: NavController, deviceId: String) {
    var device by remember { mutableStateOf<Device?>(null) }
    var images by remember { mutableStateOf<List<Pair<String, Bitmap>>>(emptyList()) }
    var owner by remember { mutableStateOf<User?>(null) }
    LocalContext.current
    val pagerState = rememberPagerState(pageCount = { images.size })

    LaunchedEffect(deviceId) {
        try {
            coroutineScope {
                val deviceDeferred = async {
                    FirebaseService.getDeviceById(deviceId) { success, document, _ ->
                        if (success && document != null) {
                            device = document.toObject(Device::class.java)
                        }
                    }
                }
                
                deviceDeferred.await()
                
                device?.let { dev ->
                    launch {
                        FirebaseService.getUserById(dev.ownerId) { success, doc, _ ->
                            if (success && doc != null) {
                                owner = doc.toObject(User::class.java)
                            }
                        }
                    }

                    val imageResults = dev.imageIds.map { imageId ->
                        async {
                            FirebaseService.getImageById(imageId)
                        }
                    }.awaitAll()

                    images = imageResults.filterNotNull()
                }
            }
        } catch (e: Exception) {
            println("Error loading device details: ${e.message}")
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            if (images.isNotEmpty()) {
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                ) { page ->
                    Image(
                        bitmap = images[page].second.asImageBitmap(),
                        contentDescription = "Device image",
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Crop
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            device?.let { dev ->
                Text(
                    text = dev.deviceName.capitalize(),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = AppUtil.convertUppercaseToTitleCase(dev.category),
                    color = Color.Gray
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.ShoppingCart,
                        contentDescription = "Price",
                        tint = Color.Gray
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "€${dev.price} / day")
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = "Location",
                        tint = Color.Gray
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    owner?.let { Text(text = it.city.capitalize()) }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Description",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(text = dev.description.replaceFirstChar { it.uppercase() })

                Spacer(modifier = Modifier.height(16.dp))

                owner?.let { ownerData ->
                    Text(
                        text = "Owner Information",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(text = "Name: ${ownerData.fullName.split(" ").first().capitalize()} ${ownerData.fullName.split(" ").last().capitalize()}")
                    Text(text = "Phone: ${ownerData.phoneNumber}")
                }
            }
        }

        IconButton(
            onClick = { navController.popBackStack() },
            modifier = Modifier
                .padding(16.dp)
                .size(48.dp)
                .background(Color.White, CircleShape)
                .border(1.dp, Color.Gray, CircleShape)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = Color.Black
            )
        }
    }
} 