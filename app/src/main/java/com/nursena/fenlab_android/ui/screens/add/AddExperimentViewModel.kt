package com.nursena.fenlab_android.ui.screens.add

import android.content.Context
import androidx.lifecycle.viewModelScope
import com.nursena.fenlab_android.core.base.BaseViewModel
import com.nursena.fenlab_android.core.base.UiEvent
import com.nursena.fenlab_android.core.network.ApiResult
import com.nursena.fenlab_android.core.util.FileUtils
import com.nursena.fenlab_android.data.remote.dto.request.*
import com.nursena.fenlab_android.domain.model.enums.DifficultyLevel
import com.nursena.fenlab_android.domain.model.enums.EnvironmentType
import com.nursena.fenlab_android.domain.model.enums.MediaType
import com.nursena.fenlab_android.domain.model.enums.SubjectType
import com.nursena.fenlab_android.domain.repository.ExperimentRepository
import com.nursena.fenlab_android.domain.repository.FileUploadRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import android.net.Uri
import javax.inject.Inject

// Dinamik liste item'ları için
data class MaterialItem(val name: String = "", val quantity: String = "")
data class StepItem(val text: String = "")

data class AddExperimentUiState(
    val currentStep: Int = 0,   // 0=Temel 1=Medya 2=Malzeme&Adım 3=İnceleme

    // Adım 0 — Temel bilgiler
    val title: String                      = "",
    val description: String                = "",
    val gradeLevel: Int                    = 5,
    val subject: SubjectType?              = null,
    val environment: EnvironmentType?      = null,
    val difficulty: DifficultyLevel        = DifficultyLevel.MEDIUM,
    val topic: String                      = "",
    val expectedResult: String             = "",
    val safetyNotes: String                = "",

    // Adım 1 — Medya
    val coverImageUrl: String?             = null,   // MinIO'dan gelen URL
    val videoUrl: String?                  = null,
    val isUploadingImage: Boolean          = false,
    val isUploadingVideo: Boolean          = false,

    // Adım 2 — Malzeme & Adımlar
    val materials: List<MaterialItem>      = listOf(MaterialItem()),
    val steps: List<StepItem>             = listOf(StepItem()),

    // Genel
    val isLoading: Boolean                 = false,
    val error: String?                     = null
)

