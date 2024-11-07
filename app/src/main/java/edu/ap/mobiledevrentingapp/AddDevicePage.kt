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
import androidx.compose.foundation.border
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

@Composable
fun AddDevicePage(navController: NavController) {
    var imageUris by remember { mutableStateOf<List<Uri>>(emptyList()) }
    var bitmaps by remember { mutableStateOf<List<Bitmap>>(emptyList()) }
    var selectedIndex by remember { mutableIntStateOf(0) }
    val context = LocalContext.current
    val listState = rememberLazyListState()
    var deviceName by remember { mutableStateOf("") }
    var categoryQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf<DeviceCategory>(DeviceCategory.OTHER) }
    var description by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("0") }

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

        OutlinedTextField(
            value = categoryQuery,
            onValueChange = { query ->
                categoryQuery = query
                selectedCategory = DeviceCategory.entries.find { it.name.contains(query, ignoreCase = true) }!!
            },
            label = { Text("Device Category", color = Color.Black) },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color.Red,
                unfocusedBorderColor = Color.Black,
            )
        )
        DropdownMenu(
            expanded = categoryQuery.isNotBlank(),
            onDismissRequest = { categoryQuery = "" }
        ) {
            DeviceCategory.entries.filter {
                it.name.contains(categoryQuery, ignoreCase = true)
            }.forEach { category ->
                DropdownMenuItem(
                    onClick = {
                        selectedCategory = category
                        categoryQuery = category.name
                    },
                    text = { Text(category.name, color = Color.Black) }
                )
            }
        }

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
            Text("Select images to upload.")
        }

        Spacer(modifier = Modifier.height(2.dp))

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
                selectedIndex = remember { derivedStateOf { listState.firstVisibleItemIndex } }.value
            }
        }

        Spacer(modifier = Modifier.height(2.dp))

        Text(
            text = "Image ${selectedIndex + 1} of ${bitmaps.size}",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = {
            if (bitmaps.isNotEmpty()) {
                FirebaseService.uploadImages(bitmaps) { success, imageIds, error ->
                    if (success) {
                        Log.e("AddDevicePage", "Images uploaded successfully!")

                        if (imageIds != null) {
                            FirebaseService.getCurrentUserId()?.let {
                                FirebaseService.saveDevice(it, deviceName, selectedCategory, description, price, imageIds) { success, deviceId, error ->
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
