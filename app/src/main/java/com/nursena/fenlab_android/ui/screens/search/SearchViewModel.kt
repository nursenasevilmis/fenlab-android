package com.nursena.fenlab_android.ui.screens.search

import androidx.lifecycle.viewModelScope
import com.nursena.fenlab_android.core.base.BaseViewModel
import com.nursena.fenlab_android.core.network.ApiResult
import com.nursena.fenlab_android.domain.model.Experiment
import com.nursena.fenlab_android.domain.repository.ExperimentRepository
import com.nursena.fenlab_android.domain.repository.FavoriteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

// Trend aramalar — ileride backend'den de çekilebilir
val TRENDING_SEARCHES = listOf(
    "Kimya", "Fizik", "Çevre", "Mıknatıs", "Volkan",
    "Optik", "Asit-Baz", "Elektrik", "Su Döngüsü", "Bitki"
)

data class SearchUiState(
    val query: String             = "",
    val results: List<Experiment> = emptyList(),
    val allExperiments: List<Experiment> = emptyList(), // boş sorguda tümünü göster
    val isLoading: Boolean        = false,
    val isLoadingAll: Boolean     = false,
    val isEmpty: Boolean          = false,
    val error: String?            = null
)

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val experimentRepository: ExperimentRepository,
    private val favoriteRepository: FavoriteRepository
) : BaseViewModel() {

    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    private var debounceJob: Job? = null

    init { loadAllExperiments() }

    // Ekran açılınca tüm deneyleri yükle (hint state için)
    private fun loadAllExperiments() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingAll = true) }
            when (val result = experimentRepository.getAllExperiments(size = 20)) {
                is ApiResult.Success -> _uiState.update {
                    it.copy(allExperiments = result.data.content, isLoadingAll = false)
                }
                else -> _uiState.update { it.copy(isLoadingAll = false) }
            }
        }
    }

    fun onQueryChange(query: String) {
        _uiState.update { it.copy(query = query, error = null) }

        debounceJob?.cancel()
        if (query.isBlank()) {
            _uiState.update { it.copy(results = emptyList(), isEmpty = false, isLoading = false) }
            return
        }

        debounceJob = viewModelScope.launch {
            delay(400)
            search(query)
        }
    }

    fun onTrendClick(trend: String) {
        onQueryChange(trend)
    }

    private suspend fun search(query: String) {
        _uiState.update { it.copy(isLoading = true) }
        try {
            when (val result = experimentRepository.getAllExperiments(search = query, size = 30)) {
                is ApiResult.Success -> _uiState.update {
                    it.copy(
                        results   = result.data.content,
                        isLoading = false,
                        isEmpty   = result.data.content.isEmpty()
                    )
                }
                is ApiResult.Error -> _uiState.update {
                    it.copy(isLoading = false, error = result.message)
                }
                is ApiResult.Loading -> Unit
            }
        } catch (e: CancellationException) {
            throw e
        }
    }

    fun toggleFavorite(experiment: Experiment) {
        viewModelScope.launch {
            val isFav = experiment.isFavoritedByCurrentUser

            // Optimistic — ikisini de güncelle
            fun updateList(list: List<Experiment>) = list.map {
                if (it.id == experiment.id)
                    it.copy(
                        isFavoritedByCurrentUser = !isFav,
                        favoriteCount = if (isFav) it.favoriteCount - 1 else it.favoriteCount + 1
                    )
                else it
            }
            _uiState.update { it.copy(
                results       = updateList(it.results),
                allExperiments = updateList(it.allExperiments)
            )}

            val result = if (isFav)
                favoriteRepository.removeFromFavorites(experiment.id)
            else
                favoriteRepository.addToFavorites(experiment.id)

            if (result is ApiResult.Error) {
                fun revertList(list: List<Experiment>) = list.map {
                    if (it.id == experiment.id)
                        it.copy(isFavoritedByCurrentUser = isFav, favoriteCount = experiment.favoriteCount)
                    else it
                }
                _uiState.update { it.copy(
                    results        = revertList(it.results),
                    allExperiments = revertList(it.allExperiments)
                )}
                showSnackbar(result.message)
            }
        }
    }
}