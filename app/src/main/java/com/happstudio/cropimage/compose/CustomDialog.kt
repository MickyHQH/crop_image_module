package com.happstudio.cropimage.compose

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

@Composable
fun LoadingDialog(
    onDismiss: () -> Unit = {},
) {
    CustomDialog(
        header = {
            CircularProgressIndicator(
                modifier = Modifier.width(42.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                trackColor = MaterialTheme.colorScheme.secondary,
            )
        },
        onDismiss = onDismiss,
    )
}

@Composable
fun ErrorDialog(
    message: String? = null,
    title: String = "Error",
    onDismiss: () -> Unit = {},
) {
    CustomDialog(
        header = {
            Column(
                modifier = Modifier.padding(7.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = title,
                    textAlign = TextAlign.Center,
                )
                message?.let {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = it,
                    )
                }
            }
        },
        footer = {
            ElevatedButton(onClick = { onDismiss() }) {
                Text(text = "OK")
            }
        },
        onDismiss = onDismiss,
    )
}

@Composable
private fun CustomDialog(
    onDismiss: () -> Unit,
    header: (@Composable () -> Unit)? = null,
    footer: (@Composable () -> Unit)? = null,
) {
    Dialog(
        onDismissRequest = {
            onDismiss()
        },
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = false,
            dismissOnClickOutside = false,
        ),
    ) {
        Box(
            modifier = Modifier
                .background(Color.White.copy(0.8f)),
        ) {
            Column(
                modifier = Modifier
                    .align(Alignment.Center)
                    .wrapContentHeight()
                    .border(BorderStroke(1.dp, Color.DarkGray))
                    .padding(40.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                if (header != null) {
                    header()
                }
                if (footer != null) {
                    Spacer(modifier = Modifier.height(25.dp))
                    footer()
                }
            }
        }
    }
}