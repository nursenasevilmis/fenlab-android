package com.nursena.fenlab_android.ui.screens.detail

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.nursena.fenlab_android.core.base.BaseViewModel
import com.nursena.fenlab_android.core.datastore.TokenManager
import com.nursena.fenlab_android.core.network.ApiResult
import com.nursena.fenlab_android.core.util.PdfUtils
import com.nursena.fenlab_android.data.remote.dto.request.*
import com.nursena.fenlab_android.domain.model.*
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
    val currentUserRating: Int?       = null
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

    fun toggleFavorite() {
        viewModelScope.launch {
            val isFav = _uiState.value.isFavorited
            _uiState.update { it.copy(isFavorited = !isFav) }
            val result = if (isFav) favoriteRepository.removeFromFavorites(experimentId)
            else       favoriteRepository.addToFavorites(experimentId)
            if (result is ApiResult.Error) {
                _uiState.update { it.copy(isFavorited = isFav) }
                showSnackbar(result.message)
            }
        }
    }

    fun rateExperiment(rating: Int) {
        viewModelScope.launch {
            when (val result = ratingRepository.rateExperiment(experimentId, RatingRequest(rating = rating))) {
                is ApiResult.Success -> {
                    _uiState.update { it.copy(currentUserRating = result.data.rating) }
                    showSnackbar("Puanınız kaydedildi.")
                    // Yeni ortalama puanı hemen çek
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
            when (val result = pdfRepository.generatePdf(experimentId)) {
                is ApiResult.Success -> {
                    val url = result.data.pdfUrl
                    if (url.isNotBlank()) {
                        PdfUtils.downloadPdfViaManager(context, url, "deney_${experimentId}.pdf")
                        showSnackbar("PDF indiriliyor...")
                    } else showSnackbar("PDF URL alınamadı.")
                    _uiState.update { it.copy(isPdfLoading = false) }
                }
                is ApiResult.Error   -> { _uiState.update { it.copy(isPdfLoading = false) }; showSnackbar(result.message) }
                is ApiResult.Loading -> Unit
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
}