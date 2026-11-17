package ru.rfdocs.wallet.data.model

import kotlinx.serialization.Serializable
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.UUID

@Serializable
data class DocCard(
    val id: String = UUID.randomUUID().toString(),
    val typeKey: String = "",
    val title: String = "",
    val subtitle: String = "",
    val category: DocCategory = DocCategory.OTHER,
    val gradientKey: String = GradientKeys.OBSIDIAN,
    val iconKey: String = IconKeys.STAR,
    val fields: List<DocField> = emptyList(),
    val photoIds: List<String> = emptyList(),
    val validUntil: String = "",
    val phone: String = "",
    val note: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {

    val primaryMaskedField: DocField?
        get() = fields.firstOrNull { it.masked && it.isFilled }
            ?: fields.firstOrNull { it.isFilled && it.mono }
            ?: fields.firstOrNull { it.isFilled && it.pattern.isNotBlank() }

    val maskedNumber: String
        get() = primaryMaskedField?.maskedPreview() ?: ""

    val filledFields: List<DocField>
        get() = fields.filter { it.isFilled }

    val copyableFields: List<DocField>
        get() = fields.filter { it.isFilled }

    fun numberForDisplay(showFull: Boolean): String {
        val field = primaryMaskedField ?: return subtitle
        if (field.value.isBlank()) return subtitle
        if (!field.masked) return formatPattern(field.value, field.pattern)
        return if (showFull) formatPattern(field.value, field.pattern) else field.maskedPreview()
    }

    fun searchHaystack(): String {
        val builder = StringBuilder()
        builder.append(title).append(' ')
        builder.append(subtitle).append(' ')
        builder.append(category.label).append(' ')
        for (field in fields) {
            builder.append(field.label).append(' ')
            builder.append(field.value).append(' ')
            builder.append(field.digits()).append(' ')
        }
        return builder.toString().lowercase()
    }

    fun expiryDate(): LocalDate? = parseDate(validUntil)

    fun daysLeft(): Long? {
        val date = expiryDate() ?: return null
        return ChronoUnit.DAYS.between(LocalDate.now(), date)
    }

    fun expiryLabel(): String {
        val date = expiryDate() ?: return ""
        val days = daysLeft() ?: return ""
        return when {
            days < 0 -> "Истёк"
            days == 0L -> "Истекает сегодня"
            days <= 30 -> "Осталось $days дн."
            else -> "до ${date.format(OUTPUT_FORMAT)}"
        }
    }

    fun expiryLevel(): Int {
        val days = daysLeft() ?: return 0
        return when {
            days < 0 -> 2
            days <= 30 -> 1
            else -> 0
        }
    }

    fun withUpdated(): DocCard = copy(updatedAt = System.currentTimeMillis())

    companion object {

        val OUTPUT_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")

        private val INPUT_FORMATS: List<DateTimeFormatter> = listOf(
            DateTimeFormatter.ofPattern("dd.MM.yyyy"),
            DateTimeFormatter.ISO_LOCAL_DATE,
            DateTimeFormatter.ofPattern("dd.MM.yy")
        )

        fun formatPattern(value: String, pattern: String): String {
            if (pattern.isBlank()) return value
            val digits = value.filter { it.isDigit() }
            val out = StringBuilder()
            var index = 0
            for (symbol in pattern) {
                if (index >= digits.length) break
                if (symbol == '0') {
                    out.append(digits[index])
                    index++
                } else {
                    out.append(symbol)
                }
            }
            return out.toString()
        }

        fun parseDate(value: String): LocalDate? {
            val clean = value.trim()
            if (clean.isEmpty()) return null
            for (format in INPUT_FORMATS) {
                runCatching { return LocalDate.parse(clean, format) }
            }
            return runCatching { LocalDate.parse(clean) }.getOrNull()
        }

        fun formatDate(value: String): String {
            val date = parseDate(value) ?: return value
            return date.format(OUTPUT_FORMAT)
        }

        fun normalizeDate(value: String): String {
            val date = parseDate(value) ?: return value
            return date.format(DateTimeFormatter.ISO_LOCAL_DATE)
        }
    }
}