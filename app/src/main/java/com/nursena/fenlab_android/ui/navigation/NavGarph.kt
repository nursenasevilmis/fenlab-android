package com.nursena.fenlab_android.ui.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.sp
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.material3.Text
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.nursena.fenlab_android.ui.theme.GradientEnd
import com.nursena.fenlab_android.ui.theme.GradientMid
import com.nursena.fenlab_android.ui.theme.GradientStart
import com.nursena.fenlab_android.ui.theme.LightBg
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

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

    var userRole by remember { mutableStateOf(UserRole.USER) }
    LaunchedEffect(currentRoute) {
        if (currentRoute in bottomBarRoutes) {
            val roleStr = tokenManager.getRole()
            userRole = if (roleStr == "TEACHER") UserRole.TEACHER else UserRole.USER
        }
    }

    // ── Global 401 Session Expired — her yerde dinlenir ──────────────────────
    // NavGraph doğrudan ViewModel'lere erişemez, bu yüzden BaseRepository'den
    // gelen 401 hata mesajını FavoritesViewModel ve ProfileViewModel handle eder.
    // Aşağıda manuel logout butonu SessionExpired eventi ile tetiklenir.

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(GradientStart, GradientMid, GradientEnd)
                )
            )
    ) {
        NavHost(
            navController    = navController,
            startDestination = Routes.SPLASH,   // ← SPLASH ile başla
            modifier           = Modifier.fillMaxSize(),
            enterTransition    = { fadeIn(tween(200)) + slideInHorizontally(tween(200)) { it / 8 } },
            exitTransition     = { fadeOut(tween(160)) + slideOutHorizontally(tween(160)) { -it / 8 } },
            popEnterTransition = { fadeIn(tween(200)) + slideInHorizontally(tween(200)) { -it / 8 } },
            popExitTransition  = { fadeOut(tween(160)) + slideOutHorizontally(tween(160)) { it / 8 } }
        ) {

                // ── Splash — token kontrol eder, flash olmadan yönlendirir ───────
                composable(Routes.SPLASH) {
                    SplashDecision(
                        tokenManager = tokenManager,
                        onLoggedIn = {
                            navController.navigate(Routes.HOME) {
                                popUpTo(Routes.SPLASH) { inclusive = true }
                            }
                        },
                        onGuest = {
                            navController.navigate(Routes.AUTH) {
                                popUpTo(Routes.SPLASH) { inclusive = true }
                            }
                        }
                    )
                }

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
                        onUserClick = { userId -> navController.navigate(Routes.profile(userId)) }
                    )
                }

                composable(Routes.FAVORITES) {
                    FavoritesScreen(
                        onExperimentClick = { id -> navController.navigate(Routes.detail(id)) },
                        onSessionExpired = {
                            navController.navigate(Routes.AUTH) {
                                popUpTo(0) { inclusive = true }
                            }
                        }
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
                    route = Routes.PROFILE_USER,
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
                    route = Routes.DETAIL,
                    arguments = listOf(androidx.navigation.navArgument("experimentId") {
                        type = androidx.navigation.NavType.LongType
                    })
                ) {
                    ExperimentDetailScreen(
                        onBack = { navController.popBackStack() },
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

        // ── Bottom Bar overlay — gradient üstünde, tam transparan zemin ──────
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

// ─────────────────────────────────────────────────────────────────────────────
// Splash karar ekranı — kullanıcıya hiç gösterilmez, sadece yönlendirir
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun SplashDecision(
    tokenManager: TokenManager,
    onLoggedIn: () -> Unit,
    onGuest: () -> Unit
) {
    // Token kontrolü yapılırken DarkBg göster — auth ekranı flash'lamaz
    Box(modifier = Modifier.fillMaxSize().background(Color(0xFFF5F7FA)), contentAlignment = Alignment.Center) {
        Text("⚗️", fontSize = 40.sp, fontWeight = FontWeight.Bold)
    }

    LaunchedEffect(Unit) {
        if (tokenManager.isLoggedIn()) onLoggedIn() else onGuest()
    }
}