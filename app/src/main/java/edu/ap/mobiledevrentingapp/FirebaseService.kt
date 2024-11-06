package edu.ap.mobiledevrentingapp

import android.graphics.Bitmap
import android.util.Base64
import android.util.Log
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.firestore.FirebaseFirestore
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
                    val errorMessage = when (val exception = task.exception) {
                        is FirebaseAuthInvalidCredentialsException -> "The e-mail or password is incorrect."
                        is FirebaseAuthInvalidUserException -> "There is no account associated with this email."
                        is FirebaseNetworkException -> "Network error. Please try again."
                        else -> "Login failed. Please check your credentials and try again."
                    }
                    callback(false, errorMessage)
                }
            }
    }

    fun signup(email: String, password: String, fullname: String, callback: (Boolean, String?) -> Unit) {

        if(fullname.isEmpty()) {
            callback(false, "Please provide your full name.")
            return
        }

        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val userId: String = task.result.user?.uid.toString();

                    saveUser(userId, fullname) { success, errorMessage ->
                        if (success) {
                            callback(true, null)
                        } else {
                            callback(false, errorMessage)
                        }
                    }

                } else {
                    val errorMessage = when (val exception = task.exception) {
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

    private fun saveUser(userId: String, fullname: String, callback: (Boolean, String?) -> Unit) {
        val data = hashMapOf(
            "userId" to userId,
            "fullname" to fullname
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

    fun getUserById(userId: String, callback: (Boolean, String?, String?) -> Unit) {
        firestore.collection("users").document(userId).get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val document = task.result
                    if (document != null) {
                        val fullname = document.getString("fullname")
                        callback(true, fullname, null)
                    } else {
                        callback(false, null, "The current user does not exist.")
                    }
                } else {
                    callback(false, null, "Failed to retrieve the details of the current user.")
                }
            }
    }

    private fun uploadSingleImage(bitmap: Bitmap, onComplete: (Boolean, String?) -> Unit) {
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
}