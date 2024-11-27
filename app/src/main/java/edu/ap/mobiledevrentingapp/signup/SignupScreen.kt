package edu.ap.mobiledevrentingapp.signup

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import edu.ap.mobiledevrentingapp.R
import edu.ap.mobiledevrentingapp.firebase.FirebaseService
import edu.ap.mobiledevrentingapp.map.GeocodingService
import edu.ap.mobiledevrentingapp.map.RetrofitClient
import edu.ap.mobiledevrentingapp.ui.theme.Yellow40
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

@Composable
fun SignupScreen(
    onSignupSuccess: () -> Unit,
    onNavigateToLogin: () -> Unit
) {
    val context = LocalContext.current
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var fullName by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var ibanNumber by remember { mutableStateOf("") }
    var country by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("") }
    var zipCode by remember { mutableStateOf("") }
    var streetName by remember { mutableStateOf("") }
    var addressNr by remember { mutableStateOf("") }
    var latitude by remember { mutableDoubleStateOf(50.0) }
    var longitude by remember { mutableDoubleStateOf(50.0) }
    var isLoading by remember { mutableStateOf(false) }

    val geocodingService = RetrofitClient.retrofit.create(GeocodingService::class.java)

    suspend fun getCoordinatesFromAddress(address: String): Pair<Double, Double>? {
        return withContext(Dispatchers.IO) {
            try {
                val response = geocodingService.getCoordinates(address)
                val result = response.firstOrNull()
                result?.let {
                    return@withContext Pair(it.lat.toDouble(), it.lon.toDouble())
                }
                return@withContext null
            } catch (e: Exception) {
                return@withContext null
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        Image(
            painter = painterResource(id = R.drawable.background),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Personal Information", fontSize = 20.sp, fontWeight = FontWeight.Bold)

            OutlinedTextField(
                value = fullName,
                singleLine = true,
                onValueChange = { fullName = it },
                label = { Text("Full Name", color = Color.Black) },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Henry Jekyll") },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Yellow40,
                    unfocusedBorderColor = Color.Black,
                )
            )

            Spacer(modifier = Modifier.height(2.dp))

            OutlinedTextField(
                value = phoneNumber,
                singleLine = true,
                onValueChange = { phoneNumber = it },
                label = { Text("Phone Number", color = Color.Black) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("+32123456789") },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Yellow40,
                    unfocusedBorderColor = Color.Black,
                )
            )

            Spacer(modifier = Modifier.height(2.dp))

            OutlinedTextField(
                value = ibanNumber,
                singleLine = true,
                onValueChange = { ibanNumber = it },
                label = { Text("IBAN Number", color = Color.Black) },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("BE00 0123 4567 8910") },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Yellow40,
                    unfocusedBorderColor = Color.Black,
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text("Address Details", fontSize = 20.sp, fontWeight = FontWeight.Bold)

            OutlinedTextField(
                value = country,
                singleLine = true,
                onValueChange = { country = it },
                label = { Text("Country", color = Color.Black) },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Belgium") },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Yellow40,
                    unfocusedBorderColor = Color.Black,
                )
            )

            Spacer(modifier = Modifier.height(2.dp))

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = city,
                    singleLine = true,
                    onValueChange = { city = it },
                    label = { Text("City", color = Color.Black) },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Antwerp") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Yellow40,
                        unfocusedBorderColor = Color.Black,
                    )
                )

                OutlinedTextField(
                    value = zipCode,
                    singleLine = true,
                    onValueChange = { zipCode = it },
                    label = { Text("Zip", color = Color.Black) },
                    modifier = Modifier.width(80.dp),
                    placeholder = { Text("1000") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Yellow40,
                        unfocusedBorderColor = Color.Black,
                    )
                )
            }

            Spacer(modifier = Modifier.height(2.dp))

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = streetName,
                    singleLine = true,
                    onValueChange = { streetName = it },
                    label = { Text("Street", color = Color.Black) },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Rue Avenue") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Yellow40,
                        unfocusedBorderColor = Color.Black,
                    )
                )

                OutlinedTextField(
                    value = addressNr,
                    singleLine = true,
                    onValueChange = { addressNr = it },
                    label = { Text("Nr.", color = Color.Black) },
                    modifier = Modifier.width(80.dp),
                    placeholder = { Text("1") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Yellow40,
                        unfocusedBorderColor = Color.Black,
                    )
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text("Account Credentials", fontSize = 20.sp, fontWeight = FontWeight.Bold)

            OutlinedTextField(
                value = email,
                singleLine = true,
                onValueChange = { email = it },
                label = { Text("E-mail", color = Color.Black) },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("someone@example.com") },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Yellow40,
                    unfocusedBorderColor = Color.Black,
                )
            )

            Spacer(modifier = Modifier.height(2.dp))

            OutlinedTextField(
                value = password,
                singleLine = true,
                onValueChange = { password = it },
                label = { Text("Password", color = Color.Black) },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("********") },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Yellow40,
                    unfocusedBorderColor = Color.Black
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    val fullAddress = "$streetName + $addressNr + $city + $country"
                    isLoading = true

                    CoroutineScope(Dispatchers.Main).launch {
                        val coordinates = getCoordinatesFromAddress(fullAddress)
                        Log.e("Coordinates", coordinates.toString())
                        Log.e("Coordinates", fullAddress)

                        if (coordinates == null) {
                            isLoading = false
                            Toast.makeText(context, "Address not found. Please check your address.", Toast.LENGTH_SHORT).show()
                        } else {
                            val (lat, lon) = coordinates
                            latitude = lat
                            longitude = lon

                            FirebaseService.signup(
                                email, password, fullName, phoneNumber, ibanNumber, country, city, zipCode, streetName, addressNr,
                                latitude, longitude
                            ) { success, errorMessage ->
                                isLoading = false
                                if (success) {
                                    onSignupSuccess()
                                } else {
                                    Toast.makeText(context, errorMessage ?: "Signup failed", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = email.isNotBlank() && password.isNotBlank(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Black,
                    disabledContainerColor = Color.Black
                )
            ) {
                Text("Sign Up", color = Color.White)
            }

            Spacer(modifier = Modifier.height(8.dp))

            TextButton(
                onClick = { onNavigateToLogin() }
            ) {
                Text("Already have an account? Login", color = Color.Black)
            }

            Spacer(modifier = Modifier.height(196.dp))
        }
    }
}
