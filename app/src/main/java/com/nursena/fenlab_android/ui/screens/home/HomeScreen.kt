package com.nursena.fenlab_android.ui.screens.home

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nursena.fenlab_android.domain.model.enums.GradeGroup
import com.nursena.fenlab_android.domain.model.enums.DifficultyLevel
import com.nursena.fenlab_android.domain.model.enums.EnvironmentType
import com.nursena.fenlab_android.domain.model.enums.SortType
import com.nursena.fenlab_android.domain.model.enums.SubjectType
import com.nursena.fenlab_android.ui.components.EmptyState
import com.nursena.fenlab_android.ui.components.ErrorMessage
import com.nursena.fenlab_android.ui.components.ExperimentCard
import com.nursena.fenlab_android.ui.components.LoadingIndicator
import com.nursena.fenlab_android.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onExperimentClick: (Long) -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState   by viewModel.uiState.collectAsStateWithLifecycle()
    val listState  = rememberLazyListState()
    val scope      = rememberCoroutineScope()

    // Sheet durumları
    var showFilterSheet by remember { mutableStateOf(false) }
    var showSortSheet   by remember { mutableStateOf(false) }

    // Geçici filtre seçimleri (sheet içinde)
    var tempSubject     by remember { mutableStateOf(uiState.selectedSubject) }
    var tempEnvironment by remember { mutableStateOf(uiState.selectedEnvironment) }
    var tempDifficulty  by remember { mutableStateOf(uiState.selectedDifficulty) }
    var tempGradeGroup  by remember { mutableStateOf(uiState.selectedGradeGroup) }

    // Sona yaklaşınca yükle
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

    // Aktif filtre sayısı (badge)
    val activeFilterCount = listOf(
        uiState.selectedSubject,
        uiState.selectedEnvironment,
        uiState.selectedDifficulty,
        uiState.selectedGradeGroup
    ).count { it != null }

    Scaffold(
        containerColor = DarkBg,
        topBar = {
            FenlabTopBar(
                activeFilterCount = activeFilterCount,
                currentSort       = uiState.sortType,
                onFilterClick     = {
                    tempSubject     = uiState.selectedSubject
                    tempEnvironment = uiState.selectedEnvironment
                    tempDifficulty  = uiState.selectedDifficulty
                    tempGradeGroup  = uiState.selectedGradeGroup
                    showFilterSheet = true
                },
                onSortClick = { showSortSheet = true }
            )
        }
    ) { padding ->

        // ── Ana içerik ────────────────────────────────────────────────────────
        when {
            uiState.isLoading && uiState.experiments.isEmpty() -> LoadingIndicator()

            uiState.error != null && uiState.experiments.isEmpty() -> ErrorMessage(
                message = uiState.error!!,
                onRetry = { viewModel.loadExperiments() }
            )

            uiState.experiments.isEmpty() -> EmptyState(
                emoji    = "🔬",
                title    = "Henüz deney yok",
                subtitle = "Filtreleri temizlemeyi dene"
            )

            else -> LazyColumn(
                state          = listState,
                contentPadding = PaddingValues(
                    top    = padding.calculateTopPadding() + 8.dp,
                    bottom = padding.calculateBottomPadding() + 96.dp,
                    start  = 16.dp, end = 16.dp
                ),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Hoş geldin banner
                item { WelcomeBanner(fullName = uiState.fullName) }

                // Aktif filtre chip'leri
                if (activeFilterCount > 0) {
                    item { ActiveFilterRow(uiState = uiState, onClear = viewModel::clearFilters) }
                }

                // Bölüm başlığı
                item {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            Modifier.width(3.dp).height(18.dp)
                                .background(Teal400, RoundedCornerShape(2.dp))
                        )
                        Spacer(Modifier.width(8.dp))
                        Text("Tüm Deneyler", fontSize = 18.sp,
                            fontWeight = FontWeight.Bold, color = TextPrimary)
                    }
                }

                // Kartlar
                items(items = uiState.experiments, key = { it.id }) { exp ->
                    ExperimentCard(
                        experiment      = exp,
                        onCardClick     = { onExperimentClick(exp.id) },
                        onFavoriteClick = { viewModel.toggleFavorite(exp) }
                    )
                }

                // Daha fazla yükleniyor
                if (uiState.isLoadingMore) {
                    item {
                        Box(Modifier.fillMaxWidth().padding(16.dp),
                            contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = Teal400, strokeWidth = 2.dp
                            )
                        }
                    }
                }
            }
        }
    }

    // ── Filtre Bottom Sheet ───────────────────────────────────────────────────
    if (showFilterSheet) {
        ModalBottomSheet(
            onDismissRequest    = { showFilterSheet = false },
            containerColor      = DarkSurface,
            dragHandle          = {
                Box(Modifier.fillMaxWidth().padding(top = 12.dp, bottom = 4.dp),
                    contentAlignment = Alignment.Center) {
                    Box(Modifier.size(width = 40.dp, height = 4.dp)
                        .background(DarkSurface3, RoundedCornerShape(2.dp)))
                }
            }
        ) {
            FilterSheetContent(
                selectedSubject     = tempSubject,
                selectedEnvironment = tempEnvironment,
                selectedDifficulty  = tempDifficulty,
                selectedGradeGroup  = tempGradeGroup,
                onSubjectChange     = { tempSubject = if (tempSubject == it) null else it },
                onEnvironmentChange = { tempEnvironment = if (tempEnvironment == it) null else it },
                onDifficultyChange  = { tempDifficulty = if (tempDifficulty == it) null else it },
                onGradeGroupChange  = { tempGradeGroup = if (tempGradeGroup == it) null else it },
                onApply = {
                    viewModel.applyFilters(tempSubject, tempEnvironment, tempDifficulty, tempGradeGroup)
                    showFilterSheet = false
                },
                onReset = {
                    tempSubject     = null
                    tempEnvironment = null
                    tempDifficulty  = null
                    tempGradeGroup  = null
                    viewModel.applyFilters(null, null, null, null)
                    showFilterSheet = false
                },
                onDismiss = { showFilterSheet = false }
            )
        }
    }

    // ── Sıralama Bottom Sheet ─────────────────────────────────────────────────
    if (showSortSheet) {
        ModalBottomSheet(
            onDismissRequest = { showSortSheet = false },
            containerColor   = DarkSurface,
            dragHandle = {
                Box(Modifier.fillMaxWidth().padding(top = 12.dp, bottom = 4.dp),
                    contentAlignment = Alignment.Center) {
                    Box(Modifier.size(width = 40.dp, height = 4.dp)
                        .background(DarkSurface3, RoundedCornerShape(2.dp)))
                }
            }
        ) {
            SortSheetContent(
                currentSort = uiState.sortType,
                onSelect = { sort ->
                    viewModel.applySort(sort)
                    showSortSheet = false
                },
                onDismiss = { showSortSheet = false }
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// TopBar
// ─────────────────────────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FenlabTopBar(
    activeFilterCount: Int,
    currentSort: SortType,
    onFilterClick: () -> Unit,
    onSortClick: () -> Unit
) {
    TopAppBar(
        colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkBg),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(30.dp)
                        .background(Brush.linearGradient(listOf(Teal400, Teal500)),
                            androidx.compose.foundation.shape.CircleShape),
                    contentAlignment = Alignment.Center
                ) { Text("⚗", fontSize = 15.sp) }
                Spacer(Modifier.width(8.dp))
                Text("Fen", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = TextPrimary)
                Text("lab", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = Teal400)
            }
        },
        actions = {
            // Filtrele butonu — aktif filtre varsa badge göster
            BadgedBox(
                badge = {
                    if (activeFilterCount > 0) {
                        Badge(containerColor = Orange400) {
                            Text("$activeFilterCount", fontSize = 9.sp, color = DarkBg)
                        }
                    }
                }
            ) {
                OutlinedButton(
                    onClick  = onFilterClick,
                    shape    = RoundedCornerShape(20.dp),
                    colors   = ButtonDefaults.outlinedButtonColors(
                        contentColor = if (activeFilterCount > 0) Teal400 else TextSecondary,
                        containerColor = if (activeFilterCount > 0) Teal400.copy(alpha = 0.1f) else Color.Transparent
                    ),
                    border   = androidx.compose.foundation.BorderStroke(
                        1.dp, if (activeFilterCount > 0) Teal400 else DarkSurface3
                    ),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp),
                    modifier = Modifier.height(32.dp)
                ) {
                    Icon(Icons.Default.FilterList, null, modifier = Modifier.size(13.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(if (activeFilterCount > 0) "Filtrele ($activeFilterCount)" else "Filtrele", fontSize = 12.sp)
                }
            }

            Spacer(Modifier.width(6.dp))

            OutlinedButton(
                onClick  = onSortClick,
                shape    = RoundedCornerShape(20.dp),
                colors   = ButtonDefaults.outlinedButtonColors(contentColor = TextSecondary),
                border   = androidx.compose.foundation.BorderStroke(1.dp, DarkSurface3),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp),
                modifier = Modifier.height(32.dp)
            ) {
                Icon(Icons.AutoMirrored.Filled.Sort, null, modifier = Modifier.size(13.dp))
                Spacer(Modifier.width(4.dp))
                Text("Sırala", fontSize = 12.sp)
            }
            Spacer(Modifier.width(12.dp))
        }
    )
}

