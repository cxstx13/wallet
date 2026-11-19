package ru.rfdocs.wallet.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

data class CardGradient(
    val colors: List<Color>,
    val accent: Color,
    val deep: Color
)

object DocumentGradients {

    private val Ink = Color(0xFF0B0812)

    val PassportRf = CardGradient(
        colors = listOf(Color(0xFF4B1E63), Color(0xFF2A0E3A), Ink),
        accent = Color(0xFFD9A7FF),
        deep = Color(0xFF2A0E3A)
    )

    val PassportForeign = CardGradient(
        colors = listOf(Color(0xFF1C2B52), Color(0xFF111B33), Ink),
        accent = Color(0xFF9BB0FF),
        deep = Color(0xFF111B33)
    )

    val Snils = CardGradient(
        colors = listOf(Color(0xFF6B4A12), Color(0xFF3A2708), Ink),
        accent = Color(0xFFE9A93C),
        deep = Color(0xFF3A2708)
    )

    val Inn = CardGradient(
        colors = listOf(Color(0xFF2A3346), Color(0xFF161C28), Ink),
        accent = Color(0xFFB9C6E8),
        deep = Color(0xFF161C28)
    )

    val Vehicle = CardGradient(
        colors = listOf(Color(0xFF243B8F), Color(0xFF13225A), Ink),
        accent = Color(0xFF8FB4FF),
        deep = Color(0xFF13225A)
    )

    val Insurance = CardGradient(
        colors = listOf(Color(0xFF2A2C6B), Color(0xFF15173C), Ink),
        accent = Color(0xFFA9B4FF),
        deep = Color(0xFF15173C)
    )

    val Medicine = CardGradient(
        colors = listOf(Color(0xFF0E6B4F), Color(0xFF0A3B2C), Ink),
        accent = Color(0xFF6FE3C0),
        deep = Color(0xFF0A3B2C)
    )

    val Family = CardGradient(
        colors = listOf(Color(0xFF5A2438), Color(0xFF2E1120), Ink),
        accent = Color(0xFFF3B79A),
        deep = Color(0xFF2E1120)
    )

    val Bank = CardGradient(
        colors = listOf(Color(0xFF2A2A36), Color(0xFF14141C), Ink),
        accent = Color(0xFFE8C675),
        deep = Color(0xFF14141C)
    )

    val Custom = CardGradient(
        colors = listOf(Color(0xFF2B2350), Color(0xFF141026), Ink),
        accent = Palette.Lavender,
        deep = Color(0xFF141026)
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
            colors = listOf(Palette.NightTop, Palette.NightBase)
        )
    } else {
        Brush.verticalGradient(
            colors = listOf(Palette.LightTop, Palette.LightBase)
        )
    }

    fun glowBrush(dark: Boolean): Brush = if (dark) {
        Brush.radialGradient(
            colors = listOf(Palette.Lavender.copy(alpha = 0.26f), Color.Transparent)
        )
    } else {
        Brush.radialGradient(
            colors = listOf(Palette.LavenderDeep.copy(alpha = 0.20f), Color.Transparent)
        )
    }

    fun shineBrush(): Brush = Brush.linearGradient(
        colors = listOf(
            Color.White.copy(alpha = 0.14f),
            Color.White.copy(alpha = 0.03f),
            Color.Transparent
        )
    )

    fun borderBrush(accent: Color): Brush = Brush.linearGradient(
        colors = listOf(
            accent.copy(alpha = 0.45f),
            Color.White.copy(alpha = 0.12f),
            accent.copy(alpha = 0.20f)
        )
    )
}