package com.happstudio.cropimage

import android.app.Application
import android.net.Uri
import androidx.core.content.FileProvider
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import com.happstudio.cropimage.ui.utils.createImageFile
import java.util.Objects

class MainViewModel(application: Application): AndroidViewModel(application) {

    val file = application.createImageFile()
    val uri: Uri = FileProvider.getUriForFile(
        Objects.requireNonNull(application),
        BuildConfig.APPLICATION_ID + ".provider",
        file,
    )

    companion object {
        fun Factory(app: Application): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(
                modelClass: Class<T>,
                extras: CreationExtras
            ): T {
                return MainViewModel(app) as T
            }
        }
    }
}
