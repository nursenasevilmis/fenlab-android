package com.nursena.fenlab_android.ui.screens.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.nursena.fenlab_android.domain.model.*
import com.nursena.fenlab_android.ui.components.LoadingIndicator
import com.nursena.fenlab_android.ui.components.ErrorMessage
import com.nursena.fenlab_android.ui.theme.*

@Composable
fun ExperimentDetailScreen(
    onBack: () -> Unit,
    viewModel: ExperimentDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(viewModel) {
        viewModel.eventFlow.collect { event ->
            if (event is com.nursena.fenlab_android.core.base.UiEvent.ShowSnackbar)
                snackbarHostState.showSnackbar(event.message)
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = DarkBg
    ) { padding ->
        when {
            uiState.isLoading -> LoadingIndicator()
            uiState.error != null -> ErrorMessage(message = uiState.error!!, onRetry = viewModel::loadExperiment)
            uiState.experiment != null -> {
                val exp = uiState.experiment!!
                LazyColumn(
                    modifier       = Modifier.fillMaxSize().padding(padding),
                    contentPadding = PaddingValues(bottom = 32.dp)
                ) {
                    item { MediaSection(exp, uiState.isFavorited, onBack, viewModel::toggleFavorite) }
                    item { InfoSection(exp, uiState.currentUserRating, viewModel::rateExperiment, viewModel::downloadPdf) }
                    item {
                        DetailTabBar(
                            selected      = uiState.selectedTab,
                            commentCount  = uiState.comments.size,
                            questionCount = uiState.questions.size,
                            onSelect      = viewModel::selectTab
                        )
                    }
                    item { DescriptionCard(description = exp.description) }

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
                                    question = question,
                                    onAnswer = { text -> viewModel.answerQuestion(question.id, text) },
                                    onDelete = { viewModel.deleteQuestion(question.id) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// ── Media bölümü ─────────────────────────────────────────────────────────────
@Composable
private fun MediaSection(
    exp: ExperimentDetail,
    isFavorited: Boolean,
    onBack: () -> Unit,
    onFavorite: () -> Unit
) {
    Box(modifier = Modifier.fillMaxWidth().height(210.dp).background(DarkSurface)) {
        val imageUrl = exp.videoMedia?.mediaUrl ?: exp.imageMediaList.firstOrNull()?.mediaUrl
        AsyncImage(
            model = imageUrl, contentDescription = exp.title,
            contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize()
        )
        Box(
            modifier = Modifier.fillMaxSize().background(
                Brush.verticalGradient(listOf(Color.Black.copy(0.35f), Color.Transparent, Color.Black.copy(0.5f)))
            )
        )
        // Geri
        Box(
            modifier = Modifier.statusBarsPadding().padding(10.dp).size(34.dp)
                .clip(CircleShape).background(Color.Black.copy(0.38f))
                .clickable(onClick = onBack).align(Alignment.TopStart),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.ArrowBackIosNew, null, tint = Color.White, modifier = Modifier.size(14.dp))
        }
        // Favori
        Box(
            modifier = Modifier.statusBarsPadding().padding(10.dp).size(34.dp)
                .clip(CircleShape).background(Color.Black.copy(0.38f))
                .clickable(onClick = onFavorite).align(Alignment.TopEnd),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                if (isFavorited) Icons.Default.Favorite else Icons.Outlined.FavoriteBorder,
                null,
                tint = if (isFavorited) Red400 else Color.White,
                modifier = Modifier.size(16.dp)
            )
        }
        // Play
        if (exp.videoMedia != null) {
            Box(
                modifier = Modifier.size(46.dp).clip(CircleShape)
                    .background(Color.White.copy(0.18f))
                    .border(1.5.dp, Color.White.copy(0.4f), CircleShape)
                    .align(Alignment.Center),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.PlayArrow, null, tint = Color.White, modifier = Modifier.size(24.dp))
            }
        }
    }
}

// ── Başlık + yazar + etiket + puan ───────────────────────────────────────────
@Composable
private fun InfoSection(
    exp: ExperimentDetail,
    currentRating: Int?,
    onRate: (Int) -> Unit,
    onDownloadPdf: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth().background(DarkBg)
            .padding(horizontal = 14.dp, vertical = 12.dp)
    ) {
        // Başlık
        Text(exp.title, color = TextPrimary, fontSize = 17.sp, fontWeight = FontWeight.Bold, lineHeight = 23.sp)
        Spacer(Modifier.height(10.dp))

        // Yazar satırı — avatar küçük (28dp)
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier.size(28.dp).clip(CircleShape)
                    .background(Brush.linearGradient(listOf(Teal400.copy(0.5f), Teal500.copy(0.4f)))),
                contentAlignment = Alignment.Center
            ) {
                if (exp.author.profileImageUrl != null) {
                    AsyncImage(
                        model = exp.author.profileImageUrl, contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize().clip(CircleShape)
                    )
                } else {
                    Text(exp.author.initials, color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                }
            }
            Spacer(Modifier.width(7.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(exp.author.displayName, color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                if (exp.author.isTeacher)
                    Text("Öğretmen", color = TextSecondary, fontSize = 10.sp)
            }
            // Puan + beğeni
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        exp.averageRating?.let { "%.1f".format(it) } ?: "-",
                        color = if (exp.averageRating != null) Orange400 else TextSecondary,
                        fontSize = 13.sp, fontWeight = FontWeight.Bold
                    )
                    Text("Puan", color = TextSecondary, fontSize = 9.sp)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(exp.favoriteCount.toString(), color = Red400, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    Text("Beğeni", color = TextSecondary, fontSize = 9.sp)
                }
            }
        }

        Spacer(Modifier.height(10.dp))

        // Chip'ler
        @OptIn(ExperimentalLayoutApi::class)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(5.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
            DetailChip(exp.displayDifficulty)
            if (exp.subject != null) DetailChip(exp.displaySubject, isSubject = true)
            if (exp.environment != null) DetailChip(exp.displayEnvironment)
            exp.topic?.takeIf { it.isNotBlank() }?.let { DetailChip(it) }
            if (exp.safetyNotes?.isNotBlank() == true) DetailChip("⚠️ Güvenlik")
        }

        Spacer(Modifier.height(12.dp))

        // Yıldız puanlama
        RatingBar(currentRating = currentRating, onRate = onRate)

        Spacer(Modifier.height(12.dp))

        // PDF butonu
        Button(
            onClick = onDownloadPdf,
            modifier = Modifier.fillMaxWidth().height(40.dp),
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
            contentPadding = PaddingValues(0.dp)
        ) {
            Box(
                modifier = Modifier.fillMaxSize()
                    .background(Brush.linearGradient(listOf(Teal400, Color(0xFF00A896))), RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Icon(Icons.Default.PictureAsPdf, null, tint = DarkBg, modifier = Modifier.size(15.dp))
                    Text("PDF Olarak İndir", color = DarkBg, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
private fun DetailChip(label: String, isSubject: Boolean = false) {
    Box(
        modifier = Modifier.clip(RoundedCornerShape(16.dp))
            .background(if (isSubject) Orange400.copy(alpha = 0.12f) else Teal400.copy(alpha = 0.1f))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(label, color = if (isSubject) Orange400 else Teal400, fontSize = 10.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun RatingBar(currentRating: Int?, onRate: (Int) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(3.dp)) {
        Text("Puan ver:", color = TextSecondary, fontSize = 11.sp)
        Spacer(Modifier.width(3.dp))
        (1..5).forEach { star ->
            Icon(
                if ((currentRating ?: 0) >= star) Icons.Default.Star else Icons.Outlined.StarBorder,
                null,
                tint = if ((currentRating ?: 0) >= star) Orange400 else TextSecondary,
                modifier = Modifier.size(20.dp).clickable { onRate(star) }
            )
        }
    }
}

// ── Tab bar ───────────────────────────────────────────────────────────────────
@Composable
private fun DetailTabBar(selected: Int, commentCount: Int, questionCount: Int, onSelect: (Int) -> Unit) {
    val tabs = listOf("Malzemeler", "Adımlar", "Yorumlar (${commentCount + questionCount})")
    Column(modifier = Modifier.fillMaxWidth().background(DarkBg)) {
        Row {
            tabs.forEachIndexed { i, label ->
                Column(
                    modifier = Modifier.weight(1f).clickable { onSelect(i) },
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        label,
                        color = if (selected == i) Teal400 else TextSecondary,
                        fontSize = 11.sp,
                        fontWeight = if (selected == i) FontWeight.SemiBold else FontWeight.Normal,
                        modifier = Modifier.padding(vertical = 9.dp),
                        textAlign = TextAlign.Center
                    )
                    Box(Modifier.fillMaxWidth().height(2.dp).background(if (selected == i) Teal400 else Color.Transparent))
                }
            }
        }
        HorizontalDivider(thickness = 0.5.dp, color = DarkSurface3)
    }
}

// ── Açıklama ──────────────────────────────────────────────────────────────────
@Composable
private fun DescriptionCard(description: String) {
    Box(
        modifier = Modifier.fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 8.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(DarkSurface)
            .border(1.dp, Brush.horizontalGradient(listOf(Teal400.copy(0.4f), Color.Transparent)), RoundedCornerShape(10.dp))
            .padding(11.dp)
    ) {
        Column {
            Text("AÇIKLAMA", color = Teal400, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.8.sp)
            Spacer(Modifier.height(5.dp))
            Text(description, color = TextSecondary, fontSize = 12.sp, lineHeight = 18.sp)
        }
    }
}

// ── Malzeme satırı ────────────────────────────────────────────────────────────
@Composable
private fun MaterialRow(index: Int, material: Material) {
    Row(
        modifier = Modifier.fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 3.dp)
            .clip(RoundedCornerShape(9.dp))
            .background(DarkSurface)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(
            modifier = Modifier.size(24.dp).background(DarkSurface3, CircleShape),
            contentAlignment = Alignment.Center
        ) { Text("$index", color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold) }
        Text(material.materialName, color = TextPrimary, fontSize = 12.sp, modifier = Modifier.weight(1f))
        if (material.quantity.isNotBlank())
            Text(material.quantity, color = Teal400, fontSize = 11.sp, fontWeight = FontWeight.Medium)
    }
}

// ── Adım satırı ───────────────────────────────────────────────────────────────
@Composable
private fun StepRow(step: Step) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 4.dp),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(
            modifier = Modifier.size(24.dp).clip(CircleShape)
                .background(Brush.linearGradient(listOf(Teal400, Color(0xFF00A896)))),
            contentAlignment = Alignment.Center
        ) { Text("${step.stepOrder}", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold) }
        Box(
            modifier = Modifier.weight(1f).clip(RoundedCornerShape(9.dp))
                .background(DarkSurface).padding(10.dp)
        ) {
            Text(step.stepText, color = TextPrimary, fontSize = 12.sp, lineHeight = 17.sp)
        }
    }
}

// ── Yorum & Soru input ────────────────────────────────────────────────────────
@Composable
private fun CommentQuestionInput(
    commentInput: String,
    questionInput: String,
    onCommentChange: (String) -> Unit,
    onQuestionChange: (String) -> Unit,
    onAddComment: () -> Unit,
    onAskQuestion: () -> Unit
) {
    var isQuestionMode by remember { mutableStateOf(false) }
    val text = if (isQuestionMode) questionInput else commentInput
    val onChange = if (isQuestionMode) onQuestionChange else onCommentChange

    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 8.dp),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface)
    ) {
        Column(Modifier.padding(10.dp)) {
            Text("Yorum veya Soru", color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(6.dp))
            TextField(
                value = text, onValueChange = onChange,
                placeholder = {
                    Text(
                        if (isQuestionMode) "Sorunuzu yazın..." else "Yorumunuzu yazın...",
                        color = Color(0xFF3D5070), fontSize = 11.sp
                    )
                },
                modifier = Modifier.fillMaxWidth().height(80.dp),
                maxLines = 4,
                shape = RoundedCornerShape(8.dp),
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
            Spacer(Modifier.height(7.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                OutlinedButton(
                    onClick = { isQuestionMode = false; if (!isQuestionMode) onAddComment() },
                    modifier = Modifier.weight(1f).height(36.dp),
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, if (!isQuestionMode) Teal400 else DarkSurface3),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = if (!isQuestionMode) Teal400 else TextSecondary)
                ) { Text("💬 Yorum", fontSize = 11.sp) }
                Button(
                    onClick = { isQuestionMode = true; if (isQuestionMode) onAskQuestion() },
                    modifier = Modifier.weight(1f).height(36.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Teal400)
                ) { Text("❓ Soru Sor", color = DarkBg, fontSize = 11.sp) }
            }
        }
    }
}

// ── Yorum item ────────────────────────────────────────────────────────────────
@Composable
private fun CommentItem(comment: Comment, onDelete: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 5.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        AuthorAvatar(initials = comment.author.initials, size = 28)
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(comment.author.displayName, color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                Text(comment.createdAt.take(10), color = TextSecondary, fontSize = 9.sp)
            }
            Spacer(Modifier.height(2.dp))
            Text(comment.content, color = TextSecondary, fontSize = 12.sp, lineHeight = 17.sp)
        }
        if (comment.isOwner) {
            IconButton(onClick = onDelete, modifier = Modifier.size(24.dp)) {
                Icon(Icons.Default.DeleteOutline, null, tint = Red400.copy(0.7f), modifier = Modifier.size(13.dp))
            }
        }
    }
    HorizontalDivider(modifier = Modifier.padding(horizontal = 14.dp, vertical = 3.dp), thickness = 0.5.dp, color = DarkSurface3)
}

