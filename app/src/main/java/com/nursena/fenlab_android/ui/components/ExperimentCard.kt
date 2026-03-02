package com.nursena.fenlab_android.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.nursena.fenlab_android.domain.model.Experiment

@Composable
fun ExperimentCard(
    experiment: Experiment,
    onCardClick: () -> Unit,
    onFavoriteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier  = modifier
            .fillMaxWidth()
            .clickable(onClick = onCardClick),
        shape     = RoundedCornerShape(12.dp),
        colors    = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {

            // ── Medya alanı ────────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)  // 200 → 130
            ) {
                AsyncImage(
                    model              = experiment.thumbnailUrl ?: experiment.videoUrl,
                    contentDescription = experiment.title,
                    contentScale       = ContentScale.Crop,
                    modifier           = Modifier.fillMaxSize()
                )

                // Alt gradient overlay
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                0.0f to Color.Transparent,
                                0.4f to Color.Transparent,
                                1.0f to Color.Black.copy(alpha = 0.65f)
                            )
                        )
                )

                // Konu chip — sol üst
                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(8.dp)
                        .background(Color(0xFF00897B), RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = experiment.displaySubject,
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                // Favori butonu — sağ üst
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(6.dp)
                        .size(32.dp)
                        .background(Color.White, CircleShape)
                        .clickable { onFavoriteClick() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (experiment.isFavoritedByCurrentUser)
                            Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Favori",
                        tint = if (experiment.isFavoritedByCurrentUser)
                            Color(0xFFE53935) else Color(0xFF9E9E9E),
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Play ikonu — sadece video varsa
                if (experiment.videoUrl != null) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .size(38.dp)
                            .background(Color.White.copy(alpha = 0.85f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector        = Icons.Default.PlayArrow,
                            contentDescription = "Videoyu Oynat",
                            tint               = Color(0xFF00897B),
                            modifier           = Modifier.size(22.dp)
                        )
                    }
                }

                // Başlık — sol alt, overlay üzerinde
                Text(
                    text       = experiment.title,
                    color      = Color.White,
                    fontSize   = 13.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines   = 2,
                    overflow   = TextOverflow.Ellipsis,
                    modifier   = Modifier
                        .align(Alignment.BottomStart)
                        .padding(10.dp)
                )
            }

            // ── Kart alt bilgi satırı ──────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Yazar avatar
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .background(Color(0xFF00897B).copy(alpha = 0.15f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = experiment.author.initials,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF00897B)
                    )
                }
                Spacer(Modifier.width(6.dp))

                // Yazar adı
                Text(
                    text = experiment.author.displayName,
                    fontSize = 11.sp,
                    color = Color(0xFF757575),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )

                Spacer(Modifier.width(8.dp))

                // Etiketler: zorluk + ortam
                val (chipBg, chipFg) = difficultyColors(experiment.displayDifficulty)
                Box(
                    modifier = Modifier
                        .background(chipBg, RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = experiment.displayDifficulty,
                        fontSize = 10.sp,
                        color = chipFg,
                        fontWeight = FontWeight.Medium
                    )
                }

                if (experiment.displayEnvironment.isNotBlank()) {
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = experiment.displayEnvironment,
                        fontSize = 10.sp,
                        color = Color(0xFF9E9E9E)
                    )
                }

                Spacer(Modifier.width(8.dp))

                // Favori sayısı
                Text(
                    text = "♡ ${experiment.favoriteCount}",
                    fontSize = 11.sp,
                    color = Color(0xFF9E9E9E)
                )
            }
        }
    }
}

private fun difficultyColors(level: String): Pair<Color, Color> = when (level.lowercase()) {
    "kolay", "easy"  -> Color(0xFFE8F5E9) to Color(0xFF2E7D32)
    "orta", "medium" -> Color(0xFFFFF8E1) to Color(0xFFF57F17)
    "zor",  "hard"   -> Color(0xFFFFEBEE) to Color(0xFFC62828)
    else             -> Color(0xFFF5F5F5) to Color(0xFF757575)
}