package com.nursena.fenlab_android.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.nursena.fenlab_android.core.base.UiEvent
import com.nursena.fenlab_android.domain.model.Experiment
import com.nursena.fenlab_android.domain.model.User
import com.nursena.fenlab_android.ui.components.ErrorMessage
import com.nursena.fenlab_android.ui.components.LoadingIndicator
import com.nursena.fenlab_android.ui.components.SubjectChip
import com.nursena.fenlab_android.ui.components.formatCount
import com.nursena.fenlab_android.ui.theme.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.text.style.TextAlign

@Composable
fun ProfileScreen(
    onExperimentClick: (Long) -> Unit,
    onLogout: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showNotifications by remember { mutableStateOf(false) }
    var showSettings      by remember { mutableStateOf(false) }
    var selectedTab       by remember { mutableIntStateOf(0) }

    LaunchedEffect(viewModel) {
        viewModel.eventFlow.collect { if (it is UiEvent.LoggedOut) onLogout() }
    }

    when {
        uiState.isLoading && uiState.user == null -> LoadingIndicator()
        uiState.error != null && uiState.user == null -> ErrorMessage(
            message = uiState.error!!, onRetry = viewModel::loadProfile
        )
        else -> LazyColumn(
            modifier       = Modifier.fillMaxSize().background(DarkBg),
            contentPadding = PaddingValues(bottom = 100.dp)
        ) {
            item {
                ProfileHeader(
                    user                = uiState.user,
                    isOwnProfile        = uiState.isOwnProfile,
                    onNotificationClick = { showNotifications = true },
                    onSettingsClick     = { showSettings = true }
                )
            }

            item { StatsRow(user = uiState.user, experimentCount = uiState.experiments.size) }

            item {
                val tabs = listOf("Deneyleri", "Hakkında")
                ProfileTabBar(selected = selectedTab, tabs = tabs, onSelect = { selectedTab = it })
            }

            if (uiState.isEditing) {
                item { EditForm(state = uiState, viewModel = viewModel) }
            }

            // Tab içerik
            when {
                selectedTab == 0 -> {
                    if (uiState.experiments.isEmpty()) {
                        item { EmptyTab(emoji = "🔬", text = "Henüz deney eklenmemiş") }
                    } else {
                        items(uiState.experiments, key = { it.id }) { exp ->
                            ProfileExperimentCard(
                                experiment  = exp,
                                onCardClick = { onExperimentClick(exp.id) }
                            )
                        }
                    }
                }
                else -> {
                    item { AboutTab(user = uiState.user, isOwnProfile = uiState.isOwnProfile, onEditClick = viewModel::toggleEdit) }
                }
            }
        }
    }

    if (showNotifications) NotificationsSheet(onDismiss = { showNotifications = false })
    if (showSettings) SettingsSheet(
        onDismiss = { showSettings = false },
        onLogout  = { showSettings = false; viewModel.logout() }
    )
}

