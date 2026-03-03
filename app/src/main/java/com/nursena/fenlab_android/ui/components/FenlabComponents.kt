package com.nursena.fenlab_android.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.nursena.fenlab_android.domain.model.UserSummary
import com.nursena.fenlab_android.domain.model.enums.DifficultyLevel
import com.nursena.fenlab_android.domain.model.enums.EnvironmentType
import com.nursena.fenlab_android.domain.model.enums.SubjectType
import com.nursena.fenlab_android.ui.theme.*

// ── SubjectChip ───────────────────────────────────────────────────────────────
@Composable
fun SubjectChip(subject: SubjectType, modifier: Modifier = Modifier) {
    val (bg, fg) = when (subject) {
        SubjectType.SCIENCE   -> ChipScience   to ChipScienceText
        SubjectType.PHYSICS   -> ChipPhysics   to ChipPhysicsText
        SubjectType.CHEMISTRY -> ChipChemistry to ChipChemistryText
        SubjectType.BIOLOGY   -> ChipBiology   to ChipBiologyText
        SubjectType.OTHER     -> ChipOther     to ChipOtherText
    }
    Surface(modifier = modifier, color = bg, shape = RoundedCornerShape(8.dp)) {
        Text(
            text = subject.toDisplayString(),
            color = fg, fontSize = 11.sp, fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}

// ── DifficultyChip ────────────────────────────────────────────────────────────
@Composable
fun DifficultyChip(difficulty: DifficultyLevel, modifier: Modifier = Modifier) {
    val (bg, fg) = when (difficulty) {
        DifficultyLevel.EASY   -> Color(0xFF0D3020) to Green400
        DifficultyLevel.MEDIUM -> Color(0xFF3D2C00) to Yellow400
        DifficultyLevel.HARD   -> Color(0xFF3D0F0F) to Red400
    }
    Surface(modifier = modifier, color = bg, shape = RoundedCornerShape(8.dp)) {
        Text(
            text = difficulty.toDisplayString(),
            color = fg, fontSize = 11.sp, fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}

// ── EnvironmentChip ───────────────────────────────────────────────────────────
@Composable
fun EnvironmentChip(environment: EnvironmentType, modifier: Modifier = Modifier) {
    Surface(modifier = modifier, color = DarkSurface3, shape = RoundedCornerShape(8.dp)) {
        Text(
            text = environment.toDisplayString(),
            color = TextSecondary, fontSize = 11.sp,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}

// ── TagChip ───────────────────────────────────────────────────────────────────
@Composable
fun TagChip(text: String, modifier: Modifier = Modifier) {
    Surface(modifier = modifier, color = DarkSurface3, shape = RoundedCornerShape(8.dp)) {
        Text(
            text = text, fontSize = 11.sp, color = TextSecondary,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}

// ── RoleBadge ─────────────────────────────────────────────────────────────────
@Composable
fun RoleBadge(displayRole: String, modifier: Modifier = Modifier) {
    Surface(modifier = modifier, color = Teal100, shape = RoundedCornerShape(4.dp)) {
        Text(
            text = displayRole, color = Teal400,
            fontSize = 10.sp, fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        )
    }
}

// ── UserAvatar ────────────────────────────────────────────────────────────────
@Composable
fun UserAvatar(user: UserSummary, size: Dp = 36.dp, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.size(size).background(Teal500, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        if (user.profileImageUrl != null) {
            AsyncImage(
                model = user.profileImageUrl, contentDescription = user.displayName,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize().clip(CircleShape)
            )
        } else {
            Text(
                text = user.initials, color = Color.White,
                fontSize = (size.value * 0.35f).sp, fontWeight = FontWeight.Bold
            )
        }
    }
}

// ── AuthorRow ─────────────────────────────────────────────────────────────────
@Composable
fun AuthorRow(
    user: UserSummary,
    favoriteCount: Long,
    commentCount: Long,
    modifier: Modifier = Modifier
) {
    Row(modifier = modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        UserAvatar(user = user, size = 32.dp)
        Spacer(Modifier.width(8.dp))
        Text(
            text = user.displayName, fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold, color = TextPrimary,
            modifier = Modifier.weight(1f)
        )
        Spacer(Modifier.width(6.dp))
        Icon(
            imageVector = Icons.Default.FavoriteBorder,
            contentDescription = null, tint = TextSecondary,
            modifier = Modifier.size(13.dp)
        )
        Spacer(Modifier.width(3.dp))
        Text(text = formatCount(favoriteCount), fontSize = 12.sp, color = TextSecondary)
    }
}

// ── FavoriteButton ────────────────────────────────────────────────────────────
@Composable
fun FavoriteButton(isFavorited: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    IconButton(
        onClick = onClick,
        modifier = modifier
            .size(34.dp)
            .background(Color.Black.copy(alpha = 0.45f), CircleShape)
    ) {
        Icon(
            imageVector = if (isFavorited) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
            contentDescription = null,
            tint = if (isFavorited) Red400 else TextSecondary,
            modifier = Modifier.size(16.dp)
        )
    }
}

// ── LoadingIndicator ──────────────────────────────────────────────────────────
@Composable
fun LoadingIndicator(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(color = Teal400, strokeWidth = 2.dp)
    }
}

// ── ErrorMessage ──────────────────────────────────────────────────────────────
@Composable
fun ErrorMessage(message: String, onRetry: (() -> Unit)? = null, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("⚠️", fontSize = 36.sp)
        Spacer(Modifier.height(8.dp))
        Text(text = message, color = TextSecondary, fontSize = 14.sp)
        if (onRetry != null) {
            Spacer(Modifier.height(12.dp))
            Button(
                onClick = onRetry,
                colors = ButtonDefaults.buttonColors(containerColor = Teal400)
            ) { Text("Tekrar Dene", color = DarkBg) }
        }
    }
}

// ── EmptyState ────────────────────────────────────────────────────────────────
@Composable
fun EmptyState(emoji: String, title: String, subtitle: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(emoji, fontSize = 48.sp)
        Spacer(Modifier.height(12.dp))
        Text(text = title, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
        Spacer(Modifier.height(4.dp))
        Text(text = subtitle, fontSize = 13.sp, color = TextSecondary)
    }
}

// ── Yardımcı ─────────────────────────────────────────────────────────────────
fun formatCount(count: Long): String = when {
    count >= 1_000_000 -> "${"%.1f".format(count / 1_000_000.0)}M"
    count >= 1_000     -> "${"%.1f".format(count / 1_000.0)}B"
    else               -> count.toString()
}