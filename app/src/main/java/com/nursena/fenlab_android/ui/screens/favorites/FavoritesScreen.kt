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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import com.nursena.fenlab_android.ui.theme.*

@Composable
fun FavoritesScreen(
    onExperimentClick: (Long) -> Unit,
    viewModel: FavoritesViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Column(modifier = Modifier.fillMaxSize().background(DarkBg)) {
        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp).padding(top = 14.dp, bottom = 10.dp)) {
            Text("Favorilerim", color = TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.Bold)
            if (uiState.favorites.isNotEmpty()) {
                Spacer(Modifier.height(2.dp))
                Text("${uiState.favorites.size} deney kayıtlı", color = TextSecondary, fontSize = 11.sp)
            }
        }

        when {
            uiState.isLoading -> LoadingIndicator()
            uiState.error != null -> ErrorMessage(message = uiState.error!!, onRetry = viewModel::loadFavorites)
            uiState.favorites.isEmpty() -> EmptyFavorites()
            else -> LazyColumn(
                contentPadding      = PaddingValues(top = 4.dp, bottom = 96.dp, start = 14.dp, end = 14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(items = uiState.favorites, key = { it.id }) { exp ->
                    FavoriteCard(
                        experiment    = exp,
                        onCardClick   = { onExperimentClick(exp.id) },
                        onRemoveClick = { viewModel.removeFromFavorites(exp) }
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyFavorites() {
    Column(
        modifier = Modifier.fillMaxSize().padding(28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("❤️", fontSize = 44.sp)
        Spacer(Modifier.height(12.dp))
        Text("Henüz favori yok", color = TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(6.dp))
        Text("Beğendiğin deneyleri favorilere\nekleyerek buradan ulaşabilirsin",
            color = TextSecondary, fontSize = 12.sp, lineHeight = 18.sp)
    }
}

@Composable
private fun FavoriteCard(experiment: Experiment, onCardClick: () -> Unit, onRemoveClick: () -> Unit) {
    Card(
        modifier  = Modifier.fillMaxWidth().clickable(onClick = onCardClick),
        shape     = RoundedCornerShape(12.dp),
        colors    = CardDefaults.cardColors(containerColor = DarkSurface),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(
            modifier              = Modifier.fillMaxWidth().padding(9.dp),
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier.size(width = 62.dp, height = 50.dp)
                    .clip(RoundedCornerShape(8.dp)).background(DarkSurface2)
            ) {
                AsyncImage(
                    model = experiment.thumbnailUrl ?: experiment.videoUrl,
                    contentDescription = experiment.title,
                    contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize()
                )
                if (experiment.videoUrl != null) {
                    Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(0.2f)), contentAlignment = Alignment.Center) {
                        Box(modifier = Modifier.size(24.dp).background(Color.White.copy(0.2f), CircleShape), contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.PlayArrow, null, tint = Color.White, modifier = Modifier.size(14.dp))
                        }
                    }
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(experiment.title, color = TextPrimary, fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold, maxLines = 2,
                    overflow = TextOverflow.Ellipsis, lineHeight = 17.sp)
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Box(modifier = Modifier.size(14.dp).background(Teal500, CircleShape), contentAlignment = Alignment.Center) {
                        Text(experiment.author.displayName.take(1).uppercase(), color = Color.White, fontSize = 7.sp, fontWeight = FontWeight.Bold)
                    }
                    Text(experiment.author.displayName, color = TextSecondary, fontSize = 11.sp,
                        maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                Spacer(Modifier.height(5.dp))
                experiment.subject?.let { SubjectChip(subject = it) }
            }

            IconButton(onClick = onRemoveClick, modifier = Modifier.size(36.dp)) {
                Icon(Icons.Default.Favorite, "Kaldır", tint = Red400, modifier = Modifier.size(18.dp))
            }
        }
    }
}