// ─────────────────────────────────────────────────────────────────────────────
// Welcome Banner
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun WelcomeBanner(fullName: String) {
    Box(
        modifier = Modifier.fillMaxWidth()
            .background(
                Brush.horizontalGradient(listOf(Color(0xFF0D2D28), Color(0xFF0A1A2E))),
                RoundedCornerShape(16.dp)
            )
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        Row(
            modifier              = Modifier.fillMaxWidth(),
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text       = "Merhaba${if (fullName.isNotBlank()) ", ${fullName.split(" ").first()}" else ""}! 👋",
                    color      = TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.height(4.dp))
                Text("Bugün ne keşfedeceksin?", color = TextSecondary, fontSize = 12.sp)
            }
            Text("🔬", fontSize = 34.sp)
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Aktif filtre satırı
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun ActiveFilterRow(uiState: HomeUiState, onClear: () -> Unit) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding        = PaddingValues(vertical = 2.dp)
    ) {
        uiState.selectedGradeGroup?.let {
            item { ActiveChip(label = it.toDisplayString(), onRemove = onClear) }
        }
        uiState.selectedSubject?.let {
            item { ActiveChip(label = it.toDisplayString(), onRemove = onClear) }
        }
        uiState.selectedDifficulty?.let {
            item { ActiveChip(label = it.toDisplayString(), onRemove = onClear) }
        }
        uiState.selectedEnvironment?.let {
            item { ActiveChip(label = it.toDisplayString(), onRemove = onClear) }
        }
    }
}

