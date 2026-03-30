package com.duchamp.control.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// Kurumsal renk paleti
object EnterpriseColors {
    val Navy900   = Color(0xFF0A0E1A)
    val Navy800   = Color(0xFF0D1321)
    val Navy700   = Color(0xFF111827)
    val Navy600   = Color(0xFF1C2333)
    val Navy500   = Color(0xFF243044)
    val Navy400   = Color(0xFF2E3D55)
    val Slate300  = Color(0xFF8892A4)
    val Slate200  = Color(0xFFB0BAC8)
    val Slate100  = Color(0xFFD4DAE4)
    val White     = Color(0xFFF0F4FA)
    val Divider   = Color(0xFF1E2A3A)
}

@Composable
fun DuchampControlTheme(
    darkTheme: Boolean = true,
    accentColor: Color = Color(0xFF3B82F6),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) {
        darkColorScheme(
            primary             = accentColor,
            onPrimary           = Color.White,
            primaryContainer    = accentColor.copy(alpha = 0.15f),
            onPrimaryContainer  = accentColor,
            secondary           = Color(0xFF06B6D4),
            onSecondary         = Color.White,
            secondaryContainer  = Color(0xFF06B6D4).copy(alpha = 0.12f),
            onSecondaryContainer= Color(0xFF06B6D4),
            tertiary            = Color(0xFF10B981),
            onTertiary          = Color.White,
            tertiaryContainer   = Color(0xFF10B981).copy(alpha = 0.12f),
            onTertiaryContainer = Color(0xFF10B981),
            background          = EnterpriseColors.Navy900,
            surface             = EnterpriseColors.Navy800,
            surfaceVariant      = EnterpriseColors.Navy600,
            surfaceTint         = accentColor,
            onBackground        = EnterpriseColors.White,
            onSurface           = EnterpriseColors.White,
            onSurfaceVariant    = EnterpriseColors.Slate200,
            error               = Color(0xFFEF4444),
            onError             = Color.White,
            errorContainer      = Color(0xFFEF4444).copy(alpha = 0.12f),
            onErrorContainer    = Color(0xFFEF4444),
            outline             = EnterpriseColors.Navy400,
            outlineVariant      = EnterpriseColors.Navy500
        )
    } else {
        lightColorScheme(
            primary             = accentColor,
            onPrimary           = Color.White,
            primaryContainer    = accentColor.copy(alpha = 0.1f),
            onPrimaryContainer  = accentColor,
            secondary           = Color(0xFF0891B2),
            background          = Color(0xFFF1F5F9),
            surface             = Color.White,
            surfaceVariant      = Color(0xFFE8EDF5),
            onBackground        = Color(0xFF0F172A),
            onSurface           = Color(0xFF0F172A),
            onSurfaceVariant    = Color(0xFF475569),
            error               = Color(0xFFDC2626),
            outline             = Color(0xFFCBD5E1)
        )
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = MaterialTheme.typography.copy(
            titleLarge  = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 20.sp, letterSpacing = (-0.3).sp),
            titleMedium = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 16.sp, letterSpacing = (-0.2).sp),
            titleSmall  = TextStyle(fontWeight = FontWeight.Medium,   fontSize = 14.sp, letterSpacing = 0.sp),
            bodyMedium  = TextStyle(fontWeight = FontWeight.Normal,   fontSize = 14.sp, letterSpacing = 0.1.sp),
            bodySmall   = TextStyle(fontWeight = FontWeight.Normal,   fontSize = 12.sp, letterSpacing = 0.1.sp),
            labelLarge  = TextStyle(fontWeight = FontWeight.Medium,   fontSize = 13.sp, letterSpacing = 0.3.sp),
            labelMedium = TextStyle(fontWeight = FontWeight.Medium,   fontSize = 11.sp, letterSpacing = 0.5.sp),
            labelSmall  = TextStyle(fontWeight = FontWeight.Normal,   fontSize = 10.sp, letterSpacing = 0.5.sp)
        ),
        content = content
    )
}