@HiltViewModel
class AddExperimentViewModel @Inject constructor(
    private val experimentRepository: ExperimentRepository,
    private val fileUploadRepository: FileUploadRepository
) : BaseViewModel() {

    private val _uiState = MutableStateFlow(AddExperimentUiState())
    val uiState: StateFlow<AddExperimentUiState> = _uiState.asStateFlow()

    // ── Step 0 — Temel bilgi ──────────────────────────────────────────────────
    fun onTitleChange(v: String)           = _uiState.update { it.copy(title = v) }
    fun onDescriptionChange(v: String)     = _uiState.update { it.copy(description = v) }
    fun onGradeLevelChange(v: Int)         = _uiState.update { it.copy(gradeLevel = v) }
    fun onSubjectChange(v: SubjectType?)   = _uiState.update { it.copy(subject = v) }
    fun onEnvironmentChange(v: EnvironmentType?) = _uiState.update { it.copy(environment = v) }
    fun onDifficultyChange(v: DifficultyLevel)   = _uiState.update { it.copy(difficulty = v) }
    fun onTopicChange(v: String)           = _uiState.update { it.copy(topic = v) }
    fun onExpectedResultChange(v: String)  = _uiState.update { it.copy(expectedResult = v) }
    fun onSafetyNotesChange(v: String)     = _uiState.update { it.copy(safetyNotes = v) }

    // ── Step 1 — Medya yükleme ────────────────────────────────────────────────
    fun uploadCoverImage(context: Context, uri: Uri) {
        viewModelScope.launch {
            _uiState.update { it.copy(isUploadingImage = true) }
            val file = FileUtils.uriToFile(context, uri) ?: run {
                showSnackbar("Dosya okunamadı.")
                _uiState.update { it.copy(isUploadingImage = false) }
                return@launch
            }
            val mime = FileUtils.getMimeType(context, uri)
            val part = FileUtils.fileToMultipart(file, mime, "file")
            when (val result = fileUploadRepository.uploadImage(part)) {
                is ApiResult.Success -> _uiState.update {
                    it.copy(coverImageUrl = result.data.fileUrl, isUploadingImage = false)
                }
                is ApiResult.Error -> {
                    showSnackbar(result.message)
                    _uiState.update { it.copy(isUploadingImage = false) }
                }
                is ApiResult.Loading -> Unit
            }
        }
    }

    fun uploadVideo(context: Context, uri: Uri) {
        viewModelScope.launch {
            _uiState.update { it.copy(isUploadingVideo = true) }
            val file = FileUtils.uriToFile(context, uri) ?: run {
                showSnackbar("Dosya okunamadı.")
                _uiState.update { it.copy(isUploadingVideo = false) }
                return@launch
            }
            val mime = FileUtils.getMimeType(context, uri)
            val part = FileUtils.fileToMultipart(file, mime, "file")
            when (val result = fileUploadRepository.uploadVideo(part)) {
                is ApiResult.Success -> _uiState.update {
                    it.copy(videoUrl = result.data.fileUrl, isUploadingVideo = false)
                }
                is ApiResult.Error -> {
                    showSnackbar(result.message)
                    _uiState.update { it.copy(isUploadingVideo = false) }
                }
                is ApiResult.Loading -> Unit
            }
        }
    }

    // ── Step 2 — Malzemeler ───────────────────────────────────────────────────
    fun addMaterial()  = _uiState.update { it.copy(materials = it.materials + MaterialItem()) }
    fun removeMaterial(index: Int) = _uiState.update { it.copy(materials = it.materials.toMutableList().also { l -> l.removeAt(index) }) }
    fun onMaterialNameChange(index: Int, v: String) = _uiState.update {
        it.copy(materials = it.materials.toMutableList().also { l -> l[index] = l[index].copy(name = v) })
    }
    fun onMaterialQuantityChange(index: Int, v: String) = _uiState.update {
        it.copy(materials = it.materials.toMutableList().also { l -> l[index] = l[index].copy(quantity = v) })
    }

    // ── Step 2 — Adımlar ──────────────────────────────────────────────────────
    fun addStep()  = _uiState.update { it.copy(steps = it.steps + StepItem()) }
    fun removeStep(index: Int) = _uiState.update { it.copy(steps = it.steps.toMutableList().also { l -> l.removeAt(index) }) }
    fun onStepTextChange(index: Int, v: String) = _uiState.update {
        it.copy(steps = it.steps.toMutableList().also { l -> l[index] = l[index].copy(text = v) })
    }

    // ── Adım navigasyonu ──────────────────────────────────────────────────────
    fun nextStep() {
        val state = _uiState.value
        if (!validateCurrentStep(state)) return
        if (state.currentStep < 3)
            _uiState.update { it.copy(currentStep = it.currentStep + 1) }
        else
            publish()
    }

    fun prevStep() {
        if (_uiState.value.currentStep > 0)
            _uiState.update { it.copy(currentStep = it.currentStep - 1) }
    }

    private fun validateCurrentStep(state: AddExperimentUiState): Boolean {
        return when (state.currentStep) {
            0 -> {
                if (state.title.isBlank()) { _uiState.update { it.copy(error = "Başlık boş olamaz.") }; false }
                else if (state.description.isBlank()) { _uiState.update { it.copy(error = "Açıklama boş olamaz.") }; false }
                else { _uiState.update { it.copy(error = null) }; true }
            }
            2 -> {
                val badMat  = state.materials.any { it.name.isBlank() || it.quantity.isBlank() }
                val badStep = state.steps.any { it.text.isBlank() }
                if (badMat)  { _uiState.update { it.copy(error = "Malzeme adı ve miktarı boş olamaz.") }; false }
                else if (badStep) { _uiState.update { it.copy(error = "Adım açıklaması boş olamaz.") }; false }
                else { _uiState.update { it.copy(error = null) }; true }
            }
            else -> true
        }
    }

    // ── Yayınla ───────────────────────────────────────────────────────────────
    private fun publish() {
        val state = _uiState.value
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            // Medya listesi oluştur
            val mediaList = buildList {
                state.coverImageUrl?.let {
                    add(MediaRequest(mediaType = MediaType.IMAGE.name, mediaUrl = it, mediaOrder = 0))
                }
                state.videoUrl?.let {
                    add(MediaRequest(mediaType = MediaType.VIDEO.name, mediaUrl = it, mediaOrder = 1))
                }
            }

            val request = ExperimentCreateRequest(
                title          = state.title.trim(),
                description    = state.description.trim(),
                gradeLevel     = state.gradeLevel,
                subject        = state.subject?.name,
                environment    = state.environment?.name,
                difficulty     = state.difficulty.name,
                topic          = state.topic.ifBlank { null },
                expectedResult = state.expectedResult.ifBlank { null },
                safetyNotes    = state.safetyNotes.ifBlank { null },
                isPublished    = true,
                materials      = state.materials.mapIndexed { _, m ->
                    MaterialRequest(materialName = m.name.trim(), quantity = m.quantity.trim())
                },
                steps = state.steps.mapIndexed { index, s ->
                    StepRequest(stepOrder = index + 1, stepText = s.text.trim())
                },
                media = mediaList
            )

            when (val result = experimentRepository.createExperiment(request)) {
                is ApiResult.Success -> {
                    _uiState.update { it.copy(isLoading = false) }
                    sendEvent(UiEvent.Navigate("experiment/${result.data.id}"))
                }
                is ApiResult.Error -> _uiState.update {
                    it.copy(isLoading = false, error = result.message)
                }
                is ApiResult.Loading -> Unit
            }
        }
    }
}