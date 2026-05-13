package com.nursena.fenlab_android.ui.screens.search

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.nursena.fenlab_android.core.toMinioUrl
import com.nursena.fenlab_android.domain.model.Experiment
import com.nursena.fenlab_android.domain.model.User
import com.nursena.fenlab_android.domain.model.enums.SubjectType
import com.nursena.fenlab_android.ui.components.EmptyState
import com.nursena.fenlab_android.ui.components.ErrorMessage
import com.nursena.fenlab_android.ui.components.LoadingIndicator
import com.nursena.fenlab_android.ui.components.LottieLoadingIndicator
import com.nursena.fenlab_android.ui.theme.*
import com.airbnb.lottie.compose.*
import com.nursena.fenlab_android.ui.components.AnimatedFavoriteButton

@Composable
fun SearchScreen(
    onExperimentClick: (Long) -> Unit,
    onUserClick: (Long) -> Unit = {},
    viewModel: SearchViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Her geçişte o anki kullanıcının aramalarını yükle
    LaunchedEffect(Unit) {
        viewModel.loadRecentSearches()
    }
    val keyboard = LocalSoftwareKeyboardController.current

    Column(modifier = Modifier.fillMaxSize()) {

        // ── Header + Arama kutusu ────────────────────────────────────────────
        Box(modifier = Modifier.fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 16.dp).padding(top = 16.dp, bottom = 14.dp)
        ) {
            Column {
                Text("Keşfet", color = TextPrimary, fontSize = 24.sp, fontWeight = FontWeight.ExtraBold)
                Spacer(Modifier.height(12.dp))
                TextField(
                    value = uiState.query, onValueChange = viewModel::onQueryChange,
                    placeholder = { Text("Deney, konu veya kullanıcı ara...", color = TextSecondary, fontSize = 14.sp) },
                    leadingIcon = { Icon(Icons.Default.Search, null, tint = TextSecondary, modifier = Modifier.size(20.dp)) },
                    trailingIcon = {
                        if (uiState.query.isNotBlank()) {
                            IconButton(onClick = { viewModel.onQueryChange("") }) {
                                Icon(Icons.Default.Close, null, tint = TextSecondary, modifier = Modifier.size(17.dp))
                            }
                        }
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = { keyboard?.hide() }),
                    shape = RoundedCornerShape(14.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor   = Color(0xFFF0F3F1),
                        unfocusedContainerColor = Color(0xFFF0F3F1),
                        focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary,
                        cursorColor = FenGreen, focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        // ── İçerik ──────────────────────────────────────────────────────────
        when {
            uiState.isLoading -> LottieLoadingIndicator()
            uiState.error != null -> ErrorMessage(message = uiState.error!!, onRetry = { viewModel.onQueryChange(uiState.query) })
            uiState.query.isBlank() -> HintContent(
                recentSearches = uiState.recentSearches,
                onRecentClick  = viewModel::onRecentClick,
                onRemoveRecent = viewModel::removeRecent,
                onClearAll     = viewModel::clearRecents,
                onTrendClick   = viewModel::onQueryChange
            )
            uiState.isEmpty -> EmptyState(emoji = "🔍", title = "Sonuç bulunamadı",
                subtitle = "\"${uiState.query}\" için sonuç bulunamadı")
            else -> ResultsContent(
                results           = uiState.results,
                userResults       = uiState.userResults,
                onExperimentClick = onExperimentClick,
                onUserClick       = onUserClick,
                onFavoriteClick   = viewModel::toggleFavorite
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Hint: Son aramalar + trendler
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun HintContent(
    recentSearches: List<String>, onRecentClick: (String) -> Unit,
    onRemoveRecent: (String) -> Unit, onClearAll: () -> Unit,
    onTrendClick: (String) -> Unit
) {
    if (recentSearches.isEmpty()) {
        SearchStartAnimation()
        return
    }

    LazyColumn(contentPadding = PaddingValues(bottom = 100.dp)) {
        item {
            Row(modifier = Modifier.fillMaxWidth()
                .padding(horizontal = 16.dp).padding(top = 16.dp, bottom = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Icon(Icons.Default.History, null, tint = TextSecondary, modifier = Modifier.size(16.dp))
                    Text("Son Aramalar", color = TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                }
                Text("Temizle", color = FenGreen, fontSize = 12.sp,
                    modifier = Modifier.clickable(onClick = onClearAll))
            }
        }
        items(recentSearches) { term ->
            Row(modifier = Modifier.fillMaxWidth().clickable { onRecentClick(term) }
                .padding(horizontal = 16.dp, vertical = 11.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(modifier = Modifier.size(32.dp).background(GlassSurface, CircleShape),
                    contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.History, null, tint = TextSecondary, modifier = Modifier.size(14.dp))
                }
                Text(term, color = TextPrimary, fontSize = 14.sp, modifier = Modifier.weight(1f))
                IconButton(onClick = { onRemoveRecent(term) }, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Default.Close, null, tint = Color(0xFFDDDDDD), modifier = Modifier.size(13.dp))
                }
            }
            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp),
                thickness = 0.5.dp, color = Color(0xFFFFFFFF))
        }
    }
}

@Composable
private fun SearchStartAnimation() {
    val composition by rememberLottieComposition(
        LottieCompositionSpec.Asset("not-data-animation.json")
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(40.dp)
            .offset(y = (-45).dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        LottieAnimation(
            composition = composition,
            iterations = LottieConstants.IterateForever,
            modifier = Modifier.size(145.dp)
        )

        Spacer(Modifier.height(12.dp))

        Text(
            "Aramaya başla",
            color = TextPrimary,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(Modifier.height(6.dp))

        Text(
            "Deney, konu veya kullanıcı ara",
            color = TextSecondary,
            fontSize = 13.sp,
            textAlign = TextAlign.Center
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Sonuçlar — kullanıcılar + deneyler ayrı bölüm
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun ResultsContent(
    results: List<Experiment>, userResults: List<User>,
    onExperimentClick: (Long) -> Unit, onUserClick: (Long) -> Unit,
    onFavoriteClick: (Experiment) -> Unit
) {
    LazyColumn(contentPadding = PaddingValues(bottom = 100.dp)) {

        // ── Kullanıcılar ──────────────────────────────────────────────────
        if (userResults.isNotEmpty()) {
            item {
                SectionLabel("Kullanıcılar", "${userResults.size} sonuç")
            }
            items(items = userResults, key = { "u${it.id}" }) { user ->
                UserRow(user = user, onClick = { onUserClick(user.id) })
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp),
                    thickness = 0.5.dp, color = Color(0xFFFFFFFF))
            }
        }

        // ── Deneyler ──────────────────────────────────────────────────────
        if (results.isNotEmpty()) {
            item {
                SectionLabel("Deneyler", "${results.size} sonuç",
                    topPadding = if (userResults.isNotEmpty()) 16.dp else 8.dp)
            }
            items(items = results, key = { it.id }) { exp ->
                ExperimentRow(
                    experiment      = exp,
                    onCardClick     = { onExperimentClick(exp.id) },
                    onFavoriteClick = { onFavoriteClick(exp) }
                )
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp),
                    thickness = 0.5.dp, color = Color(0xFFFFFFFF))
            }
        }
    }
}

@Composable
private fun SectionLabel(title: String, subtitle: String, topPadding: androidx.compose.ui.unit.Dp = 8.dp) {
    Row(modifier = Modifier.fillMaxWidth()
        .padding(horizontal = 16.dp).padding(top = topPadding, bottom = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically) {
        Text(title, color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
        Text(subtitle, color = TextSecondary, fontSize = 12.sp)
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Kullanıcı satırı
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun UserRow(user: User, onClick: () -> Unit) {
    Row(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)
        .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Box(modifier = Modifier.size(42.dp).clip(CircleShape)
            .background(Brush.linearGradient(listOf(Color(0x80418765), Color(0x66EC407A)))),
            contentAlignment = Alignment.Center) {
            if (user.profileImageUrl != null) {
                AsyncImage(model = user.profileImageUrl?.toMinioUrl(), contentDescription = null,
                    contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize().clip(CircleShape))
            } else {
                Text(user.displayName.take(2).uppercase(), color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            }
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(user.displayName, color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            Text("@${user.username}", color = TextSecondary, fontSize = 11.sp)
        }
        Box(modifier = Modifier
            .background(if (user.isTeacher) Color(0x1FF06292) else Color(0xFFFFFFFF), RoundedCornerShape(10.dp))
            .padding(horizontal = 8.dp, vertical = 3.dp)) {
            Text(user.displayRole, color = if (user.isTeacher) FenGreen else TextSecondary, fontSize = 10.sp)
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Deney satırı
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun ExperimentRow(
    experiment: Experiment, onCardClick: () -> Unit, onFavoriteClick: () -> Unit
) {
    Row(modifier = Modifier.fillMaxWidth().clickable(onClick = onCardClick)
        .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)) {

        Box(modifier = Modifier.size(width = 88.dp, height = 70.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(Brush.linearGradient(listOf(Color(0xFFFFFFFF), Color(0xFFFFFFFF))))) {
            AsyncImage(model = experiment.thumbnailUrl ?: experiment.videoUrl,
                contentDescription = null, contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize())
            if (experiment.videoUrl != null) {
                Box(modifier = Modifier.fillMaxSize().background(Color(0x33000000)),
                    contentAlignment = Alignment.Center) {
                    Box(modifier = Modifier.size(26.dp).background(Color(0x33FFFFFF), CircleShape),
                        contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.PlayArrow, null, tint = Color.White, modifier = Modifier.size(14.dp))
                    }
                }
            }
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(experiment.title, color = TextPrimary, fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold, maxLines = 2,
                overflow = TextOverflow.Ellipsis, lineHeight = 18.sp)
            Spacer(Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                Box(modifier = Modifier.size(15.dp).clip(CircleShape).background(FenGreenDark),
                    contentAlignment = Alignment.Center) {
                    if (experiment.author.profileImageUrl != null) {
                        AsyncImage(
                            model = experiment.author.profileImageUrl.toMinioUrl(),
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize().clip(CircleShape)
                        )
                    } else {
                        Text(experiment.author.displayName.take(1).uppercase(),
                            color = Color.White, fontSize = 7.sp, fontWeight = FontWeight.Bold)
                    }
                }
                Text(experiment.author.displayName, color = TextSecondary, fontSize = 11.sp,
                    maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f, fill = false))
            }
            Spacer(Modifier.height(5.dp))
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                experiment.averageRating?.let {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                        Icon(Icons.Default.Star, null, tint = Orange400, modifier = Modifier.size(11.dp))
                        Text("%.1f".format(it), color = TextSecondary, fontSize = 11.sp)
                    }
                }
                experiment.subject?.let { subject ->
                    Box(modifier = Modifier.background(Color(0x1A0D7D7C), RoundedCornerShape(6.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)) {
                        Text(subject.toDisplayString(), color = FenGreen, fontSize = 10.sp)
                    }
                }
            }
        }

        AnimatedFavoriteButton(
            isFavorited = experiment.isFavoritedByCurrentUser,
            backgroundColor = Color.Transparent,
            iconWhenNotFavorited = TextSecondary,
            modifier = Modifier.size(34.dp),
            onClick = onFavoriteClick
        )




    }

}