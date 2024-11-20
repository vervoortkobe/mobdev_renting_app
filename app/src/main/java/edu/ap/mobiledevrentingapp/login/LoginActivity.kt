package edu.ap.mobiledevrentingapp.login

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import edu.ap.mobiledevrentingapp.MainActivity
import edu.ap.mobiledevrentingapp.signup.SignupActivity

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        auth = FirebaseAuth.getInstance()

        setContent {
        LoginScreen(
                onLoginSuccess = {
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                    finish()
                },
                onNavigateToSignup = {
                    val intent = Intent(this, SignupActivity::class.java)
                    startActivity(intent)
                }
            )
        }
    }
}