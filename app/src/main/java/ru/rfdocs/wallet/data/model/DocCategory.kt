package ru.rfdocs.wallet.data.model

import kotlinx.serialization.Serializable

@Serializable
enum class DocCategory(val label: String) {
    IDENTITY("Личность"),
    AUTO("Авто"),
    MEDICINE("Медицина"),
    FAMILY("Семья"),
    OTHER("Другое");

    companion object {
        fun fromLabel(label: String): DocCategory =
            entries.firstOrNull { it.label.equals(label, ignoreCase = true) } ?: OTHER

        fun fromName(name: String): DocCategory =
            entries.firstOrNull { it.name.equals(name, ignoreCase = true) } ?: OTHER
    }
}