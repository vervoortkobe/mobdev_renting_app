package edu.ap.mobiledevrentingapp.devices

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import edu.ap.mobiledevrentingapp.ui.theme.MobileDevRentingAppTheme
import edu.ap.mobiledevrentingapp.ui.theme.Yellow40

@Composable
fun DevicesPage(navController: NavController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(2.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            Button(
                onClick = { navController.navigate("map") },
                modifier = Modifier
                    .padding(end = 8.dp)
                    .width(180.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Yellow40,
                    disabledContainerColor = Yellow40
                )
            ) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = "Map Icon",
                    modifier = Modifier
                        .size(20.dp)
                        .padding(end = 4.dp)
                )
                Text("Map")
            }
            Button(
                onClick = { navController.navigate("add_device") },
                modifier = Modifier.width(180.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Yellow40,
                    disabledContainerColor = Yellow40
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Device Icon",
                    modifier = Modifier
                        .size(20.dp)
                        .padding(end = 4.dp)
                )
                Text("Add Device")
            }
        }

        DisplayDevicesWithImages()
    }
}

@Preview(showBackground = true)
@Composable
fun DevicesPagePreview() {
    MobileDevRentingAppTheme {
        DevicesPage(navController = rememberNavController())
    }
}