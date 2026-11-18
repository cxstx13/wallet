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
import ru.rfdocs.wallet.data.model.DocCard
import ru.rfdocs.wallet.data.model.DocField
import ru.rfdocs.wallet.ui.input.FieldMasks
import ru.rfdocs.wallet.ui.input.digitsOnly
import ru.rfdocs.wallet.ui.theme.DocumentGradients
import ru.rfdocs.wallet.ui.theme.MonoLarge
import ru.rfdocs.wallet.ui.theme.MonoSmall
import kotlin.math.sin
import kotlin.random.Random

const val ID_CARD_RATIO = 1.586f

fun documentIconOf(typeKey: String): ImageVector {
    val key = typeKey.lowercase()
    return when {
        key.contains("passport") || key.contains("zagran") || key.contains("foreign") ->
            Icons.Rounded.Badge

        key.contains("snils") || key.contains("inn") ->
            Icons.Rounded.AssignmentInd

        key.contains("driver") || key.contains("vu") ||
            key.contains("sts") || key.contains("pts") ->
            Icons.Rounded.DirectionsCar

        key.contains("osago") || key.contains("kasko") ->
            Icons.Rounded.VerifiedUser

        key.contains("oms") || key.contains("dms") ->
            Icons.Rounded.MedicalServices

        key.contains("birth") || key.contains("family") ->
            Icons.Rounded.ChildCare

        key.contains("bank") ->
            Icons.Rounded.AccountBalance

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
        targetValue = if (pressed) 0.972f else 1f,
        animationSpec = spring(
            dampingRatio = 0.75f,
            stiffness = 350f
        ),
        label = "documentCardScale"
    )
    val shape = RoundedCornerShape(if (compact) 20.dp else 24.dp)
    val caption = if (isCustomCard(card.typeKey)) "ЛИЧНАЯ КАРТОЧКА" else "ДОКУМЕНТ РФ"

    Box(
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .shadow(elevation = if (compact) 10.dp else 18.dp, shape = shape, clip = false)
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
        DocumentCardCanvas(
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

        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 22.dp, end = 16.dp, top = 16.dp, bottom = 14.dp)
        ) {
            AvatarBadge(
                initials = initialsOf(card),
                accent = gradient.accent,
                size = if (compact) 44.dp else 52.dp
            )

            Spacer(Modifier.width(14.dp))

            Column(modifier = Modifier.fillMaxSize()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = card.title,
                            style = if (compact) {
                                MaterialTheme.typography.titleSmall
                            } else {
                                MaterialTheme.typography.titleMedium
                            },
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = nameLineOf(card).ifBlank { caption },
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.88f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Spacer(Modifier.width(8.dp))
                    TypeBadge(
                        icon = documentIconOf(card.typeKey),
                        accent = gradient.accent,
                        size = if (compact) 30.dp else 34.dp
                    )
                }

                Spacer(Modifier.weight(1f))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(Color.White.copy(alpha = 0.18f))
                )

                Spacer(Modifier.height(8.dp))

                Text(
                    text = numberLineOf(card, showNumber),
                    style = if (compact) MonoSmall else MonoLarge,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(Modifier.height(2.dp))

                Text(
                    text = caption,
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.62f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
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
            .border(1.5.dp, accent.copy(alpha = 0.75f), CircleShape),
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
private fun DocumentCardCanvas(
    seed: Int,
    accent: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val stripeWidth = 8.dp.toPx()
        drawRect(
            color = accent.copy(alpha = 0.85f),
            topLeft = Offset.Zero,
            size = Size(stripeWidth, size.height)
        )

        val waves = 6
        val steps = 90
        val strokeWidth = 0.8.dp.toPx()
        for (index in 0 until waves) {
            val baseY = size.height * (0.16f + index * 0.14f)
            val amplitude = size.height * 0.035f
            val path = Path()
            path.moveTo(0f, baseY)
            for (step in 1..steps) {
                val x = size.width * step / steps
                val y = baseY + amplitude * sin(step * 0.32f + index * 0.9f)
                path.lineTo(x, y)
            }
            drawPath(
                path = path,
                color = Color.White.copy(alpha = 0.07f),
                style = Stroke(width = strokeWidth)
            )
        }

        val sealCenter = Offset(size.width * 0.9f, size.height * 0.74f)
        drawCircle(
            color = accent.copy(alpha = 0.38f),
            radius = size.height * 0.2f,
            center = sealCenter,
            style = Stroke(width = 1.6.dp.toPx())
        )
        drawCircle(
            color = accent.copy(alpha = 0.22f),
            radius = size.height * 0.15f,
            center = sealCenter,
            style = Stroke(width = 1.dp.toPx())
        )
        drawCircle(
            color = accent.copy(alpha = 0.12f),
            radius = size.height * 0.09f,
            center = sealCenter,
            style = Stroke(width = 1.dp.toPx())
        )

        val random = Random(seed)
        var barX = 30.dp.toPx()
        val barTop = size.height - 12.dp.toPx()
        val barHeight = 7.dp.toPx()
        val barLimit = size.width * 0.42f
        while (barX < barLimit) {
            val barWidth = (1 + random.nextInt(3)).dp.toPx()
            if (random.nextBoolean()) {
                drawRect(
                    color = Color.White.copy(alpha = 0.55f),
                    topLeft = Offset(barX, barTop),
                    size = Size(barWidth, barHeight)
                )
            }
            barX += barWidth + (1 + random.nextInt(2)).dp.toPx()
        }

        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(accent.copy(alpha = 0.22f), Color.Transparent),
                center = Offset(size.width * 0.85f, size.height * 0.1f),
                radius = size.minDimension * 0.6f
            ),
            radius = size.minDimension * 0.6f,
            center = Offset(size.width * 0.85f, size.height * 0.1f)
        )
    }
}