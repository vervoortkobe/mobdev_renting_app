package edu.ap.mobiledevrentingapp.profile

import android.graphics.Bitmap
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import edu.ap.mobiledevrentingapp.R
import edu.ap.mobiledevrentingapp.firebase.AppUtil.decode
import edu.ap.mobiledevrentingapp.firebase.FirebaseService
import edu.ap.mobiledevrentingapp.ui.theme.Yellow40

@Composable
fun ProfilePage(navController: NavController, onLogout: () -> Unit) {
    val context = LocalContext.current
    var name by remember { mutableStateOf<String?>(null) }
    var phoneNumber by remember { mutableStateOf<String?>(null) }
    var streetName by remember { mutableStateOf<String?>(null) }
    var zipCode by remember { mutableStateOf<String?>(null) }
    var city by remember { mutableStateOf<String?>(null) }
    var adressNr by remember { mutableStateOf<String?>(null) }
    var ibanNumber by remember { mutableStateOf<String?>(null) }
    var totalAdress by remember { mutableStateOf<String?>(null) }
    var email by remember { mutableStateOf<String?>(null) }
    var country by remember { mutableStateOf<String?>(null) }
    var encodedImage by remember { mutableStateOf<String?>(null) }
    var profileBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var devices by remember { mutableStateOf<List<Map<String, Any>>?>(null) }

    LaunchedEffect(Unit) {
        FirebaseService.getCurrentUser { success, document, _ ->
            if (success && document != null) {
                name = document.getString("fullName")
                phoneNumber = document.getString("phoneNumber")
                streetName = document.getString("streetName")
                zipCode = document.getString("zipCode")
                city = document.getString("city")
                adressNr = document.getString("addressNr")
                ibanNumber = document.getString("ibanNumber")
                country = document.getString("country")
                totalAdress = "$streetName $adressNr $city $zipCode $country"
                email = FirebaseService.getCurrentUserEmail()
                encodedImage = document.getString("profileImage")
                profileBitmap = if (!encodedImage.isNullOrEmpty()) decode(encodedImage!!) else null

                val userId = document.getString("userId")
                if (!userId.isNullOrEmpty()) {
                    FirebaseService.getDevicesByUserId(userId) { succ, documents, _ ->
                        devices = if (succ) {
                            documents?.map { it.data ?: emptyMap() }
                        } else {
                            emptyList()
                        }
                    }
                }
            } else {
                profileBitmap = null
                Toast.makeText(
                    context,
                    context.getString(R.string.error_loading_user_data),
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 72.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                ProfileHeader(
                    profileBitmap = profileBitmap,
                    name = name,
                    email = email,
                    phoneNumber = phoneNumber,
                    totalAdress = totalAdress,
                    ibanNumber = ibanNumber,
                    onLogout
                )
            }

            item {
                Text(
                    text = context.getString(R.string.profile_your_devices),
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Start
                )
            }

            devices?.let { it ->
                items(it) { device ->
                    DeviceCardToDelete(device) { deviceId ->
                        FirebaseService.deleteDeviceById(deviceId) { success, error ->
                            if (success) {
                                Toast.makeText(context, context.getString(R.string.profile_device_deleted), Toast.LENGTH_SHORT).show()
                                devices = devices?.filterNot { it["deviceId"] == deviceId }
                            } else {
                                Toast.makeText(context, "${context.getString(R.string.profile_failed_device_delete)} $error", Toast.LENGTH_LONG).show()
                            }
                        }
                    }
                }
            }
        }

        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp)
                .size(50.dp)
                .clip(CircleShape)
                .background(Yellow40)
                .clickable {
                    navController.navigate("profileSettings")
                }
        ) {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = context.getString(R.string.profile_settings),
                tint = Color.Black,
                modifier = Modifier.align(Alignment.Center)
            )
        }
    }
}

@Composable
fun ProfileHeader(
    profileBitmap: Bitmap?,
    name: String?,
    email: String?,
    phoneNumber: String?,
    totalAdress: String?,
    ibanNumber: String?,
    onLogout: () -> Unit
) {
    val context = LocalContext.current

    Box(
        modifier = Modifier
            .size(100.dp)
            .clip(RoundedCornerShape(50.dp))
            .border(2.dp, Color.Gray, RoundedCornerShape(50.dp))
    ) {
        if (profileBitmap != null) {
            Image(
                bitmap = profileBitmap.asImageBitmap(),
                contentDescription = context.getString(R.string.profile_image),
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(50.dp)),
                contentScale = ContentScale.Crop
            )
        } else {
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(RoundedCornerShape(50.dp))
                    .background(Color.Gray)
                    .border(2.dp, Color.LightGray, RoundedCornerShape(50.dp))
            )
        }
    }

    Spacer(modifier = Modifier.height(16.dp))

    Text(
        text = name ?: "N/A",
        modifier = Modifier.fillMaxWidth(),
        style = MaterialTheme.typography.bodyMedium,
        textAlign = TextAlign.Center
    )
    Text(
        text = email ?: "N/A",
        modifier = Modifier.fillMaxWidth(),
        style = MaterialTheme.typography.bodyMedium,
        textAlign = TextAlign.Center
    )
    Text(
        text = phoneNumber ?: "N/A",
        modifier = Modifier.fillMaxWidth(),
        style = MaterialTheme.typography.bodyMedium,
        textAlign = TextAlign.Center
    )
    Text(
        text = totalAdress ?: "N/A",
        modifier = Modifier.fillMaxWidth(),
        style = MaterialTheme.typography.bodyMedium,
        textAlign = TextAlign.Center
    )
    Text(
        text = ibanNumber ?: "N/A",
        modifier = Modifier.fillMaxWidth(),
        style = MaterialTheme.typography.bodyMedium,
        textAlign = TextAlign.Center
    )
    OutlinedButton(
        onClick = onLogout,
        content = {
            Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "Logout", tint = Color.Black)
            Text(text = context.getString(R.string.profile_logout), color = Color.Black)
        }
    )
}




