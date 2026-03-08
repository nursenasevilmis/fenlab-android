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

    var showFilterSheet by remember { mutableStateOf(false) }
    var showSortSheet   by remember { mutableStateOf(false) }

    var tempSubject     by remember { mutableStateOf(uiState.selectedSubject) }
    var tempEnvironment by remember { mutableStateOf(uiState.selectedEnvironment) }
    var tempDifficulty  by remember { mutableStateOf(uiState.selectedDifficulty) }
    var tempGradeGroup  by remember { mutableStateOf(uiState.selectedGradeGroup) }

    val shouldLoadMore by remember {
        derivedStateOf {
            val last  = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            val total = listState.layoutInfo.totalItemsCount
            last >= total - 3 && total > 0
        }
    }
    LaunchedEffect(shouldLoadMore) { if (shouldLoadMore) viewModel.loadNextPage() }

    val activeFilterCount = listOf(
        uiState.selectedSubject, uiState.selectedEnvironment,
        uiState.selectedDifficulty, uiState.selectedGradeGroup
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
                    top    = padding.calculateTopPadding() + 6.dp,
                    bottom = padding.calculateBottomPadding() + 90.dp,
                    start  = 14.dp, end = 14.dp
                ),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item { WelcomeBanner(fullName = uiState.fullName) }

                if (activeFilterCount > 0) {
                    item { ActiveFilterRow(uiState = uiState, onClear = viewModel::clearFilters) }
                }

                item {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.width(2.dp).height(14.dp).background(Teal400, RoundedCornerShape(2.dp)))
                        Spacer(Modifier.width(6.dp))
                        Text("Tüm Deneyler", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                    }
                }

                items(items = uiState.experiments, key = { it.id }) { exp ->
                    ExperimentCard(
                        experiment      = exp,
                        onCardClick     = { onExperimentClick(exp.id) },
                        onFavoriteClick = { viewModel.toggleFavorite(exp) }
                    )
                }

                if (uiState.isLoadingMore) {
                    item {
                        Box(Modifier.fillMaxWidth().padding(12.dp), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Teal400, strokeWidth = 2.dp)
                        }
                    }
                }
            }
        }
    }

    if (showFilterSheet) {
        ModalBottomSheet(
            onDismissRequest = { showFilterSheet = false },
            containerColor   = DarkSurface,
            dragHandle = {
                Box(Modifier.fillMaxWidth().padding(top = 10.dp, bottom = 2.dp), Alignment.Center) {
                    Box(Modifier.size(width = 36.dp, height = 3.dp).background(DarkSurface3, RoundedCornerShape(2.dp)))
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
                    tempSubject = null; tempEnvironment = null; tempDifficulty = null; tempGradeGroup = null
                    viewModel.applyFilters(null, null, null, null)
                    showFilterSheet = false
                },
                onDismiss = { showFilterSheet = false }
            )
        }
    }

    if (showSortSheet) {
        ModalBottomSheet(
            onDismissRequest = { showSortSheet = false },
            containerColor   = DarkSurface,
            dragHandle = {
                Box(Modifier.fillMaxWidth().padding(top = 10.dp, bottom = 2.dp), Alignment.Center) {
                    Box(Modifier.size(width = 36.dp, height = 3.dp).background(DarkSurface3, RoundedCornerShape(2.dp)))
                }
            }
        ) {
            SortSheetContent(
                currentSort = uiState.sortType,
                onSelect    = { sort -> viewModel.applySort(sort); showSortSheet = false },
                onDismiss   = { showSortSheet = false }
            )
        }
    }
}

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
                    modifier = Modifier.size(26.dp)
                        .background(Brush.linearGradient(listOf(Teal400, Teal500)), androidx.compose.foundation.shape.CircleShape),
                    contentAlignment = Alignment.Center
                ) { Text("⚗", fontSize = 13.sp) }
                Spacer(Modifier.width(7.dp))
                Text("Fen", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = TextPrimary)
                Text("lab", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Teal400)
            }
        },
        actions = {
            BadgedBox(badge = {
                if (activeFilterCount > 0) {
                    Badge(containerColor = Orange400) {
                        Text("$activeFilterCount", fontSize = 8.sp, color = DarkBg)
                    }
                }
            }) {
                OutlinedButton(
                    onClick = onFilterClick,
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = if (activeFilterCount > 0) Teal400 else TextSecondary,
                        containerColor = if (activeFilterCount > 0) Teal400.copy(alpha = 0.08f) else Color.Transparent
                    ),
                    border = androidx.compose.foundation.BorderStroke(1.dp, if (activeFilterCount > 0) Teal400 else DarkSurface3),
                    contentPadding = PaddingValues(horizontal = 9.dp, vertical = 0.dp),
                    modifier = Modifier.height(30.dp)
                ) {
                    Icon(Icons.Default.FilterList, null, modifier = Modifier.size(12.dp))
                    Spacer(Modifier.width(3.dp))
                    Text(if (activeFilterCount > 0) "Filtre ($activeFilterCount)" else "Filtre", fontSize = 11.sp)
                }
            }
            Spacer(Modifier.width(5.dp))
            OutlinedButton(
                onClick = onSortClick,
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = TextSecondary),
                border = androidx.compose.foundation.BorderStroke(1.dp, DarkSurface3),
                contentPadding = PaddingValues(horizontal = 9.dp, vertical = 0.dp),
                modifier = Modifier.height(30.dp)
            ) {
                Icon(Icons.AutoMirrored.Filled.Sort, null, modifier = Modifier.size(12.dp))
                Spacer(Modifier.width(3.dp))
                Text("Sırala", fontSize = 11.sp)
            }
            Spacer(Modifier.width(10.dp))
        }
    )
}

