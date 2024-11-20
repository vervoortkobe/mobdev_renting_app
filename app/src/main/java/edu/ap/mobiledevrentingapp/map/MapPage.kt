package edu.ap.mobiledevrentingapp.map

import android.graphics.drawable.Drawable
import androidx.appcompat.content.res.AppCompatResources
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.utsman.osmandcompose.Marker
import com.utsman.osmandcompose.OpenStreetMap
import com.utsman.osmandcompose.rememberCameraState
import com.utsman.osmandcompose.rememberMarkerState
import edu.ap.mobiledevrentingapp.R
import org.osmdroid.util.GeoPoint

@Composable
fun MapPage(navController: NavController) {
    val context = LocalContext.current

    val cameraState = rememberCameraState {
        geoPoint = GeoPoint(50.85, 4.35)
        zoom = 12.0
    }

    val icon: Drawable? by remember {
        mutableStateOf(AppCompatResources.getDrawable(context, R.drawable.custom_marker_icon))
    }

    var geoPoints by remember { mutableStateOf<List<GeoPoint>>(emptyList()) }

    LaunchedEffect(Unit) {
        Coordinates.fetchAllDevices { points ->
            geoPoints = points
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        OpenStreetMap(
            modifier = Modifier.fillMaxSize(),
            cameraState = cameraState
        ) {
            geoPoints.forEachIndexed { index, geoPoint ->
                val markerState = rememberMarkerState(
                    geoPoint = geoPoint,
                    rotation = 0f
                )

                Marker(
                    state = markerState,
                    icon = icon,
                    title = "Device $index",
                    snippet = "Lat: ${geoPoint.latitude}, Lon: ${geoPoint.longitude}"
                ) {
                    Column(
                        modifier = Modifier
                            .size(100.dp)
                            .background(color = Color.Gray, shape = RoundedCornerShape(7.dp)),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = it.title)
                        Text(text = it.snippet, fontSize = 10.sp)
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
    }
}