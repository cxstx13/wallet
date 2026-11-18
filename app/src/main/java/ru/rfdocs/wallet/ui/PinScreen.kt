package ru.rfdocs.wallet.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import ru.rfdocs.wallet.ui.theme.MonoStyle
import ru.rfdocs.wallet.ui.theme.Palette
import ru.rfdocs.wallet.ui.theme.WalletTheme

const val PIN_LENGTH = 4

@Composable
fun PinHeader(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(WalletTheme.extra.glassFill)
                .border(1.dp, WalletTheme.extra.glassBorder, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.Lock,
                contentDescription = null,
                tint = Palette.Gold,
                modifier = Modifier.size(30.dp)
            )
        }
        Spacer(Modifier.height(18.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun PinDots(
    length: Int,
    filled: Int,
    errorCount: Int,
    modifier: Modifier = Modifier
) {
    val shake = remember { Animatable(0f) }

    LaunchedEffect(errorCount) {
        if (errorCount <= 0) return@LaunchedEffect
        shake.snapTo(0f)
        shake.animateTo(
            targetValue = 0f,
            animationSpec = keyframes {
                durationMillis = 420
                0f at 0
                -20f at 55
                18f at 120
                -14f at 190
                9f at 260
                -4f at 330
                0f at 420
            }
        )
    }

    Row(
        modifier = modifier
            .graphicsLayer { translationX = shake.value },
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(length) { index ->
            val isFilled = index < filled
            val scale by animateFloatAsState(
                targetValue = if (isFilled) 1f else 0.72f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                ),
                label = "pinDotScale"
            )
            val color = when {
                errorCount > 0 && errorCount % 2 == 1 -> Palette.Danger
                isFilled -> Palette.Gold
                else -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.32f)
            }
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                    }
                    .clip(CircleShape)
                    .background(if (isFilled) color else Color.Transparent)
                    .border(1.5.dp, color, CircleShape)
            )
        }
    }
}

@Composable
fun PinKeypad(
    onDigit: (String) -> Unit,
    onBackspace: () -> Unit,
    onBiometric: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val haptics = LocalHapticFeedback.current
    val rows = listOf(
        listOf("1", "2", "3"),
        listOf("4", "5", "6"),
        listOf("7", "8", "9")
    )

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        rows.forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                row.forEach { digit ->
                    PinKeyButton(
                        modifier = Modifier.weight(1f),
                        onClick = {
                            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                            onDigit(digit)
                        }
                    ) {
                        Text(
                            text = digit,
                            style = MonoStyle,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                if (onBiometric != null) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(WalletTheme.extra.glassFill)
                            .border(1.dp, WalletTheme.extra.glassBorder, RoundedCornerShape(20.dp))
                            .clickable(
                                interactionSource = MutableInteractionSource(),
                                indication = null
                            ) {
                                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                onBiometric()
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Fingerprint,
                            contentDescription = "Вход по отпечатку",
                            tint = Palette.Gold,
                            modifier = Modifier.size(30.dp)
                        )
                    }
                }
            }

            PinKeyButton(
                modifier = Modifier.weight(1f),
                onClick = {
                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    onDigit("0")
                }
            ) {
                Text(
                    text = "0",
                    style = MonoStyle,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                PinKeyButton(
                    modifier = Modifier.width(64.dp),
                    onClick = {
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        onBackspace()
                    }
                ) {
                    Icon(
                        imageVector = Icons.Filled.Backspace,
                        contentDescription = "Удалить",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun PinKeyButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .height(64.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(WalletTheme.extra.glassFill)
            .border(1.dp, WalletTheme.extra.glassBorder, RoundedCornerShape(20.dp))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}