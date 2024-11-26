package edu.ap.mobiledevrentingapp.addDevice

import android.graphics.Bitmap
import android.net.Uri
import android.util.Base64
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.launch
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import edu.ap.mobiledevrentingapp.firebase.AppUtil
import edu.ap.mobiledevrentingapp.firebase.DeviceCategory
import edu.ap.mobiledevrentingapp.firebase.FirebaseService
import edu.ap.mobiledevrentingapp.ui.theme.MobileDevRentingAppTheme
import edu.ap.mobiledevrentingapp.ui.theme.Yellow40
import java.io.ByteArrayOutputStream

@Composable
fun AddDevicePage(navController: NavController) {
    var imageUris by remember { mutableStateOf<List<Uri>>(emptyList()) }
    var bitmaps by remember { mutableStateOf<List<Bitmap>>(emptyList()) }
    var selectedImageIndex by remember { mutableIntStateOf(0) }
    var showImageOverlay by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val listState = rememberLazyListState()
    var deviceName by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var selectedCategoryIndex by remember { mutableIntStateOf(0) }

    val galleryLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.GetMultipleContents()) { uris: List<Uri> ->
            if (uris.size + bitmaps.size <= 5) {
                imageUris = uris
                bitmaps =
                    bitmaps + uris.mapNotNull { uri -> AppUtil.loadBitmapFromUri(context, uri) }
            } else {
                Toast.makeText(
                    context,
                    "You can select up to 5 images.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }

    val cameraLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap: Bitmap? ->
            if (bitmap != null) {
                bitmaps = bitmaps + bitmap
            } else {
                Toast.makeText(context, "Failed to capture image.", Toast.LENGTH_SHORT).show()
            }
        }

    Box(modifier = Modifier.background(Color.White)) {
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.Black
                    )
                }
                Text(
                    "Device Information",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.size(48.dp))
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = deviceName,
                onValueChange = { deviceName = it },
                label = { Text("Device Name", color = Color.Black) },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Makita drill") },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Yellow40,
                    unfocusedBorderColor = Color.Black,
                )
            )

            Spacer(modifier = Modifier.height(2.dp))

            DropdownListAddDevice(
                selectedIndex = selectedCategoryIndex,
                onItemClick = { selectedCategoryIndex = it }
            )

            Spacer(modifier = Modifier.height(2.dp))

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description", color = Color.Black) },
                modifier = Modifier.fillMaxWidth().height(150.dp),
                placeholder = { Text("Max. 500 characters") },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Yellow40,
                    unfocusedBorderColor = Color.Black,
                )
            )

            Spacer(modifier = Modifier.height(2.dp))

            OutlinedTextField(
                value = price,
                onValueChange = { price = it },
                label = { Text("Price (in Euro €)", color = Color.Black) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("10") },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Yellow40,
                    unfocusedBorderColor = Color.Black,
                )
            )

            Spacer(modifier = Modifier.height(32.dp))

            Text("Device Images", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.Black)

            Spacer(modifier = Modifier.height(22.dp))

            Button(
                onClick = { galleryLauncher.launch("image/*") },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Black)
            ) {
                Text("Select images to upload", color = Color.White)
            }

            Spacer(modifier = Modifier.height(2.dp))

            Button(
                onClick = { cameraLauncher.launch() },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Gray)
            ) {
                Text("Take a picture", color = Color.White)
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (bitmaps.isEmpty()) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(250.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .border(1.dp, Color.LightGray, RoundedCornerShape(16.dp))
                        .background(Color.LightGray)
                ) {
                    Text("No images selected.", color = Color.Black)
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text("Select at least 1 and at most 5 images to upload.", color = Color.Black)
            } else {
                Text("Swipe left or right to view all images.")
                Spacer(modifier = Modifier.height(2.dp))
                LazyRow(
                    state = listState,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(250.dp),
                    horizontalArrangement = if (bitmaps.size == 1) Arrangement.Center else Arrangement.spacedBy(
                        8.dp
                    ),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    itemsIndexed(bitmaps) { index, bitmap ->
                        Box(
                            modifier = Modifier
                                .size(250.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .border(1.dp, Color.LightGray, RoundedCornerShape(16.dp))
                                .background(Color.LightGray)
                                .clickable {
                                    selectedImageIndex = index
                                    showImageOverlay = true
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                bitmap = bitmap.asImageBitmap(),
                                contentDescription = "Selected image",
                                modifier = Modifier.fillMaxSize(),
                            )
                        }
                    }
                }

                if (showImageOverlay) {
                    ImageOverlay(
                        bitmaps = bitmaps,
                        initialIndex = selectedImageIndex,
                        onDismiss = { showImageOverlay = false },
                        onDelete = { index ->
                            bitmaps = bitmaps.toMutableList().apply { removeAt(index) }
                            showImageOverlay = false
                        }
                    )
                }

                Text(
                    text = "Image ${selectedImageIndex + 1} of ${bitmaps.size}",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = {
                    if (bitmaps.isNotEmpty()) {
                        val imageStrings = bitmaps.map { bitmap ->
                            val outputStream = ByteArrayOutputStream()
                            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
                            val byteArray = outputStream.toByteArray()
                            Base64.encodeToString(byteArray, Base64.DEFAULT)
                        }

                        FirebaseService.getCurrentUser { succ, document, error ->
                            if (succ && document != null) {
                                val latitude = document.getDouble("latitude")!!
                                val longitude = document.getDouble("longitude")!!
                                FirebaseService.getCurrentUserId()?.let {
                                    FirebaseService.saveDevice(
                                        it,
                                        deviceName,
                                        enumValues<DeviceCategory>()[selectedCategoryIndex],
                                        description,
                                        price,
                                        imageStrings,
                                        latitude,
                                        longitude
                                    ) { success, _, saveError ->
                                        if (success) {
                                            Toast.makeText(
                                                context,
                                                "The device was added successfully!",
                                                Toast.LENGTH_LONG
                                            ).show()
                                            navController.popBackStack()
                                        } else {
                                            Toast.makeText(
                                                context,
                                                "Failed to save the device. Please try again.",
                                                Toast.LENGTH_LONG
                                            ).show()
                                            Log.e(
                                                "AddDevice",
                                                "Error saving device: $saveError"
                                            )
                                        }
                                    }
                                }
                            } else {
                                Toast.makeText(
                                    context,
                                    "Failed to load user data: $error",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Yellow40)
            ) {
                Text("Submit device")
            }

            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AddDevicePagePreview() {
    MobileDevRentingAppTheme {
        AddDevicePage(navController = rememberNavController())
    }
}