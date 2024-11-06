package edu.ap.mobiledevrentingapp

import android.graphics.ImageDecoder
import android.os.Build
import android.provider.MediaStore
import android.util.Base64
import android.util.Log
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
import java.util.UUID
import java.io.ByteArrayOutputStream
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import android.graphics.Bitmap
import android.net.Uri
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.firestore.FirebaseFirestore

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
            bitmaps = uris.mapNotNull { uri -> loadBitmapFromUri(context, uri) }
        } else {
            Toast.makeText(context, "You can select up to 5 photos", Toast.LENGTH_SHORT).show()
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
            Text("Select photos")
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
                    contentDescription = "Selected photo",
                    modifier = Modifier
                        .size(200.dp)
                        .padding(horizontal = 16.dp)
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
                uploadImages(bitmaps) { success, error ->
                    if (success) {
                        Toast.makeText(context, "Photos uploaded successfully!", Toast.LENGTH_SHORT).show()
                        navController.popBackStack()
                    } else {
                        Toast.makeText(context, "Error while uploading: $error", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }) {
            Text("Upload photos")
        }
    }
}

fun loadBitmapFromUri(context: android.content.Context, uri: Uri): Bitmap? {
    return try {
        if (Build.VERSION.SDK_INT < 28) {
            MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
        } else {
            val source = ImageDecoder.createSource(context.contentResolver, uri)
            ImageDecoder.decodeBitmap(source)
        }
    } catch (e: Exception) {
        Log.e("AddDevicePage", "Error loading bitmap", e)
        null
    }
}

fun uploadSingleImage(bitmap: Bitmap, onComplete: (Boolean, String?) -> Unit) {
    val firestore = FirebaseFirestore.getInstance()
    val outputStream = ByteArrayOutputStream()
    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
    val base64String = Base64.encodeToString(outputStream.toByteArray(), Base64.DEFAULT)
    val uuid = UUID.randomUUID().toString()

    val data = hashMapOf(
        "image" to base64String,
        "id" to uuid
    )

    firestore.collection("images").document(uuid)
        .set(data)
        .addOnSuccessListener {
            Log.d("UploadSingleImage", "Photo uploaded!")
            onComplete(true, null)
        }
        .addOnFailureListener { e ->
            Log.e("UploadSingleImage", "Error while uploading: ", e)
            onComplete(false, e.localizedMessage)
        }
}

fun uploadImages(bitmaps: List<Bitmap>, onComplete: (Boolean, String?) -> Unit) {
    bitmaps.forEach { bitmap ->
        uploadSingleImage(bitmap) { success, error ->
            if (!success) {
                onComplete(false, error)
                return@uploadSingleImage
            }
        }
    }
    onComplete(true, null)
}