package ru.rfdocs.wallet.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ru.rfdocs.wallet.ui.theme.Palette
import ru.rfdocs.wallet.ui.theme.WalletTheme

@Composable
fun PinDots(
    count: Int,
    length: Int,
    modifier: Modifier = Modifier,
    error: Boolean = false
) {
    val extras = WalletTheme.extra
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(length) { index ->
            val filled = index < count
            Box(
                modifier = Modifier
                    .size(if (filled) 14.dp else 12.dp)
                    .clip(CircleShape)
                    .background(
                        when {
                            error -> Palette.Danger
                            filled -> extras.chipSelectedFill
                            else -> extras.chipFill
                        }
                    )
                    .border(
                        1.dp,
                        if (filled) Color.Transparent else extras.pillStroke,
                        CircleShape
                    )
            )
        }
    }
}

@Composable
fun PinPad(
    onDigit: (Char) -> Unit,
    onBackspace: () -> Unit,
    modifier: Modifier = Modifier,
    biometric: Boolean = false,
    onBiometric: () -> Unit = {}
) {
    val rows = listOf(
        listOf('1', '2', '3'),
        listOf('4', '5', '6'),
        listOf('7', '8', '9')
    )

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        rows.forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                row.forEach { digit ->
                    DigitKey(digit = digit, onClick = onDigit)
                }
            }
        }
        Row(
            horizontalArrangement = Arrangement.spacedBy(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (biometric) {
                ActionKey(icon = Icons.Filled.Fingerprint, onClick = onBiometric)
            } else {
                Spacer(Modifier.size(68.dp))
            }
            DigitKey(digit = '0', onClick = onDigit)
            ActionKey(icon = Icons.Filled.Backspace, onClick = { onBackspace() })
        }
    }
}

@Composable
private fun DigitKey(
    digit: Char,
    onClick: (Char) -> Unit
) {
    val extras = WalletTheme.extra
    val haptics = LocalHapticFeedback.current

    Box(
        modifier = Modifier
            .size(68.dp)
            .clip(CircleShape)
            .background(extras.glassFill)
            .border(1.dp, extras.glassBorder, CircleShape)
            .clickable {
                haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                onClick(digit)
            },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = digit.toString(),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun ActionKey(
    icon: ImageVector,
    onClick: () -> Unit
) {
    val haptics = LocalHapticFeedback.current

    Box(
        modifier = Modifier
            .size(68.dp)
            .clip(CircleShape)
            .clickable {
                haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                onClick()
            },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(24.dp)
        )
    }
}