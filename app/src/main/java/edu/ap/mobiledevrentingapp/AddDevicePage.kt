package edu.ap.mobiledevrentingapp

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
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
import android.location.Location
import android.net.Uri
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.launch
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ColumnScope
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
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.Task
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

    // Inside your composable or activity
    val fusedLocationClient: FusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context)

    fun getLocation(context: Context, callback: (Double?, Double?) -> Unit) {
        val activity = context as? Activity
        if (activity != null) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                val locationTask: Task<Location> = fusedLocationClient.lastLocation
                locationTask.addOnSuccessListener { location: Location? ->
                    if (location != null) {
                        callback(location.latitude, location.longitude)
                    } else {
                        callback(null, null)  // Location is not available
                    }
                }.addOnFailureListener {
                    callback(null, null)  // Handle error
                }
            } else {
                // Request location permission
                ActivityCompat.requestPermissions(activity, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1)
            }
        } else {
            callback(null, null)  // Handle context issue (not an Activity)
        }
    }


    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetMultipleContents()) { uris: List<Uri> ->
        if (uris.size + bitmaps.size <= 5) {
            imageUris = uris
            bitmaps = bitmaps + uris.mapNotNull { uri -> FormUtil.loadBitmapFromUri(context, uri) }
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
            onValueChange = { price = it },
            label = { Text("Price (in Euro €)", color = Color.Black) },
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
            onClick = {
                if (bitmaps.isNotEmpty()) {
                    FirebaseService.uploadImages(bitmaps) { success, imageIds, error ->
                        Log.e("AddDevicePage", "Images uploaded successfully: $success, $imageIds, $error")
                        if (success) {
                            Log.e("AddDevicePage", "Images uploaded successfully!")

                            if (imageIds != null) {
                                getLocation(context) { latitude, longitude ->
                                    if (latitude != null && longitude != null) {
                                        FirebaseService.uploadImages(bitmaps) { success, imageIds, error ->
                                            if (success && imageIds != null) {
                                                FirebaseService.getCurrentUserId()?.let {
                                                    FirebaseService.saveDevice(
                                                        it,
                                                        deviceName,
                                                        enumValues<DeviceCategory>()[selectedCategoryIndex],
                                                        description,
                                                        price,
                                                        imageIds,
                                                        latitude,
                                                        longitude
                                                    ) { success, _, error ->
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
                                                                "The device's images failed to upload. Please try again.",
                                                                Toast.LENGTH_LONG
                                                            ).show()
                                                        }
                                                    }
                                                }
                                            } else {
                                                Toast.makeText(context, "Failed to upload images.", Toast.LENGTH_LONG).show()
                                            }
                                        }
                                    } else {
                                        Toast.makeText(context, "Unable to retrieve location.", Toast.LENGTH_LONG).show()
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
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
        ) {
            Text("Submit device")
        }

        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
fun ImageOverlay(bitmaps: List<Bitmap>, initialIndex: Int, onDismiss: () -> Unit, onDelete: (Int) -> Unit) {
    val pagerState = rememberPagerState( initialPage = initialIndex, pageCount = { bitmaps.size } )

    Popup(
        alignment = Alignment.Center,
        properties = PopupProperties(focusable = true),
        onDismissRequest = onDismiss
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.9f))
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                Button(
                    onClick = { onDelete(pagerState.currentPage) },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) {
                    Text("Delete")
                }

                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Gray)
                ) {
                    Text("Close")
                }
            }

            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { page ->
                Image(
                    bitmap = bitmaps[page].asImageBitmap(),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                        .clickable {  }
                )
            }
        }
    }
}

@Composable
fun DropdownList(selectedIndex: Int, onItemClick: (Int) -> Unit) {

    var showDropdown by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()

    Spacer(modifier = Modifier.height(8.dp))

    Column(
        modifier = Modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center) {

        Box(
            modifier = Modifier.fillMaxWidth()
                .background(Color.White, shape = RoundedCornerShape(6.dp))
                .border(
                    width = 1.dp,
                    color = Color.Black,
                    shape = RoundedCornerShape(6.dp)
                )
                .clickable { showDropdown = !showDropdown }
                .padding(12.dp)
                .fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .heightIn(max = 120.dp)
                    .verticalScroll(state = scrollState)
            ) {
                Text(
                    text = FormUtil.convertUppercaseToTitleCase(enumValues<DeviceCategory>()[selectedIndex].name),
                    modifier = Modifier.padding(3.dp),
                    color = Color.Black
                )
            }
        }

        Box {
            if (showDropdown) {
                Popup(
                    alignment = Alignment.TopCenter,
                    properties = PopupProperties(
                        excludeFromSystemGesture = true,
                    ),
                    onDismissRequest = { showDropdown = false }
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp, 0.dp)
                            .heightIn(max = 155.dp)
                            .verticalScroll(state = scrollState)
                            .border(width = 2.dp, shape = RoundedCornerShape(6.dp), color = Color.Black)
                    ) {
                        enumValues<DeviceCategory>().onEachIndexed { index, item ->
                            if (index != 0) {
                                Divider(thickness = 1.dp, color = Color.Gray)
                            }
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color.White)
                                    .padding(12.dp, 3.dp)
                                    .clickable {
                                        onItemClick(index)
                                        showDropdown = !showDropdown
                                    },
                            ) {
                                Text(text = FormUtil.convertUppercaseToTitleCase(item.name), modifier = Modifier.padding(3.dp))
                            }
                        }
                    }
                }
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