package edu.ap.mobiledevrentingapp

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth
import edu.ap.mobiledevrentingapp.ui.theme.Black
import edu.ap.mobiledevrentingapp.ui.theme.Red40

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

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onNavigateToSignup: () -> Unit
) {
    val context = LocalContext.current
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        Image(
            painter = painterResource(id = R.drawable.background), // Replace with your image resource
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("E-mail", color = Black) },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Red40,
                    unfocusedBorderColor = Black,
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password", color = Black) },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Red40,
                    unfocusedBorderColor = Black,
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    FirebaseService.login(email, password) { success, errorMessage ->
                        if (success) {
                            onLoginSuccess()
                        } else {
                            Toast.makeText(
                                context,
                                errorMessage,
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Red40)
            ) {
                Text("Log In")
            }

            Spacer(modifier = Modifier.height(8.dp))

            TextButton(
                onClick = onNavigateToSignup,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Don't have an account yet? Sign up!", color = Black)
            }
        }
    }
}