// ─────────────────────────────────────────────────────────────────────────────
// Header
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun ProfileHeader(
    user: User?,
    isOwnProfile: Boolean,
    onNotificationClick: () -> Unit,
    onSettingsClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Brush.verticalGradient(listOf(Color(0xFF0D2D28), DarkBg)))
            .statusBarsPadding()                          // ← status bar overlap fix
            .padding(horizontal = 20.dp)
            .padding(top = 12.dp, bottom = 20.dp)
    ) {
        // Sağ üst ikonlar
        if (isOwnProfile) {
            Row(
                modifier              = Modifier.align(Alignment.TopEnd),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                GlassIconButton(icon = Icons.Outlined.Notifications, onClick = onNotificationClick, badge = true)
                GlassIconButton(icon = Icons.Outlined.Settings, onClick = onSettingsClick)
            }
        }

        Row(
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            modifier              = Modifier.fillMaxWidth().padding(top = 4.dp)
        ) {
            // Avatar
            Box(
                modifier = Modifier
                    .size(62.dp)
                    .clip(CircleShape)
                    .background(Brush.linearGradient(listOf(Teal400.copy(0.5f), Teal500.copy(0.4f)))),
                contentAlignment = Alignment.Center
            ) {
                if (user?.profileImageUrl != null) {
                    AsyncImage(
                        model = user.profileImageUrl, contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize().clip(CircleShape)
                    )
                } else {
                    Text(
                        user?.displayName?.take(2)?.uppercase() ?: "?",
                        color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold
                    )
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(user?.displayName ?: "", color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Text("@${user?.username ?: ""}", color = TextSecondary, fontSize = 12.sp)
                Spacer(Modifier.height(6.dp))
                Box(
                    modifier = Modifier
                        .background(Teal400.copy(alpha = 0.15f), RoundedCornerShape(20.dp))
                        .padding(horizontal = 9.dp, vertical = 3.dp)
                ) {
                    Text(
                        "${if (user?.isTeacher == true) "👨‍🏫" else "🎓"} ${user?.displayRole ?: ""}",
                        color = Teal400, fontSize = 11.sp, fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
private fun GlassIconButton(icon: ImageVector, onClick: () -> Unit, badge: Boolean = false) {
    Box {
        Box(
            modifier = Modifier.size(36.dp).clip(CircleShape)
                .background(DarkSurface2).clickable(onClick = onClick),
            contentAlignment = Alignment.Center
        ) { Icon(icon, null, tint = TextSecondary, modifier = Modifier.size(18.dp)) }
        if (badge) {
            Box(Modifier.size(8.dp).align(Alignment.TopEnd).offset(1.dp, (-1).dp)
                .background(Orange400, CircleShape))
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// İstatistikler — Görüntülenme yerine Yorum
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun StatsRow(user: User?, experimentCount: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(DarkSurface),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        StatItem(value = experimentCount.toString(), label = "Deney")
        Box(Modifier.width(1.dp).height(36.dp).align(Alignment.CenterVertically).background(DarkSurface3))
        // Görüntülenme yok — branş/deneyim yılı gibi anlamlı bir şey göster
        if (user?.isTeacher == true && user.experienceYears != null) {
            StatItem(value = "${user.experienceYears}", label = "Yıl Deneyim")
        } else {
            StatItem(value = "0", label = "Yorum")
        }
        Box(Modifier.width(1.dp).height(36.dp).align(Alignment.CenterVertically).background(DarkSurface3))
        StatItem(value = "0", label = "Beğeni")
    }
}

@Composable
private fun RowScope.StatItem(value: String, label: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier            = Modifier.padding(vertical = 12.dp)
    ) {
        Text(value, color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(2.dp))
        Text(label, color = TextSecondary, fontSize = 11.sp)
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Tab bar
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun ProfileTabBar(selected: Int, tabs: List<String>, onSelect: (Int) -> Unit) {
    Row(Modifier.fillMaxWidth().background(DarkBg).padding(top = 6.dp)) {
        tabs.forEachIndexed { i, label ->
            Column(
                modifier            = Modifier.weight(1f).clickable { onSelect(i) },
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    label,
                    color      = if (selected == i) Teal400 else TextSecondary,
                    fontSize   = 13.sp,
                    fontWeight = if (selected == i) FontWeight.SemiBold else FontWeight.Normal,
                    modifier   = Modifier.padding(vertical = 11.dp)
                )
                Box(
                    Modifier.fillMaxWidth().height(2.dp)
                        .background(if (selected == i) Teal400 else Color.Transparent)
                )
            }
        }
    }
    HorizontalDivider(thickness = 0.5.dp, color = DarkSurface3)
}

// ─────────────────────────────────────────────────────────────────────────────
// Profildeki küçük deney kartı (ExperimentCard değil — kompakt)
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun ProfileExperimentCard(experiment: Experiment, onCardClick: () -> Unit) {
    Card(
        modifier  = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 5.dp)
            .clickable(onClick = onCardClick),
        shape     = RoundedCornerShape(12.dp),
        colors    = CardDefaults.cardColors(containerColor = DarkSurface),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(
            modifier              = Modifier.fillMaxWidth().padding(10.dp),
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Küçük thumbnail
            Box(
                modifier = Modifier.size(width = 80.dp, height = 64.dp)
                    .clip(RoundedCornerShape(8.dp)).background(DarkSurface2)
            ) {
                AsyncImage(
                    model = experiment.thumbnailUrl ?: experiment.videoUrl,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    experiment.title, color = TextPrimary, fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold, maxLines = 2,
                    overflow = TextOverflow.Ellipsis, lineHeight = 18.sp
                )
                Spacer(Modifier.height(5.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                    experiment.subject?.let { SubjectChip(subject = it) }
                }
            }

            // Sağ: favori sayısı
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Outlined.FavoriteBorder, null, tint = TextSecondary, modifier = Modifier.size(14.dp))
                Text(formatCount(experiment.favoriteCount), color = TextSecondary, fontSize = 10.sp)
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Hakkımda tab — kendi profili için
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun AboutTab(user: User?, isOwnProfile: Boolean, onEditClick: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Bio
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurface)
        ) {
            Column(Modifier.fillMaxWidth().padding(14.dp)) {
                Text("Biyografi", color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                Spacer(Modifier.height(6.dp))
                Text(
                    user?.bio?.ifBlank { null } ?: "Henüz biyografi eklenmemiş.",
                    color = if (user?.bio.isNullOrBlank()) TextSecondary else TextPrimary,
                    fontSize = 13.sp, lineHeight = 19.sp
                )
            }
        }

        // Branş (öğretmen için)
        if (user?.isTeacher == true) {
            Card(shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface)) {
                Row(
                    Modifier.fillMaxWidth().padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Branş", color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                        Spacer(Modifier.height(4.dp))
                        Text(user.branch?.ifBlank { null } ?: "Belirtilmemiş",
                            color = TextPrimary, fontSize = 13.sp)
                    }
                    Text("🏫", fontSize = 22.sp)
                }
            }
        }

        // Düzenle butonu — sadece kendi profili
        if (isOwnProfile) OutlinedButton(
            onClick = onEditClick,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp),
            border = BorderStroke(1.dp, Teal400.copy(alpha = 0.5f)),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = Teal400)
        ) {
            Icon(Icons.Default.Edit, null, modifier = Modifier.size(15.dp))
            Spacer(Modifier.width(6.dp))
            Text("Profili Düzenle", fontSize = 13.sp)
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Edit form
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun EditForm(state: ProfileUiState, viewModel: ProfileViewModel) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface)
    ) {
        Column(Modifier.padding(14.dp)) {
            Text("Profili Düzenle", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = TextPrimary)
            Spacer(Modifier.height(10.dp))
            ProfileField(state.editFullName, viewModel::onFullNameChange, "Ad Soyad")
            Spacer(Modifier.height(8.dp))
            ProfileField(state.editBio, viewModel::onBioChange, "Biyografi", maxLines = 3)
            if (state.user?.isTeacher == true) {
                Spacer(Modifier.height(8.dp))
                ProfileField(state.editBranch, viewModel::onBranchChange, "Branş")
            }
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = viewModel::toggleEdit, modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, DarkSurface3),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = TextSecondary)
                ) { Text("İptal", fontSize = 13.sp) }
                Button(
                    onClick = viewModel::saveProfile, enabled = !state.isSaving,
                    modifier = Modifier.weight(1f), shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Teal400)
                ) {
                    if (state.isSaving)
                        CircularProgressIndicator(Modifier.size(15.dp), strokeWidth = 2.dp, color = DarkBg)
                    else Text("Kaydet", color = DarkBg, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                }
            }
        }
    }
}

@Composable
private fun ProfileField(value: String, onChange: (String) -> Unit, label: String, maxLines: Int = 1) {
    OutlinedTextField(
        value = value, onValueChange = onChange,
        label = { Text(label, fontSize = 11.sp) },
        modifier = Modifier.fillMaxWidth().then(if (maxLines > 1) Modifier.height(80.dp) else Modifier),
        shape = RoundedCornerShape(8.dp), maxLines = maxLines,
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary,
            focusedBorderColor = Teal400, unfocusedBorderColor = DarkSurface3,
            focusedContainerColor = DarkSurface2, unfocusedContainerColor = DarkSurface2,
            focusedLabelColor = Teal400, unfocusedLabelColor = TextSecondary,
            cursorColor = Teal400
        )
    )
}

