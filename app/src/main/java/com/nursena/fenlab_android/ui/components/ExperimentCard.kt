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
import androidx.compose.material.icons.filled.RemoveRedEye
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
import com.nursena.fenlab_android.ui.theme.*

@Composable
fun ExperimentCard(
    experiment: Experiment,
    onCardClick: () -> Unit,
    onFavoriteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(220.dp)
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onCardClick)
    ) {
        // ── Tam resim ─────────────────────────────────────────────────────────
        AsyncImage(
            model              = experiment.thumbnailUrl ?: experiment.videoUrl,
            contentDescription = experiment.title,
            contentScale       = ContentScale.Crop,
            modifier           = Modifier.fillMaxSize()
        )

        // ── Güçlü alt gradient — bilgilerin okunabilmesi için ─────────────────
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        0.0f to Color.Transparent,
                        0.25f to Color.Transparent,
                        0.55f to Color.Black.copy(alpha = 0.5f),
                        1.0f  to Color.Black.copy(alpha = 0.92f)
                    )
                )
        )

        // ── Play butonu (video varsa) ──────────────────────────────────────────
        if (experiment.videoUrl != null) {
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(48.dp)
                    .background(Color.White.copy(alpha = 0.18f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.PlayArrow, null,
                    tint = Color.White, modifier = Modifier.size(28.dp))
            }
        }

        // ── Favori butonu — sağ üst ────────────────────────────────────────────
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(10.dp)
                .size(34.dp)
                .background(Color.Black.copy(alpha = 0.35f), CircleShape)
                .clickable(onClick = onFavoriteClick),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (experiment.isFavoritedByCurrentUser)
                    Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                contentDescription = null,
                tint = if (experiment.isFavoritedByCurrentUser) Red400 else Color.White,
                modifier = Modifier.size(16.dp)
            )
        }

        // ── Alt bilgi bloğu — resmin üstünde şeffaf ───────────────────────────
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp)
        ) {
            // Başlık
            Text(
                text       = experiment.title,
                color      = Color.White,
                fontSize   = 15.sp,
                fontWeight = FontWeight.Bold,
                maxLines   = 1,
                overflow   = TextOverflow.Ellipsis
            )

            Spacer(Modifier.height(6.dp))

            // Yazar satırı
            Row(
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(7.dp)
            ) {
                UserAvatar(user = experiment.author, size = 26.dp)

                Text(
                    text     = experiment.author.displayName,
                    color    = Color.White.copy(alpha = 0.9f),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                // Görüntülenme
                Row(verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                    Icon(Icons.Default.RemoveRedEye, null,
                        tint = Color.White.copy(alpha = 0.7f),
                        modifier = Modifier.size(12.dp))
                    Text(formatCount(experiment.favoriteCount * 3),
                        color = Color.White.copy(alpha = 0.7f), fontSize = 11.sp)
                }

                // Beğeni
                Row(verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                    Icon(Icons.Default.FavoriteBorder, null,
                        tint = Color.White.copy(alpha = 0.7f),
                        modifier = Modifier.size(12.dp))
                    Text(formatCount(experiment.favoriteCount),
                        color = Color.White.copy(alpha = 0.7f), fontSize = 11.sp)
                }
            }

            Spacer(Modifier.height(7.dp))

            // Chip satırı
            Row(
                horizontalArrangement = Arrangement.spacedBy(5.dp),
                verticalAlignment     = Alignment.CenterVertically
            ) {
                DifficultyChip(difficulty = experiment.difficulty)
                experiment.environment?.let { EnvironmentChip(environment = it) }
                experiment.subject?.let { SubjectChip(subject = it) }
            }
        }
    }
}