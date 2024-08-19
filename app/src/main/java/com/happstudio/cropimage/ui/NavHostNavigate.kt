package com.happstudio.cropimage.ui

import androidx.compose.runtime.Composable
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.happstudio.cropimage.MainViewModel

sealed class Screen(val route: String) {
    data object Home : Screen("Home")
    data object Crop : Screen("Crop/{image_string}") {
        fun pushParam(imageUri: String): String {
            return "Crop/$imageUri";
        }
    }
    data object Complete : Screen("Complete")
}

@Composable
fun MainNavigation(
    mainViewModel: MainViewModel
) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Screen.Home.route) {
        composable(Screen.Home.route) {
            HomeScreen(
                mainViewModel = mainViewModel,
                gotoCropScreen = { navController.navigate(Screen.Crop.pushParam(it)) }
            )
        }
        composable(
            Screen.Crop.route,
            arguments = listOf(
                navArgument("image_string") {
                    type = NavType.StringType
                },
            ),
        ) { backStackEntry ->
            val image = backStackEntry.arguments?.getString("image_string")
            CropScreen(
                mainViewModel = mainViewModel,
                imageUri = image ?: "",
                onNext = {
                    navController.navigate(Screen.Complete.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            inclusive = true
                        }
                    }
                }) {
                navController.popBackStack()
            }
        }
        composable(Screen.Complete.route) {
            CompleteScreen(
                mainViewModel = mainViewModel,
                gotoHome = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Complete.route) {
                            inclusive = true
                        }
                    }
                },
            )
        }
    }
}
