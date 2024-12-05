package edu.ap.mobiledevrentingapp.userView

import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.google.firebase.firestore.DocumentSnapshot
import edu.ap.mobiledevrentingapp.firebase.FirebaseService

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserDetailPage(userId: String?) {
    val context = LocalContext.current
    var fullName by remember { mutableStateOf("") }

    LaunchedEffect(userId) {
        if (userId != null) {
            FirebaseService.getUserById(userId) { success, doc, error ->
                if (success) {
                    fullName = doc?.data?.get("fullName").toString()
                } else {
                    Log.e("Error", error ?: "Unknown error")
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("User Detail") })
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            Text(
                text = "Full Name: $fullName",
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}

