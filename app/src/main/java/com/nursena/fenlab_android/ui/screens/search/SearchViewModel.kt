package com.nursena.fenlab_android.ui.screens.search

import androidx.lifecycle.viewModelScope
import com.nursena.fenlab_android.core.base.BaseViewModel
import com.nursena.fenlab_android.core.datastore.TokenManager
import com.nursena.fenlab_android.core.network.ApiResult
import com.nursena.fenlab_android.domain.model.Experiment
import com.nursena.fenlab_android.domain.model.User
import com.nursena.fenlab_android.domain.repository.ExperimentRepository
import com.nursena.fenlab_android.domain.repository.FavoriteRepository
import com.nursena.fenlab_android.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SearchUiState(
    val query: String                = "",
    val results: List<Experiment>    = emptyList(),
    val userResults: List<User>      = emptyList(),
    val recentSearches: List<String> = emptyList(),
    val isLoading: Boolean           = false,
    val isEmpty: Boolean             = false,
    val error: String?               = null
)

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val experimentRepository: ExperimentRepository,
    private val favoriteRepository: FavoriteRepository,
    private val userRepository: UserRepository,
    private val tokenManager: TokenManager
) : BaseViewModel() {

    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    private var debounceJob: Job? = null

    init {
        loadRecentSearches()
    }

    // Her tab geçişinde çağrılır — güncel kullanıcının aramalarını yükler
    fun loadRecentSearches() {
        viewModelScope.launch {
            val saved = tokenManager.getRecentSearches()
            _uiState.update { it.copy(recentSearches = saved) }
        }
    }

    fun onQueryChange(query: String) {
        _uiState.update { it.copy(query = query, error = null) }
        debounceJob?.cancel()
        if (query.isBlank()) {
            _uiState.update { it.copy(results = emptyList(), userResults = emptyList(), isEmpty = false, isLoading = false) }
            return
        }
        debounceJob = viewModelScope.launch {
            delay(400)
            search(query)
        }
    }

    fun onRecentClick(term: String) = onQueryChange(term)

    fun removeRecent(term: String) {
        viewModelScope.launch {
            tokenManager.removeRecentSearch(term)
            _uiState.update { it.copy(recentSearches = it.recentSearches - term) }
        }
    }

    fun clearRecents() {
        viewModelScope.launch {
            tokenManager.clearRecentSearches()
            _uiState.update { it.copy(recentSearches = emptyList()) }
        }
    }

    private suspend fun search(query: String) {
        _uiState.update { it.copy(isLoading = true) }
        try {
            val expDeferred  = viewModelScope.async { experimentRepository.getAllExperiments(search = query, size = 30) }
            val userDeferred = viewModelScope.async { userRepository.searchUsers(query) }
            val expResult    = expDeferred.await()
            val userResult   = userDeferred.await()

            val experiments = if (expResult  is ApiResult.Success) expResult.data.content  else emptyList()
            val users       = if (userResult is ApiResult.Success) userResult.data          else emptyList()

            // En az 2 karakter olan aramaları kaydet
            if (query.length >= 2) {
                tokenManager.addRecentSearch(query)
                _uiState.update { it.copy(recentSearches = tokenManager.getRecentSearches()) }
            }

            _uiState.update {
                it.copy(
                    results     = experiments,
                    userResults = users,
                    isLoading   = false,
                    isEmpty     = experiments.isEmpty() && users.isEmpty(),
                    error       = if (expResult is ApiResult.Error) expResult.message else null
                )
            }
        } catch (e: CancellationException) { throw e }
    }

    fun toggleFavorite(experiment: Experiment) {
        viewModelScope.launch {
            val isFav = experiment.isFavoritedByCurrentUser
            fun toggle(list: List<Experiment>) = list.map {
                if (it.id == experiment.id)
                    it.copy(isFavoritedByCurrentUser = !isFav,
                        favoriteCount = if (isFav) it.favoriteCount - 1 else it.favoriteCount + 1)
                else it
            }
            _uiState.update { it.copy(results = toggle(it.results)) }
            val result = if (isFav) favoriteRepository.removeFromFavorites(experiment.id)
            else       favoriteRepository.addToFavorites(experiment.id)
            if (result is ApiResult.Error) {
                fun revert(list: List<Experiment>) = list.map {
                    if (it.id == experiment.id)
                        it.copy(isFavoritedByCurrentUser = isFav, favoriteCount = experiment.favoriteCount)
                    else it
                }
                _uiState.update { it.copy(results = revert(it.results)) }
                showSnackbar(result.message)
            }
        }
    }
}