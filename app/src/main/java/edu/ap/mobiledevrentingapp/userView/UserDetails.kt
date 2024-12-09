package edu.ap.mobiledevrentingapp.userView

import android.graphics.Bitmap
import android.util.Log
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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import edu.ap.mobiledevrentingapp.R
import edu.ap.mobiledevrentingapp.firebase.AppUtil
import edu.ap.mobiledevrentingapp.firebase.FirebaseService

@Composable
fun UserDetailPage(userId: String?, navController: NavController) {
    val context = LocalContext.current
    var fullName by remember { mutableStateOf("") }
    var profileBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var rating by remember { mutableStateOf(0) }
    var showDialog by remember { mutableStateOf(false) }

    LaunchedEffect(userId) {
        if (userId != null) {
            FirebaseService.getUserById(userId) { success, doc, error ->
                if (success && doc != null) {
                    fullName = doc.getString("fullName") ?: "Unknown"
                    val encodedImage = doc.getString("profileImage")
                    profileBitmap = if (!encodedImage.isNullOrEmpty()) {
                        AppUtil.decode(encodedImage)
                    } else {
                        null
                    }
                } else {
                    Log.e("Error", error ?: "Unknown error")
                }
            }
        }
    }

    Scaffold { padding ->
        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(32.dp))

                if (profileBitmap != null) {
                    Image(
                        bitmap = profileBitmap!!.asImageBitmap(),
                        contentDescription = context.getString(R.string.profile_image),
                        modifier = Modifier
                            .size(100.dp)
                            .clip(CircleShape)
                            .border(2.dp, Color.LightGray, CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .clip(CircleShape)
                            .background(Color.Gray)
                            .border(2.dp, Color.LightGray, CircleShape)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(text = fullName)

                Spacer(modifier = Modifier.height(16.dp))

                if (userId != null) {
                    DisplayAverageRating(userId)

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(onClick = { showDialog = true }) {
                        Text("Give Review")
                    }

                    if (showDialog) {
                        ReviewDialog(
                            currentRating = rating,
                            onRatingChange = { rating = it },
                            onDismiss = {
                                var raterId = FirebaseService.getCurrentUserId();
                                if (rating > 0) {
                                    // Submit or update the rating
                                    raterId?.let {
                                        FirebaseService.submitRating(userId,
                                            it, rating) { success, error ->
                                            if (!success) {
                                                Log.e("SubmitError", error ?: "Unknown error")
                                            }
                                        }
                                    }
                                }
                                showDialog = false
                            }
                        )
                    }
                }
            }
        }
    }

    IconButton(
        onClick = { navController.popBackStack() },
        modifier = Modifier
            .padding(16.dp)
            .size(48.dp)
            .background(Color.White, CircleShape)
            .border(1.dp, Color.Gray, CircleShape)
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = context.getString(R.string.back),
            tint = Color.Black
        )
    }
}

@Composable
fun ReviewDialog(
    currentRating: Int,
    onRatingChange: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Give Review") },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = "Select your rating:")
                Spacer(modifier = Modifier.height(8.dp))

                // Stars for selecting rating
                Row(horizontalArrangement = Arrangement.Center) {
                    for (i in 1..5) {
                        val starIcon = if (i <= currentRating) Icons.Filled.Star else Icons.Outlined.Star
                        Image(
                            imageVector = starIcon,
                            contentDescription = "Star $i",
                            modifier = Modifier
                                .size(32.dp)
                                .clickable { onRatingChange(i) },
                            colorFilter = ColorFilter.tint(Color.Yellow)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(text = "Rating: $currentRating/5")
            }
        },
        confirmButton = {
            Button(onClick = {
                onDismiss()
            }) {
                Text("Submit")
            }
        },
        dismissButton = {
            androidx.compose.material3.TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun DisplayAverageRating(userId: String) {
    var averageRating by remember { mutableStateOf(0.0) }

    LaunchedEffect(userId) {
        FirebaseService.getUserRating(userId) { success, avg, error ->
            if (success && avg != null) {
                averageRating = avg
            } else {
                Log.e("RatingError", error ?: "Unknown error")
            }
        }
    }

    Row(horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
        for (i in 1..5) {
            val filled = i <= averageRating
            val partial = i - 1 < averageRating && averageRating < i

            val starIcon = when {
                filled -> Icons.Filled.Star
                partial -> Icons.Filled.Star
                else -> Icons.Outlined.Star
            }

            Image(
                imageVector = starIcon,
                contentDescription = "Star $i",
                modifier = Modifier.size(32.dp),
                colorFilter = ColorFilter.tint(Color.Yellow)
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        Text(text = String.format("%.1f/5", averageRating), color = Color.Black)
    }
}




