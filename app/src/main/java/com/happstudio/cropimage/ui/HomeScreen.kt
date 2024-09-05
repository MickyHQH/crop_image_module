package com.happstudio.cropimage.ui

import android.Manifest
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
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
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.ContentAlpha
import androidx.wear.compose.material.LocalContentAlpha
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.happstudio.cropimage.MainViewModel
import com.happstudio.cropimage.compose.ErrorDialog
import com.happstudio.cropimage.ui.theme.CropImageTheme
import com.happstudio.cropimage.ui.utils.ImageOrientation
import com.happstudio.cropimage.ui.utils.bitmapToFileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun HomeScreen(
    mainViewModel: MainViewModel,
    gotoCropScreen: (String) -> Unit = {},
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var showErrorDialog by remember { mutableStateOf<Pair<Boolean, String?>>(false to null) }


    fun goToNext() {
        scope.launch(Dispatchers.IO) {
            val encodeUrl = URLEncoder.encode(
                mainViewModel.uri.toString(),
                StandardCharsets.UTF_8.toString(),
            )
            withContext(Dispatchers.Main) {
                gotoCropScreen.invoke(encodeUrl)
            }
        }
    }

    val cameraActivityResult =
        rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) {
            if (it) {
                if (mainViewModel.uri.path?.isNotEmpty() == true) {
                    goToNext()
                }
            }
        }

    fun handleOnSelectImage(imageUri: Uri?) {
        if (imageUri != null) {
            scope.launch(Dispatchers.IO) {
                ImageOrientation.modifyOrientation(context, imageUri)?.let {
                    val convertToFile =
                        bitmapToFileProvider(context, it, mainViewModel.file)
                    if (convertToFile != null) {
                        goToNext()
                    } else {
                        showErrorDialog = true to "Error during processing"
                    }
                }
            }
        }
    }

    val pickMediaLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) {
            handleOnSelectImage(
                it
            )
        }
    val imageLauncher =
        rememberLauncherForActivityResult(contract = ActivityResultContracts.GetContent()) {
            handleOnSelectImage(
                it
            )
        }
    val galleryPermission =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            rememberPermissionState(permission = Manifest.permission.READ_MEDIA_IMAGES) { result ->
                if (result) {
                    pickMediaLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                } else {
                    showErrorDialog = true to "Permission deny"
                }
            }
        } else {
            rememberPermissionState(permission = Manifest.permission.READ_EXTERNAL_STORAGE) { result ->
                if (result) {
                    imageLauncher.launch("image/*")
                } else {
                    showErrorDialog = true to "Permission deny"
                }
            }
        }
    val cameraPermission =
        rememberPermissionState(permission = Manifest.permission.CAMERA) { hasPermission ->
            if (hasPermission) {
                cameraActivityResult.launch(mainViewModel.uri)
            } else {
                showErrorDialog = true to "Permission deny"
            }
        }
    if (showErrorDialog.first) {
        ErrorDialog(showErrorDialog.second) {
            showErrorDialog = false to null
        }
    }
    HomeUI(
        onTakePhoto = {
            if (cameraPermission.status.isGranted) {
                cameraActivityResult.launch(mainViewModel.uri)
            } else {
                cameraPermission.launchPermissionRequest()
            }
        },
        onChooseFromGallery = {
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.S_V2) {
                if (galleryPermission.status.isGranted) {
                    imageLauncher.launch("image/*")
                } else {
                    galleryPermission.launchPermissionRequest()
                }
            } else {
                if (galleryPermission.status.isGranted) {
                    pickMediaLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                } else {
                    galleryPermission.launchPermissionRequest()
                }
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeUI(
    modifier: Modifier = Modifier,
    onTakePhoto: () -> Unit = {},
    onChooseFromGallery: () -> Unit = {},
) {
    Scaffold(
        topBar = {
            TopAppBar(title = { Text(text = "CropApp") })
        }
    ) {
        Box(
            modifier = modifier
                .padding(it)
                .fillMaxSize(),
        ) {
            Column(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(bottom = 24.dp)
                    .width(intrinsicSize = IntrinsicSize.Max),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(text = "Select or take a image: ", style = MaterialTheme.typography.titleLarge)
                Spacer(modifier = Modifier.height(15.dp))
                ElevatedButton(onClick = onTakePhoto, modifier = Modifier.fillMaxWidth()) {
                    Text(text = "Take a photo")
                }
                Spacer(modifier = Modifier.height(10.dp))
                ElevatedButton(onClick = onChooseFromGallery, modifier = Modifier.fillMaxWidth()) {
                    Text(text = "Choose from gallery")
                }
            }
            CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
                Column(
                    modifier = Modifier.align(Alignment.BottomCenter).padding(20.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(text = "Author: Huy Ha Quang (Micky)\nEmail: cv.huyha1401@gmail.com\nTechnical: Android Jetpack Compose\nLast updated: 19/8/2024",
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.secondary.copy(alpha = LocalContentAlpha.current),
                        )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    CropImageTheme {
        HomeUI()
    }
}
