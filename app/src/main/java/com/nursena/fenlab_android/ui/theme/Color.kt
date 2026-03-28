package com.nursena.fenlab_android.ui.theme

import androidx.compose.ui.graphics.Color

// ── Modern Frost — Arka Plan Gradyanı ────────────────────────────────────────
val LightBg         = Color(0xFFF5F7FA)   // hafif gri
val LightBg2        = Color(0xFFC3CFE2)   // soft mavi-gri
val GradientStart   = Color(0xFFF5F7FA)
val GradientMid     = Color(0xFFDDE4EF)
val GradientEnd     = Color(0xFFC3CFE2)
val DarkBg          = Color(0xFF37474F)   // metin rengiyle eşleşen koyu (compat)

// ── Frosted Glass / Cam Efekti ────────────────────────────────────────────────
val GlassSurface    = Color(0x80FFFFFF)   // %50 beyaz — temel cam
val GlassSurface2   = Color(0x99FFFFFF)   // %60 beyaz
val GlassSurface3   = Color(0xB3FFFFFF)   // %70 beyaz — modal / sheet
val GlassBg         = Color(0x66FFFFFF)   // %40 beyaz
val GlassBorder     = Color(0xE6FFFFFF)   // %90 beyaz — keskin cam kenar
val GlassBorder2    = Color(0x80FFFFFF)   // %50 beyaz kenar
val DarkSurface     = Color(0x80FFFFFF)   // compat
val DarkSurface2    = Color(0x99FFFFFF)   // compat
val DarkSurface3    = Color(0x40B0BEC5)   // divider — blue-gray %25

// ── Bottom Bar — Tema Uyumlu Frosted ─────────────────────────────────────────
val BottomBarBg     = Color(0xE0F5F7FA)   // %88 GradientStart tonu
val BottomBarBorder = Color(0xE6FFFFFF)   // %90 beyaz kenar

// ── Vurgu Renkleri ────────────────────────────────────────────────────────────
// "Fen" — gökyüzü mavisi (aksan)
val FrostAccent     = Color(0xFF64B5F6)   // Sky Blue — güven verici
val FrostAccentDark = Color(0xFF1E88E5)   // koyu ton (hover/pressed)
val FrostAccentLight= Color(0xFFBBDEFB)   // açık container

// "Lab" — turuncu (L büyük, sıcak kontrast)
val LabOrange       = Color(0xFFFF8F00)   // amber-turuncu
val LabOrangeDark   = Color(0xFFD1622D)   // koyu turuncu
val LabOrangeLight  = Color(0xFFFFECB3)   // açık container

// ── Logo ──────────────────────────────────────────────────────────────────────
val LogoFen         = FrostAccent         // "Fen" — gökyüzü mavisi
val LogoLab         = LabOrange           // "Lab" — turuncu

// ── Metin ─────────────────────────────────────────────────────────────────────
val TextPrimary     = Color(0xFF37474F)   // Blue Gray 800 — yumuşak ama net
val TextSecondary   = Color(0xFF546E7A)   // Blue Gray 600
val TextTertiary    = Color(0xFF78909C)   // Blue Gray 400

// ── Geriye dönük uyumluluk ────────────────────────────────────────────────────
val SoftTeal        = FrostAccent
val SoftTealDark    = FrostAccentDark
val SoftTealLight   = FrostAccentLight
val Teal400         = FrostAccent
val Teal500         = FrostAccentDark
val Teal700         = Color(0xFF1565C0)
val Teal100         = FrostAccentLight
val Teal50          = Color(0xFFE3F2FD)
val SkyBlue400      = FrostAccent
val SkyBlue500      = FrostAccentDark
val SkyBlue700      = Color(0xFF1565C0)
val SkyBlue100      = FrostAccentLight
val Orange400       = LabOrange
val Orange500       = LabOrangeDark
val Orange100       = LabOrangeLight
val Orange100_old   = LabOrangeLight
val Pink400         = Color(0xFFEF9A9A)   // yumuşak pembe (eski compat, artık kullanılmıyor)
val Pink500         = Color(0xFFE57373)
val Pink700         = Color(0xFFE53935)
val Pink100         = Color(0xFFFFEBEE)
val BlueBtn         = Color(0xFF78ADD1)
// ── Chip Renkleri ─────────────────────────────────────────────────────────────
val ChipScience       = Color(0xFFE1F5FE)
val ChipPhysics       = Color(0xFFE8EAF6)
val ChipChemistry     = Color(0xFFFFF8E1)
val ChipBiology       = Color(0xFFE8F5E9)
val ChipOther         = Color(0xFFF5F5F5)
val ChipScienceText   = Color(0xFF0277BD)
val ChipPhysicsText   = Color(0xFF283593)
val ChipChemistryText = Color(0xFFE65100)
val ChipBiologyText   = Color(0xFF2E7D32)
val ChipOtherText     = Color(0xFF546E7A)


// ── Status ────────────────────────────────────────────────────────────────────
val Red400    = Color(0xFFE53935)
val Red500    = Color(0xFFE53935)
val Green400  = Color(0xFF43A047)
val Green500  = Color(0xFF43A047)
val Yellow400 = Color(0xFFFFB300)

// ── Gray compat ───────────────────────────────────────────────────────────────
val Gray900   = TextPrimary
val Gray700   = TextSecondary
val Gray400   = TextTertiary
val Gray100   = GlassSurface2
val Gray50    = GlassSurface
