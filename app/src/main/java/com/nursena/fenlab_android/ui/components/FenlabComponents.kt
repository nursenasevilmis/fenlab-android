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

private val TealColor    = Color(0xFF00897B)
private val OrangeColor  = Color(0xFFFF8F00)
private val Gray900Color = Color(0xFF1A1A1A)
private val Gray700Color = Color(0xFF555555)
private val Gray400Color = Color(0xFFAAAAAA)
private val Gray100Color = Color(0xFFF0F0F0)
private val Red500Color  = Color(0xFFE53935)

// ── SubjectChip ───────────────────────────────────────────────────────────────
@Composable
fun SubjectChip(subject: SubjectType, modifier: Modifier = Modifier) {
    Surface(modifier = modifier, color = OrangeColor, shape = RoundedCornerShape(20.dp)) {
        Text(
            text = subject.toDisplayString(),
            color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}

// ── DifficultyChip ────────────────────────────────────────────────────────────
@Composable
fun DifficultyChip(difficulty: DifficultyLevel, modifier: Modifier = Modifier) {
    val (bg, fg) = when (difficulty) {
        DifficultyLevel.EASY   -> Color(0xFFE8F5E9) to Color(0xFF2E7D32)
        DifficultyLevel.MEDIUM -> Color(0xFFFFF8E1) to Color(0xFFF57F17)
        DifficultyLevel.HARD   -> Color(0xFFFFEBEE) to Color(0xFFC62828)
    }
    Surface(modifier = modifier, color = bg, shape = RoundedCornerShape(20.dp)) {
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
    Surface(modifier = modifier, color = Gray100Color, shape = RoundedCornerShape(20.dp)) {
        Text(
            text = environment.toDisplayString(),
            color = Gray700Color, fontSize = 11.sp,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}

// ── TagChip (genel) ───────────────────────────────────────────────────────────
@Composable
fun TagChip(text: String, modifier: Modifier = Modifier) {
    Surface(modifier = modifier, color = Gray100Color, shape = RoundedCornerShape(20.dp)) {
        Text(
            text = text, fontSize = 11.sp, color = Gray700Color,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}

// ── RoleBadge ─────────────────────────────────────────────────────────────────
@Composable
fun RoleBadge(displayRole: String, modifier: Modifier = Modifier) {
    Surface(modifier = modifier, color = TealColor.copy(alpha = 0.12f), shape = RoundedCornerShape(4.dp)) {
        Text(
            text = displayRole, color = TealColor,
            fontSize = 10.sp, fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        )
    }
}

// ── UserAvatar ────────────────────────────────────────────────────────────────
@Composable
fun UserAvatar(user: UserSummary, size: Dp = 36.dp, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.size(size).background(TealColor, CircleShape),
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
        UserAvatar(user = user, size = 34.dp)
        Spacer(Modifier.width(8.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = user.displayName, fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold, color = Gray900Color
            )
            Spacer(Modifier.height(2.dp))
            RoleBadge(displayRole = user.displayRole)
        }
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            StatItem(icon = Icons.Default.FavoriteBorder, count = favoriteCount)
        }
    }
}

// ── StatItem ──────────────────────────────────────────────────────────────────
@Composable
fun StatItem(icon: ImageVector, count: Long) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(imageVector = icon, contentDescription = null, tint = Gray400Color, modifier = Modifier.size(14.dp))
        Spacer(Modifier.width(3.dp))
        Text(text = formatCount(count), fontSize = 12.sp, color = Gray400Color)
    }
}

// ── FavoriteButton ────────────────────────────────────────────────────────────
@Composable
fun FavoriteButton(isFavorited: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    IconButton(
        onClick = onClick,
        modifier = modifier.size(36.dp).background(Color.White, CircleShape)
    ) {
        Icon(
            imageVector = if (isFavorited) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
            contentDescription = if (isFavorited) "Favoriden çıkar" else "Favoriye ekle",
            tint = if (isFavorited) Red500Color else Gray400Color,
            modifier = Modifier.size(18.dp)
        )
    }
}

// ── LoadingIndicator ──────────────────────────────────────────────────────────
@Composable
fun LoadingIndicator(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(color = TealColor)
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
        Text(text = message, color = Gray700Color, fontSize = 14.sp)
        if (onRetry != null) {
            Spacer(Modifier.height(12.dp))
            Button(onClick = onRetry, colors = ButtonDefaults.buttonColors(containerColor = TealColor)) {
                Text("Tekrar Dene", color = Color.White)
            }
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
        Text(text = title, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Gray900Color)
        Spacer(Modifier.height(4.dp))
        Text(text = subtitle, fontSize = 13.sp, color = Gray700Color)
    }
}

// ── Yardımcı ─────────────────────────────────────────────────────────────────
fun formatCount(count: Long): String = when {
    count >= 1_000_000 -> "${"%.1f".format(count / 1_000_000.0)}M"
    count >= 1_000     -> "${"%.1f".format(count / 1_000.0)}B"
    else               -> count.toString()
}