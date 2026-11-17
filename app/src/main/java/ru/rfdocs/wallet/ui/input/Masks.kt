package ru.rfdocs.wallet.ui.input

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation

object MaskPatterns {
    const val DATE = "00.00.0000"
    const val PASSPORT = "0000 000000"
    const val DEPARTMENT = "000-000"
    const val SNILS = "000-000-000 00"
    const val INN = "000000000000"
    const val DRIVER = "00 00 000000"
    const val PHONE = "0 (000) 000-00-00"
    const val OMS = "0000 0000 0000 0000"
    const val BANK_ACCOUNT = "0000 0000 0000 0000 0000"
    const val KPP = "0000 00"
    const val BIK = "000000000"
    const val CERTIFICATE = "00 000000"
    const val POLICY = "000 0000000"
    const val VIN = "00000000000000000"
}

fun digitsOnly(raw: String, maxDigits: Int = Int.MAX_VALUE): String =
    raw.filter { it.isDigit() }.take(maxDigits)

fun maxDigitsFor(mask: String): Int = mask.count { it == '0' }

fun formatWithMask(digits: String, mask: String, prefix: String = ""): String {
    val clean = digits.filter { it.isDigit() }
    val out = StringBuilder(prefix)
    var index = 0
    for (symbol in mask) {
        if (index >= clean.length) break
        if (symbol == '0') {
            out.append(clean[index])
            index++
        } else {
            out.append(symbol)
        }
    }
    return out.toString()
}

private class MaskOffsetMapping(
    private val originalToTransformedMap: IntArray,
    private val transformedToOriginalMap: IntArray
) : OffsetMapping {

    override fun originalToTransformed(offset: Int): Int {
        if (originalToTransformedMap.isEmpty()) return 0
        return originalToTransformedMap[offset.coerceIn(0, originalToTransformedMap.size - 1)]
    }

    override fun transformedToOriginal(offset: Int): Int {
        if (transformedToOriginalMap.isEmpty()) return 0
        return transformedToOriginalMap[offset.coerceIn(0, transformedToOriginalMap.size - 1)]
    }
}

class MaskVisualTransformation(
    private val mask: String,
    private val prefix: String = ""
) : VisualTransformation {

    override fun filter(text: AnnotatedString): TransformedText {
        val digits = text.text.filter { it.isDigit() }
        val body = StringBuilder()
        val digitPositions = IntArray(digits.length)
        var digitIndex = 0

        for (symbol in mask) {
            if (digitIndex >= digits.length) break
            if (symbol == '0') {
                digitPositions[digitIndex] = body.length + prefix.length
                body.append(digits[digitIndex])
                digitIndex++
            } else {
                body.append(symbol)
            }
        }

        val transformed = prefix + body.toString()
        val originalToTransformedMap = IntArray(digits.length + 1)
        for (index in digits.indices) {
            originalToTransformedMap[index] = digitPositions[index]
        }
        originalToTransformedMap[digits.length] = transformed.length

        val transformedToOriginalMap = IntArray(transformed.length + 1)
        var cursor = 0
        for (position in 0..transformed.length) {
            while (cursor < digits.length && digitPositions[cursor] < position) cursor++
            transformedToOriginalMap[position] = cursor
        }

        val annotated = AnnotatedString(transformed)
        return TransformedText(
            text = annotated,
            offsetMapping = MaskOffsetMapping(
                originalToTransformedMap = originalToTransformedMap,
                transformedToOriginalMap = transformedToOriginalMap
            )
        )
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is MaskVisualTransformation) return false
        return mask == other.mask && prefix == other.prefix
    }

    override fun hashCode(): Int = mask.hashCode() * 31 + prefix.hashCode()
}

object DateMask {
    const val MAX_DIGITS = 8

    val visual: VisualTransformation = MaskVisualTransformation(MaskPatterns.DATE)

    fun display(digits: String): String =
        formatWithMask(digitsOnly(digits, MAX_DIGITS), MaskPatterns.DATE)

    fun sanitize(raw: String): String = digitsOnly(raw, MAX_DIGITS)

    fun isValid(digits: String): Boolean {
        val clean = sanitize(digits)
        if (clean.length != MAX_DIGITS) return false
        val day = clean.substring(0, 2).toIntOrNull() ?: return false
        val month = clean.substring(2, 4).toIntOrNull() ?: return false
        val year = clean.substring(4, 8).toIntOrNull() ?: return false
        if (month !in 1..12) return false
        if (year !in 1900..2100) return false
        val maxDay = java.time.YearMonth.of(year, month).lengthOfMonth()
        return day in 1..maxDay
    }

    fun toIso(digits: String): String {
        val clean = sanitize(digits)
        if (clean.length != MAX_DIGITS) return ""
        val day = clean.substring(0, 2)
        val month = clean.substring(2, 4)
        val year = clean.substring(4, 8)
        return "$year-$month-$day"
    }

