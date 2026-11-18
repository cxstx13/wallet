package ru.rfdocs.wallet.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountBalance
import androidx.compose.material.icons.rounded.AssignmentInd
import androidx.compose.material.icons.rounded.Badge
import androidx.compose.material.icons.rounded.ChildCare
import androidx.compose.material.icons.rounded.CreditCard
import androidx.compose.material.icons.rounded.DirectionsCar
import androidx.compose.material.icons.rounded.MedicalServices
import androidx.compose.material.icons.rounded.VerifiedUser
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
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.rfdocs.wallet.data.model.DocCard
import ru.rfdocs.wallet.data.model.DocField
import ru.rfdocs.wallet.ui.input.FieldMasks
import ru.rfdocs.wallet.ui.input.digitsOnly
import ru.rfdocs.wallet.ui.theme.DocumentGradients
import ru.rfdocs.wallet.ui.theme.MonoSmall
import kotlin.math.sin
import kotlin.random.Random

const val ID_CARD_RATIO = 1.586f

fun documentIconOf(typeKey: String): ImageVector {
    val key = typeKey.lowercase()
    return when {
        key.contains("passport") || key.contains("zagran") || key.contains("foreign") ->
            Icons.Rounded.Badge

        key.contains("snils") || key.contains("inn") -> Icons.Rounded.AssignmentInd
        key.contains("driver") || key.contains("vu") ||
            key.contains("sts") || key.contains("pts") -> Icons.Rounded.DirectionsCar

        key.contains("osago") || key.contains("kasko") -> Icons.Rounded.VerifiedUser
        key.contains("oms") || key.contains("dms") -> Icons.Rounded.MedicalServices
        key.contains("birth") || key.contains("family") -> Icons.Rounded.ChildCare
        key.contains("bank") -> Icons.Rounded.AccountBalance
        else -> Icons.Rounded.CreditCard
    }
}

fun primaryNumberFieldOf(card: DocCard): DocField? =
    card.fields.firstOrNull { it.secret && it.value.isNotBlank() }
        ?: card.fields.firstOrNull { !it.mask.isNullOrBlank() && it.value.isNotBlank() }
        ?: card.fields.firstOrNull { it.value.isNotBlank() }

