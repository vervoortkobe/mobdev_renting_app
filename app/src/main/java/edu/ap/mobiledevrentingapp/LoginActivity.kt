package edu.ap.mobiledevrentingapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        auth = FirebaseAuth.getInstance()

        // Login Button
        val loginButton = findViewById<Button>(R.id.loginButton)
        loginButton.setOnClickListener {
            val email = findViewById<EditText>(R.id.emailEditText).text.toString()
            val password = findViewById<EditText>(R.id.passwordEditText).text.toString()

            FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        // Login successful, navigate to main activity or desired screen
                        val intent = Intent(this, MainActivity::class.java)
                        startActivity(intent)
                        finish()
                    } else {
                        // Handle login failure
                        Toast.makeText(this, "Login failed", Toast.LENGTH_SHORT).show()
                    }
                }
        }

        // Signup Button
        val signupButton = findViewById<Button>(R.id.signupButton)
        signupButton.setOnClickListener {
            // Navigate to SignupActivity
            val intent = Intent(this, SignupActivity::class.java) // Replace with your SignupActivity class name
            startActivity(intent)
        }
    }
}