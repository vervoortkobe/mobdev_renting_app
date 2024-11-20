package edu.ap.mobiledevrentingapp.profile

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.MediaStore.Images.Media.getBitmap
import android.util.Base64
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import edu.ap.mobiledevrentingapp.firebase.FirebaseService
import edu.ap.mobiledevrentingapp.map.GeocodingService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

val retrofit = Retrofit.Builder()
    .baseUrl("https://nominatim.openstreetmap.org/")
    .addConverterFactory(GsonConverterFactory.create())
    .client(
        OkHttpClient.Builder()
            .addInterceptor { chain ->
                val request = chain.request().newBuilder()
                    .header("User-Agent", "BorrowBee/1.0 (kobe.vervoort@student.ap.be)")
                    .build()
                chain.proceed(request)
            }
            .build()
    )
    .build()

val geocodingService = retrofit.create(GeocodingService::class.java)

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

@Composable
fun ProfilePage() {
    val context = LocalContext.current
    var name by remember { mutableStateOf<String?>(null) }
    var phoneNumber by remember { mutableStateOf<String?>(null) }
    var streetName by remember { mutableStateOf<String?>(null) }
    var zipCode by remember { mutableStateOf<String?>(null) }
    var city by remember { mutableStateOf<String?>(null) }
    var adressNr by remember { mutableStateOf<String?>(null) }
    var ibanNumber by remember { mutableStateOf<String?>(null) }
    var totalAdress by remember { mutableStateOf<String?>(null) }
    var email by remember { mutableStateOf<String?>(null) }
    var country by remember { mutableStateOf<String?>(null) }
    var encodedImage by remember { mutableStateOf<String?>(null) }
    var id by remember { mutableStateOf<String?>(null) }
    var profileBitmap by remember { mutableStateOf<Bitmap?>(null) }

    // Variables for editable fields
    var editableName by remember { mutableStateOf(name) }
    var editablePhoneNumber by remember { mutableStateOf(phoneNumber) }
    var editableStreetName by remember { mutableStateOf(streetName) }
    var editableZipCode by remember { mutableStateOf(zipCode) }
    var editableCity by remember { mutableStateOf(city) }
    var editableAdressNr by remember { mutableStateOf(adressNr) }
    var editableIbanNumber by remember { mutableStateOf(ibanNumber) }
    var editableCountry by remember { mutableStateOf(country) }

    LaunchedEffect(Unit) {
        FirebaseService.getCurrentUser { success, document, _ ->
            if (success && document != null) {
                name = document.getString("fullName")
                phoneNumber = document.getString("phoneNumber")
                streetName = document.getString("streetName")
                zipCode = document.getString("zipCode")
                city = document.getString("city")
                adressNr = document.getString("addressNr")
                ibanNumber = document.getString("ibanNumber")
                country = document.getString("country")
                totalAdress = "${streetName} ${adressNr} ${city} ${zipCode} ${country}"
                email = FirebaseService.getCurrentUserEmail()
                id = document.getString("userId")
                encodedImage = document.getString("profileImage")
                profileBitmap = if (!encodedImage.isNullOrEmpty()) decode(encodedImage) else null

                // Initialize editable fields
                editableName = name
                editablePhoneNumber = phoneNumber
                editableStreetName = streetName
                editableZipCode = zipCode
                editableCity = city
                editableAdressNr = adressNr
                editableIbanNumber = ibanNumber
                editableCountry = country
            } else {
                profileBitmap = null
                Toast.makeText(
                    context,
                    "Your user data couldn't be loaded.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            val bitmap = getBitmap(context.contentResolver, it)
            profileBitmap = bitmap
            id?.let { userId ->
                FirebaseService.uploadUserProfileImage(userId, bitmap) { success, _, _ ->
                    if (success) {
                        Toast.makeText(context, "Profile image uploaded successfully!", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, "Failed to upload image", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    val scrollState = rememberScrollState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        Column(
            modifier = Modifier
                .verticalScroll(scrollState)
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box {
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(RoundedCornerShape(50.dp))
                        .border(2.dp, Color.Gray, RoundedCornerShape(50.dp))
                ) {
                    if (profileBitmap != null) {
                        Image(
                            bitmap = profileBitmap!!.asImageBitmap(),
                            contentDescription = "Profile Image",
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(50.dp)),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .size(100.dp)
                                .clip(RoundedCornerShape(50.dp))
                                .background(Color.Gray)
                                .border(2.dp, Color.LightGray, RoundedCornerShape(50.dp))
                        )
                    }
                }
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .clip(RoundedCornerShape(50.dp))
                        .background(Color.White)
                        .border(2.dp, Color.Gray, RoundedCornerShape(50.dp))
                        .clickable {
                            launcher.launch("image/*")
                        }
                        .align(Alignment.TopEnd)
                        .offset(x = 18.dp, y = (-18).dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Home,
                        contentDescription = "Edit Profile Image",
                        modifier = Modifier
                            .size(18.dp)
                            .align(Alignment.Center),
                        tint = Color.Gray
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Editable fields
            EditableField(
                label = "Name",
                value = editableName,
                onValueChange = { editableName = it },
                minLength = 1,
                errorMessage = "Geen geldige naam"
            )
            EditableField(
                label = "Phone Number",
                value = editablePhoneNumber,
                onValueChange = { editablePhoneNumber = it },
                minLength = 2,
                errorMessage = "Geen geldig telefoonnummer"
            )
            EditableField(
                label = "Street Name",
                value = editableStreetName,
                onValueChange = { editableStreetName = it },
                minLength = 3,
                errorMessage = "Geen geldige straatnaam"
            )
            EditableField(
                label = "Zip Code",
                value = editableZipCode,
                onValueChange = { editableZipCode = it },
                minLength = 4,
                errorMessage = "Geen geldige postcode"
            )
            EditableField(
                label = "City",
                value = editableCity,
                onValueChange = { editableCity = it },
                minLength = 3,
                errorMessage = "Geen geldige stad"

            )
            EditableField(
                label = "Address Number",
                value = editableAdressNr,
                onValueChange = { editableAdressNr = it },
                minLength = 2,
                errorMessage = "Geen geldige adresnummer"
            )
            EditableField(
                label = "Country",
                value = editableCountry,
                onValueChange = { editableCountry = it },
                minLength = 3,
                errorMessage = "Geen geldig landsnaam"
            )
            EditableField(
                label = "IBAN Number",
                value = editableIbanNumber,
                onValueChange = { editableIbanNumber = it },
                minLength = 14,
                errorMessage = "Geen geldige IBAN nummer"
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Save button
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.BottomCenter
            ) {
                Text(
                    text = "Save Changes",
                    modifier = Modifier
                        .padding(16.dp)
                        .clickable {
                            CoroutineScope(Dispatchers.Main).launch {
                                val fullAddress = "$editableStreetName+$editableAdressNr+$editableCity+$editableCountry"
                                val coordinates = getCoordinatesFromAddress(fullAddress)
                                if (coordinates == null) {
                                    Toast.makeText(context, "Address not found. Please check your address.", Toast.LENGTH_SHORT).show()
                                } else {
                                    val (lat, lon) = coordinates
                                    FirebaseService.getCurrentUserId()?.let {
                                        FirebaseService.updateUserProfile(
                                            userId = it,
                                            fullName = editableName,
                                            phoneNumber = editablePhoneNumber,
                                            streetName = editableStreetName,
                                            zipCode = editableZipCode,
                                            city = editableCity,
                                            addressNr = editableAdressNr,
                                            ibanNumber = editableIbanNumber,
                                            country = editableCountry,
                                            longitude = lon,
                                            latitude = lat
                                        ) { success, errorMessage ->
                                            Toast.makeText(
                                                context,
                                                errorMessage ?: if (success) "Profile Updated" else "Failed to update",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    }
                                }
                            }
                        },
                    color = Color.Blue
                )
            }
        }
    }
}

@Composable
fun EditableField(
    label: String,
    value: String?,
    onValueChange: (String) -> Unit,
    minLength: Int = 0,
    errorMessage: String? = null
) {
    Column(
        modifier = Modifier.padding(8.dp),
        horizontalAlignment = Alignment.Start
    ) {
        Text(text = label, color = Color.Gray)
        Spacer(modifier = Modifier.height(4.dp))
        BasicTextField(
            value = value ?: "",
            onValueChange = { newValue ->
                if (newValue.length >= minLength) { // Ensure street name has at least 3 characters
                    onValueChange(newValue)
                }
            },
            textStyle = LocalTextStyle.current.copy(color = Color.Black),
            modifier = Modifier
                .background(Color.LightGray, shape = RoundedCornerShape(8.dp))
                .padding(8.dp)
                .fillMaxWidth(),
            maxLines = 1
        )

        // Show error message if validation fails
        errorMessage?.let {
            Text(
                text = it,
                color = Color.Red,
                style = LocalTextStyle.current.copy(fontSize = 12.sp)
            )
        }
    }
}


fun decode(toDecodeString: String?): Bitmap? {
    if (toDecodeString.isNullOrEmpty()) return null
    return try {
        val byteArray = Base64.decode(toDecodeString, Base64.DEFAULT)
        BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
    } catch (e: IllegalArgumentException) {
        null
    }
}