package com.bansi.restaurantexplorer.presentation.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.bansi.restaurantexplorer.presentation.theme.Typography

private val OrangePrimary = Color(0xFFE65100)
private val OrangeSecondary = Color(0xFFFF8A50)
private val DarkSurface = Color(0xFF1C1B1F)

private val LightColorScheme = lightColorScheme(
    primary = OrangePrimary,
    onPrimary = Color.White,
    secondary = OrangeSecondary,
    onSecondary = Color.White,
    background = Color(0xFFFFFBFE),
    surface = Color(0xFFFFFBFE),
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F),
)

private val DarkColorScheme = darkColorScheme(
    primary = OrangeSecondary,
    onPrimary = Color.Black,
    secondary = OrangePrimary,
    onSecondary = Color.White,
    background = DarkSurface,
    surface = DarkSurface,
)

@Composable
fun RestaurantExplorerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content,
    )
}
