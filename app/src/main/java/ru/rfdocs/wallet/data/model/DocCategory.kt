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
        fun fromId(id: String): DocCategory =
            entries.firstOrNull { it.name == id } ?: OTHER
    }
}

object GradientKeys {
    const val RUBY = "ruby"
    const val SAPPHIRE = "sapphire"
    const val EMERALD = "emerald"
    const val GRAPHITE = "graphite"
    const val ANTHRACITE = "anthracite"
    const val INDIGO = "indigo"
    const val TURQUOISE = "turquoise"
    const val TERRACOTTA = "terracotta"
    const val VIOLET = "violet"
    const val TEAL = "teal"
    const val BRONZE = "bronze"
    const val OBSIDIAN = "obsidian"

    val all: List<String> = listOf(
        RUBY, SAPPHIRE, EMERALD, GRAPHITE, ANTHRACITE,
        INDIGO, TURQUOISE, TERRACOTTA, VIOLET, TEAL, BRONZE, OBSIDIAN
    )
}

object IconKeys {
    const val PASSPORT = "passport"
    const val GLOBE = "globe"
    const val SNILS = "snils"
    const val INN = "inn"
    const val DRIVER = "driver"
    const val CAR = "car"
    const val VIN = "vin"
    const val SHIELD = "shield"
    const val SHIELD_PLUS = "shield_plus"
    const val MEDICAL = "medical"
    const val MEDICAL_PLUS = "medical_plus"
    const val BABY = "baby"
    const val BANK = "bank"
    const val STAR = "star"
}
