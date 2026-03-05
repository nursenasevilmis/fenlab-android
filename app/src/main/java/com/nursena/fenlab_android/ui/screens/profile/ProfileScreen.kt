package com.nursena.fenlab_android.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.nursena.fenlab_android.domain.model.User
import com.nursena.fenlab_android.ui.components.EmptyState
import com.nursena.fenlab_android.ui.components.ErrorMessage
import com.nursena.fenlab_android.ui.components.ExperimentCard
import com.nursena.fenlab_android.ui.components.LoadingIndicator
import com.nursena.fenlab_android.ui.components.RoleBadge
import com.nursena.fenlab_android.ui.theme.*

@Composable
fun ProfileScreen(
    onExperimentClick: (Long) -> Unit,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    when {
        uiState.isLoading && uiState.user == null -> LoadingIndicator()
        uiState.error != null && uiState.user == null -> ErrorMessage(
            message = uiState.error!!,
            onRetry = viewModel::loadProfile
        )
        else -> ProfileContent(
            uiState           = uiState,
            onExperimentClick = onExperimentClick,
            onEditClick       = viewModel::toggleEdit,
            onSaveEdit        = viewModel::saveProfile,
            onLogout          = viewModel::logout,
            onFullNameChange  = viewModel::onFullNameChange,
            onBioChange       = viewModel::onBioChange,
            onBranchChange    = viewModel::onBranchChange
        )
    }
}

