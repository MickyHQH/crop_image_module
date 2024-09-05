package com.happstudio.cropimage

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.happstudio.cropimage.ui.navigate.MainNavigation
import com.happstudio.cropimage.ui.theme.CropImageTheme

class MainActivity : ComponentActivity() {
    private val mainViewModel: MainViewModel by viewModels { MainViewModel.Factory(application) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CropImageTheme {
                MainNavigation(mainViewModel)
            }
        }
    }
}


