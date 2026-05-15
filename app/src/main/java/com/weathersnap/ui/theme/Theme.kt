package com.weathersnap.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat

// Light palette
val Blue700    = Color(0xFF0061A4)
val BlueOnPri  = Color(0xFFFFFFFF)
val BluePC     = Color(0xFFD1E4FF)
val BlueOnPC   = Color(0xFF001D36)
val SecLight   = Color(0xFF535F70)
val BgLight    = Color(0xFFF4F6FB)
val SurfLight  = Color(0xFFFFFFFF)

// Dark palette
val Blue200    = Color(0xFF9ECAFF)
val BlueOnPriD = Color(0xFF003258)
val BluePCD    = Color(0xFF00497D)
val BlueOnPCD  = Color(0xFFD1E4FF)
val SecDark    = Color(0xFFBBC7DB)
val BgDark     = Color(0xFF111318)
val SurfDark   = Color(0xFF1A1C22)

private val LightColors = lightColorScheme(
    primary              = Blue700,
    onPrimary            = BlueOnPri,
    primaryContainer     = BluePC,
    onPrimaryContainer   = BlueOnPC,
    secondary            = SecLight,
    onSecondary          = Color.White,
    background           = BgLight,
    onBackground         = Color(0xFF1A1C1E),
    surface              = SurfLight,
    onSurface            = Color(0xFF1A1C1E),
    surfaceVariant       = Color(0xFFE8EDF5),
    onSurfaceVariant     = Color(0xFF44474F),
    outline              = Color(0xFF74777F),
)

private val DarkColors = darkColorScheme(
    primary              = Blue200,
    onPrimary            = BlueOnPriD,
    primaryContainer     = BluePCD,
    onPrimaryContainer   = BlueOnPCD,
    secondary            = SecDark,
    onSecondary          = Color(0xFF253140),
    background           = BgDark,
    onBackground         = Color(0xFFE2E2E6),
    surface              = SurfDark,
    onSurface            = Color(0xFFE2E2E6),
    surfaceVariant       = Color(0xFF1E2028),
    onSurfaceVariant     = Color(0xFFC4C6CF),
    outline              = Color(0xFF8E9099),
)

val AppTypography = Typography(
    displayLarge  = TextStyle(fontWeight = FontWeight.Light,   fontSize = 57.sp, lineHeight = 64.sp),
    displayMedium = TextStyle(fontWeight = FontWeight.Light,   fontSize = 45.sp, lineHeight = 52.sp),
    displaySmall  = TextStyle(fontWeight = FontWeight.Normal,  fontSize = 36.sp, lineHeight = 44.sp),
    headlineLarge = TextStyle(fontWeight = FontWeight.SemiBold,fontSize = 32.sp, lineHeight = 40.sp),
    headlineMedium= TextStyle(fontWeight = FontWeight.SemiBold,fontSize = 28.sp, lineHeight = 36.sp),
    headlineSmall = TextStyle(fontWeight = FontWeight.SemiBold,fontSize = 24.sp, lineHeight = 32.sp),
    titleLarge    = TextStyle(fontWeight = FontWeight.SemiBold,fontSize = 22.sp, lineHeight = 28.sp),
    titleMedium   = TextStyle(fontWeight = FontWeight.Medium,  fontSize = 16.sp, lineHeight = 24.sp),
    titleSmall    = TextStyle(fontWeight = FontWeight.Medium,  fontSize = 14.sp, lineHeight = 20.sp),
    bodyLarge     = TextStyle(fontWeight = FontWeight.Normal,  fontSize = 16.sp, lineHeight = 24.sp),
    bodyMedium    = TextStyle(fontWeight = FontWeight.Normal,  fontSize = 14.sp, lineHeight = 20.sp),
    bodySmall     = TextStyle(fontWeight = FontWeight.Normal,  fontSize = 12.sp, lineHeight = 16.sp),
    labelLarge    = TextStyle(fontWeight = FontWeight.Medium,  fontSize = 14.sp, lineHeight = 20.sp),
    labelMedium   = TextStyle(fontWeight = FontWeight.Medium,  fontSize = 12.sp, lineHeight = 16.sp),
    labelSmall    = TextStyle(fontWeight = FontWeight.Medium,  fontSize = 11.sp, lineHeight = 16.sp),
)

@Composable
fun WeatherSnapTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColors else LightColors
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }
    MaterialTheme(
        colorScheme = colorScheme,
        typography  = AppTypography,
        content     = content
    )
}
