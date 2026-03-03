package com.nursena.fenlab_android.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nursena.fenlab_android.ui.components.EmptyState
import com.nursena.fenlab_android.ui.components.ErrorMessage
import com.nursena.fenlab_android.ui.components.ExperimentCard
import com.nursena.fenlab_android.ui.components.LoadingIndicator
import com.nursena.fenlab_android.ui.theme.*

// ─────────────────────────────────────────────────────────────────────────────
// HomeScreen  — gerçek ViewModel, backend bağlı
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun HomeScreen(
    onExperimentClick: (Long) -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState   by viewModel.uiState.collectAsStateWithLifecycle()
    val listState  = rememberLazyListState()

    // Sona yaklaşınca sonraki sayfayı yükle
    val shouldLoadMore by remember {
        derivedStateOf {
            val last  = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            val total = listState.layoutInfo.totalItemsCount
            last >= total - 3 && total > 0
        }
    }
    LaunchedEffect(shouldLoadMore) {
        if (shouldLoadMore) viewModel.loadNextPage()
    }

    Scaffold(
        containerColor = DarkBg,
        topBar         = { FenlabTopBar(
            onFilterClick = { viewModel.showFilterSheet() },
            onSortClick   = { viewModel.showSortSheet() }
        )}
    ) { padding ->

        when {
            uiState.isLoading && uiState.experiments.isEmpty() -> LoadingIndicator()

            uiState.error != null && uiState.experiments.isEmpty() -> ErrorMessage(
                message = uiState.error!!,
                onRetry = { viewModel.loadExperiments() }
            )

            uiState.experiments.isEmpty() -> EmptyState(
                emoji    = "🔬",
                title    = "Henüz deney yok",
                subtitle = "İlk deneyi eklemek için + butonuna tıkla"
            )

            else -> LazyColumn(
                state          = listState,
                contentPadding = PaddingValues(
                    top    = padding.calculateTopPadding() + 8.dp,
                    bottom = padding.calculateBottomPadding() + 90.dp,
                    start  = 16.dp,
                    end    = 16.dp
                ),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {

                // ── Küçük hoş geldin banner ──────────────────────────────
                item {
                    SmallWelcomeBanner(
                        fullName       = uiState.fullName,
                        experimentCount = uiState.experiments.size
                    )
                }

                // ── Bölüm başlığı ─────────────────────────────────────────
                item {
                    Row(
                        modifier          = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .width(3.dp)
                                .height(18.dp)
                                .background(Teal400, RoundedCornerShape(2.dp))
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text       = "Tüm Deneyler",
                            fontSize   = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color      = TextPrimary
                        )
                    }
                }

                // ── Deney kartları ────────────────────────────────────────
                items(items = uiState.experiments, key = { it.id }) { exp ->
                    ExperimentCard(
                        experiment      = exp,
                        onCardClick     = { onExperimentClick(exp.id) },
                        onFavoriteClick = { viewModel.toggleFavorite(exp) }
                    )
                }

                // ── Sayfa yükleniyor ──────────────────────────────────────
                if (uiState.isLoadingMore) {
                    item {
                        Box(
                            modifier         = Modifier.fillMaxWidth().padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                modifier    = Modifier.size(24.dp),
                                color       = Teal400,
                                strokeWidth = 2.dp
                            )
                        }
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// TopBar
// ─────────────────────────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FenlabTopBar(onFilterClick: () -> Unit, onSortClick: () -> Unit) {
    TopAppBar(
        colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkBg),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier         = Modifier
                        .size(30.dp)
                        .background(
                            Brush.linearGradient(listOf(Teal400, Teal500)),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text("⚗", fontSize = 15.sp)
                }
                Spacer(Modifier.width(8.dp))
                Text("Fen",  fontWeight = FontWeight.Bold, fontSize = 20.sp, color = TextPrimary)
                Text("lab",  fontWeight = FontWeight.Bold, fontSize = 20.sp, color = Teal400)
            }
        },
        actions = {
            // Filtrele
            OutlinedButton(
                onClick  = onFilterClick,
                shape    = RoundedCornerShape(20.dp),
                colors   = ButtonDefaults.outlinedButtonColors(contentColor = TextSecondary),
                border   = androidx.compose.foundation.BorderStroke(1.dp, DarkSurface3),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp),
                modifier = Modifier.height(32.dp)
            ) {
                Icon(Icons.Default.FilterList, contentDescription = null, modifier = Modifier.size(13.dp))
                Spacer(Modifier.width(4.dp))
                Text("Filtrele", fontSize = 12.sp)
            }
            Spacer(Modifier.width(6.dp))
            // Sırala
            OutlinedButton(
                onClick  = onSortClick,
                shape    = RoundedCornerShape(20.dp),
                colors   = ButtonDefaults.outlinedButtonColors(contentColor = TextSecondary),
                border   = androidx.compose.foundation.BorderStroke(1.dp, DarkSurface3),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp),
                modifier = Modifier.height(32.dp)
            ) {
                Icon(Icons.AutoMirrored.Filled.Sort, contentDescription = null, modifier = Modifier.size(13.dp))
                Spacer(Modifier.width(4.dp))
                Text("Sırala", fontSize = 12.sp)
            }
            Spacer(Modifier.width(12.dp))
        }
    )
}

// ─────────────────────────────────────────────────────────────────────────────
// Küçük Hoş Geldin Banner  (ekranın tepesine sığan kompakt versiyon)
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun SmallWelcomeBanner(fullName: String, experimentCount: Int) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.horizontalGradient(listOf(Color(0xFF0D2D28), Color(0xFF0A1A2E))),
                RoundedCornerShape(16.dp)
            )
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        Row(
            modifier          = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text       = "Merhaba${if (fullName.isNotBlank()) ", ${fullName.split(" ").first()}" else ""}! 👋",
                    color      = TextPrimary,
                    fontSize   = 15.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    StatBadge(icon = "🔬", label = "Deneyler", value = experimentCount.toString())
                }
            }
            Text("🔬", fontSize = 32.sp)
        }
    }
}

@Composable
private fun StatBadge(icon: String, label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(icon, fontSize = 12.sp)
        Text(
            text     = "$label $value",
            color    = TextSecondary,
            fontSize = 12.sp
        )
    }
}