@Composable
private fun ActiveChip(label: String, onRemove: () -> Unit) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(Teal400.copy(alpha = 0.15f))
            .border(1.dp, Teal400.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
            .padding(horizontal = 10.dp, vertical = 5.dp),
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(label, color = Teal400, fontSize = 12.sp)
        Icon(Icons.Default.Close, null, tint = Teal400, modifier = Modifier.size(12.dp)
            .clickable(onClick = onRemove))
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Filtre Sheet içeriği
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun FilterSheetContent(
    selectedSubject: SubjectType?,
    selectedEnvironment: EnvironmentType?,
    selectedDifficulty: DifficultyLevel?,
    selectedGradeGroup: GradeGroup?,
    onSubjectChange: (SubjectType) -> Unit,
    onEnvironmentChange: (EnvironmentType) -> Unit,
    onDifficultyChange: (DifficultyLevel) -> Unit,
    onGradeGroupChange: (GradeGroup) -> Unit,
    onApply: () -> Unit,
    onReset: () -> Unit,
    onDismiss: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .padding(bottom = 32.dp)
    ) {
        // Başlık
        Row(
            modifier              = Modifier.fillMaxWidth(),
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Filtrele", color = TextPrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            IconButton(onClick = onDismiss) {
                Icon(Icons.Default.Close, null, tint = TextSecondary)
            }
        }

        Spacer(Modifier.height(16.dp))

        // Sınıf Düzeyi
        FilterSection(title = "SINIF DÜZEYİ") {
            FilterChipRow(
                items    = GradeGroup.entries,
                selected = selectedGradeGroup,
                label    = { it.toDisplayString() },
                onClick  = onGradeGroupChange
            )
        }

        Spacer(Modifier.height(16.dp))

        // Ders
        FilterSection(title = "DERS") {
            FilterChipRow(
                items    = SubjectType.entries,
                selected = selectedSubject,
                label    = { it.toDisplayString() },
                onClick  = onSubjectChange
            )
        }

        Spacer(Modifier.height(16.dp))

        // Seviye
        FilterSection(title = "SEVİYE") {
            FilterChipRow(
                items    = DifficultyLevel.entries,
                selected = selectedDifficulty,
                label    = { it.toDisplayString() },
                onClick  = onDifficultyChange
            )
        }

        Spacer(Modifier.height(16.dp))

        // Mekan
        FilterSection(title = "MEKAN") {
            FilterChipRow(
                items    = EnvironmentType.entries,
                selected = selectedEnvironment,
                label    = { it.toDisplayString() },
                onClick  = onEnvironmentChange
            )
        }

        Spacer(Modifier.height(24.dp))

        // Butonlar
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedButton(
                onClick  = onReset,
                modifier = Modifier.weight(1f).height(46.dp),
                shape    = RoundedCornerShape(12.dp),
                colors   = ButtonDefaults.outlinedButtonColors(contentColor = TextSecondary),
                border   = androidx.compose.foundation.BorderStroke(1.dp, DarkSurface3)
            ) { Text("Sıfırla") }

            Button(
                onClick  = onApply,
                modifier = Modifier.weight(1f).height(46.dp),
                shape    = RoundedCornerShape(12.dp),
                colors   = ButtonDefaults.buttonColors(containerColor = Teal400)
            ) { Text("Uygula", color = DarkBg, fontWeight = FontWeight.SemiBold) }
        }
    }
}

