package com.duchamp.control.ui.theme

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Modern Color Palette (Sefer Defteri inspired)
private val md_theme_light_primary = Color(0xFF1976D2)
private val md_theme_light_onPrimary = Color(0xFFFFFFFF)
private val md_theme_light_primaryContainer = Color(0xFFBBDEFB)
private val md_theme_light_secondary = Color(0xFF0288D1)
private val md_theme_light_surface = Color(0xFFFAFAFA)
private val md_theme_light_background = Color(0xFFFFFFFF)

private val md_theme_dark_primary = Color(0xFF64B5F6)
private val md_theme_dark_onPrimary = Color(0xFF003258)
private val md_theme_dark_primaryContainer = Color(0xFF004A77)
private val md_theme_dark_secondary = Color(0xFF4FC3F7)
private val md_theme_dark_surface = Color(0xFF1E1E1E)
private val md_theme_dark_background = Color(0xFF121212)

private val LightColors = lightColorScheme(
    primary = md_theme_light_primary,
    onPrimary = md_theme_light_onPrimary,
    primaryContainer = md_theme_light_primaryContainer,
    secondary = md_theme_light_secondary,
    surface = md_theme_light_surface,
    background = md_theme_light_background
)

private val DarkColors = darkColorScheme(
    primary = md_theme_dark_primary,
    onPrimary = md_theme_dark_onPrimary,
    primaryContainer = md_theme_dark_primaryContainer,
    secondary = md_theme_dark_secondary,
    surface = md_theme_dark_surface,
    background = md_theme_dark_background
)

@Composable
fun ModernDuchampTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColors else LightColors
    
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}

// Animasyon sabitleri
object AnimationConstants {
    const val FAST_DURATION = 150
    const val NORMAL_DURATION = 300
    const val SLOW_DURATION = 500
    
    val FastEasing = FastOutSlowInEasing
    val NormalEasing = EaseInOut
    val SlowEasing = LinearOutSlowInEasing
}

// Fade animasyonu
@Composable
fun FadeInAnimation(
    visible: Boolean,
    content: @Composable () -> Unit
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(
            animationSpec = tween(
                durationMillis = AnimationConstants.NORMAL_DURATION,
                easing = AnimationConstants.NormalEasing
            )
        ),
        exit = fadeOut(
            animationSpec = tween(
                durationMillis = AnimationConstants.FAST_DURATION,
                easing = AnimationConstants.FastEasing
            )
        )
    ) {
        content()
    }
}

// Slide animasyonu
@Composable
fun SlideInAnimation(
    visible: Boolean,
    content: @Composable () -> Unit
) {
    AnimatedVisibility(
        visible = visible,
        enter = slideInVertically(
            initialOffsetY = { it },
            animationSpec = tween(
                durationMillis = AnimationConstants.NORMAL_DURATION,
                easing = AnimationConstants.NormalEasing
            )
        ) + fadeIn(),
        exit = slideOutVertically(
            targetOffsetY = { it },
            animationSpec = tween(
                durationMillis = AnimationConstants.FAST_DURATION,
                easing = AnimationConstants.FastEasing
            )
        ) + fadeOut()
    ) {
        content()
    }
}
