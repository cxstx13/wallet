package ru.rfdocs.wallet.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import ru.rfdocs.wallet.data.model.DocField
import ru.rfdocs.wallet.ui.input.DateMask
import ru.rfdocs.wallet.ui.input.FieldMasks
import ru.rfdocs.wallet.ui.input.Masks
import ru.rfdocs.wallet.ui.input.MaskedVisualTransformation
import ru.rfdocs.wallet.ui.input.PhoneMask
import ru.rfdocs.wallet.ui.input.digitCount
import ru.rfdocs.wallet.ui.input.digitsOnly
import ru.rfdocs.wallet.ui.input.formatWithMask
import ru.rfdocs.wallet.ui.theme.MonoStyle
import ru.rfdocs.wallet.ui.theme.Palette
import ru.rfdocs.wallet.ui.theme.WalletTheme
import java.time.LocalDate
import java.util.Locale

fun fieldDisplayValue(field: DocField): String {
    val mask = field.mask
    return if (mask.isNullOrBlank()) field.value else FieldMasks.display(field.value, mask)
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
        Spacer(Modifier.height(6.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(extras.fieldFill)
                .border(1.dp, extras.fieldStroke, RoundedCornerShape(14.dp))
                .padding(
                    start = 14.dp,
                    end = if (trailing != null) 4.dp else 14.dp,
                    top = 12.dp,
                    bottom = 12.dp
                ),
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

@Composable
private fun DatePickerButton(
    currentDigits: String,
    onPick: (String) -> Unit
) {
    val context = LocalContext.current

    IconButton(
        onClick = {
            val iso = DateMask.toIso(currentDigits)
            val base = if (iso.isNotBlank()) {
                runCatching { LocalDate.parse(iso) }.getOrNull() ?: LocalDate.now()
            } else {
                LocalDate.now()
            }
            android.app.DatePickerDialog(
                context,
                { _, year, month, dayOfMonth ->
                    onPick(String.format(Locale.US, "%02d%02d%04d", dayOfMonth, month + 1, year))
                },
                base.year,
                base.monthValue - 1,
                base.dayOfMonth
            ).show()
        },
        modifier = Modifier.size(36.dp)
    ) {
        Icon(
            imageVector = Icons.Filled.DateRange,
            contentDescription = "Выбрать дату",
            tint = Palette.Gold
        )
    }
}

@Composable
fun DocFieldEditor(
    field: DocField,
    onFieldChange: (DocField) -> Unit,
    modifier: Modifier = Modifier
) {
    val mask = field.mask
    val isDate = mask == Masks.DATE
    val isPhone = field.phone || mask == Masks.PHONE
    val isMasked = !mask.isNullOrBlank() && !isDate && !isPhone
    val maxDigits = remember(mask) { mask?.let { digitCount(it) } ?: Int.MAX_VALUE }

    val transformation: VisualTransformation = remember(mask) {
        when {
            isDate -> DateMask.transformation()
            isPhone -> PhoneMask.transformation()
            !mask.isNullOrBlank() -> MaskedVisualTransformation(mask, digitCount(mask))
            else -> VisualTransformation.None
        }
    }

    val keyboardType = when {
        isDate || isPhone || isMasked -> KeyboardType.Number
        else -> KeyboardType.Text
    }

    WalletField(
        label = field.label,
        modifier = modifier,
        trailing = if (isDate) {
            { DatePickerButton(currentDigits = field.value) { digits -> onFieldChange(field.copy(value = digits)) } }
        } else {
            null
        }
    ) {
        SoftTextField(
            value = digitsOnly(field.value).take(maxDigits),
            onValueChange = { raw ->
                onFieldChange(field.copy(value = digitsOnly(raw).take(maxDigits)))
            },
            placeholder = when {
                isDate -> "дд.мм.гггг"
                isPhone -> "+7 (___) ___-__-__"
                isMasked -> formatWithMask("", mask.orEmpty())
                else -> field.label
            },
            mono = isMasked || isDate || isPhone,
            multiline = false,
            keyboardOptions = KeyboardOptions(
                keyboardType = keyboardType,
                capitalization = KeyboardCapitalization.None
            ),
            visualTransformation = transformation
        )
    }
}