fun numberLineOf(card: DocCard, showNumber: Boolean): String {
    val field = primaryNumberFieldOf(card) ?: return ""
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

fun nameLineOf(card: DocCard): String {
    val nameField = card.fields.firstOrNull { field ->
        field.value.isNotBlank() && (
            field.label.contains("ФИО", ignoreCase = true) ||
                field.label.contains("Фамилия", ignoreCase = true) ||
                field.label.contains("Имя", ignoreCase = true)
            )
    }
    return nameField?.value?.trim().orEmpty()
}

fun initialsOf(card: DocCard): String {
    val source = nameLineOf(card)
    if (source.isBlank()) return "ID"
    val parts = source.split(" ").filter { it.isNotBlank() }
    val letters = parts.take(2).mapNotNull { part -> part.firstOrNull()?.uppercaseChar() }
    return if (letters.isEmpty()) "ID" else letters.joinToString("")
}

fun isCustomCard(typeKey: String): Boolean = typeKey.equals("CUSTOM", ignoreCase = true)

@Composable
fun DocumentStyleCard(
    card: DocCard,
    modifier: Modifier = Modifier,
    showNumber: Boolean = false,
    compact: Boolean = false,
    onClick: (() -> Unit)? = null
) {
    val gradient = DocumentGradients.forType(card.typeKey)
    val haptics = LocalHapticFeedback.current
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.97f else 1f,
        animationSpec = spring(
            dampingRatio = 0.75f,
            stiffness = 350f
        ),
        label = "documentCardScale"
    )
    val shape = RoundedCornerShape(if (compact) 22.dp else 26.dp)
    val caption = if (isCustomCard(card.typeKey)) "ЛИЧНАЯ КАРТОЧКА" else "ДОКУМЕНТ РФ"
    val name = nameLineOf(card)

    Box(
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .shadow(elevation = if (compact) 12.dp else 20.dp, shape = shape, clip = false)
            .aspectRatio(ID_CARD_RATIO)
            .clip(shape)
            .background(Brush.linearGradient(gradient.colors))
            .clickable(
                interactionSource = interaction,
                indication = null
            ) {
                haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                onClick?.invoke()
            }
    ) {
        CardCanvas(
            seed = card.id.hashCode(),
            accent = gradient.accent,
            modifier = Modifier.fillMaxSize()
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .border(
                    width = 1.dp,
                    brush = DocumentGradients.borderBrush(gradient.accent),
                    shape = shape
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 22.dp, end = 18.dp, top = 16.dp, bottom = 16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = card.title.uppercase(),
                    style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.6.sp),
                    color = Color.White.copy(alpha = 0.7f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Spacer(Modifier.width(8.dp))
                TypeBadge(
                    icon = documentIconOf(card.typeKey),
                    accent = gradient.accent,
                    size = if (compact) 30.dp else 34.dp
                )
            }

            Spacer(Modifier.weight(1f))

            Row(verticalAlignment = Alignment.CenterVertically) {
                AvatarBadge(
                    initials = initialsOf(card),
                    accent = gradient.accent,
                    size = if (compact) 38.dp else 44.dp
                )
                Spacer(Modifier.width(12.dp))
                Text(
                    text = name.ifBlank { caption },
                    style = if (compact) {
                        MaterialTheme.typography.titleMedium
                    } else {
                        MaterialTheme.typography.titleLarge
                    },
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(Modifier.height(12.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(Color.White.copy(alpha = 0.16f))
            )

            Spacer(Modifier.height(10.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = numberLineOf(card, showNumber),
                    style = MonoSmall,
                    color = Color.White.copy(alpha = 0.95f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = caption,
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.5f),
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
private fun AvatarBadge(
    initials: String,
    accent: Color,
    size: Dp
) {
    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(Color.White.copy(alpha = 0.14f))
            .border(1.5.dp, accent.copy(alpha = 0.7f), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = initials,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = Color.White
        )
    }
}

@Composable
private fun CardCanvas(
    seed: Int,
    accent: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val stripeWidth = 6.dp.toPx()
        drawRect(
            color = accent.copy(alpha = 0.8f),
            topLeft = Offset.Zero,
            size = Size(stripeWidth, size.height)
        )

        val waves = 5
        val steps = 90
        for (index in 0 until waves) {
            val baseY = size.height * (0.18f + index * 0.16f)
            val amplitude = size.height * 0.03f
            val path = Path()
            path.moveTo(0f, baseY)
            for (step in 1..steps) {
                val x = size.width * step / steps
                val y = baseY + amplitude * sin(step * 0.3f + index * 0.9f)
                path.lineTo(x, y)
            }
            drawPath(
                path = path,
                color = Color.White.copy(alpha = 0.05f),
                style = Stroke(width = 0.8.dp.toPx())
            )
        }

        val sealCenter = Offset(size.width * 0.88f, size.height * 0.7f)
        drawCircle(
            color = accent.copy(alpha = 0.3f),
            radius = size.height * 0.18f,
            center = sealCenter,
            style = Stroke(width = 1.4.dp.toPx())
        )
        drawCircle(
            color = accent.copy(alpha = 0.16f),
            radius = size.height * 0.12f,
            center = sealCenter,
            style = Stroke(width = 1.dp.toPx())
        )

        val random = Random(seed)
        var barX = 24.dp.toPx()
        val barTop = size.height - 10.dp.toPx()
        val barHeight = 6.dp.toPx()
        while (barX < size.width * 0.34f) {
            val barWidth = (1 + random.nextInt(2)).dp.toPx()
            if (random.nextBoolean()) {
                drawRect(
                    color = Color.White.copy(alpha = 0.4f),
                    topLeft = Offset(barX, barTop),
                    size = Size(barWidth, barHeight)
                )
            }
            barX += barWidth + (1 + random.nextInt(2)).dp.toPx()
        }

        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color.White.copy(alpha = 0.12f), Color.Transparent),
                center = Offset(size.width * 0.8f, 0f),
                radius = size.minDimension * 0.7f
            ),
            radius = size.minDimension * 0.7f,
            center = Offset(size.width * 0.8f, 0f)
        )
    }
}