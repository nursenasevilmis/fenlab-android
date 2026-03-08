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
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import com.nursena.fenlab_android.ui.theme.*

@Composable
fun SearchScreen(
    onExperimentClick: (Long) -> Unit,
    viewModel: SearchViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val keyboard = LocalSoftwareKeyboardController.current

    Column(modifier = Modifier.fillMaxSize().background(DarkBg)) {
        SearchHeader(
            query         = uiState.query,
            onQueryChange = viewModel::onQueryChange,
            onClear       = { viewModel.onQueryChange("") },
            onSearch      = { keyboard?.hide() }
        )

        when {
            uiState.isLoading -> LoadingIndicator()
            uiState.error != null -> ErrorMessage(message = uiState.error!!, onRetry = { viewModel.onQueryChange(uiState.query) })
            uiState.query.isBlank() -> HintContent(
                recentSearches = uiState.recentSearches,
                onRecentClick  = viewModel::onRecentClick,
                onRemoveRecent = viewModel::removeRecent,
                onClearAll     = viewModel::clearRecents,
                onTrendClick   = viewModel::onQueryChange
            )
            uiState.isEmpty -> EmptyState(emoji = "🔍", title = "Sonuç bulunamadı", subtitle = "\"${uiState.query}\" için deney bulunamadı")
            else -> SearchResultList(results = uiState.results, onExperimentClick = onExperimentClick, onFavoriteClick = viewModel::toggleFavorite)
        }
    }
}

@Composable
private fun SearchHeader(query: String, onQueryChange: (String) -> Unit, onClear: () -> Unit, onSearch: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth().background(DarkBg)
            .padding(horizontal = 14.dp).padding(top = 14.dp, bottom = 10.dp)
    ) {
        Text("Deney Ara", color = TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(10.dp))
        TextField(
            value = query, onValueChange = onQueryChange,
            placeholder = { Text("Deney adı, konu, öğretmen...", color = TextSecondary, fontSize = 13.sp) },
            leadingIcon  = { Icon(Icons.Default.Search, null, tint = TextSecondary, modifier = Modifier.size(18.dp)) },
            trailingIcon = {
                if (query.isNotBlank()) {
                    IconButton(onClick = onClear) { Icon(Icons.Default.Close, null, tint = TextSecondary, modifier = Modifier.size(16.dp)) }
                }
            },
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = { onSearch() }),
            shape  = RoundedCornerShape(12.dp),
            colors = TextFieldDefaults.colors(
                focusedContainerColor   = DarkSurface2,
                unfocusedContainerColor = DarkSurface2,
                focusedTextColor        = TextPrimary,
                unfocusedTextColor      = TextPrimary,
                cursorColor             = Teal400,
                focusedIndicatorColor   = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent
            ),
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun HintContent(
    recentSearches: List<String>,
    onRecentClick: (String) -> Unit,
    onRemoveRecent: (String) -> Unit,
    onClearAll: () -> Unit,
    onTrendClick: (String) -> Unit
) {
    LazyColumn(contentPadding = PaddingValues(bottom = 96.dp)) {
        if (recentSearches.isNotEmpty()) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp).padding(top = 6.dp, bottom = 8.dp),
                    verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                        Icon(Icons.Default.History, null, tint = TextSecondary, modifier = Modifier.size(15.dp))
                        Text("Son Arananlar", color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    }
                    Text("Temizle", color = Teal400, fontSize = 11.sp, modifier = Modifier.clickable(onClick = onClearAll))
                }
            }
            items(recentSearches) { term ->
                Row(
                    modifier = Modifier.fillMaxWidth().clickable { onRecentClick(term) }
                        .padding(horizontal = 14.dp, vertical = 9.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(modifier = Modifier.size(30.dp).background(DarkSurface2, CircleShape), contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.History, null, tint = TextSecondary, modifier = Modifier.size(14.dp))
                    }
                    Text(term, color = TextPrimary, fontSize = 13.sp, modifier = Modifier.weight(1f))
                    IconButton(onClick = { onRemoveRecent(term) }, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.Close, null, tint = TextSecondary, modifier = Modifier.size(12.dp))
                    }
                }
                HorizontalDivider(modifier = Modifier.padding(horizontal = 14.dp), thickness = 0.5.dp, color = DarkSurface2)
            }
            item { Spacer(Modifier.height(16.dp)) }
        }

        item {
            Row(modifier = Modifier.padding(horizontal = 14.dp).padding(bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                Text("🔥", fontSize = 14.sp)
                Text("Trend Aramalar", color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            }
        }
        item {
            LazyRow(contentPadding = PaddingValues(horizontal = 14.dp), horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                items(TRENDING_SEARCHES) { trend ->
                    TrendChip(label = trend, onClick = { onTrendClick(trend) })
                }
            }
        }
    }
}

