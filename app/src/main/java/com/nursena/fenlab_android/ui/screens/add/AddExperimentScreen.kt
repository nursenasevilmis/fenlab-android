package com.nursena.fenlab_android.ui.screens.add

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.nursena.fenlab_android.core.base.UiEvent
import com.nursena.fenlab_android.domain.model.enums.*
import com.nursena.fenlab_android.ui.theme.*

private val stepLabels = listOf("1.TEMEL", "2.VİDEO", "3.MALZEME", "4.ÖNİZLEME")

@Composable
fun AddExperimentScreen(
    onBack: () -> Unit,
    onPublished: (Long) -> Unit,
    viewModel: AddExperimentViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showSuccess by remember { mutableStateOf(false) }
    var publishedId by remember { mutableLongStateOf(-1L) }

    LaunchedEffect(viewModel) {
        viewModel.eventFlow.collect { event ->
            if (event is UiEvent.Navigate && event.route.startsWith("experiment/")) {
                publishedId = event.route.removePrefix("experiment/").toLongOrNull() ?: -1L
                showSuccess = true
            }
        }
    }

    if (showSuccess) {
        SuccessScreen(
            onNewExperiment = {
                showSuccess = false
                onBack()
            }
        )
        return
    }

    Column(modifier = Modifier.fillMaxSize().background(DarkBg)) {
        // Top bar
        AddTopBar(
            currentStep = uiState.currentStep,
            onBack      = { if (uiState.currentStep == 0) onBack() else viewModel.prevStep() }
        )

        // Step indicator
        StepIndicator(currentStep = uiState.currentStep)

        // Content
        Box(modifier = Modifier.weight(1f)) {
            AnimatedContent(
                targetState = uiState.currentStep,
                transitionSpec = {
                    if (targetState > initialState)
                        slideInHorizontally { it } + fadeIn() togetherWith slideOutHorizontally { -it } + fadeOut()
                    else
                        slideInHorizontally { -it } + fadeIn() togetherWith slideOutHorizontally { it } + fadeOut()
                },
                label = "step"
            ) { step ->
                val scrollState = rememberScrollState()
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState)
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                        .padding(bottom = 80.dp)
                ) {
                    when (step) {
                        0 -> Step0Basic(uiState, viewModel)
                        1 -> Step1Media(uiState, viewModel)
                        2 -> Step2Materials(uiState, viewModel)
                        3 -> Step3Preview(uiState)
                    }
                }
            }
        }

        // Hata mesajı
        uiState.error?.let { error ->
            Text(
                text     = error,
                color    = Red400,
                fontSize = 12.sp,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
            )
        }

        // Bottom butonlar
        BottomButtons(
            currentStep = uiState.currentStep,
            isLoading   = uiState.isLoading,
            onNext      = viewModel::nextStep,
            onBack      = { if (uiState.currentStep == 0) onBack() else viewModel.prevStep() }
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Top Bar
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun AddTopBar(currentStep: Int, onBack: () -> Unit) {
    Row(
        modifier          = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBack, modifier = Modifier.size(36.dp)) {
            Icon(Icons.Default.ArrowBackIosNew, null, tint = TextPrimary, modifier = Modifier.size(18.dp))
        }
        Spacer(Modifier.width(8.dp))
        Text("Deney Ekle", color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.width(4.dp))
        Text("+", color = Teal400, fontSize = 20.sp, fontWeight = FontWeight.Bold)
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Step indicator çubuğu
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun StepIndicator(currentStep: Int) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(DarkSurface)
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        // İlerleme çubukları
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            repeat(4) { i ->
                val progress by animateFloatAsState(
                    targetValue = when {
                        i < currentStep  -> 1f
                        i == currentStep -> 1f
                        else             -> 0f
                    },
                    animationSpec = tween(400),
                    label = "bar$i"
                )
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(3.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(DarkSurface3)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(progress)
                            .background(
                                Brush.horizontalGradient(listOf(Teal400, Color(0xFF00A896))),
                                RoundedCornerShape(2.dp)
                            )
                    )
                }
            }
        }

        Spacer(Modifier.height(6.dp))

        // Etiketler
        Row {
            stepLabels.forEachIndexed { i, label ->
                Text(
                    text      = label,
                    color     = if (i <= currentStep) Teal400 else TextSecondary,
                    fontSize  = 9.sp,
                    fontWeight = if (i == currentStep) FontWeight.Bold else FontWeight.Normal,
                    modifier  = Modifier.weight(1f),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// ADIM 0 — Temel Bilgiler
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun Step0Basic(state: AddExperimentUiState, vm: AddExperimentViewModel) {
    SectionCard(title = "Temel Bilgiler") {
        // Başlık
        AddLabel("Başlık *")
        AddTextField(value = state.title, onValueChange = vm::onTitleChange, placeholder = "örn. Volkan Patlaması Deneyi")
        Spacer(Modifier.height(12.dp))

        // Açıklama
        AddLabel("Açıklama *")
        AddTextField(
            value         = state.description,
            onValueChange = vm::onDescriptionChange,
            placeholder   = "Deneyi kısaca açıkla...",
            minLines      = 3, maxLines = 5
        )
        Spacer(Modifier.height(14.dp))

        // Sınıf seviyesi
        AddLabel("Sınıf Seviyesi")
        GradeSelector(selected = state.gradeLevel, onSelect = vm::onGradeLevelChange)
        Spacer(Modifier.height(14.dp))

        // Ders + Seviye yan yana
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Column(modifier = Modifier.weight(1f)) {
                AddLabel("Ders")
                DropdownSelector(
                    label    = state.subject?.toDisplayString() ?: "Seç...",
                    items    = SubjectType.entries,
                    selected = state.subject,
                    display  = { it.toDisplayString() },
                    onSelect = vm::onSubjectChange
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                AddLabel("Seviye")
                DropdownSelector(
                    label    = state.difficulty.toDisplayString(),
                    items    = DifficultyLevel.entries,
                    selected = state.difficulty,
                    display  = { it.toDisplayString() },
                    onSelect = vm::onDifficultyChange
                )
            }
        }
        Spacer(Modifier.height(14.dp))

        // Mekan
        AddLabel("Mekan")
        ChipGroup(
            items    = EnvironmentType.entries,
            selected = state.environment,
            display  = { it.toDisplayString() },
            onSelect = vm::onEnvironmentChange
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// ADIM 1 — Video & Kapak
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun Step1Media(state: AddExperimentUiState, vm: AddExperimentViewModel) {
    val context = LocalContext.current

    val videoLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let { vm.uploadVideo(context, it) }
    }
    val imageLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let { vm.uploadCoverImage(context, it) }
    }

    // Video
    SectionCard(title = "Deney Videosu") {
        UploadBox(
            isUploading = state.isUploadingVideo,
            doneUrl     = state.videoUrl,
            icon        = "☁️",
            mainText    = "Videoyu Seç",
            subText     = "MP4, MOV · Maks. 500MB",
            buttonText  = "Dosya Seç",
            onSelect    = { videoLauncher.launch("video/*") }
        )
    }

    Spacer(Modifier.height(12.dp))

    // Kapak görseli
    SectionCard(title = "Kapak Görseli") {
        if (state.coverImageUrl != null) {
            Box(
                modifier = Modifier.fillMaxWidth().height(160.dp)
                    .clip(RoundedCornerShape(10.dp))
            ) {
                AsyncImage(
                    model = state.coverImageUrl, contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                Box(
                    modifier = Modifier.align(Alignment.TopEnd).padding(8.dp)
                        .size(28.dp).clip(CircleShape)
                        .background(Color.Black.copy(0.5f))
                        .clickable { vm.uploadCoverImage(context, Uri.EMPTY) },
                    contentAlignment = Alignment.Center
                ) { Icon(Icons.Default.Close, null, tint = Color.White, modifier = Modifier.size(14.dp)) }
            }
        } else {
            UploadBox(
                isUploading = state.isUploadingImage,
                doneUrl     = null,
                icon        = "🖼️",
                mainText    = "Kapak Görseli Yükle",
                subText     = "JPG, PNG · Maks. 5MB",
                buttonText  = null,
                onSelect    = { imageLauncher.launch("image/*") }
            )
        }
    }
}

@Composable
private fun UploadBox(
    isUploading: Boolean,
    doneUrl: String?,
    icon: String,
    mainText: String,
    subText: String,
    buttonText: String?,
    onSelect: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .border(1.dp, Brush.linearGradient(listOf(DarkSurface3, Teal400.copy(0.3f))), RoundedCornerShape(12.dp))
            .background(DarkSurface2)
            .clickable(enabled = !isUploading) { onSelect() }
            .padding(vertical = 28.dp),
        contentAlignment = Alignment.Center
    ) {
        if (isUploading) {
            CircularProgressIndicator(color = Teal400, modifier = Modifier.size(32.dp), strokeWidth = 3.dp)
        } else if (doneUrl != null) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Default.CheckCircle, null, tint = Teal400, modifier = Modifier.size(20.dp))
                Text("Yüklendi", color = Teal400, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            }
        } else {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(icon, fontSize = 28.sp)
                Spacer(Modifier.height(8.dp))
                Text(mainText, color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                Text(subText, color = TextSecondary, fontSize = 11.sp)
                if (buttonText != null) {
                    Spacer(Modifier.height(12.dp))
                    Button(
                        onClick = onSelect,
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Teal400),
                        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp)
                    ) { Text(buttonText, color = DarkBg, fontSize = 13.sp, fontWeight = FontWeight.SemiBold) }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// ADIM 2 — Malzeme & Adımlar
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun Step2Materials(state: AddExperimentUiState, vm: AddExperimentViewModel) {
    // Malzemeler
    SectionCard(title = "🧪 Malzemeler") {
        state.materials.forEachIndexed { index, mat ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                // Numara
                Box(
                    modifier = Modifier.size(28.dp).background(DarkSurface3, CircleShape),
                    contentAlignment = Alignment.Center
                ) { Text("${index + 1}", color = TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.Bold) }

                // Ad
                AddTextField(
                    value         = mat.name,
                    onValueChange = { vm.onMaterialNameChange(index, it) },
                    placeholder   = "Malzeme adı",
                    modifier      = Modifier.weight(1.8f)
                )
                // Miktar
                AddTextField(
                    value         = mat.quantity,
                    onValueChange = { vm.onMaterialQuantityChange(index, it) },
                    placeholder   = "Miktar",
                    modifier      = Modifier.weight(1f)
                )
                // Sil
                IconButton(onClick = { if (state.materials.size > 1) vm.removeMaterial(index) },
                    modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Close, null,
                        tint = if (state.materials.size > 1) Red400 else DarkSurface3,
                        modifier = Modifier.size(16.dp))
                }
            }
        }

        Spacer(Modifier.height(4.dp))
        AddOutlinedButton(text = "+ Malzeme Ekle", onClick = vm::addMaterial)
    }

    Spacer(Modifier.height(12.dp))

    // Adımlar
    SectionCard(title = "📋 Adımlar") {
        state.steps.forEachIndexed { index, step ->
            Row(
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.padding(bottom = 10.dp)
            ) {
                // Numara daire
                Box(
                    modifier = Modifier.size(28.dp).clip(CircleShape)
                        .background(Brush.linearGradient(listOf(Teal400, Color(0xFF00A896)))),
                    contentAlignment = Alignment.Center
                ) { Text("${index + 1}", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold) }

                Column(modifier = Modifier.weight(1f)) {
                    AddTextField(
                        value         = step.text,
                        onValueChange = { vm.onStepTextChange(index, it) },
                        placeholder   = "${index + 1}. adımı açıkla...",
                        minLines      = 2, maxLines = 4
                    )
                    if (state.steps.size > 1) {
                        Text(
                            "Adımı sil",
                            color    = Red400,
                            fontSize = 12.sp,
                            modifier = Modifier.clickable { vm.removeStep(index) }.padding(top = 4.dp)
                        )
                    }
                }
            }
        }

        AddOutlinedButton(text = "+ Adım Ekle", onClick = vm::addStep)
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// ADIM 3 — Önizleme
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun Step3Preview(state: AddExperimentUiState) {
    // Kapak
    Box(
        modifier = Modifier.fillMaxWidth().height(180.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(DarkSurface)
    ) {
        if (state.coverImageUrl != null) {
            AsyncImage(state.coverImageUrl, null, contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize())
        } else {
            Column(Modifier.fillMaxSize(), Arrangement.Center, Alignment.CenterHorizontally) {
                Text("🔬", fontSize = 40.sp)
                Text("Kapak görseli yüklenmedi", color = TextSecondary, fontSize = 13.sp)
            }
        }
        // Gradient overlay
        Box(modifier = Modifier.fillMaxSize().background(
            Brush.verticalGradient(listOf(Color.Transparent, DarkBg.copy(0.7f)))
        ))
    }

    Spacer(Modifier.height(14.dp))

    // Başlık & açıklama
    Card(shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = DarkSurface)) {
        Column(Modifier.padding(14.dp)) {
            Text(state.title.ifBlank { "Deney Başlığı" },
                color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(4.dp))
            Text(state.description.ifBlank { "Açıklama..." },
                color = TextSecondary, fontSize = 13.sp, lineHeight = 18.sp)
        }
    }

    Spacer(Modifier.height(10.dp))

    // Özet kart
    Card(shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = DarkSurface)) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("📊 Özet", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            HorizontalDivider(thickness = 0.5.dp, color = DarkSurface3)
            SummaryRow("🧪", "${state.materials.count { it.name.isNotBlank() }} malzeme eklendi")
            SummaryRow("📌", "${state.steps.count { it.text.isNotBlank() }} adım eklendi")
            SummaryRow("📚", "Ders: ${state.subject?.toDisplayString() ?: "—"} · Seviye: ${state.difficulty.toDisplayString()} · Mekan: ${state.environment?.toDisplayString() ?: "—"}")
            if (state.videoUrl != null) SummaryRow("🎬", "Video yüklendi ✓")
        }
    }
}

@Composable
private fun SummaryRow(icon: String, text: String) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(icon, fontSize = 13.sp)
        Text(text, color = TextSecondary, fontSize = 12.sp, lineHeight = 17.sp)
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Başarı ekranı
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun SuccessScreen(onNewExperiment: () -> Unit) {
    Column(
        modifier            = Modifier.fillMaxSize().background(DarkBg).padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("🎉", fontSize = 64.sp)
        Spacer(Modifier.height(20.dp))
        Text("Deney Yayınlandı!", color = TextPrimary, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(10.dp))
        Text(
            "Deneyin topluluğa katıldı.\nÖğrenciler artık keşfedebilir.",
            color     = TextSecondary, fontSize = 14.sp,
            lineHeight = 21.sp, textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(32.dp))
        Button(
            onClick  = onNewExperiment,
            shape    = RoundedCornerShape(14.dp),
            colors   = ButtonDefaults.buttonColors(containerColor = Teal400),
            modifier = Modifier.fillMaxWidth().height(52.dp)
        ) {
            Text("+ Yeni Deney Ekle", color = DarkBg, fontSize = 15.sp, fontWeight = FontWeight.Bold)
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Bottom Butonlar
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun BottomButtons(
    currentStep: Int,
    isLoading: Boolean,
    onNext: () -> Unit,
    onBack: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(DarkSurface)
            .padding(horizontal = 16.dp, vertical = 10.dp)
            .navigationBarsPadding(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Geri (step 0'da gizle)
        if (currentStep > 0) {
            OutlinedButton(
                onClick = onBack,
                modifier = Modifier.weight(1f).height(48.dp),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, DarkSurface3),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = TextSecondary)
            ) {
                Icon(Icons.Default.ArrowBackIosNew, null, modifier = Modifier.size(14.dp))
                Spacer(Modifier.width(6.dp))
                Text("Geri", fontSize = 14.sp)
            }
        }

        // İleri / Yayınla
        Button(
            onClick  = onNext,
            enabled  = !isLoading,
            modifier = Modifier.weight(if (currentStep == 0) 1f else 1.4f).height(48.dp),
            shape    = RoundedCornerShape(12.dp),
            colors   = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
            contentPadding = PaddingValues(0.dp)
        ) {
            Box(
                modifier = Modifier.fillMaxSize()
                    .background(
                        Brush.linearGradient(listOf(Teal400, Color(0xFF00A896))),
                        RoundedCornerShape(12.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (isLoading) {
                    CircularProgressIndicator(color = DarkBg, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        if (currentStep == 3) Text("🚀", fontSize = 15.sp)
                        Text(
                            if (currentStep < 3) "İleri" else "Yayınla",
                            color = DarkBg, fontSize = 15.sp, fontWeight = FontWeight.Bold
                        )
                        if (currentStep < 3) Icon(Icons.Default.ArrowForward, null,
                            tint = DarkBg, modifier = Modifier.size(16.dp))
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Ortak UI bileşenleri
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun SectionCard(title: String, content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape    = RoundedCornerShape(14.dp),
        colors   = CardDefaults.cardColors(containerColor = DarkSurface),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(Modifier.padding(14.dp)) {
            Text(title, color = TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
private fun AddLabel(text: String) {
    Text(text, color = TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.Medium,
        modifier = Modifier.padding(bottom = 5.dp))
}

@Composable
private fun AddTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier.fillMaxWidth(),
    minLines: Int = 1,
    maxLines: Int = 1
) {
    TextField(
        value = value, onValueChange = onValueChange,
        placeholder = { Text(placeholder, color = Color(0xFF3D5070), fontSize = 13.sp) },
        modifier = modifier,
        minLines = minLines, maxLines = maxLines,
        shape = RoundedCornerShape(10.dp),
        colors = TextFieldDefaults.colors(
            focusedContainerColor   = DarkSurface2,
            unfocusedContainerColor = DarkSurface2,
            focusedTextColor        = TextPrimary,
            unfocusedTextColor      = TextPrimary,
            cursorColor             = Teal400,
            focusedIndicatorColor   = Teal400,
            unfocusedIndicatorColor = Color.Transparent
        )
    )
}

@Composable
private fun <T> DropdownSelector(
    label: String,
    items: List<T>,
    selected: T?,
    display: (T) -> String,
    onSelect: (T) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        Row(
            modifier = Modifier.fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(DarkSurface2)
                .border(1.dp, if (selected != null) Teal400.copy(0.4f) else Color.Transparent, RoundedCornerShape(10.dp))
                .clickable { expanded = true }
                .padding(horizontal = 12.dp, vertical = 11.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(label, color = if (selected != null) TextPrimary else Color(0xFF3D5070), fontSize = 13.sp,
                modifier = Modifier.weight(1f))
            Icon(Icons.Default.ExpandMore, null, tint = TextSecondary, modifier = Modifier.size(18.dp))
        }
        DropdownMenu(
            expanded = expanded, onDismissRequest = { expanded = false },
            modifier = Modifier.background(DarkSurface2)
        ) {
            items.forEach { item ->
                DropdownMenuItem(
                    text = { Text(display(item), color = if (item == selected) Teal400 else TextPrimary, fontSize = 13.sp) },
                    onClick = { onSelect(item); expanded = false },
                    trailingIcon = { if (item == selected) Icon(Icons.Default.Check, null, tint = Teal400, modifier = Modifier.size(14.dp)) }
                )
            }
        }
    }
}

@Composable
private fun <T> ChipGroup(
    items: List<T>,
    selected: T?,
    display: (T) -> String,
    onSelect: (T?) -> Unit
) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
        items.forEach { item ->
            val isSelected = item == selected
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(if (isSelected) Teal400.copy(0.15f) else DarkSurface2)
                    .border(1.dp, if (isSelected) Teal400 else Color.Transparent, RoundedCornerShape(20.dp))
                    .clickable { onSelect(if (isSelected) null else item) }
                    .padding(horizontal = 12.dp, vertical = 7.dp)
            ) {
                Text(display(item), color = if (isSelected) Teal400 else TextSecondary, fontSize = 12.sp,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal)
            }
        }
    }
}

@Composable
private fun GradeSelector(selected: Int, onSelect: (Int) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        listOf(1 to "1-4", 5 to "5-8", 9 to "9-12").forEach { (grade, label) ->
            val isSelected = selected in grade until grade + 4
            Box(
                modifier = Modifier.weight(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (isSelected) Teal400.copy(0.15f) else DarkSurface2)
                    .border(1.dp, if (isSelected) Teal400 else Color.Transparent, RoundedCornerShape(10.dp))
                    .clickable { onSelect(grade) }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(label, color = if (isSelected) Teal400 else TextSecondary, fontSize = 13.sp,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal)
            }
        }
    }
}

@Composable
private fun AddOutlinedButton(text: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .border(1.dp, Brush.linearGradient(listOf(Teal400.copy(0.5f), Color(0xFF00A896).copy(0.5f))), RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 11.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text, color = Teal400, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
    }
}