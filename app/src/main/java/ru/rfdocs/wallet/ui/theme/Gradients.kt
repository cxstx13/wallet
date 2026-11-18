package ru.rfdocs.wallet.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

data class CardGradient(
    val colors: List<Color>,
    val accent: Color,
    val deep: Color
)

object DocumentGradients {

    val PassportRf = CardGradient(
        colors = listOf(Palette.Ruby, Palette.RubyDeep),
        accent = Palette.Gold,
        deep = Palette.RubyDeep
    )

    val PassportForeign = CardGradient(
        colors = listOf(Palette.Sapphire, Palette.SapphireDeep),
        accent = Palette.SapphireLight,
        deep = Palette.SapphireDeep
    )

    val Snils = CardGradient(
        colors = listOf(Palette.Emerald, Palette.EmeraldDeep),
        accent = Palette.Gold,
        deep = Palette.EmeraldDeep
    )

    val Inn = CardGradient(
        colors = listOf(Palette.Graphite, Palette.GraphiteDeep),
        accent = Palette.Azure,
        deep = Palette.GraphiteDeep
    )

    val Vehicle = CardGradient(
        colors = listOf(Palette.Anthracite, Palette.AnthraciteDeep),
        accent = Palette.Amber,
        deep = Palette.AnthraciteDeep
    )

    val Insurance = CardGradient(
        colors = listOf(Palette.Indigo, Palette.IndigoDeep),
        accent = Palette.SapphireLight,
        deep = Palette.IndigoDeep
    )

    val Medicine = CardGradient(
        colors = listOf(Palette.Turquoise, Palette.AzureDeep),
        accent = Palette.Azure,
        deep = Palette.AzureDeep
    )

    val Family = CardGradient(
        colors = listOf(Palette.Terracotta, Palette.TerracottaDeep),
        accent = Palette.GoldSoft,
        deep = Palette.TerracottaDeep
    )

    val Bank = CardGradient(
        colors = listOf(Palette.Graphite, Palette.NightBase),
        accent = Palette.Gold,
        deep = Palette.NightBase
    )

    val Custom = CardGradient(
        colors = listOf(Palette.Sapphire, Palette.IndigoDeep),
        accent = Palette.Gold,
        deep = Palette.IndigoDeep
    )

    fun forType(typeKey: String): CardGradient {
        val key = typeKey.lowercase()
        return when {
            key.contains("foreign") || key.contains("zagran") -> PassportForeign
            key.contains("passport") || key.contains("pasport") -> PassportRf
            key.contains("snils") -> Snils
            key.contains("inn") -> Inn
            key.contains("driver") || key.contains("vu") -> Vehicle
            key.contains("sts") -> Vehicle
            key.contains("pts") || key.contains("epts") -> Vehicle
            key.contains("osago") || key.contains("kasko") -> Insurance
            key.contains("oms") || key.contains("dms") -> Medicine
            key.contains("birth") || key.contains("family") || key.contains("sem") -> Family
            key.contains("bank") || key.contains("requisite") -> Bank
            else -> Custom
        }
    }

    fun colorsFor(typeKey: String): List<Color> = forType(typeKey).colors

    fun accentFor(typeKey: String): Color = forType(typeKey).accent

    fun brush(typeKey: String): Brush = Brush.linearGradient(colors = forType(typeKey).colors)

    fun brush(gradient: CardGradient): Brush = Brush.linearGradient(colors = gradient.colors)

    fun screenBrush(dark: Boolean): Brush = if (dark) {
        Brush.verticalGradient(
            colors = listOf(Palette.NightSurfaceHigh, Palette.NightBase)
        )
    } else {
        Brush.verticalGradient(
            colors = listOf(Palette.LightSurface, Palette.LightBase)
        )
    }

    fun shineBrush(): Brush = Brush.linearGradient(
        colors = listOf(
            Color.White.copy(alpha = 0.20f),
            Color.White.copy(alpha = 0.04f),
            Color.Transparent,
            Color.White.copy(alpha = 0.07f)
        )
    )

    fun borderBrush(accent: Color): Brush = Brush.linearGradient(
        colors = listOf(
            accent.copy(alpha = 0.55f),
            Color.White.copy(alpha = 0.16f),
            accent.copy(alpha = 0.25f)
        )
    )
}