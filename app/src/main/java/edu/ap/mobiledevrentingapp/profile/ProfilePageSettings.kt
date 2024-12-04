package edu.ap.mobiledevrentingapp.profile

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.net.Uri
import android.provider.MediaStore.Images.Media.getBitmap
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.navigation.NavController
import edu.ap.mobiledevrentingapp.R
import edu.ap.mobiledevrentingapp.firebase.AppUtil.decode
import edu.ap.mobiledevrentingapp.firebase.FirebaseService
import edu.ap.mobiledevrentingapp.map.GeocodingService
import edu.ap.mobiledevrentingapp.map.RetrofitClient
import edu.ap.mobiledevrentingapp.ui.theme.Yellow40
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

val geocodingService: GeocodingService = RetrofitClient.retrofit.create(GeocodingService::class.java)

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

@SuppressLint("SuspiciousIndentation")
@Composable
fun ProfilePageSettings(navController: NavController) {
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

    // Error state for each field
    var nameError by remember { mutableStateOf<String?>(null) }
    var phoneError by remember { mutableStateOf<String?>(null) }
    var streetError by remember { mutableStateOf<String?>(null) }
    var zipError by remember { mutableStateOf<String?>(null) }
    var cityError by remember { mutableStateOf<String?>(null) }
    var addressError by remember { mutableStateOf<String?>(null) }
    var countryError by remember { mutableStateOf<String?>(null) }
    var ibanError by remember { mutableStateOf<String?>(null) }

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
                totalAdress = "$streetName $adressNr $city $zipCode $country"
                email = FirebaseService.getCurrentUserEmail()
                id = document.getString("userId")
                encodedImage = document.getString("profileImage")
                profileBitmap = if (!encodedImage.isNullOrEmpty()) decode(encodedImage!!) else null

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
                    context.getString(R.string.error_loading_user_data),
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
                        Toast.makeText(context, context.getString(R.string.profile_profile_image_uploaded_successfully), Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, context.getString(R.string.profile_profile_image_upload_failed), Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .verticalScroll(scrollState)
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier.
            fillMaxWidth()
        ){
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(16.dp)
                    .size(50.dp)
                    .clip(CircleShape)
                    .background(Yellow40)
                    .clickable {
                        navController.popBackStack()
                    }
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = context.getString(R.string.arrow_back),
                    tint = Color.Black,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }
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
                        contentDescription = context.getString(R.string.profile_image),
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
                    contentDescription = context.getString(R.string.profile_edit_profile_image),
                    modifier = Modifier
                        .size(18.dp)
                        .align(Alignment.Center),
                    tint = Color.Gray
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Editable fields with error handling
        EditableFieldWithError(
            label = context.getString(R.string.signup_full_name),
            value = editableName,
            error = nameError,
            onValueChange = { editableName = it }
        )
        EditableFieldWithError(
            label = context.getString(R.string.signup_phone_number),
            value = editablePhoneNumber,
            error = phoneError,
            onValueChange = { editablePhoneNumber = it }
        )
        EditableFieldWithError(
            label = context.getString(R.string.signup_street_name),
            value = editableStreetName,
            error = streetError,
            onValueChange = { editableStreetName = it }
        )
        EditableFieldWithError(
            label = context.getString(R.string.profile_zip_code),
            value = editableZipCode,
            error = zipError,
            onValueChange = { editableZipCode = it }
        )
        EditableFieldWithError(
            label = context.getString(R.string.signup_city),
            value = editableCity,
            error = cityError,
            onValueChange = { editableCity = it }
        )
        EditableFieldWithError(
            label = context.getString(R.string.signup_street_number),
            value = editableAdressNr,
            error = addressError,
            onValueChange = { editableAdressNr = it }
        )
        EditableFieldWithError(
            label = context.getString(R.string.signup_country),
            value = editableCountry,
            error = countryError,
            onValueChange = { editableCountry = it }
        )
        EditableFieldWithError(
            label = context.getString(R.string.signup_iban_number),
            value = editableIbanNumber,
            error = ibanError,
            onValueChange = { editableIbanNumber = it }
        )

        Spacer(modifier = Modifier.height(16.dp))

        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.BottomCenter
        ) {
            Text(
                text = context.getString(R.string.profile_save_changes),
                modifier = Modifier
                    .padding(16.dp)
                    .clickable {
                        validateAndSave(
                            name = editableName,
                            phoneNumber = editablePhoneNumber,
                            streetName = editableStreetName,
                            zipCode = editableZipCode,
                            city = editableCity,
                            addressNumber = editableAdressNr,
                            country = editableCountry,
                            ibanNumber = editableIbanNumber,
                            onError = { field, message ->
                                when (field) {
                                    "Name" -> nameError = message
                                    "Phone" -> phoneError = message
                                    "Street" -> streetError = message
                                    "Zip" -> zipError = message
                                    "City" -> cityError = message
                                    "Address" -> addressError = message
                                    "Country" -> countryError = message
                                    "IBAN" -> ibanError = message
                                }
                            },
                            onSuccess = {
                                CoroutineScope(Dispatchers.Main).launch {
                                    val fullAddress = "$editableStreetName+$editableAdressNr+$editableCity+$editableCountry"
                                    val coordinates = getCoordinatesFromAddress(fullAddress)
                                    if (coordinates == null) {
                                        Toast.makeText(context, context.getString(R.string.profile_error_getting_coordinates), Toast.LENGTH_SHORT).show()
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
                                                    errorMessage
                                                        ?: if (success) context.getString(R.string.profile_profile_updated_successfully) else context.getString(R.string.profile_profile_update_failed),
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            }
                                        }
                                    }
                                }
                            }
                        )
                    },
                color = Color.Blue,
            )
        }
    }
}

