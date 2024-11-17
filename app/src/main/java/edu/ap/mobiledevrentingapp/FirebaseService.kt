package edu.ap.mobiledevrentingapp

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import android.util.Log
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.io.ByteArrayOutputStream
import java.util.UUID

object FirebaseService {
    private val firestore: FirebaseFirestore get() = FirebaseFirestore.getInstance()

    fun login(email: String, password: String, callback: (Boolean, String?) -> Unit) {
        FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    callback(true, null)
                } else {
                    val errorMessage = when (task.exception) {
                        is FirebaseAuthInvalidCredentialsException -> "The e-mail or password is incorrect."
                        is FirebaseAuthInvalidUserException -> "There is no account associated with this email."
                        is FirebaseNetworkException -> "Network error. Please try again."
                        else -> "Login failed. Please check your credentials and try again."
                    }
                    callback(false, errorMessage)
                }
            }
    }

    fun signup(email: String, password: String, fullName: String, phoneNumber: String, ibanNumber: String, country: String, city: String, zipCode: String, streetName: String, addressNr: String, callback: (Boolean, String?) -> Unit) {
        if(email.isEmpty()) {
            callback(false, "Please provide your full name.")
            return
        }
        if(password.isEmpty()) {
            callback(false, "Please provide your full name.")
            return
        }
        if(fullName.isEmpty()) {
            callback(false, "Please provide your full name.")
            return
        }
        if(phoneNumber.isEmpty()) {
            callback(false, "Please provide your phone number.")
            return
        }
        if(!AppUtil.isValidPhoneNumber(phoneNumber)) {
            callback(false, "Please provide a valid phone number.")
            return
        }
        if(ibanNumber.isEmpty()) {
            callback(false, "Please provide your IBAN number.")
            return
        }
        if(!AppUtil.isValidIbanNumber(ibanNumber)) {
            callback(false, "Please provide a valid IBAN number.")
            return
        }
        if(country.isEmpty()) {
            callback(false, "Please provide your country.")
            return
        }
        if(city.isEmpty()) {
            callback(false, "Please provide your city.")
            return
        }
        if(zipCode.isEmpty()) {
            callback(false, "Please provide your zip code.")
            return
        }
        if(streetName.isEmpty()) {
            callback(false, "Please provide your street name.")
            return
        }
        if(addressNr.isEmpty()) {
            callback(false, "Please provide your house number.")
            return
        }

        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val userId: String = task.result.user?.uid.toString()

                    saveUser(userId, fullName, phoneNumber, ibanNumber, country, city, zipCode, streetName, addressNr) { success, errorMessage ->
                        if (success) {
                            callback(true, null)
                        } else {
                            callback(false, errorMessage)
                        }
                    }

                } else {
                    val errorMessage = when (task.exception) {
                        is FirebaseAuthWeakPasswordException -> "Password is too weak. Please use at least 6 characters."
                        is FirebaseAuthInvalidCredentialsException -> "The e-mail you provided is invalid."
                        is FirebaseAuthUserCollisionException -> "This email is already associated with another account."
                        is FirebaseNetworkException -> "Network error. Please try again."
                        else -> "Signup failed. Please check the provided information and try again."
                    }
                    callback(false, errorMessage)
                }
            }
    }

    private fun saveUser(userId: String, fullName: String, phoneNumber: String, ibanNumber: String, country: String, city: String, zipCode: String, streetName: String, addressNr: String, callback: (Boolean, String?) -> Unit) {
        val data = hashMapOf(
            "userId" to userId,
            "fullName" to fullName,
            "phoneNumber" to phoneNumber,
            "ibanNumber" to ibanNumber,
            "country" to country,
            "city" to city,
            "zipCode" to zipCode,
            "streetName" to streetName,
            "addressNr" to addressNr
        )

        firestore.collection("users").document(userId)
            .set(data)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    callback(true, null)
                } else {
                    callback(false, "Failed to save user data. Please try again.")
                }
            }
    }

    fun getUserById(userId: String, callback: (Boolean, DocumentSnapshot?, String?) -> Unit) {
        firestore.collection("users").document(userId).get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val document = task.result
                    if (document != null && document.exists()) {
                        callback(true, document, null)
                    } else {
                        callback(false, null, "The current user does not exist.")
                    }
                } else {
                    callback(false, null, "Failed to retrieve the details of the current user.")
                }
            }
    }

    fun getUserByEmail(email: String, callback: (Boolean, DocumentSnapshot?, String?) -> Unit) {
        firestore.collection("users")
            .whereEqualTo("email", email)
            .get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val documents = task.result
                    if (documents != null && !documents.isEmpty) {
                        val document = documents.documents[0]
                        val userId = document.id

                        getUserById(userId) { success, userDocument, error ->
                            callback(success, userDocument, error)
                        }
                    } else {
                        callback(false, null, "User with the specified e-mail does not exist.")
                    }
                } else {
                    callback(false, null, "Failed to retrieve user by e-mail.")
                }
            }
    }

    fun uploadUserProfileImage(userId: String, bitmap: Bitmap, onComplete: (Boolean, String?, String?) -> Unit) {
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
        val base64String = Base64.encodeToString(outputStream.toByteArray(), Base64.DEFAULT)

        val data = hashMapOf(
            "profileImage" to base64String
        )

        firestore.collection("users").document(userId)
            .update(data as Map<String, Any>)
            .addOnSuccessListener {
                Log.d("UploadSingleImage", "Image uploaded!")
                onComplete(true, userId, null)
            }
            .addOnFailureListener { e ->
                Log.e("UploadSingleImage", "Error while uploading: ", e)
                onComplete(false, null, e.localizedMessage)
            }
    }

    private fun uploadSingleImage(bitmap: Bitmap, onComplete: (Boolean, String?, String?) -> Unit) {
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
                Log.d("UploadSingleImage", "Image uploaded!")
                onComplete(true, uuid, null)
            }
            .addOnFailureListener { e ->
                Log.e("UploadSingleImage", "Error while uploading: ", e)
                onComplete(false, null, e.localizedMessage)
            }
    }

    fun uploadImages(bitmaps: List<Bitmap>, onComplete: (Boolean, List<String>?, String?) -> Unit) {
        val imageIds = mutableListOf<String>()
        var completedUploads = 0

        bitmaps.forEach { bitmap ->
            uploadSingleImage(bitmap) { success, id, error ->
                Log.d("UploadSingleImage", "Image upload result: $success, ID: $id, Error: $error")
                if (success) {
                    id?.let { imageIds.add(it) }
                } else {
                    onComplete(false, null, error)
                    return@uploadSingleImage
                }

                completedUploads++
                if (completedUploads == bitmaps.size) {
                    onComplete(true, imageIds, null)
                }
            }
        }
    }

    suspend fun getAllImages(): List<Pair<String, Bitmap>> {
        return try {
            val result = firestore.collection("images").get().await()
            result.documents.mapNotNull { document ->
                val base64String = document.getString("image")
                val id = document.getString("id")
                if (base64String != null && id != null) {
                    try {
                        val byteArray = Base64.decode(base64String, Base64.DEFAULT)
                        val bitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
                        Pair(id, bitmap)
                    } catch (e: Exception) {
                        null
                    }
                } else {
                    null
                }
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    private suspend fun getImageById(imageId: String): Pair<String, Bitmap>? {
        return try {
            val document = firestore.collection("images").document(imageId).get().await()
            val base64String = document.getString("image")
            if (base64String != null) {
                val byteArray = Base64.decode(base64String, Base64.DEFAULT)
                val bitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
                Pair(imageId, bitmap)
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    fun saveDevice(ownerId: String, deviceName: String, category: DeviceCategory?, description: String, price: String, imageIds: List<String>, callback: (Boolean, String?, String?) -> Unit) {
        if (ownerId.isEmpty()) {
            callback(false, null, "The owner of the device couldn't be registered.")
            return
        }
        if (deviceName.isEmpty()) {
            callback(false, null, "Please provide a device name.")
            return
        }
        if (category == null) {
            callback(false, null, "Please select a category for the device.")
            return
        }
        if (description.isEmpty()) {
            callback(false, null, "Please provide a device description.")
            return
        }
        if (description.length !in 5..500) {
            callback(false, null, "Please provide a valid phone number.")
            return
        }
        if (price.isEmpty()) {
            callback(false, null, "Please provide a realistic price for the lease of the device.")
            return
        }
        if (imageIds.isEmpty() || imageIds.size !in 1..5) {
            callback(false, null, "Please provide at least 1 and at most 5 images of the device.")
            return
        }

        val uuid = UUID.randomUUID().toString()

        val data = Device(description, uuid, deviceName, imageIds.toList(), ownerId, price, category.name)

        firestore.collection("devices").document(uuid)
            .set(data)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    callback(true, uuid, null) // Return the device ID on success
                } else {
                    callback(false, null, "Failed to save device data. Please try again.")
                }
            }
    }

    fun getCurrentUserId(): String? {
        return FirebaseAuth.getInstance().currentUser?.uid
    }

    fun getCurrentUserEmail(): String? {
        return FirebaseAuth.getInstance().currentUser?.email
    }

    fun getCurrentUser(callback: (Boolean, DocumentSnapshot?, String?) -> Unit) {
        val currentUserId = getCurrentUserId()
        if (currentUserId != null) {
            getUserById(currentUserId) { success, document, error ->
                callback(success, document, error)
            }
        } else {
            callback(false, null, "There is no user logged in.")
        }
    }

    private fun getAllDevices(callback: (Boolean, List<Device?>, String?) -> Unit) {
        firestore.collection("devices").get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val documents = task.result
                    if (documents != null && !documents.isEmpty) {
                        callback(true,
                            documents.mapNotNull { document ->
                                document.toObject<Device?>()?.copy(deviceId = document.id)
                            },
                            null
                        )
                    } else {
                        callback(false, emptyList(), "No devices found.")
                    }
                } else {
                    callback(false, emptyList(), "Failed to retrieve devices.")
                }
            }
    }

    fun getDeviceById(deviceId: String, callback: (Boolean, DocumentSnapshot?, String?) -> Unit) {
        firestore.collection("devices").document(deviceId).get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val document = task.result
                    if (document != null && document.exists()) {
                        callback(true, document, null)
                    } else {
                        callback(false, null, "The device with the specified ID does not exist.")
                    }
                } else {
                    callback(false, null, "Failed to retrieve the device by ID.")
                }
            }
    }

    fun getAllDevicesWithImages(callback: (Boolean, List<Pair<Device, List<Pair<String, Bitmap>>>>, String?) -> Unit) {
        getAllDevices { success, devices, errorMessage ->
            if (success) {
                val devicesWithImages = mutableListOf<Pair<Device, List<Pair<String, Bitmap>>>>()
                val jobs = mutableListOf<Job>()

                val scope = CoroutineScope(Dispatchers.IO)
                for (device in devices.filterNotNull()) {
                    val job = scope.launch {
                        val images = device.imageIds.mapNotNull { imageId ->
                            getImageById(imageId)
                        }
                        synchronized(devicesWithImages) {
                            devicesWithImages.add(Pair(device, images))
                        }
                    }
                    jobs.add(job)
                }

                scope.launch {
                    jobs.joinAll()
                    callback(true, devicesWithImages, null)
                }
            } else {
                callback(false, emptyList(), errorMessage)
            }
        }
    }
}
