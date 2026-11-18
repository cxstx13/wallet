package ru.rfdocs.wallet.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

data class CardGradient(
    val colors: List<Color>,
    val accent: Color,
    val deep: Color
)

object DocumentGradients {

    private val Ink = Color(0xFF0C1016)

    val PassportRf = CardGradient(
        colors = listOf(Color(0xFF3A1526), Color(0xFF1C0A12), Ink),
        accent = Color(0xFFE7A9C0),
        deep = Color(0xFF1C0A12)
    )

    val PassportForeign = CardGradient(
        colors = listOf(Color(0xFF1C2B52), Color(0xFF111B33), Ink),
        accent = Color(0xFF9BB0FF),
        deep = Color(0xFF111B33)
    )

    val Snils = CardGradient(
        colors = listOf(Color(0xFF12432F), Color(0xFF0B2A1F), Ink),
        accent = Palette.Mint,
        deep = Color(0xFF0B2A1F)
    )

    val Inn = CardGradient(
        colors = listOf(Color(0xFF262C36), Color(0xFF151A21), Ink),
        accent = Color(0xFFB9C6E8),
        deep = Color(0xFF151A21)
    )

    val Vehicle = CardGradient(
        colors = listOf(Color(0xFF1B2129), Color(0xFF0F1318), Ink),
        accent = Color(0xFFB9C6E8),
        deep = Color(0xFF0F1318)
    )

    val Insurance = CardGradient(
        colors = listOf(Color(0xFF2A2C6B), Color(0xFF15173C), Ink),
        accent = Color(0xFFA9B4FF),
        deep = Color(0xFF15173C)
    )

    val Medicine = CardGradient(
        colors = listOf(Color(0xFF0F4E59), Color(0xFF0A2E36), Ink),
        accent = Palette.Azure,
        deep = Color(0xFF0A2E36)
    )

    val Family = CardGradient(
        colors = listOf(Color(0xFF4A2418), Color(0xFF27110B), Ink),
        accent = Color(0xFFF3B79A),
        deep = Color(0xFF27110B)
    )

    val Bank = CardGradient(
        colors = listOf(Color(0xFF232A34), Color(0xFF11151B), Ink),
        accent = Palette.GoldSoft,
        deep = Color(0xFF11151B)
    )

    val Custom = CardGradient(
        colors = listOf(Color(0xFF23264F), Color(0xFF12142C), Ink),
        accent = Palette.Gold,
        deep = Color(0xFF12142C)
    )

    fun forType(typeKey: String): CardGradient {
        val key = typeKey.lowercase()
        return when {
            key.contains("foreign") || key.contains("zagran") -> PassportForeign
            key.contains("passport") || key.contains("pasport") -> PassportRf
            key.contains("snils") -> Snils
            key.contains("inn") -> Inn
            key.contains("driver") || key.contains("vu") -> Vehicle
            key.contains("sts") || key.contains("pts") -> Vehicle
            key.contains("osago") || key.contains("kasko") -> Insurance
            key.contains("oms") || key.contains("dms") -> Medicine
            key.contains("birth") || key.contains("family") -> Family
            key.contains("bank") -> Bank
            else -> Custom
        }
    }

    fun colorsFor(typeKey: String): List<Color> = forType(typeKey).colors

    fun accentFor(typeKey: String): Color = forType(typeKey).accent

    fun brush(typeKey: String): Brush = Brush.linearGradient(colors = forType(typeKey).colors)

    fun brush(gradient: CardGradient): Brush = Brush.linearGradient(colors = gradient.colors)

    fun screenBrush(dark: Boolean): Brush = if (dark) {
        Brush.verticalGradient(
            colors = listOf(Color(0xFF0B0E15), Palette.NightBase)
        )
    } else {
        Brush.verticalGradient(
            colors = listOf(Color(0xFFF7F9FD), Palette.LightBase)
        )
    }

    fun glowBrush(dark: Boolean): Brush = if (dark) {
        Brush.radialGradient(
            colors = listOf(Palette.Gold.copy(alpha = 0.16f), Color.Transparent)
        )
    } else {
        Brush.radialGradient(
            colors = listOf(Palette.Gold.copy(alpha = 0.20f), Color.Transparent)
        )
    }

    fun shineBrush(): Brush = Brush.linearGradient(
        colors = listOf(
            Color.White.copy(alpha = 0.16f),
            Color.White.copy(alpha = 0.03f),
            Color.Transparent
        )
    )

    fun borderBrush(accent: Color): Brush = Brush.linearGradient(
        colors = listOf(
            accent.copy(alpha = 0.45f),
            Color.White.copy(alpha = 0.14f),
            accent.copy(alpha = 0.20f)
        )
    )
}