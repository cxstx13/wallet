package ru.rfdocs.wallet.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat

private val DarkColors = darkColorScheme(
    primary = Palette.Gold,
    onPrimary = Palette.NightBase,
    primaryContainer = Palette.Ruby,
    onPrimaryContainer = Palette.CardOnDark,
    secondary = Palette.SapphireLight,
    onSecondary = Color.White,
    tertiary = Palette.Azure,
    onTertiary = Palette.NightBase,
    background = Palette.NightBase,
    onBackground = Palette.TextPrimaryDark,
    surface = Palette.NightSurface,
    onSurface = Palette.TextPrimaryDark,
    surfaceVariant = Palette.NightSurfaceHigh,
    onSurfaceVariant = Palette.TextSecondaryDark,
    outline = Palette.NightOutline,
    outlineVariant = Palette.NightOutline,
    error = Palette.Danger,
    onError = Color.White
)

private val LightColors = lightColorScheme(
    primary = Palette.Ruby,
    onPrimary = Color.White,
    primaryContainer = Palette.GoldSoft,
    onPrimaryContainer = Palette.TextPrimaryLight,
    secondary = Palette.Sapphire,
    onSecondary = Color.White,
    tertiary = Palette.Turquoise,
    onTertiary = Color.White,
    background = Palette.LightBase,
    onBackground = Palette.TextPrimaryLight,
    surface = Palette.LightSurface,
    onSurface = Palette.TextPrimaryLight,
    surfaceVariant = Palette.LightSurfaceHigh,
    onSurfaceVariant = Palette.TextSecondaryLight,
    outline = Palette.LightOutline,
    outlineVariant = Palette.LightOutline,
    error = Palette.Danger,
    onError = Color.White
)

val WalletShapes = Shapes(
    extraSmall = RoundedCornerShape(10.dp),
    small = RoundedCornerShape(14.dp),
    medium = RoundedCornerShape(18.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(30.dp)
)

@Immutable
data class WalletExtraColors(
    val glassFill: Color,
    val glassBorder: Color,
    val glassHighlight: Color,
    val shadow: Color,
    val onCardPrimary: Color,
    val onCardSecondary: Color,
    val monoAccent: Color,
    val chipFill: Color,
    val chipSelectedFill: Color,
    val chipSelectedText: Color,
    val fieldFill: Color,
    val fieldStroke: Color,
    val fieldLabel: Color,
    val pillFill: Color,
    val pillStroke: Color,
    val pillText: Color,
    val screenTop: Color,
    val screenBottom: Color,
    val isDark: Boolean
)

private val DarkExtras = WalletExtraColors(
    glassFill = Color(0xFF141821),
    glassBorder = Color(0xFF232A36),
    glassHighlight = Color(0x14FFFFFF),
    shadow = Color(0xCC000000),
    onCardPrimary = Palette.CardOnDark,
    onCardSecondary = Palette.CardOnDarkSecondary,
    monoAccent = Palette.GoldSoft,
    chipFill = Color(0xFF171B23),
    chipSelectedFill = Palette.CardOnDark,
    chipSelectedText = Palette.NightBase,
    fieldFill = Color(0xFF141821),
    fieldStroke = Color(0xFF212734),
    fieldLabel = Palette.TextSecondaryDark,
    pillFill = Color(0xFF171B23),
    pillStroke = Color(0xFF252C39),
    pillText = Palette.TextPrimaryDark,
    screenTop = Palette.NightBase,
    screenBottom = Palette.NightBase,
    isDark = true
)

private val LightExtras = WalletExtraColors(
    glassFill = Color(0xFFFFFFFF),
    glassBorder = Color(0xFFE3E8EF),
    glassHighlight = Color(0x33000000),
    shadow = Color(0x1F1F2937),
    onCardPrimary = Color.White,
    onCardSecondary = Color(0xFFF0F2F6),
    monoAccent = Palette.GoldSoft,
    chipFill = Color(0xFFFFFFFF),
    chipSelectedFill = Palette.TextPrimaryLight,
    chipSelectedText = Color.White,
    fieldFill = Color(0xFFFFFFFF),
    fieldStroke = Color(0xFFE3E8EF),
    fieldLabel = Palette.TextSecondaryLight,
    pillFill = Color(0xFFFFFFFF),
    pillStroke = Color(0xFFE3E8EF),
    pillText = Palette.TextPrimaryLight,
    screenTop = Palette.LightBase,
    screenBottom = Palette.LightBase,
    isDark = false
)

val LocalWalletExtra = staticCompositionLocalOf { DarkExtras }

object WalletTheme {
    val extra: WalletExtraColors
        @Composable
        @ReadOnlyComposable
        get() = LocalWalletExtra.current
}

@Composable
fun WalletTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val extras = if (darkTheme) DarkExtras else LightExtras

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            val controller = WindowCompat.getInsetsController(window, view)
            controller.isAppearanceLightStatusBars = !darkTheme
            controller.isAppearanceLightNavigationBars = !darkTheme
        }
    }

    CompositionLocalProvider(LocalWalletExtra provides extras) {
        MaterialTheme(
            colorScheme = if (darkTheme) DarkColors else LightColors,
            typography = WalletTypography,
            shapes = WalletShapes,
            content = content
        )
    }
}