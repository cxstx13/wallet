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
            drawCircle(
                brush = DocumentGradients.glowBrush(isDark),
                radius = size.minDimension * 0.85f,
                center = Offset(size.width * 0.85f, size.height * 0.04f)
            )
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Palette.Mint.copy(alpha = if (isDark) 0.10f else 0.14f),
                        Color.Transparent
                    )
                ),
                radius = size.minDimension * 0.7f,
                center = Offset(size.width * 0.05f, size.height * 0.45f)
            )
        }
        content()
    }
}