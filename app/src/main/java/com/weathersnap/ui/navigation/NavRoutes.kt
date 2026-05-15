package com.weathersnap.ui.navigation

sealed class Screen(val route: String) {
    object Weather : Screen("weather")
    object CreateReport : Screen("create_report")
    object Camera : Screen("camera")
    object SavedReports : Screen("saved_reports")
}
