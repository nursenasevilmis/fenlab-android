package com.nursena.fenlab_android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import com.nursena.fenlab_android.domain.model.Experiment
import com.nursena.fenlab_android.ui.screens.home.HomeScreenContent
import com.nursena.fenlab_android.ui.theme.FenlabAndroidTheme
import com.nursena.fenlab_android.ui.preview.MockData

// @AndroidEntryPoint YOK — Hilt kullanmıyoruz, mock modda çalışıyoruz
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FenlabAndroidTheme {
                MockHomeApp()
            }
        }
    }
}

@Composable
fun MockHomeApp() {
    var experiments by remember { mutableStateOf(MockData.mockExperiments) }

    fun toggleFavorite(exp: Experiment) {
        experiments = experiments.map {
            if (it.id == exp.id)
                it.copy(
                    isFavoritedByCurrentUser = !it.isFavoritedByCurrentUser,
                    favoriteCount = if (it.isFavoritedByCurrentUser)
                        it.favoriteCount - 1 else it.favoriteCount + 1
                )
            else it
        }
    }

    HomeScreenContent(
        experiments       = experiments,
        isLoading         = false,
        isLoadingMore     = false,
        error             = null,
        onFavoriteClick   = { toggleFavorite(it) },
        onExperimentClick = { _ -> },
        onFilterClick     = {},
        onSortClick       = {}
    )
}