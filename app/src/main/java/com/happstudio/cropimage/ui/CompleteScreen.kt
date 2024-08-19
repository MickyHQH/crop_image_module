package com.happstudio.cropimage.ui

import android.net.Uri
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.happstudio.cropimage.MainViewModel
import com.happstudio.cropimage.compose.ErrorDialog
import com.happstudio.cropimage.createImageFromOldUri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun CompleteScreen(
    mainViewModel: MainViewModel,
    gotoHome: () -> Unit,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var showNotifyDialog by remember { mutableStateOf<Pair<Boolean, String?>>(false to null) }
    var showErrorDialog by remember { mutableStateOf<Pair<Boolean, String?>>(false to null) }

    if (showNotifyDialog.first) {
        ErrorDialog(showNotifyDialog.second, title = "Success") {
            showNotifyDialog = false to null
        }
    }
    if (showErrorDialog.first) {
        ErrorDialog(showErrorDialog.second) {
            showErrorDialog = false to null
        }
    }
    CompleteUI(
        imageUri = mainViewModel.uri,
        gotoHome = gotoHome,
        onSave = {
            scope.launch(Dispatchers.IO) {
                try {
                    if (context.createImageFromOldUri(mainViewModel.uri) != null) {
                        showNotifyDialog = true to "Saved successfully"
                    } else {
                        showErrorDialog = true to "Error during processing"
                    }
                } catch (e: Exception) {
                    showErrorDialog = true to (e.message ?: "Error during processing")
                }
            }
        },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompleteUI(
    imageUri: Uri?,
    gotoHome: () -> Unit = {},
    onSave: () -> Unit = {},
) {
    Scaffold(
        topBar = {
            TopAppBar(title = { Text(text = "Complete") })
        }
    ) {
        Column(
            modifier = Modifier
                .padding(it)
                .fillMaxSize()
                .padding(bottom = 30.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = "Congratulations! \nThis is your image",
                style = MaterialTheme.typography.titleLarge,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(20.dp))
            if (imageUri == null)
                Box(
                    modifier = Modifier
                        .fillMaxWidth(.6f)
                        .height(150.dp)
                        .border(1.dp, Color.DarkGray),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "No image to display", textAlign = TextAlign.Center)
                }
            else
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(imageUri)
                        .build(),
                    contentDescription = "icon",
                    contentScale = ContentScale.Inside,
                )
            Spacer(modifier = Modifier.height(20.dp))
            Row {
                ElevatedButton(onClick = gotoHome) {
                    Text(text = "Go to home")
                }
                Spacer(modifier = Modifier.width(20.dp))
                ElevatedButton(onClick = onSave) {
                    Text(text = "Save to device")
                }
            }
        }
    }
}

@Composable
@Preview(showBackground = true)
fun CompleteScreenPreview() {
    CompleteUI(null)
}