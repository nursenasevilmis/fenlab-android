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
        shape     = RoundedCornerShape(16.dp),
        colors    = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {

            // ── Medya alanı ────────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            ) {
                // Thumbnail veya video URL'den resim
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
                                0.55f to Color.Transparent,
                                1.0f to Color.Black.copy(alpha = 0.68f)
                            )
                        )
                )

                // Ders chip — sol üst
                experiment.subject?.let { subject ->
                    SubjectChip(
                        subject  = subject,
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(10.dp)
                    )
                }

                // Favori butonu — sağ üst
                FavoriteButton(
                    isFavorited = experiment.isFavoritedByCurrentUser,
                    onClick     = onFavoriteClick,
                    modifier    = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                )

                // Play ikonu — sadece videoUrl varsa
                if (experiment.videoUrl != null) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .size(50.dp)
                            .background(Color.White.copy(alpha = 0.85f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector        = Icons.Default.PlayArrow,
                            contentDescription = "Videoyu Oynat",
                            tint               = Color(0xFF00897B),
                            modifier           = Modifier.size(30.dp)
                        )
                    }
                }

                // Başlık — sol alt overlay üzerinde
                Text(
                    text       = experiment.title,
                    color      = Color.White,
                    fontSize   = 16.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines   = 2,
                    overflow   = TextOverflow.Ellipsis,
                    modifier   = Modifier
                        .align(Alignment.BottomStart)
                        .padding(12.dp)
                )
            }

            // ── Kart içeriği ───────────────────────────────────────────────
            Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {

                // Yazar satırı
                AuthorRow(
                    user          = experiment.author,
                    favoriteCount = experiment.favoriteCount,
                    commentCount  = experiment.commentCount
                )

                Spacer(Modifier.height(8.dp))

                // Açıklama
                Text(
                    text       = experiment.description,
                    fontSize   = 13.sp,
                    color      = Color(0xFF555555),
                    maxLines   = 2,
                    overflow   = TextOverflow.Ellipsis,
                    lineHeight = 18.sp
                )

                Spacer(Modifier.height(10.dp))

                // Alt etiket satırı: zorluk + ortam + konu
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    DifficultyChip(difficulty = experiment.difficulty)

                    experiment.environment?.let { env ->
                        EnvironmentChip(environment = env)
                    }

                    experiment.subject?.let { sub ->
                        TagChip(text = sub.toDisplayString())
                    }
                }
            }
        }
    }
}