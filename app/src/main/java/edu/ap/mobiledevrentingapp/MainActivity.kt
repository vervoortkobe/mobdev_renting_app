package edu.ap.mobiledevrentingapp

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import com.google.firebase.auth.FirebaseAuth
import edu.ap.mobiledevrentingapp.ui.theme.MobileDevRentingAppTheme

class MainActivity : ComponentActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = FirebaseAuth.getInstance()

        // Check if user is logged in
        val currentUser = auth.currentUser

        setContent {
            MobileDevRentingAppTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    if (currentUser != null) {
                        // User is logged in, display main app content
                        Text(text = "Welcome, ${currentUser.email}!")
                    } else {
                        // User is not logged in, redirect to LoginActivity
                        val intent = Intent(this, LoginActivity::class.java)
                        startActivity(intent)
                        finish() // Close MainActivity to avoid stacking activities
                    }
                }
            }
        }
    }
}