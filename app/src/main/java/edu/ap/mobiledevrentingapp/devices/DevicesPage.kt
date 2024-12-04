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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import edu.ap.mobiledevrentingapp.R
import edu.ap.mobiledevrentingapp.ui.theme.MobileDevRentingAppTheme
import edu.ap.mobiledevrentingapp.ui.theme.Yellow40

@Composable
fun DevicesPage(navController: NavController) {
    val context = LocalContext.current

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
                    .width(192.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Yellow40,
                    disabledContainerColor = Yellow40
                )
            ) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = context.getString(R.string.devices_map_icon),
                    modifier = Modifier
                        .size(22.dp)
                        .padding(end = 4.dp)
                )
                Text("Map", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium, fontSize = 16.sp))
            }
            Button(
                onClick = { navController.navigate("add_device") },
                modifier = Modifier.width(192.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Yellow40,
                    disabledContainerColor = Yellow40
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = context.getString(R.string.devices_add_icon),
                    modifier = Modifier
                        .size(22.dp)
                        .padding(end = 4.dp)
                )
                Text(context.getString(R.string.devices_add_button), style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium, fontSize = 16.sp))
            }
        }

        DisplayDevicesWithImages(navController = navController)
    }
}

@Preview(showBackground = true)
@Composable
fun DevicesPagePreview() {
    MobileDevRentingAppTheme {
        DevicesPage(navController = rememberNavController())
    }
}