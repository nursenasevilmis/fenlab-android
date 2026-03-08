package com.nursena.fenlab_android.ui.screens.profile

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.nursena.fenlab_android.core.base.BaseViewModel
import com.nursena.fenlab_android.core.base.UiEvent
import com.nursena.fenlab_android.core.datastore.TokenManager
import com.nursena.fenlab_android.core.network.ApiResult
import com.nursena.fenlab_android.data.remote.dto.request.UserUpdateRequest
import com.nursena.fenlab_android.domain.model.Experiment
import com.nursena.fenlab_android.domain.model.Notification
import com.nursena.fenlab_android.domain.model.User
import com.nursena.fenlab_android.domain.repository.ExperimentRepository
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
    val isOwnProfile: Boolean             = true,
    val error: String?                    = null,
    // Edit form
    val editFullName: String              = "",
    val editBio: String                   = "",
    val editBranch: String                = "",
    val editExperienceYears: String       = "",
    val isEditing: Boolean                = false,
    val isSaving: Boolean                 = false
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val userRepository: UserRepository,
    private val experimentRepository: ExperimentRepository,
    private val notificationRepository: NotificationRepository,
    private val tokenManager: TokenManager
) : BaseViewModel() {

    private val userId: Long? = savedStateHandle.get<Long>("userId")

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        loadProfile()
    }

    fun loadProfile() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            val userResult = if (userId == null)
                userRepository.getCurrentUser()
            else
                userRepository.getUserById(userId)

            when (userResult) {
                is ApiResult.Success -> {
                    val user = userResult.data
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
                    loadUserExperiments(user.id)
                    if (userId == null) {
                        loadNotifications()
                        loadUnreadCount()
                    }
                }
                is ApiResult.Error -> _uiState.update {
                    it.copy(isLoading = false, error = userResult.message)
                }
                is ApiResult.Loading -> Unit
            }
        }
    }

    private fun loadUserExperiments(uid: Long) {
        viewModelScope.launch {
            when (val result = experimentRepository.getUserExperiments(uid, size = 50)) {
                is ApiResult.Success -> _uiState.update { it.copy(experiments = result.data.content) }
                else -> Unit
            }
        }
    }

    // ── Bildirimler ───────────────────────────────────────────────────────────
    fun loadNotifications() {
        viewModelScope.launch {
            _uiState.update { it.copy(isNotifLoading = true) }
            when (val result = notificationRepository.getUserNotifications(page = 0, size = 20)) {
                is ApiResult.Success -> _uiState.update {
                    it.copy(notifications = result.data.content, isNotifLoading = false)
                }
                is ApiResult.Error -> _uiState.update { it.copy(isNotifLoading = false) }
                is ApiResult.Loading -> Unit
            }
        }
    }

    private fun loadUnreadCount() {
        viewModelScope.launch {
            when (val result = notificationRepository.getUnreadCount()) {
                is ApiResult.Success -> _uiState.update { it.copy(unreadCount = result.data) }
                else -> Unit
            }
        }
    }

    fun markNotificationRead(notificationId: Long) {
        viewModelScope.launch {
            notificationRepository.markAsRead(notificationId)
            _uiState.update {
                it.copy(
                    notifications = it.notifications.map { n ->
                        if (n.id == notificationId) n.copy(isRead = true) else n
                    },
                    unreadCount = maxOf(0L, it.unreadCount - 1)
                )
            }
        }
    }

    fun markAllRead() {
        viewModelScope.launch {
            notificationRepository.markAllAsRead()
            _uiState.update {
                it.copy(
                    notifications = it.notifications.map { n -> n.copy(isRead = true) },
                    unreadCount = 0L
                )
            }
        }
    }

    // ── Edit form ─────────────────────────────────────────────────────────────
    fun toggleEdit() = _uiState.update { it.copy(isEditing = !it.isEditing) }
    fun onFullNameChange(v: String)          = _uiState.update { it.copy(editFullName = v) }
    fun onBioChange(v: String)               = _uiState.update { it.copy(editBio = v) }
    fun onBranchChange(v: String)            = _uiState.update { it.copy(editBranch = v) }
    fun onExperienceYearsChange(v: String)   = _uiState.update { it.copy(editExperienceYears = v) }

    fun saveProfile() {
        val state = _uiState.value
        val uid   = state.user?.id ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            when (val result = userRepository.updateUser(
                uid,
                UserUpdateRequest(
                    fullName        = state.editFullName.ifBlank { null },
                    bio             = state.editBio.ifBlank { null },
                    branch          = state.editBranch.ifBlank { null },
                    experienceYears = state.editExperienceYears.toIntOrNull()
                )
            )) {
                is ApiResult.Success -> {
                    _uiState.update {
                        it.copy(user = result.data, isSaving = false, isEditing = false)
                    }
                    showSnackbar("Profil güncellendi.")
                }
                is ApiResult.Error -> {
                    _uiState.update { it.copy(isSaving = false) }
                    showSnackbar(result.message)
                }
                is ApiResult.Loading -> Unit
            }
        }
    }

    // ── Logout ────────────────────────────────────────────────────────────────
    fun logout() {
        viewModelScope.launch {
            tokenManager.clearSession()
            sendEvent(UiEvent.LoggedOut)
        }
    }
}