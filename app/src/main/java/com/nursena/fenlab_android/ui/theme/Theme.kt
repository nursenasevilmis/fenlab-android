package com.nursena.fenlab_android.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val FenlabDarkColorScheme = darkColorScheme(
    primary            = Teal400,
    onPrimary          = DarkBg,
    primaryContainer   = Teal100,
    onPrimaryContainer = Teal400,
    secondary          = Orange400,
    onSecondary        = DarkBg,
    secondaryContainer = Orange100,
    onSecondaryContainer = Orange400,
    background         = DarkBg,
    onBackground       = TextPrimary,
    surface            = DarkSurface,
    onSurface          = TextPrimary,
    surfaceVariant     = DarkSurface2,
    onSurfaceVariant   = TextSecondary,
    outline            = DarkSurface3,
    error              = Red400,
    onError            = Color.White
)

@Composable
fun FenlabAndroidTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = FenlabDarkColorScheme,
        typography  = Typography,
        content     = content
    )
}