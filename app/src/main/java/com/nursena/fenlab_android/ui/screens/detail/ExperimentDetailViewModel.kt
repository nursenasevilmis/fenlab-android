package com.nursena.fenlab_android.ui.screens.detail

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.nursena.fenlab_android.core.base.BaseViewModel
import com.nursena.fenlab_android.core.datastore.TokenManager
import com.nursena.fenlab_android.core.network.ApiResult
import com.nursena.fenlab_android.core.util.FileUtils
import com.nursena.fenlab_android.core.util.PdfUtils
import com.nursena.fenlab_android.data.remote.dto.request.*
import com.nursena.fenlab_android.data.remote.dto.request.MaterialRequest
import com.nursena.fenlab_android.data.remote.dto.request.StepRequest
import com.nursena.fenlab_android.domain.model.*
import com.nursena.fenlab_android.domain.model.enums.*
import com.nursena.fenlab_android.domain.repository.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DetailUiState(
    val isPdfLoading: Boolean         = false,
    val isOwner: Boolean              = false,
    val experiment: ExperimentDetail? = null,
    val comments: List<Comment>       = emptyList(),
    val questions: List<Question>     = emptyList(),
    val isLoading: Boolean            = false,
    val isCommentsLoading: Boolean    = false,
    val isQuestionsLoading: Boolean   = false,
    val error: String?                = null,
    val selectedTab: Int              = 0,
    val commentInput: String          = "",
    val questionInput: String         = "",
    val isFavorited: Boolean          = false,
    val currentUserRating: Int?       = null,

    // ── Düzenleme ────────────────────────────────────────────────────────────
    val isEditing: Boolean              = false,
    val isSaving: Boolean               = false,
    val editTitle: String               = "",
    val editDescription: String         = "",
    val editGradeLevel: Int             = 5,
    val editSubject: SubjectType?       = null,
    val editCustomSubject: String       = "",
    val editEnvironment: EnvironmentType? = null,
    val editDifficulty: DifficultyLevel = DifficultyLevel.MEDIUM,
    val editTopic: String               = "",
    val editSafetyNotes: String         = "",
    val editExpectedResult: String      = "",

    // Resim düzenleme
    val editCoverImageUrl: String?          = null,
    val editAdditionalImages: List<String>  = emptyList(),
    val isUploadingEditCover: Boolean       = false,
    val isUploadingEditAdditional: Boolean  = false,

    // Malzeme düzenleme
    val editMaterials: List<EditMaterial>   = emptyList(),

    // Adım düzenleme
    val editSteps: List<EditStep>           = emptyList(),
)

data class EditMaterial(
    val id: Long?  = null,          // null = yeni eklendi
    val name: String = "",
    val quantity: String = ""
)

data class EditStep(
    val id: Long?  = null,          // null = yeni eklendi
    val order: Int = 0,
    val text: String = ""
)

