package com.nursena.fenlab_android.ui.screens.detail

import android.app.Activity
import android.content.pm.ActivityInfo
import android.net.Uri
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.activity.compose.BackHandler
import androidx.annotation.OptIn
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import coil.compose.AsyncImage
import com.nursena.fenlab_android.domain.model.*
import com.nursena.fenlab_android.ui.components.LoadingIndicator
import com.nursena.fenlab_android.ui.components.ErrorMessage
import com.nursena.fenlab_android.ui.theme.*

@OptIn(UnstableApi::class)
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

    // ExoPlayer — screen seviyesinde tutulur
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

    // Fullscreen state — screen seviyesinde
    var isFullscreen by remember { mutableStateOf(false) }

    fun enterFullscreen() {
        isFullscreen = true
        exoPlayer?.playWhenReady = true
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
        // Sistem çubuklarını gizle
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

    // TAM EKRAN OVERLAY — Scaffold'un üstünde, tüm ekranı kaplar
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
                        setShowNextButton(false)
                        setShowPreviousButton(false)
                        setShowRewindButton(true)
                        setShowFastForwardButton(true)
                        controllerAutoShow    = true
                        controllerHideOnTouch = true
                    }
                },
                update   = { it.player = exoPlayer },
                modifier = Modifier.fillMaxSize()
            )
            // Tam ekrandan çık
            IconButton(
                onClick  = { exitFullscreen() },
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .systemBarsPadding()
                    .padding(8.dp)
            ) {
                Box(
                    modifier = Modifier.size(40.dp).clip(CircleShape).background(Color(0xA6000000)),
                    contentAlignment = Alignment.Center
                ) { Icon(Icons.Default.FullscreenExit, null, tint = Color.White, modifier = Modifier.size(22.dp)) }
            }
        }
        return  // Normal içeriği render etme
    }

    // NORMAL EKRAN
    Scaffold(
        snackbarHost   = { SnackbarHost(snackbarHostState) },
        containerColor = Color.Transparent
    ) { padding ->
        when {
            uiState.isLoading && uiState.experiment == null -> LoadingIndicator()
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
                            exp            = exp,
                            exoPlayer      = exoPlayer,
                            isFavorited    = uiState.isFavorited,
                            isOwner        = uiState.isOwner,
                            onBack         = onBack,
                            onFavorite     = viewModel::toggleFavorite,
                            onDelete       = { viewModel.deleteExperiment(onBack) },
                            onFullscreen   = { enterFullscreen() }
                        )
                    }
                    item {
                        InfoSection(
                            exp           = exp,
                            currentRating = uiState.currentUserRating,
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
                    item { DescriptionCard(description = exp.description, safetyNotes = exp.safetyNotes, expectedResult = exp.expectedResult) }
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
                                CommentItem(comment = comment, onDelete = { viewModel.deleteComment(comment.id) })
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
}

// ─────────────────────────────────────────────────────────────────────────────
// Medya bölümü (inline video + galeri)
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

    val isVideoPage    = hasVideo && currentPage == 0
    val playerHeight   = if (isVideoPlaying && isVideoPage) 280.dp else 240.dp

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            containerColor = Color(0xFFF8F9FB),
            title = { Text("Deneyi Sil", color = TextPrimary, fontWeight = FontWeight.Bold) },
            text  = { Text("Bu deneyi kalıcı olarak silmek istediğinizden emin misiniz?", color = TextSecondary, fontSize = 13.sp) },
            confirmButton = {
                TextButton(onClick = { showDeleteDialog = false; onDelete() }) {
                    Text("Evet, Sil", color = Red400, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("İptal", color = TextSecondary) }
            }
        )
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(playerHeight)
            .background(Color.Black)
            .pointerInput(pageCount, isVideoPlaying) {
                if (pageCount <= 1 || (isVideoPlaying && isVideoPage)) return@pointerInput
                detectHorizontalDragGestures { _, drag ->
                    if (drag < -40f && currentPage < pageCount - 1) currentPage++
                    else if (drag > 40f && currentPage > 0) currentPage--
                }
            }
    ) {
        // ── Video inline player ──────────────────────────────────────────────
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
                        setShowNextButton(false)
                        setShowPreviousButton(false)
                        setShowRewindButton(true)
                        setShowFastForwardButton(true)
                        controllerAutoShow    = true
                        controllerHideOnTouch = true
                    }
                },
                update   = { it.player = exoPlayer },
                modifier = Modifier.fillMaxSize()
            )

            // Sol üst — videoyu durdur (← GERI butonu yok, çakışma yok)
            Box(
                modifier = Modifier.statusBarsPadding().padding(10.dp).size(36.dp)
                    .clip(CircleShape).background(Color(0xA6000000))
                    .clickable { isVideoPlaying = false; exoPlayer.pause() }
                    .align(Alignment.TopStart),
                contentAlignment = Alignment.Center
            ) { Icon(Icons.Default.KeyboardArrowDown, null, tint = Color.White, modifier = Modifier.size(20.dp)) }

            // Sağ üst — tam ekran
            Box(
                modifier = Modifier.statusBarsPadding().padding(10.dp).size(36.dp)
                    .clip(CircleShape).background(Color(0xA6000000))
                    .clickable { onFullscreen() }
                    .align(Alignment.TopEnd),
                contentAlignment = Alignment.Center
            ) { Icon(Icons.Default.Fullscreen, null, tint = Color.White, modifier = Modifier.size(20.dp)) }

        } else {
            // ── Kapak / Resim galerisi ───────────────────────────────────────
            val displayUrl = when {
                isVideoPage -> allImages.firstOrNull()
                else        -> allImages.getOrNull(if (hasVideo) currentPage - 1 else currentPage)
            }

            Box(modifier = Modifier.fillMaxSize()
                .background(Brush.linearGradient(listOf(Color(0xFFF8F9FB), Color(0xFFF8F9FB))))) {
                if (displayUrl != null) {
                    AsyncImage(model = displayUrl, contentDescription = null,
                        contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                }
            }

            Box(modifier = Modifier.fillMaxSize().background(
                Brush.verticalGradient(listOf(Color(0x59000000), Color.Transparent, Color(0x8C000000)))
            ))

            // Video oynat
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

            // Sayfa noktaları
            if (pageCount > 1) {
                Row(
                    modifier              = Modifier.align(Alignment.BottomCenter).padding(bottom = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    repeat(pageCount) { i ->
                        Box(modifier = Modifier
                            .size(if (i == currentPage) 8.dp else 5.dp)
                            .clip(CircleShape)
                            .background(if (i == currentPage) FrostAccent else Color(0x80FFFFFF)))
                    }
                }
            }

            // ── Üst butonlar (video OYNATILMIYOR) ───────────────────────────
            // Sol üst: Geri (video durdurulduğunda tekrar görünür)
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
                Box(
                    modifier = Modifier.size(38.dp).clip(CircleShape)
                        .background(Color(0x73000000)).clickable(onClick = onFavorite),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        if (isFavorited) Icons.Default.Favorite else Icons.Outlined.FavoriteBorder,
                        null,
                        tint     = if (isFavorited) Red400 else Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Box {
                    Box(
                        modifier = Modifier.size(38.dp).clip(CircleShape)
                            .background(Color(0x73000000)).clickable { showMenu = true },
                        contentAlignment = Alignment.Center
                    ) { Icon(Icons.Default.MoreVert, null, tint = Color.White, modifier = Modifier.size(18.dp)) }

                    DropdownMenu(
                        expanded         = showMenu,
                        onDismissRequest = { showMenu = false },
                        modifier         = Modifier.background(Color(0xFFF8F9FB))
                    ) {
                        DropdownMenuItem(
                            text        = { Text("Paylaş", color = TextPrimary, fontSize = 13.sp) },
                            leadingIcon = { Icon(Icons.Default.Share, null, tint = TextSecondary, modifier = Modifier.size(16.dp)) },
                            onClick     = { showMenu = false }
                        )
                        if (isOwner) {
                            HorizontalDivider(thickness = 0.5.dp, color = Color(0xFFCFD8DC))
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
}

// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun InfoSection(
    exp: ExperimentDetail, currentRating: Int?, isPdfLoading: Boolean,
    onAuthorClick: (Long) -> Unit = {},
    onRate: (Int) -> Unit, onDownloadPdf: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp)) {

        // ── Başlık ────────────────────────────────────────────────────────────
        Text(exp.title, color = TextPrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold, lineHeight = 26.sp)
        Spacer(Modifier.height(4.dp))

        // ── Meta: sınıf, zorluk, ders, ortam ─────────────────────────────────
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("${exp.gradeLevel}. Sınıf", color = TextSecondary, fontSize = 12.sp)
            Text("·", color = Color(0xFFCFD8DC), fontSize = 12.sp)
            Text(exp.displayDifficulty, color = TextSecondary, fontSize = 12.sp)
            if (exp.subject != null) {
                Text("·", color = Color(0xFFCFD8DC), fontSize = 12.sp)
                Text(exp.displaySubject, color = TextSecondary, fontSize = 12.sp)
            }
            if (exp.environment != null) {
                Text("·", color = Color(0xFFCFD8DC), fontSize = 12.sp)
                Text(exp.displayEnvironment, color = TextSecondary, fontSize = 12.sp)
            }
        }

        Spacer(Modifier.height(12.dp))

        // ── Yazar — tıklanabilir kart ─────────────────────────────────────────
        Row(
            modifier = Modifier.fillMaxWidth()
                .clip(RoundedCornerShape(10.dp)).background(Color(0xFFF8F9FB))
                .clickable { onAuthorClick(exp.author.id) }
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(modifier = Modifier.size(36.dp).clip(CircleShape)
                .background(Brush.linearGradient(listOf(Color(0x80F06292), Color(0x66EC407A)))),
                contentAlignment = Alignment.Center) {
                if (exp.author.profileImageUrl != null)
                    AsyncImage(model = exp.author.profileImageUrl, contentDescription = null,
                        contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize().clip(CircleShape))
                else Text(exp.author.initials, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(exp.author.displayName, color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                Text(if (exp.author.isTeacher) "Öğretmen" else "Kullanıcı", color = TextSecondary, fontSize = 11.sp)
            }
            // Puan + Beğeni
            Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(exp.averageRating?.let { "%.1f".format(it) } ?: "-",
                        color = if (exp.averageRating != null) Orange400 else TextSecondary, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    Text("Puan", color = TextSecondary, fontSize = 11.sp)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(exp.favoriteCount.toString(), color = Red400, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    Text("Beğeni", color = TextSecondary, fontSize = 11.sp)
                }
            }
        }

        // ── Konu etiketi ──────────────────────────────────────────────────────
        if (!exp.topic.isNullOrBlank()) {
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                Icon(Icons.Default.Tag, null, tint = TextSecondary, modifier = Modifier.size(13.dp))
                Text(exp.topic!!, color = TextSecondary, fontSize = 12.sp)
            }
        }

        Spacer(Modifier.height(14.dp))
        RatingBar(currentRating = currentRating, onRate = onRate)
        Spacer(Modifier.height(14.dp))

        Button(onClick = onDownloadPdf, enabled = !isPdfLoading,
            modifier = Modifier.fillMaxWidth().height(46.dp), shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent), contentPadding = PaddingValues(0.dp)) {
            Box(modifier = Modifier.fillMaxSize()
                .background(Brush.linearGradient(listOf(FrostAccent, FrostAccentDark)), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center) {
                if (isPdfLoading) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                else Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.PictureAsPdf, null, tint = Color.White, modifier = Modifier.size(18.dp))
                    Text("PDF Olarak İndir", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable private fun RatingBar(currentRating: Int?, onRate: (Int) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        Text("Puan ver:", color = TextSecondary, fontSize = 12.sp)
        Spacer(Modifier.width(4.dp))
        (1..5).forEach { star ->
            Icon(if ((currentRating ?: 0) >= star) Icons.Default.Star else Icons.Outlined.StarBorder, null,
                tint = if ((currentRating ?: 0) >= star) Orange400 else TextSecondary,
                modifier = Modifier.size(22.dp).clickable { onRate(star) })
        }
    }
}

@Composable private fun DetailTabBar(selected: Int, commentCount: Int, questionCount: Int, onSelect: (Int) -> Unit) {
    val tabs = listOf("Malzemeler", "Adımlar", "Yorum & S/C (${commentCount + questionCount})")
    Column(modifier = Modifier.fillMaxWidth()) {
        Row {
            tabs.forEachIndexed { i, label ->
                Column(modifier = Modifier.weight(1f).clickable { onSelect(i) }, horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(label, color = if (selected == i) FrostAccent else TextSecondary, fontSize = 12.sp,
                        fontWeight = if (selected == i) FontWeight.SemiBold else FontWeight.Normal,
                        modifier = Modifier.padding(vertical = 11.dp), textAlign = TextAlign.Center)
                    Box(Modifier.fillMaxWidth().height(2.dp).background(if (selected == i) FrostAccent else Color.Transparent))
                }
            }
        }
        HorizontalDivider(thickness = 0.5.dp, color = Color(0xFFCFD8DC))
    }
}

@Composable private fun DescriptionCard(description: String, safetyNotes: String? = null, expectedResult: String? = null) {
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp)) {
        // Açıklama
        Box(modifier = Modifier.fillMaxWidth()
            .clip(RoundedCornerShape(10.dp)).background(Color(0xFFF8F9FB))
            .border(1.dp, Brush.horizontalGradient(listOf(Color(0xFFF8F9FB), Color.Transparent)), RoundedCornerShape(10.dp))
            .padding(12.dp)) {
            Column {
                Text("AÇIKLAMA", color = FrostAccent, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                Spacer(Modifier.height(6.dp))
                Text(description, color = TextSecondary, fontSize = 13.sp, lineHeight = 19.sp)
            }
        }
        // Güvenlik notu
        if (!safetyNotes.isNullOrBlank()) {
            Spacer(Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp))
                .background(Color(0xFFF8F9FB)).border(1.dp, Color(0xFFF8F9FB), RoundedCornerShape(10.dp))
                .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(8.dp)) {

                Column {
                    Text("GÜVENLİK NOTU", color = FrostAccent, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.8.sp)
                    Spacer(Modifier.height(3.dp))
                    Text(safetyNotes, color = TextSecondary, fontSize = 12.sp, lineHeight = 17.sp)
                }
            }
        }
        // Beklenen sonuç
        if (!expectedResult.isNullOrBlank()) {
            Spacer(Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp))
                .background(Color(0xFFF8F9FB)).border(1.dp, Color(0xFFF8F9FB), RoundedCornerShape(10.dp))
                .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(8.dp)) {

                Column {
                    Text("BEKLENEN SONUÇ", color = FrostAccent, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.8.sp)
                    Spacer(Modifier.height(3.dp))
                    Text(expectedResult, color = TextSecondary, fontSize = 12.sp, lineHeight = 17.sp)
                }
            }
        }
    }
}

@Composable private fun MaterialRow(index: Int, material: Material) {
    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp)
        .clip(RoundedCornerShape(10.dp)).background(Color(0xFFF8F9FB)).padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Box(modifier = Modifier.size(28.dp).background(Color(0xFFCFD8DC), CircleShape), contentAlignment = Alignment.Center) {
            Text("$index", color = TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
        Text(material.materialName, color = TextPrimary, fontSize = 13.sp, modifier = Modifier.weight(1f))
        if (material.quantity.isNotBlank()) Text(material.quantity, color = FrostAccent, fontSize = 12.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable private fun StepRow(step: Step) {
    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 5.dp),
        verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Box(modifier = Modifier.size(28.dp).clip(CircleShape)
            .background(Brush.linearGradient(listOf(FrostAccent, FrostAccentDark))), contentAlignment = Alignment.Center) {
            Text("${step.stepOrder}", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
        Box(modifier = Modifier.weight(1f).clip(RoundedCornerShape(10.dp)).background(Color(0xFFF8F9FB)).padding(12.dp)) {
            Text(step.stepText, color = TextPrimary, fontSize = 13.sp, lineHeight = 19.sp)
        }
    }
}

@Composable private fun CommentQuestionInput(
    commentInput: String, questionInput: String,
    onCommentChange: (String) -> Unit, onQuestionChange: (String) -> Unit,
    onAddComment: () -> Unit, onAskQuestion: () -> Unit
) {
    var isQuestionMode by remember { mutableStateOf(false) }
    val text = if (isQuestionMode) questionInput else commentInput
    val onChange = if (isQuestionMode) onQuestionChange else onCommentChange
    Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp),
        shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F9FB))) {
        Column(Modifier.padding(12.dp)) {
            Row(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).background(Color(0xFFECEFF1))) {
                listOf("💬 Yorum" to false, "❓ Soru" to true).forEach { (lbl, isQ) ->
                    Box(modifier = Modifier.weight(1f).clip(RoundedCornerShape(8.dp))
                        .background(if (isQuestionMode == isQ) Color(0xFF64B5F6) else Color.Transparent)
                        .clickable { isQuestionMode = isQ }.padding(vertical = 9.dp),
                        contentAlignment = Alignment.Center) {
                        Text(lbl, color = if (isQuestionMode == isQ) Color.White else TextSecondary, fontSize = 12.sp,
                            fontWeight = if (isQuestionMode == isQ) FontWeight.SemiBold else FontWeight.Normal)
                    }
                }
            }
            Spacer(Modifier.height(10.dp))
            TextField(value = text, onValueChange = onChange,
                placeholder = { Text(if (isQuestionMode) "Sorunuzu yazın..." else "Yorumunuzu yazın...", color = TextTertiary, fontSize = 12.sp) },
                modifier = Modifier.fillMaxWidth().height(80.dp), maxLines = 4, shape = RoundedCornerShape(8.dp),
                colors = TextFieldDefaults.colors(focusedContainerColor = Color(0xFFECEFF1), unfocusedContainerColor = Color(0xFFECEFF1),
                    focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary,
                    cursorColor = FrostAccent, focusedIndicatorColor = FrostAccent, unfocusedIndicatorColor = Color.Transparent))
            Spacer(Modifier.height(8.dp))
            Button(onClick = { if (isQuestionMode) onAskQuestion() else onAddComment() },
                modifier = Modifier.fillMaxWidth().height(40.dp), shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent), contentPadding = PaddingValues(0.dp)) {
                Box(modifier = Modifier.fillMaxSize()
                    .background(Brush.linearGradient(listOf(FrostAccent, FrostAccentDark)), RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center) {
                    Text(if (isQuestionMode) "Soruyu Gönder" else "Yorum Ekle", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable private fun CommentItem(comment: Comment, onDelete: () -> Unit) {
    var showConfirm by remember { mutableStateOf(false) }
    if (showConfirm) {
        AlertDialog(onDismissRequest = { showConfirm = false }, containerColor = Color(0xFFF8F9FB),
            title = { Text("Yorumu Sil", color = TextPrimary) },
            text  = { Text("Bu yorumu silmek istediğinizden emin misiniz?", color = TextSecondary, fontSize = 13.sp) },
            confirmButton = { TextButton(onClick = { showConfirm = false; onDelete() }) { Text("Sil", color = Red400, fontWeight = FontWeight.Bold) } },
            dismissButton = { TextButton(onClick = { showConfirm = false }) { Text("İptal", color = TextSecondary) } })
    }
    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 5.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        AuthorAvatar(initials = comment.author.initials, size = 34)
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(comment.author.displayName, color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                Text(comment.createdAt.take(10), color = TextSecondary, fontSize = 12.sp)
            }
            Spacer(Modifier.height(3.dp))
            Text(comment.content, color = TextSecondary, fontSize = 13.sp, lineHeight = 18.sp)
        }
        if (comment.isOwner) {
            IconButton(onClick = { showConfirm = true }, modifier = Modifier.size(28.dp)) {
                Icon(Icons.Default.DeleteOutline, null, tint = Color(0xB3EF5350), modifier = Modifier.size(15.dp))
            }
        }
    }
    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp), thickness = 0.5.dp, color = Color(0xFFCFD8DC))
}

@Composable private fun QuestionItem(question: Question, canDelete: Boolean = false, onAnswer: (String) -> Unit, onDelete: () -> Unit) {
    var answerInput by remember { mutableStateOf("") }
    var showAnswerInput by remember { mutableStateOf(false) }
    var showConfirm by remember { mutableStateOf(false) }
    if (showConfirm) {
        AlertDialog(onDismissRequest = { showConfirm = false }, containerColor = Color(0xFFF8F9FB),
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
                    Box(modifier = Modifier.background(Color(0x26FFD54F), RoundedCornerShape(8.dp)).padding(horizontal = 6.dp, vertical = 2.dp)) {
                        Text("❓ Soru", color = Orange400, fontSize = 12.sp)
                    }
                    Text(question.createdAt.take(10), color = TextSecondary, fontSize = 12.sp)
                }
                Spacer(Modifier.height(3.dp))
                Text(question.questionText, color = TextSecondary, fontSize = 13.sp, lineHeight = 18.sp)
                if (question.canAnswer && !question.isAnswered) {
                    Text("Yanıtla", color = FrostAccent, fontSize = 11.sp,
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
            Row(modifier = Modifier.padding(start = 44.dp), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                TextField(value = answerInput, onValueChange = { answerInput = it },
                    placeholder = { Text("Yanıtınızı yazın...", color = TextTertiary, fontSize = 12.sp) },
                    modifier = Modifier.weight(1f), shape = RoundedCornerShape(8.dp), singleLine = true,
                    colors = TextFieldDefaults.colors(focusedContainerColor = Color(0xFFECEFF1), unfocusedContainerColor = Color(0xFFECEFF1),
                        focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary,
                        cursorColor = FrostAccent, focusedIndicatorColor = FrostAccent, unfocusedIndicatorColor = Color.Transparent))
                IconButton(onClick = { if (answerInput.isNotBlank()) { onAnswer(answerInput); answerInput = ""; showAnswerInput = false } },
                    modifier = Modifier.size(38.dp).clip(CircleShape).background(FrostAccent)) {
                    Icon(Icons.AutoMirrored.Filled.Send, null, tint = Color.White, modifier = Modifier.size(16.dp))
                }
            }
        }
        if (question.isAnswered && question.answerText != null) {
            Spacer(Modifier.height(8.dp))
            Row(modifier = Modifier.padding(start = 44.dp).clip(RoundedCornerShape(10.dp))
                .background(Color(0x14F06292)).border(1.dp, Color(0x33F06292), RoundedCornerShape(10.dp)).padding(10.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Default.School, null, tint = FrostAccent, modifier = Modifier.size(16.dp))
                Column {
                    Text("ÖĞRETMEN YANITI", color = TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp)
                    Spacer(Modifier.height(3.dp))
                    Text(question.answerText, color = TextSecondary, fontSize = 12.sp, lineHeight = 17.sp)
                }
            }
        }
    }
    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp), thickness = 0.5.dp, color = Color(0xFFCFD8DC))
}

@Composable private fun AuthorAvatar(initials: String, size: Int) {
    Box(modifier = Modifier.size(size.dp).clip(CircleShape)
        .background(Brush.linearGradient(listOf(Color(0x80F06292), Color(0x66EC407A)))),
        contentAlignment = Alignment.Center) {
        Text(initials, color = Color.White, fontSize = (size / 3).sp, fontWeight = FontWeight.Bold)
    }
}