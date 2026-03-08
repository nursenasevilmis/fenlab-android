package com.nursena.fenlab_android.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import com.nursena.fenlab_android.domain.model.Notification
import com.nursena.fenlab_android.domain.model.User
import com.nursena.fenlab_android.ui.components.ErrorMessage
import com.nursena.fenlab_android.ui.components.LoadingIndicator
import com.nursena.fenlab_android.ui.components.SubjectChip
import com.nursena.fenlab_android.ui.components.formatCount
import com.nursena.fenlab_android.ui.theme.*
import androidx.compose.foundation.BorderStroke

@Composable
fun ProfileScreen(
    onExperimentClick: (Long) -> Unit,
    onLogout: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val uiState      by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbar     = remember { SnackbarHostState() }
    var showNotif    by remember { mutableStateOf(false) }
    var showSettings by remember { mutableStateOf(false) }
    var selectedTab  by remember { mutableIntStateOf(0) }

    LaunchedEffect(viewModel) {
        viewModel.eventFlow.collect { event ->
            when (event) {
                is UiEvent.LoggedOut    -> onLogout()
                is UiEvent.ShowSnackbar -> snackbar.showSnackbar(event.message)
                else -> Unit
            }
        }
    }

    Scaffold(snackbarHost = { SnackbarHost(snackbar) }, containerColor = DarkBg) { pad ->
        when {
            uiState.isLoading && uiState.user == null -> LoadingIndicator()
            uiState.error != null && uiState.user == null -> ErrorMessage(message = uiState.error!!, onRetry = viewModel::loadProfile)
            else -> LazyColumn(
                modifier       = Modifier.fillMaxSize().padding(pad),
                contentPadding = PaddingValues(bottom = 90.dp)
            ) {
                item {
                    ProfileHeader(
                        user                = uiState.user,
                        isOwnProfile        = uiState.isOwnProfile,
                        unreadCount         = uiState.unreadCount,
                        onNotificationClick = { showNotif = true },
                        onSettingsClick     = { showSettings = true }
                    )
                }
                item { StatsRow(user = uiState.user, experimentCount = uiState.experiments.size) }
                item {
                    ProfileTabBar(selected = selectedTab, tabs = listOf("Deneyleri", "Hakkında"), onSelect = { selectedTab = it })
                }
                if (uiState.isEditing) item { EditForm(state = uiState, viewModel = viewModel) }
                when (selectedTab) {
                    0 -> {
                        if (uiState.experiments.isEmpty()) {
                            item { EmptyTab("🔬", "Henüz deney eklenmemiş") }
                        } else {
                            items(uiState.experiments, key = { it.id }) { exp ->
                                ProfileExperimentCard(exp) { onExperimentClick(exp.id) }
                            }
                        }
                    }
                    else -> item {
                        AboutTab(user = uiState.user, isOwnProfile = uiState.isOwnProfile, onEditClick = viewModel::toggleEdit)
                    }
                }
            }
        }
    }

    if (showNotif) {
        NotificationsSheet(
            notifications = uiState.notifications,
            isLoading     = uiState.isNotifLoading,
            onMarkRead    = viewModel::markNotificationRead,
            onMarkAllRead = viewModel::markAllRead,
            onDismiss     = { showNotif = false }
        )
    }
    if (showSettings) {
        SettingsSheet(
            user          = uiState.user,
            onEditProfile = { showSettings = false; viewModel.toggleEdit() },
            onDismiss     = { showSettings = false },
            onLogout      = { showSettings = false; viewModel.logout() }
        )
    }
}