@HiltViewModel
class ExperimentDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val experimentRepository: ExperimentRepository,
    private val commentRepository: CommentRepository,
    private val questionRepository: QuestionRepository,
    private val favoriteRepository: FavoriteRepository,
    private val ratingRepository: RatingRepository,
    private val pdfRepository: PdfRepository,
    private val fileUploadRepository: FileUploadRepository,
    private val tokenManager: TokenManager
) : BaseViewModel() {

    private val experimentId: Long = checkNotNull(savedStateHandle["experimentId"])

    private val _uiState = MutableStateFlow(DetailUiState())
    val uiState: StateFlow<DetailUiState> = _uiState.asStateFlow()

    init {
        loadExperiment()
        loadComments()
        loadQuestions()
    }

    fun loadExperiment() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            when (val result = experimentRepository.getExperimentById(experimentId)) {
                is ApiResult.Success -> {
                    val myId = tokenManager.getUserId()
                    _uiState.update {
                        it.copy(
                            experiment        = result.data,
                            isLoading         = false,
                            isFavorited       = result.data.isFavoritedByCurrentUser,
                            currentUserRating = result.data.currentUserRating,
                            isOwner           = myId != null && myId == result.data.author.id
                        )
                    }
                }
                is ApiResult.Error   -> _uiState.update { it.copy(isLoading = false, error = result.message) }
                is ApiResult.Loading -> Unit
            }
        }
    }

    fun loadComments() {
        viewModelScope.launch {
            _uiState.update { it.copy(isCommentsLoading = true) }
            when (val result = commentRepository.getExperimentComments(experimentId)) {
                is ApiResult.Success -> _uiState.update { it.copy(comments = result.data.content, isCommentsLoading = false) }
                is ApiResult.Error   -> _uiState.update { it.copy(isCommentsLoading = false) }
                is ApiResult.Loading -> Unit
            }
        }
    }

    fun onCommentInputChange(v: String) = _uiState.update { it.copy(commentInput = v) }

    fun addComment() {
        val text = _uiState.value.commentInput.trim()
        if (text.isBlank()) return
        viewModelScope.launch {
            when (val result = commentRepository.addComment(experimentId, CommentCreateRequest(content = text))) {
                is ApiResult.Success -> _uiState.update { it.copy(comments = listOf(result.data) + it.comments, commentInput = "") }
                is ApiResult.Error   -> showSnackbar(result.message)
                is ApiResult.Loading -> Unit
            }
        }
    }

    fun deleteComment(commentId: Long) {
        viewModelScope.launch {
            when (val result = commentRepository.deleteComment(commentId)) {
                is ApiResult.Success -> _uiState.update { it.copy(comments = it.comments.filter { c -> c.id != commentId }) }
                is ApiResult.Error   -> showSnackbar(result.message)
                is ApiResult.Loading -> Unit
            }
        }
    }

    fun loadQuestions() {
        viewModelScope.launch {
            _uiState.update { it.copy(isQuestionsLoading = true) }
            when (val result = questionRepository.getExperimentQuestions(experimentId)) {
                is ApiResult.Success -> _uiState.update { it.copy(questions = result.data.content, isQuestionsLoading = false) }
                is ApiResult.Error   -> _uiState.update { it.copy(isQuestionsLoading = false) }
                is ApiResult.Loading -> Unit
            }
        }
    }

    fun onQuestionInputChange(v: String) = _uiState.update { it.copy(questionInput = v) }

    fun askQuestion() {
        val text = _uiState.value.questionInput.trim()
        if (text.isBlank()) return
        viewModelScope.launch {
            when (val result = questionRepository.askQuestion(experimentId, QuestionCreateRequest(questionText = text))) {
                is ApiResult.Success -> _uiState.update { it.copy(questions = it.questions + result.data, questionInput = "") }
                is ApiResult.Error   -> showSnackbar(result.message)
                is ApiResult.Loading -> Unit
            }
        }
    }

    fun answerQuestion(questionId: Long, answerText: String) {
        if (answerText.isBlank()) return
        viewModelScope.launch {
            when (val result = questionRepository.answerQuestion(questionId, AnswerCreateRequest(answerText = answerText))) {
                is ApiResult.Success -> _uiState.update { it.copy(questions = it.questions.map { q -> if (q.id == questionId) result.data else q }) }
                is ApiResult.Error   -> showSnackbar(result.message)
                is ApiResult.Loading -> Unit
            }
        }
    }

    fun deleteQuestion(questionId: Long) {
        viewModelScope.launch {
            when (val result = questionRepository.deleteQuestion(questionId)) {
                is ApiResult.Success -> _uiState.update { it.copy(questions = it.questions.filter { q -> q.id != questionId }) }
                is ApiResult.Error   -> showSnackbar(result.message)
                is ApiResult.Loading -> Unit
            }
        }
    }

    // ── Favori — owner ise engelle ────────────────────────────────────────────
    fun toggleFavorite() {
        if (_uiState.value.isOwner) {
            showSnackbar("Kendi deneyin — beğeni yapamazsın.")
            return
        }
        viewModelScope.launch {
            val isFav = _uiState.value.isFavorited
            _uiState.update { it.copy(isFavorited = !isFav) }
            val result = if (isFav) favoriteRepository.removeFromFavorites(experimentId)
            else favoriteRepository.addToFavorites(experimentId)
            if (result is ApiResult.Error) {
                _uiState.update { it.copy(isFavorited = isFav) }
                showSnackbar(result.message)
            }
        }
    }

    // ── Puan — owner ise engelle ──────────────────────────────────────────────
    fun rateExperiment(rating: Int) {
        if (_uiState.value.isOwner) {
            showSnackbar("Kendi deneyin — puan veremezsin.")
            return
        }
        viewModelScope.launch {
            when (val result = ratingRepository.rateExperiment(experimentId, RatingRequest(rating = rating))) {
                is ApiResult.Success -> {
                    _uiState.update { it.copy(currentUserRating = result.data.rating) }
                    showSnackbar("Puanınız kaydedildi.")
                    when (val avg = ratingRepository.getAverageRating(experimentId)) {
                        is ApiResult.Success -> _uiState.update { state ->
                            state.copy(experiment = state.experiment?.let { exp ->
                                exp.copy(averageRating = avg.data)
                            })
                        }
                        else -> Unit
                    }
                }
                is ApiResult.Error   -> showSnackbar(result.message)
                is ApiResult.Loading -> Unit
            }
        }
    }

    fun downloadPdf(context: Context) {
        viewModelScope.launch {
            _uiState.update { it.copy(isPdfLoading = true) }
            try {
                val token = tokenManager.getToken()
                when (val result = pdfRepository.generatePdf(experimentId)) {
                    is ApiResult.Success -> {
                        val url = result.data.pdfUrl
                        if (url.isNotBlank()) {
                            val downloadId = PdfUtils.downloadPdfViaManager(
                                context  = context,
                                pdfUrl   = url,
                                fileName = "deney_${experimentId}.pdf",
                                token    = token
                            )
                            if (downloadId != -1L) showSnackbar("PDF indiriliyor...")
                            else showSnackbar("PDF indirilemedi.")
                        } else showSnackbar("PDF URL alınamadı.")
                    }
                    is ApiResult.Error   -> showSnackbar(result.message)
                    is ApiResult.Loading -> Unit
                }
            } catch (e: Exception) {
                showSnackbar("PDF indirme hatası: ${e.message}")
            } finally {
                _uiState.update { it.copy(isPdfLoading = false) }
            }
        }
    }

    fun deleteExperiment(onSuccess: () -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            when (val result = experimentRepository.deleteExperiment(experimentId)) {
                is ApiResult.Success -> { _uiState.update { it.copy(isLoading = false) }; onSuccess() }
                is ApiResult.Error   -> { _uiState.update { it.copy(isLoading = false) }; showSnackbar(result.message) }
                is ApiResult.Loading -> Unit
            }
        }
    }

    fun selectTab(index: Int) = _uiState.update { it.copy(selectedTab = index) }

    // ── Düzenleme aç ─────────────────────────────────────────────────────────
    fun openEdit() {
        val exp = _uiState.value.experiment ?: return
        _uiState.update {
            it.copy(
                isEditing              = true,
                editTitle              = exp.title,
                editDescription        = exp.description,
                editGradeLevel         = exp.gradeLevel ?: 5,
                editSubject            = exp.subject,
                editCustomSubject      = "",
                editEnvironment        = exp.environment,
                editDifficulty         = exp.difficulty ?: DifficultyLevel.MEDIUM,
                editTopic              = exp.topic ?: "",
                editSafetyNotes        = exp.safetyNotes ?: "",
                editExpectedResult     = exp.expectedResult ?: "",
                editCoverImageUrl      = exp.imageMediaList.firstOrNull()?.mediaUrl,
                editAdditionalImages   = exp.imageMediaList.drop(1).map { m -> m.mediaUrl },
                editMaterials          = exp.materials.map { m ->
                    EditMaterial(id = m.id, name = m.materialName, quantity = m.quantity)
                },
                editSteps              = exp.sortedSteps.mapIndexed { idx, s ->
                    EditStep(id = s.id, order = idx, text = s.stepText)
                },
            )
        }
    }

    fun closeEdit() = _uiState.update { it.copy(isEditing = false, isSaving = false) }

    // Edit field setters
    fun onEditTitleChange(v: String)             = _uiState.update { it.copy(editTitle = v) }
    fun onEditDescChange(v: String)              = _uiState.update { it.copy(editDescription = v) }
    fun onEditGradeLevelChange(v: Int)           = _uiState.update { it.copy(editGradeLevel = v) }
    fun onEditSubjectChange(v: SubjectType?)     = _uiState.update { it.copy(editSubject = v, editCustomSubject = if (v != SubjectType.OTHER) "" else it.editCustomSubject) }
    fun onEditCustomSubjectChange(v: String)     = _uiState.update { it.copy(editCustomSubject = v) }
    fun onEditEnvironmentChange(v: EnvironmentType?) = _uiState.update { it.copy(editEnvironment = v) }
    fun onEditDifficultyChange(v: DifficultyLevel)   = _uiState.update { it.copy(editDifficulty = v) }
    fun onEditTopicChange(v: String)             = _uiState.update { it.copy(editTopic = v) }
    fun onEditSafetyNotesChange(v: String)       = _uiState.update { it.copy(editSafetyNotes = v) }
    fun onEditExpectedResultChange(v: String)    = _uiState.update { it.copy(editExpectedResult = v) }

    // Malzeme işlemleri
    fun addMaterial() = _uiState.update {
        it.copy(editMaterials = it.editMaterials + EditMaterial())
    }
    fun updateMaterialName(index: Int, name: String) = _uiState.update {
        it.copy(editMaterials = it.editMaterials.toMutableList().also { l ->
            l[index] = l[index].copy(name = name)
        })
    }
    fun updateMaterialQuantity(index: Int, qty: String) = _uiState.update {
        it.copy(editMaterials = it.editMaterials.toMutableList().also { l ->
            l[index] = l[index].copy(quantity = qty)
        })
    }
    fun removeMaterial(index: Int) = _uiState.update {
        it.copy(editMaterials = it.editMaterials.toMutableList().also { l -> l.removeAt(index) })
    }

    // Adım işlemleri
    fun addStep() = _uiState.update {
        val nextOrder = it.editSteps.size
        it.copy(editSteps = it.editSteps + EditStep(order = nextOrder, text = ""))
    }
    fun updateStepText(index: Int, text: String) = _uiState.update {
        it.copy(editSteps = it.editSteps.toMutableList().also { l ->
            l[index] = l[index].copy(text = text)
        })
    }
    fun removeStep(index: Int) = _uiState.update {
        it.copy(editSteps = it.editSteps.toMutableList().also { l -> l.removeAt(index) }
            .mapIndexed { i, s -> s.copy(order = i) })
    }

    // Kapak resmi
    fun uploadEditCoverImage(context: Context, uri: android.net.Uri) {
        viewModelScope.launch {
            _uiState.update { it.copy(isUploadingEditCover = true) }
            val file = FileUtils.uriToFile(context, uri) ?: run {
                showSnackbar("Dosya okunamadı.")
                _uiState.update { it.copy(isUploadingEditCover = false) }
                return@launch
            }
            val part = FileUtils.fileToMultipart(file, FileUtils.getMimeType(context, uri), "file")
            when (val result = fileUploadRepository.uploadImage(part)) {
                is ApiResult.Success -> _uiState.update { it.copy(editCoverImageUrl = result.data.fileUrl, isUploadingEditCover = false) }
                is ApiResult.Error   -> { showSnackbar(result.message); _uiState.update { it.copy(isUploadingEditCover = false) } }
                is ApiResult.Loading -> Unit
            }
        }
    }

    fun clearEditCoverImage() = _uiState.update { it.copy(editCoverImageUrl = null) }

    // Ek görseller
    fun uploadEditAdditionalImage(context: Context, uri: android.net.Uri) {
        if (_uiState.value.editAdditionalImages.size >= 10) {
            showSnackbar("En fazla 10 ek görsel ekleyebilirsin.")
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isUploadingEditAdditional = true) }
            val file = FileUtils.uriToFile(context, uri) ?: run {
                showSnackbar("Dosya okunamadı.")
                _uiState.update { it.copy(isUploadingEditAdditional = false) }
                return@launch
            }
            val part = FileUtils.fileToMultipart(file, FileUtils.getMimeType(context, uri), "file")
            when (val result = fileUploadRepository.uploadImage(part)) {
                is ApiResult.Success -> _uiState.update { it.copy(editAdditionalImages = it.editAdditionalImages + result.data.fileUrl, isUploadingEditAdditional = false) }
                is ApiResult.Error   -> { showSnackbar(result.message); _uiState.update { it.copy(isUploadingEditAdditional = false) } }
                is ApiResult.Loading -> Unit
            }
        }
    }

    fun removeEditAdditionalImage(index: Int) = _uiState.update {
        it.copy(editAdditionalImages = it.editAdditionalImages.toMutableList().also { l -> l.removeAt(index) })
    }

    // Kaydet
    fun saveEdit() {
        val state = _uiState.value
        val expId = state.experiment?.id ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            try {
                val mediaList = buildList<MediaRequest> {
                    state.editCoverImageUrl?.let {
                        add(MediaRequest(mediaType = MediaType.IMAGE.name, mediaUrl = it, mediaOrder = 0))
                    }
                    state.editAdditionalImages.forEachIndexed { idx, url ->
                        add(MediaRequest(mediaType = MediaType.IMAGE.name, mediaUrl = url, mediaOrder = idx + 1))
                    }
                    // Mevcut videoyu koru
                    state.experiment?.videoMedia?.let {
                        add(MediaRequest(mediaType = MediaType.VIDEO.name, mediaUrl = it.mediaUrl, mediaOrder = state.editAdditionalImages.size + 1))
                    }
                }

                val materialList = state.editMaterials
                    .filter { it.name.isNotBlank() }
                    .map { MaterialRequest(materialName = it.name.trim(), quantity = it.quantity.trim()) }

                val stepList = state.editSteps
                    .filter { it.text.isNotBlank() }
                    .mapIndexed { idx, s -> StepRequest(stepOrder = idx + 1, stepText = s.text.trim()) }

                val request = ExperimentUpdateRequest(
                    title          = state.editTitle.trim(),
                    description    = state.editDescription.trim(),
                    gradeLevel     = state.editGradeLevel,
                    subject        = if (state.editSubject == SubjectType.OTHER && state.editCustomSubject.isNotBlank())
                        state.editCustomSubject else state.editSubject?.name,
                    environment    = state.editEnvironment?.name,
                    difficulty     = state.editDifficulty.name,
                    topic          = state.editTopic.trim().ifBlank { null },
                    safetyNotes    = state.editSafetyNotes.trim().ifBlank { null },
                    expectedResult = state.editExpectedResult.trim().ifBlank { null },
                    materials      = materialList,
                    steps          = stepList,
                    media          = mediaList
                )

                when (val result = experimentRepository.updateExperiment(expId, request)) {
                    is ApiResult.Success -> {
                        loadExperiment()
                        _uiState.update { it.copy(isEditing = false, isSaving = false) }
                        showSnackbar("Deney güncellendi ✓")
                    }
                    is ApiResult.Error -> {
                        _uiState.update { it.copy(isSaving = false) }
                        showSnackbar(result.message)
                    }
                    is ApiResult.Loading -> Unit
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isSaving = false) }
                showSnackbar("Güncelleme başarısız: ${e.message}")
            }
        }
    }
}