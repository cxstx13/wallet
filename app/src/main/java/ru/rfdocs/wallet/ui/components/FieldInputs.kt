package ru.rfdocs.wallet.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import ru.rfdocs.wallet.data.model.DocField
import ru.rfdocs.wallet.data.model.FieldKind
import ru.rfdocs.wallet.ui.input.DateMask
import ru.rfdocs.wallet.ui.input.MaskVisualTransformation
import ru.rfdocs.wallet.ui.input.PhoneMask
import ru.rfdocs.wallet.ui.input.digitsOnly
import ru.rfdocs.wallet.ui.input.formatWithMask
import ru.rfdocs.wallet.ui.input.maxDigitsFor
import ru.rfdocs.wallet.ui.theme.MonoStyle
import ru.rfdocs.wallet.ui.theme.Palette
import ru.rfdocs.wallet.ui.theme.WalletTheme
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.util.Locale

fun fieldDisplayValue(field: DocField): String = when (field.kind) {
    FieldKind.DATE -> DateMask.display(field.value)
    FieldKind.PHONE -> PhoneMask.display(field.value)
    else -> if (field.pattern.isNotBlank()) formatWithMask(field.value, field.pattern) else field.value
}

@Composable
fun FieldLabel(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelMedium,
        color = WalletTheme.extra.fieldLabel,
        modifier = modifier
    )
}

