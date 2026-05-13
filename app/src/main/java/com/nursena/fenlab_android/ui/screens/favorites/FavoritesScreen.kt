package com.nursena.fenlab_android.ui.screens.favorites

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.nursena.fenlab_android.core.toMinioUrl
import com.nursena.fenlab_android.core.base.UiEvent
import com.nursena.fenlab_android.domain.model.Experiment
import com.nursena.fenlab_android.ui.components.ErrorMessage
import com.nursena.fenlab_android.ui.components.LoadingIndicator
import com.nursena.fenlab_android.ui.theme.*
import com.airbnb.lottie.compose.*
import com.nursena.fenlab_android.ui.components.LottieLoadingIndicator

@Composable
fun FavoritesScreen(
    onExperimentClick: (Long) -> Unit,
    onSessionExpired: () -> Unit, // ← eklendi
    viewModel: FavoritesViewModel = hiltViewModel()
) {
    // UI state
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Global session expired listener
    LaunchedEffect(viewModel) {
        viewModel.eventFlow.collect { event ->
            if (event is UiEvent.SessionExpired) {
                onSessionExpired()
            }
        }
    }

    // Tab'a her geçişte favorileri yenile
    LaunchedEffect(Unit) {
        viewModel.loadFavorites()
    }

    Column(modifier = Modifier
        .fillMaxSize()
        .background(Brush.verticalGradient(listOf(GradientStart, GradientMid, GradientEnd)))
    ) {

        // ── Header ─────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                //.background(Brush.verticalGradient(listOf(GradientStart, GradientEnd)))
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 16.dp)
        ) {
            Column {
                Text(
                    "Favorilerim",
                    color = TextPrimary,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold
                )
                if (uiState.favorites.isNotEmpty()) {
                    Spacer(Modifier.height(3.dp))
                    Text(
                        "${uiState.favorites.size} deney kaydedildi",
                        color = TextSecondary,
                        fontSize = 13.sp
                    )
                }
            }
        }

        // ── İçerik ─────────────────────────────
        when {
            uiState.isLoading -> LottieLoadingIndicator()
            uiState.error != null -> ErrorMessage(message = uiState.error!!, onRetry = viewModel::loadFavorites)
            uiState.favorites.isEmpty() -> EmptyFavorites()
            else -> LazyColumn(
                contentPadding = PaddingValues(top = 8.dp, bottom = 100.dp, start = 16.dp, end = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                itemsIndexed(
                    items = uiState.favorites,
                    key = { _: Int, exp: Experiment -> exp.id } // explicit key
                ) { _: Int, exp: Experiment ->
                    FavoriteCard(
                        experiment = exp,
                        onCardClick = { onExperimentClick(exp.id) },
                        onRemoveClick = { viewModel.removeFromFavorites(exp) }
                    )
                }
            }
        }
    }
}

// ── Boş durum ─────────────────────────────
@Composable
private fun EmptyFavorites() {
    val composition by rememberLottieComposition(
        LottieCompositionSpec.Asset("not-data-animation.json")
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(40.dp)
            .offset(y = (-60).dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(
            0.dp,
            Alignment.CenterVertically
        )
    ) {
        LottieAnimation(
            composition = composition,
            iterations = LottieConstants.IterateForever,
            modifier = Modifier.size(150.dp)
        )

        Spacer(Modifier.height(14.dp))

        Text(
            "Henüz favori yok",
            color = TextPrimary,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(8.dp))

        Text(
            "Beğendiğin deneyleri favorilere\nekleyerek buradan ulaşabilirsin",
            color = TextSecondary,
            fontSize = 14.sp,
            lineHeight = 21.sp,
            textAlign = TextAlign.Center
        )
    }
}

// ── Favori kart ─────────────────────────────
@Composable
private fun FavoriteCard(
    experiment: Experiment,
    onCardClick: () -> Unit,
    onRemoveClick: () -> Unit
) {
    var showConfirm by remember { mutableStateOf(false) }

    if (showConfirm) {
        AlertDialog(
            onDismissRequest = { showConfirm = false },
            containerColor = Color(0xFFFFFFFF),
            title = { Text("Favoriden Kaldır", color = TextPrimary, fontWeight = FontWeight.SemiBold) },
            text = { Text("Bu deneyi favorilerden kaldırmak istiyor musun?", color = TextSecondary, fontSize = 13.sp) },
            confirmButton = {
                TextButton(onClick = { showConfirm = false; onRemoveClick() }) {
                    Text("Kaldır", color = Red400, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirm = false }) {
                    Text("İptal", color = TextSecondary)
                }
            }
        )
    }

    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onCardClick),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFFFF)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Thumbnail
            Box(
                modifier = Modifier.size(width = 88.dp, height = 72.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Brush.linearGradient(listOf(Color(0xFFFFFFFF), Color(0xFFFFFFFF))))
            ) {
                AsyncImage(
                    model = experiment.thumbnailUrl ?: experiment.videoUrl,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                if (experiment.videoUrl != null) {
                    Box(
                        modifier = Modifier.fillMaxSize().background(Color(0x40000000)),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier.size(26.dp).background(Color(0x33FFFFFF), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.PlayArrow, null, tint = Color.White, modifier = Modifier.size(15.dp))
                        }
                    }
                }
            }

            // Bilgiler
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    experiment.title,
                    color = TextPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 18.sp
                )
                Spacer(Modifier.height(5.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Box(
                        modifier = Modifier.size(16.dp).clip(CircleShape)
                            .background(FenGreenDark),
                        contentAlignment = Alignment.Center
                    ) {
                        if (experiment.author.profileImageUrl != null) {
                            AsyncImage(
                                model              = experiment.author.profileImageUrl.toMinioUrl(),
                                contentDescription = null,
                                contentScale       = ContentScale.Crop,
                                modifier           = Modifier.fillMaxSize().clip(CircleShape)
                            )
                        } else {
                            Text(
                                experiment.author.displayName.take(1).uppercase(),
                                color      = Color.White,
                                fontSize   = 7.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Text(
                        experiment.author.displayName,
                        color = TextSecondary,
                        fontSize = 11.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Spacer(Modifier.height(6.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    experiment.averageRating?.let { rating ->
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                            Icon(Icons.Default.Star, null, tint = Orange400, modifier = Modifier.size(11.dp))
                            Text("%.1f".format(rating), color = TextSecondary, fontSize = 11.sp)
                        }
                    }
                    experiment.subject?.let { subject ->
                        Box(
                            modifier = Modifier
                                .background(Color(0x1FF06292), RoundedCornerShape(6.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(subject.toDisplayString(), color = FenGreen, fontSize = 10.sp, fontWeight = FontWeight.Medium)
                        }
                    }
                }
            }

            // Kaldır butonu
            Box(
                modifier = Modifier.size(34.dp)
                    .clip(CircleShape)
                    .background(Color(0x1AEF5350))
                    .clickable { showConfirm = true },
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Favorite, null, tint = Red400, modifier = Modifier.size(16.dp))
            }
        }
    }
}