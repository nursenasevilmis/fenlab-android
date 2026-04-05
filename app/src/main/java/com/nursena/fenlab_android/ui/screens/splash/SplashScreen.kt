package com.nursena.fenlab_android.ui.screens.splash

import android.app.Activity
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// ─────────────────────────────────────────────────────────────────────────────
// Renkler — logodaki yeşil ve turuncu
// ─────────────────────────────────────────────────────────────────────────────
private val BgColor     = Color(0xFFFFFFFF)
private val GreenColor  = Color(0xFF3A7D44)
private val OrangeColor = Color(0xFFD4860B)

// ─────────────────────────────────────────────────────────────────────────────
// Font — res/font/montserrat_bold.ttf olmalı
// ─────────────────────────────────────────────────────────────────────────────
private val MontserratFamily = try {
    FontFamily(
        Font(
            resId  = com.nursena.fenlab_android.R.font.montserrat_bold,
            weight = FontWeight.Bold
        )
    )
} catch (e: Exception) {
    FontFamily.Default
}

// ─────────────────────────────────────────────────────────────────────────────
// Sistem çubuklarını beyaz yap
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun SplashSystemUi() {
    val view = LocalView.current
    SideEffect {
        val window = (view.context as Activity).window
        window.statusBarColor     = android.graphics.Color.WHITE
        window.navigationBarColor = android.graphics.Color.WHITE
        androidx.core.view.WindowCompat
            .getInsetsController(window, view)
            .isAppearanceLightStatusBars = true
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Splash Screen
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun SplashScreen(onAnimationEnd: () -> Unit) {

    val density      = LocalDensity.current
    val textMeasurer = rememberTextMeasurer()

    // Kaç harf görünüyor (0..6, "FenLab")
    var visibleCount by remember { mutableIntStateOf(0) }

    // İmleç yanıp sönme
    val cursorAlpha = remember { Animatable(1f) }

    // Ayrılma
    val splitProgress = remember { Animatable(0f) }
    var startSplit    by remember { mutableStateOf(false) }

    // Global solar
    val globalAlpha = remember { Animatable(1f) }

    LaunchedEffect(Unit) {
        // İmleç sürekli yanıp sönsün (coroutine iptal olunca durur)
        val cursorJob = launch {
            while (true) {
                cursorAlpha.animateTo(0f, tween(450))
                cursorAlpha.animateTo(1f, tween(450))
            }
        }

        delay(500)

        // Daktilo: her harf 130ms
        for (i in 1..6) {
            visibleCount = i
            delay(150)
        }

        // İmleç 3 kez tam döngü yapsın (~900ms bekleme)
        delay(900)

        // Split
        cursorJob.cancel()
        startSplit = true
        splitProgress.animateTo(
            targetValue    = 1f,
            animationSpec  = tween(900, easing = CubicBezierEasing(0.4f, 0f, 0.8f, 1f))
        )

        // Solar
        globalAlpha.animateTo(0f, tween(250))

        delay(70)
        onAnimationEnd()
    }

    SplashSystemUi()

    // ── Stil ─────────────────────────────────────────────────────────────────
    val textStyle = TextStyle(
        fontFamily = MontserratFamily,
        fontWeight = FontWeight.Bold,
        fontSize   = 64.sp,
        textAlign  = TextAlign.Left
    )
    val fenStyle    = textStyle.copy(color = GreenColor)
    val labStyle    = textStyle.copy(color = OrangeColor)
    val cursorStyle = textStyle.copy(color = GreenColor)

    val fenFull = "Fen"
    val labFull = "Lab"

    // Canvas boyutu için ön ölçüm
    val canvasWidthDp = with(density) {
        val fL = textMeasurer.measure(buildAnnotatedString { append(fenFull) }, fenStyle)
        val lL = textMeasurer.measure(buildAnnotatedString { append(labFull) }, labStyle)
        val cL = textMeasurer.measure(buildAnnotatedString { append("|") }, cursorStyle)
        (fL.size.width + lL.size.width + cL.size.width + 20).toDp()
    }
    val canvasHeightDp = with(density) {
        val fL = textMeasurer.measure(buildAnnotatedString { append(fenFull) }, fenStyle)
        fL.size.height.toDp()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BgColor),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(canvasWidthDp, canvasHeightDp)
                .drawWithCache {

                    val fenLayout    = textMeasurer.measure(buildAnnotatedString { append(fenFull) }, fenStyle)
                    val labLayout    = textMeasurer.measure(buildAnnotatedString { append(labFull) }, labStyle)
                    val cursorLayout = textMeasurer.measure(buildAnnotatedString { append("|") }, cursorStyle)

                    val fenW  = fenLayout.size.width.toFloat()
                    val labW  = labLayout.size.width.toFloat()
                    val lineH = fenLayout.size.height.toFloat()

                    // FenLab tam genişlik, ortalanmış
                    val totalW = fenW + labW
                    val fenX0  = (size.width - totalW) / 2f
                    val labX0  = fenX0 + fenW
                    val textY  = (size.height - lineH) / 2f

                    onDrawBehind {
                        val alpha = globalAlpha.value
                        val sp    = splitProgress.value
                        val vc    = visibleCount

                        val fenVisible = vc.coerceIn(0, 3)
                        val labVisible = (vc - 3).coerceIn(0, 3)

                        // Ayrılma: Fen sola, Lab sağa
                        val splitOffset = sp * (totalW / 2f + 80.dp.toPx())

                        // ── Fen ──────────────────────────────────────────────
                        if (fenVisible > 0) {
                            val partial = textMeasurer.measure(
                                buildAnnotatedString { append(fenFull.take(fenVisible)) },
                                fenStyle
                            )
                            drawText(
                                textLayoutResult = partial,
                                color            = GreenColor.copy(alpha = alpha),
                                topLeft          = Offset(fenX0 - splitOffset, textY)
                            )
                        }

                        // ── Lab ──────────────────────────────────────────────
                        if (labVisible > 0) {
                            val partial = textMeasurer.measure(
                                buildAnnotatedString { append(labFull.take(labVisible)) },
                                labStyle
                            )
                            drawText(
                                textLayoutResult = partial,
                                color            = OrangeColor.copy(alpha = alpha),
                                topLeft          = Offset(labX0 + splitOffset, textY)
                            )
                        }

                        // ── İmleç — split başlayana kadar ────────────────────
                        if (!startSplit) {
                            // İmleci son yazılan harfin hemen sağına koy
                            val cursorX = when {
                                vc == 0 -> fenX0
                                vc <= 3 -> {
                                    val w = textMeasurer.measure(
                                        buildAnnotatedString { append(fenFull.take(fenVisible)) },
                                        fenStyle
                                    ).size.width.toFloat()
                                    fenX0 + w
                                }
                                else -> {
                                    val w = textMeasurer.measure(
                                        buildAnnotatedString { append(labFull.take(labVisible)) },
                                        labStyle
                                    ).size.width.toFloat()
                                    labX0 + w
                                }
                            }
                            drawText(
                                textLayoutResult = cursorLayout,
                                color            = GreenColor.copy(alpha = cursorAlpha.value * alpha),
                                topLeft          = Offset(cursorX, textY)
                            )
                        }
                    }
                }
        )
    }
}