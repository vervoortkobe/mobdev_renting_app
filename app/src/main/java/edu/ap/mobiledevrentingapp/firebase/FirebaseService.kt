package edu.ap.mobiledevrentingapp.firebase

import android.graphics.Bitmap
import android.util.Base64
import android.util.Log
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObject
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

    fun signup(email: String, password: String, fullName: String, phoneNumber: String, ibanNumber: String, country: String, city: String, zipCode: String, streetName: String, addressNr: String, latitude: Double, longitude: Double, callback: (Boolean, String?) -> Unit) {
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

                    saveUser(userId, fullName, phoneNumber, ibanNumber, country, city, zipCode, streetName, addressNr, latitude, longitude) { success, errorMessage ->
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

    private fun saveUser(userId: String, fullName: String, phoneNumber: String, ibanNumber: String, country: String, city: String, zipCode: String, streetName: String, addressNr: String, latitude: Double, longitude: Double, callback: (Boolean, String?) -> Unit) {
        val data = hashMapOf(
            "userId" to userId,
            "fullName" to fullName,
            "phoneNumber" to phoneNumber,
            "ibanNumber" to ibanNumber,
            "country" to country,
            "city" to city,
            "zipCode" to zipCode,
            "streetName" to streetName,
            "addressNr" to addressNr,
            "latitude" to latitude,
            "longitude" to longitude
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

    fun updateUserProfile(userId: String, fullName: String?, phoneNumber: String?, ibanNumber: String?, country: String?, city: String?, zipCode: String?, streetName: String?, addressNr: String?, latitude: Double?, longitude: Double?, callback: (Boolean, String?) -> Unit) {
        val data = mutableMapOf<String, Any?>()

        fullName?.let { data["fullName"] = it }
        phoneNumber?.let { data["phoneNumber"] = it }
        ibanNumber?.let { data["ibanNumber"] = it }
        country?.let { data["country"] = it }
        city?.let { data["city"] = it }
        zipCode?.let { data["zipCode"] = it }
        streetName?.let { data["streetName"] = it }
        addressNr?.let { data["addressNr"] = it }
        latitude?.let { data["latitude"] = it }
        longitude?.let { data["longitude"] = it }

        if (data.isEmpty()) {
            callback(false, "No data to update.")
            return
        }

        firestore.collection("users").document(userId)
            .update(data)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    callback(true, null)
                } else {
                    callback(false, "Failed to update user profile. Please try again.")
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

    fun saveDevice(ownerId: String, deviceName: String, category: DeviceCategory, description: String, price: String, images: List<String>, latitude: Double, longitude: Double, callback: (Boolean, String?, String?) -> Unit) {
        val deviceId = UUID.randomUUID().toString()
        val device = hashMapOf(
            "deviceId" to deviceId,
            "ownerId" to ownerId,
            "deviceName" to deviceName,
            "category" to category.toString(),
            "description" to description,
            "price" to price,
            "images" to images,
            "latitude" to latitude,
            "longitude" to longitude
        )

        firestore.collection("devices").document(deviceId)
            .set(device)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    callback(true, deviceId, null) // Return the device ID on success
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

    fun getAllDevices(callback: (Boolean, List<Device>, String?) -> Unit) {
        firestore.collection("devices").get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val documents = task.result
                    if (documents != null && !documents.isEmpty) {
                        callback(true,
                            documents.mapNotNull { document ->
                                document.toObject<Device>().copy(deviceId = document.id)
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

    fun deleteDeviceById(deviceId: String, callback: (Boolean, String?) -> Unit) {
        getRentalsByDeviceId(deviceId) { rentals ->
            if (rentals.isNotEmpty()) {
                callback(false, "The device is currently being rented and cannot be deleted.")
            } else {
                firestore.collection("devices").document(deviceId).delete()
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            callback(true, null)
                        } else {
                            callback(false, "Failed to delete the device. Error: ${task.exception?.message}")
                        }
                    }
                    .addOnFailureListener { exception ->
                        callback(false, "An error occurred while deleting the device: ${exception.message}")
                    }
            }
        }
    }

    fun getDevicesByUserId(userId: String, callback: (Boolean, List<DocumentSnapshot>?, String?) -> Unit) {
        firestore.collection("devices").whereEqualTo("ownerId", userId).get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val documents = task.result?.documents
                    if (!documents.isNullOrEmpty()) {
                        callback(true, documents, null)
                    } else {
                        callback(false, null, "No devices found for the specified user ID.")
                    }
                } else {
                    callback(false, null, "Failed to retrieve devices for the specified user ID.")
                }
            }
    }

    fun getRentalsByDeviceId(deviceId: String, callback: (List<Rental>) -> Unit) {
        firestore.collection("rentals")
            .whereEqualTo("deviceId", deviceId)
            .get()
            .addOnSuccessListener { querySnapshot ->
                val rentals = querySnapshot.documents.mapNotNull { doc ->
                    doc.toObject(Rental::class.java)
                }
                callback(rentals)
            }
            .addOnFailureListener {
                Log.e("FirebaseService", "Error getting rentals", it)
                callback(emptyList())
            }
    }

    fun createRental(rental: Rental, callback: (Boolean) -> Unit) {
        firestore.collection("rentals")
            .document(rental.rentalId)
            .set(rental)
            .addOnSuccessListener {
                callback(true)
            }
            .addOnFailureListener { e ->
                Log.e("FirebaseService", "Error creating rental", e)
                callback(false)
            }
    }

    fun getDevicesRentedByUser(userId: String, callback: (List<Pair<Device, String>>) -> Unit) {
        firestore.collection("rentals")
            .whereEqualTo("renterId", userId)
            .get()
            .addOnSuccessListener { querySnapshot ->
                val rentals = querySnapshot.documents.mapNotNull { doc ->
                    val rental = doc.toObject<Rental>()
                    rental?.apply { rentalId = doc.id }
                }

                if (rentals.isEmpty()) {
                    callback(emptyList())
                    return@addOnSuccessListener
                }

                val deviceIds = rentals.map { it.deviceId }

                firestore.collection("devices")
                    .whereIn(FieldPath.documentId(), deviceIds)
                    .get()
                    .addOnSuccessListener { devicesSnapshot ->
                        val devices = devicesSnapshot.documents.mapNotNull { doc ->
                            doc.toObject<Device>()?.copy(deviceId = doc.id)
                        }

                        val rentedDevicesWithNames = mutableListOf<Pair<Device, String>>()

                        rentals.forEach { rental ->
                            val device = devices.find { it.deviceId == rental.deviceId }
                            if (device != null) {
                                firestore.collection("users").document(rental.renterId).get()
                                    .addOnSuccessListener { userDoc ->
                                        val userName = userDoc.getString("fullName") ?: "Unknown User"
                                        rentedDevicesWithNames.add(Pair(device, userName))

                                        if (rentedDevicesWithNames.size == rentals.size) {
                                            callback(rentedDevicesWithNames)
                                        }
                                    }
                                    .addOnFailureListener {
                                        Log.e("FirebaseService", "Error getting user details", it)
                                        rentedDevicesWithNames.add(Pair(device, "Unknown User"))
                                        if (rentedDevicesWithNames.size == rentals.size) {
                                            callback(rentedDevicesWithNames)
                                        }
                                    }
                            }
                        }
                    }
                    .addOnFailureListener {
                        Log.e("FirebaseService", "Error getting rented devices", it)
                        callback(emptyList())
                    }
            }
            .addOnFailureListener {
                Log.e("FirebaseService", "Error getting rentals", it)
                callback(emptyList())
            }
    }

    fun getUserRentals(userId: String, callback: (List<Rental>) -> Unit) {
        firestore.collection("rentals")
            .whereEqualTo("renterId", userId)
            .get()
            .addOnSuccessListener { documents ->
                val rentals = documents.mapNotNull { document ->
                    document.toObject<Rental>()
                }
                callback(rentals)
            }
            .addOnFailureListener { _ ->
                callback(emptyList())
            }
    }

    fun getMyRentedOutDevices(userId: String, callback: (List<Triple<Device, User, Rental>>) -> Unit) {
        firestore.collection("rentals")
            .whereEqualTo("ownerId", userId)
            .get()
            .addOnSuccessListener { querySnapshot ->
                val rentals = querySnapshot.documents.mapNotNull { doc ->
                    val rental = doc.toObject<Rental>()
                    rental?.apply { rentalId = doc.id }
                }

                if (rentals.isEmpty()) {
                    Log.d("FirebaseService", "No rentals found for user: $userId")
                    callback(emptyList())
                    return@addOnSuccessListener
                }

                val deviceIds = rentals.map { it.deviceId }

                firestore.collection("devices")
                    .whereIn(FieldPath.documentId(), deviceIds)
                    .get()
                    .addOnSuccessListener { devicesSnapshot ->
                        val devices = devicesSnapshot.documents.mapNotNull { doc ->
                            doc.toObject<Device>()?.copy(deviceId = doc.id)
                        }

                        val rentedOutDevices = mutableListOf<Triple<Device, User, Rental>>()

                        rentals.forEach { rental ->
                            val device = devices.find { it.deviceId == rental.deviceId }
                            if (device != null) {
                                firestore.collection("users").document(rental.renterId).get()
                                    .addOnSuccessListener { userDoc ->
                                        val user = userDoc.toObject<User>() ?: User("Unknown", "Unknown")
                                        rentedOutDevices.add(Triple(device, user, rental))

                                        if (rentedOutDevices.size == rentals.size) {
                                            callback(rentedOutDevices)
                                        }
                                    }
                                    .addOnFailureListener {
                                        Log.e("FirebaseService", "Error getting user details", it)
                                        rentedOutDevices.add(Triple(device, User("Unknown", "Unknown"), rental))
                                        if (rentedOutDevices.size == rentals.size) {
                                            callback(rentedOutDevices)
                                        }
                                    }
                            }
                        }
                    }
                    .addOnFailureListener {
                        Log.e("FirebaseService", "Error getting rented devices", it)
                        callback(emptyList())
                    }
            }
            .addOnFailureListener {
                Log.e("FirebaseService", "Error getting rentals", it)
                callback(emptyList())
            }
    }

    fun sendChat(senderId: String, receiverId: String, message: String, deviceId: String, callback: (Boolean, String?) -> Unit) {
        val chatId = UUID.randomUUID().toString()
        val chat = Chat(
            id = chatId,
            senderId = senderId,
            receiverId = receiverId,
            message = message,
            timestamp = System.currentTimeMillis(),
            deviceId = deviceId
        )

        firestore.collection("chats")
            .document(chatId)
            .set(chat)
            .addOnSuccessListener {
                callback(true, null)
            }
            .addOnFailureListener { e ->
                callback(false, e.message)
            }
    }

    fun getChatsForUser(userId: String, callback: (List<Triple<Chat, User, Device>>) -> Unit) {
        firestore.collection("chats")
            .whereEqualTo("senderId", userId)
            .get()
            .addOnSuccessListener { sentChats ->
                firestore.collection("chats")
                    .whereEqualTo("receiverId", userId)
                    .get()
                    .addOnSuccessListener { receivedChats ->
                        val allChats = (sentChats.documents + receivedChats.documents)
                            .map { it.toObject<Chat>()!! }
                            .distinctBy { listOf(it.senderId, it.receiverId, it.deviceId).sorted() }

                        val result = mutableListOf<Triple<Chat, User, Device>>()
                        var completed = 0

                        if (allChats.isEmpty()) {
                            callback(emptyList())
                            return@addOnSuccessListener
                        }

                        for (chat in allChats) {
                            val otherUserId = if (chat.senderId == userId) chat.receiverId else chat.senderId
                            getUserById(otherUserId) { userSuccess, userDoc, _ ->
                                if (userSuccess && userDoc != null) {
                                    getDeviceById(chat.deviceId) { deviceSuccess, deviceDoc, _ ->
                                        if (deviceSuccess && deviceDoc != null) {
                                            val user = userDoc.toObject<User>()!!
                                            val device = deviceDoc.toObject<Device>()!!
                                            result.add(Triple(chat, user, device))
                                        }
                                        completed++
                                        if (completed == allChats.size) {
                                            callback(result.sortedByDescending { it.first.timestamp })
                                        }
                                    }
                                }
                            }
                        }
                    }
            }
    }

    fun getChatMessages(userId1: String, userId2: String, deviceId: String, callback: (List<Chat>) -> Unit) {
        firestore.collection("chats")
            .whereIn("senderId", listOf(userId1, userId2))
            .whereIn("receiverId", listOf(userId1, userId2))
            .whereEqualTo("deviceId", deviceId)
            .get()
            .addOnSuccessListener { documents ->
                val chats = documents.mapNotNull { it.toObject<Chat>() }
                    .sortedBy { it.timestamp }
                callback(chats)
            }
            .addOnFailureListener {
                callback(emptyList())
            }
    }
}