@Composable
private fun FilterSection(title: String, content: @Composable () -> Unit) {
    Text(title, color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.SemiBold,
        letterSpacing = 1.sp)
    Spacer(Modifier.height(10.dp))
    content()
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun <T> FilterChipRow(
    items: List<T>,
    selected: T?,
    label: (T) -> String,
    onClick: (T) -> Unit
) {
    androidx.compose.foundation.layout.FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement   = Arrangement.spacedBy(8.dp)
    ) {
        items.forEach { item ->
            val isSelected = selected == item
            val bgColor by animateColorAsState(
                if (isSelected) Teal400.copy(alpha = 0.15f) else DarkSurface2,
                label = "chip_bg"
            )
            val borderColor by animateColorAsState(
                if (isSelected) Teal400 else DarkSurface3,
                label = "chip_border"
            )
            val textColor by animateColorAsState(
                if (isSelected) Teal400 else TextSecondary,
                label = "chip_text"
            )

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(bgColor)
                    .border(1.dp, borderColor, RoundedCornerShape(20.dp))
                    .clickable { onClick(item) }
                    .padding(horizontal = 14.dp, vertical = 8.dp)
            ) {
                Text(label(item), color = textColor, fontSize = 13.sp)
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Sıralama Sheet içeriği
// ─────────────────────────────────────────────────────────────────────────────
private data class SortOption(
    val type: SortType,
    val label: String,
    val emoji: String
)

private val sortOptions = listOf(
    SortOption(SortType.MOST_RECENT,    "En Yeni",          "🕐"),
    SortOption(SortType.MOST_FAVORITED, "En Popüler",       "🔥"),
    SortOption(SortType.HIGHEST_RATED,  "En Beğenilen",     "❤️"),
    SortOption(SortType.OLDEST,         "En Çok Yorumlanan","💬")
)

@Composable
private fun SortSheetContent(
    currentSort: SortType,
    onSelect: (SortType) -> Unit,
    onDismiss: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .padding(bottom = 36.dp)
    ) {
        Row(
            modifier              = Modifier.fillMaxWidth(),
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Sırala", color = TextPrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            IconButton(onClick = onDismiss) {
                Icon(Icons.Default.Close, null, tint = TextSecondary)
            }
        }

        Spacer(Modifier.height(8.dp))

        sortOptions.forEach { option ->
            val isSelected = currentSort == option.type
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        if (isSelected) Teal400.copy(alpha = 0.1f) else Color.Transparent
                    )
                    .border(
                        1.dp,
                        if (isSelected) Teal400.copy(alpha = 0.5f) else Color.Transparent,
                        RoundedCornerShape(12.dp)
                    )
                    .clickable { onSelect(option.type) }
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(option.emoji, fontSize = 18.sp)
                Text(
                    text       = option.label,
                    color      = if (isSelected) Teal400 else TextPrimary,
                    fontSize   = 15.sp,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                    modifier   = Modifier.weight(1f)
                )
                if (isSelected) {
                    Icon(Icons.Default.Check, null, tint = Teal400, modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}