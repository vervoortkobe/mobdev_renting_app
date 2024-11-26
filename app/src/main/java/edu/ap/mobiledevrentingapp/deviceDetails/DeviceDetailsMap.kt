package edu.ap.mobiledevrentingapp.deviceDetails

import android.graphics.drawable.Drawable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import com.utsman.osmandcompose.CameraState
import com.utsman.osmandcompose.MapProperties
import com.utsman.osmandcompose.Marker
import com.utsman.osmandcompose.OpenStreetMap
import com.utsman.osmandcompose.ZoomButtonVisibility
import com.utsman.osmandcompose.rememberMarkerState
import edu.ap.mobiledevrentingapp.firebase.Device
import org.osmdroid.util.GeoPoint

@Composable
fun DeviceDetailsMap(cameraState: CameraState, device: Device, userLocation: Pair<Double, Double>?, deviceIcon: Drawable?, userIcon: Drawable?) {
    OpenStreetMap(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures { }
            },
        cameraState = cameraState,
        properties = MapProperties(
            zoomButtonVisibility = ZoomButtonVisibility.NEVER // Disable +- buttons
        )
    ) {
        // Device marker
        val markerState = rememberMarkerState(
            geoPoint = GeoPoint(device.latitude, device.longitude),
            rotation = 0f
        )

        Marker(
            state = markerState,
            icon = deviceIcon,
            title = device.deviceName,
            snippet = "Lat: ${device.latitude}, Lon: ${device.longitude}"
        )

        // User location marker
        userLocation?.let { (lat, lon) ->
            val userMarkerState = rememberMarkerState(
                geoPoint = GeoPoint(lat, lon),
                rotation = 0f
            )

            Marker(
                state = userMarkerState,
                icon = userIcon,
                title = "Your Location",
                snippet = "Lat: $lat, Lon: $lon"
            )
        }
    }
}