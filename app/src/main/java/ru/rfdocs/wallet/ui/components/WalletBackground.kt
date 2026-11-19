package ru.rfdocs.wallet.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import ru.rfdocs.wallet.ui.theme.DocumentGradients
import ru.rfdocs.wallet.ui.theme.Palette
import ru.rfdocs.wallet.ui.theme.WalletTheme

@Composable
fun WalletBackground(
    modifier: Modifier = Modifier,
    glow: Boolean = true,
    content: @Composable BoxScope.() -> Unit
) {
    val dark = WalletTheme.extra.isDark

    val lavender = if (dark) {
        Palette.Lavender.copy(alpha = 0.28f)
    } else {
        Palette.LavenderDeep.copy(alpha = 0.26f)
    }
    val indigo = if (dark) {
        Palette.IndigoDeep.copy(alpha = 0.42f)
    } else {
        Palette.Sapphire.copy(alpha = 0.14f)
    }
    val bottom = if (dark) {
        Palette.Lavender.copy(alpha = 0.12f)
    } else {
        Palette.LavenderDeep.copy(alpha = 0.10f)
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DocumentGradients.screenBrush(dark))
    ) {
        if (glow) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height

                drawRect(
                    brush = Brush.radialGradient(
                        colors = listOf(lavender, Color.Transparent),
                        center = Offset(w * 0.16f, h * 0.02f),
                        radius = w * 1.10f
                    )
                )
                drawRect(
                    brush = Brush.radialGradient(
                        colors = listOf(indigo, Color.Transparent),
                        center = Offset(w * 0.94f, h * 0.16f),
                        radius = w * 1.00f
                    )
                )
                drawRect(
                    brush = Brush.radialGradient(
                        colors = listOf(bottom, Color.Transparent),
                        center = Offset(w * 0.50f, h * 1.05f),
                        radius = w * 0.95f
                    )
                )
            }
        }
        content()
    }
}