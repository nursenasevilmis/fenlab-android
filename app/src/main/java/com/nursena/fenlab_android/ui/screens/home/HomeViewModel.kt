package com.nursena.fenlab_android.ui.screens.home

import androidx.lifecycle.viewModelScope
import com.nursena.fenlab_android.core.base.BaseViewModel
import com.nursena.fenlab_android.core.datastore.TokenManager
import com.nursena.fenlab_android.core.network.ApiResult
import com.nursena.fenlab_android.domain.model.Experiment
import com.nursena.fenlab_android.domain.model.enums.DifficultyLevel
import com.nursena.fenlab_android.domain.model.enums.EnvironmentType
import com.nursena.fenlab_android.domain.model.enums.GradeGroup
import com.nursena.fenlab_android.domain.model.enums.SortType
import com.nursena.fenlab_android.domain.model.enums.SubjectType
import com.nursena.fenlab_android.domain.repository.ExperimentRepository
import com.nursena.fenlab_android.domain.repository.FavoriteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val experiments: List<Experiment>         = emptyList(),
    val isLoading: Boolean                    = false,
    val isLoadingMore: Boolean                = false,
    val error: String?                        = null,
    val fullName: String                      = "",
    // Filtreler
    val selectedSubject: SubjectType?         = null,
    val selectedEnvironment: EnvironmentType? = null,
    val selectedDifficulty: DifficultyLevel?  = null,
    val selectedGradeGroup: GradeGroup?       = null,
    val sortType: SortType                    = SortType.MOST_RECENT,
    val searchQuery: String                   = "",
    // Sayfalama
    val currentPage: Int     = 0,
    val hasNextPage: Boolean = true
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val experimentRepository: ExperimentRepository,
    private val favoriteRepository: FavoriteRepository,
    private val tokenManager: TokenManager
) : BaseViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadUserInfo()
        loadExperiments()
    }

    private fun loadUserInfo() {
        viewModelScope.launch {
            val name = tokenManager.getFullName() ?: ""
            _uiState.update { it.copy(fullName = name) }
        }
    }

    fun loadExperiments() {
        _uiState.update {
            it.copy(isLoading = true, error = null, currentPage = 0,
                experiments = emptyList(), hasNextPage = true)
        }
        fetchPage(0)
    }

    fun loadNextPage() {
        val s = _uiState.value
        if (s.isLoadingMore || !s.hasNextPage) return
        _uiState.update { it.copy(isLoadingMore = true) }
        fetchPage(s.currentPage + 1)
    }

    private fun fetchPage(page: Int) {
        viewModelScope.launch {
            val s = _uiState.value
            when (val result = experimentRepository.getAllExperiments(
                search      = s.searchQuery.ifBlank { null },
                subject     = s.selectedSubject?.name,
                environment = s.selectedEnvironment?.name,
                difficulty  = s.selectedDifficulty?.name,
                minGradeLevel = s.selectedGradeGroup?.minGrade,
                maxGradeLevel = s.selectedGradeGroup?.maxGrade,
                sortType    = s.sortType.name,
                page        = page,
                size        = 20
            )) {
                is ApiResult.Success -> {
                    val data = result.data
                    _uiState.update {
                        it.copy(
                            experiments   = if (page == 0) data.content
                            else it.experiments + data.content,
                            isLoading     = false,
                            isLoadingMore = false,
                            currentPage   = page,
                            hasNextPage   = data.hasNextPage,
                            error         = null
                        )
                    }
                }
                is ApiResult.Error -> _uiState.update {
                    it.copy(isLoading = false, isLoadingMore = false, error = result.message)
                }
                is ApiResult.Loading -> Unit
            }
        }
    }

    fun toggleFavorite(experiment: Experiment) {
        viewModelScope.launch {
            val isFav = experiment.isFavoritedByCurrentUser
            _uiState.update { state ->
                state.copy(experiments = state.experiments.map {
                    if (it.id == experiment.id) it.copy(
                        isFavoritedByCurrentUser = !isFav,
                        favoriteCount = if (isFav) it.favoriteCount - 1 else it.favoriteCount + 1
                    ) else it
                })
            }
            val result = if (isFav)
                favoriteRepository.removeFromFavorites(experiment.id)
            else
                favoriteRepository.addToFavorites(experiment.id)

            if (result is ApiResult.Error) {
                _uiState.update { state ->
                    state.copy(experiments = state.experiments.map {
                        if (it.id == experiment.id) it.copy(
                            isFavoritedByCurrentUser = isFav,
                            favoriteCount = experiment.favoriteCount
                        ) else it
                    })
                }
                showSnackbar(result.message)
            }
        }
    }

    fun applyFilters(
        subject: SubjectType?,
        environment: EnvironmentType?,
        difficulty: DifficultyLevel?,
        gradeGroup: GradeGroup? = null
    ) {
        _uiState.update {
            it.copy(selectedSubject = subject, selectedEnvironment = environment,
                selectedDifficulty = difficulty, selectedGradeGroup = gradeGroup)
        }
        loadExperiments()
    }

    fun applySort(sortType: SortType) {
        _uiState.update { it.copy(sortType = sortType) }
        loadExperiments()
    }

    fun onSearchQueryChange(query: String) = _uiState.update { it.copy(searchQuery = query) }
    fun search() = loadExperiments()
    fun showFilterSheet() = Unit
    fun showSortSheet()   = Unit

    fun clearFilters() {
        _uiState.update {
            it.copy(selectedSubject = null, selectedEnvironment = null,
                selectedDifficulty = null, selectedGradeGroup = null)
        }
        loadExperiments()
    }
}