// ─────────────────────────────────────────────────────────────────────────────
// Boş tab
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun EmptyTab(emoji: String, text: String) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(vertical = 52.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(emoji, fontSize = 40.sp)
        Spacer(Modifier.height(12.dp))
        Text(text, color = TextSecondary, fontSize = 14.sp, fontWeight = FontWeight.Medium)
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Bildirimler Sheet
// ─────────────────────────────────────────────────────────────────────────────
private data class NotifItem(val icon: String, val text: String, val time: String, val isNew: Boolean = false)
private val mockNotifs = listOf(
    NotifItem("❓", "Ahmet Yılmaz deneyi hakkında soru sordu", "5 dk önce", true),
    NotifItem("❤️", "\"Volkan Patlaması\" deneyin beğenildi", "1 saat önce", true),
    NotifItem("💬", "Yeni bir yorum aldın", "3 saat önce")
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NotificationsSheet(onDismiss: () -> Unit) {
    ModalBottomSheet(onDismissRequest = onDismiss, containerColor = DarkSurface, dragHandle = { SheetHandle() }) {
        Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp).padding(bottom = 32.dp)) {
            SheetHeader("Bildirimler", onDismiss)
            Spacer(Modifier.height(4.dp))
            mockNotifs.forEach { notif ->
                Row(
                    Modifier.fillMaxWidth().padding(vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(Modifier.size(38.dp).clip(CircleShape).background(DarkSurface2),
                        contentAlignment = Alignment.Center) { Text(notif.icon, fontSize = 16.sp) }
                    Column(Modifier.weight(1f)) {
                        Text(notif.text, fontSize = 13.sp, color = TextPrimary, lineHeight = 17.sp)
                        Text(notif.time, fontSize = 11.sp, color = TextSecondary)
                    }
                    if (notif.isNew) Box(Modifier.size(7.dp).background(Teal400, CircleShape))
                }
                if (notif != mockNotifs.last()) HorizontalDivider(thickness = 0.5.dp, color = DarkSurface3)
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Ayarlar Sheet
// ─────────────────────────────────────────────────────────────────────────────
private data class SettingItem(val icon: String, val label: String)
private val settingItems = listOf(
    SettingItem("👤", "Hesap Bilgileri"),
    SettingItem("🔒", "Gizlilik"),
    SettingItem("🔔", "Bildirimler"),
    SettingItem("ℹ️", "Hakkında")
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettingsSheet(onDismiss: () -> Unit, onLogout: () -> Unit) {
    ModalBottomSheet(onDismissRequest = onDismiss, containerColor = DarkSurface, dragHandle = { SheetHandle() }) {
        Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp).padding(bottom = 36.dp)) {
            SheetHeader("Ayarlar", onDismiss)
            Spacer(Modifier.height(4.dp))
            settingItems.forEach { item ->
                Row(
                    Modifier.fillMaxWidth().clickable {}.padding(vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(item.icon, fontSize = 18.sp)
                    Text(item.label, fontSize = 14.sp, color = TextPrimary, modifier = Modifier.weight(1f))
                    Icon(Icons.Default.ChevronRight, null, tint = TextSecondary, modifier = Modifier.size(16.dp))
                }
                HorizontalDivider(thickness = 0.5.dp, color = DarkSurface3)
            }
            Spacer(Modifier.height(6.dp))
            Row(
                Modifier.fillMaxWidth().clickable(onClick = onLogout).padding(vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(Icons.Default.Logout, null, tint = Red400, modifier = Modifier.size(20.dp))
                Text("Çıkış Yap", fontSize = 14.sp, color = Red400, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
private fun SheetHandle() {
    Box(Modifier.fillMaxWidth().padding(top = 12.dp, bottom = 4.dp), Alignment.Center) {
        Box(Modifier.size(width = 36.dp, height = 4.dp).background(DarkSurface3, RoundedCornerShape(2.dp)))
    }
}

@Composable
private fun SheetHeader(title: String, onDismiss: () -> Unit) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Text(title, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
        IconButton(onClick = onDismiss) { Icon(Icons.Default.Close, null, tint = TextSecondary) }
    }
}