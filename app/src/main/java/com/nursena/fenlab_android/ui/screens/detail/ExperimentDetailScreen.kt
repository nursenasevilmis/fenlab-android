package com.nursena.fenlab_android.ui.screens.detail
import android.app.Activity
import android.content.pm.ActivityInfo
import android.net.Uri
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult   // ← EKLENDİ
import androidx.activity.result.contract.ActivityResultContracts      // ← EKLENDİ
import androidx.annotation.OptIn
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow                        // ← EKLENDİ
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed                   // ← EKLENDİ
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import coil.compose.AsyncImage
import com.nursena.fenlab_android.domain.model.enums.*
import com.nursena.fenlab_android.domain.model.*
import com.nursena.fenlab_android.ui.components.AnimatedFavoriteButton
import com.nursena.fenlab_android.ui.components.LoadingIndicator
import com.nursena.fenlab_android.ui.components.ErrorMessage
import com.nursena.fenlab_android.ui.components.LottieLoadingIndicator
import com.nursena.fenlab_android.ui.theme.*


@OptIn(UnstableApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ExperimentDetailScreen(
    onBack: () -> Unit,
    onAuthorClick: (Long) -> Unit = {},
    viewModel: ExperimentDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    val activity = context as? Activity

    val videoUrl = uiState.experiment?.videoMedia?.mediaUrl
    val exoPlayer = remember(videoUrl) {
        if (videoUrl == null) null
        else ExoPlayer.Builder(context).build().apply {
            setMediaItem(MediaItem.fromUri(Uri.parse(videoUrl)))
            prepare()
        }
    }
    DisposableEffect(exoPlayer) {
        onDispose {
            exoPlayer?.release()
            activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        }
    }

    var isFullscreen by remember { mutableStateOf(false) }

    fun enterFullscreen() {
        isFullscreen = true
        exoPlayer?.playWhenReady = true
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
        activity?.window?.decorView?.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_FULLSCREEN or
                        View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                        View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                )
    }

    fun exitFullscreen() {
        isFullscreen = false
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        activity?.window?.decorView?.systemUiVisibility = View.SYSTEM_UI_FLAG_VISIBLE
    }

    BackHandler(enabled = isFullscreen) { exitFullscreen() }

    LaunchedEffect(viewModel) {
        viewModel.eventFlow.collect { event ->
            if (event is com.nursena.fenlab_android.core.base.UiEvent.ShowSnackbar)
                snackbarHostState.showSnackbar(event.message)
        }
    }

    // ── Tam ekran overlay ────────────────────────────────────────────────────
    if (isFullscreen && exoPlayer != null) {
        Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
            AndroidView(
                factory = { ctx ->
                    PlayerView(ctx).apply {
                        player        = exoPlayer
                        useController = true
                        layoutParams  = FrameLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )
                        setShowNextButton(false); setShowPreviousButton(false)
                        setShowRewindButton(true); setShowFastForwardButton(true)
                        controllerAutoShow = true; controllerHideOnTouch = true
                    }
                },
                update   = { it.player = exoPlayer },
                modifier = Modifier.fillMaxSize()
            )
            IconButton(
                onClick  = { exitFullscreen() },
                modifier = Modifier.align(Alignment.TopStart).systemBarsPadding().padding(8.dp)
            ) {
                Box(modifier = Modifier.size(40.dp).clip(CircleShape).background(Color(0xA6000000)),
                    contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.FullscreenExit, null, tint = Color.White, modifier = Modifier.size(22.dp))
                }
            }
        }
        return
    }

    // ── Normal ekran ─────────────────────────────────────────────────────────
    Scaffold(
        snackbarHost   = { SnackbarHost(snackbarHostState) },
        containerColor = Color.Transparent
    ) { padding ->
        when {
            uiState.isLoading && uiState.experiment == null -> LottieLoadingIndicator()
            uiState.error != null && uiState.experiment == null ->
                ErrorMessage(message = uiState.error!!, onRetry = viewModel::loadExperiment)
            uiState.experiment != null -> {
                val exp = uiState.experiment!!
                LazyColumn(
                    modifier       = Modifier.fillMaxSize().padding(padding),
                    contentPadding = PaddingValues(bottom = 40.dp)
                ) {
                    item {
                        MediaSection(
                            exp          = exp,
                            exoPlayer    = exoPlayer,
                            isFavorited  = uiState.isFavorited,
                            isOwner      = uiState.isOwner,
                            onBack       = onBack,
                            onFavorite   = viewModel::toggleFavorite,
                            onDelete     = { viewModel.deleteExperiment(onBack) },
                            onEdit       = viewModel::openEdit,          // ← YENİ
                            onFullscreen = { enterFullscreen() }
                        )
                    }

                    item {
                        InfoSection(
                            exp           = exp,
                            currentRating = uiState.currentUserRating,
                            isOwner       = uiState.isOwner,            // ← YENİ
                            isPdfLoading  = uiState.isPdfLoading,
                            onAuthorClick = onAuthorClick,
                            onRate        = viewModel::rateExperiment,
                            onDownloadPdf = { viewModel.downloadPdf(context) }
                        )
                    }
                    item {
                        DetailTabBar(
                            selected      = uiState.selectedTab,
                            commentCount  = uiState.comments.size,
                            questionCount = uiState.questions.size,
                            onSelect      = viewModel::selectTab
                        )
                    }
                    item {
                        DescriptionCard(
                            topic          = exp.topic,
                            description    = exp.description,
                            safetyNotes    = exp.safetyNotes,
                            expectedResult = exp.expectedResult
                        )
                    }
                    when (uiState.selectedTab) {
                        0 -> items(exp.materials, key = { it.id }) { mat ->
                            MaterialRow(index = exp.materials.indexOf(mat) + 1, material = mat)
                        }
                        1 -> items(exp.sortedSteps, key = { it.id }) { step ->
                            StepRow(step = step)
                        }
                        2 -> {
                            item {
                                CommentQuestionInput(
                                    commentInput     = uiState.commentInput,
                                    questionInput    = uiState.questionInput,
                                    onCommentChange  = viewModel::onCommentInputChange,
                                    onQuestionChange = viewModel::onQuestionInputChange,
                                    onAddComment     = viewModel::addComment,
                                    onAskQuestion    = viewModel::askQuestion
                                )
                            }
                            items(uiState.comments, key = { "c${it.id}" }) { comment ->
                                CommentItem(
                                    comment = comment,
                                    canDelete = comment.isOwner || uiState.isOwner,
                                    onDelete = { viewModel.deleteComment(comment.id) }
                                )
                            }
                            items(uiState.questions, key = { "q${it.id}" }) { question ->
                                QuestionItem(
                                    question  = question,
                                    canDelete = uiState.isOwner || question.canAnswer,
                                    onAnswer  = { text -> viewModel.answerQuestion(question.id, text) },
                                    onDelete  = { viewModel.deleteQuestion(question.id) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // ── Düzenleme bottom sheet ────────────────────────────────────────────────
    if (uiState.isEditing) {
        EditExperimentSheet(
            state    = uiState,
            vm       = viewModel,
            onDismiss = viewModel::closeEdit
        )
    }
}


// ─────────────────────────────────────────────────────────────────────────────
// Düzenleme bottom sheet
// ─────────────────────────────────────────────────────────────────────────────

@kotlin.OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditExperimentSheet(
    state: DetailUiState,
    vm: ExperimentDetailViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    val coverLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { vm.uploadEditCoverImage(context, it) }
    }
    val additionalLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { vm.uploadEditAdditionalImage(context, it) }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor   = Color(0xFFFFFFFF),
        dragHandle = {
            Box(Modifier.fillMaxWidth().padding(top = 8.dp, bottom = 4.dp), Alignment.Center) {
                Box(Modifier.size(width = 32.dp, height = 3.dp).background(Color(0xFFB0BEC5), RoundedCornerShape(2.dp)))
            }
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
                .padding(bottom = 40.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Başlık
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Deneyi Düzenle", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, null, tint = TextSecondary)
                }
            }
            HorizontalDivider(thickness = 0.5.dp, color = Color(0xFFEEEEEE))

            // ── Temel Bilgiler ────────────────────────────────────────────────
            EditSectionHeader("Temel Bilgiler")

            EditOutlinedField(
                value = state.editTitle,
                onChange = vm::onEditTitleChange,
                label = "Başlık *",
                icon = Icons.Default.Title,
                modifier = Modifier.weight(1.8f)
            )
            EditOutlinedField(
                value = state.editDescription,
                onChange = vm::onEditDescChange,
                label = "Açıklama *",
                icon = Icons.Default.Description,
                minLines = 3,
                modifier = Modifier.weight(1.8f)
            )
            EditOutlinedField(
                value = state.editTopic,
                onChange = vm::onEditTopicChange,
                label = "Konu Etiketi",
                icon = Icons.Default.Tag,
                modifier = Modifier.weight(1.8f)
            )

            // Sınıf seviyesi
            Text("Sınıf Seviyesi", color = TextSecondary, fontSize = 12.sp)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(1 to "1-4", 5 to "5-8", 9 to "9-12").forEach { (grade, label) ->
                    val isSel = state.editGradeLevel in grade until grade + 4
                    Box(
                        modifier = Modifier.weight(1f).clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFFF2F4F5))
                            .border(1.dp, if (isSel) FenGreen else Color.Transparent, RoundedCornerShape(10.dp))
                            .clickable { vm.onEditGradeLevelChange(grade) }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(label, color = if (isSel) FenGreen else TextSecondary, fontSize = 12.sp,
                            fontWeight = if (isSel) FontWeight.SemiBold else FontWeight.Normal)
                    }
                }
            }

            // Ders & Seviye yan yana
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Column(Modifier.weight(1f)) {
                    Text("Ders", color = TextSecondary, fontSize = 12.sp, modifier = Modifier.padding(bottom = 4.dp))
                    EditDropdown(
                        label    = if (state.editSubject == SubjectType.OTHER && state.editCustomSubject.isNotBlank())
                            state.editCustomSubject
                        else state.editSubject?.toDisplayString() ?: "Seç...",
                        items    = SubjectType.entries,
                        selected = state.editSubject,
                        display  = { it.toDisplayString() },
                        onSelect = vm::onEditSubjectChange
                    )
                }
                Column(Modifier.weight(1f)) {
                    Text("Seviye", color = TextSecondary, fontSize = 12.sp, modifier = Modifier.padding(bottom = 4.dp))
                    EditDropdown(
                        label    = state.editDifficulty.toDisplayString(),
                        items    = DifficultyLevel.entries,
                        selected = state.editDifficulty,
                        display  = { it.toDisplayString() },
                        onSelect = vm::onEditDifficultyChange
                    )
                }
            }
            if (state.editSubject == SubjectType.OTHER) {
                EditOutlinedField(
                    value = state.editCustomSubject,
                    onChange = vm::onEditCustomSubjectChange,
                    label = "Ders adını yaz...",
                    icon = Icons.Default.School,
                    modifier = Modifier.weight(1.8f)
                )
            }

            // Mekan
            Text("Mekan", color = TextSecondary, fontSize = 12.sp)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                EnvironmentType.entries.forEach { env ->
                    val isSel = state.editEnvironment == env
                    Box(
                        modifier = Modifier.clip(RoundedCornerShape(20.dp))
                            .background(Color(0xFFF2F4F5))
                            .border(1.dp, if (isSel) FenGreen else Color.Transparent, RoundedCornerShape(20.dp))
                            .clickable { vm.onEditEnvironmentChange(if (isSel) null else env) }
                            .padding(horizontal = 12.dp, vertical = 7.dp)
                    ) {
                        Text(env.toDisplayString(), color = if (isSel) FenGreen else TextSecondary, fontSize = 12.sp,
                            fontWeight = if (isSel) FontWeight.SemiBold else FontWeight.Normal)
                    }
                }
            }

            HorizontalDivider(thickness = 0.5.dp, color = Color(0xFFEEEEEE))

            // ── Ek Bilgiler ───────────────────────────────────────────────────
            EditSectionHeader("Ek Bilgiler")

            EditOutlinedField(
                value = state.editSafetyNotes,
                onChange = vm::onEditSafetyNotesChange,
                label = "Güvenlik Notu",
                icon = Icons.Default.Warning,
                minLines = 2,
                modifier = Modifier.weight(1.8f)
            )
            EditOutlinedField(
                value = state.editExpectedResult,
                onChange = vm::onEditExpectedResultChange,
                label = "Beklenen Sonuç",
                icon = Icons.Default.CheckCircle,
                minLines = 2,
                modifier = Modifier.weight(1.8f)
            )

            HorizontalDivider(thickness = 0.5.dp, color = Color(0xFFEEEEEE))

            // ── Malzemeler ────────────────────────────────────────────────────
            EditSectionHeader("Malzemeler")

            // ── Malzemeler ────────────────────────────────────────────────────
            state.editMaterials.forEachIndexed { index, material ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Sıra numarası
                    Box(
                        modifier = Modifier.size(28.dp).clip(CircleShape)
                            .background(FenGreenLight),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${index + 1}",
                            color = FenGreen,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Malzeme adı — geniş
                    OutlinedTextField(
                        value         = material.name,
                        onValueChange = { vm.updateMaterialName(index, it) },
                        label         = { Text("Malzeme", fontSize = 11.sp) },
                        leadingIcon   = { Icon(Icons.Default.Science, null, tint = FenGreen, modifier = Modifier.size(18.dp)) },
                        modifier      = Modifier.weight(2f),
                        singleLine    = true,
                        shape         = RoundedCornerShape(10.dp),
                        textStyle     = androidx.compose.ui.text.TextStyle(fontSize = 13.sp, color = TextPrimary),
                        colors        = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor      = FenGreen,
                            unfocusedBorderColor    = Color(0xFFDDDDDD),
                            focusedContainerColor   = Color(0xFFFFFFFF),
                            unfocusedContainerColor = Color(0xFFFFFFFF),
                            focusedLabelColor       = FenGreen,
                            unfocusedLabelColor     = TextSecondary,
                            cursorColor             = FenGreen
                        )
                    )

                    // Miktar — dar
                    OutlinedTextField(
                        value         = material.quantity,
                        onValueChange = { vm.updateMaterialQuantity(index, it) },
                        label         = { Text("Miktar", fontSize = 11.sp) },
                        modifier      = Modifier.weight(1f),
                        singleLine    = true,
                        shape         = RoundedCornerShape(10.dp),
                        textStyle     = androidx.compose.ui.text.TextStyle(fontSize = 13.sp, color = TextPrimary),
                        colors        = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor      = FenGreen,
                            unfocusedBorderColor    = Color(0xFFDDDDDD),
                            focusedContainerColor   = Color(0xFFFFFFFF),
                            unfocusedContainerColor = Color(0xFFFFFFFF),
                            focusedLabelColor       = FenGreen,
                            unfocusedLabelColor     = TextSecondary,
                            cursorColor             = FenGreen
                        )
                    )

                    // Sil
                    IconButton(
                        onClick  = { vm.removeMaterial(index) },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            Icons.Default.DeleteOutline,
                            contentDescription = null,
                            tint     = Color(0xFFEF5350),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
            // ── Adımlar ───────────────────────────────────────────────────────
            EditSectionHeader("Adımlar")

            state.editSteps.forEachIndexed { index, step ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Adım numarası
                    Box(
                        modifier = Modifier.size(28.dp).offset(y = 12.dp).clip(CircleShape)
                            .background(FenGreenLight),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${index + 1}",
                            color = FenGreen,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    // Adım metni
                    EditOutlinedField(
                        value    = step.text,
                        onChange = { vm.updateStepText(index, it) },
                        label    = "Adım ${index + 1}",
                        icon     = Icons.Default.EditNote,
                        minLines = 2,
                        modifier = Modifier.weight(1f)
                    )
                    // Sil
                    IconButton(
                        onClick  = { vm.removeStep(index) },
                        modifier = Modifier.size(32.dp).offset(y = 8.dp)
                    ) {
                        Icon(
                            Icons.Default.DeleteOutline,
                            contentDescription = null,
                            tint     = Color(0xFFEF5350),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            // Adım ekle butonu
            OutlinedButton(
                onClick  = vm::addStep,
                modifier = Modifier.fillMaxWidth().height(42.dp),
                shape    = RoundedCornerShape(10.dp),
                border   = androidx.compose.foundation.BorderStroke(1.dp, FenGreen),
                colors   = ButtonDefaults.outlinedButtonColors(contentColor = FenGreen)
            ) {
                Icon(Icons.Default.Add, null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text("Adım Ekle", fontSize = 13.sp)
            }

            HorizontalDivider(thickness = 0.5.dp, color = Color(0xFFEEEEEE))

            // ── Görseller ─────────────────────────────────────────────────────
            EditSectionHeader("Görseller")

            // Kapak
            Text("Kapak Görseli", color = TextSecondary, fontSize = 12.sp)
            if (state.isUploadingEditCover) {
                Box(modifier = Modifier.fillMaxWidth().height(120.dp).clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFFF2F4F5)), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = FenGreen, modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                }
            } else if (state.editCoverImageUrl != null) {
                Box(modifier = Modifier.fillMaxWidth().height(130.dp).clip(RoundedCornerShape(10.dp))) {
                    coil.compose.AsyncImage(
                        model = state.editCoverImageUrl, contentDescription = null,
                        contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize()
                    )
                    // Değiştir butonu
                    Row(
                        modifier = Modifier.align(Alignment.TopEnd).padding(8.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier.size(30.dp).clip(CircleShape).background(Color(0x99000000))
                                .clickable { coverLauncher.launch("image/*") },
                            contentAlignment = Alignment.Center
                        ) { Icon(Icons.Default.Edit, null, tint = Color.White, modifier = Modifier.size(14.dp)) }
                        Box(
                            modifier = Modifier.size(30.dp).clip(CircleShape).background(Color(0x99000000))
                                .clickable { vm.clearEditCoverImage() },
                            contentAlignment = Alignment.Center
                        ) { Icon(Icons.Default.Close, null, tint = Color.White, modifier = Modifier.size(14.dp)) }
                    }
                }
            } else {
                Box(
                    modifier = Modifier.fillMaxWidth().height(90.dp).clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFFF2F4F5))
                        .border(1.dp, androidx.compose.ui.graphics.Brush.linearGradient(listOf(Color(0xFFDDDDDD), Color(0x4D418765))), RoundedCornerShape(10.dp))
                        .clickable { coverLauncher.launch("image/*") },
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.AddPhotoAlternate, null, tint = FenGreen, modifier = Modifier.size(24.dp))
                        Spacer(Modifier.height(4.dp))
                        Text("Kapak Görseli Ekle", color = FenGreen, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                    }
                }
            }

            // Ek görseller
            Text("Ek Görseller (${state.editAdditionalImages.size}/10)", color = TextSecondary, fontSize = 12.sp)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                itemsIndexed(state.editAdditionalImages) { index, url ->
                    Box(modifier = Modifier.size(100.dp).clip(RoundedCornerShape(10.dp))) {
                        coil.compose.AsyncImage(
                            model = url, contentDescription = null,
                            contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize()
                        )
                        Box(
                            modifier = Modifier.align(Alignment.TopEnd).padding(4.dp)
                                .size(22.dp).clip(CircleShape).background(Color(0xA6000000))
                                .clickable { vm.removeEditAdditionalImage(index) },
                            contentAlignment = Alignment.Center
                        ) { Icon(Icons.Default.Close, null, tint = Color.White, modifier = Modifier.size(11.dp)) }
                    }
                }
                if (state.isUploadingEditAdditional) {
                    item {
                        Box(modifier = Modifier.size(100.dp).clip(RoundedCornerShape(10.dp)).background(Color(0xFFF2F4F5)),
                            contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = FenGreen, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                        }
                    }
                }
                if (state.editAdditionalImages.size < 10 && !state.isUploadingEditAdditional) {
                    item {
                        Box(
                            modifier = Modifier.size(100.dp).clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFFF2F4F5))
                                .border(1.dp, Color(0x66418765), RoundedCornerShape(10.dp))
                                .clickable { additionalLauncher.launch("image/*") },
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.Add, null, tint = FenGreen, modifier = Modifier.size(20.dp))
                                Text("Ekle", color = FenGreen, fontSize = 10.sp, fontWeight = FontWeight.Medium)
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            // Kaydet / İptal
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick  = onDismiss,
                    modifier = Modifier.weight(1f).height(46.dp),
                    shape    = RoundedCornerShape(10.dp),
                    border   = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFDDDDDD)),
                    colors   = ButtonDefaults.outlinedButtonColors(contentColor = TextSecondary)
                ) { Text("İptal", fontSize = 14.sp) }

                Button(
                    onClick  = vm::saveEdit,
                    enabled  = !state.isSaving,
                    modifier = Modifier.weight(1f).height(46.dp),
                    shape    = RoundedCornerShape(10.dp),
                    colors   = ButtonDefaults.buttonColors(containerColor = FenGreen)
                ) {
                    if (state.isSaving)
                        CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp, color = Color.White)
                    else {
                        Icon(Icons.Default.Save, null, modifier = Modifier.size(16.dp), tint = Color.White)
                        Spacer(Modifier.width(6.dp))
                        Text("Kaydet", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

@Composable
private fun EditField(
    value: String,
    onChange: (String) -> Unit,
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    minLines: Int = 1
) {
    OutlinedTextField(
        value          = value,
        onValueChange  = onChange,
        label          = { Text(label, fontSize = 11.sp) },
        leadingIcon    = { Icon(icon, null, tint = FenGreen, modifier = Modifier.size(18.dp)) },
        modifier       = Modifier.fillMaxWidth().then(if (minLines > 1) Modifier.heightIn(min = (minLines * 52).dp) else Modifier),
        shape          = RoundedCornerShape(10.dp),
        minLines       = minLines,
        textStyle      = androidx.compose.ui.text.TextStyle(fontSize = 13.sp, color = TextPrimary),
        colors         = OutlinedTextFieldDefaults.colors(
            focusedBorderColor    = FenGreen,
            unfocusedBorderColor  = Color(0xFFDDDDDD),
            focusedContainerColor = Color(0xFFFFFFFF),
            unfocusedContainerColor = Color(0xFFFFFFFF),
            focusedLabelColor     = FenGreen,
            unfocusedLabelColor   = TextSecondary,
            cursorColor           = FenGreen
        )
    )
}

// ─────────────────────────────────────────────────────────────────────────────
// Medya bölümü
// ─────────────────────────────────────────────────────────────────────────────
@OptIn(UnstableApi::class)
@Composable
private fun MediaSection(
    exp: ExperimentDetail,
    exoPlayer: ExoPlayer?,
    isFavorited: Boolean,
    isOwner: Boolean,
    onBack: () -> Unit,
    onFavorite: () -> Unit,
    onDelete: () -> Unit,
    onEdit: () -> Unit,        // ← YENİ parametre
    onFullscreen: () -> Unit
) {
    val videoUrl  = exp.videoMedia?.mediaUrl
    val allImages = exp.imageMediaList.map { it.mediaUrl }
    val hasVideo  = videoUrl != null
    val pageCount = (if (hasVideo) 1 else 0) + allImages.size

    var currentPage      by remember { mutableIntStateOf(0) }
    var isVideoPlaying   by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showMenu         by remember { mutableStateOf(false) }
    var showImageViewer  by remember { mutableStateOf(false) }
    var viewerStartPage  by remember { mutableIntStateOf(0) }

    val isVideoPage  = hasVideo && currentPage == 0
    val playerHeight = if (isVideoPlaying && isVideoPage) 280.dp else 240.dp

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            containerColor   = Color(0xFFFFFFFF),
            title  = { Text("Deneyi Sil", color = TextPrimary, fontWeight = FontWeight.Bold) },
            text   = { Text("Bu deneyi kalıcı olarak silmek istediğinizden emin misiniz?", color = TextSecondary, fontSize = 13.sp) },
            confirmButton  = {
                TextButton(onClick = { showDeleteDialog = false; onDelete() }) {
                    Text("Evet, Sil", color = Red400, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton  = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("İptal", color = TextSecondary) }
            }
        )
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(16f / 9f)
            .background(Color(0xFFF5F5F5))
            .pointerInput(pageCount, isVideoPlaying) {
                if (pageCount <= 1 || (isVideoPlaying && isVideoPage)) return@pointerInput
                var totalDrag = 0f
                detectDragGestures(
                    onDragStart  = { totalDrag = 0f },
                    onDragEnd    = {
                        if (totalDrag < -80f && currentPage < pageCount - 1) currentPage++
                        else if (totalDrag > 80f && currentPage > 0) currentPage--
                        totalDrag = 0f
                    },
                    onDragCancel = { totalDrag = 0f },
                    onDrag       = { _, offset -> totalDrag += offset.x }
                )
            }
    ) {
        if (isVideoPage && isVideoPlaying && exoPlayer != null) {
            AndroidView(
                factory = { ctx ->
                    PlayerView(ctx).apply {
                        player        = exoPlayer
                        useController = true
                        layoutParams  = FrameLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )
                        setShowNextButton(false); setShowPreviousButton(false)
                        setShowRewindButton(true); setShowFastForwardButton(true)
                        controllerAutoShow = true; controllerHideOnTouch = true
                    }
                },
                update   = { it.player = exoPlayer },
                modifier = Modifier.fillMaxSize()
            )
            Box(
                modifier = Modifier.statusBarsPadding().padding(10.dp).size(36.dp)
                    .clip(CircleShape).background(Color(0xA6000000))
                    .clickable { isVideoPlaying = false; exoPlayer.pause() }
                    .align(Alignment.TopStart),
                contentAlignment = Alignment.Center
            ) { Icon(Icons.Default.KeyboardArrowDown, null, tint = Color.White, modifier = Modifier.size(20.dp)) }

            Box(
                modifier = Modifier.statusBarsPadding().padding(10.dp).size(36.dp)
                    .clip(CircleShape).background(Color(0xA6000000))
                    .clickable { onFullscreen() }
                    .align(Alignment.TopEnd),
                contentAlignment = Alignment.Center
            ) { Icon(Icons.Default.Fullscreen, null, tint = Color.White, modifier = Modifier.size(20.dp)) }

        } else {
            val displayUrl = when {
                isVideoPage -> allImages.firstOrNull()
                else        -> allImages.getOrNull(if (hasVideo) currentPage - 1 else currentPage)
            }
            val imageIndex = if (hasVideo) currentPage - 1 else currentPage

            // Resim alanı — gradient yok, direkt tıklanabilir
            Box(
                modifier = Modifier.fillMaxSize()
                    .background(Color(0xFFFFFFFF))
            ) {
                if (displayUrl != null) {
                    AsyncImage(
                        model              = displayUrl,
                        contentDescription = null,
                        contentScale       = ContentScale.Crop,
                        modifier           = Modifier
                            .fillMaxSize()
                            .clickable {
                                if (!isVideoPage && allImages.isNotEmpty()) {
                                    viewerStartPage = imageIndex.coerceIn(0, allImages.size - 1)
                                    showImageViewer = true
                                }
                            }
                    )
                }
            }

            // Gradient sadece üst ve alt şeritlerde (butonların arkasında) — resmin üstüne değil
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
                    .align(Alignment.TopStart)
                    .background(
                        Brush.verticalGradient(listOf(Color(0x66000000), Color.Transparent))
                    )
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .align(Alignment.BottomStart)
                    .background(
                        Brush.verticalGradient(listOf(Color.Transparent, Color(0x55000000)))
                    )
            )

            if (isVideoPage) {
                Box(
                    modifier = Modifier.size(64.dp).clip(CircleShape)
                        .background(Color(0x80000000))
                        .border(2.dp, Color(0x99FFFFFF), CircleShape)
                        .clickable { isVideoPlaying = true; exoPlayer?.playWhenReady = true }
                        .align(Alignment.Center),
                    contentAlignment = Alignment.Center
                ) { Icon(Icons.Default.PlayArrow, null, tint = Color.White, modifier = Modifier.size(32.dp)) }

                Box(
                    modifier = Modifier.align(Alignment.BottomStart).padding(10.dp)
                        .background(Color(0x99000000), RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) { Text("▶ Video", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Medium) }
            }

            if (pageCount > 1) {
                Row(
                    modifier              = Modifier.align(Alignment.BottomCenter).padding(bottom = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    repeat(pageCount) { i ->
                        Box(modifier = Modifier
                            .size(if (i == currentPage) 8.dp else 5.dp)
                            .clip(CircleShape)
                            .background(if (i == currentPage) FenGreen else Color(0x80FFFFFF)))
                    }
                }
            }

            // Sol üst: Geri
            Box(
                modifier = Modifier.statusBarsPadding().padding(10.dp).size(38.dp)
                    .clip(CircleShape).background(Color(0x73000000))
                    .clickable(onClick = onBack).align(Alignment.TopStart),
                contentAlignment = Alignment.Center
            ) { Icon(Icons.Default.ArrowBackIosNew, null, tint = Color.White, modifier = Modifier.size(16.dp)) }

            // Sağ üst: Favori + 3 nokta
            Row(
                modifier = Modifier
                    .statusBarsPadding()
                    .padding(top = 10.dp, end = 10.dp)
                    .align(Alignment.TopEnd),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment     = Alignment.CenterVertically
            ) {
                // Favori butonu — owner ise soluk göster
                AnimatedFavoriteButton(
                    isFavorited = isFavorited,
                    enabled = !isOwner,
                    backgroundColor = Color(0x73000000),
                    iconWhenNotFavorited = if (isOwner) Color(0x66FFFFFF) else Color.White,
                    onClick = onFavorite
                )

                Box {
                    Box(
                        modifier = Modifier.size(38.dp).clip(CircleShape)
                            .background(Color(0x73000000)).clickable { showMenu = true },
                        contentAlignment = Alignment.Center
                    ) { Icon(Icons.Default.MoreVert, null, tint = Color.White, modifier = Modifier.size(18.dp)) }

                    DropdownMenu(
                        expanded         = showMenu,
                        onDismissRequest = { showMenu = false },
                        modifier         = Modifier.background(Color(0xFFFFFFFF))
                    ) {
                        DropdownMenuItem(
                            text        = { Text("Paylaş", color = TextPrimary, fontSize = 13.sp) },
                            leadingIcon = { Icon(Icons.Default.Share, null, tint = TextSecondary, modifier = Modifier.size(16.dp)) },
                            onClick     = { showMenu = false }
                        )
                        if (isOwner) {
                            HorizontalDivider(thickness = 0.5.dp, color = Color(0xFFDDDDDD))
                            // ── YENİ: Düzenle seçeneği ───────────────────────
                            DropdownMenuItem(
                                text        = { Text("Deneyi Düzenle", color = FenGreen, fontSize = 13.sp, fontWeight = FontWeight.SemiBold) },
                                leadingIcon = { Icon(Icons.Default.Edit, null, tint = FenGreen, modifier = Modifier.size(16.dp)) },
                                onClick     = { showMenu = false; onEdit() }
                            )
                            HorizontalDivider(thickness = 0.5.dp, color = Color(0xFFDDDDDD))
                            DropdownMenuItem(
                                text        = { Text("Deneyi Sil", color = Red400, fontSize = 13.sp, fontWeight = FontWeight.SemiBold) },
                                leadingIcon = { Icon(Icons.Default.DeleteOutline, null, tint = Red400, modifier = Modifier.size(16.dp)) },
                                onClick     = { showMenu = false; showDeleteDialog = true }
                            )
                        }
                    }
                }
            }
        }
    }

    // ── Tam ekran resim görüntüleyici (Dialog — gerçekten tam ekran) ──────────
    if (showImageViewer && allImages.isNotEmpty()) {
        var galPage by remember(viewerStartPage) { mutableIntStateOf(viewerStartPage) }
        BackHandler { showImageViewer = false }

        Dialog(
            onDismissRequest = { showImageViewer = false },
            properties = DialogProperties(
                usePlatformDefaultWidth = false,
                dismissOnClickOutside   = false,
                dismissOnBackPress      = true
            )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black)
                    .pointerInput(allImages.size) {
                        var totalDrag = 0f
                        detectDragGestures(
                            onDragStart  = { totalDrag = 0f },
                            onDragEnd    = {
                                if (totalDrag < -80f && galPage < allImages.size - 1) galPage++
                                else if (totalDrag > 80f && galPage > 0) galPage--
                                totalDrag = 0f
                            },
                            onDragCancel = { totalDrag = 0f },
                            onDrag       = { _, offset -> totalDrag += offset.x }
                        )
                    }
            ) {
                // Resim — tam ekran, oranı koruyarak
                AsyncImage(
                    model              = allImages.getOrNull(galPage),
                    contentDescription = null,
                    contentScale       = ContentScale.Fit,
                    modifier           = Modifier
                        .fillMaxSize()
                        .align(Alignment.Center)
                )

                // Kapat butonu (sol üst)
                Box(
                    modifier = Modifier
                        .statusBarsPadding()
                        .padding(14.dp)
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(Color(0xB3000000))
                        .clickable { showImageViewer = false }
                        .align(Alignment.TopStart),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Close, null, tint = Color.White, modifier = Modifier.size(20.dp))
                }

                // Sayfa noktaları + sayaç (birden fazla resim varsa)
                if (allImages.size > 1) {
                    Column(
                        modifier            = Modifier.align(Alignment.BottomCenter).navigationBarsPadding().padding(bottom = 20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            "${galPage + 1} / ${allImages.size}",
                            color      = Color.White,
                            fontSize   = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                            repeat(allImages.size) { i ->
                                Box(
                                    modifier = Modifier
                                        .size(if (i == galPage) 8.dp else 5.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (i == galPage) FenGreen else Color(0x80FFFFFF)
                                        )
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
// Info Section
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun InfoSection(
    exp: ExperimentDetail,
    currentRating: Int?,
    isOwner: Boolean,              // ← YENİ parametre
    isPdfLoading: Boolean,
    onAuthorClick: (Long) -> Unit = {},
    onRate: (Int) -> Unit,
    onDownloadPdf: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp)) {
        Text(exp.title, color = TextPrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold, lineHeight = 26.sp)
        Spacer(Modifier.height(6.dp))

        Row(
            modifier = Modifier.fillMaxWidth().clickable { onAuthorClick(exp.author.id) },
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(modifier = Modifier.size(28.dp).clip(CircleShape).background(FenGreen.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center) {
                if (exp.author.profileImageUrl != null)
                    AsyncImage(model = exp.author.profileImageUrl, contentDescription = null,
                        contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize().clip(CircleShape))
                else Text(exp.author.initials, color = FenGreen, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
            Text(exp.author.displayName, color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Medium)
            if (exp.author.isTeacher) Text("· Öğretmen", color = TextTertiary, fontSize = 12.sp)
            Spacer(Modifier.weight(1f))
            if (exp.averageRating != null) {
                Icon(Icons.Default.Star, null, tint = Orange400, modifier = Modifier.size(14.dp))
                Text("%.1f".format(exp.averageRating), color = Orange400, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.width(6.dp))
            }
            Icon(Icons.Default.Favorite, null, tint = Red400, modifier = Modifier.size(13.dp))
            Text(exp.favoriteCount.toString(), color = Red400, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
        }

        Spacer(Modifier.height(8.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            MetaTag("${exp.gradeLevel}. Sınıf")
            MetaTag(exp.displayDifficulty)
            if (exp.subject != null) MetaTag(exp.displaySubject)
            if (exp.environment != null) MetaTag(exp.displayEnvironment)
        }

        Spacer(Modifier.height(12.dp))

        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            // Yıldızlar — owner ise soluk ve tıklanamaz
            RatingBar(
                currentRating = currentRating,
                isOwner       = isOwner,
                onRate        = onRate
            )
            Spacer(Modifier.weight(1f))
            OutlinedButton(
                onClick  = onDownloadPdf,
                enabled  = !isPdfLoading,
                shape    = RoundedCornerShape(10.dp),
                border   = androidx.compose.foundation.BorderStroke(1.dp, FenGreen.copy(alpha = 0.5f)),
                colors   = ButtonDefaults.outlinedButtonColors(contentColor = FenGreen),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 0.dp),
                modifier = Modifier.height(36.dp)
            ) {
                if (isPdfLoading) CircularProgressIndicator(color = FenGreen, modifier = Modifier.size(14.dp), strokeWidth = 1.5.dp)
                else Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                    Icon(Icons.Default.PictureAsPdf, null, tint = FenGreen, modifier = Modifier.size(14.dp))
                    Text("PDF İndir", fontSize = 13.sp, fontWeight = FontWeight.Medium)
                }
            }
        }
    }
}

@Composable
private fun MetaTag(text: String) {
    Text(text = text, color = TextSecondary, fontSize = 11.sp,
        modifier = androidx.compose.ui.Modifier
            .background(Color(0xFFFFFFFF), RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 3.dp))
}

@Composable
private fun RatingBar(currentRating: Int?, isOwner: Boolean, onRate: (Int) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        Text("Puan ver:", color = if (isOwner) TextTertiary else TextSecondary, fontSize = 12.sp)
        Spacer(Modifier.width(4.dp))
        (1..5).forEach { star ->
            val filled  = (currentRating ?: 0) >= star
            Icon(
                imageVector = if (filled) Icons.Default.Star else Icons.Outlined.StarBorder,
                contentDescription = null,
                tint     = when {
                    isOwner -> Color(0xFFCCCCCC)                         // soluk
                    filled  -> Orange400
                    else    -> TextSecondary
                },
                modifier = Modifier.size(22.dp).then(
                    if (!isOwner) Modifier.clickable { onRate(star) } else Modifier
                )
            )
        }
    }
}

@Composable
private fun DetailTabBar(selected: Int, commentCount: Int, questionCount: Int, onSelect: (Int) -> Unit) {
    val tabs = listOf("Malzemeler", "Adımlar", "Yorum & S/C (${commentCount + questionCount})")
    Column(modifier = Modifier.fillMaxWidth()) {
        Row {
            tabs.forEachIndexed { i, label ->
                Column(modifier = Modifier.weight(1f).clickable { onSelect(i) },
                    horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(label, color = if (selected == i) FenGreen else TextSecondary, fontSize = 12.sp,
                        fontWeight = if (selected == i) FontWeight.SemiBold else FontWeight.Normal,
                        modifier = Modifier.padding(vertical = 11.dp), textAlign = TextAlign.Center)
                    Box(Modifier.fillMaxWidth().height(2.dp).background(if (selected == i) FenGreen else Color.Transparent))
                }
            }
        }
        HorizontalDivider(thickness = 0.5.dp, color = Color(0xFFDDDDDD))
    }
}
@Composable
private fun DescriptionCard(
    topic: String? = null,
    description: String,
    safetyNotes: String? = null,
    expectedResult: String? = null
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {

        if (!topic.isNullOrBlank()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFFF2F4F5))
                    .border(
                        1.dp,
                        Brush.horizontalGradient(listOf(Color(0xFFFFFFFF), Color.Transparent)),
                        RoundedCornerShape(10.dp)
                    )
                    .padding(12.dp)
            ) {
                Column {
                    Text(
                        "📌 KONU",
                        color = FenGreen,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        topic,
                        color = TextSecondary,
                        fontSize = 13.sp,
                        lineHeight = 19.sp
                    )
                }
            }

            Spacer(Modifier.height(8.dp))
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(Color(0xFFF2F4F5))
                .border(
                    1.dp,
                    Brush.horizontalGradient(listOf(Color(0xFFFFFFFF), Color.Transparent)),
                    RoundedCornerShape(10.dp)
                )
                .padding(12.dp)
        ) {
            Column {
                Text("📖 AÇIKLAMA", color = FenGreen, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                Spacer(Modifier.height(6.dp))
                Text(description, color = TextSecondary, fontSize = 13.sp, lineHeight = 19.sp)
            }
        }

        if (!safetyNotes.isNullOrBlank()) {
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFFF2F4F5))
                    .border(1.dp, Color(0xFFFFFFFF), RoundedCornerShape(10.dp))
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Column {
                    Text("⚠️ GÜVENLİK NOTU", color = FenGreen, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.8.sp)
                    Spacer(Modifier.height(3.dp))
                    Text(safetyNotes, color = TextSecondary, fontSize = 12.sp, lineHeight = 17.sp)
                }
            }
        }

        if (!expectedResult.isNullOrBlank()) {
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFFF2F4F5))
                    .border(1.dp, Color(0xFFFFFFFF), RoundedCornerShape(10.dp))
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Column {
                    Text("🎯 BEKLENEN SONUÇ", color = FenGreen, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.8.sp)
                    Spacer(Modifier.height(3.dp))
                    Text(expectedResult, color = TextSecondary, fontSize = 12.sp, lineHeight = 17.sp)
                }
            }
        }
    }
}

