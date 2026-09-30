package com.example.fendly.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

private val DarkColorScheme = darkColorScheme(
    primary = Gold,
    primaryContainer = GoldOn,
    secondary = LostGreen,
    secondaryContainer = LostGreenOn,
    background = DarkBlue,
    surface = DarkSurface,
    surfaceVariant = Border,
    onPrimary = GoldOn,
    onSecondary = LostGreenOn,
    onBackground = Color.White,
    onSurface = Color.White,
    onSurfaceVariant = Color.White,
    error = Color.Red,
    onErrorContainer = Color.White,
    inversePrimary = Gold,
    inverseSurface = DarkBlue,
    inverseOnSurface = DarkSurface,
    surfaceTint = Gold,
    outline = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = Gold,
    primaryContainer = GoldOn,
    secondary = LostGreen,
    secondaryContainer = LostGreenOn,
    background = LightBackground,
    surface = LightSurface,
    surfaceVariant = LightBorder,
    onPrimary = GoldOn,
    onSecondary = LostGreenOn,
    onBackground = Color.Black,
    onSurface = Color.Black,
    onSurfaceVariant = Color.Black,
    error = Color.Red,
    onErrorContainer = Color.Black,
    inversePrimary = Gold,
    inverseSurface = LightBackground,
    inverseOnSurface = LightSurface,
    surfaceTint = Gold,
    outline = Color.Black
)

@Composable
fun FendlyTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            dynamicColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        content = content
    )
}

private val AppTypography = Typography(
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp
    ),
    headlineLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 24.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 20.sp
    ),
    headlineSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 18.sp
    ),
    titleLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp
    ),
    titleMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp
    ),
    titleSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp
    ),
    labelLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp
    ),
    labelMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp
    ),
    labelSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 10.sp
    )
)
