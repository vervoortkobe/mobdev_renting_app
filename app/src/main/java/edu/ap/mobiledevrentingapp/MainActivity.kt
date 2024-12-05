package edu.ap.mobiledevrentingapp

import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Home
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
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.google.firebase.auth.FirebaseAuth
import edu.ap.mobiledevrentingapp.addDevice.AddDevicePage
import edu.ap.mobiledevrentingapp.devices.DevicesPage
import edu.ap.mobiledevrentingapp.home.HomePage
import edu.ap.mobiledevrentingapp.login.LoginActivity
import edu.ap.mobiledevrentingapp.map.MapPage
import edu.ap.mobiledevrentingapp.profile.ProfilePage
import edu.ap.mobiledevrentingapp.ui.theme.MobileDevRentingAppTheme
import edu.ap.mobiledevrentingapp.ui.theme.Yellow40
import edu.ap.mobiledevrentingapp.deviceDetails.DeviceDetailsPage
import edu.ap.mobiledevrentingapp.profile.ProfilePageSettings
import edu.ap.mobiledevrentingapp.userView.UserListPage

class MainActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

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

@RequiresApi(Build.VERSION_CODES.O)
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
            composable("home") { HomePage(navController = navController) }
            composable("profile") { ProfilePage(navController = navController, onLogout = onLogout) }
            composable("profileSettings") { ProfilePageSettings(navController = navController) }
            composable("map") { MapPage(navController = navController) }
            composable("add_device") { AddDevicePage(navController = navController) }
            composable(
                route = "device_details/{deviceId}",
                arguments = listOf(
                    navArgument("deviceId") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val deviceId = backStackEntry.arguments?.getString("deviceId")
                requireNotNull(deviceId) { "Device ID parameter wasn't found" }
                DeviceDetailsPage(navController = navController, deviceId = deviceId)
            }
            composable("users") { UserListPage() }
        }
    }
}

@Composable
fun BottomNavigationBar(navController: NavHostController) {
    // Define the background color
    val backgroundColor = Yellow40
    val itemColor = Color.Black

    NavigationBar(
        containerColor = backgroundColor // Set the background color here
    ) {
        val currentDestination = navController.currentBackStackEntryAsState().value?.destination
        val items = listOf(
            NavigationItem("devices", Icons.AutoMirrored.Filled.List, "Devices"),
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