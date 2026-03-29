package com.nursena.fenlab_android.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.nursena.fenlab_android.domain.model.enums.UserRole
import com.nursena.fenlab_android.ui.theme.*

sealed class BottomNavItem(
    val route: String,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    object Home      : BottomNavItem("home",      "Anasayfa",  Icons.Filled.Home,     Icons.Outlined.Home)
    object Search    : BottomNavItem("search",    "Ara",       Icons.Filled.Search,   Icons.Outlined.Search)
    object Favorites : BottomNavItem("favorites", "Favoriler", Icons.Filled.Favorite, Icons.Outlined.FavoriteBorder)
    object Profile   : BottomNavItem("profile",   "Profil",    Icons.Filled.Person,   Icons.Outlined.Person)
}

// Modern Frost bottom bar — temaya tam uyumlu, ne çok açık ne çok koyu
private val BarBg         = Color(0xDDF5F7FA)   // %87 GradientStart — buzlu cam
private val BarBorder     = Color(0xE6FFFFFF)   // %90 beyaz — parlak kenar
private val BarShadow     = Color(0x14000000)   // hafif gölge
private val IconActive    = Color(0xFF1E88E5)   // FrostAccentDark — seçili ikon
private val IconBgActive  = Color(0x1A64B5F6)   // %10 FrostAccent highlight
private val IconInactive  = Color(0xFF90A4AE)   // Blue Gray 300 — seçilmemiş

@Composable
fun FenlabBottomBar(
    navController: NavController,
    currentUserRole: UserRole,
    onAddClick: () -> Unit,
    hasNotification: Boolean = false
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val isTeacher    = currentUserRole == UserRole.TEACHER

    fun goTo(route: String) {
        navController.navigate(route) {
            popUpTo("home") { saveState = true }
            launchSingleTop = true
            restoreState    = true
        }
    }

    Box(
        modifier         = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .height(if (isTeacher) 88.dp else 76.dp)
            .background(Color.Transparent),
        contentAlignment = Alignment.BottomCenter
    ) {
        // ── Frosted pill ────────────────────────────────────────────────────
        Box(
            modifier = Modifier
                .padding(horizontal = 20.dp)
                .padding(bottom = 10.dp)
                .fillMaxWidth()
                .height(62.dp)
                .shadow(12.dp, RoundedCornerShape(31.dp), ambientColor = Color(0x1A000000))
                .clip(RoundedCornerShape(31.dp))
                .background(BarBg)
        ) {
            // Üst parlak kenar — frost efekti
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(
                        Brush.horizontalGradient(
                            listOf(Color.Transparent, BarBorder, BarBorder, Color.Transparent)
                        )
                    )
            )

            Row(
                modifier              = Modifier.fillMaxSize().padding(horizontal = 12.dp),
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                FrostNavItem(
                    icon       = if (currentRoute == "home") Icons.Filled.Home else Icons.Outlined.Home,
                    isSelected = currentRoute == "home",
                    onClick    = { goTo("home") }
                )
                FrostNavItem(
                    icon       = if (currentRoute == "search") Icons.Filled.Search else Icons.Outlined.Search,
                    isSelected = currentRoute == "search",
                    onClick    = { goTo("search") }
                )
                if (isTeacher) Spacer(Modifier.width(52.dp))
                FrostNavItem(
                    icon       = if (currentRoute == "favorites") Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                    isSelected = currentRoute == "favorites",
                    onClick    = { goTo("favorites") }
                )
                FrostNavItem(
                    icon       = if (currentRoute == "profile") Icons.Filled.Person else Icons.Outlined.Person,
                    isSelected = currentRoute == "profile",
                    onClick    = { goTo("profile") },
                    badge      = hasNotification
                )
            }
        }

        // ── FAB — teacher only ───────────────────────────────────────────────
        if (isTeacher) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .size(54.dp)
                    .shadow(10.dp, CircleShape)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(listOf(FrostAccent, FrostAccentDark))
                    )
                    .clickable(onClick = onAddClick),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector        = Icons.Default.Add,
                    contentDescription = "Deney Ekle",
                    tint               = Color.White,
                    modifier           = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Composable
private fun FrostNavItem(
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    badge: Boolean = false
) {
    Box(
        modifier         = Modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(if (isSelected) IconBgActive else Color.Transparent)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector        = icon,
            contentDescription = null,
            tint               = if (isSelected) IconActive else IconInactive,
            modifier           = Modifier.size(22.dp)
        )
        if (badge) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(Red400)
                    .align(Alignment.TopEnd)
                    .offset(x = (-8).dp, y = 8.dp)
            )
        }
    }
}
