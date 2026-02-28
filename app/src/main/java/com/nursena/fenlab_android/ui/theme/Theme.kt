package com.nursena.fenlab_android.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val FenlabColorScheme = lightColorScheme(
    primary            = Teal500,
    onPrimary          = Color.White,
    primaryContainer   = Teal100,
    onPrimaryContainer = Teal700,
    secondary          = Orange500,
    onSecondary        = Color.White,
    secondaryContainer = Orange100,
    background         = Color.White,
    onBackground       = Gray900,
    surface            = Color.White,
    onSurface          = Gray900,
    surfaceVariant     = Gray50,
    onSurfaceVariant   = Gray700,
    error              = Red500,
    onError            = Color.White
)

@Composable
fun FenlabAndroidTheme(content: @Composable () -> Unit) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = Color.White.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = true
        }
    }
    MaterialTheme(
        colorScheme = FenlabColorScheme,
        typography  = Typography,
        content     = content
    )
}