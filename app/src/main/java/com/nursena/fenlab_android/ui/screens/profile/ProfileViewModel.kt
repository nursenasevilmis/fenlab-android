package com.nursena.fenlab_android.ui.screens.profile

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.nursena.fenlab_android.core.base.BaseViewModel
import com.nursena.fenlab_android.core.base.UiEvent
import com.nursena.fenlab_android.core.datastore.TokenManager
import com.nursena.fenlab_android.core.network.ApiResult
import com.nursena.fenlab_android.data.remote.dto.request.UserUpdateRequest
import com.nursena.fenlab_android.domain.model.Experiment
import com.nursena.fenlab_android.domain.model.User
import com.nursena.fenlab_android.domain.repository.ExperimentRepository
import com.nursena.fenlab_android.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProfileUiState(
    val user: User?                    = null,
    val experiments: List<Experiment>  = emptyList(),
    val isLoading: Boolean             = false,
    val isOwnProfile: Boolean          = true,
    val error: String?                 = null,
    // Edit form
    val editFullName: String           = "",
    val editBio: String                = "",
    val editBranch: String             = "",
    val isEditing: Boolean             = false,
    val isSaving: Boolean              = false
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val userRepository: UserRepository,
    private val experimentRepository: ExperimentRepository,
    private val tokenManager: TokenManager
) : BaseViewModel() {

    // Navigation arg: null → kendi profili, Long → başka kullanıcı
    private val userId: Long? = savedStateHandle.get<Long>("userId")

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init { loadProfile() }

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
                            user         = user,
                            isLoading    = false,
                            isOwnProfile = userId == null,
                            editFullName = user.fullName,
                            editBio      = user.bio ?: "",
                            editBranch   = user.branch ?: ""
                        )
                    }
                    loadUserExperiments(user.id)
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
                is ApiResult.Success -> _uiState.update {
                    it.copy(experiments = result.data.content)
                }
                else -> Unit
            }
        }
    }

    // ── Edit form ─────────────────────────────────────────────────────────────
    fun toggleEdit() = _uiState.update { it.copy(isEditing = !it.isEditing) }
    fun onFullNameChange(v: String)  = _uiState.update { it.copy(editFullName = v) }
    fun onBioChange(v: String)       = _uiState.update { it.copy(editBio = v) }
    fun onBranchChange(v: String)    = _uiState.update { it.copy(editBranch = v) }

    fun saveProfile() {
        val state = _uiState.value
        val userId = state.user?.id ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            when (val result = userRepository.updateUser(
                userId,
                UserUpdateRequest(
                    fullName = state.editFullName.ifBlank { null },
                    bio      = state.editBio.ifBlank { null },
                    branch   = state.editBranch.ifBlank { null }
                )
            )) {
                is ApiResult.Success -> {
                    _uiState.update {
                        it.copy(
                            user     = result.data,
                            isSaving = false,
                            isEditing = false
                        )
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