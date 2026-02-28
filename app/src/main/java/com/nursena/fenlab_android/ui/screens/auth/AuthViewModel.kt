package com.nursena.fenlab_android.ui.screens.auth

import androidx.lifecycle.viewModelScope
import com.nursena.fenlab_android.core.base.BaseViewModel
import com.nursena.fenlab_android.core.base.UiEvent
import com.nursena.fenlab_android.core.datastore.TokenManager
import com.nursena.fenlab_android.core.network.ApiResult
import com.nursena.fenlab_android.data.remote.dto.request.LoginRequest
import com.nursena.fenlab_android.data.remote.dto.request.RegisterRequest
import com.nursena.fenlab_android.domain.model.enums.UserRole
import com.nursena.fenlab_android.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AuthUiState(
    // Login form
    val loginUsernameOrEmail: String = "",
    val loginPassword: String = "",
    // Register form
    val registerUsername: String = "",
    val registerFullName: String = "",
    val registerEmail: String = "",
    val registerPassword: String = "",
    val registerRole: UserRole = UserRole.USER,
    val registerBranch: String = "",
    // Genel
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val tokenManager: TokenManager
) : BaseViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    // ── Splash: oturum var mı? ────────────────────────────────────────────────
    fun checkSession() {
        viewModelScope.launch {
            val loggedIn = tokenManager.isLoggedIn()
            if (loggedIn) sendEvent(UiEvent.Navigate("home"))
            else          sendEvent(UiEvent.Navigate("login"))
        }
    }

    // ── Login form ────────────────────────────────────────────────────────────
    fun onLoginUsernameChange(v: String)  = _uiState.update { it.copy(loginUsernameOrEmail = v, error = null) }
    fun onLoginPasswordChange(v: String)  = _uiState.update { it.copy(loginPassword = v, error = null) }

    fun login() {
        val state = _uiState.value
        if (state.loginUsernameOrEmail.isBlank() || state.loginPassword.isBlank()) {
            _uiState.update { it.copy(error = "Lütfen tüm alanları doldurun.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val result = authRepository.login(
                LoginRequest(
                    usernameOrEmail = state.loginUsernameOrEmail.trim(),
                    password        = state.loginPassword
                )
            )
            when (result) {
                is ApiResult.Success -> {
                    val (token, user) = result.data
                    tokenManager.saveSession(
                        token           = token,
                        userId          = user.id,
                        username        = user.username,
                        fullName        = user.fullName,
                        role            = user.role.name,
                        profileImageUrl = user.profileImageUrl
                    )
                    _uiState.update { it.copy(isLoading = false) }
                    sendEvent(UiEvent.Navigate("home"))
                }
                is ApiResult.Error -> _uiState.update { it.copy(isLoading = false, error = result.message) }
                is ApiResult.Loading -> Unit
            }
        }
    }

    // ── Register form ─────────────────────────────────────────────────────────
    fun onRegisterUsernameChange(v: String)  = _uiState.update { it.copy(registerUsername = v, error = null) }
    fun onRegisterFullNameChange(v: String)  = _uiState.update { it.copy(registerFullName = v, error = null) }
    fun onRegisterEmailChange(v: String)     = _uiState.update { it.copy(registerEmail = v, error = null) }
    fun onRegisterPasswordChange(v: String)  = _uiState.update { it.copy(registerPassword = v, error = null) }
    fun onRegisterRoleChange(v: UserRole)    = _uiState.update { it.copy(registerRole = v, error = null) }
    fun onRegisterBranchChange(v: String)    = _uiState.update { it.copy(registerBranch = v, error = null) }

    fun register() {
        val state = _uiState.value
        if (state.registerUsername.isBlank() || state.registerFullName.isBlank() ||
            state.registerEmail.isBlank()    || state.registerPassword.isBlank()) {
            _uiState.update { it.copy(error = "Lütfen zorunlu alanları doldurun.") }
            return
        }
        if (state.registerPassword.length < 6) {
            _uiState.update { it.copy(error = "Şifre en az 6 karakter olmalıdır.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val result = authRepository.register(
                RegisterRequest(
                    username  = state.registerUsername.trim(),
                    fullName  = state.registerFullName.trim(),
                    email     = state.registerEmail.trim(),
                    password  = state.registerPassword,
                    role      = state.registerRole.name,
                    branch    = state.registerBranch.ifBlank { null }
                )
            )
            when (result) {
                is ApiResult.Success -> {
                    val (token, user) = result.data
                    tokenManager.saveSession(
                        token           = token,
                        userId          = user.id,
                        username        = user.username,
                        fullName        = user.fullName,
                        role            = user.role.name,
                        profileImageUrl = user.profileImageUrl
                    )
                    _uiState.update { it.copy(isLoading = false) }
                    sendEvent(UiEvent.Navigate("home"))
                }
                is ApiResult.Error -> _uiState.update { it.copy(isLoading = false, error = result.message) }
                is ApiResult.Loading -> Unit
            }
        }
    }
}