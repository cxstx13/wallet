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
    for (symbol in mask) {
        if (index >= clean.length) break
        if (symbol == '0') {
            builder.append(clean[index])
            index++
        } else {
            builder.append(symbol)
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

private class PreciseOffsetMapping(
    private val digitPositions: IntArray,
    private val transformedLength: Int,
    private val digitTotal: Int
) : OffsetMapping {

    override fun originalToTransformed(offset: Int): Int {
        if (digitTotal == 0) return 0
        val digits = offset.coerceIn(0, digitTotal)
        if (digits == 0) return 0
        if (digits >= digitTotal) return transformedLength
        return digitPositions[digits - 1] + 1
    }

    override fun transformedToOriginal(offset: Int): Int {
        val clamped = offset.coerceIn(0, transformedLength)
        var count = 0
        for (position in digitPositions) {
            if (position < clamped) count++ else break
        }
        return count.coerceIn(0, digitTotal)
    }
}

class MaskedVisualTransformation(
    private val mask: String,
    private val maxDigits: Int = digitCount(mask)
) : VisualTransformation {

    override fun filter(text: AnnotatedString): TransformedText {
        val raw = digitsOnly(text.text).take(maxDigits)
        val builder = StringBuilder()
        val positions = IntArray(raw.length)
        var digitIndex = 0

        for (symbol in mask) {
            if (digitIndex >= raw.length) break
            if (symbol == '0') {
                positions[digitIndex] = builder.length
                builder.append(raw[digitIndex])
                digitIndex++
            } else {
                builder.append(symbol)
            }
        }

        val formatted = builder.toString()
        val mapping = PreciseOffsetMapping(
            digitPositions = positions,
            transformedLength = formatted.length,
            digitTotal = raw.length
        )
        return TransformedText(AnnotatedString(formatted), mapping)
    }
}

typealias MaskVisualTransformation = MaskedVisualTransformation

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

    fun error(digits: String): String? {
        val clean = digitsOnly(digits).take(DIGITS)
        if (clean.length < DIGITS) return null
        val day = clean.substring(0, 2).toIntOrNull() ?: return "Некорректная дата"
        val month = clean.substring(2, 4).toIntOrNull() ?: return "Некорректная дата"
        val year = clean.substring(4, 8).toIntOrNull() ?: return "Некорректная дата"
        if (day !in 1..31) return "День должен быть от 01 до 31"
        if (month !in 1..12) return "Месяц должен быть от 01 до 12"
        if (year !in 1900..2100) return "Год должен быть от 1900 до 2100"
        return null
    }
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

    fun error(value: String, mask: String?): String? {
        if (mask == Masks.DATE) return DateMask.error(value)
        return null
    }
}