@Composable
fun WelcomeBanner(fullName: String) {
    Box(
        modifier = Modifier.fillMaxWidth()
            .background(Brush.horizontalGradient(listOf(Color(0xFF0D2D28), Color(0xFF0A1A2E))), RoundedCornerShape(12.dp))
            .padding(horizontal = 14.dp, vertical = 11.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
            Column {
                Text(
                    "Merhaba${if (fullName.isNotBlank()) ", ${fullName.split(" ").first()}" else ""}! 👋",
                    color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.height(2.dp))
                Text("Bugün ne keşfedeceksin?", color = TextSecondary, fontSize = 11.sp)
            }
            Text("🔬", fontSize = 20.sp)
        }
    }
}

@Composable
private fun ActiveFilterRow(uiState: HomeUiState, onClear: () -> Unit) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp), contentPadding = PaddingValues(vertical = 1.dp)) {
        uiState.selectedGradeGroup?.let { item { ActiveChip(label = it.toDisplayString(), onRemove = onClear) } }
        uiState.selectedSubject?.let { item { ActiveChip(label = it.toDisplayString(), onRemove = onClear) } }
        uiState.selectedDifficulty?.let { item { ActiveChip(label = it.toDisplayString(), onRemove = onClear) } }
        uiState.selectedEnvironment?.let { item { ActiveChip(label = it.toDisplayString(), onRemove = onClear) } }
    }
}

