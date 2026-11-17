package ru.rfdocs.wallet.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
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
    onPrimaryContainer = Palette.TextPrimaryDark,
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
    error = Palette.Danger,
    onError = Color.White
)

val WalletShapes = Shapes(
    extraSmall = androidx.compose.foundation.shape.RoundedCornerShape(8.dp),
    small = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
    medium = androidx.compose.foundation.shape.RoundedCornerShape(18.dp),
    large = androidx.compose.foundation.shape.RoundedCornerShape(24.dp),
    extraLarge = androidx.compose.foundation.shape.RoundedCornerShape(32.dp)
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
    val chipSelectedText: Color
)

private val DarkExtras = WalletExtraColors(
    glassFill = Color.White.copy(alpha = 0.07f),
    glassBorder = Color.White.copy(alpha = 0.14f),
    glassHighlight = Color.White.copy(alpha = 0.22f),
    shadow = Color.Black.copy(alpha = 0.55f),
    onCardPrimary = Color(0xFFF7F3EC),
    onCardSecondary = Color(0xFFE7DFD2),
    monoAccent = Palette.GoldSoft,
    chipFill = Color.White.copy(alpha = 0.08f),
    chipSelectedFill = Palette.Gold,
    chipSelectedText = Palette.NightBase
)

private val LightExtras = WalletExtraColors(
    glassFill = Color.White.copy(alpha = 0.65f),
    glassBorder = Color.Black.copy(alpha = 0.08f),
    glassHighlight = Color.White.copy(alpha = 0.9f),
    shadow = Color.Black.copy(alpha = 0.18f),
    onCardPrimary = Color.White,
    onCardSecondary = Color(0xFFF1EEE9),
    monoAccent = Palette.GoldSoft,
    chipFill = Color.Black.copy(alpha = 0.05f),
    chipSelectedFill = Palette.Ruby,
    chipSelectedText = Color.White
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
