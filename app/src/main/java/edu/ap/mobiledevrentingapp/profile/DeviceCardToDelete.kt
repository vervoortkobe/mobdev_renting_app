package edu.ap.mobiledevrentingapp.profile

import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import edu.ap.mobiledevrentingapp.R
import edu.ap.mobiledevrentingapp.firebase.FirebaseService
import edu.ap.mobiledevrentingapp.ui.theme.Yellow40

@Composable
fun DeviceCardToDelete(device: Map<String, Any>, onDelete: (String) -> Unit) {
    val context = LocalContext.current
    var renterName by remember { mutableStateOf("Loading...") }
    var showConfirmationDialog by remember { mutableStateOf(false) }

    val deviceId = device["deviceId"]?.toString()
    LaunchedEffect(deviceId) {
        if (!deviceId.isNullOrEmpty()) {
            FirebaseService.getRentalsByDeviceId(deviceId) { rentals ->
                if (rentals.isNotEmpty()) {
                    val renterId = rentals.firstOrNull()?.renterId
                    if (!renterId.isNullOrEmpty()) {
                        FirebaseService.getUserById(renterId) { success, document, error ->
                            renterName = if (success && document != null) {
                                document.getString("fullName") ?: context.getString(R.string.profile_unknown_user)
                            } else {
                                error ?: context.getString(R.string.profile_failed_user)
                            }
                        }
                    } else {
                        renterName = context.getString(R.string.profile_no_renter)
                    }
                } else {
                    renterName = context.getString(R.string.profile_not_rented)
                }
            }
        } else {
            renterName = context.getString(R.string.profile_invalid_device)
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = Yellow40,
        )
    ) {
        Column(modifier = Modifier
            .padding(16.dp)) {
            Text(
                text = device["deviceName"]?.toString() ?: context.getString(R.string.profile_unknown_device),
                style = MaterialTheme.typography.titleLarge
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = device["description"]?.toString() ?: context.getString(R.string.profile_no_description),
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Category: ${device["category"]?.toString() ?: "N/A"}",
                style = MaterialTheme.typography.bodySmall
            )
            Text(
                text = "${context.getString(R.string.profile_rented_by)} $renterName",
                style = MaterialTheme.typography.bodySmall
            )
            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    showConfirmationDialog = true
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color.Black,
                    contentColor = Yellow40)
            ) {
                Text(text = context.getString(R.string.profile_delete_device))
            }

            if (showConfirmationDialog) {
                AlertDialog(
                    containerColor = Yellow40,
                    onDismissRequest = { showConfirmationDialog = false },
                    title = {
                        Text(text = context.getString(R.string.profile_delete_device))
                    },
                    text = {
                        Text(
                            text = context.getString(R.string.profile_delete_device_confirmation),
                        )
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                showConfirmationDialog = false
                                if (!deviceId.isNullOrEmpty()) {
                                    onDelete(deviceId)
                                } else {
                                    Toast.makeText(context, context.getString(R.string.profile_invalid_device), Toast.LENGTH_SHORT).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Black,
                                contentColor = Yellow40)
                        ) {
                            Text(context.getString(R.string.yes))
                        }
                    },
                    dismissButton = {
                        Button(
                            onClick = { showConfirmationDialog = false },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Black,
                                contentColor = Yellow40)
                        ) {
                            Text(context.getString(R.string.yes))
                        }
                    }
                )
            }
        }
    }
}