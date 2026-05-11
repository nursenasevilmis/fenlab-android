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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
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
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.compose.ui.platform.LocalLifecycleOwner
import com.nursena.fenlab_android.core.base.UiEvent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onExperimentClick: (Long) -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState   by viewModel.uiState.collectAsStateWithLifecycle()
    val listState  = rememberLazyListState()

    var showFilterSheet by remember { mutableStateOf(false) }
    var showSortSheet   by remember { mutableStateOf(false) }

    // Popup bildirimi
    var popupMessage by remember { mutableStateOf<String?>(null) }

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

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) viewModel.refreshSilently()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    LaunchedEffect(shouldLoadMore) {
        if (shouldLoadMore) viewModel.loadNextPage()
    }

    // ViewModel event'lerini dinle (snackbar → popup)
    LaunchedEffect(viewModel) {
        viewModel.eventFlow.collect { event ->
            if (event is UiEvent.ShowSnackbar) popupMessage = event.message
        }
    }

    // Popup dialog
    popupMessage?.let { msg ->
        FenlabPopupDialog(message = msg, onDismiss = { popupMessage = null })
    }

    val activeFilterCount = listOf(
        uiState.selectedSubject,
        uiState.selectedEnvironment,
        uiState.selectedDifficulty,
        uiState.selectedGradeGroup
    ).count { it != null }

    // Sıralama default değil mi?
    val isSortActive = uiState.sortType != SortType.MOST_RECENT

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            FenlabTopBar(
                activeFilterCount = activeFilterCount,
                isSortActive      = isSortActive,
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
                    top    = padding.calculateTopPadding() + 8.dp,
                    bottom = padding.calculateBottomPadding() + 96.dp,
                    start  = 16.dp, end  = 16.dp
                ),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                item { WelcomeBanner(fullName = uiState.fullName) }

                if (activeFilterCount > 0) {
                    item {
                        // ← Her filtre tek tek silinebilir + "Tümünü Temizle" butonu
                        ActiveFilterRow(
                            uiState         = uiState,
                            onRemoveSubject     = { viewModel.applyFilters(null, uiState.selectedEnvironment, uiState.selectedDifficulty, uiState.selectedGradeGroup) },
                            onRemoveEnvironment = { viewModel.applyFilters(uiState.selectedSubject, null, uiState.selectedDifficulty, uiState.selectedGradeGroup) },
                            onRemoveDifficulty  = { viewModel.applyFilters(uiState.selectedSubject, uiState.selectedEnvironment, null, uiState.selectedGradeGroup) },
                            onRemoveGradeGroup  = { viewModel.applyFilters(uiState.selectedSubject, uiState.selectedEnvironment, uiState.selectedDifficulty, null) },
                            onClearAll          = viewModel::clearFilters
                        )
                    }
                }

                item {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.width(3.dp).height(18.dp).background(FenGreen, RoundedCornerShape(2.dp)))
                        Spacer(Modifier.width(8.dp))
                        Text("Tüm Deneyler", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
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
                        Box(Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp), color = FenGreen, strokeWidth = 2.dp)
                        }
                    }
                }
            }
        }
    }

    // ── Filtre Bottom Sheet ───────────────────────────────────────────────────
    if (showFilterSheet) {
        ModalBottomSheet(
            onDismissRequest = { showFilterSheet = false },
            containerColor   = Color(0xFFFFFFFF),
            dragHandle = {
                Box(Modifier.fillMaxWidth().padding(top = 12.dp, bottom = 4.dp), contentAlignment = Alignment.Center) {
                    Box(Modifier.size(width = 40.dp, height = 4.dp).background(Color(0xFFDDDDDD), RoundedCornerShape(2.dp)))
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
                    tempSubject = null; tempEnvironment = null
                    tempDifficulty = null; tempGradeGroup = null
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
            containerColor   = Color(0xFFFFFFFF),
            dragHandle = {
                Box(Modifier.fillMaxWidth().padding(top = 12.dp, bottom = 4.dp), contentAlignment = Alignment.Center) {
                    Box(Modifier.size(width = 40.dp, height = 4.dp).background(Color(0xFFDDDDDD), RoundedCornerShape(2.dp)))
                }
            }
        ) {
            SortSheetContent(
                currentSort = uiState.sortType,
                onSelect = { sort ->
                    viewModel.applySort(sort)
                    showSortSheet = false
                },
                onReset = {
                    viewModel.applySort(SortType.MOST_RECENT)
                    showSortSheet = false
                },
                onDismiss = { showSortSheet = false }
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Popup Bildirim (Snackbar yerine)
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun FenlabPopupDialog(message: String, onDismiss: () -> Unit) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(dismissOnClickOutside = true, dismissOnBackPress = true)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFFFFFFFF))
                .padding(20.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(FenGreenLight),
                    contentAlignment = Alignment.Center
                ) {
                    Text("ℹ️", fontSize = 22.sp)
                }
                Spacer(Modifier.height(12.dp))
                Text(
                    text       = message,
                    color      = TextPrimary,
                    fontSize   = 14.sp,
                    textAlign  = TextAlign.Center,
                    lineHeight = 20.sp
                )
                Spacer(Modifier.height(16.dp))
                Button(
                    onClick  = onDismiss,
                    shape    = RoundedCornerShape(10.dp),
                    colors   = ButtonDefaults.buttonColors(containerColor = FenGreen),
                    modifier = Modifier.fillMaxWidth().height(42.dp)
                ) {
                    Text("Tamam", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// TopBar — sıralama aktifse badge göster
// ─────────────────────────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FenlabTopBar(
    activeFilterCount: Int,
    isSortActive: Boolean,
    onFilterClick: () -> Unit,
    onSortClick: () -> Unit
) {
    TopAppBar(
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = GradientStart,
            scrolledContainerColor = GradientStart
        ),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Spacer(Modifier.width(8.dp))
                Text("Fen", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = FenGreen)
                Text("Lab", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = LabOrangeNew)
            }
        },
        actions = {
            // Filtrele butonu
            BadgedBox(
                modifier = Modifier.padding(end = 8.dp),
                badge = {
                    if (activeFilterCount > 0) {
                        Badge(containerColor = FenGreen) {
                            Text("$activeFilterCount", fontSize = 9.sp, color = Color.White)
                        }
                    }
                }
            ) {
                OutlinedButton(
                    onClick  = onFilterClick,
                    shape    = RoundedCornerShape(20.dp),
                    colors   = ButtonDefaults.outlinedButtonColors(
                        contentColor   = if (activeFilterCount > 0) FenGreen else Color(0xFF444444),
                        containerColor = Color.Transparent
                    ),
                    border   = androidx.compose.foundation.BorderStroke(
                        1.dp, if (activeFilterCount > 0) FenGreen else Color(0xFFCCCCCC)
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

            // Sırala butonu — aktifse badge "1" göster
            BadgedBox(
                modifier = Modifier.padding(end = 12.dp),
                badge = {
                    if (isSortActive) {
                        Badge(containerColor = FenGreen) {
                            Text("1", fontSize = 9.sp, color = Color.White)
                        }
                    }
                }
            ) {
                OutlinedButton(
                    onClick  = onSortClick,
                    shape    = RoundedCornerShape(20.dp),
                    colors   = ButtonDefaults.outlinedButtonColors(
                        contentColor   = if (isSortActive) FenGreen else TextSecondary,
                        containerColor = Color.Transparent
                    ),
                    border   = androidx.compose.foundation.BorderStroke(
                        1.dp, if (isSortActive) FenGreen else Color(0xFFDDDDDD)
                    ),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp),
                    modifier = Modifier.height(32.dp)
                ) {
                    Icon(Icons.AutoMirrored.Filled.Sort, null, modifier = Modifier.size(13.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Sırala", fontSize = 12.sp)
                }
            }
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
            .background(FenGreen, RoundedCornerShape(16.dp))
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        Row(
            modifier              = Modifier.fillMaxWidth(),
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text  = "Merhaba${if (fullName.isNotBlank()) ", ${fullName.split(" ").first()}" else ""}! 👋",
                    color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.height(4.dp))
                val greetingMessages = remember {
                    listOf(
                        "Bugün ne keşfedeceksin? 🔬",
                        "Bilim seni bekliyor! ⚗️",
                        "Yeni bir deney zamanı! 🚀",
                        "Merakını keşfe dönüştür! 💡",
                        "Bugün ne öğreneceksin? 🌟",
                        "Keşfetmeye hazır mısın? 🧬",
                        "Fen bilimleri burada! 🔭",
                        "Bugün hangi soruyu çözeceksin? 🧪"
                    )
                }
                val greetingMsg = remember { greetingMessages.random() }
                Text(greetingMsg, color = Color.White.copy(alpha = 0.85f), fontSize = 12.sp)
            }
            Text("🔬", fontSize = 34.sp)
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Aktif filtre satırı — HER FİLTRE TEK TEK SİLİNEBİLİR + Tümünü Temizle
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun ActiveFilterRow(
    uiState: HomeUiState,
    onRemoveSubject: () -> Unit,
    onRemoveEnvironment: () -> Unit,
    onRemoveDifficulty: () -> Unit,
    onRemoveGradeGroup: () -> Unit,
    onClearAll: () -> Unit
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding        = PaddingValues(vertical = 2.dp)
    ) {
        uiState.selectedGradeGroup?.let {
            item { ActiveChip(label = it.toDisplayString(), onRemove = onRemoveGradeGroup) }
        }
        uiState.selectedSubject?.let {
            item { ActiveChip(label = it.toDisplayString(), onRemove = onRemoveSubject) }
        }
        uiState.selectedDifficulty?.let {
            item { ActiveChip(label = it.toDisplayString(), onRemove = onRemoveDifficulty) }
        }
        uiState.selectedEnvironment?.let {
            item { ActiveChip(label = it.toDisplayString(), onRemove = onRemoveEnvironment) }
        }
        // ← Birden fazla filtre varsa "Tümünü Temizle" butonu göster
        val filterCount = listOf(uiState.selectedGradeGroup, uiState.selectedSubject, uiState.selectedDifficulty, uiState.selectedEnvironment).count { it != null }
        if (filterCount > 1) {
            item {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color(0x1AEF5350))
                        .border(1.dp, Color(0x60EF5350), RoundedCornerShape(20.dp))
                        .clickable(onClick = onClearAll)
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Text("Tümünü Temizle", color = Red400, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                }
            }
        }
    }
}

@Composable
private fun ActiveChip(label: String, onRemove: () -> Unit) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(Color(0x1A0D7D7C))
            .border(1.dp, Color(0x600D7D7C), RoundedCornerShape(20.dp))
            .padding(horizontal = 10.dp, vertical = 5.dp),
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(label, color = FenGreen, fontSize = 12.sp)
        Icon(Icons.Default.Close, null, tint = FenGreen, modifier = Modifier.size(12.dp)
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
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp).padding(bottom = 32.dp)
    ) {
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

        FilterSection(title = "SINIF DÜZEYİ") {
            FilterChipRow(items = GradeGroup.entries, selected = selectedGradeGroup, label = { it.toDisplayString() }, onClick = onGradeGroupChange)
        }
        Spacer(Modifier.height(16.dp))
        FilterSection(title = "DERS") {
            FilterChipRow(items = SubjectType.entries, selected = selectedSubject, label = { it.toDisplayString() }, onClick = onSubjectChange)
        }
        Spacer(Modifier.height(16.dp))
        FilterSection(title = "SEVİYE") {
            FilterChipRow(items = DifficultyLevel.entries, selected = selectedDifficulty, label = { it.toDisplayString() }, onClick = onDifficultyChange)
        }
        Spacer(Modifier.height(16.dp))
        FilterSection(title = "MEKAN") {
            FilterChipRow(items = EnvironmentType.entries, selected = selectedEnvironment, label = { it.toDisplayString() }, onClick = onEnvironmentChange)
        }
        Spacer(Modifier.height(24.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedButton(
                onClick  = onReset,
                modifier = Modifier.weight(1f).height(46.dp),
                shape    = RoundedCornerShape(12.dp),
                colors   = ButtonDefaults.outlinedButtonColors(contentColor = TextSecondary),
                border   = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFDDDDDD))
            ) { Text("Sıfırla") }

            Button(
                onClick  = onApply,
                modifier = Modifier.weight(1f).height(46.dp),
                shape    = RoundedCornerShape(12.dp),
                colors   = ButtonDefaults.buttonColors(containerColor = FenGreen)
            ) { Text("Uygula", color = Color.White, fontWeight = FontWeight.SemiBold) }
        }
    }
}

@Composable
private fun FilterSection(title: String, content: @Composable () -> Unit) {
    Text(title, color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 1.sp)
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
            val bgColor by animateColorAsState(if (isSelected) FenGreenLight else Color(0xFFFFFFFF), label = "chip_bg")
            val borderColor by animateColorAsState(if (isSelected) FenGreen else Color(0xFFCCCCCC), label = "chip_border")
            val textColor by animateColorAsState(if (isSelected) FenGreen else TextSecondary, label = "chip_text")

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
// Sıralama Sheet — Sıfırla butonu eklendi
// ─────────────────────────────────────────────────────────────────────────────
private data class SortOption(val type: SortType, val label: String)

private val sortOptions = listOf(
    SortOption(SortType.MOST_RECENT,    "En Yeni"),
    SortOption(SortType.MOST_FAVORITED, "En Popüler"),
    SortOption(SortType.HIGHEST_RATED,  "En Beğenilen"),
    SortOption(SortType.OLDEST,         "En Çok Yorumlanan")
)

@Composable
private fun SortSheetContent(
    currentSort: SortType,
    onSelect: (SortType) -> Unit,
    onReset: () -> Unit,
    onDismiss: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp).padding(bottom = 36.dp)
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
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (isSelected) FenGreenLight else Color.Transparent)
                    .border(1.dp, if (isSelected) FenGreenLight else Color.Transparent, RoundedCornerShape(12.dp))
                    .clickable { onSelect(option.type) }
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text       = option.label,
                    color      = if (isSelected) FenGreen else TextPrimary,
                    fontSize   = 15.sp,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                    modifier   = Modifier.weight(1f)
                )
                if (isSelected) {
                    Icon(Icons.Default.Check, null, tint = FenGreen, modifier = Modifier.size(18.dp))
                }
            }
        }

        // ← Sıfırla butonu — default dışındaysa göster
        if (currentSort != SortType.MOST_RECENT) {
            Spacer(Modifier.height(12.dp))
            OutlinedButton(
                onClick  = onReset,
                modifier = Modifier.fillMaxWidth().height(44.dp),
                shape    = RoundedCornerShape(12.dp),
                colors   = ButtonDefaults.outlinedButtonColors(contentColor = TextSecondary),
                border   = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFDDDDDD))
            ) {
                Text("Sıralamayı Sıfırla", fontSize = 13.sp)
            }
        }
    }
}