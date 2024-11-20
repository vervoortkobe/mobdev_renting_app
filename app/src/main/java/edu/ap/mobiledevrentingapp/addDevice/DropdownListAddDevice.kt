package edu.ap.mobiledevrentingapp.addDevice

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import edu.ap.mobiledevrentingapp.firebase.AppUtil
import edu.ap.mobiledevrentingapp.firebase.DeviceCategory

@Composable
fun DropdownListAddDevice(selectedIndex: Int, onItemClick: (Int) -> Unit) {

    var showDropdown by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()

    Spacer(modifier = Modifier.height(8.dp))

    Column(
        modifier = Modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center) {

        Box(
            modifier = Modifier.fillMaxWidth()
                .background(Color.Transparent, shape = RoundedCornerShape(6.dp))
                .border(
                    width = 1.dp,
                    color = Color.Black,
                    shape = RoundedCornerShape(4.dp)
                )
                .clickable { showDropdown = !showDropdown }
                .padding(12.dp)
                .fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .heightIn(max = 120.dp)
                    .verticalScroll(state = scrollState)
            ) {
                Text(
                    text = AppUtil.convertUppercaseToTitleCase(enumValues<DeviceCategory>()[selectedIndex].name),
                    modifier = Modifier.padding(3.dp),
                    color = Color.Black
                )
            }
        }

        Box {
            if (showDropdown) {
                Popup(
                    alignment = Alignment.TopCenter,
                    properties = PopupProperties(
                        excludeFromSystemGesture = true,
                    ),
                    onDismissRequest = { showDropdown = false }
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp, 0.dp)
                            .heightIn(max = 155.dp)
                            .verticalScroll(state = scrollState)
                            .border(width = 2.dp, shape = RoundedCornerShape(6.dp), color = Color.Black)
                    ) {
                        enumValues<DeviceCategory>().onEachIndexed { index, item ->
                            if (index != 0) {
                                HorizontalDivider(thickness = 1.dp, color = Color.Gray)
                            }
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color.White)
                                    .padding(12.dp, 3.dp)
                                    .clickable {
                                        onItemClick(index)
                                        showDropdown = !showDropdown
                                    },
                            ) {
                                Text(text = AppUtil.convertUppercaseToTitleCase(item.name), modifier = Modifier.padding(3.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}