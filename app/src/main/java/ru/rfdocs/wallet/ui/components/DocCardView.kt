package ru.rfdocs.wallet.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import ru.rfdocs.wallet.data.model.DocCard
import ru.rfdocs.wallet.ui.theme.CardGradients
import ru.rfdocs.wallet.ui.theme.DocIcons
import ru.rfdocs.wallet.ui.theme.MonoStyleSmall
import ru.rfdocs.wallet.ui.theme.WalletTheme

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
    val shape = RoundedCornerShape(if (compact) 20.dp else 26.dp)
    val clickModifier = if (onClick != null) Modifier.clickable { onClick() } else Modifier

    Box(
        modifier = modifier
            .then(clickModifier)
            .shadow(elevation = if (compact) 10.dp else 18.dp, shape = shape)
            .clip(shape)
            .background(
                Brush.linearGradient(
                    colors = listOf(gradient.start, gradient.middle, gradient.end),
                    start = Offset.Zero,
                    end = Offset.Infinite
                )
            )
            .border(1.dp, Color.White.copy(alpha = 0.18f), shape)
            .heightIn(min = if (compact) 92.dp else 168.dp)
    ) {
        CardSheen()
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(if (compact) 14.dp else 20.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                GlassIconBadge(
                    icon = DocIcons.of(card.iconKey),
                    accent = gradient.accent,
                    size = if (compact) 38.dp else 46.dp
                )
                Spacer(modifier = Modifier.size(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = card.title,
                        style = if (compact) {
                            androidx.compose.material3.MaterialTheme.typography.titleMedium
                        } else {
                            androidx.compose.material3.MaterialTheme.typography.titleLarge
                        },
                        fontWeight = FontWeight.SemiBold,
                        color = extras.onCardPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (!compact) {
                        Text(
                            text = card.category.label,
                            style = androidx.compose.material3.MaterialTheme.typography.labelSmall,
                            color = gradient.accent.copy(alpha = 0.85f)
                        )
                    }
                }
                if (card.expiryLevel() > 0) {
                    ExpiryDot(level = card.expiryLevel())
                }
            }

            if (compact) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = numberText(card, showNumber),
                    style = MonoStyleSmall,
                    color = extras.onCardSecondary.copy(alpha = 0.9f),
                    maxLines = 1
                )
            } else {
                Spacer(modifier = Modifier.height(18.dp))
                Text(
                    text = numberText(card, showNumber),
                    style = MonoStyleSmall,
                    color = Color.White.copy(alpha = 0.95f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Column {
                        Text(
                            text = card.subtitle.ifBlank { card.typeKey },
                            style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.6f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (card.validUntil.isNotBlank()) {
                            Text(
                                text = "Действует до ${DocCard.formatDate(card.validUntil)}",
                                style = androidx.compose.material3.MaterialTheme.typography.labelSmall,
                                color = Color.White.copy(alpha = 0.75f)
                            )
                        }
                    }
                    Text(
                        text = "••••",
                        style = MonoStyleSmall,
                        color = gradient.accent.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}

private fun numberText(card: DocCard, showNumber: Boolean): String {
    val field = card.primaryMaskedField ?: return card.subtitle
    if (!field.masked) return field.value
    return if (showNumber) field.value else field.maskedPreview()
}

@Composable
private fun ExpiryDot(level: Int) {
    val color by animateColorAsState(
        targetValue = if (level >= 2) Color(0xFFFF6B6B) else Color(0xFFFFC46B),
        label = "expiryDot"
    )
    Box(
        modifier = Modifier
            .size(10.dp)
            .clip(CircleShape)
            .background(color)
    )
}

@Composable
fun GlassIconBadge(
    icon: ImageVector,
    accent: Color,
    size: androidx.compose.ui.unit.Dp = 44.dp
) {
    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(
                Brush.linearGradient(
                    listOf(
                        Color.White.copy(alpha = 0.26f),
                        Color.White.copy(alpha = 0.08f)
                    )
                )
            )
            .border(1.dp, Color.White.copy(alpha = 0.28f), CircleShape),
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
private fun BoxScope.CardSheen() {
    Box(
        modifier = Modifier
            .matchParentSize()
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.16f),
                        Color.Transparent,
                        Color.White.copy(alpha = 0.04f)
                    ),
                    start = Offset(0f, 0f),
                    end = Offset(900f, 900f)
                )
            )
    )
    Box(
        modifier = Modifier
            .align(Alignment.TopEnd)
            .offset(x = 40.dp, y = (-50).dp)
            .size(180.dp)
            .background(
                Brush.radialGradient(
                    colors = listOf(Color.White.copy(alpha = 0.14f), Color.Transparent)
                ),
                CircleShape
            )
    )
    Box(
        modifier = Modifier
            .align(Alignment.BottomStart)
            .offset(x = (-30).dp, y = 40.dp)
            .size(140.dp)
            .background(
                Brush.radialGradient(
                    colors = listOf(Color.Black.copy(alpha = 0.28f), Color.Transparent)
                ),
                CircleShape
            )
    )
}
