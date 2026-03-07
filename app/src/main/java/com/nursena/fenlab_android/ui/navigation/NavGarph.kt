package com.nursena.fenlab_android.ui.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.nursena.fenlab_android.core.datastore.TokenManager
import com.nursena.fenlab_android.domain.model.enums.UserRole
import com.nursena.fenlab_android.ui.FenlabBottomBar
import com.nursena.fenlab_android.ui.screens.favorites.FavoritesScreen
import com.nursena.fenlab_android.ui.screens.add.AddExperimentScreen
import com.nursena.fenlab_android.ui.screens.auth.AuthScreen
import com.nursena.fenlab_android.ui.screens.home.HomeScreen
import com.nursena.fenlab_android.ui.screens.profile.ProfileScreen
import com.nursena.fenlab_android.ui.screens.search.SearchScreen
import com.nursena.fenlab_android.ui.theme.DarkBg
import dagger.hilt.android.EntryPointAccessors
import androidx.compose.ui.platform.LocalContext
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

// Rotalar
object Routes {
    const val HOME      = "home"
    const val SEARCH    = "search"
    const val FAVORITES = "favorites"
    const val PROFILE   = "profile"
    const val DETAIL    = "detail/{experimentId}"
    const val ADD       = "add"
    const val AUTH      = "auth"

    fun detail(id: Long) = "detail/$id"
}

private val bottomBarRoutes = setOf(
    Routes.HOME, Routes.SEARCH, Routes.FAVORITES, Routes.PROFILE
)

// TokenManager'a Hilt EntryPoint ile erişim
@EntryPoint
@InstallIn(SingletonComponent::class)
interface TokenManagerEntryPoint {
    fun tokenManager(): TokenManager
}

@Composable
fun FenlabNavGraph() {
    val navController = rememberNavController()
    val navBackStack  by navController.currentBackStackEntryAsState()
    val currentRoute  = navBackStack?.destination?.route

    // TokenManager'dan gerçek rolü al
    val context = LocalContext.current
    val tokenManager = remember {
        EntryPointAccessors.fromApplication(
            context.applicationContext,
            TokenManagerEntryPoint::class.java
        ).tokenManager()
    }

    // Role'u asenkron oku, default USER (güvenli taraf)
    var userRole by remember { mutableStateOf(UserRole.USER) }
    LaunchedEffect(Unit) {
        val roleStr = tokenManager.getRole()
        userRole = if (roleStr == "TEACHER") UserRole.TEACHER else UserRole.USER
    }

    Scaffold(
        containerColor = DarkBg,
        bottomBar = {
            if (currentRoute in bottomBarRoutes) {
                FenlabBottomBar(
                    navController   = navController,
                    currentUserRole = userRole,
                    onAddClick      = { navController.navigate(Routes.ADD) }
                )
            }
        },
        contentWindowInsets = WindowInsets(0)
    ) { innerPadding ->

        NavHost(
            navController    = navController,
            startDestination = Routes.AUTH,
            modifier         = Modifier.padding(innerPadding),
            enterTransition  = {
                fadeIn(tween(220)) + slideInHorizontally(tween(220)) { it / 6 }
            },
            exitTransition   = {
                fadeOut(tween(180)) + slideOutHorizontally(tween(180)) { -it / 6 }
            },
            popEnterTransition = {
                fadeIn(tween(220)) + slideInHorizontally(tween(220)) { -it / 6 }
            },
            popExitTransition  = {
                fadeOut(tween(180)) + slideOutHorizontally(tween(180)) { it / 6 }
            }
        ) {
            composable(Routes.AUTH) {
                AuthScreen(
                    onNavigateHome = {
                        navController.navigate(Routes.HOME) {
                            popUpTo(Routes.AUTH) { inclusive = true }
                        }
                    }
                )
            }

            composable(Routes.HOME) {
                HomeScreen(
                    onExperimentClick = { id -> navController.navigate(Routes.detail(id)) }
                )
            }

            composable(Routes.SEARCH) {
                SearchScreen(
                    onExperimentClick = { id -> navController.navigate(Routes.detail(id)) }
                )
            }

            composable(Routes.FAVORITES) {
                FavoritesScreen(
                    onExperimentClick = { id -> navController.navigate(Routes.detail(id)) }
                )
            }

            composable(Routes.PROFILE) {
                ProfileScreen(
                    onExperimentClick = { id -> navController.navigate(Routes.detail(id)) },
                    onLogout = {
                        navController.navigate(Routes.AUTH) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }

            composable(
                route     = Routes.DETAIL,
                arguments = listOf(navArgument("experimentId") { type = NavType.LongType })
            ) {
                com.nursena.fenlab_android.ui.components.EmptyState(
                    emoji    = "🔬",
                    title    = "Detay sayfası",
                    subtitle = "Yakında eklenecek"
                )
            }

            composable(Routes.ADD) {
                AddExperimentScreen(
                    onBack = { navController.popBackStack() },
                    onPublished = { id ->
                        navController.navigate(Routes.detail(id)) {
                            popUpTo(Routes.ADD) { inclusive = true }
                        }
                    }
                )
            }
        }
    }
}