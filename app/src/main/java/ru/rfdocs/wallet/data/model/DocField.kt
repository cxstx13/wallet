package ru.rfdocs.wallet.data.model

import kotlinx.serialization.Serializable

@Serializable
data class DocField(
    val key: String = "",
    val label: String = "",
    val value: String = "",
    val mask: String? = null,
    val secret: Boolean = false,
    val phone: Boolean = false
) {
    val isEmpty: Boolean
        get() = value.isBlank()

    val isFilled: Boolean
        get() = value.isNotBlank()

    fun digits(): String = value.filter { it.isDigit() }

    fun tail(length: Int = 4): String = digits().takeLast(length)

    fun maskedPreview(length: Int = 4): String {
        val tailPart = tail(length)
        return if (tailPart.isEmpty()) "••••" else "•••• $tailPart"
    }
}