@Composable
private fun SearchResultList(results: List<Experiment>, onExperimentClick: (Long) -> Unit, onFavoriteClick: (Experiment) -> Unit) {
    LazyColumn(contentPadding = PaddingValues(bottom = 96.dp)) {
        item {
            Text("${results.size} sonuç", color = TextSecondary, fontSize = 11.sp,
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp))
        }
        items(items = results, key = { it.id }) { exp ->
            CompactExperimentCard(experiment = exp, onCardClick = { onExperimentClick(exp.id) }, onFavoriteClick = { onFavoriteClick(exp) })
            HorizontalDivider(modifier = Modifier.padding(horizontal = 14.dp), thickness = 0.5.dp, color = DarkSurface2)
        }
    }
}

@Composable
private fun CompactExperimentCard(experiment: Experiment, onCardClick: () -> Unit, onFavoriteClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onCardClick)
            .padding(horizontal = 14.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(modifier = Modifier.size(width = 80.dp, height = 64.dp).clip(RoundedCornerShape(9.dp)).background(DarkSurface2)) {
            AsyncImage(model = experiment.thumbnailUrl ?: experiment.videoUrl, contentDescription = experiment.title,
                contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
            if (experiment.videoUrl != null) {
                Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(0.22f)), contentAlignment = Alignment.Center) {
                    Box(modifier = Modifier.size(24.dp).background(Color.White.copy(0.2f), CircleShape), contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.PlayArrow, null, tint = Color.White, modifier = Modifier.size(14.dp))
                    }
                }
            }
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(experiment.title, color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold,
                maxLines = 2, overflow = TextOverflow.Ellipsis, lineHeight = 18.sp)
            Spacer(Modifier.height(3.dp))
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                Box(modifier = Modifier.size(16.dp).background(Teal500, CircleShape), contentAlignment = Alignment.Center) {
                    Text(experiment.author.displayName.take(1).uppercase(), color = Color.White, fontSize = 7.sp, fontWeight = FontWeight.Bold)
                }
                Text(experiment.author.displayName, color = TextSecondary, fontSize = 11.sp,
                    maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f, fill = false))
            }
            Spacer(Modifier.height(5.dp))
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                experiment.subject?.let { SubjectChip(subject = it) }
                Spacer(Modifier.weight(1f))
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                    Icon(Icons.Default.Star, null, tint = Yellow400, modifier = Modifier.size(11.dp))
                    Text(experiment.averageRating?.let { "%.1f".format(it) } ?: "-", color = TextSecondary, fontSize = 10.sp)
                }
                IconButton(onClick = onFavoriteClick, modifier = Modifier.size(24.dp)) {
                    Icon(
                        if (experiment.isFavoritedByCurrentUser) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        null,
                        tint = if (experiment.isFavoritedByCurrentUser) Red400 else TextSecondary,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun TrendChip(label: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier.clip(RoundedCornerShape(16.dp)).background(DarkSurface2)
            .clickable(onClick = onClick).padding(horizontal = 12.dp, vertical = 7.dp)
    ) { Text(label, color = TextPrimary, fontSize = 12.sp) }
}