@Composable
private fun MaterialRow(index: Int, material: Material) {
    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp)
        .clip(RoundedCornerShape(10.dp)).background(Color(0xFFF2F4F5)).padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Box(modifier = Modifier.size(28.dp).background(Color(0xFFDDDDDD), CircleShape),
            contentAlignment = Alignment.Center) {
            Text("$index", color = TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
        Text(material.materialName, color = TextPrimary, fontSize = 13.sp, modifier = Modifier.weight(1f))
        if (material.quantity.isNotBlank())
            Text(material.quantity, color = FenGreen, fontSize = 12.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun StepRow(step: Step) {
    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 5.dp),
        verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Box(modifier = Modifier.size(28.dp).clip(CircleShape)
            .background(Brush.linearGradient(listOf(FenGreen, FenGreenDark))),
            contentAlignment = Alignment.Center) {
            Text("${step.stepOrder}", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
        Box(modifier = Modifier.weight(1f).clip(RoundedCornerShape(10.dp)).background(Color(0xFFF2F4F5)).padding(12.dp)) {
            Text(step.stepText, color = TextPrimary, fontSize = 13.sp, lineHeight = 19.sp)
        }
    }
}

@Composable
private fun CommentQuestionInput(
    commentInput: String, questionInput: String,
    onCommentChange: (String) -> Unit, onQuestionChange: (String) -> Unit,
    onAddComment: () -> Unit, onAskQuestion: () -> Unit
) {
    var isQuestionMode by remember { mutableStateOf(false) }
    val text     = if (isQuestionMode) questionInput else commentInput
    val onChange = if (isQuestionMode) onQuestionChange else onCommentChange
    Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp),
        shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFFFF))) {
        Column(Modifier.padding(12.dp)) {
            Row(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).background(Color(0xFFFFFFFF))) {
                listOf("💬 Yorum" to false, "❓ Soru" to true).forEach { (lbl, isQ) ->
                    Box(modifier = Modifier.weight(1f).clip(RoundedCornerShape(8.dp))
                        .background(if (isQuestionMode == isQ) FenGreen else Color.Transparent)
                        .clickable { isQuestionMode = isQ }.padding(vertical = 9.dp),
                        contentAlignment = Alignment.Center) {
                        Text(lbl, color = if (isQuestionMode == isQ) Color.White else TextSecondary,
                            fontSize = 12.sp, fontWeight = if (isQuestionMode == isQ) FontWeight.SemiBold else FontWeight.Normal)
                    }
                }
            }
            Spacer(Modifier.height(10.dp))
            TextField(value = text, onValueChange = onChange,
                placeholder = { Text(if (isQuestionMode) "Sorunuzu yazın..." else "Yorumunuzu yazın...", color = TextTertiary, fontSize = 12.sp) },
                modifier = Modifier.fillMaxWidth().height(80.dp), maxLines = 4, shape = RoundedCornerShape(8.dp),
                colors = TextFieldDefaults.colors(focusedContainerColor = Color(0xFFFFFFFF), unfocusedContainerColor = Color(0xFFFFFFFF),
                    focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary,
                    cursorColor = FenGreen, focusedIndicatorColor = FenGreen, unfocusedIndicatorColor = Color.Transparent))
            Spacer(Modifier.height(8.dp))
            Button(onClick = { if (isQuestionMode) onAskQuestion() else onAddComment() },
                modifier = Modifier.fillMaxWidth().height(40.dp), shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent), contentPadding = PaddingValues(0.dp)) {
                Box(modifier = Modifier.fillMaxSize()
                    .background(Brush.linearGradient(listOf(FenGreen, FenGreenDark)), RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center) {
                    Text(if (isQuestionMode) "Soruyu Gönder" else "Yorum Ekle",
                        color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
private fun CommentItem(comment: Comment,
                        canDelete: Boolean,
                        onDelete: () -> Unit) {
    var showConfirm by remember { mutableStateOf(false) }
    if (showConfirm) {
        AlertDialog(onDismissRequest = { showConfirm = false }, containerColor = Color(0xFFFFFFFF),
            title = { Text("Yorumu Sil", color = TextPrimary) },
            text  = { Text("Bu yorumu silmek istediğinizden emin misiniz?", color = TextSecondary, fontSize = 13.sp) },
            confirmButton = { TextButton(onClick = { showConfirm = false; onDelete() }) { Text("Sil", color = Red400, fontWeight = FontWeight.Bold) } },
            dismissButton = { TextButton(onClick = { showConfirm = false }) { Text("İptal", color = TextSecondary) } })
    }
    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 5.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        AuthorAvatar(initials = comment.author.initials, size = 34)
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(comment.author.displayName, color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                val formattedDate = remember(comment.createdAt) {
                    try {
                        java.time.LocalDateTime
                            .parse(comment.createdAt)
                            .format(
                                java.time.format.DateTimeFormatter.ofPattern("dd.MM.yyyy")
                            )
                    } catch (e: Exception) {
                        comment.createdAt.take(10)
                    }
                }

                Text(
                    text = formattedDate,
                    color = TextSecondary,
                    fontSize = 12.sp
                )
            }
            Spacer(Modifier.height(3.dp))
            Text(comment.content, color = TextSecondary, fontSize = 13.sp, lineHeight = 18.sp)
        }
        if (canDelete) {
            IconButton(onClick = { showConfirm = true }, modifier = Modifier.size(28.dp)) {
                Icon(Icons.Default.DeleteOutline, null, tint = Color(0xB3EF5350), modifier = Modifier.size(15.dp))
            }
        }
    }
    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp), thickness = 0.5.dp, color = Color(0xFFDDDDDD))
}

