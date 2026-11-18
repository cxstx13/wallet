package ru.rfdocs.wallet.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import ru.rfdocs.wallet.data.model.DocCard
import ru.rfdocs.wallet.ui.theme.CardGradient

@Composable
fun PlasticCard(
    card: DocCard,
    modifier: Modifier = Modifier,
    showNumber: Boolean = false,
    compact: Boolean = false,
    onClick: (() -> Unit)? = null
) {
    DocumentStyleCard(
        card = card,
        modifier = modifier,
        showNumber = showNumber,
        compact = compact,
        onClick = onClick
    )
}

fun documentIcon(typeKey: String): ImageVector = documentIconOf(typeKey)

fun cardNumberLine(card: DocCard, showNumber: Boolean): String = numberLineOf(card, showNumber)

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