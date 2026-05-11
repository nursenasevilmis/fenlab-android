package com.nursena.fenlab_android.ui.screens.auth

import android.content.Context
import android.net.Uri
import androidx.lifecycle.viewModelScope
import com.nursena.fenlab_android.core.base.BaseViewModel
import com.nursena.fenlab_android.core.base.UiEvent
import com.nursena.fenlab_android.core.datastore.TokenManager
import com.nursena.fenlab_android.core.network.ApiResult
import com.nursena.fenlab_android.core.util.FileUtils
import com.nursena.fenlab_android.data.remote.dto.request.LoginRequest
import com.nursena.fenlab_android.data.remote.dto.request.RegisterRequest
import com.nursena.fenlab_android.data.remote.dto.request.UserUpdateRequest
import com.nursena.fenlab_android.domain.model.enums.UserRole
import com.nursena.fenlab_android.domain.repository.AuthRepository
import com.nursena.fenlab_android.domain.repository.FileUploadRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class RegisterStep { ROLE, REQUIRED, BRANCH, EXPERIENCE, BIO, PHOTO }

data class AuthUiState(
    val loginUsernameOrEmail: String = "",
    val loginPassword: String = "",
    val registerUsername: String = "",
    val registerFullName: String = "",
    val registerEmail: String = "",
    val registerPassword: String = "",
    val registerRole: UserRole = UserRole.USER,
    val registerBranch: String = "",
    val registerExperienceYears: String = "",
    val registerBio: String = "",
    val registerPhotoUri: Uri? = null,
    val registerPhotoUrl: String? = null,
    val registerStep: RegisterStep = RegisterStep.ROLE,
    val isLoading: Boolean = false,
    val isUploadingPhoto: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val fileUploadRepository: FileUploadRepository,
    private val tokenManager: TokenManager
) : BaseViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun checkSession() {
        viewModelScope.launch {
            if (tokenManager.isLoggedIn()) sendEvent(UiEvent.Navigate("home"))
            else sendEvent(UiEvent.Navigate("auth"))
        }
    }

    fun clearError() = _uiState.update { it.copy(error = null) }

    // ── Login ─────────────────────────────────────────────────────────────────
    fun onLoginUsernameChange(v: String) = _uiState.update { it.copy(loginUsernameOrEmail = v, error = null) }
    fun onLoginPasswordChange(v: String) = _uiState.update { it.copy(loginPassword = v, error = null) }

    fun login() {
        val s = _uiState.value
        if (s.loginUsernameOrEmail.isBlank() || s.loginPassword.isBlank()) {
            _uiState.update { it.copy(error = "Lütfen tüm alanları doldurun.") }; return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            tokenManager.clearSession()
            when (val r = authRepository.login(LoginRequest(s.loginUsernameOrEmail.trim(), s.loginPassword))) {
                is ApiResult.Success -> {
                    val (token, user) = r.data
                    tokenManager.saveSession(token, user.id, user.username, user.fullName, user.role.name, user.profileImageUrl)
                    _uiState.update { it.copy(isLoading = false) }
                    sendEvent(UiEvent.Navigate("home"))
                }
                is ApiResult.Error -> _uiState.update { it.copy(isLoading = false, error = r.message) }
                is ApiResult.Loading -> Unit
            }
        }
    }

    // ── Register field değişiklikleri ─────────────────────────────────────────
    fun onRegisterUsernameChange(v: String)        = _uiState.update { it.copy(registerUsername = v, error = null) }
    fun onRegisterFullNameChange(v: String)        = _uiState.update { it.copy(registerFullName = v, error = null) }
    fun onRegisterEmailChange(v: String)           = _uiState.update { it.copy(registerEmail = v, error = null) }
    fun onRegisterPasswordChange(v: String)        = _uiState.update { it.copy(registerPassword = v, error = null) }
    fun onRegisterRoleChange(v: UserRole)          = _uiState.update { it.copy(registerRole = v, error = null) }
    fun onRegisterBranchChange(v: String)          = _uiState.update { it.copy(registerBranch = v, error = null) }
    fun onRegisterExperienceYearsChange(v: String) = _uiState.update { it.copy(registerExperienceYears = v, error = null) }
    fun onRegisterBioChange(v: String)             = _uiState.update { it.copy(registerBio = v, error = null) }

    // ── Fotoğraf yükle ────────────────────────────────────────────────────────
    fun onPhotoSelected(context: Context, uri: Uri) {
        _uiState.update { it.copy(registerPhotoUri = uri, isUploadingPhoto = true, error = null) }
        viewModelScope.launch {
            val file = FileUtils.uriToFile(context, uri) ?: run {
                _uiState.update { it.copy(isUploadingPhoto = false, error = "Dosya okunamadı.") }
                return@launch
            }
            val part = FileUtils.fileToMultipart(file, FileUtils.getMimeType(context, uri), "file")
            when (val r = fileUploadRepository.uploadProfileImage(part)) {
                is ApiResult.Success -> _uiState.update { it.copy(registerPhotoUrl = r.data.fileUrl, isUploadingPhoto = false) }
                is ApiResult.Error   -> _uiState.update { it.copy(isUploadingPhoto = false, error = r.message) }
                is ApiResult.Loading -> Unit
            }
        }
    }

    // ── Adım ileri ───────────────────────────────────────────────────────────
    fun nextStep() {
        val s = _uiState.value
        if (s.registerStep == RegisterStep.REQUIRED) {
            if (s.registerFullName.isBlank() || s.registerUsername.isBlank() ||
                s.registerEmail.isBlank() || s.registerPassword.isBlank()) {
                _uiState.update { it.copy(error = "Lütfen zorunlu alanları doldurun.") }; return
            }
            if (s.registerPassword.length < 6) {
                _uiState.update { it.copy(error = "Şifre en az 6 karakter olmalıdır.") }; return
            }
        }
        val next = when (s.registerStep) {
            RegisterStep.ROLE       -> RegisterStep.REQUIRED
            RegisterStep.REQUIRED   -> if (s.registerRole == UserRole.TEACHER) RegisterStep.BRANCH else RegisterStep.BIO
            RegisterStep.BRANCH     -> RegisterStep.EXPERIENCE
            RegisterStep.EXPERIENCE -> RegisterStep.BIO
            RegisterStep.BIO        -> RegisterStep.PHOTO
            RegisterStep.PHOTO      -> { register(); return }
        }
        _uiState.update { it.copy(registerStep = next, error = null) }
    }

    fun prevStep() {
        val s = _uiState.value
        val prev = when (s.registerStep) {
            RegisterStep.ROLE       -> return
            RegisterStep.REQUIRED   -> RegisterStep.ROLE
            RegisterStep.BRANCH     -> RegisterStep.REQUIRED
            RegisterStep.EXPERIENCE -> RegisterStep.BRANCH
            RegisterStep.BIO        -> if (s.registerRole == UserRole.TEACHER) RegisterStep.EXPERIENCE else RegisterStep.REQUIRED
            RegisterStep.PHOTO      -> RegisterStep.BIO
        }
        _uiState.update { it.copy(registerStep = prev, error = null) }
    }

    fun skipStep() {
        val s = _uiState.value
        when (s.registerStep) {
            RegisterStep.BRANCH     -> _uiState.update { it.copy(registerBranch = "", registerStep = RegisterStep.EXPERIENCE, error = null) }
            RegisterStep.EXPERIENCE -> _uiState.update { it.copy(registerExperienceYears = "", registerStep = RegisterStep.BIO, error = null) }
            RegisterStep.BIO        -> _uiState.update { it.copy(registerBio = "", registerStep = RegisterStep.PHOTO, error = null) }
            RegisterStep.PHOTO      -> register()
            else -> Unit
        }
    }

    fun resetRegister() = _uiState.update { it.copy(
        registerStep = RegisterStep.ROLE, registerUsername = "", registerFullName = "",
        registerEmail = "", registerPassword = "", registerRole = UserRole.USER,
        registerBranch = "", registerExperienceYears = "", registerBio = "",
        registerPhotoUri = null, registerPhotoUrl = null, error = null
    )}

    private fun register() {
        val s = _uiState.value
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            when (val r = authRepository.register(RegisterRequest(
                username        = s.registerUsername.trim(),
                fullName        = s.registerFullName.trim(),
                email           = s.registerEmail.trim(),
                password        = s.registerPassword,
                role            = s.registerRole.name,
                branch          = s.registerBranch.ifBlank { null },
                experienceYears = s.registerExperienceYears.toIntOrNull(),
                bio             = s.registerBio.ifBlank { null },
                profileImageUrl = s.registerPhotoUrl
            ))) {
                is ApiResult.Success -> {
                    val (token, user) = r.data
                    tokenManager.saveSession(token, user.id, user.username, user.fullName, user.role.name, user.profileImageUrl)
                    _uiState.update { it.copy(isLoading = false) }
                    sendEvent(UiEvent.Navigate("home"))
                }
                is ApiResult.Error -> _uiState.update { it.copy(isLoading = false, error = r.message) }
                is ApiResult.Loading -> Unit
            }
        }
    }
}