@Composable
fun WalletField(
    label: String,
    modifier: Modifier = Modifier,
    trailing: (@Composable () -> Unit)? = null,
    content: @Composable () -> Unit
) {
    val extras = WalletTheme.extra
    Column(modifier = modifier.fillMaxWidth()) {
        FieldLabel(text = label)
        Spacer(modifier = Modifier.height(6.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(extras.fieldFill)
                .border(1.dp, extras.fieldStroke, RoundedCornerShape(14.dp))
                .padding(start = 14.dp, end = if (trailing != null) 4.dp else 14.dp, top = 12.dp, bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(modifier = Modifier.weight(1f)) {
                content()
            }
            if (trailing != null) {
                trailing()
            }
        }
    }
}

@Composable
private fun SoftTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    mono: Boolean,
    multiline: Boolean,
    keyboardOptions: KeyboardOptions,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    modifier: Modifier = Modifier
) {
    val baseStyle = if (mono) MonoStyle else MaterialTheme.typography.bodyLarge
    val textStyle = baseStyle.copy(color = MaterialTheme.colorScheme.onSurface)
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = if (multiline) 78.dp else 24.dp),
        textStyle = textStyle,
        singleLine = !multiline,
        minLines = if (multiline) 3 else 1,
        keyboardOptions = keyboardOptions,
        cursorBrush = SolidColor(Palette.Gold),
        visualTransformation = visualTransformation,
        decorationBox = { innerTextField ->
            Box {
                if (value.isEmpty() && placeholder.isNotEmpty()) {
                    Text(
                        text = placeholder,
                        style = textStyle,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.55f)
                    )
                }
                innerTextField()
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CalendarButton(
    currentDigits: String,
    onPick: (String) -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }

    IconButton(onClick = { showDialog = true }, modifier = Modifier.size(36.dp)) {
        Icon(
            imageVector = Icons.Rounded.CalendarMonth,
            contentDescription = "Выбрать дату",
            tint = Palette.Gold
        )
    }

    if (showDialog) {
        val iso = DateMask.toIso(currentDigits)
        val initialMillis = remember(iso) {
            runCatching {
                LocalDate.parse(iso)
                    .atStartOfDay(ZoneOffset.UTC)
                    .toInstant()
                    .toEpochMilli()
            }.getOrNull()
        }
        val state = rememberDatePickerState(initialSelectedDateMillis = initialMillis)
        DatePickerDialog(
            onDismissRequest = { showDialog = false },
            confirmButton = {
                TextButton(onClick = {
                    val millis = state.selectedDateMillis
                    if (millis != null) {
                        val date = Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate()
                        onPick(
                            String.format(
                                Locale.US,
                                "%02d%02d%04d",
                                date.dayOfMonth,
                                date.monthValue,
                                date.year
                            )
                        )
                    }
                    showDialog = false
                }) {
                    Text("Готово", color = Palette.Gold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) { Text("Отмена") }
            }
        ) {
            DatePicker(state = state)
        }
    }
}

@Composable
fun GenderSelector(
    selected: String,
    options: List<String>,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val extras = WalletTheme.extra
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        options.forEach { option ->
            val isSelected = selected.equals(option, ignoreCase = true)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(46.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(if (isSelected) extras.chipSelectedFill else extras.fieldFill)
                    .border(
                        width = 1.dp,
                        color = if (isSelected) extras.chipSelectedFill else extras.fieldStroke,
                        shape = RoundedCornerShape(14.dp)
                    )
                    .clickable { onSelect(option) },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = option,
                    style = MaterialTheme.typography.labelLarge,
                    color = if (isSelected) extras.chipSelectedText else MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ChipsSelector(
    options: List<String>,
    selected: List<String>,
    onToggle: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val extras = WalletTheme.extra
    FlowRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        options.forEach { option ->
            val isSelected = selected.contains(option)
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(if (isSelected) extras.chipSelectedFill else extras.fieldFill)
                    .border(
                        width = 1.dp,
                        color = if (isSelected) extras.chipSelectedFill else extras.fieldStroke,
                        shape = CircleShape
                    )
                    .clickable { onToggle(option) }
                    .padding(horizontal = 16.dp, vertical = 9.dp)
            ) {
                Text(
                    text = option,
                    style = MaterialTheme.typography.labelLarge,
                    color = if (isSelected) extras.chipSelectedText else MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
fun DocFieldEditor(
    field: DocField,
    onFieldChange: (DocField) -> Unit,
    modifier: Modifier = Modifier
) {
    when (field.kind) {
        FieldKind.GENDER -> {
            Column(modifier = modifier.fillMaxWidth()) {
                FieldLabel(text = field.label)
                Spacer(modifier = Modifier.height(6.dp))
                GenderSelector(
                    selected = field.value,
                    options = field.options.ifEmpty { listOf("Мужской", "Женский") },
                    onSelect = { value -> onFieldChange(field.copy(value = value)) }
                )
            }
        }

        FieldKind.CHIPS -> {
            Column(modifier = modifier.fillMaxWidth()) {
                FieldLabel(text = field.label)
                Spacer(modifier = Modifier.height(8.dp))
                ChipsSelector(
                    options = field.options,
                    selected = field.selectedChips(),
                    onToggle = { option ->
                        val current = field.selectedChips().toMutableList()
                        if (current.contains(option)) {
                            current.remove(option)
                        } else {
                            current.add(option)
                        }
                        onFieldChange(field.copy(value = DocField.chipsValue(current)))
                    }
                )
            }
        }

        FieldKind.DATE -> {
            WalletField(
                label = field.label,
                modifier = modifier,
                trailing = {
                    CalendarButton(
                        currentDigits = field.value,
                        onPick = { digits -> onFieldChange(field.copy(value = digits)) }
                    )
                }
            ) {
                SoftTextField(
                    value = DateMask.sanitize(field.value),
                    onValueChange = { raw -> onFieldChange(field.copy(value = DateMask.sanitize(raw))) },
                    placeholder = field.placeholder.ifBlank { "дд.мм.гггг" },
                    mono = true,
                    multiline = false,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    visualTransformation = DateMask.visual
                )
            }
        }

        FieldKind.PHONE -> {
            WalletField(label = field.label, modifier = modifier) {
                SoftTextField(
                    value = PhoneMask.sanitize(field.value),
                    onValueChange = { raw -> onFieldChange(field.copy(value = PhoneMask.sanitize(raw))) },
                    placeholder = field.placeholder.ifBlank { "+7 (___) ___-__-__" },
                    mono = true,
                    multiline = false,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    visualTransformation = PhoneMask.visual
                )
            }
        }

        FieldKind.MASKED_NUMBER -> {
            val transformation = remember(field.pattern) {
                if (field.pattern.isBlank()) {
                    VisualTransformation.None
                } else {
                    MaskVisualTransformation(field.pattern)
                }
            }
            val maxDigits = remember(field.pattern) {
                if (field.pattern.isBlank()) Int.MAX_VALUE else maxDigitsFor(field.pattern)
            }
            WalletField(label = field.label, modifier = modifier) {
                SoftTextField(
                    value = digitsOnly(field.value, maxDigits),
                    onValueChange = { raw -> onFieldChange(field.copy(value = digitsOnly(raw, maxDigits))) },
                    placeholder = field.placeholder,
                    mono = true,
                    multiline = false,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    visualTransformation = transformation
                )
            }
        }

        FieldKind.MULTILINE, FieldKind.NOTE -> {
            WalletField(label = field.label, modifier = modifier) {
                SoftTextField(
                    value = field.value,
                    onValueChange = { raw -> onFieldChange(field.copy(value = raw)) },
                    placeholder = field.placeholder,
                    mono = false,
                    multiline = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        capitalization = KeyboardCapitalization.Sentences
                    )
                )
            }
        }

        FieldKind.TEXT -> {
            WalletField(label = field.label, modifier = modifier) {
                SoftTextField(
                    value = field.value,
                    onValueChange = { raw -> onFieldChange(field.copy(value = raw)) },
                    placeholder = field.placeholder,
                    mono = field.mono,
                    multiline = false,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        capitalization = if (field.capitalizeWords) {
                            KeyboardCapitalization.Words
                        } else {
                            KeyboardCapitalization.None
                        }
                    )
                )
            }
        }
    }
}