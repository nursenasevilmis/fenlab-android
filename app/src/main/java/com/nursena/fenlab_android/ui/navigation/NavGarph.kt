package com.nursena.fenlab_android.ui.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
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
import com.nursena.fenlab_android.ui.screens.splash.SplashScreen
import com.nursena.fenlab_android.ui.theme.GradientEnd
import com.nursena.fenlab_android.ui.theme.GradientMid
import com.nursena.fenlab_android.ui.theme.GradientStart
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent

private val bottomBarRoutes = setOf(
    Routes.HOME, Routes.SEARCH, Routes.FAVORITES, Routes.PROFILE
)

object Routes {
    const val SPLASH       = "splash"
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

@EntryPoint
@InstallIn(SingletonComponent::class)
interface TokenManagerEntryPoint {
    fun tokenManager(): TokenManager
}

@Composable
fun FenlabNavGraph(
    showSplash: Boolean = true,
    initiallyLoggedIn: Boolean = false
) {
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

    // ── Kullanıcı rolü — her bottom bar ekranına geçişte güncellenir ──────────
    var userRole by remember { mutableStateOf(UserRole.USER) }
    LaunchedEffect(currentRoute) {
        if (currentRoute in bottomBarRoutes) {
            val roleStr = tokenManager.getRole()
            userRole = if (roleStr == "TEACHER") UserRole.TEACHER else UserRole.USER
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(listOf(GradientStart, GradientMid, GradientEnd))
            )
    ) {
        NavHost(
            navController    = navController,
            startDestination = Routes.SPLASH,
            modifier           = Modifier.fillMaxSize(),
            enterTransition    = { fadeIn(tween(200)) + slideInHorizontally(tween(200)) { it / 8 } },
            exitTransition     = { fadeOut(tween(160)) + slideOutHorizontally(tween(160)) { -it / 8 } },
            popEnterTransition = { fadeIn(tween(200)) + slideInHorizontally(tween(200)) { -it / 8 } },
            popExitTransition  = { fadeOut(tween(160)) + slideOutHorizontally(tween(160)) { it / 8 } }
        ) {

            // ── Splash ──────────────────────────────────────────────────────
            composable(
                route = Routes.SPLASH,
                exitTransition = { fadeOut(tween(350, easing = FastOutSlowInEasing)) }
            ) {
                SplashScreen(
                    onAnimationEnd = {
                        if (initiallyLoggedIn) {
                            navController.navigate(Routes.HOME) {
                                popUpTo(Routes.SPLASH) { inclusive = true }
                            }
                        } else {
                            navController.navigate(Routes.AUTH) {
                                popUpTo(Routes.SPLASH) { inclusive = true }
                            }
                        }
                    }
                )
            }

            // ── Auth ────────────────────────────────────────────────────────
            composable(
                route = Routes.AUTH,
                enterTransition = {
                    scaleIn(
                        animationSpec   = tween(550, easing = FastOutSlowInEasing),
                        initialScale    = 0.0f,
                        transformOrigin = TransformOrigin(0.5f, 0.5f)
                    ) + fadeIn(tween(400, delayMillis = 80))
                }
            ) {
                AuthScreen(
                    onNavigateHome = {
                        navController.navigate(Routes.HOME) {
                            popUpTo(Routes.AUTH) { inclusive = true }
                        }
                    }
                )
            }

            // ── Home ────────────────────────────────────────────────────────
            composable(
                route = Routes.HOME,
                enterTransition = {
                    if (initialState.destination.route == Routes.SPLASH) {
                        scaleIn(
                            animationSpec   = tween(550, easing = FastOutSlowInEasing),
                            initialScale    = 0.0f,
                            transformOrigin = TransformOrigin(0.5f, 0.5f)
                        ) + fadeIn(tween(400, delayMillis = 80))
                    } else {
                        fadeIn(tween(200)) + slideInHorizontally(tween(200)) { it / 8 }
                    }
                }
            ) {
                HomeScreen(onExperimentClick = { id -> navController.navigate(Routes.detail(id)) })
            }

            composable(Routes.SEARCH) {
                SearchScreen(
                    onExperimentClick = { id -> navController.navigate(Routes.detail(id)) },
                    onUserClick       = { userId -> navController.navigate(Routes.profile(userId)) }
                )
            }

            composable(Routes.FAVORITES) {
                FavoritesScreen(
                    onExperimentClick = { id -> navController.navigate(Routes.detail(id)) },
                    onSessionExpired  = {
                        navController.navigate(Routes.AUTH) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }

            composable(Routes.PROFILE) {
                ProfileScreen(
                    onExperimentClick = { id -> navController.navigate(Routes.detail(id)) },
                    onLogout          = {
                        navController.navigate(Routes.AUTH) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }

            composable(
                route     = Routes.PROFILE_USER,
                arguments = listOf(androidx.navigation.navArgument("userId") {
                    type = androidx.navigation.NavType.LongType
                })
            ) {
                ProfileScreen(
                    onExperimentClick = { id -> navController.navigate(Routes.detail(id)) },
                    onLogout          = {
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
                    onBack      = { navController.popBackStack() },
                    onPublished = { id ->
                        navController.navigate(Routes.detail(id)) {
                            popUpTo(Routes.ADD) { inclusive = true }
                        }
                    }
                )
            }
        }

        // ── Bottom Bar overlay ───────────────────────────────────────────────
        if (currentRoute in bottomBarRoutes) {
            Box(
                modifier         = Modifier.fillMaxSize(),
                contentAlignment = Alignment.BottomCenter
            ) {
                FenlabBottomBar(
                    navController   = navController,
                    currentUserRole = userRole,
                    onAddClick      = { navController.navigate(Routes.ADD) }
                )
            }
        }
    }
}
