package edu.ap.mobiledevrentingapp.addDevice

import android.content.Context
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.launch
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.heightIn
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import edu.ap.mobiledevrentingapp.firebase.AppUtil
import edu.ap.mobiledevrentingapp.firebase.DeviceCategory
import edu.ap.mobiledevrentingapp.firebase.FirebaseService
import edu.ap.mobiledevrentingapp.ui.theme.MobileDevRentingAppTheme

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

    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetMultipleContents()) { uris: List<Uri> ->
        if (uris.size + bitmaps.size <= 5) {
            imageUris = uris
            bitmaps = bitmaps + uris.mapNotNull { uri -> AppUtil.loadBitmapFromUri(context, uri) }
        } else {
            Toast.makeText(
                context,
                "You can select up to 5 images.",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap: Bitmap? ->
        if (bitmap != null) {
            bitmaps = bitmaps + bitmap
        } else {
            Toast.makeText(context, "Failed to capture image.", Toast.LENGTH_SHORT).show()
        }
    }

    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .verticalScroll(rememberScrollState())
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("Device Information", fontSize = 20.sp, fontWeight = FontWeight.Bold)

        OutlinedTextField(
            value = deviceName,
            singleLine = true,
            onValueChange = { deviceName = it },
            label = { Text("Device Name", color = Color.Black) },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Makita Screwdriver") },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color.Red,
                unfocusedBorderColor = Color.Black,
            )
        )

        Spacer(modifier = Modifier.height(2.dp))

        DropdownList(selectedIndex = selectedCategoryIndex, onItemClick = { selectedCategoryIndex = it })

        Spacer(modifier = Modifier.height(2.dp))

        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            label = { Text("Description", color = Color.Black) },
            modifier = Modifier.fillMaxWidth().height(150.dp),
            placeholder = { Text("Max. 500 characters") },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color.Red,
                unfocusedBorderColor = Color.Black,
            )
        )

        Spacer(modifier = Modifier.height(2.dp))

        OutlinedTextField(
            value = price,
            singleLine = true,
            onValueChange = { price = it },
            label = { Text("Price per day (in Euro €)", color = Color.Black) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("10") },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color.Red,
                unfocusedBorderColor = Color.Black,
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text("Device Images", fontSize = 20.sp, fontWeight = FontWeight.Bold)

        Button(
            onClick = { galleryLauncher.launch("image/*") },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Black)
        ) {
            Text("Select images to upload")
        }

        Button(
            onClick = { cameraLauncher.launch() },
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Gray)
        ) {
            Text("Take a picture")
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
                Text("No images selected.")
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text("Select at least 1 and at most 5 images to upload.")
        } else {
            Text("Swipe left or right to view all images.")
            Spacer(modifier = Modifier.height(2.dp))
            LazyRow(
                state = listState,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp),
                horizontalArrangement = if (bitmaps.size == 1) Arrangement.Center else Arrangement.spacedBy(8.dp),
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
            onClick = { submitDevice(bitmaps, deviceName, selectedCategoryIndex, description, price, navController, context) },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
        ) {
            Text("Submit device")
        }

        Spacer(modifier = Modifier.height(8.dp))
    }
}

fun submitDevice(bitmaps : List<Bitmap>, deviceName: String, selectedCategoryIndex: Int, description: String, price: String, navController: NavController, context: Context) {
    if (bitmaps.isNotEmpty()) {
        FirebaseService.uploadImages(bitmaps) { success, imageIds, error ->
            Log.e("AddDevicePage", "Images uploaded successfully: $success, $imageIds, $error")
            if (success) {
                Log.e("AddDevicePage", "Images uploaded successfully!")

                if (imageIds != null) {
                    FirebaseService.getCurrentUserId()?.let {
                        FirebaseService.saveDevice(
                            it,
                            deviceName,
                            enumValues<DeviceCategory>()[selectedCategoryIndex],
                            description,
                            price,
                            imageIds
                        ) { success, _, error ->
                            if (success) {
                                Toast.makeText(
                                    context,
                                    "The device was added successfully!",
                                    Toast.LENGTH_LONG
                                ).show()
                                Log.e("AddDevicePage", "The device was added successfully!")
                                navController.popBackStack()
                            } else {
                                Toast.makeText(
                                    context,
                                    "The device's images failed to upload. Please try again.",
                                    Toast.LENGTH_LONG
                                ).show()
                                Log.e("AddDevicePage", "Error while uploading images: $error")
                            }
                        }
                    }
                } else {
                    Toast.makeText(
                        context,
                        "The device's images failed to fetch.",
                        Toast.LENGTH_LONG
                    ).show()
                    Log.e("AddDevicePage", "Error while uploading images: $error")
                }
            } else {
                Toast.makeText(
                    context,
                    "The device's images failed to upload. Please try again.",
                    Toast.LENGTH_LONG
                ).show()
                Log.e("AddDevicePage", "Error while uploading images: $error")
            }
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