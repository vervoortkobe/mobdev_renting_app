package edu.ap.mobiledevrentingapp.devices

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import edu.ap.mobiledevrentingapp.R
import edu.ap.mobiledevrentingapp.firebase.AppUtil
import edu.ap.mobiledevrentingapp.firebase.DeviceCategory

@Composable
fun DropdownListDevices(
    categories: List<DeviceCategory>,
    selectedCategoryIndex: Int,
    onCategorySelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    val context = LocalContext.current

    Box(
        modifier = modifier
            .background(Color.White, RoundedCornerShape(4.dp))
            .border(1.dp, Color.Gray, RoundedCornerShape(4.dp))
            .clickable { expanded = true }
            .padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = if (selectedCategoryIndex == 0) context.getString(R.string.devices_all_categories) else AppUtil.convertUppercaseToTitleCase(categories[selectedCategoryIndex - 1].name),
            color = Color.Black
        )
    }

    if (expanded) {
        Popup(
            alignment = Alignment.TopStart,
            onDismissRequest = { expanded = false },
            properties = PopupProperties(focusable = true)
        ) {
            Column(
                modifier = Modifier
                    .background(Color.White)
                    .border(1.dp, Color.Gray)
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            onCategorySelected(0)
                            expanded = false
                        }
                        .padding(8.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Text(context.getString(R.string.devices_all))
                }
                categories.forEachIndexed { index, category ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onCategorySelected(index + 1)
                                expanded = false
                            }
                            .padding(8.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Text(AppUtil.convertUppercaseToTitleCase(category.name))
                    }
                }
            }
        }
    }
}