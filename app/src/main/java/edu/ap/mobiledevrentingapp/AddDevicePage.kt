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
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

@Composable
fun AddDevicePage(navController: NavController) {
    var imageUris by remember { mutableStateOf<List<Uri>>(emptyList()) }
    var bitmaps by remember { mutableStateOf<List<Bitmap>>(emptyList()) }
    var selectedIndex by remember { mutableIntStateOf(0) }
    val context = LocalContext.current
    val listState = rememberLazyListState()

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetMultipleContents()) { uris: List<Uri> ->
        if (uris.size <= 5) {
            imageUris = uris
            bitmaps = uris.mapNotNull { uri -> FormUtil.loadBitmapFromUri(context, uri) }
        } else {
            Toast.makeText(context, "You can select up to 5 images.", Toast.LENGTH_SHORT).show()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Button(onClick = { launcher.launch("image/*") }) {
            Text("Select images to upload.")
        }

        Spacer(modifier = Modifier.height(16.dp))

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

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Image ${selectedIndex + 1} of ${bitmaps.size}",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = {
            if (bitmaps.isNotEmpty()) {
                FirebaseService.uploadImages(bitmaps) { success, error ->
                    if (success) {
                        Toast.makeText(context, "Images uploaded successfully!", Toast.LENGTH_SHORT).show()
                        navController.popBackStack()
                    } else {
                        Toast.makeText(context, "Error while uploading: $error", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }) {
            Text("Add this device & make it publicly available!")
        }
    }
}
