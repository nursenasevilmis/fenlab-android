package com.nursena.fenlab_android.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val FenlabColorScheme = lightColorScheme(
    primary              = Color(0xFF0D7D7C),
    onPrimary            = Color(0xFFFFFFFF),
    primaryContainer     = Color(0xFFE0F4F4),
    onPrimaryContainer   = Color(0xFF064040),
    secondary            = Color(0xFFF6A923),
    onSecondary          = Color(0xFFFFFFFF),
    secondaryContainer   = Color(0xFFFFF3DB),
    onSecondaryContainer = Color(0xFF7A4500),
    tertiary             = Color(0xFF1A9B9A),
    onTertiary           = Color(0xFFFFFFFF),
    background           = Color(0xFFFFFFFF),
    onBackground         = Color(0xFF111111),
    surface              = Color(0xFFFFFFFF),
    onSurface            = Color(0xFF111111),
    surfaceVariant       = Color(0xFFF5F5F5),
    onSurfaceVariant     = Color(0xFF555555),
    surfaceTint          = Color(0xFF0D7D7C),
    outline              = Color(0xFFDDDDDD),
    outlineVariant       = Color(0xFFEEEEEE),
    error                = Color(0xFFE53935),
    onError              = Color(0xFFFFFFFF),
    errorContainer       = Color(0xFFFFDAD6),
    onErrorContainer     = Color(0xFF410002),
    inverseSurface       = Color(0xFF111111),
    inverseOnSurface     = Color(0xFFFFFFFF),
    inversePrimary       = Color(0xFF80CFCE),
    scrim                = Color(0x52000000),
)

@Composable
fun FenlabAndroidTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = FenlabColorScheme,
        typography  = Typography,
        content     = content
    )
}
