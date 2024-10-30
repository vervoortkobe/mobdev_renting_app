package edu.ap.mobiledevrentingapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth

class SignupActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        auth = FirebaseAuth.getInstance()

        val signupButton
                = findViewById<Button>(R.id.signup_button)
        signupButton.setOnClickListener {
            val email = findViewById<EditText>(R.id.signup_email).text.toString()
            val password = findViewById<EditText>(R.id.signup_password).text.toString()


            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        val intent = Intent(this, MainActivity::class.java)
                        startActivity(intent)
                        finish()
                    } else {
                        Toast.makeText(baseContext, "Sign up failed: ${task.exception}", Toast.LENGTH_SHORT).show()
                    }
                }
        }
    }
}