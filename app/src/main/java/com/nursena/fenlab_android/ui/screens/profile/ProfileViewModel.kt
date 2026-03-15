package com.nursena.fenlab_android.ui.screens.profile

import android.content.Context
import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.nursena.fenlab_android.core.base.BaseViewModel
import com.nursena.fenlab_android.core.base.UiEvent
import com.nursena.fenlab_android.core.datastore.TokenManager
import com.nursena.fenlab_android.core.network.ApiResult
import com.nursena.fenlab_android.core.util.FileUtils
import com.nursena.fenlab_android.data.remote.dto.request.UserUpdateRequest
import com.nursena.fenlab_android.domain.model.Experiment
import com.nursena.fenlab_android.domain.model.Notification
import com.nursena.fenlab_android.domain.model.User
import com.nursena.fenlab_android.domain.repository.ExperimentRepository
import com.nursena.fenlab_android.domain.repository.FileUploadRepository
import com.nursena.fenlab_android.domain.repository.NotificationRepository
import com.nursena.fenlab_android.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProfileUiState(
    val user: User?                       = null,
    val experiments: List<Experiment>     = emptyList(),
    val notifications: List<Notification> = emptyList(),
    val unreadCount: Long                 = 0L,
    val isLoading: Boolean                = false,
    val isNotifLoading: Boolean           = false,
    val isUploadingPhoto: Boolean         = false,
    val isOwnProfile: Boolean             = true,
    val error: String?                    = null,
    // Edit form
    val editFullName: String        = "",
    val editBio: String             = "",
    val editBranch: String          = "",
    val editExperienceYears: String = "",
    val isEditing: Boolean          = false,
    val isSaving: Boolean           = false
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val userRepository: UserRepository,
    private val experimentRepository: ExperimentRepository,
    private val notificationRepository: NotificationRepository,
    private val fileUploadRepository: FileUploadRepository,
    private val tokenManager: TokenManager
) : BaseViewModel() {

    private val userId: Long? = savedStateHandle.get<Long>("userId")

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init { loadProfile() }

    fun loadProfile() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val result = if (userId == null) userRepository.getCurrentUser()
            else userRepository.getUserById(userId)
            when (result) {
                is ApiResult.Success -> {
                    val user = result.data
                    _uiState.update {
                        it.copy(
                            user              = user,
                            isLoading         = false,
                            isOwnProfile      = userId == null,
                            editFullName      = user.fullName,
                            editBio           = user.bio ?: "",
                            editBranch        = user.branch ?: "",
                            editExperienceYears = user.experienceYears?.toString() ?: ""
                        )
                    }
                    // Öğretmense deneyleri yükle
                    if (user.isTeacher) loadUserExperiments(user.id)
                    if (userId == null) { loadNotifications(); loadUnreadCount() }
                }
                is ApiResult.Error -> _uiState.update { it.copy(isLoading = false, error = result.message) }
                is ApiResult.Loading -> Unit
            }
        }
    }

    private fun loadUserExperiments(uid: Long) {
        viewModelScope.launch {
            when (val r = experimentRepository.getUserExperiments(uid, size = 50)) {
                is ApiResult.Success -> _uiState.update { it.copy(experiments = r.data.content) }
                else -> Unit
            }
        }
    }

    // ── Bildirimler ──────────────────────────────────────────────────────────
    fun loadNotifications() {
        viewModelScope.launch {
            _uiState.update { it.copy(isNotifLoading = true) }
            when (val r = notificationRepository.getUserNotifications(page = 0, size = 30)) {
                is ApiResult.Success -> _uiState.update { it.copy(notifications = r.data.content, isNotifLoading = false) }
                is ApiResult.Error   -> _uiState.update { it.copy(isNotifLoading = false) }
                is ApiResult.Loading -> Unit
            }
        }
    }

    private fun loadUnreadCount() {
        viewModelScope.launch {
            when (val r = notificationRepository.getUnreadCount()) {
                is ApiResult.Success -> _uiState.update { it.copy(unreadCount = r.data) }
                else -> Unit
            }
        }
    }

    fun markNotificationRead(id: Long) {
        viewModelScope.launch {
            notificationRepository.markAsRead(id)
            _uiState.update {
                it.copy(
                    notifications = it.notifications.map { n -> if (n.id == id) n.copy(isRead = true) else n },
                    unreadCount   = maxOf(0L, it.unreadCount - 1)
                )
            }
        }
    }

    fun markAllRead() {
        viewModelScope.launch {
            notificationRepository.markAllAsRead()
            _uiState.update {
                it.copy(notifications = it.notifications.map { n -> n.copy(isRead = true) }, unreadCount = 0L)
            }
        }
    }

    // ── Profil fotoğrafı ─────────────────────────────────────────────────────
    fun uploadProfilePhoto(context: Context, uri: Uri) {
        viewModelScope.launch {
            _uiState.update { it.copy(isUploadingPhoto = true) }
            val file = FileUtils.uriToFile(context, uri) ?: run {
                showSnackbar("Dosya okunamadı.")
                _uiState.update { it.copy(isUploadingPhoto = false) }
                return@launch
            }
            val part = FileUtils.fileToMultipart(file, FileUtils.getMimeType(context, uri), "file")
            when (val r = fileUploadRepository.uploadProfileImage(part)) {
                is ApiResult.Success -> {
                    val uid = _uiState.value.user?.id ?: return@launch
                    when (val upd = userRepository.updateUser(uid, UserUpdateRequest(profileImageUrl = r.data.fileUrl))) {
                        is ApiResult.Success -> {
                            _uiState.update { it.copy(user = upd.data, isUploadingPhoto = false) }
                            showSnackbar("Profil fotoğrafı güncellendi.")
                        }
                        is ApiResult.Error -> {
                            _uiState.update { it.copy(isUploadingPhoto = false) }
                            showSnackbar(upd.message)
                        }
                        is ApiResult.Loading -> Unit
                    }
                }
                is ApiResult.Error -> {
                    _uiState.update { it.copy(isUploadingPhoto = false) }
                    showSnackbar(r.message)
                }
                is ApiResult.Loading -> Unit
            }
        }
    }

    fun removeProfilePhoto() {
        val uid = _uiState.value.user?.id ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isUploadingPhoto = true) }
            when (val r = userRepository.updateUser(uid, UserUpdateRequest(profileImageUrl = ""))) {
                is ApiResult.Success -> {
                    _uiState.update { it.copy(user = r.data, isUploadingPhoto = false) }
                    showSnackbar("Profil fotoğrafı kaldırıldı.")
                }
                is ApiResult.Error -> {
                    _uiState.update { it.copy(isUploadingPhoto = false) }
                    showSnackbar(r.message)
                }
                is ApiResult.Loading -> Unit
            }
        }
    }

    // ── Profil düzenleme ─────────────────────────────────────────────────────
    fun toggleEdit() = _uiState.update { it.copy(isEditing = !it.isEditing) }
    fun onFullNameChange(v: String)        = _uiState.update { it.copy(editFullName = v) }
    fun onBioChange(v: String)             = _uiState.update { it.copy(editBio = v) }
    fun onBranchChange(v: String)          = _uiState.update { it.copy(editBranch = v) }
    fun onExperienceYearsChange(v: String) = _uiState.update { it.copy(editExperienceYears = v) }

    fun saveProfile() {
        val state = _uiState.value
        val uid   = state.user?.id ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            when (val r = userRepository.updateUser(uid, UserUpdateRequest(
                fullName        = state.editFullName.ifBlank { null },
                bio             = state.editBio.ifBlank { null },
                branch          = state.editBranch.ifBlank { null },
                experienceYears = state.editExperienceYears.toIntOrNull()
            ))) {
                is ApiResult.Success -> {
                    _uiState.update { it.copy(user = r.data, isSaving = false, isEditing = false) }
                    showSnackbar("Profil güncellendi.")
                }
                is ApiResult.Error -> {
                    _uiState.update { it.copy(isSaving = false) }
                    showSnackbar(r.message)
                }
                is ApiResult.Loading -> Unit
            }
        }
    }

    // ── Deney sil ────────────────────────────────────────────────────────────
    fun deleteExperiment(id: Long) {
        viewModelScope.launch {
            when (val r = experimentRepository.deleteExperiment(id)) {
                is ApiResult.Success -> {
                    _uiState.update { it.copy(experiments = it.experiments.filter { e -> e.id != id }) }
                    showSnackbar("Deney silindi.")
                }
                is ApiResult.Error -> showSnackbar(r.message)
                is ApiResult.Loading -> Unit
            }
        }
    }

    // ── Hesap sil ────────────────────────────────────────────────────────────
    fun deleteAccount(onSuccess: () -> Unit) {
        val uid = _uiState.value.user?.id ?: return
        viewModelScope.launch {
            when (userRepository.deleteUser(uid)) {
                is ApiResult.Success -> { tokenManager.clearSession(); onSuccess() }
                is ApiResult.Error   -> showSnackbar("Hesap silinemedi.")
                is ApiResult.Loading -> Unit
            }
        }
    }

    // ── Çıkış ────────────────────────────────────────────────────────────────
    fun logout() {
        viewModelScope.launch {
            tokenManager.clearSession()
            sendEvent(UiEvent.LoggedOut)
        }
    }
}