@Composable
private fun QuestionItem(question: Question, canDelete: Boolean = false, onAnswer: (String) -> Unit, onDelete: () -> Unit) {
    var answerInput      by remember { mutableStateOf("") }
    var showAnswerInput  by remember { mutableStateOf(false) }
    var showConfirm      by remember { mutableStateOf(false) }
    if (showConfirm) {
        AlertDialog(onDismissRequest = { showConfirm = false }, containerColor = Color(0xFFFFFFFF),
            title = { Text("Soruyu Sil", color = TextPrimary) },
            text  = { Text("Bu soruyu silmek istediğinizden emin misiniz?", color = TextSecondary, fontSize = 13.sp) },
            confirmButton = { TextButton(onClick = { showConfirm = false; onDelete() }) { Text("Sil", color = Red400, fontWeight = FontWeight.Bold) } },
            dismissButton = { TextButton(onClick = { showConfirm = false }) { Text("İptal", color = TextSecondary) } })
    }
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            AuthorAvatar(initials = question.asker.initials, size = 34)
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(question.asker.displayName, color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    Box(modifier = Modifier.background(Color(0x1464B5F6), RoundedCornerShape(8.dp)).padding(horizontal = 6.dp, vertical = 2.dp)) {
                        Text("Soru", color = FenGreen, fontSize = 11.sp)
                    }
                    Text(question.createdAt.take(10), color = TextSecondary, fontSize = 12.sp)
                }
                Spacer(Modifier.height(3.dp))
                Text(question.questionText, color = TextSecondary, fontSize = 13.sp, lineHeight = 18.sp)
                if (question.canAnswer && !question.isAnswered) {
                    Text("Yanıtla", color = FenGreen, fontSize = 11.sp,
                        modifier = Modifier.clickable { showAnswerInput = !showAnswerInput }.padding(top = 4.dp))
                }
            }
            if (canDelete) {
                IconButton(onClick = { showConfirm = true }, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Default.DeleteOutline, null, tint = Color(0xB3EF5350), modifier = Modifier.size(15.dp))
                }
            }
        }
        if (showAnswerInput) {
            Spacer(Modifier.height(8.dp))
            Row(modifier = Modifier.padding(start = 44.dp), horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically) {
                TextField(value = answerInput, onValueChange = { answerInput = it },
                    placeholder = { Text("Yanıtınızı yazın...", color = TextTertiary, fontSize = 12.sp) },
                    modifier = Modifier.weight(1f), shape = RoundedCornerShape(8.dp), singleLine = true,
                    colors = TextFieldDefaults.colors(focusedContainerColor = Color(0xFFFFFFFF), unfocusedContainerColor = Color(0xFFFFFFFF),
                        focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary,
                        cursorColor = FenGreen, focusedIndicatorColor = FenGreen, unfocusedIndicatorColor = Color.Transparent))
                IconButton(onClick = { if (answerInput.isNotBlank()) { onAnswer(answerInput); answerInput = ""; showAnswerInput = false } },
                    modifier = Modifier.size(38.dp).clip(CircleShape).background(FenGreen)) {
                    Icon(Icons.AutoMirrored.Filled.Send, null, tint = Color.White, modifier = Modifier.size(16.dp))
                }
            }
        }
        if (question.isAnswered && question.answerText != null) {
            Spacer(Modifier.height(8.dp))
            Row(modifier = Modifier.padding(start = 44.dp).fillMaxWidth()
                .clip(RoundedCornerShape(10.dp)).background(Color(0xFFF0F7F7))
                .border(1.dp, Color(0xFFB2DADA), RoundedCornerShape(10.dp)).padding(10.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.Top) {
                Icon(Icons.Default.School, null, tint = FenGreen, modifier = Modifier.size(15.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text("Öğretmen Yanıtı", color = FenGreen, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(2.dp))
                    Text(question.answerText, color = TextSecondary, fontSize = 12.sp, lineHeight = 17.sp)
                }
                if (canDelete) {
                    IconButton(onClick = { showConfirm = true }, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.DeleteOutline, null, tint = Color(0x80EF5350), modifier = Modifier.size(13.dp))
                    }
                }
            }
        }
    }
    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp), thickness = 0.5.dp, color = Color(0xFFDDDDDD))
}

