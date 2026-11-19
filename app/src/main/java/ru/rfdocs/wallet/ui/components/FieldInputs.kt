package ru.rfdocs.wallet.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import ru.rfdocs.wallet.data.model.DocField

fun fieldIsMasked(field: DocField): Boolean = !field.mask.isNullOrBlank()

fun fieldDisplayValue(field: DocField): String {
    val mask = field.mask
    if (mask.isNullOrBlank()) return field.value
    val digits = field.value.filter { it.isDigit() }
    if (digits.isEmpty()) return field.value

    val builder = StringBuilder()
    var index = 0
    for (symbol in mask) {
        if (index >= digits.length) break
        if (symbol == '0' || symbol == 'X' || symbol == '#') {
            builder.append(digits[index])
            index++
        } else {
            builder.append(symbol)
        }
    }
    return builder.toString()
}

fun fieldDigits(field: DocField): String = field.value.filter { it.isDigit() }

@Composable
fun DocFieldEditor(
    field: DocField,
    onFieldChange: (DocField) -> Unit,
    modifier: Modifier = Modifier
) {
    GlassTextField(
        label = field.label,
        value = field.value,
        onValueChange = { value -> onFieldChange(field.copy(value = value)) },
        modifier = modifier,
        placeholder = "—",
        singleLine = !field.label.lowercase().contains("адрес"),
        keyboardType = if (fieldIsMasked(field)) KeyboardType.Number else KeyboardType.Text
    )
}