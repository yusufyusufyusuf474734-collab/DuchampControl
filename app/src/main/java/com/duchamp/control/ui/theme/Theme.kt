package com.duchamp.control.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

@Composable
fun DuchampControlTheme(
    darkTheme: Boolean = true,
    accentColor: Color = Color(0xFF7C4DFF),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) {
        darkColorScheme(
            primary          = accentColor,
            onPrimary        = Color.White,
            primaryContainer = accentColor.copy(alpha = 0.3f),
            onPrimaryContainer = Color.White,
            secondary        = Color(0xFF00BCD4),
            onSecondary      = Color.Black,
            tertiary         = Color(0xFF69F0AE),
            background       = Color(0xFF0F0F0F),
            surface          = Color(0xFF1A1A1A),
            surfaceVariant   = Color(0xFF242424),
            onBackground     = Color(0xFFEEEEEE),
            onSurface        = Color(0xFFEEEEEE),
            onSurfaceVariant = Color(0xFFBDBDBD),
            error            = Color(0xFFFF5252),
            outline          = Color(0xFF424242)
        )
    } else {
        lightColorScheme(
            primary          = accentColor,
            onPrimary        = Color.White,
            primaryContainer = accentColor.copy(alpha = 0.15f),
            onPrimaryContainer = accentColor,
            secondary        = Color(0xFF0097A7),
            background       = Color(0xFFF5F5F5),
            surface          = Color.White,
            surfaceVariant   = Color(0xFFEEEEEE),
            onBackground     = Color(0xFF212121),
            onSurface        = Color(0xFF212121),
            error            = Color(0xFFD32F2F)
        )
    }

    MaterialTheme(colorScheme = colorScheme, content = content)
}
