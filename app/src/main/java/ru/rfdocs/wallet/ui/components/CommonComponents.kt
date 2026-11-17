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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import ru.rfdocs.wallet.data.model.DocCard
import ru.rfdocs.wallet.data.model.DocCategory
import ru.rfdocs.wallet.data.model.DocField
import ru.rfdocs.wallet.ui.theme.MonoStyle
import ru.rfdocs.wallet.ui.theme.Palette
import ru.rfdocs.wallet.ui.theme.WalletTheme

fun copyText(context: Context, text: String, message: String = "Скопировано") {
    val manager = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    manager.setPrimaryClip(ClipData.newPlainText("wallet", text))
    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
}

@Composable
fun GlassPanel(
    modifier: Modifier = Modifier,
    shape: RoundedCornerShape = RoundedCornerShape(22.dp),
    contentPadding: androidx.compose.ui.unit.Dp = 16.dp,
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
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        fontWeight = FontWeight.SemiBold,
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
    TextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .border(1.dp, extras.glassBorder, RoundedCornerShape(18.dp)),
        placeholder = {
            Text(
                text = "Поиск по документам",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        leadingIcon = {
            Icon(
                imageVector = Icons.Rounded.Search,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(
                        imageVector = Icons.Rounded.Close,
                        contentDescription = "Очистить",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        singleLine = true,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(onSearch = {}),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = extras.glassFill,
            unfocusedContainerColor = extras.glassFill,
            disabledContainerColor = extras.glassFill,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            disabledIndicatorColor = Color.Transparent,
            cursorColor = MaterialTheme.colorScheme.primary
        ),
        textStyle = MaterialTheme.typography.bodyMedium
    )
}

@Composable
fun CategoryChips(
    selected: DocCategory?,
    onSelect: (DocCategory?) -> Unit,
    modifier: Modifier = Modifier
) {
    val extras = WalletTheme.extra
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        ChipItem(
            text = "Все",
            selectedState = selected == null,
            onClick = { onSelect(null) }
        )
        DocCategory.entries.forEach { category ->
            ChipItem(
                text = category.label,
                selectedState = selected == category,
                onClick = { onSelect(category) }
            )
        }
    }
}

@Composable
private fun ChipItem(text: String, selectedState: Boolean, onClick: () -> Unit) {
    val extras = WalletTheme.extra
    val bg = if (selectedState) extras.chipSelectedFill else extras.chipFill
    val fg = if (selectedState) extras.chipSelectedText else MaterialTheme.colorScheme.onSurface
    Box(
        modifier = Modifier
            .clip(CircleShape)
            .background(bg)
            .border(1.dp, extras.glassBorder, CircleShape)
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 9.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            color = fg,
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
    val extras = WalletTheme.extra
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = field.label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(4.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            val display = when {
                field.value.isBlank() -> field.placeholder.ifBlank { "—" }
                field.masked && !revealed -> field.maskedPreview()
                field.mono || field.masked -> field.value
                else -> field.value
            }
            Text(
                text = display,
                style = if (field.mono || field.masked) MonoStyle else MaterialTheme.typography.bodyLarge,
                color = if (field.value.isBlank()) {
                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                } else {
                    MaterialTheme.colorScheme.onSurface
                },
                maxLines = if (field.multiline) 12 else 3,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            if (field.masked && field.value.isNotBlank()) {
                IconButton(onClick = onToggleReveal, modifier = Modifier.size(36.dp)) {
                    Icon(
                        imageVector = if (revealed) Icons.Rounded.VisibilityOff else Icons.Rounded.Visibility,
                        contentDescription = if (revealed) "Скрыть" else "Показать",
                        tint = extras.monoAccent
                    )
                }
            }
            if (field.value.isNotBlank()) {
                IconButton(
                    onClick = { copyText(context, field.value, "Скопировано: ${field.label}") },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.ContentCopy,
                        contentDescription = "Копировать",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(10.dp))
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
    val bg = if (filled) extras.chipSelectedFill else extras.chipFill
    val fg = if (filled) extras.chipSelectedText else MaterialTheme.colorScheme.onSurface
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(bg)
            .border(1.dp, extras.glassBorder, RoundedCornerShape(14.dp))
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = fg, modifier = Modifier.size(18.dp))
        Text(text = text, style = MaterialTheme.typography.labelLarge, color = fg, maxLines = 1)
    }
}

@Composable
fun ExpiryBanner(card: DocCard, modifier: Modifier = Modifier) {
    if (card.validUntil.isBlank()) return
    val level = card.expiryLevel()
    val color = when (level) {
        2 -> Palette.Danger
        1 -> Palette.Amber
        else -> Palette.Success
    }
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(color.copy(alpha = 0.14f))
            .border(1.dp, color.copy(alpha = 0.4f), RoundedCornerShape(14.dp))
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.size(10.dp))
        Text(
            text = card.expiryLabel(),
            style = MaterialTheme.typography.labelLarge,
            color = color
        )
    }
}
