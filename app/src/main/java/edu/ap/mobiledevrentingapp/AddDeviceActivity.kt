package edu.ap.mobiledevrentingapp

import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Base64
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import java.util.UUID
import com.google.firebase.firestore.FirebaseFirestore
import java.io.ByteArrayOutputStream

@Composable
fun AddDeviceActivity() {
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var bitmap by remember { mutableStateOf<Bitmap?>(null) }
    val context = LocalContext.current

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        imageUri = uri
        if (uri != null) {
            bitmap = if (Build.VERSION.SDK_INT < 28) {
                MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
            } else {
                val source = ImageDecoder.createSource(context.contentResolver, uri)
                ImageDecoder.decodeBitmap(source)
            }
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
            Text("Select a photo")
        }

        Spacer(modifier = Modifier.height(16.dp))

        bitmap?.let {
            Image(bitmap = it.asImageBitmap(), contentDescription = "Selected photo", modifier = Modifier.size(200.dp))
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = {
            bitmap?.let { bmp ->
                val outputStream = ByteArrayOutputStream()
                bmp.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
                val base64String = Base64.encodeToString(outputStream.toByteArray(), Base64.DEFAULT)

                val uuid = UUID.randomUUID().toString()

                val firestore = FirebaseFirestore.getInstance()
                val data = hashMapOf(
                    "image" to base64String,
                    "id" to uuid
                )

                firestore.collection("images").document(uuid)
                    .set(data)
                    .addOnSuccessListener {
                        Log.d("UploadPhotoScreen", "Photo uploaded!")
                        Toast.makeText(context, "Photo uploaded!", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener { e ->
                        Log.e("UploadPhotoScreen", "Error while uploading: ", e)
                        Toast.makeText(context, "Error while uploading: $e", Toast.LENGTH_SHORT).show()
                    }
            }
        }) {
            Text("Upload the photo")
        }
    }
}