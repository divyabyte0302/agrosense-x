package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val LightColorScheme = lightColorScheme(
    primary = PrimaryForestGreen,
    secondary = SecondaryLeafGreen,
    tertiary = AccentFreshGreen,
    background = MainBackground,
    surface = CardBackground,
    onBackground = PrimaryText,
    onSurface = PrimaryText,
    onPrimary = Color.White,
    onSecondary = Color.White
)

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryForestGreen,
    secondary = SecondaryLeafGreen,
    tertiary = AccentFreshGreen,
    background = MainBackground,
    surface = CardBackground,
    onBackground = PrimaryText,
    onSurface = PrimaryText,
    onPrimary = Color.White,
    onSecondary = Color.White
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
