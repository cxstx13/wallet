package ru.rfdocs.wallet.data.model

import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
enum class FieldKind {
    TEXT,
    MULTILINE,
    DATE,
    PHONE,
    GENDER,
    CHIPS,
    MASKED_NUMBER,
    NOTE
}

@Serializable
data class DocField(
    val id: String = UUID.randomUUID().toString(),
    val label: String = "",
    val value: String = "",
    val kind: FieldKind = FieldKind.TEXT,
    val pattern: String = "",
    val masked: Boolean = false,
    val mono: Boolean = false,
    val multiline: Boolean = false,
    val placeholder: String = "",
    val options: List<String> = emptyList(),
    val editableLabel: Boolean = false,
    val capitalizeWords: Boolean = false
) {

    val isEmpty: Boolean
        get() = value.isBlank()

    val isFilled: Boolean
        get() = value.isNotBlank()

    fun digits(): String = value.filter { it.isDigit() }

    fun tail(count: Int = 4): String {
        val raw = value.trim()
        val numeric = digits()
        return if (numeric.isNotEmpty()) numeric.takeLast(count) else raw.takeLast(count)
    }

    fun maskedPreview(count: Int = 4): String {
        val tailPart = tail(count)
        return if (tailPart.isEmpty()) "••••" else "•••• $tailPart"
    }

    fun selectedChips(): List<String> =
        value.split(',')
            .map { it.trim() }
            .filter { it.isNotEmpty() }

    companion object {
        fun chipsValue(selected: Collection<String>): String = selected.joinToString(", ")
    }
}