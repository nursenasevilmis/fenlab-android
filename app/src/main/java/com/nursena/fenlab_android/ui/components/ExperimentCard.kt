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
                .height(160.dp)
                .clip(RoundedCornerShape(12.dp))
                .clickable(onClick = onCardClick)
        ) {
            // ── Tam resim ─────────────────────────────────────────────────────────
            AsyncImage(
                model = coil.request.ImageRequest.Builder(androidx.compose.ui.platform.LocalContext.current)
                    .data(experiment.thumbnailUrl ?: experiment.videoUrl)
                    .allowHardware(false)
                    .build(),
                contentDescription = experiment.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
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
                    .size(30.dp)
                    .background(Color.Black.copy(alpha = 0.35f), CircleShape)
                    .clickable(onClick = onFavoriteClick),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (experiment.isFavoritedByCurrentUser)
                        Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = null,
                    tint = if (experiment.isFavoritedByCurrentUser) Red400 else Color.White,
                    modifier = Modifier.size(14.dp)
                )
            }

            // ── Alt bilgi — sadece başlık + rating ───────────────────────────────────
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp)
            ) {
                Text(
                    text       = experiment.title,
                    color      = Color.White,
                    fontSize   = 14.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines   = 2,
                    overflow   = TextOverflow.Ellipsis,
                    lineHeight = 18.sp
                )
                Spacer(Modifier.height(5.dp))
                Row(
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(Icons.Default.Star, null, tint = Yellow400, modifier = Modifier.size(13.dp))
                    Text(
                        text  = experiment.averageRating?.let { "%.1f".format(it) } ?: "—",
                        color = Color.White.copy(alpha = 0.9f),
                        fontSize = 12.sp, fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }