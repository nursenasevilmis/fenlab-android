package com.nursena.fenlab_android.ui.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.compose.*
import com.nursena.fenlab_android.core.datastore.TokenManager
import com.nursena.fenlab_android.domain.model.enums.UserRole
import com.nursena.fenlab_android.ui.FenlabBottomBar
import com.nursena.fenlab_android.ui.screens.favorites.FavoritesScreen
import com.nursena.fenlab_android.ui.screens.add.AddExperimentScreen
import com.nursena.fenlab_android.ui.screens.detail.ExperimentDetailScreen
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

object Routes {
    const val HOME         = "home"
    const val SEARCH       = "search"
    const val FAVORITES    = "favorites"
    const val PROFILE      = "profile"
    const val PROFILE_USER = "profile/{userId}"
    const val DETAIL       = "detail/{experimentId}"
    const val ADD          = "add"
    const val AUTH         = "auth"

    fun detail(id: Long)  = "detail/$id"
    fun profile(id: Long) = "profile/$id"
}

private val bottomBarRoutes = setOf(
    Routes.HOME, Routes.SEARCH, Routes.FAVORITES, Routes.PROFILE
)

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

    val context = LocalContext.current
    val tokenManager = remember {
        EntryPointAccessors.fromApplication(
            context.applicationContext,
            TokenManagerEntryPoint::class.java
        ).tokenManager()
    }

    // Rol'ü her route değişiminde yeniden oku (login sonrası güncel olsun)
    var userRole by remember { mutableStateOf(UserRole.USER) }
    LaunchedEffect(currentRoute) {
        if (currentRoute in bottomBarRoutes) {
            val roleStr = tokenManager.getRole()
            userRole = if (roleStr == "TEACHER") UserRole.TEACHER else UserRole.USER
        }
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
            enterTransition  = { fadeIn(tween(200)) + slideInHorizontally(tween(200)) { it / 8 } },
            exitTransition   = { fadeOut(tween(160)) + slideOutHorizontally(tween(160)) { -it / 8 } },
            popEnterTransition = { fadeIn(tween(200)) + slideInHorizontally(tween(200)) { -it / 8 } },
            popExitTransition  = { fadeOut(tween(160)) + slideOutHorizontally(tween(160)) { it / 8 } }
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
                HomeScreen(onExperimentClick = { id -> navController.navigate(Routes.detail(id)) })
            }

            composable(Routes.SEARCH) {
                SearchScreen(
                    onExperimentClick = { id -> navController.navigate(Routes.detail(id)) },
                    onUserClick       = { userId -> navController.navigate(Routes.profile(userId)) }
                )
            }

            composable(Routes.FAVORITES) {
                FavoritesScreen(onExperimentClick = { id -> navController.navigate(Routes.detail(id)) })
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

            // Başka kullanıcının profili
            composable(
                route     = Routes.PROFILE_USER,
                arguments = listOf(androidx.navigation.navArgument("userId") {
                    type = androidx.navigation.NavType.LongType
                })
            ) {
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
                arguments = listOf(androidx.navigation.navArgument("experimentId") {
                    type = androidx.navigation.NavType.LongType
                })
            ) {
                ExperimentDetailScreen(
                    onBack        = { navController.popBackStack() },
                    onAuthorClick = { userId -> navController.navigate(Routes.profile(userId)) }
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