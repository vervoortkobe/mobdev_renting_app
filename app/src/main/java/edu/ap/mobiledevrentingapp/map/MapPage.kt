package edu.ap.mobiledevrentingapp.map

import android.graphics.drawable.Drawable
import androidx.appcompat.content.res.AppCompatResources
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.utsman.osmandcompose.Marker
import com.utsman.osmandcompose.OpenStreetMap
import com.utsman.osmandcompose.rememberCameraState
import com.utsman.osmandcompose.rememberMarkerState
import edu.ap.mobiledevrentingapp.R
import edu.ap.mobiledevrentingapp.firebase.AppUtil
import edu.ap.mobiledevrentingapp.firebase.FirebaseService
import edu.ap.mobiledevrentingapp.firebase.User
import edu.ap.mobiledevrentingapp.ui.theme.Yellow40
import org.osmdroid.util.GeoPoint

@Composable
fun MapPage(navController: NavController) {
    val context = LocalContext.current
    var accountLocation by remember { mutableStateOf<GeoPoint?>(null) }
    var userProfileImage by remember { mutableStateOf<String?>(null) }
    var markers by remember { mutableStateOf<List<MarkerInfo>>(emptyList()) }

    val cameraState = rememberCameraState {
        zoom = 12.0
    }

    val deviceIcon: Drawable? by remember {
        mutableStateOf(AppCompatResources.getDrawable(context, R.drawable.custom_marker_icon))
    }

    val userIcon: Drawable? by remember {
        mutableStateOf(AppCompatResources.getDrawable(context, R.drawable.user_marker_icon))
    }

    LaunchedEffect(Unit) {
        FirebaseService.getCurrentUser { success, document, _ ->
            if (success && document != null) {
                val user = document.toObject(User::class.java)
                user?.let {
                    accountLocation = GeoPoint(it.latitude, it.longitude)
                    userProfileImage = it.profileImage
                    cameraState.geoPoint = accountLocation as GeoPoint
                    cameraState.zoom = 15.0
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        Coordinates.fetchAllDevices { markerInfoList ->
            markers = markerInfoList
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        OpenStreetMap(
            modifier = Modifier.fillMaxSize(),
            cameraState = cameraState
        ) {
            markers.forEach { markerInfo ->
                val markerState = rememberMarkerState(
                    geoPoint = markerInfo.geoPoint,
                    rotation = 0f
                )

                Marker(
                    state = markerState,
                    icon = deviceIcon,
                    title = "Device ${markerInfo.deviceId}",
                    snippet = "Lat: ${markerInfo.geoPoint.latitude}, Lon: ${markerInfo.geoPoint.longitude}"
                ) {
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .background(Color.White, CircleShape)
                            .border(1.dp, Yellow40, CircleShape)
                            .clickable { 
                                navController.navigate("device_details/${markerInfo.deviceId}")
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        AppUtil.decode(markerInfo.imageUrl)?.let { it ->
                            Image(
                                bitmap = it.asImageBitmap(),
                                contentDescription = "Device Image",
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(CircleShape)
                                    .background(Color.White, CircleShape)
                                    .border(1.dp, Color.Gray, CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }
                }
            }

            accountLocation?.let { location ->
                val userMarkerState = rememberMarkerState(
                    geoPoint = location,
                    rotation = 0f
                )

                Marker(
                    state = userMarkerState,
                    icon = userIcon,
                    title = "Your Location",
                    snippet = "Lat: ${location.latitude}, Lon: ${location.longitude}"
                ) {
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .background(Color.White, CircleShape)
                            .border(1.dp, Color.Black, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        userProfileImage?.let { imageBase64 ->
                            AppUtil.decode(imageBase64)?.let { bitmap ->
                                Image(
                                    bitmap = bitmap.asImageBitmap(),
                                    contentDescription = "Your Profile Image",
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(CircleShape)
                                        .background(Color.White, CircleShape)
                                        .border(1.dp, Color.Gray, CircleShape),
                                    contentScale = ContentScale.Crop
                                )
                            }
                        }
                    }
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

        IconButton(
            onClick = {
                accountLocation?.let { location ->
                    cameraState.geoPoint = location
                    cameraState.zoom = 15.0
                }
            },
            modifier = Modifier
                .padding(16.dp)
                .align(Alignment.TopEnd)
                .size(48.dp)
                .background(Color.White, CircleShape)
                .border(1.dp, Color.Gray, CircleShape)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.user_marker_icon),
                    contentDescription = "My Location",
                    tint = Color.Unspecified,
                    modifier = Modifier.size(52.dp)
                        .padding(start = 8.dp, top = 8.dp)
                )
            }
        }
    }
}