@Composable
private fun AuthorAvatar(initials: String, size: Int) {
    Box(modifier = Modifier.size(size.dp).clip(CircleShape)
        .background(Brush.linearGradient(listOf(FenGreenLight, FenGreen))),
        contentAlignment = Alignment.Center) {
        Text(initials, color = Color.White, fontSize = (size / 3).sp, fontWeight = FontWeight.Bold)
    }
}
@Composable
private fun EditSectionHeader(title: String) {
    Text(title, color = FenGreen, fontSize = 13.sp, fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(top = 4.dp))
}

@Composable
private fun EditOutlinedField(
    value: String,
    onChange: (String) -> Unit,
    label: String,
    icon: ImageVector,
    minLines: Int = 1,
    modifier: Modifier
) {
    OutlinedTextField(
        value         = value,
        onValueChange = onChange,
        label         = { Text(label, fontSize = 11.sp) },
        leadingIcon   = { Icon(icon, null, tint = FenGreen, modifier = Modifier.size(18.dp)) },
        modifier      = Modifier.fillMaxWidth()
            .then(if (minLines > 1) Modifier.heightIn(min = (minLines * 52).dp) else Modifier),
        shape         = RoundedCornerShape(10.dp),
        minLines      = minLines,
        textStyle     = androidx.compose.ui.text.TextStyle(fontSize = 13.sp, color = TextPrimary),
        colors        = OutlinedTextFieldDefaults.colors(
            focusedBorderColor      = FenGreen,
            unfocusedBorderColor    = Color(0xFFDDDDDD),
            focusedContainerColor   = Color(0xFFFFFFFF),
            unfocusedContainerColor = Color(0xFFFFFFFF),
            focusedLabelColor       = FenGreen,
            unfocusedLabelColor     = TextSecondary,
            cursorColor             = FenGreen
        )
    )
}

@Composable
private fun <T> EditDropdown(
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
                .background(Color(0xFFF2F4F5))
                .border(1.dp, if (selected != null) Color(0x66418765) else Color.Transparent, RoundedCornerShape(10.dp))
                .clickable { expanded = true }
                .padding(horizontal = 12.dp, vertical = 11.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment     = Alignment.CenterVertically
        ) {
            Text(label, color = if (selected != null) TextPrimary else TextTertiary,
                fontSize = 12.sp, modifier = Modifier.weight(1f))
            Icon(Icons.Default.ExpandMore, null, tint = TextSecondary, modifier = Modifier.size(16.dp))
        }
        DropdownMenu(
            expanded = expanded, onDismissRequest = { expanded = false },
            modifier = Modifier.background(Color(0xFFF5F5F5))
        ) {
            items.forEach { item ->
                DropdownMenuItem(
                    text = { Text(display(item), color = if (item == selected) FenGreen else TextPrimary, fontSize = 12.sp) },
                    onClick = { onSelect(item); expanded = false },
                    trailingIcon = {
                        if (item == selected)
                            Icon(Icons.Default.Check, null, tint = FenGreen, modifier = Modifier.size(13.dp))
                    }
                )
            }
        }
    }
}