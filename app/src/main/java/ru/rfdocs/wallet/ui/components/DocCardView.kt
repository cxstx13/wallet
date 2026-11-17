package ru.rfdocs.wallet.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.matchParentSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import ru.rfdocs.wallet.data.model.DocCard
import ru.rfdocs.wallet.ui.theme.CardGradient
import ru.rfdocs.wallet.ui.theme.CardGradients
import ru.rfdocs.wallet.ui.theme.DocIcons
import ru.rfdocs.wallet.ui.theme.MonoStyle
import ru.rfdocs.wallet.ui.theme.MonoStyleSmall
import ru.rfdocs.wallet.ui.theme.WalletTheme
import kotlin.random.Random

@Composable
fun PlasticCard(
    card: DocCard,
    modifier: Modifier = Modifier,
    showNumber: Boolean = false,
    compact: Boolean = false,
    onClick: (() -> Unit)? = null
) {
    val gradient = CardGradients.of(card.gradientKey)
    val extras = WalletTheme.extra
    val haptics = LocalHapticFeedback.current
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.985f else 1f,
        label = "cardScale"
    )
    val shape = RoundedCornerShape(if (compact) 22.dp else 26.dp)

    Box(
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .shadow(elevation = if (compact) 8.dp else 16.dp, shape = shape, clip = false)
            .clip(shape)
            .clickable(
                interactionSource = interaction,
                indication = null
            ) {
                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                onClick?.invoke()
            }
    ) {
        CardSurface(cardId = card.id, gradient = gradient, modifier = Modifier.matchParentSize())

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = if (compact) 86.dp else 176.dp)
                .padding(if (compact) 16.dp else 22.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = card.title,
                        style = if (compact) {
                            MaterialTheme.typography.titleMedium
                        } else {
                            MaterialTheme.typography.titleLarge
                        },
                        fontWeight = FontWeight.SemiBold,
                        color = extras.onCardPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = card.category.label.uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        color = gradient.accent.copy(alpha = 0.9f),
                        maxLines = 1
                    )
                }
                TypeBadge(
                    icon = DocIcons.of(card.iconKey),
                    accent = gradient.accent,
                    size = if (compact) 30.dp else 38.dp
                )
            }

            if (compact) {
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = numberLine(card, showNumber),
                    style = MonoStyleSmall,
                    color = Color.White.copy(alpha = 0.92f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            } else {
                Spacer(modifier = Modifier.height(26.dp))
                Text(
                    text = numberLine(card, showNumber),
                    style = MonoStyle,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = card.subtitle.ifBlank { card.typeKey },
                    style = MaterialTheme.typography.bodySmall,
                    color = extras.onCardSecondary.copy(alpha = 0.75f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(18.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Column {
                        if (card.validUntil.isNotBlank()) {
                            Text(
                                text = "Действует до ${DocCard.formatDate(card.validUntil)}",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White.copy(alpha = 0.8f)
                            )
                        }
                    }
                    Text(
                        text = "••••",
                        style = MonoStyleSmall,
                        color = gradient.accent.copy(alpha = 0.85f)
                    )
                }
            }
        }
    }
}

private fun numberLine(card: DocCard, showNumber: Boolean): String {
    val field = card.primaryMaskedField ?: return card.subtitle
    if (!field.masked) return field.value
    return if (showNumber) field.value else field.maskedPreview()
}

@Composable
private fun CardSurface(
    cardId: String,
    gradient: CardGradient,
    modifier: Modifier = Modifier
) {
    val seed = remember(cardId) { cardId.hashCode() }
    Box(
        modifier = modifier.background(
            Brush.linearGradient(
                colors = listOf(gradient.start, gradient.middle, gradient.end),
                start = Offset.Zero,
                end = Offset.Infinite
            )
        )
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color.White.copy(alpha = 0.17f), Color.Transparent),
                    center = Offset(size.width * 0.88f, -size.height * 0.16f),
                    radius = size.maxDimension * 0.85f
                ),
                radius = size.maxDimension * 0.85f,
                center = Offset(size.width * 0.88f, -size.height * 0.16f)
            )

            drawRect(
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color.Transparent,
                        Color.White.copy(alpha = 0.07f),
                        Color.Transparent
                    ),
                    start = Offset(0f, size.height),
                    end = Offset(size.width, 0f)
                )
            )

            val random = Random(seed)
            repeat(64) {
                val x = random.nextFloat() * size.width
                val y = random.nextFloat() * size.height
                val radius = 0.5f + random.nextFloat() * 1.3f
                drawCircle(
                    color = Color.White.copy(alpha = 0.02f + random.nextFloat() * 0.035f),
                    radius = radius,
                    center = Offset(x, y)
                )
            }

            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color.Black.copy(alpha = 0.30f), Color.Transparent),
                    center = Offset(-size.width * 0.05f, size.height * 1.05f),
                    radius = size.maxDimension * 0.7f
                ),
                radius = size.maxDimension * 0.7f,
                center = Offset(-size.width * 0.05f, size.height * 1.05f)
            )
        }
        Box(
            modifier = Modifier
                .matchParentSize()
                .border(1.dp, Color.White.copy(alpha = 0.16f), RoundedCornerShape(26.dp))
        )
    }
}

@Composable
fun TypeBadge(
    icon: ImageVector,
    accent: Color,
    size: androidx.compose.ui.unit.Dp = 38.dp
) {
    Box(
        modifier = Modifier
            .size(size)
            .clip(RoundedCornerShape(size / 3f))
            .background(Color.White.copy(alpha = 0.12f))
            .border(1.dp, Color.White.copy(alpha = 0.22f), RoundedCornerShape(size / 3f)),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = accent,
            modifier = Modifier.size(size * 0.52f)
        )
    }
}

@Composable
fun GlassIconBadge(
    icon: ImageVector,
    accent: Color,
    size: androidx.compose.ui.unit.Dp = 40.dp
) {
    TypeBadge(icon = icon, accent = accent, size = size)
}