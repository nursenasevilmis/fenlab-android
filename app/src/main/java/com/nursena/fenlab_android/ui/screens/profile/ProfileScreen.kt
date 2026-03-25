package com.nursena.fenlab_android.ui.screens.profile

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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

@Composable
fun ProfileScreen(
    onExperimentClick: (Long) -> Unit,
    onLogout: () -> Unit,
    onUnreadCountChange: (Long) -> Unit = {},
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    var selectedTab  by remember { mutableIntStateOf(0) }
    var showNotif    by remember { mutableStateOf(false) }
    var showSettings by remember { mutableStateOf(false) }
    val context = LocalContext.current

    val photoLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let { viewModel.uploadProfilePhoto(context, it) }
    }

    LaunchedEffect(viewModel) {
        viewModel.eventFlow.collect { event ->
            when (event) {
                is UiEvent.LoggedOut    -> onLogout()
                is UiEvent.SessionExpired -> onLogout()
                is UiEvent.ShowSnackbar -> snackbar.showSnackbar(event.message)
                else -> Unit
            }
        }
    }
    LaunchedEffect(uiState.unreadCount) { onUnreadCountChange(uiState.unreadCount) }

    Scaffold(snackbarHost = { SnackbarHost(snackbar) }, containerColor = DarkBg) { pad ->
        when {
            uiState.isLoading && uiState.user == null -> LoadingIndicator()
            uiState.error != null && uiState.user == null ->
                ErrorMessage(message = uiState.error!!, onRetry = viewModel::loadProfile)
            else -> {
                val user       = uiState.user
                val isTeacher  = user?.isTeacher == true
                val isOwn      = uiState.isOwnProfile

                LazyColumn(
                    modifier       = Modifier.fillMaxSize().padding(pad),
                    contentPadding = PaddingValues(bottom = 100.dp)
                ) {
                    // ── Header ───────────────────────────────────────────────
                    item {
                        ProfileHeader(
                            user             = user,
                            isOwn            = isOwn,
                            unreadCount      = uiState.unreadCount,
                            isUploadingPhoto = uiState.isUploadingPhoto,
                            onPhotoClick     = { if (isOwn) photoLauncher.launch("image/*") },
                            onNotifClick     = { showNotif = true },
                            onSettingsClick  = { showSettings = true }
                        )
                    }

                    // ── Stats — rolle anlamlı ────────────────────────────────
                    item {
                        if (isTeacher) {
                            TeacherStats(
                                experimentCount = uiState.experiments.size,
                                totalFavorites  = uiState.experiments.sumOf { it.favoriteCount },
                                experienceYears = user?.experienceYears
                            )
                        } else {
                            StudentStats(user = user)
                        }
                    }

                    // Edit formu
                    if (uiState.isEditing) item { EditFormCard(state = uiState, vm = viewModel) }

                    // ── Hakkında (önce) ───────────────────────────────────────
                    item { SectionDivider(label = "Hakkında") }
                    item {
                        AboutCard(
                            user      = user,
                            isOwn     = isOwn,
                            isTeacher = isTeacher,
                            onEdit    = viewModel::toggleEdit
                        )
                    }

                    // ── Deneyleri (öğretmen için, sonra) ─────────────────────
                    if (isTeacher) {
                        item { SectionDivider(label = "Deneyleri") }
                        if (uiState.experiments.isEmpty()) {
                            item { EmptyExperiments() }
                        } else {
                            items(uiState.experiments, key = { it.id }) { exp ->
                                TeacherExperimentCard(
                                    exp         = exp,
                                    isOwn       = isOwn,
                                    onCardClick = { onExperimentClick(exp.id) },
                                    onDelete    = { viewModel.deleteExperiment(exp.id) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // ── Bildirim Sheet ───────────────────────────────────────────────────────
    if (showNotif) {
        NotificationsSheet(
            notifications = uiState.notifications,
            isLoading     = uiState.isNotifLoading,
            onMarkRead    = viewModel::markNotificationRead,
            onMarkAllRead = viewModel::markAllRead,
            onDismiss     = { showNotif = false }
        )
    }

    // ── Ayarlar Sheet ────────────────────────────────────────────────────────
    if (showSettings) {
        SettingsSheet(
            user            = uiState.user,
            onEditProfile   = { showSettings = false; viewModel.toggleEdit() },
            onDismiss       = { showSettings = false },
            onLogout        = { showSettings = false; viewModel.logout() },
            onDeleteAccount = { showSettings = false; viewModel.deleteAccount(onLogout) }
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Profil Header
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun ProfileHeader(
    user: User?,
    isOwn: Boolean,
    unreadCount: Long,
    isUploadingPhoto: Boolean,
    onPhotoClick: () -> Unit,
    onNotifClick: () -> Unit,
    onSettingsClick: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxWidth()
            .background(Brush.verticalGradient(listOf(Color(0xFF0A2820), Color(0xFF0D1B2A), DarkBg)))
            .statusBarsPadding()
            .padding(horizontal = 16.dp)
            .padding(top = 16.dp, bottom = 20.dp)
    ) {
        // Sağ üst ikonlar
        if (isOwn) {
            Row(
                modifier              = Modifier.align(Alignment.TopEnd),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box {
                    Box(
                        modifier = Modifier.size(32.dp).clip(CircleShape)
                            .background(DarkSurface2).clickable(onClick = onNotifClick),
                        contentAlignment = Alignment.Center
                    ) { Icon(Icons.Outlined.Notifications, null, tint = TextSecondary, modifier = Modifier.size(16.dp)) }
                    if (unreadCount > 0) {
                        Box(
                            modifier = Modifier.align(Alignment.TopEnd).size(7.dp)
                                .background(Orange400, CircleShape)
                        )
                    }
                }
                Box(
                    modifier = Modifier.size(32.dp).clip(CircleShape)
                        .background(DarkSurface2).clickable(onClick = onSettingsClick),
                    contentAlignment = Alignment.Center
                ) { Icon(Icons.Outlined.Settings, null, tint = TextSecondary, modifier = Modifier.size(16.dp)) }
            }
        }

        // Sol — Avatar + bilgiler
        Row(
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            modifier              = Modifier.fillMaxWidth().padding(top = 4.dp)
        ) {
            // Avatar
            Box(modifier = Modifier.size(72.dp)) {
                Box(
                    modifier = Modifier.fillMaxSize().clip(CircleShape)
                        .background(Brush.linearGradient(listOf(Teal400.copy(0.6f), Color(0xFF006EFF).copy(0.4f))))
                        .border(2.dp, Teal400.copy(0.4f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    if (isUploadingPhoto) {
                        CircularProgressIndicator(color = Teal400, modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                    } else if (user?.profileImageUrl != null) {
                        AsyncImage(
                            model            = user.profileImageUrl,
                            contentDescription = null,
                            contentScale     = ContentScale.Crop,
                            modifier         = Modifier.fillMaxSize().clip(CircleShape)
                        )
                    } else {
                        Text(
                            user?.displayName?.take(2)?.uppercase() ?: "?",
                            color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold
                        )
                    }
                }
                // Fotoğraf değiştir butonu
                if (isOwn) {
                    Box(
                        modifier = Modifier.size(22.dp).align(Alignment.BottomEnd)
                            .clip(CircleShape).background(Teal400).clickable(onClick = onPhotoClick),
                        contentAlignment = Alignment.Center
                    ) { Icon(Icons.Default.CameraAlt, null, tint = Color.White, modifier = Modifier.size(13.dp)) }
                }
            }

            // İsim ve badge
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    user?.displayName ?: "",
                    color = TextPrimary, fontSize = 17.sp, fontWeight = FontWeight.Bold,
                    maxLines = 1, overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(2.dp))
                Text("@${user?.username ?: ""}", color = TextSecondary, fontSize = 12.sp)
                Spacer(Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    RoleBadge(user = user)
                    if (user?.isTeacher == true && user.branch != null) {
                        BranchBadge(branch = user.branch)
                    }
                }
            }
        }
    }
}

@Composable
private fun RoleBadge(user: User?) {
    val isTeacher = user?.isTeacher == true
    Row(
        modifier = Modifier.clip(RoundedCornerShape(20.dp))
            .background(if (isTeacher) Color(0xFF1A3A25) else Color(0xFF1A2A3A))
            .border(1.dp, if (isTeacher) Teal400.copy(0.4f) else Color(0xFF3A5A7A), RoundedCornerShape(20.dp))
            .padding(horizontal = 8.dp, vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(if (isTeacher) "👨‍🏫" else "🎓", fontSize = 11.sp)
        Text(
            if (isTeacher) "Öğretmen" else "Öğrenci",
            color = if (isTeacher) Teal400 else Color(0xFF7AB8E8),
            fontSize = 11.sp, fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun BranchBadge(branch: String) {
    Text(
        branch,
        modifier = Modifier.clip(RoundedCornerShape(20.dp))
            .background(DarkSurface2).padding(horizontal = 8.dp, vertical = 3.dp),
        color = TextSecondary, fontSize = 11.sp
    )
}

// ─────────────────────────────────────────────────────────────────────────────
// Stats
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun TeacherStats(experimentCount: Int, totalFavorites: Long, experienceYears: Int?) {
    Row(
        modifier              = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp)
            .clip(RoundedCornerShape(12.dp)).background(DarkSurface)
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        StatColumn(value = experimentCount.toString(), label = "Deney", color = Teal400)
        StatDivider()
        StatColumn(value = formatCount(totalFavorites), label = "Beğeni", color = Red400)
        if (experienceYears != null) {
            StatDivider()
            StatColumn(value = "${experienceYears}", label = "Yıl Deneyim", color = Orange400)
        }
    }
}

@Composable
private fun StudentStats(user: User?) {
    val memberSince = user?.createdAt?.take(10) ?: "-"
    Row(
        modifier              = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp)
            .clip(RoundedCornerShape(12.dp)).background(DarkSurface)
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        StatColumn(value = "🎓", label = "Öğrenci", color = Color(0xFF7AB8E8), isEmoji = true)
        StatDivider()
        StatColumn(value = memberSince, label = "Üye Tarihi", color = TextSecondary, smallValue = true)
    }
}

@Composable
private fun StatColumn(value: String, label: String, color: Color, isEmoji: Boolean = false, smallValue: Boolean = false) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(horizontal = 8.dp)) {
        Text(
            value,
            color = color,
            fontSize = if (isEmoji) 20.sp else if (smallValue) 12.sp else 15.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(2.dp))
        Text(label, color = TextSecondary, fontSize = 11.sp)
    }
}

@Composable
private fun RowScope.StatDivider() {
    Box(Modifier.width(1.dp).height(28.dp).align(Alignment.CenterVertically).background(DarkSurface3))
}

// ─────────────────────────────────────────────────────────────────────────────
// Section divider başlık
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun ProfileTabBar(selected: Int, tabs: List<String>, onSelect: (Int) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().background(DarkBg)
            .padding(horizontal = 16.dp).padding(top = 8.dp)
    ) {
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
                    modifier   = Modifier.padding(vertical = 10.dp)
                )
                Box(Modifier.fillMaxWidth().height(2.dp)
                    .background(if (selected == i) Teal400 else Color.Transparent))
            }
        }
    }
    HorizontalDivider(thickness = 0.5.dp, color = DarkSurface3)
}

@Composable
private fun SectionDivider(label: String) {
    Row(
        modifier          = Modifier.fillMaxWidth().padding(horizontal = 16.dp).padding(top = 16.dp, bottom = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(Modifier.size(width = 3.dp, height = 16.dp).clip(RoundedCornerShape(2.dp)).background(Teal400))
        Text(label, color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Deney kartı (öğretmen için)
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun TeacherExperimentCard(
    exp: Experiment,
    isOwn: Boolean,
    onCardClick: () -> Unit,
    onDelete: () -> Unit
) {
    var showConfirm by remember { mutableStateOf(false) }

    if (showConfirm) {
        AlertDialog(
            onDismissRequest = { showConfirm = false },
            containerColor   = DarkSurface,
            title = { Text("Deneyi Sil", color = TextPrimary) },
            text  = { Text("Bu deneyi kalıcı olarak silmek istediğinizden emin misiniz?", color = TextSecondary, fontSize = 13.sp) },
            confirmButton = {
                TextButton(onClick = { showConfirm = false; onDelete() }) {
                    Text("Sil", color = Red400, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = { TextButton(onClick = { showConfirm = false }) { Text("İptal", color = TextSecondary) } }
        )
    }

    Card(
        modifier  = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp).clickable(onClick = onCardClick),
        shape     = RoundedCornerShape(10.dp),
        colors    = CardDefaults.cardColors(containerColor = DarkSurface),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(
            modifier              = Modifier.fillMaxWidth().padding(10.dp),
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Thumbnail
            Box(
                modifier = Modifier.size(width = 64.dp, height = 52.dp).clip(RoundedCornerShape(8.dp))
                    .background(Brush.linearGradient(listOf(Color(0xFF0D2D28), Color(0xFF1A2235))))
            ) {
                if (exp.thumbnailUrl != null || exp.videoUrl != null) {
                    AsyncImage(
                        model = exp.thumbnailUrl ?: exp.videoUrl,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("🔬", fontSize = 18.sp)
                    }
                }
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    exp.title, color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold,
                    maxLines = 1, overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(3.dp))
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                        Icon(Icons.Default.Star, null, tint = Orange400, modifier = Modifier.size(12.dp))
                        Text(exp.averageRating?.let { "%.1f".format(it) } ?: "-", color = TextSecondary, fontSize = 11.sp)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                        Icon(Icons.Outlined.FavoriteBorder, null, tint = Red400.copy(0.7f), modifier = Modifier.size(12.dp))
                        Text(formatCount(exp.favoriteCount), color = TextSecondary, fontSize = 11.sp)
                    }
                    exp.subject?.let { SubjectChip(subject = it) }
                }
            }
            if (isOwn) {
                IconButton(onClick = { showConfirm = true }, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.DeleteOutline, null, tint = Red400.copy(0.5f), modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}

@Composable
private fun EmptyExperiments() {
    Column(
        modifier            = Modifier.fillMaxWidth().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("🔬", fontSize = 28.sp)
        Spacer(Modifier.height(8.dp))
        Text("Henüz deney eklenmemiş", color = TextSecondary, fontSize = 13.sp)
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Hakkında Kartı
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun AboutCard(user: User?, isOwn: Boolean, isTeacher: Boolean, onEdit: () -> Unit) {
    Card(
        modifier  = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
        shape     = RoundedCornerShape(12.dp),
        colors    = CardDefaults.cardColors(containerColor = DarkSurface)
    ) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            // Bio
            InfoRow(
                icon  = Icons.Default.Person,
                label = "Biyografi",
                value = user?.bio?.ifBlank { null } ?: "Henüz biyografi eklenmemiş"
            )

            // E-posta (sadece kendi profili)
            if (isOwn) {
                HorizontalDivider(thickness = 0.5.dp, color = DarkSurface3)
                InfoRow(icon = Icons.Default.Email, label = "E-posta", value = user?.email ?: "-")
            }

            // Üyelik tarihi
            HorizontalDivider(thickness = 0.5.dp, color = DarkSurface3)
            InfoRow(icon = Icons.Default.CalendarToday, label = "Üyelik", value = user?.createdAt?.take(10) ?: "-")

            // Öğretmen ekstra
            if (isTeacher) {
                if (user?.branch != null) {
                    HorizontalDivider(thickness = 0.5.dp, color = DarkSurface3)
                    InfoRow(icon = Icons.Default.School, label = "Branş", value = user.branch)
                }
                if (user?.experienceYears != null) {
                    HorizontalDivider(thickness = 0.5.dp, color = DarkSurface3)
                    InfoRow(icon = Icons.Default.WorkHistory, label = "Deneyim", value = "${user.experienceYears} yıl")
                }
            }

            // Düzenle butonu
            if (isOwn) {
                HorizontalDivider(thickness = 0.5.dp, color = DarkSurface3)
                OutlinedButton(
                    onClick  = onEdit,
                    modifier = Modifier.fillMaxWidth().height(38.dp),
                    shape    = RoundedCornerShape(8.dp),
                    border   = BorderStroke(1.dp, Teal400.copy(0.4f)),
                    colors   = ButtonDefaults.outlinedButtonColors(contentColor = Teal400)
                ) {
                    Icon(Icons.Default.Edit, null, modifier = Modifier.size(13.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Profili Düzenle", fontSize = 13.sp)
                }
            }
        }
    }
}

@Composable
private fun InfoRow(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String) {
    Row(verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Icon(icon, null, tint = Teal400, modifier = Modifier.size(16.dp).padding(top = 1.dp))
        Column {
            Text(label, color = TextSecondary, fontSize = 11.sp)
            Spacer(Modifier.height(1.dp))
            Text(value, color = TextPrimary, fontSize = 13.sp, lineHeight = 18.sp)
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Edit Form Kartı
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun EditFormCard(state: ProfileUiState, vm: ProfileViewModel) {
    Card(
        modifier  = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
        shape     = RoundedCornerShape(12.dp),
        colors    = CardDefaults.cardColors(containerColor = DarkSurface2)
    ) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Profili Düzenle", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            HorizontalDivider(thickness = 0.5.dp, color = DarkSurface3)
            ProfileTextField(value = state.editFullName, onChange = vm::onFullNameChange, label = "Ad Soyad", icon = Icons.Default.Person)
            ProfileTextField(value = state.editBio, onChange = vm::onBioChange, label = "Biyografi", icon = Icons.Default.Edit, minLines = 3)
            if (state.user?.isTeacher == true) {
                ProfileTextField(value = state.editBranch, onChange = vm::onBranchChange, label = "Branş", icon = Icons.Default.School)
                ProfileTextField(value = state.editExperienceYears, onChange = vm::onExperienceYearsChange, label = "Deneyim Yılı", icon = Icons.Default.WorkHistory)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick  = vm::toggleEdit,
                    modifier = Modifier.weight(1f).height(38.dp),
                    shape    = RoundedCornerShape(8.dp),
                    border   = BorderStroke(1.dp, DarkSurface3),
                    colors   = ButtonDefaults.outlinedButtonColors(contentColor = TextSecondary)
                ) { Text("İptal", fontSize = 13.sp) }
                Button(
                    onClick  = vm::saveProfile,
                    enabled  = !state.isSaving,
                    modifier = Modifier.weight(1f).height(38.dp),
                    shape    = RoundedCornerShape(8.dp),
                    colors   = ButtonDefaults.buttonColors(containerColor = Teal400)
                ) {
                    if (state.isSaving) CircularProgressIndicator(Modifier.size(14.dp), strokeWidth = 2.dp, color = DarkBg)
                    else Text("Kaydet", color = DarkBg, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
private fun ProfileTextField(
    value: String, onChange: (String) -> Unit, label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector, minLines: Int = 1
) {
    OutlinedTextField(
        value = value, onValueChange = onChange,
        label = { Text(label, fontSize = 11.sp) },
        leadingIcon = { Icon(icon, null, tint = Teal400, modifier = Modifier.size(16.dp)) },
        modifier   = Modifier.fillMaxWidth().then(if (minLines > 1) Modifier.height(80.dp) else Modifier),
        shape      = RoundedCornerShape(8.dp),
        maxLines   = minLines,
        textStyle  = androidx.compose.ui.text.TextStyle(fontSize = 13.sp, color = TextPrimary),
        colors     = OutlinedTextFieldDefaults.colors(
            focusedBorderColor   = Teal400, unfocusedBorderColor = DarkSurface3,
            focusedContainerColor = DarkSurface, unfocusedContainerColor = DarkSurface,
            focusedLabelColor    = Teal400, unfocusedLabelColor = TextSecondary,
            cursorColor          = Teal400
        )
    )
}

// ─────────────────────────────────────────────────────────────────────────────
// Bildirim Sheet
// ─────────────────────────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NotificationsSheet(
    notifications: List<Notification>, isLoading: Boolean,
    onMarkRead: (Long) -> Unit, onMarkAllRead: () -> Unit, onDismiss: () -> Unit
) {
    ModalBottomSheet(onDismissRequest = onDismiss, containerColor = DarkSurface, dragHandle = { SheetHandle() }) {
        Column(Modifier.fillMaxWidth().padding(horizontal = 16.dp).padding(bottom = 24.dp)) {
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                Text("Bildirimler", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                Row {
                    if (notifications.any { !it.isRead }) {
                        TextButton(onClick = onMarkAllRead) { Text("Tümünü Oku", color = Teal400, fontSize = 12.sp) }
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, null, tint = TextSecondary)
                    }
                }
            }
            if (isLoading) {
                Box(Modifier.fillMaxWidth().padding(20.dp), Alignment.Center) {
                    CircularProgressIndicator(color = Teal400, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                }
            } else if (notifications.isEmpty()) {
                Column(Modifier.fillMaxWidth().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("🔔", fontSize = 24.sp)
                    Spacer(Modifier.height(6.dp))
                    Text("Henüz bildirim yok", color = TextSecondary, fontSize = 13.sp)
                }
            } else {
                notifications.forEach { notif ->
                    Row(
                        modifier              = Modifier.fillMaxWidth().clickable { if (!notif.isRead) onMarkRead(notif.id) }
                            .background(if (!notif.isRead) Teal400.copy(0.05f) else Color.Transparent)
                            .padding(vertical = 10.dp),
                        verticalAlignment     = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(modifier = Modifier.size(32.dp).clip(CircleShape).background(DarkSurface3), contentAlignment = Alignment.Center) {
                            Text(notif.icon, fontSize = 13.sp)
                        }
                        Column(Modifier.weight(1f)) {
                            Text(notif.message, fontSize = 12.sp, color = TextPrimary, lineHeight = 16.sp)
                            Text(notif.createdAt.take(10), fontSize = 11.sp, color = TextSecondary)
                        }
                        if (!notif.isRead) Box(Modifier.size(6.dp).background(Teal400, CircleShape))
                    }
                    if (notif != notifications.last()) HorizontalDivider(thickness = 0.5.dp, color = DarkSurface3)
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Ayarlar Sheet
// ─────────────────────────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettingsSheet(
    user: User?, onEditProfile: () -> Unit, onDismiss: () -> Unit,
    onLogout: () -> Unit, onDeleteAccount: () -> Unit
) {
    var showDeleteConfirm by remember { mutableStateOf(false) }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            containerColor   = DarkSurface,
            title = { Text("Hesabı Sil", color = Red400, fontWeight = FontWeight.Bold) },
            text  = { Text("Tüm deneyler ve verileriniz kalıcı olarak silinecek. Bu işlem geri alınamaz.", color = TextSecondary, fontSize = 13.sp) },
            confirmButton = {
                TextButton(onClick = { showDeleteConfirm = false; onDeleteAccount() }) {
                    Text("Evet, Sil", color = Red400, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = { TextButton(onClick = { showDeleteConfirm = false }) { Text("İptal", color = TextSecondary) } }
        )
    }

    ModalBottomSheet(onDismissRequest = onDismiss, containerColor = DarkSurface, dragHandle = { SheetHandle() }) {
        Column(Modifier.fillMaxWidth().padding(horizontal = 16.dp).padding(bottom = 32.dp)) {
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                Text("Ayarlar", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                IconButton(onClick = onDismiss) { Icon(Icons.Default.Close, null, tint = TextSecondary) }
            }
            Spacer(Modifier.height(4.dp))
            SettingsItem(icon = Icons.Default.Person, title = "Hesap Bilgileri",
                subtitle = "${user?.email ?: ""}", onClick = onEditProfile)
            HorizontalDivider(thickness = 0.5.dp, color = DarkSurface3)
            SettingsItem(icon = Icons.Default.AlternateEmail, title = "Kullanıcı Adı",
                subtitle = "@${user?.username ?: ""}", onClick = {})
            HorizontalDivider(thickness = 0.5.dp, color = DarkSurface3)
            SettingsItem(icon = Icons.Default.CalendarToday, title = "Üyelik Tarihi",
                subtitle = user?.createdAt?.take(10) ?: "", onClick = {})
            HorizontalDivider(thickness = 0.5.dp, color = DarkSurface3)
            Spacer(Modifier.height(4.dp))
            // Çıkış
            Row(
                modifier              = Modifier.fillMaxWidth().clickable(onClick = onLogout).padding(vertical = 10.dp),
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(modifier = Modifier.size(32.dp).clip(CircleShape).background(Red400.copy(0.1f)), contentAlignment = Alignment.Center) {
                    Icon(Icons.AutoMirrored.Filled.Logout, null, tint = Red400, modifier = Modifier.size(15.dp))
                }
                Text("Çıkış Yap", fontSize = 13.sp, color = Red400, fontWeight = FontWeight.SemiBold)
            }
            HorizontalDivider(thickness = 0.5.dp, color = DarkSurface3)
            // Hesap sil
            Row(
                modifier              = Modifier.fillMaxWidth().clickable { showDeleteConfirm = true }.padding(vertical = 10.dp),
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(modifier = Modifier.size(32.dp).clip(CircleShape).background(Red400.copy(0.06f)), contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.DeleteForever, null, tint = Red400.copy(0.7f), modifier = Modifier.size(15.dp))
                }
                Column {
                    Text("Hesabı Sil", fontSize = 13.sp, color = Red400.copy(0.8f), fontWeight = FontWeight.SemiBold)
                    Text("Tüm veriler kalıcı olarak silinir", fontSize = 11.sp, color = TextSecondary)
                }
            }
        }
    }
}

@Composable
private fun SettingsItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String, subtitle: String, onClick: () -> Unit
) {
    Row(
        modifier              = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(vertical = 10.dp),
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(modifier = Modifier.size(32.dp).clip(CircleShape).background(DarkSurface2), contentAlignment = Alignment.Center) {
            Icon(icon, null, tint = Teal400, modifier = Modifier.size(14.dp))
        }
        Column(Modifier.weight(1f)) {
            Text(title, fontSize = 13.sp, color = TextPrimary)
            if (subtitle.isNotBlank()) Text(subtitle, fontSize = 11.sp, color = TextSecondary)
        }
        Icon(Icons.Default.ChevronRight, null, tint = DarkSurface3, modifier = Modifier.size(14.dp))
    }
}

@Composable
private fun SheetHandle() {
    Box(Modifier.fillMaxWidth().padding(top = 8.dp, bottom = 2.dp), Alignment.Center) {
        Box(Modifier.size(width = 32.dp, height = 3.dp).background(DarkSurface3, RoundedCornerShape(2.dp)))
    }
}