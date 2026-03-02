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
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
//import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nursena.fenlab_android.domain.model.Experiment
import com.nursena.fenlab_android.ui.components.EmptyState
import com.nursena.fenlab_android.ui.components.ErrorMessage
import com.nursena.fenlab_android.ui.components.ExperimentCard
import com.nursena.fenlab_android.ui.components.LoadingIndicator
import com.nursena.fenlab_android.ui.preview.MockData

val FenlabTeal     = Color(0xFF00897B)
val FenlabTealDark = Color(0xFF00695C)

// ─────────────────────────────────────────────────────────────────────────────
// Entry point — ViewModel'lı gerçek ekran
// ─────────────────────────────────────────────────────────────────────────────
@Composable
/*fun HomeScreen(
    onExperimentClick: (Long) -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) */
fun HomeScreen(
    onExperimentClick: (Long) -> Unit,
    viewModel: HomeViewModel  // default yok artık
)
{
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()

    val shouldLoadMore by remember {
        derivedStateOf {
            val last = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            last >= listState.layoutInfo.totalItemsCount - 3
        }
    }
    LaunchedEffect(shouldLoadMore) {
        if (shouldLoadMore) viewModel.loadNextPage()
    }

    HomeScreenContent(
        experiments     = uiState.experiments,
        isLoading       = uiState.isLoading,
        isLoadingMore   = uiState.isLoadingMore,
        error           = uiState.error,
        listState       = listState,
        onExperimentClick = onExperimentClick,
        onFavoriteClick = { viewModel.toggleFavorite(it) },
        onFilterClick   = { viewModel.showFilterSheet() },
        onSortClick     = { viewModel.showSortSheet() },
        onRetry         = { viewModel.loadExperiments() }
    )
}

// ─────────────────────────────────────────────────────────────────────────────
// Stateless içerik — hem gerçek hem mock preview'da kullanılır
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun HomeScreenContent(
    experiments: List<Experiment>,
    isLoading: Boolean,
    isLoadingMore: Boolean,
    error: String?,
    listState: androidx.compose.foundation.lazy.LazyListState = rememberLazyListState(),
    onExperimentClick: (Long) -> Unit = {},
    onFavoriteClick: (Experiment) -> Unit = {},
    onFilterClick: () -> Unit = {},
    onSortClick: () -> Unit = {},
    onRetry: () -> Unit = {}
) {
    Scaffold(
        containerColor = Color(0xFFF5F5F5),
        topBar = {
            FenlabTopBar(
                onFilterClick = onFilterClick,
                onSortClick   = onSortClick
            )
        }
    ) { padding ->
        when {
            isLoading && experiments.isEmpty() -> LoadingIndicator()

            error != null && experiments.isEmpty() -> ErrorMessage(
                message = error,
                onRetry = onRetry
            )

            experiments.isEmpty() -> EmptyState(
                emoji    = "🔬",
                title    = "Henüz deney yok",
                subtitle = "İlk deneyi eklemek için + butonuna tıkla"
            )

            else -> {
                LazyColumn(
                    state          = listState,
                    contentPadding = PaddingValues(
                        top    = padding.calculateTopPadding() + 8.dp,
                        bottom = padding.calculateBottomPadding() + 80.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(0.dp)
                ) {
                    item {
                        WelcomeBanner(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                    }

                    item {
                        Text(
                            text       = "Tüm Deneyler",
                            fontSize   = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color      = Color(0xFF1A1A1A),
                            modifier   = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                        )
                    }

                    items(items = experiments, key = { it.id }) { experiment ->
                        ExperimentCard(
                            experiment      = experiment,
                            onCardClick     = { onExperimentClick(experiment.id) },
                            onFavoriteClick = { onFavoriteClick(experiment) },
                            modifier        = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                        )
                    }

                    if (isLoadingMore) {
                        item {
                            Box(
                                modifier         = Modifier.fillMaxWidth().padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(
                                    modifier    = Modifier.size(24.dp),
                                    color       = FenlabTeal,
                                    strokeWidth = 2.dp
                                )
                            }
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
        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier         = Modifier
                        .size(28.dp)
                        .background(FenlabTeal, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text("⚗", fontSize = 14.sp)
                }
                Spacer(Modifier.width(8.dp))
                Text("Fen", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = Color(0xFF1A1A1A))
                Text("lab", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = FenlabTeal)
            }
        },
        actions = {
            OutlinedButton(
                onClick        = onFilterClick,
                shape          = RoundedCornerShape(20.dp),
                colors         = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF444444)),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                modifier       = Modifier.height(32.dp)
            ) {
                Icon(Icons.Default.FilterList, contentDescription = null, modifier = Modifier.size(14.dp))
                Spacer(Modifier.width(4.dp))
                Text("Filtrele", fontSize = 12.sp)
            }
            Spacer(Modifier.width(6.dp))
            OutlinedButton(
                onClick        = onSortClick,
                shape          = RoundedCornerShape(20.dp),
                colors         = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF444444)),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                modifier       = Modifier.height(32.dp)
            ) {
                Icon(Icons.Default.Sort, contentDescription = null, modifier = Modifier.size(14.dp))
                Spacer(Modifier.width(4.dp))
                Text("Sırala", fontSize = 12.sp)
            }
            Spacer(Modifier.width(8.dp))
        }
    )
}

// ─────────────────────────────────────────────────────────────────────────────
// WelcomeBanner
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun WelcomeBanner(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(90.dp)
            .background(
                Brush.horizontalGradient(listOf(FenlabTeal, FenlabTealDark)),
                RoundedCornerShape(16.dp)
            )
    ) {
        Row(
            modifier              = Modifier.fillMaxSize().padding(horizontal = 20.dp),
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text       = "Merhaba! 👋",
                    color      = Color.White,
                    fontSize   = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text     = "Bugün ne keşfedeceksin?",
                    color    = Color.White.copy(alpha = 0.85f),
                    fontSize = 13.sp
                )
            }
            Box(
                modifier         = Modifier
                    .size(56.dp)
                    .background(Color.White.copy(alpha = 0.15f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text("🔬", fontSize = 28.sp)
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// PREVIEW  — Backend'e gerek yok, mock data kullanır
// ─────────────────────────────────────────────────────────────────────────────
@Preview(showBackground = true, showSystemUi = true, name = "Home - Mock Data")
@Composable
fun HomeScreenPreview() {
    MaterialTheme {
        HomeScreenContent(
            experiments   = MockData.mockExperiments,
            isLoading     = false,
            isLoadingMore = false,
            error         = null
        )
    }
}

@Preview(showBackground = true, name = "Home - Loading")
@Composable
fun HomeScreenLoadingPreview() {
    MaterialTheme {
        HomeScreenContent(
            experiments   = emptyList(),
            isLoading     = true,
            isLoadingMore = false,
            error         = null
        )
    }
}

@Preview(showBackground = true, name = "Home - Empty")
@Composable
fun HomeScreenEmptyPreview() {
    MaterialTheme {
        HomeScreenContent(
            experiments   = emptyList(),
            isLoading     = false,
            isLoadingMore = false,
            error         = null
        )
    }
}