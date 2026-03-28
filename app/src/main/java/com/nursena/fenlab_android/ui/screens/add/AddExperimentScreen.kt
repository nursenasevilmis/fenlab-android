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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
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

// Adım sırası: Temel → Malzeme&Adım → Video → Önizleme
private val stepLabels = listOf("1.TEMEL", "2.MALZEME", "3.VİDEO", "4.ÖNİZLEME")

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
            onViewExperiment = { if (publishedId > 0) onPublished(publishedId) },
            onNewExperiment  = { showSuccess = false; viewModel.resetForm() },
            onHome           = onBack
        )
        return
    }

    Column(modifier = Modifier.fillMaxSize().background(Color(0xFFF5F7FA))) {
        AddTopBar(
            currentStep = uiState.currentStep,
            onBack      = { if (uiState.currentStep == 0) onBack() else viewModel.prevStep() }
        )
        StepIndicator(currentStep = uiState.currentStep)

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
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                        .padding(bottom = 80.dp)
                ) {
                    when (step) {
                        0 -> Step0Basic(uiState, viewModel)
                        1 -> Step1Materials(uiState, viewModel)
                        2 -> Step2Media(uiState, viewModel)
                        3 -> Step3Preview(uiState)
                    }
                }
            }
        }

        uiState.error?.let { error ->
            Text(
                text     = error,
                color    = Red400,
                fontSize = 12.sp,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
            )
        }

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
        modifier          = Modifier.fillMaxWidth().statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBack, modifier = Modifier.size(36.dp)) {
            Icon(Icons.Default.ArrowBackIosNew, null, tint = TextPrimary, modifier = Modifier.size(18.dp))
        }
        Spacer(Modifier.width(8.dp))
        Text("Deney Ekle", color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.width(4.dp))
        Text("+", color = FrostAccent, fontSize = 18.sp, fontWeight = FontWeight.Bold)
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Step Indicator
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun StepIndicator(currentStep: Int) {
    Column(
        modifier = Modifier.fillMaxWidth().background(Color(0xFFF8F9FB))
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            repeat(4) { i ->
                val progress by animateFloatAsState(
                    targetValue   = if (i <= currentStep) 1f else 0f,
                    animationSpec = tween(400),
                    label         = "bar$i"
                )
                Box(
                    modifier = Modifier.weight(1f).height(3.dp).clip(RoundedCornerShape(2.dp))
                        .background(Color(0xFFCFD8DC))
                ) {
                    Box(
                        modifier = Modifier.fillMaxHeight().fillMaxWidth(progress)
                            .background(Brush.horizontalGradient(listOf(FrostAccent, FrostAccentDark)), RoundedCornerShape(2.dp))
                    )
                }
            }
        }
        Spacer(Modifier.height(5.dp))
        Row {
            stepLabels.forEachIndexed { i, label ->
                Text(
                    text      = label,
                    color     = if (i <= currentStep) FrostAccent else TextSecondary,
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
        AddLabel("Başlık *")
        AddTextField(state.title, vm::onTitleChange, "örn. Volkan Patlaması Deneyi")
        Spacer(Modifier.height(10.dp))

        AddLabel("Açıklama *")
        AddTextField(state.description, vm::onDescriptionChange, "Deneyi kısaca açıkla...", minLines = 3, maxLines = 5)
        Spacer(Modifier.height(10.dp))

        AddLabel("Konu")
        AddTextField(state.topic, vm::onTopicChange, "Deneyin konusu / başlığı")
        Spacer(Modifier.height(10.dp))

        AddLabel("Sınıf Seviyesi")
        GradeSelector(selected = state.gradeLevel, onSelect = vm::onGradeLevelChange)
        Spacer(Modifier.height(10.dp))

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
        Spacer(Modifier.height(10.dp))

        AddLabel("Mekan")
        ChipGroup(
            items    = EnvironmentType.entries,
            selected = state.environment,
            display  = { it.toDisplayString() },
            onSelect = vm::onEnvironmentChange
        )
    }

    Spacer(Modifier.height(12.dp))

    // Beklenen Sonuç
    SectionCard(title = "Beklenen Sonuç") {
        AddTextField(
            value         = state.expectedResult,
            onValueChange = vm::onExpectedResultChange,
            placeholder   = "Deneyin sonucunda ne gözlemlenmeli?",
            minLines      = 2, maxLines = 4
        )
    }

    Spacer(Modifier.height(12.dp))

    // Güvenlik Notları
    SectionCard(title = "Güvenlik Notları") {
        AddTextField(
            value         = state.safetyNotes,
            onValueChange = vm::onSafetyNotesChange,
            placeholder   = "Dikkat edilmesi gereken güvenlik kuralları...",
            minLines      = 2, maxLines = 4
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// ADIM 1 — Malzeme & Adımlar  (eski step2)
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun Step1Materials(state: AddExperimentUiState, vm: AddExperimentViewModel) {
    SectionCard(title = "Malzemeler") {
        state.materials.forEachIndexed { index, mat ->
            Row(
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier              = Modifier.padding(bottom = 8.dp)
            ) {
                Box(
                    modifier = Modifier.size(26.dp).background(Color(0xFFCFD8DC), CircleShape),
                    contentAlignment = Alignment.Center
                ) { Text("${index + 1}", color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold) }

                AddTextField(mat.name, { vm.onMaterialNameChange(index, it) }, "Malzeme adı", modifier = Modifier.weight(1.8f))
                AddTextField(mat.quantity, { vm.onMaterialQuantityChange(index, it) }, "Miktar", modifier = Modifier.weight(1f))

                IconButton(
                    onClick  = { if (state.materials.size > 1) vm.removeMaterial(index) },
                    modifier = Modifier.size(30.dp)
                ) {
                    Icon(Icons.Default.Close, null,
                        tint     = if (state.materials.size > 1) Red400 else Color(0xFFCFD8DC),
                        modifier = Modifier.size(14.dp))
                }
            }
        }
        Spacer(Modifier.height(4.dp))
        AddOutlinedButton("+ Malzeme Ekle", vm::addMaterial)
    }

    Spacer(Modifier.height(12.dp))

    SectionCard(title = "Adımlar") {
        state.steps.forEachIndexed { index, step ->
            Row(
                verticalAlignment     = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier              = Modifier.padding(bottom = 10.dp)
            ) {
                Box(
                    modifier = Modifier.size(26.dp).clip(CircleShape)
                        .background(Brush.linearGradient(listOf(FrostAccent, FrostAccentDark))),
                    contentAlignment = Alignment.Center
                ) { Text("${index + 1}", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold) }

                Column(modifier = Modifier.weight(1f)) {
                    AddTextField(step.text, { vm.onStepTextChange(index, it) }, "${index + 1}. adımı açıkla...", minLines = 2, maxLines = 4)
                    if (state.steps.size > 1) {
                        Text("Sil", color = Red400, fontSize = 11.sp,
                            modifier = Modifier.clickable { vm.removeStep(index) }.padding(top = 4.dp))
                    }
                }
            }
        }
        AddOutlinedButton("+ Adım Ekle", vm::addStep)
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// ADIM 2 — Video & Kapak  (eski step1, şimdi önizlemeden önce)
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun Step2Media(state: AddExperimentUiState, vm: AddExperimentViewModel) {
    val context = LocalContext.current

    // Video — kırpma yok
    val videoLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let { vm.uploadVideo(context, it) }
    }

    val coverLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let { vm.uploadCoverImage(context, it) }
    }
    val additionalLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let { vm.uploadAdditionalImage(context, it) }
    }

    // ── Video ──────────────────────────────────────────────────────────────────
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
        if (state.videoUrl != null) {
            Spacer(Modifier.height(6.dp))
            Text("× Videoyu kaldır", color = Red400, fontSize = 11.sp,
                modifier = Modifier.clickable { vm.clearVideo() })
        }
    }

    Spacer(Modifier.height(12.dp))

    // ── Kapak Görseli ─────────────────────────────────────────────────────────
    SectionCard(title = "Kapak Görseli") {
        if (state.coverImageUrl != null) {
            Box(modifier = Modifier.fillMaxWidth().height(150.dp).clip(RoundedCornerShape(10.dp))) {
                AsyncImage(model = state.coverImageUrl, contentDescription = null,
                    contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                Box(
                    modifier = Modifier.align(Alignment.TopEnd).padding(8.dp)
                        .size(26.dp).clip(CircleShape).background(Color(0x99000000))
                        .clickable { vm.clearCoverImage() },
                    contentAlignment = Alignment.Center
                ) { Icon(Icons.Default.Close, null, tint = Color.White, modifier = Modifier.size(13.dp)) }
            }
        } else {
            UploadBox(
                isUploading = state.isUploadingImage,
                doneUrl     = null,
                icon        = "🖼️",
                mainText    = "Kapak Görseli Yükle",
                subText     = "JPG, PNG · İlk gösterilecek resim",
                buttonText  = null,
                onSelect    = { coverLauncher.launch("image/*") }
            )
        }
    }

    Spacer(Modifier.height(12.dp))

    // ── Ek Görseller ─────────────────────────────────────────────────────────
    SectionCard(title = "Ek Görseller (${state.additionalImages.size}/10)") {
        if (state.additionalImages.isNotEmpty() || state.isUploadingAdditional) {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                itemsIndexed(items = state.additionalImages) { index: Int, url: String ->
                    Box(modifier = Modifier.size(110.dp).clip(RoundedCornerShape(10.dp))) {
                        AsyncImage(model = url, contentDescription = null,
                            contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                        Box(
                            modifier = Modifier.align(Alignment.TopEnd).padding(4.dp)
                                .size(22.dp).clip(CircleShape).background(Color(0xA6000000))
                                .clickable { vm.removeAdditionalImage(index) },
                            contentAlignment = Alignment.Center
                        ) { Icon(Icons.Default.Close, null, tint = Color.White, modifier = Modifier.size(11.dp)) }
                        Box(
                            modifier = Modifier.align(Alignment.BottomStart).padding(4.dp)
                                .background(Color(0x80000000), RoundedCornerShape(4.dp))
                                .padding(horizontal = 4.dp, vertical = 2.dp)
                        ) { Text("${index + 1}", color = Color.White, fontSize = 9.sp) }
                    }
                }
                if (state.isUploadingAdditional) {
                    item {
                        Box(modifier = Modifier.size(110.dp).clip(RoundedCornerShape(10.dp)).background(Color(0xFFECEFF1)),
                            contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = FrostAccent, modifier = Modifier.size(22.dp), strokeWidth = 2.dp)
                        }
                    }
                }
                if (state.additionalImages.size < 10 && !state.isUploadingAdditional) {
                    item {
                        Box(
                            modifier = Modifier.size(110.dp).clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFFECEFF1))
                                .border(1.dp, Brush.linearGradient(listOf(Color(0x66F06292), Color(0x4DEC407A))), RoundedCornerShape(10.dp))
                                .clickable { additionalLauncher.launch("image/*") },
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.Add, null, tint = FrostAccent, modifier = Modifier.size(22.dp))
                                Spacer(Modifier.height(3.dp))
                                Text("Ekle", color = FrostAccent, fontSize = 10.sp, fontWeight = FontWeight.Medium)
                            }
                        }
                    }
                }
            }
        } else {
            Box(
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp))
                    .border(1.dp, Brush.linearGradient(listOf(Color(0xFFCFD8DC), Color(0x4DF06292))), RoundedCornerShape(12.dp))
                    .background(Color(0xFFECEFF1)).clickable { additionalLauncher.launch("image/*") }
                    .padding(vertical = 22.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Add, null, tint = FrostAccent, modifier = Modifier.size(26.dp))
                    Spacer(Modifier.height(5.dp))
                    Text("Ek Görsel Yükle", color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    Text("Deneyin farklı aşamalarını göster", color = TextSecondary, fontSize = 10.sp)
                }
            }
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
        modifier = Modifier.fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .border(1.dp, Brush.linearGradient(listOf(Color(0xFFCFD8DC), Color(0x4DF06292))), RoundedCornerShape(12.dp))
            .background(Color(0xFFECEFF1))
            .clickable(enabled = !isUploading) { onSelect() }
            .padding(vertical = 24.dp),
        contentAlignment = Alignment.Center
    ) {
        if (isUploading) {
            CircularProgressIndicator(color = FrostAccent, modifier = Modifier.size(28.dp), strokeWidth = 3.dp)
        } else if (doneUrl != null) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Default.CheckCircle, null, tint = FrostAccent, modifier = Modifier.size(18.dp))
                Text("Yüklendi ✓", color = FrostAccent, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            }
        } else {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(icon, fontSize = 26.sp)
                Spacer(Modifier.height(6.dp))
                Text(mainText, color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                Text(subText, color = TextSecondary, fontSize = 11.sp)
                if (buttonText != null) {
                    Spacer(Modifier.height(10.dp))
                    Button(
                        onClick = onSelect, shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = FrostAccent),
                        contentPadding = PaddingValues(horizontal = 18.dp, vertical = 6.dp)
                    ) { Text(buttonText, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold) }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// ADIM 3 — Önizleme
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun Step3Preview(state: AddExperimentUiState) {
    Box(
        modifier = Modifier.fillMaxWidth().height(170.dp)
            .clip(RoundedCornerShape(14.dp)).background(Color(0xFFF8F9FB))
    ) {
        if (state.coverImageUrl != null) {
            AsyncImage(state.coverImageUrl, null, contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize())
        } else {
            Column(Modifier.fillMaxSize(), Arrangement.Center, Alignment.CenterHorizontally) {
                Text("🔬", fontSize = 36.sp)
                Text("Kapak görseli yüklenmedi", color = TextSecondary, fontSize = 12.sp)
            }
        }
        Box(modifier = Modifier.fillMaxSize().background(
            Brush.verticalGradient(listOf(Color.Transparent, Color(0xB30D1642)))
        ))
    }

    Spacer(Modifier.height(12.dp))

    Card(shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F9FB))) {
        Column(Modifier.padding(14.dp)) {
            Text(state.title.ifBlank { "Deney Başlığı" },
                color = TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(4.dp))
            Text(state.description.ifBlank { "Açıklama..." },
                color = TextSecondary, fontSize = 12.sp, lineHeight = 17.sp)
            if (state.topic.isNotBlank()) {
                Spacer(Modifier.height(4.dp))
                Text("📌 Konu: ${state.topic}", color = TextSecondary, fontSize = 11.sp)
            }
        }
    }

    Spacer(Modifier.height(8.dp))

    Card(shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F9FB))) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
            Text("📊 Özet", color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            HorizontalDivider(thickness = 0.5.dp, color = Color(0xFFCFD8DC))
            SummaryRow("🧪", "${state.materials.count { it.name.isNotBlank() }} malzeme")
            SummaryRow("📌", "${state.steps.count { it.text.isNotBlank() }} adım")
            SummaryRow("📚", "${state.subject?.toDisplayString() ?: "—"} · ${state.difficulty.toDisplayString()} · ${state.environment?.toDisplayString() ?: "—"}")
            if (state.videoUrl != null)      SummaryRow("🎬", "Video yüklendi ✓")
            if (state.coverImageUrl != null) SummaryRow("🖼️", "Kapak görseli yüklendi ✓")
            if (state.expectedResult.isNotBlank()) SummaryRow("🎯", "Beklenen sonuç girildi ✓")
            if (state.safetyNotes.isNotBlank())    SummaryRow("⚠️", "Güvenlik notu girildi ✓")
        }
    }
}

