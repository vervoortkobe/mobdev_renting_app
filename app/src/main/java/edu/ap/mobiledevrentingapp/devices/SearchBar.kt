package edu.ap.mobiledevrentingapp.devices

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import edu.ap.mobiledevrentingapp.ui.theme.Yellow40

@Composable
fun SearchBar(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = searchQuery,
        singleLine = true,
        onValueChange = onSearchQueryChange,
        placeholder = { Text("Search devices...") },
        modifier = modifier.height(56.dp)
            .background(Color.White, RoundedCornerShape(4.dp)),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Yellow40,
            unfocusedBorderColor = Color.Gray
        )
    )
}