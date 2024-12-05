@file:OptIn(ExperimentalMaterial3Api::class)

package edu.ap.mobiledevrentingapp.userView

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.google.firebase.firestore.DocumentSnapshot
import edu.ap.mobiledevrentingapp.firebase.AppUtil
import edu.ap.mobiledevrentingapp.firebase.FirebaseService
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import edu.ap.mobiledevrentingapp.R
import edu.ap.mobiledevrentingapp.firebase.AppUtil.decode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

@Composable
fun UserListPage(
    onUserClick: (String) -> Unit,
    viewModel: UserListViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Users") })
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            when (uiState) {
                is UserListUiState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.fillMaxSize())
                }
                is UserListUiState.Success -> {
                    val users = (uiState as UserListUiState.Success).users
                    if (users.isEmpty()) {
                        Text(
                            text = "No users found.",
                            modifier = Modifier.fillMaxSize(),
                            style = MaterialTheme.typography.bodyLarge
                        )
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(users.size) { index ->
                                UserListItem(user = users[index], onClick = onUserClick)
                            }
                        }
                    }
                }
                is UserListUiState.Error -> {
                    val message = (uiState as UserListUiState.Error).message
                    Text(
                        text = "Error: $message",
                        modifier = Modifier.fillMaxSize(),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }
    }
}

@Composable
fun UserListItem(user: DocumentSnapshot, onClick: (String) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable { onClick(user.id)  }
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val context = LocalContext.current
            val encodedImage = user.getString("profileImage")
            val profileBitmap = if (!encodedImage.isNullOrEmpty()) decode(encodedImage) else null

            if (profileBitmap != null) {
                Image(
                    bitmap = profileBitmap.asImageBitmap(),
                    contentDescription = context.getString(R.string.profile_image),
                    modifier = Modifier
                        .size(50.dp)
                        .clip(CircleShape)
                        .border(2.dp, Color.LightGray, CircleShape),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(50.dp)
                        .clip(CircleShape)
                        .background(Color.Gray)
                        .border(2.dp, Color.LightGray, CircleShape)
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            BasicText(
                text = user.getString("fullName") ?: "Unknown"
            )
        }
    }
}


class UserListViewModel : ViewModel() {
    private val _uiState = MutableStateFlow<UserListUiState>(UserListUiState.Loading)
    val uiState: StateFlow<UserListUiState> get() = _uiState

    init {
        fetchUsers()
    }

    private fun fetchUsers() {
        FirebaseService.getAllUsers { isSuccess, documents, errorMessage ->
            viewModelScope.launch {
                if (isSuccess && documents != null) {
                    _uiState.value = UserListUiState.Success(users = documents)
                } else {
                    _uiState.value = UserListUiState.Error(message = errorMessage ?: "Unknown error")
                }
            }
        }
    }
}

// UI State
sealed class UserListUiState {
    object Loading : UserListUiState()
    data class Success(val users: List<DocumentSnapshot>) : UserListUiState()
    data class Error(val message: String) : UserListUiState()
}
