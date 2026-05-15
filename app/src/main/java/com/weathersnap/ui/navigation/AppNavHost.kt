package com.weathersnap.ui.navigation

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.weathersnap.ui.camera.CustomCameraScreen
import com.weathersnap.ui.create.CreateReportScreen
import com.weathersnap.ui.saved.SavedReportsScreen
import com.weathersnap.ui.weather.WeatherScreen

private const val ANIM_MS = 350

@Composable
fun AppNavHost() {
    val navController = rememberNavController()

    NavHost(
        navController    = navController,
        startDestination = Screen.Weather.route,
        enterTransition  = {
            slideInHorizontally(initialOffsetX = { it }, animationSpec = tween(ANIM_MS)) +
                    fadeIn(animationSpec = tween(ANIM_MS))
        },
        exitTransition   = {
            slideOutHorizontally(targetOffsetX = { -it / 3 }, animationSpec = tween(ANIM_MS)) +
                    fadeOut(animationSpec = tween(ANIM_MS))
        },
        popEnterTransition = {
            slideInHorizontally(initialOffsetX = { -it / 3 }, animationSpec = tween(ANIM_MS)) +
                    fadeIn(animationSpec = tween(ANIM_MS))
        },
        popExitTransition  = {
            slideOutHorizontally(targetOffsetX = { it }, animationSpec = tween(ANIM_MS)) +
                    fadeOut(animationSpec = tween(ANIM_MS))
        }
    ) {

        // Weather Screen
        composable(Screen.Weather.route) {
            WeatherScreen(
                onNavigateToCreateReport = { snapshotJson ->
                    navController.navigate("${Screen.CreateReport.route}/$snapshotJson")
                },
                onNavigateToSavedReports = { navController.navigate(Screen.SavedReports.route) }
            )
        }

        // Create Report Screen
        composable(
            route     = "${Screen.CreateReport.route}/{snapshotJson}",
            arguments = listOf(navArgument("snapshotJson") { type = NavType.StringType })
        ) { backStackEntry ->
            val snapshotJson     = backStackEntry.arguments?.getString("snapshotJson") ?: ""
            val capturedImageUri = backStackEntry.savedStateHandle.get<String>("captured_image_uri")

            CreateReportScreen(
                snapshotJson           = snapshotJson,
                capturedImageUri       = capturedImageUri,
                onNavigateToCamera     = { navController.navigate(Screen.Camera.route) },
                onNavigateBack         = { navController.popBackStack() },
                onNavigateToSavedReports = {
                    navController.navigate(Screen.SavedReports.route) {
                        popUpTo(Screen.Weather.route)
                    }
                }
            )
        }

        // Custom Camera Screen
        composable(Screen.Camera.route) {
            CustomCameraScreen(
                onPhotoCaptured = { uri ->
                    navController.previousBackStackEntry
                        ?.savedStateHandle
                        ?.set("captured_image_uri", uri.toString())
                    navController.popBackStack()
                },
                onClose = { navController.popBackStack() }
            )
        }

        // Saved Reports Screen
        composable(Screen.SavedReports.route) {
            SavedReportsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
