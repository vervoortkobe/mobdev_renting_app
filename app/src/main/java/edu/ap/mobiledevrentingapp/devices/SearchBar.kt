package edu.ap.mobiledevrentingapp.devices

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import edu.ap.mobiledevrentingapp.R
import edu.ap.mobiledevrentingapp.ui.theme.Yellow40

@Composable
fun SearchBar(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    TextField(
        value = searchQuery,
        singleLine = true,
        onValueChange = onSearchQueryChange,
        placeholder = { Text(context.getString(R.string.devices_search_placeholder)) },
        trailingIcon = {
            if (searchQuery.isNotEmpty()) {
                IconButton(onClick = { onSearchQueryChange("") }) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = context.getString(R.string.devices_clear_search),
                        tint = Color.Gray
                    )
                }
            }
        },
        modifier = modifier,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Yellow40,
            unfocusedBorderColor = Color.Transparent,
            disabledBorderColor = Color.Transparent,
            errorBorderColor = Color.Transparent
        )
    )
}