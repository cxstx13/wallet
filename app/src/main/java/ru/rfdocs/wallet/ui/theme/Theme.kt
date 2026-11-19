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
    primary = Palette.Lavender,
    onPrimary = Color(0xFF14101F),
    primaryContainer = Palette.IndigoDeep,
    onPrimaryContainer = Palette.CardOnDark,
    secondary = Palette.SapphireLight,
    onSecondary = Color.White,
    tertiary = Palette.Azure,
    onTertiary = Color(0xFF14101F),
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
    primary = Palette.LavenderDeep,
    onPrimary = Color.White,
    primaryContainer = Palette.LavenderSoft,
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
    extraSmall = RoundedCornerShape(12.dp),
    small = RoundedCornerShape(16.dp),
    medium = RoundedCornerShape(20.dp),
    large = RoundedCornerShape(26.dp),
    extraLarge = RoundedCornerShape(32.dp)
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
    glassFill = Color(0xFF13121D).copy(alpha = 0.75f),
    glassBorder = Color.White.copy(alpha = 0.08f),
    glassHighlight = Color.White.copy(alpha = 0.06f),
    shadow = Color.Black.copy(alpha = 0.55f),
    onCardPrimary = Palette.CardOnDark,
    onCardSecondary = Palette.CardOnDarkMuted,
    monoAccent = Palette.Lavender,
    chipFill = Color(0xFF17151F),
    chipSelectedFill = Palette.Lavender,
    chipSelectedText = Color(0xFF14101F),
    fieldFill = Color(0xFF17151F),
    fieldStroke = Color.White.copy(alpha = 0.08f),
    fieldLabel = Palette.TextSecondaryDark,
    pillFill = Color(0xFF17151F),
    pillStroke = Color.White.copy(alpha = 0.10f),
    pillText = Palette.TextPrimaryDark,
    screenTop = Palette.NightTop,
    screenBottom = Palette.NightBase,
    isDark = true
)

private val LightExtras = WalletExtraColors(
    glassFill = Color.White.copy(alpha = 0.90f),
    glassBorder = Color(0xFF11151C).copy(alpha = 0.08f),
    glassHighlight = Color.White.copy(alpha = 0.60f),
    shadow = Color(0xFF11151C).copy(alpha = 0.12f),
    onCardPrimary = Palette.CardOnDark,
    onCardSecondary = Palette.CardOnDarkMuted,
    monoAccent = Palette.LavenderDeep,
    chipFill = Color(0xFFEDEAF7),
    chipSelectedFill = Palette.LavenderDeep,
    chipSelectedText = Color.White,
    fieldFill = Color(0xFFF0EDF9),
    fieldStroke = Color(0xFF11151C).copy(alpha = 0.06f),
    fieldLabel = Palette.TextSecondaryLight,
    pillFill = Color(0xFFEDEAF7),
    pillStroke = Color(0xFF11151C).copy(alpha = 0.08f),
    pillText = Palette.TextPrimaryLight,
    screenTop = Palette.LightTop,
    screenBottom = Palette.LightBase,
    isDark = false
)

val LocalWalletExtraColors = staticCompositionLocalOf { DarkExtras }

object WalletTheme {

    val extra: WalletExtraColors
        @Composable
        @ReadOnlyComposable
        get() = LocalWalletExtraColors.current

    val shapes: Shapes
        @Composable
        @ReadOnlyComposable
        get() = MaterialTheme.shapes
}

@Composable
fun WalletTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColors else LightColors
    val extras = if (darkTheme) DarkExtras else LightExtras
    val view = LocalView.current

    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                val controller = WindowCompat.getInsetsController(window, view)
                controller.isAppearanceLightStatusBars = !darkTheme
                controller.isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    CompositionLocalProvider(LocalWalletExtraColors provides extras) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = WalletTypography,
            shapes = WalletShapes,
            content = content
        )
    }
}