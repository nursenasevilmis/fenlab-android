package com.nursena.fenlab_android.ui.theme

import android.os.Build
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

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
    // statusBarColor vs WindowCompat → enableEdgeToEdge() hallediyor
    // Burada sadece MaterialTheme yeterli, ekstra window işlemi crash riskini artırıyor
    MaterialTheme(
        colorScheme = FenlabColorScheme,
        typography  = Typography,
        content     = content
    )
}