// ── Soru item ─────────────────────────────────────────────────────────────────
@Composable
private fun QuestionItem(question: Question, onAnswer: (String) -> Unit, onDelete: () -> Unit) {
    var answerInput by remember { mutableStateOf("") }
    var showAnswerInput by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 5.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            AuthorAvatar(initials = question.asker.initials, size = 28)
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(question.asker.displayName, color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    Box(
                        modifier = Modifier.background(Orange400.copy(0.12f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 5.dp, vertical = 2.dp)
                    ) { Text("Soru", color = Orange400, fontSize = 9.sp) }
                    Text(question.createdAt.take(10), color = TextSecondary, fontSize = 9.sp)
                }
                Spacer(Modifier.height(2.dp))
                Text(question.questionText, color = TextSecondary, fontSize = 12.sp, lineHeight = 17.sp)
                if (question.canAnswer && !question.isAnswered) {
                    Text("Yanıtla", color = Teal400, fontSize = 10.sp,
                        modifier = Modifier.clickable { showAnswerInput = !showAnswerInput }.padding(top = 3.dp))
                }
            }
        }

        if (showAnswerInput) {
            Spacer(Modifier.height(6.dp))
            Row(
                modifier = Modifier.padding(start = 36.dp),
                horizontalArrangement = Arrangement.spacedBy(7.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextField(
                    value = answerInput, onValueChange = { answerInput = it },
                    placeholder = { Text("Yanıtınızı yazın...", color = Color(0xFF3D5070), fontSize = 11.sp) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    singleLine = true,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = DarkSurface2, unfocusedContainerColor = DarkSurface2,
                        focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary,
                        cursorColor = Teal400, focusedIndicatorColor = Teal400, unfocusedIndicatorColor = Color.Transparent
                    )
                )
                IconButton(
                    onClick = { if (answerInput.isNotBlank()) { onAnswer(answerInput); answerInput = ""; showAnswerInput = false } },
                    modifier = Modifier.size(34.dp).clip(CircleShape).background(Teal400)
                ) { Icon(Icons.Default.Send, null, tint = DarkBg, modifier = Modifier.size(14.dp)) }
            }
        }

        if (question.isAnswered && question.answerText != null) {
            Spacer(Modifier.height(6.dp))
            Row(
                modifier = Modifier.padding(start = 36.dp).clip(RoundedCornerShape(9.dp))
                    .background(Teal400.copy(0.07f))
                    .border(1.dp, Teal400.copy(0.18f), RoundedCornerShape(9.dp))
                    .padding(9.dp),
                horizontalArrangement = Arrangement.spacedBy(7.dp)
            ) {
                Icon(Icons.Default.School, null, tint = Teal400, modifier = Modifier.size(13.dp))
                Column {
                    Text("ÖĞRETMEN YANITI", color = Teal400, fontSize = 9.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp)
                    Spacer(Modifier.height(2.dp))
                    Text(question.answerText, color = TextSecondary, fontSize = 11.sp, lineHeight = 16.sp)
                }
            }
        }
    }
    HorizontalDivider(modifier = Modifier.padding(horizontal = 14.dp, vertical = 3.dp), thickness = 0.5.dp, color = DarkSurface3)
}

// ── Avatar ────────────────────────────────────────────────────────────────────
@Composable
private fun AuthorAvatar(initials: String, size: Int) {
    Box(
        modifier = Modifier.size(size.dp).clip(CircleShape)
            .background(Brush.linearGradient(listOf(Teal400.copy(0.5f), Teal500.copy(0.4f)))),
        contentAlignment = Alignment.Center
    ) {
        Text(initials, color = Color.White, fontSize = (size / 3).sp, fontWeight = FontWeight.Bold)
    }
}