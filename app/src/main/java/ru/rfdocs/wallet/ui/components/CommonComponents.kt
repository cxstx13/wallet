package ru.rfdocs.wallet.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import ru.rfdocs.wallet.data.model.DocCard
import ru.rfdocs.wallet.data.model.DocCategory
import ru.rfdocs.wallet.data.model.DocField
import ru.rfdocs.wallet.ui.input.DateMask
import ru.rfdocs.wallet.ui.input.FieldMasks
import ru.rfdocs.wallet.ui.input.Masks
import ru.rfdocs.wallet.ui.input.digitsOnly
import ru.rfdocs.wallet.ui.theme.MonoSmall
import ru.rfdocs.wallet.ui.theme.WalletTheme
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

private val ExpiryPattern: DateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")

fun copyText(context: Context, text: String, message: String = "Скопировано") {
    if (text.isBlank()) return
    val manager = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    manager.setPrimaryClip(ClipData.newPlainText("wallet", text))
    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
}

@Composable
fun GlassPanel(
    modifier: Modifier = Modifier,
    shape: RoundedCornerShape = RoundedCornerShape(20.dp),
    contentPadding: Dp = 18.dp,
    content: @Composable ColumnScope.() -> Unit
) {
    val extras = WalletTheme.extra
    Column(
        modifier = modifier
            .clip(shape)
            .background(extras.glassFill)
            .border(1.dp, extras.glassBorder, shape)
            .padding(contentPadding),
        content = content
    )
}

@Composable
fun SectionTitle(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text.uppercase(),
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.SemiBold,
        color = WalletTheme.extra.fieldLabel,
        modifier = modifier
    )
}

