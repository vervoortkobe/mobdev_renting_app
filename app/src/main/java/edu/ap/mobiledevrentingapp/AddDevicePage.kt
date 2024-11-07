package edu.ap.mobiledevrentingapp

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
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import androidx.navigation.NavController

@Composable
fun AddDevicePage(navController: NavController) {
    var imageUris by remember { mutableStateOf<List<Uri>>(emptyList()) }
    var bitmaps by remember { mutableStateOf<List<Bitmap>>(emptyList()) }
    var selectedImageIndex by remember { mutableIntStateOf(0) }
    val context = LocalContext.current
    val listState = rememberLazyListState()
    var deviceName by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf<DeviceCategory?>(null) }
    var description by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var selectedCategoryIndex by remember { mutableIntStateOf(0) }

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetMultipleContents()) { uris: List<Uri> ->
        if (uris.size <= 5) {
            imageUris = uris
            bitmaps = uris.mapNotNull { uri -> FormUtil.loadBitmapFromUri(context, uri) }
        } else {
            Toast.makeText(
                context,
                "You can select up to 5 images.",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
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

        DropdownList(selectedIndex = selectedCategoryIndex, modifier = Modifier.width(350.dp), onItemClick = {selectedCategoryIndex = it})

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
            onClick = { launcher.launch("image/*") },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Black)
        ) {
            Text("Select images to upload")
        }

        Spacer(modifier = Modifier.height(2.dp))

        if(bitmaps.isEmpty()) {
            Text("Select at least 1 and at most 5 images to upload.")
        } else {
            Text("Swipe left or right to view all images.")
            LazyRow(
                state = listState,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                itemsIndexed(bitmaps) { _, bitmap ->
                    Image(
                        bitmap = bitmap.asImageBitmap(),
                        contentDescription = "Selected image",
                        modifier = Modifier
                            .size(200.dp)
                            .padding(horizontal = 8.dp)
                            .border(
                                BorderStroke(2.dp, Color.Black),
                                shape = MaterialTheme.shapes.medium
                            )
                    )
                    selectedImageIndex = remember { derivedStateOf { listState.firstVisibleItemIndex } }.value
                }
            }

            Text(
                text = "Image ${selectedImageIndex + 1} of ${bitmaps.size}",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = {
            if (bitmaps.isNotEmpty()) {
                FirebaseService.uploadImages(bitmaps) { success, imageIds, error ->
                    Log.e("AddDevicePage", "Images uploaded successfully: $success, $imageIds, $error")
                    if (success) {
                        Log.e("AddDevicePage", "Images uploaded successfully!")

                        if (imageIds != null) {
                            FirebaseService.getCurrentUserId()?.let {
                                FirebaseService.saveDevice(it, deviceName,
                                    enumValues<DeviceCategory>()[selectedCategoryIndex], description, price, imageIds) { success, deviceId, error ->
                                    if (success) {
                                        Toast.makeText(
                                            context,
                                            "The device was added successfully!",
                                            Toast.LENGTH_LONG
                                        ).show()
                                        Log.e("AddDevicePage", "The device was added successfully!")

                                        // TODO: Navigate to the device details page using deviceId
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
        },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
        ) {
            Text("Add device")
        }
    }
}

@Composable
fun DropdownList(selectedIndex: Int, modifier: Modifier, onItemClick: (Int) -> Unit) {

    var showDropdown by remember { mutableStateOf(true) }
    val scrollState = rememberScrollState()

    Spacer(modifier = Modifier.height(8.dp))

    Column(
        modifier = Modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center) {

        Box(
            modifier = modifier
                .background(Color.LightGray)
                .clickable { showDropdown = !showDropdown },
        ) {
            Column(
                modifier = modifier
                    .heightIn(max = 100.dp)
                    .verticalScroll(state = scrollState)
                    .border(width = 2.dp, shape = RoundedCornerShape(2.dp), color = Color.Black)
            ) {
                Text(
                    text = FormUtil.convertUppercaseToTitleCase(enumValues<DeviceCategory>()[selectedIndex].name),
                    modifier = Modifier.padding(3.dp)
                )
            }
        }

        Box() {
            if (showDropdown) {
                Popup(
                    alignment = Alignment.TopCenter,
                    properties = PopupProperties(
                        excludeFromSystemGesture = true,
                    ),
                    onDismissRequest = { showDropdown = false }
                ) {
                    Column(
                        modifier = modifier
                            .heightIn(max = 100.dp)
                            .verticalScroll(state = scrollState)
                            .border(width = 2.dp, shape = RoundedCornerShape(2.dp), color = Color.Black)
                    ) {
                        enumValues<DeviceCategory>().onEachIndexed { index, item ->
                            if (index != 0) {
                                Divider(thickness = 1.dp, color = Color.Gray)
                            }
                            Box(
                                modifier = Modifier
                                    .background(Color.White)
                                    .fillMaxWidth()
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