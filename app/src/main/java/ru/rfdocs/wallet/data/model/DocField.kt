package ru.rfdocs.wallet.data.model

import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class DocField(
    val id: String = UUID.randomUUID().toString(),
    val label: String = "",
    val value: String = "",
    val masked: Boolean = false,
    val mono: Boolean = false,
    val inputMask: String = "",
    val multiline: Boolean = false,
    val placeholder: String = ""
) {
    val isEmpty: Boolean
        get() = value.isBlank()

    val isFilled: Boolean
        get() = value.isNotBlank()

    fun digits(): String = value.filter { it.isDigit() }

    fun tail(count: Int = 4): String {
        val raw = value.trim()
        val digits = digits()
        return if (digits.isNotEmpty()) digits.takeLast(count) else raw.takeLast(count)
    }

    fun maskedPreview(count: Int = 4): String {
        val tailPart = tail(count)
        return if (tailPart.isEmpty()) "••••" else "•••• $tailPart"
    }

    companion object {
        const val MASK_PASSPORT = "0000 000000"
        const val MASK_SNILS = "000-000-000 00"
        const val MASK_DEPARTMENT = "000-000"
        const val MASK_ZAGRAN = "00 0000000"
        const val MASK_POLICY = "000 0000000"
        const val MASK_OMS = "0000 0000 0000 0000"
        const val MASK_INN = "000000000000"
        const val MASK_ACCOUNT = "00000000000000000000"
        const val MASK_BIK = "000000000"
        const val MASK_KPP = "0000 00"
        const val MASK_PHONE = "+0 (000) 000-00-00"
    }
}

fun maskDigits(input: String, mask: String): String {
    if (mask.isEmpty()) return input
    val sb = StringBuilder()
    var index = 0
    for (ch in mask) {
        if (ch == '0') {
            if (index >= input.length) break
            sb.append(input[index])
            index++
        } else {
            sb.append(ch)
        }
    }
    return sb.toString()
}

fun applyInputMask(raw: String, mask: String): String {
    if (mask.isEmpty()) return raw
    val digits = raw.filter { it.isDigit() }
    return maskDigits(digits, mask)
}
