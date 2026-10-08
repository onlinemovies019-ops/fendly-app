package com.example.fendly

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat

class SafeTradeCheckActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        val darkMode = getSharedPreferences("fendly_settings", MODE_PRIVATE)
            .getBoolean("dark_mode", false)
        setTheme(if (darkMode) R.style.Theme_Fendly_Dark else R.style.Theme_Fendly)
        super.onCreate(savedInstanceState)

        window.statusBarColor = if (darkMode) android.graphics.Color.rgb(18, 19, 25)
            else android.graphics.Color.rgb(247, 243, 238)
        window.navigationBarColor = window.statusBarColor
        WindowCompat.getInsetsController(window, window.decorView).apply {
            isAppearanceLightStatusBars = !darkMode
            isAppearanceLightNavigationBars = !darkMode
        }

        val colors = if (darkMode) {
            darkColorScheme(
                primary = Color(0xFFE8B24A),
                onPrimary = Color(0xFF2B1D05),
                background = Color(0xFF121319),
                onBackground = Color(0xFFF7F9FC),
                surface = Color(0xFF181D24),
                onSurface = Color(0xFFF7F9FC),
                onSurfaceVariant = Color(0xFFAAB1BC),
                outline = Color(0xFF444D5A),
            )
        } else {
            lightColorScheme(
                primary = Color(0xFFE8B24A),
                onPrimary = Color(0xFF2B1D05),
                background = Color(0xFFF7F3EE),
                onBackground = Color.Black,
                surface = Color(0xFFFDFBF8),
                onSurface = Color.Black,
                onSurfaceVariant = Color(0xFF4D505A),
                outline = Color(0xFFCDC5B8),
            )
        }
        setContent {
            MaterialTheme(colorScheme = colors) {
                SafeTradeCheckScreen(
                    onBack = { finish() },
                    darkMode = darkMode,
                )
            }
        }
    }
}