@Composable
fun WalletSearchField(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val extras = WalletTheme.extra
    val haptics = LocalHapticFeedback.current

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(extras.fieldFill)
            .border(1.dp, extras.fieldStroke, RoundedCornerShape(16.dp))
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Filled.Search,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(20.dp)
        )
        Spacer(Modifier.width(10.dp))
        Box(modifier = Modifier.weight(1f)) {
            if (query.isEmpty()) {
                Text(
                    text = "Поиск по документам",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }
            BasicTextField(
                value = query,
                onValueChange = onQueryChange,
                singleLine = true,
                textStyle = MaterialTheme.typography.bodyMedium.copy(
                    color = MaterialTheme.colorScheme.onSurface
                ),
                cursorBrush = SolidColor(WalletTheme.extra.monoAccent),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                modifier = Modifier.fillMaxWidth()
            )
        }
        if (query.isNotEmpty()) {
            IconButton(
                onClick = {
                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    onQueryChange("")
                },
                modifier = Modifier.size(26.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Clear,
                    contentDescription = "Очистить",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
fun CategoryChips(
    selected: DocCategory?,
    onSelect: (DocCategory?) -> Unit,
    modifier: Modifier = Modifier
) {
    val haptics = LocalHapticFeedback.current

    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        ChipItem(
            text = "Все",
            selectedState = selected == null,
            onClick = {
                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                onSelect(null)
            }
        )
        DocCategory.entries.forEach { category ->
            ChipItem(
                text = category.label,
                selectedState = selected == category,
                onClick = {
                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    onSelect(category)
                }
            )
        }
    }
}

@Composable
private fun ChipItem(
    text: String,
    selectedState: Boolean,
    onClick: () -> Unit
) {
    val extras = WalletTheme.extra
    val background = if (selectedState) extras.chipSelectedFill else extras.chipFill
    val foreground = if (selectedState) extras.chipSelectedText else MaterialTheme.colorScheme.onSurface

    Box(
        modifier = Modifier
            .clip(CircleShape)
            .background(background)
            .border(
                width = 1.dp,
                color = if (selectedState) extras.chipSelectedFill else extras.pillStroke,
                shape = CircleShape
            )
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 9.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            color = foreground,
            maxLines = 1
        )
    }
}

@Composable
fun FieldRow(
    field: DocField,
    revealed: Boolean,
    onToggleReveal: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val haptics = LocalHapticFeedback.current
    val extras = WalletTheme.extra

    val isMasked = field.secret || (!field.mask.isNullOrBlank() && field.mask != Masks.PHONE)
    val fullValue = if (field.mask.isNullOrBlank()) field.value else FieldMasks.display(field.value, field.mask)

    val display = when {
        field.value.isBlank() -> "—"
        isMasked && !revealed -> {
            val tail = digitsOnly(field.value).takeLast(4)
            if (tail.isEmpty()) "••••" else "•••• $tail"
        }

        else -> fullValue
    }

    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = field.label,
            style = MaterialTheme.typography.labelSmall,
            color = extras.fieldLabel
        )
        Spacer(Modifier.height(5.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = display,
                style = if (isMasked || !field.mask.isNullOrBlank()) MonoSmall else MaterialTheme.typography.bodyLarge,
                color = if (field.value.isBlank()) {
                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.55f)
                } else {
                    MaterialTheme.colorScheme.onSurface
                },
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            if (isMasked && field.value.isNotBlank()) {
                IconButton(
                    onClick = {
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        onToggleReveal()
                    },
                    modifier = Modifier.size(34.dp)
                ) {
                    Icon(
                        imageVector = if (revealed) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                        contentDescription = if (revealed) "Скрыть" else "Показать",
                        tint = extras.monoAccent,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
            if (field.value.isNotBlank()) {
                IconButton(
                    onClick = {
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        copyText(context, fullValue, "Скопировано: ${field.label}")
                    },
                    modifier = Modifier.size(34.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.ContentCopy,
                        contentDescription = "Копировать",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(17.dp)
                    )
                }
            }
        }
        Spacer(Modifier.height(12.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.45f))
        )
    }
}

@Composable
fun ActionPill(
    text: String,
    icon: ImageVector,
    onClick: () -> Unit,
    filled: Boolean = false,
    modifier: Modifier = Modifier
) {
    val extras = WalletTheme.extra
    val haptics = LocalHapticFeedback.current
    val background = if (filled) extras.monoAccent else extras.pillFill
    val foreground = if (filled) MaterialTheme.colorScheme.background else extras.pillText

    Row(
        modifier = modifier
            .clip(CircleShape)
            .background(background)
            .border(1.dp, extras.pillStroke, CircleShape)
            .clickable {
                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                onClick()
            }
            .padding(horizontal = 16.dp, vertical = 11.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = foreground,
            modifier = Modifier.size(18.dp)
        )
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            color = foreground,
            maxLines = 1
        )
    }
}

fun expiryDateOf(card: DocCard): LocalDate? {
    val keywords = listOf("до", "по", "окончания", "срок")
    val field = card.fields.firstOrNull { candidate ->
        val label = candidate.label.lowercase()
        candidate.mask == Masks.DATE && keywords.any { label.contains(it) }
    } ?: return null

    val iso = DateMask.toIso(field.value)
    if (iso.isBlank()) return null
    return runCatching { LocalDate.parse(iso) }.getOrNull()
}

@Composable
fun ExpiryBanner(card: DocCard, modifier: Modifier = Modifier) {
    val date = expiryDateOf(card) ?: return
    val days = ChronoUnit.DAYS.between(LocalDate.now(), date)

    val color = when {
        days < 0 -> ru.rfdocs.wallet.ui.theme.Palette.Danger
        days <= 30 -> ru.rfdocs.wallet.ui.theme.Palette.Warning
        else -> ru.rfdocs.wallet.ui.theme.Palette.Success
    }

    val text = when {
        days < 0 -> "Срок истёк ${date.format(ExpiryPattern)}"
        days == 0L -> "Истекает сегодня"
        days <= 30 -> "Осталось $days дн."
        else -> "Действует до ${date.format(ExpiryPattern)}"
    }

    Row(
        modifier = modifier
            .clip(CircleShape)
            .background(color.copy(alpha = 0.14f))
            .border(1.dp, color.copy(alpha = 0.42f), CircleShape)
            .padding(horizontal = 14.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(Modifier.width(10.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            color = color
        )
    }
}