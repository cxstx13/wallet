package ru.rfdocs.wallet.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import ru.rfdocs.wallet.data.model.DocCard
import ru.rfdocs.wallet.ui.input.digitsOnly
import ru.rfdocs.wallet.ui.theme.DocumentGradients
import ru.rfdocs.wallet.ui.theme.Palette
import ru.rfdocs.wallet.ui.theme.WalletTheme
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

enum class DocStatus(val label: String) {
    VALID("Действителен"),
    SOON("Скоро истекает"),
    EXPIRED("Истёк"),
    NONE("")
}

private val rowDateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")

fun docStatusOf(card: DocCard): DocStatus {
    val date = expiryDateOf(card) ?: return DocStatus.NONE
    val days = ChronoUnit.DAYS.between(LocalDate.now(), date)
    return when {
        days < 0L -> DocStatus.EXPIRED
        days <= 30L -> DocStatus.SOON
        else -> DocStatus.VALID
    }
}

fun docStatusColor(status: DocStatus): Color = when (status) {
    DocStatus.VALID -> Palette.Success
    DocStatus.SOON -> Palette.Warning
    DocStatus.EXPIRED -> Palette.Danger
    DocStatus.NONE -> Palette.Lavender
}

fun docSubtitle(card: DocCard): String {
    val date = expiryDateOf(card)
    if (date != null) {
        val formatted = rowDateFormatter.format(date)
        return when (docStatusOf(card)) {
            DocStatus.EXPIRED -> "Истёк $formatted"
            DocStatus.SOON -> "Истекает $formatted"
            else -> "Действителен до $formatted"
        }
    }
    val masked = card.fields.firstOrNull { !it.mask.isNullOrBlank() && it.value.isNotBlank() }
    if (masked != null) {
        val tail = digitsOnly(masked.value).takeLast(4)
        if (tail.isNotEmpty()) return "•••• $tail"
    }
    return "Без срока действия"
}

@Composable
fun DocCardView(
    card: DocCard,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val extras = WalletTheme.extra
    val gradient = DocumentGradients.forType(card.typeKey)
    val haptics = LocalHapticFeedback.current
    val status = docStatusOf(card)
    val shape = RoundedCornerShape(20.dp)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(extras.glassFill)
            .border(1.dp, extras.glassBorder, shape)
            .clickable {
                haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                onClick()
            }
            .padding(horizontal = 14.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(Brush.linearGradient(gradient.colors))
                .border(1.dp, Color.White.copy(alpha = 0.14f), RoundedCornerShape(14.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = documentIconOf(card.typeKey),
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(22.dp)
            )
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = card.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(3.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (status != DocStatus.NONE) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(docStatusColor(status))
                    )
                    Spacer(Modifier.width(6.dp))
                }
                Text(
                    text = docSubtitle(card),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
fun DocStatusChip(
    status: DocStatus,
    modifier: Modifier = Modifier
) {
    if (status == DocStatus.NONE) return
    val color = docStatusColor(status)
    val shape = RoundedCornerShape(50)

    Row(
        modifier = modifier
            .clip(shape)
            .background(color.copy(alpha = 0.14f))
            .border(1.dp, color.copy(alpha = 0.35f), shape)
            .padding(horizontal = 10.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = when (status) {
                DocStatus.VALID -> Icons.Filled.CheckCircle
                DocStatus.SOON -> Icons.Filled.Schedule
                DocStatus.EXPIRED -> Icons.Filled.Cancel
                DocStatus.NONE -> Icons.Filled.CheckCircle
            },
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(14.dp)
        )
        Spacer(Modifier.width(6.dp))
        Text(
            text = status.label,
            style = MaterialTheme.typography.labelMedium,
            color = color
        )
    }
}