@Composable
private fun ProfileContent(
    uiState: ProfileUiState,
    onExperimentClick: (Long) -> Unit,
    onEditClick: () -> Unit,
    onSaveEdit: () -> Unit,
    onLogout: () -> Unit,
    onFullNameChange: (String) -> Unit,
    onBioChange: (String) -> Unit,
    onBranchChange: (String) -> Unit
) {
    val user = uiState.user ?: return

    LazyColumn(
        modifier            = Modifier.fillMaxSize().background(DarkBg),
        contentPadding      = PaddingValues(bottom = 100.dp)
    ) {
        // ── Header ────────────────────────────────────────────────────────────
        item {
            ProfileHeader(
                user       = user,
                isOwn      = uiState.isOwnProfile,
                onEdit     = onEditClick,
                onLogout   = onLogout
            )
        }

        // ── Edit formu ────────────────────────────────────────────────────────
        if (uiState.isEditing) {
            item {
                EditForm(
                    fullName       = uiState.editFullName,
                    bio            = uiState.editBio,
                    branch         = uiState.editBranch,
                    isSaving       = uiState.isSaving,
                    onFullName     = onFullNameChange,
                    onBio          = onBioChange,
                    onBranch       = onBranchChange,
                    onSave         = onSaveEdit,
                    onCancel       = onEditClick
                )
            }
        }

        // ── İstatistikler ─────────────────────────────────────────────────────
        item {
            StatsRow(
                experimentCount = uiState.experiments.size,
                modifier        = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }

        // ── Deney başlığı ─────────────────────────────────────────────────────
        if (uiState.experiments.isNotEmpty()) {
            item {
                Text(
                    text       = "Deneyleri",
                    color      = TextPrimary,
                    fontSize   = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier   = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
        }

        // ── Deney listesi ─────────────────────────────────────────────────────
        if (uiState.experiments.isEmpty() && !uiState.isLoading) {
            item {
                EmptyState(
                    emoji    = "🔬",
                    title    = "Henüz deney yok",
                    subtitle = "Paylaşılan deney bulunmuyor"
                )
            }
        } else {
            items(items = uiState.experiments, key = { it.id }) { exp ->
                ExperimentCard(
                    experiment      = exp,
                    onCardClick     = { onExperimentClick(exp.id) },
                    onFavoriteClick = {},
                    modifier        = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                )
            }
        }
    }
}

@Composable
private fun ProfileHeader(
    user: User,
    isOwn: Boolean,
    onEdit: () -> Unit,
    onLogout: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp)
            .background(
                Brush.verticalGradient(listOf(Color(0xFF0D2D28), DarkBg))
            )
    ) {
        Column(
            modifier            = Modifier
                .fillMaxSize()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(16.dp))

            // Avatar
            Box(
                modifier         = Modifier
                    .size(80.dp)
                    .background(Teal500, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                if (user.profileImageUrl != null) {
                    AsyncImage(
                        model              = user.profileImageUrl,
                        contentDescription = user.fullName,
                        contentScale       = ContentScale.Crop,
                        modifier           = Modifier.fillMaxSize().clip(CircleShape)
                    )
                } else {
                    Text(
                        text       = user.fullName.take(2).uppercase(),
                        color      = Color.White,
                        fontSize   = 26.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(Modifier.height(10.dp))

            Text(
                text       = user.fullName,
                color      = TextPrimary,
                fontSize   = 18.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(4.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment     = Alignment.CenterVertically
            ) {
                Text("@${user.username}", color = TextSecondary, fontSize = 13.sp)
                RoleBadge(displayRole = user.displayRole)
            }

            if (!user.bio.isNullOrBlank()) {
                Spacer(Modifier.height(6.dp))
                Text(
                    text      = user.bio,
                    color     = TextSecondary,
                    fontSize  = 13.sp
                )
            }
        }

        // Aksiyonlar — sağ üst
        if (isOwn) {
            Row(
                modifier = Modifier.align(Alignment.TopEnd).padding(12.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, contentDescription = "Düzenle",
                        tint = TextSecondary, modifier = Modifier.size(20.dp))
                }
                IconButton(onClick = onLogout) {
                    Icon(Icons.Default.Logout, contentDescription = "Çıkış",
                        tint = TextSecondary, modifier = Modifier.size(20.dp))
                }
            }
        }
    }
}

@Composable
private fun StatsRow(experimentCount: Int, modifier: Modifier = Modifier) {
    Row(
        modifier              = modifier
            .fillMaxWidth()
            .background(DarkSurface2, RoundedCornerShape(12.dp))
            .padding(vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceAround
    ) {
        StatItem(value = experimentCount.toString(), label = "Deney")
    }
}

@Composable
private fun StatItem(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, color = Teal400, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Text(label, color = TextSecondary, fontSize = 12.sp)
    }
}

@Composable
private fun EditForm(
    fullName: String,
    bio: String,
    branch: String,
    isSaving: Boolean,
    onFullName: (String) -> Unit,
    onBio: (String) -> Unit,
    onBranch: (String) -> Unit,
    onSave: () -> Unit,
    onCancel: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        colors   = CardDefaults.cardColors(containerColor = DarkSurface2),
        shape    = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Profili Düzenle", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)

            FenlabTextField(value = fullName, label = "Ad Soyad", onChange = onFullName)
            FenlabTextField(value = bio, label = "Biyografi", onChange = onBio, maxLines = 3)
            FenlabTextField(value = branch, label = "Branş", onChange = onBranch)

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = onCancel, modifier = Modifier.weight(1f),
                    colors  = ButtonDefaults.outlinedButtonColors(contentColor = TextSecondary)
                ) { Text("İptal") }

                Button(
                    onClick  = onSave,
                    modifier = Modifier.weight(1f),
                    colors   = ButtonDefaults.buttonColors(containerColor = Teal400),
                    enabled  = !isSaving
                ) {
                    if (isSaving)
                        CircularProgressIndicator(modifier = Modifier.size(16.dp),
                            color = DarkBg, strokeWidth = 2.dp)
                    else
                        Text("Kaydet", color = DarkBg)
                }
            }
        }
    }
}

@Composable
private fun FenlabTextField(
    value: String,
    label: String,
    onChange: (String) -> Unit,
    maxLines: Int = 1
) {
    OutlinedTextField(
        value         = value,
        onValueChange = onChange,
        label         = { Text(label, color = TextSecondary, fontSize = 13.sp) },
        maxLines      = maxLines,
        colors        = OutlinedTextFieldDefaults.colors(
            focusedBorderColor   = Teal400,
            unfocusedBorderColor = DarkSurface3,
            focusedTextColor     = TextPrimary,
            unfocusedTextColor   = TextPrimary,
            cursorColor          = Teal400
        ),
        modifier = Modifier.fillMaxWidth()
    )
}