@Composable
private fun ActiveChip(label: String, onRemove: () -> Unit) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(Teal400.copy(alpha = 0.12f))
            .border(1.dp, Teal400.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        Text(label, color = Teal400, fontSize = 11.sp)
        Icon(Icons.Default.Close, null, tint = Teal400, modifier = Modifier.size(10.dp).clickable(onClick = onRemove))
    }
}

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
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp).padding(bottom = 28.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Filtrele", color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Default.Close, null, tint = TextSecondary, modifier = Modifier.size(16.dp))
            }
        }
        Spacer(Modifier.height(12.dp))
        FilterSection("SINIF DÜZEYİ") {
            FilterChipRow(items = GradeGroup.entries, selected = selectedGradeGroup, label = { it.toDisplayString() }, onClick = onGradeGroupChange)
        }
        Spacer(Modifier.height(12.dp))
        FilterSection("DERS") {
            FilterChipRow(items = SubjectType.entries, selected = selectedSubject, label = { it.toDisplayString() }, onClick = onSubjectChange)
        }
        Spacer(Modifier.height(12.dp))
        FilterSection("SEVİYE") {
            FilterChipRow(items = DifficultyLevel.entries, selected = selectedDifficulty, label = { it.toDisplayString() }, onClick = onDifficultyChange)
        }
        Spacer(Modifier.height(12.dp))
        FilterSection("MEKAN") {
            FilterChipRow(items = EnvironmentType.entries, selected = selectedEnvironment, label = { it.toDisplayString() }, onClick = onEnvironmentChange)
        }
        Spacer(Modifier.height(18.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(
                onClick = onReset, modifier = Modifier.weight(1f).height(40.dp),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = TextSecondary),
                border = androidx.compose.foundation.BorderStroke(1.dp, DarkSurface3)
            ) { Text("Sıfırla", fontSize = 12.sp) }
            Button(
                onClick = onApply, modifier = Modifier.weight(1f).height(40.dp),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Teal400)
            ) { Text("Uygula", color = DarkBg, fontWeight = FontWeight.SemiBold, fontSize = 12.sp) }
        }
    }
}

@Composable
private fun FilterSection(title: String, content: @Composable () -> Unit) {
    Text(title, color = TextSecondary, fontSize = 10.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 0.8.sp)
    Spacer(Modifier.height(8.dp))
    content()
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun <T> FilterChipRow(items: List<T>, selected: T?, label: (T) -> String, onClick: (T) -> Unit) {
    androidx.compose.foundation.layout.FlowRow(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalArrangement   = Arrangement.spacedBy(6.dp)
    ) {
        items.forEach { item ->
            val isSelected = selected == item
            val bgColor by animateColorAsState(if (isSelected) Teal400.copy(alpha = 0.12f) else DarkSurface2, label = "bg")
            val borderColor by animateColorAsState(if (isSelected) Teal400 else DarkSurface3, label = "border")
            val textColor by animateColorAsState(if (isSelected) Teal400 else TextSecondary, label = "text")
            Box(
                modifier = Modifier.clip(RoundedCornerShape(16.dp)).background(bgColor)
                    .border(1.dp, borderColor, RoundedCornerShape(16.dp))
                    .clickable { onClick(item) }.padding(horizontal = 11.dp, vertical = 6.dp)
            ) { Text(label(item), color = textColor, fontSize = 12.sp) }
        }
    }
}

private data class SortOption(val type: SortType, val label: String, val emoji: String)
private val sortOptions = listOf(
    SortOption(SortType.MOST_RECENT,    "En Yeni",     "🕐"),
    SortOption(SortType.MOST_FAVORITED, "En Popüler",  "🔥"),
    SortOption(SortType.HIGHEST_RATED,  "En Beğenilen","❤️"),
    SortOption(SortType.OLDEST,         "En Eski",     "📅")
)

@Composable
private fun SortSheetContent(currentSort: SortType, onSelect: (SortType) -> Unit, onDismiss: () -> Unit) {
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp).padding(bottom = 32.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Sırala", color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Default.Close, null, tint = TextSecondary, modifier = Modifier.size(16.dp))
            }
        }
        Spacer(Modifier.height(6.dp))
        sortOptions.forEach { option ->
            val isSelected = currentSort == option.type
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (isSelected) Teal400.copy(alpha = 0.08f) else Color.Transparent)
                    .border(1.dp, if (isSelected) Teal400.copy(0.4f) else Color.Transparent, RoundedCornerShape(10.dp))
                    .clickable { onSelect(option.type) }
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(option.emoji, fontSize = 16.sp)
                Text(option.label, color = if (isSelected) Teal400 else TextPrimary, fontSize = 13.sp,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal, modifier = Modifier.weight(1f))
                if (isSelected) Icon(Icons.Default.Check, null, tint = Teal400, modifier = Modifier.size(15.dp))
            }
        }
    }
}