@Composable
private fun SummaryRow(icon: String, text: String) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(icon, fontSize = 12.sp)
        Text(text, color = TextSecondary, fontSize = 11.sp, lineHeight = 16.sp)
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Başarı ekranı — Geri dön + Yeni Deney + Deneyi Gör butonları
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun SuccessScreen(
    onViewExperiment: () -> Unit,
    onNewExperiment: () -> Unit,
    onHome: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize().background(Color(0xFFF5F7FA))) {
        // Sol üst geri
        IconButton(
            onClick  = onHome,
            modifier = Modifier.statusBarsPadding().padding(8.dp).align(Alignment.TopStart)
        ) {
            Icon(Icons.Default.ArrowBackIosNew, null, tint = TextSecondary, modifier = Modifier.size(18.dp))
        }

        Column(
            modifier            = Modifier.fillMaxSize().padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("🎉", fontSize = 60.sp)
            Spacer(Modifier.height(18.dp))
            Text("Deney Yayınlandı!", color = TextPrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            Text(
                "Deneyin topluluğa katıldı.\nÖğrenciler artık keşfedebilir.",
                color = TextSecondary, fontSize = 13.sp,
                lineHeight = 20.sp, textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(28.dp))

            // Deneyi Gör
            Button(
                onClick  = onViewExperiment,
                shape    = RoundedCornerShape(12.dp),
                colors   = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                modifier = Modifier.fillMaxWidth().height(46.dp),
                contentPadding = PaddingValues(0.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize()
                        .background(Brush.linearGradient(listOf(FrostAccent, FrostAccentDark)), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Icon(Icons.Default.Visibility, null, tint = Color.White, modifier = Modifier.size(16.dp))
                        Text("Deneyi Görüntüle", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(Modifier.height(10.dp))

            // Yeni Deney Ekle
            OutlinedButton(
                onClick  = onNewExperiment,
                shape    = RoundedCornerShape(12.dp),
                border   = androidx.compose.foundation.BorderStroke(1.dp, Color(0x80F06292)),
                colors   = ButtonDefaults.outlinedButtonColors(contentColor = FrostAccent),
                modifier = Modifier.fillMaxWidth().height(44.dp)
            ) {
                Icon(Icons.Default.Add, null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text("Yeni Deney Ekle", fontSize = 13.sp)
            }

            Spacer(Modifier.height(10.dp))

            // Ana Sayfaya Dön
            TextButton(onClick = onHome, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Default.Home, null, tint = TextSecondary, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text("Ana Sayfaya Dön", color = TextSecondary, fontSize = 13.sp)
            }
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
        modifier = Modifier.fillMaxWidth().background(Color(0xFFF8F9FB))
            .padding(horizontal = 16.dp, vertical = 10.dp).navigationBarsPadding(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        if (currentStep > 0) {
            OutlinedButton(
                onClick = onBack,
                modifier = Modifier.weight(1f).height(44.dp),
                shape    = RoundedCornerShape(12.dp),
                border   = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFCFD8DC)),
                colors   = ButtonDefaults.outlinedButtonColors(contentColor = TextSecondary)
            ) {
                Icon(Icons.Default.ArrowBackIosNew, null, modifier = Modifier.size(13.dp))
                Spacer(Modifier.width(5.dp))
                Text("Geri", fontSize = 13.sp)
            }
        }

        Button(
            onClick  = onNext,
            enabled  = !isLoading,
            modifier = Modifier.weight(if (currentStep == 0) 1f else 1.4f).height(44.dp),
            shape    = RoundedCornerShape(12.dp),
            colors   = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
            contentPadding = PaddingValues(0.dp)
        ) {
            Box(
                modifier = Modifier.fillMaxSize()
                    .background(Brush.linearGradient(listOf(FrostAccent, FrostAccentDark)), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                if (isLoading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                        if (currentStep == 3) Text("🚀", fontSize = 14.sp)
                        Text(
                            if (currentStep < 3) "İleri" else "Yayınla",
                            color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold
                        )
                        if (currentStep < 3) Icon(Icons.Default.ArrowForward, null,
                            tint = Color.White, modifier = Modifier.size(15.dp))
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Ortak UI
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun SectionCard(title: String, content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(12.dp),
        colors    = CardDefaults.cardColors(containerColor = Color(0xFFF8F9FB)),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(Modifier.padding(14.dp)) {
            Text(title, color = TextSecondary, fontSize = 13.sp, fontWeight = FontWeight.Medium)
            Spacer(Modifier.height(10.dp))
            content()
        }
    }
}

@Composable
private fun AddLabel(text: String) {
    Text(text, color = TextSecondary, fontSize = 13.sp, fontWeight = FontWeight.Medium,
        modifier = Modifier.padding(bottom = 4.dp))
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
        placeholder = { Text(placeholder, color = TextTertiary, fontSize = 12.sp) },
        modifier = modifier,
        minLines = minLines, maxLines = maxLines,
        textStyle = androidx.compose.ui.text.TextStyle(fontSize = 13.sp),
        shape = RoundedCornerShape(10.dp),
        colors = TextFieldDefaults.colors(
            focusedContainerColor   = Color(0xFFECEFF1),
            unfocusedContainerColor = Color(0xFFECEFF1),
            focusedTextColor        = TextPrimary,
            unfocusedTextColor      = TextPrimary,
            cursorColor             = FrostAccent,
            focusedIndicatorColor   = FrostAccent,
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
            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp))
                .background(Color(0xFFECEFF1))
                .border(1.dp, if (selected != null) Color(0x66F06292) else Color.Transparent, RoundedCornerShape(10.dp))
                .clickable { expanded = true }
                .padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment     = Alignment.CenterVertically
        ) {
            Text(label, color = if (selected != null) TextPrimary else TextTertiary, fontSize = 12.sp, modifier = Modifier.weight(1f))
            Icon(Icons.Default.ExpandMore, null, tint = TextSecondary, modifier = Modifier.size(16.dp))
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false },
            modifier = Modifier.background(Color(0xFFECEFF1))) {
            items.forEach { item ->
                DropdownMenuItem(
                    text = { Text(display(item), color = if (item == selected) FrostAccent else TextPrimary, fontSize = 12.sp) },
                    onClick = { onSelect(item); expanded = false },
                    trailingIcon = { if (item == selected) Icon(Icons.Default.Check, null, tint = FrostAccent, modifier = Modifier.size(13.dp)) }
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
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items.forEach { item ->
            val isSel = item == selected
            Box(
                modifier = Modifier.clip(RoundedCornerShape(20.dp))
                    .background(if (isSel) Color(0xFFECEFF1) else Color(0xFFECEFF1))
                    .border(1.dp, if (isSel) FrostAccent else Color.Transparent, RoundedCornerShape(20.dp))
                    .clickable { onSelect(if (isSel) null else item) }
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Text(display(item), color = if (isSel) FrostAccent else TextSecondary, fontSize = 11.sp,
                    fontWeight = if (isSel) FontWeight.SemiBold else FontWeight.Normal)
            }
        }
    }
}

@Composable
private fun GradeSelector(selected: Int, onSelect: (Int) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        listOf(1 to "1-4", 5 to "5-8", 9 to "9-12").forEach { (grade, label) ->
            val isSel = selected in grade until grade + 4
            Box(
                modifier = Modifier.weight(1f).clip(RoundedCornerShape(10.dp))
                    .background(if (isSel) Color(0xFFECEFF1) else Color(0xFFECEFF1))
                    .border(1.dp, if (isSel) FrostAccent else Color.Transparent, RoundedCornerShape(10.dp))
                    .clickable { onSelect(grade) }
                    .padding(vertical = 9.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(label, color = if (isSel) FrostAccent else TextSecondary, fontSize = 12.sp,
                    fontWeight = if (isSel) FontWeight.SemiBold else FontWeight.Normal)
            }
        }
    }
}

@Composable
private fun AddOutlinedButton(text: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .border(1.dp, Brush.linearGradient(listOf(Color(0x80F06292), Color(0x80EC407A))), RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text, color = FrostAccent, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
    }
}