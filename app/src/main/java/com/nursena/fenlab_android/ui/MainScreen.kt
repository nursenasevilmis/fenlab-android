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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.nursena.fenlab_android.domain.model.enums.UserRole
import com.nursena.fenlab_android.ui.screens.home.FenlabTeal

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

@Composable
fun FenlabBottomBar(
    navController: NavController,
    currentUserRole: UserRole,
    onAddClick: () -> Unit
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    fun goTo(route: String) {
        navController.navigate(route) {
            popUpTo("home") { saveState = true }
            launchSingleTop = true
            restoreState    = true
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(72.dp)
    ) {
        // ── Bar ──────────────────────────────────────────────────────────────
        Surface(
            modifier        = Modifier
                .fillMaxWidth()
                .height(72.dp)
                .align(Alignment.BottomCenter),
            color           = Color.White,
            shadowElevation = 12.dp,
            shape           = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
        ) {
            Row(
                modifier              = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 8.dp),
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                NavTabItem(
                    label       = BottomNavItem.Home.label,
                    icon        = if (currentRoute == BottomNavItem.Home.route) BottomNavItem.Home.selectedIcon else BottomNavItem.Home.unselectedIcon,
                    isSelected  = currentRoute == BottomNavItem.Home.route,
                    onClick     = { goTo(BottomNavItem.Home.route) }
                )
                NavTabItem(
                    label      = BottomNavItem.Search.label,
                    icon       = if (currentRoute == BottomNavItem.Search.route) BottomNavItem.Search.selectedIcon else BottomNavItem.Search.unselectedIcon,
                    isSelected = currentRoute == BottomNavItem.Search.route,
                    onClick    = { goTo(BottomNavItem.Search.route) }
                )
                // FAB boşluğu
                Spacer(Modifier.width(56.dp))
                NavTabItem(
                    label      = BottomNavItem.Favorites.label,
                    icon       = if (currentRoute == BottomNavItem.Favorites.route) BottomNavItem.Favorites.selectedIcon else BottomNavItem.Favorites.unselectedIcon,
                    isSelected = currentRoute == BottomNavItem.Favorites.route,
                    onClick    = { goTo(BottomNavItem.Favorites.route) }
                )
                NavTabItem(
                    label      = BottomNavItem.Profile.label,
                    icon       = if (currentRoute == BottomNavItem.Profile.route) BottomNavItem.Profile.selectedIcon else BottomNavItem.Profile.unselectedIcon,
                    isSelected = currentRoute == BottomNavItem.Profile.route,
                    onClick    = { goTo(BottomNavItem.Profile.route) }
                )
            }
        }

        // ── FAB — sadece TEACHER ─────────────────────────────────────────────
        if (currentUserRole == UserRole.TEACHER) {
            FloatingActionButton(
                onClick        = onAddClick,
                containerColor = FenlabTeal,
                contentColor   = Color.White,
                elevation      = FloatingActionButtonDefaults.elevation(8.dp),
                shape          = CircleShape,
                modifier       = Modifier
                    .align(Alignment.TopCenter)
                    .size(56.dp)
                    .offset(y = (-8).dp)
            ) {
                Icon(
                    imageVector        = Icons.Default.Add,
                    contentDescription = "Deney Ekle",
                    modifier           = Modifier.size(28.dp)
                )
            }
        }
    }
}

@Composable
private fun NavTabItem(
    label: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val tint = if (isSelected) FenlabTeal else Color(0xFFAAAAAA)

    Column(
        modifier            = Modifier
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector        = icon,
            contentDescription = label,
            tint               = tint,
            modifier           = Modifier.size(24.dp)
        )
        Spacer(Modifier.height(3.dp))
        Text(
            text       = label,
            fontSize   = 10.sp,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
            color      = tint
        )
    }
}