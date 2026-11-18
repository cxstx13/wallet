package ru.rfdocs.wallet.ui.input

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation

object Masks {
    const val DATE = "00.00.0000"
    const val PASSPORT = "0000 000000"
    const val DEPARTMENT = "000-000"
    const val SNILS = "000-000-000 00"
    const val VEHICLE = "00 00 000000"
    const val PHONE = "+0 (000) 000-00-00"
    const val INN = "000000000000"
    const val OMS = "0000000000000000"
    const val ACCOUNT = "00000000000000000000"
}

fun digitsOnly(value: String): String = value.filter { it.isDigit() }

fun digitCount(mask: String): Int = mask.count { it == '0' }

fun formatWithMask(digits: String, mask: String): String {
    val clean = digitsOnly(digits)
    val builder = StringBuilder()
    var index = 0
    for (ch in mask) {
        if (index >= clean.length) break
        if (ch == '0') {
            builder.append(clean[index])
            index++
        } else {
            builder.append(ch)
        }
    }
    return builder.toString()
}

fun sanitizeForMask(value: String, mask: String?): String {
    val clean = digitsOnly(value)
    if (mask.isNullOrBlank()) return clean
    return clean.take(digitCount(mask))
}

fun maxDigitsFor(mask: String?): Int? = mask?.let { digitCount(it) }

fun keyboardTypeFor(mask: String?): KeyboardType =
    if (mask.isNullOrBlank()) KeyboardType.Text else KeyboardType.Number

private class MaskOffsetMapping(
    private val original: String,
    private val mask: String,
    private val digitTotal: Int,
    private val transformedLength: Int
) : OffsetMapping {

    override fun originalToTransformed(offset: Int): Int {
        val clamped = offset.coerceIn(0, original.length)
        val digits = original.take(clamped).count { it.isDigit() }
        if (digits <= 0) return 0
        if (digits >= digitTotal) return transformedLength

        var placed = 0
        var position = 0
        for (ch in mask) {
            if (placed >= digitTotal) break
            if (ch == '0') {
                placed++
                position++
                if (placed == digits) return position
            } else {
                position++
            }
        }
        return position
    }

    override fun transformedToOriginal(offset: Int): Int {
        val clamped = offset.coerceIn(0, transformedLength)
        var digits = 0
        var position = 0
        for (ch in mask) {
            if (position >= clamped) break
            if (ch == '0') {
                digits++
            }
            position++
        }
        return digits.coerceAtMost(digitTotal)
    }
}

class MaskedVisualTransformation(
    private val mask: String,
    private val maxDigits: Int = digitCount(mask)
) : VisualTransformation {

    override fun filter(text: AnnotatedString): TransformedText {
        val raw = digitsOnly(text.text).take(maxDigits)
        val formatted = formatWithMask(raw, mask)
        val mapping = MaskOffsetMapping(
            original = text.text,
            mask = mask,
            digitTotal = raw.length,
            transformedLength = formatted.length
        )
        return TransformedText(AnnotatedString(formatted), mapping)
    }
}

object DateMask {
    const val PATTERN = "00.00.0000"
    const val DIGITS = 8

    fun toIso(digits: String): String {
        val clean = digitsOnly(digits).take(DIGITS)
        if (clean.length < DIGITS) return ""
        return "${clean.substring(4, 8)}-${clean.substring(2, 4)}-${clean.substring(0, 2)}"
    }

    fun fromIso(iso: String): String {
        val parts = iso.split("-")
        if (parts.size != 3) return ""
        if (parts[0].length != 4 || parts[1].length != 2 || parts[2].length != 2) return ""
        return parts[2] + parts[1] + parts[0]
    }

    fun display(digits: String): String = formatWithMask(digits, PATTERN)

    fun transformation(): VisualTransformation = MaskedVisualTransformation(PATTERN, DIGITS)
}

object PhoneMask {
    const val PATTERN = "+0 (000) 000-00-00"
    const val DIGITS = 11

    fun display(digits: String): String = formatWithMask(digits, PATTERN)

    fun transformation(): VisualTransformation = MaskedVisualTransformation(PATTERN, DIGITS)

    fun toDialable(digits: String): String {
        val clean = digitsOnly(digits).take(DIGITS)
        if (clean.isEmpty()) return ""
        val normalized = if (clean.length == DIGITS && clean.startsWith("8")) {
            "7" + clean.substring(1)
        } else {
            clean
        }
        return "+$normalized"
    }
}

object FieldMasks {

    fun transformation(mask: String?): VisualTransformation? {
        if (mask.isNullOrBlank()) return null
        return MaskedVisualTransformation(mask, digitCount(mask))
    }

    fun apply(value: String, mask: String?): String = sanitizeForMask(value, mask)

    fun display(value: String, mask: String?): String {
        if (mask.isNullOrBlank()) return value
        return formatWithMask(value, mask)
    }

    fun isNumeric(mask: String?): Boolean = !mask.isNullOrBlank()
}