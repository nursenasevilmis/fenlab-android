package com.nursena.fenlab_android.core.base

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nursena.fenlab_android.core.network.ApiResult
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

// ── Tek seferlik UI olayları ───────────────────────────────────────────────
sealed class UiEvent {
    data class ShowSnackbar(val message: String) : UiEvent()
    data class Navigate(val route: String)       : UiEvent()
    object NavigateBack                          : UiEvent()
    object LoggedOut                             : UiEvent()
}

// ── Genel UI durumu ───────────────────────────────────────────────────────
sealed class UiState<out T> {
    object Idle                                  : UiState<Nothing>()
    object Loading                               : UiState<Nothing>()
    data class Success<T>(val data: T)           : UiState<T>()
    data class Error(val message: String)        : UiState<Nothing>()
}

abstract class BaseViewModel : ViewModel() {

    private val _eventChannel = Channel<UiEvent>(Channel.BUFFERED)
    val eventFlow: Flow<UiEvent> = _eventChannel.receiveAsFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // ── API çağrısını güvenli çalıştır ────────────────────────────────────
    protected fun <T> launchRequest(
        onLoading: (() -> Unit)? = null,
        onSuccess: (T) -> Unit,
        onError: ((String) -> Unit)? = null,
        request: suspend () -> ApiResult<T>
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            onLoading?.invoke()

            when (val result = request()) {
                is ApiResult.Success -> {
                    _isLoading.value = false
                    onSuccess(result.data)
                }
                is ApiResult.Error -> {
                    _isLoading.value = false
                    onError?.invoke(result.message)
                        ?: sendEvent(UiEvent.ShowSnackbar(result.message))
                }
                is ApiResult.Loading -> Unit
            }
        }
    }

    // ── Event yardımcıları ─────────────────────────────────────────────────
    protected fun sendEvent(event: UiEvent) {
        viewModelScope.launch { _eventChannel.send(event) }
    }

    protected fun showSnackbar(message: String) =
        sendEvent(UiEvent.ShowSnackbar(message))

    protected fun navigateTo(route: String) =
        sendEvent(UiEvent.Navigate(route))

    protected fun navigateBack() =
        sendEvent(UiEvent.NavigateBack)
}