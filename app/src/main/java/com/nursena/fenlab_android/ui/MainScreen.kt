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

private val GlassBg     = Color(0xFF1A2235).copy(alpha = 0.95f)
private val GlassBorder = Color(0xFF2E3D5C).copy(alpha = 0.5f)
private val IconInactive = Color(0xFF6B7A99)

@Composable
fun FenlabBottomBar(
    navController: NavController,
    currentUserRole: UserRole,
    onAddClick: () -> Unit
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
            .height(if (isTeacher) 88.dp else 76.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        // ── Glass pill ──────────────────────────────────────────────────────
        Box(
            modifier = Modifier
                .padding(horizontal = 20.dp)
                .padding(bottom = 10.dp)
                .fillMaxWidth()
                .height(62.dp)
                .shadow(20.dp, RoundedCornerShape(31.dp))
                .clip(RoundedCornerShape(31.dp))
                .background(GlassBg)
        ) {
            // Üst kenarlık — glass parlaması
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(
                        Brush.horizontalGradient(
                            listOf(Color.Transparent, GlassBorder, GlassBorder, Color.Transparent)
                        )
                    )
            )

            Row(
                modifier              = Modifier.fillMaxSize().padding(horizontal = 12.dp),
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                GlassNavItem(icon = if (currentRoute == "home") Icons.Filled.Home else Icons.Outlined.Home,
                    isSelected = currentRoute == "home", onClick = { goTo("home") })

                GlassNavItem(icon = if (currentRoute == "search") Icons.Filled.Search else Icons.Outlined.Search,
                    isSelected = currentRoute == "search", onClick = { goTo("search") })

                if (isTeacher) Spacer(Modifier.width(52.dp))

                GlassNavItem(icon = if (currentRoute == "favorites") Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                    isSelected = currentRoute == "favorites", onClick = { goTo("favorites") })

                GlassNavItem(icon = if (currentRoute == "profile") Icons.Filled.Person else Icons.Outlined.Person,
                    isSelected = currentRoute == "profile", onClick = { goTo("profile") })
            }
        }

        // ── FAB — teacher only, tam ortada ──────────────────────────────────
        if (isTeacher) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .size(54.dp)
                    .shadow(16.dp, CircleShape)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(listOf(Teal400, Color(0xFF00A896)))
                    )
                    .clickable(onClick = onAddClick),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector        = Icons.Default.Add,
                    contentDescription = "Deney Ekle",
                    tint               = DarkBg,
                    modifier           = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Composable
private fun GlassNavItem(
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier         = Modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(if (isSelected) Teal400.copy(alpha = 0.15f) else Color.Transparent)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector        = icon,
            contentDescription = null,
            tint               = if (isSelected) Teal400 else IconInactive,
            modifier           = Modifier.size(22.dp)
        )
    }
}