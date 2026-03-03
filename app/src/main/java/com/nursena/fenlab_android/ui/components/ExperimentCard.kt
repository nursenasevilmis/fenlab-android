package com.nursena.fenlab_android.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
        // ── Resim ──────────────────────────────────────────────────────────
        AsyncImage(
            model              = experiment.thumbnailUrl ?: experiment.videoUrl,
            contentDescription = experiment.title,
            contentScale       = ContentScale.Crop,
            modifier           = Modifier.fillMaxSize()
        )

        // ── Gradient overlay (alt yarı) ────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        0.0f  to Color.Transparent,
                        0.35f to Color.Transparent,
                        1.0f  to Color.Black.copy(alpha = 0.88f)
                    )
                )
        )

        // ── Sol üst: Subject chip ──────────────────────────────────────────
        experiment.subject?.let {
            SubjectChip(
                subject  = it,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(12.dp)
            )
        }

        // ── Sağ üst: Favori butonu ─────────────────────────────────────────
        FavoriteButton(
            isFavorited = experiment.isFavoritedByCurrentUser,
            onClick     = onFavoriteClick,
            modifier    = Modifier
                .align(Alignment.TopEnd)
                .padding(10.dp)
        )

        // ── Orta: Play ikonu (video varsa) ─────────────────────────────────
        if (experiment.videoUrl != null) {
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(48.dp)
                    .background(Color.White.copy(alpha = 0.2f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector        = Icons.Default.PlayArrow,
                    contentDescription = null,
                    tint               = Color.White,
                    modifier           = Modifier.size(28.dp)
                )
            }
        }

        // ── Alt bilgi bloğu ────────────────────────────────────────────────
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(12.dp)
        ) {
            // Başlık
            Text(
                text       = experiment.title,
                color      = Color.White,
                fontSize   = 15.sp,
                fontWeight = FontWeight.Bold,
                maxLines   = 2,
                overflow   = TextOverflow.Ellipsis
            )

            Spacer(Modifier.height(8.dp))

            // Yazar + chip'ler
            Row(
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                UserAvatar(user = experiment.author, size = 26.dp)

                Text(
                    text     = experiment.author.displayName,
                    color    = Color.White.copy(alpha = 0.85f),
                    fontSize = 12.sp,
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                DifficultyChip(difficulty = experiment.difficulty)

                experiment.environment?.let { EnvironmentChip(environment = it) }

                // Favori sayısı
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text     = "♡ ${formatCount(experiment.favoriteCount)}",
                        color    = Color.White.copy(alpha = 0.75f),
                        fontSize = 11.sp
                    )
                }
            }
        }
    }
}