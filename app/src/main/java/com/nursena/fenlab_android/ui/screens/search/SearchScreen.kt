package com.nursena.fenlab_android.ui.screens.search

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RemoveRedEye
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.nursena.fenlab_android.domain.model.Experiment
import com.nursena.fenlab_android.ui.components.EmptyState
import com.nursena.fenlab_android.ui.components.ErrorMessage
import com.nursena.fenlab_android.ui.components.LoadingIndicator
import com.nursena.fenlab_android.ui.components.SubjectChip
import com.nursena.fenlab_android.ui.components.formatCount
import com.nursena.fenlab_android.ui.theme.*

@Composable
fun SearchScreen(
    onExperimentClick: (Long) -> Unit,
    viewModel: SearchViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val focusRequester = remember { FocusRequester() }
    val keyboard = LocalSoftwareKeyboardController.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg)
    ) {
        // ── Top bar ile arama kutusu ──────────────────────────────────────────
        SearchHeader(
            query          = uiState.query,
            onQueryChange  = viewModel::onQueryChange,
            onClear        = { viewModel.onQueryChange("") },
            focusRequester = focusRequester,
            onSearch       = { keyboard?.hide() }
        )

        // ── İçerik ───────────────────────────────────────────────────────────
        when {
            uiState.isLoading -> LoadingIndicator()

            uiState.error != null -> ErrorMessage(
                message = uiState.error!!,
                onRetry = { viewModel.onQueryChange(uiState.query) }
            )

            uiState.query.isBlank() -> HintContent(
                isLoading        = uiState.isLoadingAll,
                allExperiments   = uiState.allExperiments,
                onTrendClick     = viewModel::onTrendClick,
                onExperimentClick = onExperimentClick,
                onFavoriteClick  = viewModel::toggleFavorite
            )

            uiState.isEmpty -> EmptyState(
                emoji    = "🔍",
                title    = "Sonuç bulunamadı",
                subtitle = "\"${uiState.query}\" için deney yok"
            )

            else -> SearchResultList(
                results          = uiState.results,
                onExperimentClick = onExperimentClick,
                onFavoriteClick  = viewModel::toggleFavorite
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Header + Arama kutusu
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun SearchHeader(
    query: String,
    onQueryChange: (String) -> Unit,
    onClear: () -> Unit,
    focusRequester: FocusRequester,
    onSearch: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(DarkBg)
            .padding(horizontal = 16.dp)
            .padding(top = 16.dp, bottom = 12.dp)
    ) {
        Text(
            text       = "Deney Ara",
            color      = TextPrimary,
            fontSize   = 24.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(12.dp))

        TextField(
            value         = query,
            onValueChange = onQueryChange,
            placeholder   = {
                Text("Deney adı, konu, öğretmen...",
                    color = TextSecondary, fontSize = 14.sp)
            },
            leadingIcon = {
                Icon(Icons.Default.Search, null, tint = TextSecondary,
                    modifier = Modifier.size(20.dp))
            },
            trailingIcon = {
                if (query.isNotBlank()) {
                    IconButton(onClick = onClear) {
                        Icon(Icons.Default.Close, null, tint = TextSecondary,
                            modifier = Modifier.size(18.dp))
                    }
                }
            },
            singleLine      = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = { onSearch() }),
            shape  = RoundedCornerShape(14.dp),
            colors = TextFieldDefaults.colors(
                focusedContainerColor   = DarkSurface2,
                unfocusedContainerColor = DarkSurface2,
                focusedTextColor        = TextPrimary,
                unfocusedTextColor      = TextPrimary,
                cursorColor             = Teal400,
                focusedIndicatorColor   = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent
            ),
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(focusRequester)
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Hint durumu: Trend aramalar + Tüm Deneyler
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun HintContent(
    isLoading: Boolean,
    allExperiments: List<Experiment>,
    onTrendClick: (String) -> Unit,
    onExperimentClick: (Long) -> Unit,
    onFavoriteClick: (Experiment) -> Unit
) {
    LazyColumn(
        contentPadding      = PaddingValues(bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        // Trend aramalar
        item {
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("🔍", fontSize = 16.sp)
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text       = "Trend Aramalar",
                        color      = TextPrimary,
                        fontSize   = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Spacer(Modifier.height(10.dp))
            }
        }

        item {
            LazyRow(
                contentPadding        = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(TRENDING_SEARCHES) { trend ->
                    TrendChip(label = trend, onClick = { onTrendClick(trend) })
                }
            }
            Spacer(Modifier.height(20.dp))
        }

        // Tüm Deneyler başlığı
        item {
            Row(
                modifier          = Modifier.padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("📋", fontSize = 16.sp)
                Spacer(Modifier.width(6.dp))
                Text(
                    text       = "Tüm Deneyler",
                    color      = TextPrimary,
                    fontSize   = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Spacer(Modifier.height(8.dp))
        }

        if (isLoading) {
            item { LoadingIndicator() }
        } else {
            items(items = allExperiments, key = { it.id }) { exp ->
                CompactExperimentCard(
                    experiment      = exp,
                    onCardClick     = { onExperimentClick(exp.id) },
                    onFavoriteClick = { onFavoriteClick(exp) }
                )
                HorizontalDivider(
                    modifier  = Modifier.padding(horizontal = 16.dp),
                    thickness = 0.5.dp,
                    color     = DarkSurface2
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Arama sonuçları listesi
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun SearchResultList(
    results: List<Experiment>,
    onExperimentClick: (Long) -> Unit,
    onFavoriteClick: (Experiment) -> Unit
) {
    LazyColumn(
        contentPadding = PaddingValues(bottom = 100.dp)
    ) {
        item {
            Text(
                text     = "${results.size} sonuç bulundu",
                color    = TextSecondary,
                fontSize = 12.sp,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }
        items(items = results, key = { it.id }) { exp ->
            CompactExperimentCard(
                experiment      = exp,
                onCardClick     = { onExperimentClick(exp.id) },
                onFavoriteClick = { onFavoriteClick(exp) }
            )
            HorizontalDivider(
                modifier  = Modifier.padding(horizontal = 16.dp),
                thickness = 0.5.dp,
                color     = DarkSurface2
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Küçük kart — referanstaki gibi: sol küçük resim, sağda bilgiler
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun CompactExperimentCard(
    experiment: Experiment,
    onCardClick: () -> Unit,
    onFavoriteClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onCardClick)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Küçük kare resim
        Box(
            modifier = Modifier
                .size(width = 88.dp, height = 72.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(DarkSurface2)
        ) {
            AsyncImage(
                model              = experiment.thumbnailUrl ?: experiment.videoUrl,
                contentDescription = experiment.title,
                contentScale       = ContentScale.Crop,
                modifier           = Modifier.fillMaxSize()
            )
            // Play ikonu
            if (experiment.videoUrl != null) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.25f)),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .background(Color.White.copy(alpha = 0.2f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.PlayArrow, null,
                            tint = Color.White, modifier = Modifier.size(16.dp))
                    }
                }
            }
        }

        // Sağ: Bilgiler
        Column(modifier = Modifier.weight(1f)) {
            // Başlık
            Text(
                text       = experiment.title,
                color      = TextPrimary,
                fontSize   = 14.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines   = 2,
                overflow   = TextOverflow.Ellipsis,
                lineHeight = 20.sp
            )

            Spacer(Modifier.height(4.dp))

            // Yazar + rol
            Row(
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Avatar küçük
                Box(
                    modifier = Modifier
                        .size(18.dp)
                        .background(Teal500, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text     = experiment.author.displayName.take(1).uppercase(),
                        color    = Color.White,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Text(
                    text     = experiment.author.displayName,
                    color    = TextSecondary,
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false)
                )
                if (experiment.author.isTeacher) {
                    Box(
                        modifier = Modifier
                            .background(Orange400.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 5.dp, vertical = 1.dp)
                    ) {
                        Text("Öğretmen", color = Orange400, fontSize = 10.sp,
                            fontWeight = FontWeight.Medium)
                    }
                }
            }

            Spacer(Modifier.height(6.dp))

            // Chip + istatistikler
            Row(
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                experiment.subject?.let { SubjectChip(subject = it) }

                Spacer(Modifier.weight(1f))

                // Görüntülenme
                Row(verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                    Icon(Icons.Default.RemoveRedEye, null,
                        tint = TextSecondary, modifier = Modifier.size(12.dp))
                    Text(formatCount(experiment.favoriteCount * 3),
                        color = TextSecondary, fontSize = 11.sp)
                }

                // Favori butonu
                IconButton(
                    onClick  = onFavoriteClick,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = if (experiment.isFavoritedByCurrentUser)
                            Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = null,
                        tint = if (experiment.isFavoritedByCurrentUser) Red400 else TextSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Trend chip
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun TrendChip(label: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(DarkSurface2)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        Text(
            text      = label,
            color     = TextPrimary,
            fontSize  = 13.sp,
            fontWeight = FontWeight.Medium
        )
    }
}