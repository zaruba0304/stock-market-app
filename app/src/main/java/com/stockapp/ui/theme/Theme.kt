package com.stockapp.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF4CAF50),
    primaryContainer = Color(0xFF2E7D32),
    secondary = Color(0xFF81C784),
    secondaryContainer = Color(0xFF388E3C),
    tertiary = Color(0xFFFF9800),
    tertiaryContainer = Color(0xFFF57C00),
    error = Color(0xFFEF5350),
    errorContainer = Color(0xFFC62828),
    background = Color(0xFF121212),
    surface = Color(0xFF1E1E1E),
    surfaceVariant = Color(0xFF2C2C2C),
    onPrimary = Color.White,
    onPrimaryContainer = Color.White,
    onSecondary = Color.Black,
    onSecondaryContainer = Color.White,
    onTertiary = Color.Black,
    onTertiaryContainer = Color.White,
    onError = Color.White,
    onErrorContainer = Color.White,
    onBackground = Color.White,
    onSurface = Color.White,
    onSurfaceVariant = Color(0xFFB0B0B0),
    outline = Color(0xFF444444),
    outlineVariant = Color(0xFF333333),
    shadow = Color.Black,
    scrim = Color.Black,
    inverseSurface = Color(0xFFE0E0E0),
    inverseOnSurface = Color(0xFF121212),
    inversePrimary = Color(0xFF2E7D32)
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF2E7D32),
    primaryContainer = Color(0xFFC8E6C9),
    secondary = Color(0xFF388E3C),
    secondaryContainer = Color(0xFF81C784),
    tertiary = Color(0xFFF57C00),
    tertiaryContainer = Color(0xFFFFE0B2),
    error = Color(0xFFC62828),
    errorContainer = Color(0xFFEF5350),
    background = Color(0xFFFAFAFA),
    surface = Color.White,
    surfaceVariant = Color(0xFFF5F5F5),
    onPrimary = Color.White,
    onPrimaryContainer = Color(0xFF1B5E20),
    onSecondary = Color.White,
    onSecondaryContainer = Color(0xFF1B5E20),
    onTertiary = Color.White,
    onTertiaryContainer = Color(0xFFE65100),
    onError = Color.White,
    onErrorContainer = Color(0xFFB71C1C),
    onBackground = Color(0xFF121212),
    onSurface = Color(0xFF121212),
    onSurfaceVariant = Color(0xFF444444),
    outline = Color(0xFFBDBDBD),
    outlineVariant = Color(0xFFE0E0E0),
    shadow = Color.Black,
    scrim = Color.Black,
    inverseSurface = Color(0xFF121212),
    inverseOnSurface = Color.White,
    inversePrimary = Color(0xFF81C784)
)

@Composable
fun StockAppTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

object Typography {
    val displayLarge = androidx.compose.material3.Typography.Default.displayLarge
    val displayMedium = androidx.compose.material3.Typography.Default.displayMedium
    val displaySmall = androidx.compose.material3.Typography.Default.displaySmall
    val headlineLarge = androidx.compose.material3.Typography.Default.headlineLarge
    val headlineMedium = androidx.compose.material3.Typography.Default.headlineMedium
    val headlineSmall = androidx.compose.material3.Typography.Default.headlineSmall
    val titleLarge = androidx.compose.material3.Typography.Default.titleLarge
    val titleMedium = androidx.compose.material3.Typography.Default.titleMedium
    val titleSmall = androidx.compose.material3.Typography.Default.titleSmall
    val bodyLarge = androidx.compose.material3.Typography.Default.bodyLarge
    val bodyMedium = androidx.compose.material3.Typography.Default.bodyMedium
    val bodySmall = androidx.compose.material3.Typography.Default.bodySmall
    val labelLarge = androidx.compose.material3.Typography.Default.labelLarge
    val labelMedium = androidx.compose.material3.Typography.Default.labelMedium
    val labelSmall = androidx.compose.material3.Typography.Default.labelSmall
}