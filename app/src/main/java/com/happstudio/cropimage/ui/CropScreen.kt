package com.happstudio.cropimage.ui

import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.happstudio.cropimage.MainViewModel
import com.happstudio.cropimage.R
import com.happstudio.cropimage.compose.ErrorDialog
import com.happstudio.cropimage.compose.LoadingDialog
import com.happstudio.cropimage.ui.utils.ImageOrientation
import com.happstudio.cropimage.ui.utils.bitmapToFileProvider
import com.smarttoolfactory.cropper.ImageCropper
import com.smarttoolfactory.cropper.model.OutlineType
import com.smarttoolfactory.cropper.model.RectCropShape
import com.smarttoolfactory.cropper.model.aspectRatios
import com.smarttoolfactory.cropper.settings.CropDefaults
import com.smarttoolfactory.cropper.settings.CropOutlineProperty
import kotlinx.coroutines.launch
import java.net.URLDecoder
import java.nio.charset.StandardCharsets

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CropScreen(
    mainViewModel: MainViewModel,
    imageUri: String,
    onNext: () -> Unit,
    onBack: () -> Unit
) {
    val decode = URLDecoder.decode(imageUri, StandardCharsets.UTF_8.toString())
    val convertStringToUri = Uri.parse(decode)
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    var showLoadingDialog by remember { mutableStateOf(false) }
    var showErrorDialog by remember { mutableStateOf<Pair<Boolean, String?>>(false to null) }
    var crop by remember { mutableStateOf(false) }
    val handleSize: Float = LocalDensity.current.run { 20.dp.toPx() }
    var bitmapImageSelected by remember {
        mutableStateOf<ImageBitmap?>(null)
    }
    LaunchedEffect(key1 = imageUri) {
        coroutineScope.launch {
            bitmapImageSelected = ImageOrientation.modifyOrientation(
                context,
                convertStringToUri,
            )?.asImageBitmap()
        }
    }
    if (showLoadingDialog) {
        LoadingDialog {
            showLoadingDialog = false
        }
    }
    if (showErrorDialog.first) {
        ErrorDialog(showErrorDialog.second) {
            showErrorDialog = false to null
        }
    }
    Scaffold(
        topBar = {
            TopAppBar(title = { Text(text = "Edit photo") }, navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(
                        Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                        contentDescription = null,
                        modifier = Modifier.size(26.dp),
                    )
                }
            })
        }
    ) { it ->
        Column(
            modifier = Modifier
                .padding(it)
                .fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
                    .padding(horizontal = 15.dp, vertical = 10.dp),
                contentAlignment = Alignment.Center,
            ) {
                bitmapImageSelected?.let { imageBitmap ->
                    val bmWidth = imageBitmap.width.toFloat()
                    val bmHeight = imageBitmap.height.toFloat()
                    ImageCropper(
                        modifier = Modifier.aspectRatio(bmWidth / bmHeight),
                        imageBitmap = imageBitmap,
                        contentDescription = "Image Cropper",
                        cropProperties = CropDefaults.properties(
                            contentScale = ContentScale.Fit,
                            aspectRatio = aspectRatios[4].aspectRatio,
                            overlayRatio = 1f,
                            cropOutlineProperty = CropOutlineProperty(
                                OutlineType.Rect,
                                RectCropShape(0, "Rect"),
                            ),
                            handleSize = handleSize,
                        ),
                        crop = crop,
                        onCropStart = {
                            showLoadingDialog = true
                        },
                        onCropSuccess = {
                            val convertToFile = try {
                                bitmapToFileProvider(
                                    context,
                                    it.asAndroidBitmap(),
                                    mainViewModel.file,
                                )
                            } catch (e: Exception) {
                                null
                            }
                            if (convertToFile != null) {
                                onNext()
                            } else {
                                showErrorDialog = true to "Error during processing"
                            }
                            crop = false
                            showLoadingDialog = false
                        },
                        backgroundColor = Color.Transparent,
                    )
                    ControllerContainer(
                        Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth()
                            .padding(horizontal = 7.dp),
                        onRotateLeft = {
                            bitmapImageSelected?.asAndroidBitmap()?.let {
                                bitmapImageSelected =
                                    ImageOrientation.rotate(it, -90f).asImageBitmap()
                            }
                        },
                        onRotateRight = {
                            bitmapImageSelected?.asAndroidBitmap()?.let {
                                bitmapImageSelected =
                                    ImageOrientation.rotate(it, 90f).asImageBitmap()
                            }
                        },
                        onCrop = {
                            if (!crop) {
                                crop = true
                            }
                        },
                    )
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
private fun ControllerContainer(
    modifier: Modifier = Modifier,
    onRotateLeft: () -> Unit = {},
    onRotateRight: () -> Unit = {},
    onCrop: () -> Unit = {},
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        IconButton(
            onClick = {},
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_mood_24px),
                contentDescription = null,
                modifier = Modifier.size(26.dp),
            )
        }
        Row {
            IconButton(
                onClick = onRotateLeft,
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_rotate_left_24px),
                    contentDescription = null,
                    modifier = Modifier.size(26.dp),
                )
            }
            IconButton(
                onClick = onRotateRight,
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_rotate_right_24px),
                    contentDescription = null,
                    modifier = Modifier.size(26.dp),
                )
            }
        }
        FilledTonalButton(onClick = onCrop) {
            Text(text = "Finish")
        }
    }
}

@Composable
@Preview(showBackground = true)
private fun ControllerContainerPreview() {
    ControllerContainer()
}
