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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import ru.rfdocs.wallet.data.model.DocCard
import ru.rfdocs.wallet.data.model.DocField
import ru.rfdocs.wallet.ui.input.FieldMasks
import ru.rfdocs.wallet.ui.input.digitsOnly
import ru.rfdocs.wallet.ui.theme.CardGradient
import ru.rfdocs.wallet.ui.theme.DocumentGradients
import ru.rfdocs.wallet.ui.theme.MonoLarge
import ru.rfdocs.wallet.ui.theme.MonoSmall
import ru.rfdocs.wallet.ui.theme.WalletTheme

fun documentIcon(typeKey: String): ImageVector {
    val key = typeKey.lowercase()
    return when {
        key.contains("foreign") || key.contains("zagran") -> Icons.Filled.AccountBox
        key.contains("passport") -> Icons.Filled.AccountBox
        key.contains("snils") -> Icons.Filled.AccountCircle
        key.contains("inn") -> Icons.Filled.Info
        key.contains("driver") || key.contains("vu") -> Icons.Filled.Place
        key.contains("sts") || key.contains("pts") -> Icons.Filled.Place
        key.contains("osago") || key.contains("kasko") -> Icons.Filled.Lock
        key.contains("oms") || key.contains("dms") -> Icons.Filled.Favorite
        key.contains("birth") -> Icons.Filled.Star
        key.contains("bank") -> Icons.Filled.ShoppingCart
        key.contains("date") -> Icons.Filled.DateRange
        else -> Icons.Filled.Settings
    }
}

private fun primaryNumberField(card: DocCard): DocField? =
    card.fields.firstOrNull { it.secret && it.value.isNotBlank() }
        ?: card.fields.firstOrNull { !it.mask.isNullOrBlank() && it.value.isNotBlank() }
        ?: card.fields.firstOrNull { it.value.isNotBlank() }

fun cardNumberLine(card: DocCard, showNumber: Boolean): String {
    val field = primaryNumberField(card) ?: return ""
    val formatted = if (field.mask.isNullOrBlank()) {
        field.value
    } else {
        FieldMasks.display(field.value, field.mask)
    }

    if (field.secret && !showNumber) {
        val tail = digitsOnly(field.value).takeLast(4)
        return if (tail.isEmpty()) "••••" else "•••• $tail"
    }
    return formatted
}

@Composable
fun PlasticCard(
    card: DocCard,
    modifier: Modifier = Modifier,
    showNumber: Boolean = false,
    compact: Boolean = false,
    onClick: (() -> Unit)? = null
) {
    val gradient = DocumentGradients.forType(card.typeKey)
    val extras = WalletTheme.extra
    val haptics = LocalHapticFeedback.current
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.985f else 1f,
        label = "cardScale"
    )
    val shape = RoundedCornerShape(if (compact) 22.dp else 26.dp)
    val numberLine = cardNumberLine(card, showNumber)

    Box(
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .shadow(elevation = if (compact) 10.dp else 18.dp, shape = shape, clip = false)
            .clip(shape)
            .background(Brush.linearGradient(gradient.colors))
            .clickable(
                interactionSource = interaction,
                indication = null
            ) {
                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                onClick?.invoke()
            }
    ) {
        CardShine(accent = gradient.accent, modifier = Modifier.matchParentSize())

        Box(
            modifier = Modifier
                .matchParentSize()
                .border(
                    width = 1.dp,
                    brush = DocumentGradients.borderBrush(gradient.accent),
                    shape = shape
                )
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = if (compact) 116.dp else 190.dp)
                .padding(if (compact) 18.dp else 22.dp)
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
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = "ДОКУМЕНТ РФ",
                        style = MaterialTheme.typography.labelSmall,
                        color = gradient.accent.copy(alpha = 0.92f)
                    )
                }
                Spacer(Modifier.size(12.dp))
                TypeBadge(
                    icon = documentIcon(card.typeKey),
                    accent = gradient.accent,
                    size = if (compact) 38.dp else 44.dp
                )
            }

            Spacer(Modifier.height(if (compact) 14.dp else 30.dp))

            Text(
                text = numberLine,
                style = if (compact) MonoSmall else MonoLarge,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            if (!compact) {
                Spacer(Modifier.height(6.dp))
                Text(
                    text = "Хранится только на этом устройстве",
                    style = MaterialTheme.typography.bodySmall,
                    color = extras.onCardSecondary.copy(alpha = 0.8f),
                    maxLines = 1
                )
                Spacer(Modifier.height(20.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    val expiry = expiryDateOf(card)
                    Text(
                        text = if (expiry != null) {
                            "до ${expiry.format(java.time.format.DateTimeFormatter.ofPattern("dd.MM.yyyy"))}"
                        } else {
                            "Офлайн‑кошелёк"
                        },
                        style = MaterialTheme.typography.labelSmall,
                        color = extras.onCardSecondary.copy(alpha = 0.75f)
                    )
                    Text(
                        text = "••••",
                        style = MonoSmall,
                        color = gradient.accent.copy(alpha = 0.85f)
                    )
                }
            }
        }
    }
}

@Composable
private fun CardShine(accent: Color, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        drawRect(
            brush = Brush.linearGradient(
                colors = listOf(
                    Color.White.copy(alpha = 0.14f),
                    Color.Transparent,
                    Color.White.copy(alpha = 0.05f)
                ),
                start = Offset(0f, 0f),
                end = Offset(size.width, size.height)
            )
        )
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(accent.copy(alpha = 0.22f), Color.Transparent),
                center = Offset(size.width * 0.88f, size.height * 0.12f),
                radius = size.minDimension * 0.7f
            ),
            radius = size.minDimension * 0.7f,
            center = Offset(size.width * 0.88f, size.height * 0.12f)
        )
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color.Black.copy(alpha = 0.35f), Color.Transparent),
                center = Offset(size.width * 0.1f, size.height * 1.05f),
                radius = size.minDimension * 0.9f
            ),
            radius = size.minDimension * 0.9f,
            center = Offset(size.width * 0.1f, size.height * 1.05f)
        )
    }
}

@Composable
fun TypeBadge(
    icon: ImageVector,
    accent: Color,
    size: Dp = 38.dp
) {
    Box(
        modifier = Modifier
            .size(size)
            .clip(RoundedCornerShape(size / 3f))
            .background(Color.White.copy(alpha = 0.14f))
            .border(
                width = 1.dp,
                color = Color.White.copy(alpha = 0.26f),
                shape = RoundedCornerShape(size / 3f)
            ),
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
    size: Dp = 40.dp
) {
    TypeBadge(icon = icon, accent = accent, size = size)
}

@Composable
fun DocumentCardGradientPreview(
    gradient: CardGradient,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(Brush.linearGradient(gradient.colors))
    )
}