    fun fromIso(iso: String): String {
        val clean = iso.trim()
        if (clean.isEmpty()) return ""
        val parsed = runCatching { java.time.LocalDate.parse(clean) }.getOrNull()
            ?: runCatching {
                java.time.LocalDate.parse(clean, java.time.format.DateTimeFormatter.ofPattern("dd.MM.yyyy"))
            }.getOrNull()
            ?: return digitsOnly(clean, MAX_DIGITS)
        return String.format(
            java.util.Locale.US,
            "%02d%02d%04d",
            parsed.dayOfMonth,
            parsed.monthValue,
            parsed.year
        )
    }

    fun isoToDisplay(iso: String): String = display(fromIso(iso))
}

object PhoneMask {
    const val MAX_DIGITS = 11
    const val PREFIX = "+"

    val visual: VisualTransformation = MaskVisualTransformation(MaskPatterns.PHONE, PREFIX)

    fun sanitize(raw: String): String {
        var digits = digitsOnly(raw, MAX_DIGITS)
        if (digits.startsWith("8")) digits = "7" + digits.drop(1)
        return digits
    }

    fun display(digits: String): String =
        formatWithMask(sanitize(digits), MaskPatterns.PHONE, PREFIX)

    fun forDial(digits: String): String {
        val clean = sanitize(digits)
        return if (clean.isEmpty()) "" else "+$clean"
    }
}

object InputMasks {

    val date: VisualTransformation = DateMask.visual
    val phone: VisualTransformation = PhoneMask.visual
    val passport: VisualTransformation = MaskVisualTransformation(MaskPatterns.PASSPORT)
    val department: VisualTransformation = MaskVisualTransformation(MaskPatterns.DEPARTMENT)
    val snils: VisualTransformation = MaskVisualTransformation(MaskPatterns.SNILS)
    val driverLicense: VisualTransformation = MaskVisualTransformation(MaskPatterns.DRIVER)
    val oms: VisualTransformation = MaskVisualTransformation(MaskPatterns.OMS)
    val bankAccount: VisualTransformation = MaskVisualTransformation(MaskPatterns.BANK_ACCOUNT)
    val kpp: VisualTransformation = MaskVisualTransformation(MaskPatterns.KPP)
    val certificate: VisualTransformation = MaskVisualTransformation(MaskPatterns.CERTIFICATE)
    val policy: VisualTransformation = MaskVisualTransformation(MaskPatterns.POLICY)

    fun byKey(key: String): VisualTransformation? = when (key) {
        "date" -> date
        "phone" -> phone
        "passport" -> passport
        "department" -> department
        "snils" -> snils
        "driver" -> driverLicense
        "oms" -> oms
        "bank_account" -> bankAccount
        "kpp" -> kpp
        "certificate" -> certificate
        "policy" -> policy
        else -> null
    }

    fun maxFor(key: String): Int = when (key) {
        "date" -> DateMask.MAX_DIGITS
        "phone" -> PhoneMask.MAX_DIGITS
        "passport" -> maxDigitsFor(MaskPatterns.PASSPORT)
        "department" -> maxDigitsFor(MaskPatterns.DEPARTMENT)
        "snils" -> maxDigitsFor(MaskPatterns.SNILS)
        "driver" -> maxDigitsFor(MaskPatterns.DRIVER)
        "oms" -> maxDigitsFor(MaskPatterns.OMS)
        "bank_account" -> maxDigitsFor(MaskPatterns.BANK_ACCOUNT)
        "kpp" -> maxDigitsFor(MaskPatterns.KPP)
        "certificate" -> maxDigitsFor(MaskPatterns.CERTIFICATE)
        "policy" -> maxDigitsFor(MaskPatterns.POLICY)
        "inn" -> maxDigitsFor(MaskPatterns.INN)
        else -> Int.MAX_VALUE
    }

    fun sanitize(key: String, raw: String): String = when (key) {
        "phone" -> PhoneMask.sanitize(raw)
        "date" -> DateMask.sanitize(raw)
        else -> digitsOnly(raw, maxFor(key))
    }

    fun display(key: String, value: String): String = when (key) {
        "date" -> DateMask.display(value)
        "phone" -> PhoneMask.display(value)
        "passport" -> formatWithMask(value, MaskPatterns.PASSPORT)
        "department" -> formatWithMask(value, MaskPatterns.DEPARTMENT)
        "snils" -> formatWithMask(value, MaskPatterns.SNILS)
        "driver" -> formatWithMask(value, MaskPatterns.DRIVER)
        "oms" -> formatWithMask(value, MaskPatterns.OMS)
        "bank_account" -> formatWithMask(value, MaskPatterns.BANK_ACCOUNT)
        "kpp" -> formatWithMask(value, MaskPatterns.KPP)
        "certificate" -> formatWithMask(value, MaskPatterns.CERTIFICATE)
        "policy" -> formatWithMask(value, MaskPatterns.POLICY)
        else -> value
    }
}