fun validateAndSave(
    name: String?,
    phoneNumber: String?,
    streetName: String?,
    zipCode: String?,
    city: String?,
    addressNumber: String?,
    country: String?,
    ibanNumber: String?,
    onError: (field: String, message: String?) -> Unit,
    onSuccess: () -> Unit
) {
    var isValid = true

    if (name.isNullOrEmpty() || name.length < 1) {
        onError("Name", "Name must be at least 1 character")
        isValid = false
    }
    if (phoneNumber.isNullOrEmpty()) {
        onError("Phone", "Invalid phone number")
        isValid = false
    }
    if (streetName.isNullOrEmpty() || streetName.length < 3) {
        onError("Street", "Street name must be at least 3 characters")
        isValid = false
    }
    if (zipCode.isNullOrEmpty() || !zipCode.matches("\\d+".toRegex())) {
        onError("Zip", "Invalid zip code")
        isValid = false
    }
    if (city.isNullOrEmpty() || city.length < 2) {
        onError("City", "City must be at least 2 characters")
        isValid = false
    }
    if (addressNumber.isNullOrEmpty() || !addressNumber.matches("\\d+".toRegex())) {
        onError("Address", "Invalid address number")
        isValid = false
    }
    if (country.isNullOrEmpty() || country.length < 2) {
        onError("Country", "Country must be at least 2 characters")
        isValid = false
    }
    if (ibanNumber.isNullOrEmpty() || !ibanNumber.matches("BE[0-9]+ [0-9]+".toRegex())) {
        onError("IBAN", "Invalid IBAN format")
        isValid = false
    }

    if (isValid) {
        onSuccess()
    }
}

@Composable
fun EditableFieldWithError(label: String, value: String?, error: String?, onValueChange: (String) -> Unit) {
    Column(
        modifier = Modifier.padding(vertical = 8.dp),
        horizontalAlignment = Alignment.Start
    ) {
        Text(text = label, color = Color.Gray)
        Spacer(modifier = Modifier.height(4.dp))
        BasicTextField(
            value = value ?: "",
            onValueChange = onValueChange,
            textStyle = LocalTextStyle.current.copy(color = Color.Black),
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.LightGray, shape = RoundedCornerShape(8.dp))
                .padding(8.dp),
            maxLines = 1
        )
        if (!error.isNullOrEmpty()) {
            Text(text = error, color = Color.Red)
        }
    }
}