package com.nursena.fenlab_android.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val FenlabFrostColorScheme = lightColorScheme(
    primary              = FrostAccent,         // #64B5F6 — gökyüzü mavisi
    onPrimary            = Color.White,
    primaryContainer     = FrostAccentLight,    // #BBDEFB
    onPrimaryContainer   = Color(0xFF0D47A1),
    secondary            = LabOrange,           // #FF8F00 — turuncu
    onSecondary          = Color.White,
    secondaryContainer   = LabOrangeLight,      // #FFECB3
    onSecondaryContainer = Color(0xFF4E2600),
    tertiary             = Color(0xFF66BB6A),   // soft yeşil
    onTertiary           = Color.White,
    background           = LightBg,             // #F5F7FA
    onBackground         = TextPrimary,         // #37474F
    surface              = GlassSurface3,       // %70 beyaz cam
    onSurface            = TextPrimary,
    surfaceVariant       = GlassSurface2,       // %60 beyaz
    onSurfaceVariant     = TextSecondary,
    surfaceTint          = FrostAccent,
    outline              = Color(0xFFB0BEC5),   // Blue Gray 200
    outlineVariant       = GlassBorder,
    error                = Red400,
    onError              = Color.White,
    errorContainer       = Color(0xFFFFDAD6),
    onErrorContainer     = Color(0xFF410002),
    inverseSurface       = TextPrimary,
    inverseOnSurface     = LightBg,
    inversePrimary       = FrostAccentLight,
    scrim                = Color(0x52000000),
)

@Composable
fun FenlabAndroidTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = FenlabFrostColorScheme,
        typography  = Typography,
        content     = content
    )
}