@Composable
private fun ProfileHeader(
    user: User?, isOwnProfile: Boolean, unreadCount: Long,
    onNotificationClick: () -> Unit, onSettingsClick: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxWidth()
            .background(Brush.verticalGradient(listOf(Color(0xFF0D2D28), DarkBg)))
            .statusBarsPadding()
            .padding(horizontal = 14.dp).padding(top = 8.dp, bottom = 14.dp)
    ) {
        if (isOwnProfile) {
            Row(modifier = Modifier.align(Alignment.TopEnd), horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                Box {
                    Box(
                        modifier = Modifier.size(30.dp).clip(CircleShape).background(DarkSurface2).clickable(onClick = onNotificationClick),
                        contentAlignment = Alignment.Center
                    ) { Icon(Icons.Outlined.Notifications, null, tint = TextSecondary, modifier = Modifier.size(15.dp)) }
                    if (unreadCount > 0) {
                        Box(Modifier.align(Alignment.TopEnd).size(6.dp).background(Orange400, CircleShape))
                    }
                }
                Box(
                    modifier = Modifier.size(30.dp).clip(CircleShape).background(DarkSurface2).clickable(onClick = onSettingsClick),
                    contentAlignment = Alignment.Center
                ) { Icon(Icons.Outlined.Settings, null, tint = TextSecondary, modifier = Modifier.size(15.dp)) }
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(11.dp),
            modifier = Modifier.fillMaxWidth().padding(top = 2.dp)
        ) {
            Box(
                modifier = Modifier.size(46.dp).clip(CircleShape)
                    .background(Brush.linearGradient(listOf(Teal400.copy(0.5f), Teal500.copy(0.4f)))),
                contentAlignment = Alignment.Center
            ) {
                if (user?.profileImageUrl != null) {
                    AsyncImage(model = user.profileImageUrl, contentDescription = null,
                        contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize().clip(CircleShape))
                } else {
                    Text(user?.displayName?.take(2)?.uppercase() ?: "?", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(user?.displayName ?: "", color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                Text("@${user?.username ?: ""}", color = TextSecondary, fontSize = 10.sp)
                Spacer(Modifier.height(3.dp))
                Box(
                    modifier = Modifier.background(Teal400.copy(alpha = 0.12f), RoundedCornerShape(16.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        "${if (user?.isTeacher == true) "👨‍🏫" else "🎓"} ${user?.displayRole ?: ""}",
                        color = Teal400, fontSize = 9.sp, fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
private fun StatsRow(user: User?, experimentCount: Int) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 3.dp)
            .clip(RoundedCornerShape(10.dp)).background(DarkSurface),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        StatItem(experimentCount.toString(), "Deney")
        Box(Modifier.width(1.dp).height(22.dp).align(Alignment.CenterVertically).background(DarkSurface3))
        if (user?.isTeacher == true && user.experienceYears != null)
            StatItem("${user.experienceYears}", "Yıl")
        else
            StatItem("0", "Yorum")
        Box(Modifier.width(1.dp).height(22.dp).align(Alignment.CenterVertically).background(DarkSurface3))
        StatItem("0", "Beğeni")
    }
}

@Composable
private fun RowScope.StatItem(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(vertical = 9.dp)) {
        Text(value, color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
        Text(label, color = TextSecondary, fontSize = 9.sp)
    }
}

@Composable
private fun ProfileTabBar(selected: Int, tabs: List<String>, onSelect: (Int) -> Unit) {
    Row(Modifier.fillMaxWidth().background(DarkBg).padding(top = 3.dp)) {
        tabs.forEachIndexed { i, label ->
            Column(modifier = Modifier.weight(1f).clickable { onSelect(i) }, horizontalAlignment = Alignment.CenterHorizontally) {
                Text(label, color = if (selected == i) Teal400 else TextSecondary, fontSize = 11.sp,
                    fontWeight = if (selected == i) FontWeight.SemiBold else FontWeight.Normal,
                    modifier = Modifier.padding(vertical = 8.dp))
                Box(Modifier.fillMaxWidth().height(2.dp).background(if (selected == i) Teal400 else Color.Transparent))
            }
        }
    }
    HorizontalDivider(thickness = 0.5.dp, color = DarkSurface3)
}

@Composable
private fun ProfileExperimentCard(experiment: Experiment, onCardClick: () -> Unit) {
    Card(
        modifier  = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 3.dp).clickable(onClick = onCardClick),
        shape     = RoundedCornerShape(9.dp),
        colors    = CardDefaults.cardColors(containerColor = DarkSurface),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth().padding(8.dp), verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(9.dp)) {
            Box(modifier = Modifier.size(width = 60.dp, height = 48.dp).clip(RoundedCornerShape(7.dp)).background(DarkSurface2)) {
                AsyncImage(model = experiment.thumbnailUrl ?: experiment.videoUrl, contentDescription = null,
                    contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(experiment.title, color = TextPrimary, fontSize = 11.sp, fontWeight = FontWeight.SemiBold,
                    maxLines = 2, overflow = TextOverflow.Ellipsis, lineHeight = 15.sp)
                Spacer(Modifier.height(3.dp))
                experiment.subject?.let { SubjectChip(subject = it) }
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Outlined.FavoriteBorder, null, tint = TextSecondary, modifier = Modifier.size(11.dp))
                Text(formatCount(experiment.favoriteCount), color = TextSecondary, fontSize = 9.sp)
            }
        }
    }
}

@Composable
private fun AboutTab(user: User?, isOwnProfile: Boolean, onEditClick: () -> Unit) {
    Column(modifier = Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
        Card(shape = RoundedCornerShape(10.dp), colors = CardDefaults.cardColors(containerColor = DarkSurface)) {
            Column(Modifier.fillMaxWidth().padding(11.dp)) {
                Text("Biyografi", color = TextSecondary, fontSize = 9.sp, fontWeight = FontWeight.Medium)
                Spacer(Modifier.height(3.dp))
                Text(user?.bio?.ifBlank { null } ?: "Henüz biyografi eklenmemiş.",
                    color = if (user?.bio.isNullOrBlank()) TextSecondary else TextPrimary,
                    fontSize = 12.sp, lineHeight = 16.sp)
            }
        }
        if (user?.isTeacher == true) {
            Card(shape = RoundedCornerShape(10.dp), colors = CardDefaults.cardColors(containerColor = DarkSurface)) {
                Row(Modifier.fillMaxWidth().padding(11.dp), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                    Column {
                        Text("Branş", color = TextSecondary, fontSize = 9.sp)
                        Text(user.branch?.ifBlank { null } ?: "Belirtilmemiş", color = TextPrimary, fontSize = 12.sp)
                    }
                    Text("🏫", fontSize = 16.sp)
                }
            }
        }
        if (isOwnProfile) OutlinedButton(
            onClick = onEditClick, modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(9.dp),
            border = BorderStroke(1.dp, Teal400.copy(alpha = 0.4f)),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = Teal400)
        ) {
            Icon(Icons.Default.Edit, null, modifier = Modifier.size(12.dp))
            Spacer(Modifier.width(5.dp))
            Text("Profili Düzenle", fontSize = 12.sp)
        }
    }
}

@Composable
private fun EditForm(state: ProfileUiState, viewModel: ProfileViewModel) {
    Card(modifier = Modifier.fillMaxWidth().padding(14.dp), shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface)) {
        Column(Modifier.padding(11.dp)) {
            Text("Profili Düzenle", fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = TextPrimary)
            Spacer(Modifier.height(7.dp))
            ProfileField(state.editFullName, viewModel::onFullNameChange, "Ad Soyad")
            Spacer(Modifier.height(5.dp))
            ProfileField(state.editBio, viewModel::onBioChange, "Biyografi", maxLines = 3)
            if (state.user?.isTeacher == true) {
                Spacer(Modifier.height(5.dp))
                ProfileField(state.editBranch, viewModel::onBranchChange, "Branş")
                Spacer(Modifier.height(5.dp))
                ProfileField(state.editExperienceYears, viewModel::onExperienceYearsChange, "Deneyim Yılı")
            }
            Spacer(Modifier.height(9.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                OutlinedButton(onClick = viewModel::toggleEdit, modifier = Modifier.weight(1f).height(36.dp),
                    shape = RoundedCornerShape(8.dp), border = BorderStroke(1.dp, DarkSurface3),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = TextSecondary)
                ) { Text("İptal", fontSize = 11.sp) }
                Button(onClick = viewModel::saveProfile, enabled = !state.isSaving,
                    modifier = Modifier.weight(1f).height(36.dp), shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Teal400)
                ) {
                    if (state.isSaving) CircularProgressIndicator(Modifier.size(13.dp), strokeWidth = 2.dp, color = DarkBg)
                    else Text("Kaydet", color = DarkBg, fontWeight = FontWeight.SemiBold, fontSize = 11.sp)
                }
            }
        }
    }
}

@Composable
private fun ProfileField(value: String, onChange: (String) -> Unit, label: String, maxLines: Int = 1) {
    OutlinedTextField(
        value = value, onValueChange = onChange,
        label = { Text(label, fontSize = 9.sp) },
        modifier = Modifier.fillMaxWidth().then(if (maxLines > 1) Modifier.height(68.dp) else Modifier),
        shape = RoundedCornerShape(7.dp), maxLines = maxLines,
        textStyle = androidx.compose.ui.text.TextStyle(fontSize = 12.sp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary,
            focusedBorderColor = Teal400, unfocusedBorderColor = DarkSurface3,
            focusedContainerColor = DarkSurface2, unfocusedContainerColor = DarkSurface2,
            focusedLabelColor = Teal400, unfocusedLabelColor = TextSecondary, cursorColor = Teal400
        )
    )
}

@Composable
private fun EmptyTab(emoji: String, text: String) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 36.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text(emoji, fontSize = 28.sp)
        Spacer(Modifier.height(7.dp))
        Text(text, color = TextSecondary, fontSize = 12.sp)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NotificationsSheet(
    notifications: List<Notification>, isLoading: Boolean,
    onMarkRead: (Long) -> Unit, onMarkAllRead: () -> Unit, onDismiss: () -> Unit
) {
    ModalBottomSheet(onDismissRequest = onDismiss, containerColor = DarkSurface, dragHandle = { SheetHandle() }) {
        Column(Modifier.fillMaxWidth().padding(horizontal = 14.dp).padding(bottom = 24.dp)) {
            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                Text("Bildirimler", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (notifications.any { !it.isRead }) {
                        TextButton(onClick = onMarkAllRead) { Text("Tümünü Oku", color = Teal400, fontSize = 10.sp) }
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(30.dp)) {
                        Icon(Icons.Default.Close, null, tint = TextSecondary, modifier = Modifier.size(14.dp))
                    }
                }
            }
            if (isLoading) {
                Box(Modifier.fillMaxWidth().padding(20.dp), Alignment.Center) {
                    CircularProgressIndicator(color = Teal400, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                }
            } else if (notifications.isEmpty()) {
                Column(Modifier.fillMaxWidth().padding(vertical = 28.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("🔔", fontSize = 24.sp)
                    Spacer(Modifier.height(5.dp))
                    Text("Bildirim yok", color = TextSecondary, fontSize = 11.sp)
                }
            } else {
                notifications.forEach { notif ->
                    Row(
                        Modifier.fillMaxWidth().clickable { if (!notif.isRead) onMarkRead(notif.id) }
                            .background(if (!notif.isRead) Teal400.copy(0.04f) else Color.Transparent)
                            .padding(vertical = 9.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(9.dp)
                    ) {
                        Box(Modifier.size(30.dp).clip(CircleShape).background(DarkSurface2), contentAlignment = Alignment.Center) {
                            Text(notif.icon, fontSize = 12.sp)
                        }
                        Column(Modifier.weight(1f)) {
                            Text(notif.message, fontSize = 11.sp, color = TextPrimary, lineHeight = 15.sp)
                            Text(notif.createdAt.take(10), fontSize = 9.sp, color = TextSecondary)
                        }
                        if (!notif.isRead) Box(Modifier.size(5.dp).background(Teal400, CircleShape))
                    }
                    if (notif != notifications.last()) HorizontalDivider(thickness = 0.5.dp, color = DarkSurface3)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettingsSheet(user: User?, onEditProfile: () -> Unit, onDismiss: () -> Unit, onLogout: () -> Unit) {
    ModalBottomSheet(onDismissRequest = onDismiss, containerColor = DarkSurface, dragHandle = { SheetHandle() }) {
        Column(Modifier.fillMaxWidth().padding(horizontal = 14.dp).padding(bottom = 28.dp)) {
            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                Text("Ayarlar", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                IconButton(onClick = onDismiss, modifier = Modifier.size(30.dp)) {
                    Icon(Icons.Default.Close, null, tint = TextSecondary, modifier = Modifier.size(14.dp))
                }
            }
            Spacer(Modifier.height(3.dp))
            SettingsRow(Icons.Default.Person, "Hesap Bilgileri", "${user?.fullName ?: ""} · ${user?.email ?: ""}", onEditProfile)
            HorizontalDivider(thickness = 0.5.dp, color = DarkSurface3)
            SettingsRow(Icons.Default.AlternateEmail, "Kullanıcı Adı", "@${user?.username ?: ""}", {})
            HorizontalDivider(thickness = 0.5.dp, color = DarkSurface3)
            if (user?.isTeacher == true) {
                SettingsRow(Icons.Default.School, "Branş", user.branch ?: "Belirtilmemiş", onEditProfile)
                HorizontalDivider(thickness = 0.5.dp, color = DarkSurface3)
            }
            SettingsRow(Icons.Default.CalendarToday, "Üyelik Tarihi", user?.createdAt?.take(10) ?: "", {})
            HorizontalDivider(thickness = 0.5.dp, color = DarkSurface3)
            Spacer(Modifier.height(3.dp))
            Row(
                Modifier.fillMaxWidth().clickable(onClick = onLogout).padding(vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(9.dp)
            ) {
                Box(Modifier.size(30.dp).clip(CircleShape).background(Red400.copy(0.1f)), contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Logout, null, tint = Red400, modifier = Modifier.size(14.dp))
                }
                Text("Çıkış Yap", fontSize = 12.sp, color = Red400, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
private fun SettingsRow(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, sublabel: String, onClick: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().clickable(onClick = onClick).padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(9.dp)
    ) {
        Box(Modifier.size(30.dp).clip(CircleShape).background(DarkSurface2), contentAlignment = Alignment.Center) {
            Icon(icon, null, tint = Teal400, modifier = Modifier.size(13.dp))
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(label, fontSize = 12.sp, color = TextPrimary)
            if (sublabel.isNotBlank()) Text(sublabel, fontSize = 9.sp, color = TextSecondary)
        }
        Icon(Icons.Default.ChevronRight, null, tint = DarkSurface3, modifier = Modifier.size(13.dp))
    }
}

@Composable
private fun SheetHandle() {
    Box(Modifier.fillMaxWidth().padding(top = 8.dp, bottom = 2.dp), Alignment.Center) {
        Box(Modifier.size(width = 30.dp, height = 3.dp).background(DarkSurface3, RoundedCornerShape(2.dp)))
    }
}