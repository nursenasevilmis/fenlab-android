package com.nursena.fenlab_android.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Star
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
import com.nursena.fenlab_android.ui.components.AnimatedFavoriteButton
@Composable
fun ExperimentCard(
    experiment: Experiment,
    onCardClick: () -> Unit,
    onFavoriteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier  = modifier.fillMaxWidth().clickable(onClick = onCardClick),
        shape     = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors    = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column {
            // ── Üst: resim alanı ──────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(170.dp)
            ) {
                // Resim
                AsyncImage(
                    model = coil.request.ImageRequest.Builder(
                        androidx.compose.ui.platform.LocalContext.current
                    ).data(experiment.thumbnailUrl ?: experiment.videoUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = experiment.title,
                    contentScale       = ContentScale.Crop,
                    modifier           = Modifier.fillMaxSize()
                )

                // Alt gradient — başlık okunabilir olsun
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                0.0f to Color.Transparent,
                                0.4f to Color.Transparent,
                                1.0f to Color(0xCC000000)
                            )
                        )
                )

                // Sağ üst — favori + rating yan yana
                Row(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment     = Alignment.CenterVertically
                ) {
                    // Rating chip
                    experiment.averageRating?.let { rating ->
                        Row(
                            modifier = Modifier
                                .background(Color(0xCC000000), RoundedCornerShape(20.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment     = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            Icon(Icons.Default.Star, null,
                                tint     = Yellow400,
                                modifier = Modifier.size(12.dp))
                            Text(
                                "%.1f".format(rating),
                                color      = Color.White,
                                fontSize   = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    // Favori butonu
                    AnimatedFavoriteButton(
                        isFavorited = experiment.isFavoritedByCurrentUser,
                        backgroundColor = Color(0x73000000),
                        iconWhenNotFavorited = Color.White,
                        onClick = onFavoriteClick
                    )
                }

                // Alt sol — sadece başlık
                Text(
                    text       = experiment.title,
                    color      = Color.White,
                    fontSize   = 15.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines   = 2,
                    overflow   = TextOverflow.Ellipsis,
                    lineHeight = 20.sp,
                    modifier   = Modifier
                        .align(Alignment.BottomStart)
                        .padding(horizontal = 12.dp, vertical = 10.dp)
                        .fillMaxWidth(0.85f)
                )
            }

            // ── Alt: beyaz açıklama alanı ────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(horizontal = 12.dp, vertical = 10.dp)
            ) {
                Text(
                    text       = experiment.description,
                    color      = TextSecondary,
                    fontSize   = 12.sp,
                    lineHeight = 17.sp,
                    maxLines   = 2,
                    overflow   = TextOverflow.Ellipsis
                )
            }
        }
    }
}