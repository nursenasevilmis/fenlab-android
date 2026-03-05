package com.nursena.fenlab_android.ui.screens.favorites

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RemoveRedEye
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.nursena.fenlab_android.domain.model.Experiment
import com.nursena.fenlab_android.ui.components.ErrorMessage
import com.nursena.fenlab_android.ui.components.LoadingIndicator
import com.nursena.fenlab_android.ui.components.SubjectChip
import com.nursena.fenlab_android.ui.components.formatCount
import com.nursena.fenlab_android.ui.theme.*

@Composable
fun FavoritesScreen(
    onExperimentClick: (Long) -> Unit,
    viewModel: FavoritesViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier.fillMaxSize().background(DarkBg)
    ) {
        FavoritesHeader(count = uiState.favorites.size)

        when {
            uiState.isLoading -> LoadingIndicator()
            uiState.error != null -> ErrorMessage(
                message = uiState.error!!,
                onRetry = viewModel::loadFavorites
            )
            uiState.favorites.isEmpty() -> FavoritesEmptyState()
            else -> FavoritesList(
                favorites         = uiState.favorites,
                onExperimentClick = onExperimentClick,
                onRemoveClick     = viewModel::removeFromFavorites
            )
        }
    }
}

@Composable
private fun FavoritesHeader(count: Int) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(DarkBg)
            .padding(horizontal = 16.dp)
            .padding(top = 16.dp, bottom = 12.dp)
    ) {
        Text("Favorilerim", color = TextPrimary, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        if (count > 0) {
            Spacer(Modifier.height(4.dp))
            Text("$count deney kayıtlı", color = TextSecondary, fontSize = 13.sp)
        }
    }
}

@Composable
private fun FavoritesEmptyState() {
    Column(
        modifier            = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("❤️", fontSize = 56.sp)
        Spacer(Modifier.height(16.dp))
        Text("Henüz favori yok", color = TextPrimary,
            fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(8.dp))
        Text(
            text      = "Beğendiğin deneyleri favorilere\nekleyerek buradan ulaşabilirsin",
            color     = TextSecondary, fontSize = 14.sp, lineHeight = 21.sp
        )
    }
}

@Composable
private fun FavoritesList(
    favorites: List<Experiment>,
    onExperimentClick: (Long) -> Unit,
    onRemoveClick: (Experiment) -> Unit
) {
    LazyColumn(
        contentPadding      = PaddingValues(top = 4.dp, bottom = 100.dp, start = 16.dp, end = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(items = favorites, key = { it.id }) { exp ->
            FavoriteCard(
                experiment    = exp,
                onCardClick   = { onExperimentClick(exp.id) },
                onRemoveClick = { onRemoveClick(exp) }
            )
        }
    }
}

@Composable
private fun FavoriteCard(
    experiment: Experiment,
    onCardClick: () -> Unit,
    onRemoveClick: () -> Unit
) {
    Card(
        modifier  = Modifier.fillMaxWidth().clickable(onClick = onCardClick),
        shape     = RoundedCornerShape(14.dp),
        colors    = CardDefaults.cardColors(containerColor = DarkSurface),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(
            modifier              = Modifier.fillMaxWidth().padding(10.dp),
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Thumbnail
            Box(
                modifier = Modifier
                    .size(width = 96.dp, height = 78.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(DarkSurface2)
            ) {
                AsyncImage(
                    model              = experiment.thumbnailUrl ?: experiment.videoUrl,
                    contentDescription = experiment.title,
                    contentScale       = ContentScale.Crop,
                    modifier           = Modifier.fillMaxSize()
                )
                Box(
                    modifier = Modifier.fillMaxSize().background(
                        Brush.verticalGradient(0.5f to Color.Transparent, 1f to Color.Black.copy(0.5f))
                    )
                )
                if (experiment.videoUrl != null) {
                    Box(
                        modifier         = Modifier.fillMaxSize().background(Color.Black.copy(0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier         = Modifier.size(26.dp).background(Color.White.copy(0.2f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.PlayArrow, null, tint = Color.White,
                                modifier = Modifier.size(15.dp))
                        }
                    }
                }
            }

            // Bilgiler
            Column(modifier = Modifier.weight(1f)) {
                Text(experiment.title, color = TextPrimary, fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold, maxLines = 2,
                    overflow = TextOverflow.Ellipsis, lineHeight = 20.sp)

                Spacer(Modifier.height(5.dp))

                Row(verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                    Box(
                        modifier         = Modifier.size(16.dp).background(Teal500, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(experiment.author.displayName.take(1).uppercase(),
                            color = Color.White, fontSize = 7.sp, fontWeight = FontWeight.Bold)
                    }
                    Text(experiment.author.displayName, color = TextSecondary, fontSize = 12.sp,
                        maxLines = 1, overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false))
                    if (experiment.author.isTeacher) {
                        Box(
                            modifier = Modifier.background(Orange400.copy(0.15f), RoundedCornerShape(4.dp))
                                .padding(horizontal = 5.dp, vertical = 1.dp)
                        ) {
                            Text("Öğretmen", color = Orange400, fontSize = 10.sp,
                                fontWeight = FontWeight.Medium)
                        }
                    }
                }

                Spacer(Modifier.height(6.dp))

                Row(verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    experiment.subject?.let { SubjectChip(subject = it) }
                    Spacer(Modifier.weight(1f))
                    Row(verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                        Icon(Icons.Default.RemoveRedEye, null,
                            tint = TextSecondary, modifier = Modifier.size(11.dp))
                        Text(formatCount(experiment.favoriteCount * 3),
                            color = TextSecondary, fontSize = 10.sp)
                    }
                }
            }

            // Kırmızı kalp + sayı
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                IconButton(onClick = onRemoveClick, modifier = Modifier.size(36.dp)) {
                    Icon(Icons.Default.Favorite, "Favorilerden kaldır",
                        tint = Red400, modifier = Modifier.size(20.dp))
                }
                Text(formatCount(experiment.favoriteCount), color = TextSecondary, fontSize = 10.sp)
            }
        }
    }
}