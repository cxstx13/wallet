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
        start = Color(0xFF23272F),
        middle = Color(0xFF171A21),
        end = Color(0xFF0D0F14),
        accent = Palette.Gold
    )

    private val map: Map<String, CardGradient> = mapOf(
        GradientKeys.RUBY to CardGradient(
            start = Color(0xFF7E1728),
            middle = Color(0xFF5B0F1D),
            end = Color(0xFF2A060D),
            accent = Color(0xFFF3D59B)
        ),
        GradientKeys.SAPPHIRE to CardGradient(
            start = Color(0xFF1E4E86),
            middle = Color(0xFF12325C),
            end = Color(0xFF061224),
            accent = Color(0xFFA8CAFF)
        ),
        GradientKeys.EMERALD to CardGradient(
            start = Color(0xFF14795A),
            middle = Color(0xFF0C5843),
            end = Color(0xFF04241B),
            accent = Color(0xFF9BEFCD)
        ),
        GradientKeys.GRAPHITE to CardGradient(
            start = Color(0xFF3C434F),
            middle = Color(0xFF272C35),
            end = Color(0xFF12151A),
            accent = Color(0xFFDDE3EC)
        ),
        GradientKeys.ANTHRACITE to CardGradient(
            start = Color(0xFF2A3038),
            middle = Color(0xFF1A1E25),
            end = Color(0xFF0A0C10),
            accent = Color(0xFFF0B95E)
        ),
        GradientKeys.INDIGO to CardGradient(
            start = Color(0xFF4A3AA6),
            middle = Color(0xFF322578),
            end = Color(0xFF15103F),
            accent = Color(0xFFC9BCFF)
        ),
        GradientKeys.VIOLET to CardGradient(
            start = Color(0xFF6B48C4),
            middle = Color(0xFF4A2F92),
            end = Color(0xFF22134F),
            accent = Color(0xFFE3D2FF)
        ),
        GradientKeys.TURQUOISE to CardGradient(
            start = Color(0xFF14909C),
            middle = Color(0xFF0B6773),
            end = Color(0xFF03303A),
            accent = Color(0xFFA8F0F4)
        ),
        GradientKeys.TEAL to CardGradient(
            start = Color(0xFF1D8FBB),
            middle = Color(0xFF0E6285),
            end = Color(0xFF062C40),
            accent = Color(0xFFBEEBFF)
        ),
        GradientKeys.TERRACOTTA to CardGradient(
            start = Color(0xFFAE5533),
            middle = Color(0xFF813A22),
            end = Color(0xFF3E150A),
            accent = Color(0xFFF8D3B3)
        ),
        GradientKeys.BRONZE to CardGradient(
            start = Color(0xFF6F5330),
            middle = Color(0xFF4E3A1D),
            end = Color(0xFF241809),
            accent = Color(0xFFF0DDB4)
        ),
        GradientKeys.OBSIDIAN to CardGradient(
            start = Color(0xFF2B2F3A),
            middle = Color(0xFF1A1D25),
            end = Color(0xFF08090C),
            accent = Color(0xFFD5DAE4)
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