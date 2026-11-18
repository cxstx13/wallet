package ru.rfdocs.wallet.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import ru.rfdocs.wallet.ui.theme.Palette

@Composable
fun PinSetupScreen(
    title: String,
    subtitle: String,
    onComplete: (String) -> Unit,
    onCancel: (() -> Unit)? = null
) {
    val haptics = LocalHapticFeedback.current

    var firstPin by remember { mutableStateOf<String?>(null) }
    var pin by remember { mutableStateOf("") }
    var errorCount by remember { mutableIntStateOf(0) }

    val currentTitle = if (firstPin == null) title else "Повторите PIN-код"
    val currentSubtitle = if (firstPin == null) subtitle else "Введите тот же код ещё раз"

    fun handleDigit(digit: String) {
        if (pin.length >= PIN_LENGTH) return
        val next = pin + digit
        pin = next
        if (next.length < PIN_LENGTH) return

        val stored = firstPin
        when {
            stored == null -> {
                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                firstPin = next
                pin = ""
            }

            stored == next -> {
                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                onComplete(next)
            }

            else -> {
                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                errorCount += 1
                firstPin = null
                pin = ""
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .navigationBarsPadding()
                .padding(horizontal = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.weight(1f))

            PinHeader(title = currentTitle, subtitle = currentSubtitle)

            Spacer(Modifier.height(30.dp))
            PinDots(length = PIN_LENGTH, filled = pin.length, errorCount = errorCount)
            Spacer(Modifier.height(12.dp))
            Text(
                text = if (errorCount > 0) "PIN-коды не совпали, попробуйте снова" else " ",
                style = MaterialTheme.typography.labelLarge,
                color = Palette.Danger,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.weight(1f))

            PinKeypad(
                onDigit = { digit -> handleDigit(digit) },
                onBackspace = { pin = pin.dropLast(1) },
                onBiometric = null
            )

            Spacer(Modifier.height(16.dp))
            if (onCancel != null) {
                TextButton(onClick = onCancel) {
                    Text(
                        text = "Отмена",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}