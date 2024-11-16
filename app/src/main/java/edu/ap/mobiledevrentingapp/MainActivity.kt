package edu.ap.mobiledevrentingapp

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.google.firebase.auth.FirebaseAuth
import edu.ap.mobiledevrentingapp.osm.MapPage
import edu.ap.mobiledevrentingapp.ui.theme.MobileDevRentingAppTheme

class MainActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = FirebaseAuth.getInstance()

        val currentUser = auth.currentUser

        setContent {
            MobileDevRentingAppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    if (currentUser != null) {
                        MainPage(onLogout = { signOut() })
                    } else {
                        val intent = Intent(this, LoginActivity::class.java)
                        startActivity(intent)
                        finish()
                    }
                }
            }
        }
    }

    private fun signOut() {
        auth.signOut()
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }
}

@Composable
fun MainPage(onLogout: () -> Unit) {
    val navController = rememberNavController()
    Scaffold(
        bottomBar = { BottomNavigationBar(navController = navController) },
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = "home",
            modifier = Modifier.padding(paddingValues)
        ) {
            composable("devices") { DevicesPage(navController = navController) }
            composable("home") { HomePage(onLogout = onLogout) }
            composable("profile") { ProfilePage() }
            composable("map") { MapPage() }
            composable("add_device") { AddDevicePage(navController = navController) }
        }
    }
}

@Composable
fun BottomNavigationBar(navController: NavHostController) {
    // Define the background color
    val backgroundColor = Color(0xFFFF5232)
    val itemColor = Color(0xFFFF9e81)

    NavigationBar(
        containerColor = backgroundColor // Set the background color here
    ) {
        val currentDestination = navController.currentBackStackEntryAsState().value?.destination
        val items = listOf(
            NavigationItem("devices", Icons.Filled.List, "Devices"),
            NavigationItem("home", Icons.Filled.Home, "Home"),
            NavigationItem("profile", Icons.Filled.Person, "Profile")
        )

        items.forEach { item ->
            NavigationBarItem(
                icon = { Icon(item.icon, contentDescription = item.label) },
                label = { Text(item.label, maxLines = 1) }, // Set maxLines to 1
                selected = currentDestination?.route == item.route,
                onClick = {
                    navController.navigate(item.route) {
                        // Clear previous back stack to prevent navigation issues
                        popUpTo(navController.graph.startDestinationId) {
                            inclusive = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                colors = NavigationBarItemDefaults.colors(
                    indicatorColor = itemColor,
                    selectedIconColor = Color.White,
                    unselectedIconColor = Color.LightGray,
                    selectedTextColor = Color.White,
                    unselectedTextColor = Color.LightGray
                )
            )
        }
    }
}


data class NavigationItem(val route: String, val icon: ImageVector, val label: String)
