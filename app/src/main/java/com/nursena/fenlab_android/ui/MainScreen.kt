package com.nursena.fenlab_android.ui

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

// ── NavItem tanımı ────────────────────────────────────────────────────────────
sealed class BottomNavItem(
    val route: String,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    object Home      : BottomNavItem("home",      "Anasayfa", Icons.Filled.Home,        Icons.Outlined.Home)
    object Search    : BottomNavItem("search",    "Ara",      Icons.Filled.Search,      Icons.Outlined.Search)
    object Favorites : BottomNavItem("favorites", "Favoriler",Icons.Filled.Favorite,    Icons.Outlined.FavoriteBorder)
    object Profile   : BottomNavItem("profile",   "Profil",   Icons.Filled.Person,      Icons.Outlined.Person)
}

private val navItems = listOf(
    BottomNavItem.Home, BottomNavItem.Search,
    BottomNavItem.Favorites, BottomNavItem.Profile
)

// ── FenlabBottomBar ───────────────────────────────────────────────────────────
@Composable
fun FenlabBottomBar(
    navController: NavController,
    currentUserRole: UserRole,
    onAddClick: () -> Unit
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Box(modifier = Modifier.fillMaxWidth().height(72.dp)) {

        // Arka plan bar
        Surface(
            modifier        = Modifier.fillMaxWidth().height(72.dp).align(Alignment.BottomCenter),
            color           = Color.White,
            shadowElevation = 12.dp,
            shape           = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
        ) {
            Row(
                modifier              = Modifier.fillMaxSize().padding(horizontal = 8.dp),
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                // Sol 2: Anasayfa + Ara
                navItems.take(2).forEach { item ->
                    NavItemView(item = item, isSelected = currentRoute == item.route) {
                        navigateTo(navController, item.route)
                    }
                }
                // FAB için boşluk
                Spacer(Modifier.width(56.dp))
                // Sağ 2: Favoriler + Profil
                navItems.drop(2).forEach { item ->
                    NavItemView(item = item, isSelected = currentRoute == item.route) {
                        navigateTo(navController, item.route)
                    }
                }
            }
        }

        // FAB — sadece TEACHER rolü için
        if (currentUserRole == UserRole.TEACHER) {
            FloatingActionButton(
                onClick          = onAddClick,
                containerColor   = FenlabTeal,
                contentColor     = Color.White,
                elevation        = FloatingActionButtonDefaults.elevation(8.dp),
                shape            = CircleShape,
                modifier         = Modifier
                    .align(Alignment.TopCenter)
                    .size(56.dp)
                    .offset(y = (-8).dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Deney Ekle", modifier = Modifier.size(28.dp))
            }
        }
    }
}

@Composable
private fun NavItemView(item: BottomNavItem, isSelected: Boolean, onClick: () -> Unit) {
    val color = if (isSelected) FenlabTeal else Color(0xFFAAAAAA)
    NavigationBarItem(
        selected = isSelected,
        onClick  = onClick,
        icon = {
            Icon(
                imageVector        = if (isSelected) item.selectedIcon else item.unselectedIcon,
                contentDescription = item.label,
                tint               = color,
                modifier           = Modifier.size(24.dp)
            )
        },
        label = {
            Text(
                text       = item.label,
                fontSize   = 10.sp,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                color      = color
            )
        },
        colors = NavigationBarItemDefaults.colors(indicatorColor = FenlabTeal.copy(alpha = 0.1f))
    )
}

private fun navigateTo(navController: NavController, route: String) {
    navController.navigate(route) {
        popUpTo("home") { saveState = true }
        launchSingleTop = true
        restoreState    = true
    }
}