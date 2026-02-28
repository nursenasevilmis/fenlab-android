package com.nursena.fenlab_android.ui.screens.favorites

import androidx.lifecycle.viewModelScope
import com.nursena.fenlab_android.core.base.BaseViewModel
import com.nursena.fenlab_android.core.network.ApiResult
import com.nursena.fenlab_android.domain.model.Experiment
import com.nursena.fenlab_android.domain.repository.FavoriteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class FavoritesUiState(
    val favorites: List<Experiment> = emptyList(),
    val isLoading: Boolean          = false,
    val error: String?              = null
)

@HiltViewModel
class FavoritesViewModel @Inject constructor(
    private val favoriteRepository: FavoriteRepository
) : BaseViewModel() {

    private val _uiState = MutableStateFlow(FavoritesUiState())
    val uiState: StateFlow<FavoritesUiState> = _uiState.asStateFlow()

    init { loadFavorites() }

    fun loadFavorites() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            when (val result = favoriteRepository.getUserFavorites(size = 50)) {
                is ApiResult.Success -> _uiState.update {
                    it.copy(favorites = result.data.content, isLoading = false)
                }
                is ApiResult.Error   -> _uiState.update {
                    it.copy(isLoading = false, error = result.message)
                }
                is ApiResult.Loading -> Unit
            }
        }
    }

    fun removeFromFavorites(experiment: Experiment) {
        viewModelScope.launch {
            // Optimistic: hemen listeden kaldır
            _uiState.update { it.copy(favorites = it.favorites.filter { f -> f.id != experiment.id }) }
            val result = favoriteRepository.removeFromFavorites(experiment.id)
            if (result is ApiResult.Error) {
                // Geri al
                _uiState.update { it.copy(favorites = it.favorites + experiment) }
                showSnackbar(result.message)
            }
        }
    }
}