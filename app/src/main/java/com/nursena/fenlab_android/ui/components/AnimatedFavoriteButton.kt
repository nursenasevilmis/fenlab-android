package com.nursena.fenlab_android.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.nursena.fenlab_android.ui.theme.Red400

@Composable
fun AnimatedFavoriteButton(
    isFavorited: Boolean,
    modifier: Modifier = Modifier,
    backgroundColor: Color = Color(0x73000000),
    iconWhenNotFavorited: Color = Color.White,
    iconWhenFavorited: Color = Red400,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    var clicked by remember { mutableStateOf(false) }

    val scale by animateFloatAsState(
        targetValue = if (clicked) 1.35f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        finishedListener = { clicked = false },
        label = "favorite_scale"
    )

    val rotation by animateFloatAsState(
        targetValue = if (clicked) -12f else 0f,
        animationSpec = tween(durationMillis = 180),
        label = "favorite_rotation"
    )

    Box(
        modifier = modifier
            .size(38.dp)
            .clip(CircleShape)
            .background(backgroundColor)
            .clickable(enabled = enabled) {
                clicked = true
                onClick()
            },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = if (isFavorited) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
            contentDescription = null,
            tint = if (isFavorited) iconWhenFavorited else iconWhenNotFavorited,
            modifier = Modifier
                .size(19.dp)
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                    rotationZ = rotation
                    alpha = if (enabled) 1f else 0.45f
                }
        )
    }
}