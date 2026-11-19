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
    content: @Composable BoxScope.() -> Unit
) {
    val isDark = WalletTheme.extra.isDark

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DocumentGradients.screenBrush(isDark))
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val topRight = Offset(size.width * 0.88f, size.height * 0.02f)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Palette.Lavender.copy(alpha = if (isDark) 0.26f else 0.20f),
                        Color.Transparent
                    ),
                    center = topRight,
                    radius = size.minDimension * 0.90f
                ),
                radius = size.minDimension * 0.90f,
                center = topRight
            )

            val leftMiddle = Offset(size.width * 0.02f, size.height * 0.34f)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Palette.Indigo.copy(alpha = if (isDark) 0.34f else 0.16f),
                        Color.Transparent
                    ),
                    center = leftMiddle,
                    radius = size.minDimension * 0.95f
                ),
                radius = size.minDimension * 0.95f,
                center = leftMiddle
            )

            val bottomRight = Offset(size.width * 0.92f, size.height * 0.92f)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Palette.Mint.copy(alpha = if (isDark) 0.08f else 0.12f),
                        Color.Transparent
                    ),
                    center = bottomRight,
                    radius = size.minDimension * 0.70f
                ),
                radius = size.minDimension * 0.70f,
                center = bottomRight
            )
        }
        content()
    }
}