package ru.rfdocs.wallet.ui.theme

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountBalance
import androidx.compose.material.icons.rounded.Badge
import androidx.compose.material.icons.rounded.ChildCare
import androidx.compose.material.icons.rounded.ConfirmationNumber
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.DirectionsCar
import androidx.compose.material.icons.rounded.DriveEta
import androidx.compose.material.icons.rounded.LocalHospital
import androidx.compose.material.icons.rounded.MedicalServices
import androidx.compose.material.icons.rounded.Public
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.VerifiedUser
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import ru.rfdocs.wallet.data.model.DocCategory
import ru.rfdocs.wallet.data.model.GradientKeys
import ru.rfdocs.wallet.data.model.IconKeys

data class CardGradient(
    val start: Color,
    val middle: Color,
    val end: Color,
    val accent: Color
)

object CardGradients {

    private val default = CardGradient(
        start = Palette.Graphite,
        middle = Palette.GraphiteDark,
        end = Color(0xFF0E1013),
        accent = Palette.Gold
    )

    private val map: Map<String, CardGradient> = mapOf(
        GradientKeys.RUBY to CardGradient(
            start = Color(0xFF8E1B2E),
            middle = Color(0xFF6A0F20),
            end = Color(0xFF3B0611),
            accent = Palette.Gold
        ),
        GradientKeys.SAPPHIRE to CardGradient(
            start = Color(0xFF1B4B85),
            middle = Color(0xFF12325C),
            end = Color(0xFF071528),
            accent = Color(0xFF9CC4F5)
        ),
        GradientKeys.EMERALD to CardGradient(
            start = Color(0xFF14805F),
            middle = Color(0xFF0F5F49),
            end = Color(0xFF052A20),
            accent = Color(0xFF8CE8C2)
        ),
        GradientKeys.GRAPHITE to CardGradient(
            start = Color(0xFF3A3F47),
            middle = Color(0xFF2B2F36),
            end = Color(0xFF16181C),
            accent = Color(0xFFD3D8DF)
        ),
        GradientKeys.ANTHRACITE to CardGradient(
            start = Color(0xFF2A2F36),
            middle = Color(0xFF1C2025),
            end = Color(0xFF0D0F12),
            accent = Palette.Amber
        ),
        GradientKeys.INDIGO to CardGradient(
            start = Color(0xFF4B3AA0),
            middle = Color(0xFF362A78),
            end = Color(0xFF1A1450),
            accent = Color(0xFFC3B6FF)
        ),
        GradientKeys.VIOLET to CardGradient(
            start = Color(0xFF6E4BC0),
            middle = Color(0xFF4E3391),
            end = Color(0xFF261752),
            accent = Color(0xFFE0CDFF)
        ),
        GradientKeys.TURQUOISE to CardGradient(
            start = Color(0xFF16949F),
            middle = Color(0xFF0E6E7A),
            end = Color(0xFF04343C),
            accent = Color(0xFF9BEEF2)
        ),
        GradientKeys.TEAL to CardGradient(
            start = Color(0xFF1E93B8),
            middle = Color(0xFF0F6E8C),
            end = Color(0xFF073447),
            accent = Color(0xFFB7EBFF)
        ),
        GradientKeys.TERRACOTTA to CardGradient(
            start = Color(0xFFB85B37),
            middle = Color(0xFF8E4026),
            end = Color(0xFF451A0C),
            accent = Color(0xFFF7CFAE)
        ),
        GradientKeys.BRONZE to CardGradient(
            start = Color(0xFF7A5A32),
            middle = Color(0xFF57401F),
            end = Color(0xFF2A1D0C),
            accent = Palette.GoldSoft
        ),
        GradientKeys.OBSIDIAN to CardGradient(
            start = Color(0xFF2E2A34),
            middle = Color(0xFF1E1B24),
            end = Color(0xFF0A090D),
            accent = Color(0xFFCFC6D8)
        )
    )

    fun of(key: String): CardGradient = map[key] ?: default

    fun of(category: DocCategory): CardGradient = when (category) {
        DocCategory.IDENTITY -> map.getValue(GradientKeys.RUBY)
        DocCategory.AUTO -> map.getValue(GradientKeys.ANTHRACITE)
        DocCategory.MEDICINE -> map.getValue(GradientKeys.TURQUOISE)
        DocCategory.FAMILY -> map.getValue(GradientKeys.TERRACOTTA)
        DocCategory.OTHER -> map.getValue(GradientKeys.GRAPHITE)
    }
}

object DocIcons {

    fun of(key: String): ImageVector = when (key) {
        IconKeys.PASSPORT -> Icons.Rounded.Badge
        IconKeys.GLOBE -> Icons.Rounded.Public
        IconKeys.SNILS -> Icons.Rounded.Description
        IconKeys.INN -> Icons.Rounded.AccountBalance
        IconKeys.DRIVER -> Icons.Rounded.DriveEta
        IconKeys.CAR -> Icons.Rounded.DirectionsCar
        IconKeys.VIN -> Icons.Rounded.ConfirmationNumber
        IconKeys.SHIELD -> Icons.Rounded.Shield
        IconKeys.SHIELD_PLUS -> Icons.Rounded.VerifiedUser
        IconKeys.MEDICAL -> Icons.Rounded.LocalHospital
        IconKeys.MEDICAL_PLUS -> Icons.Rounded.MedicalServices
        IconKeys.BABY -> Icons.Rounded.ChildCare
        IconKeys.BANK -> Icons.Rounded.AccountBalance
        else -> Icons.Rounded.Star
    }
}
