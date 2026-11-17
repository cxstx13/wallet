package ru.rfdocs.wallet.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
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
    var mismatch by remember { mutableStateOf(false) }

    val currentTitle = if (firstPin == null) title else "Повторите PIN-код"
    val currentSubtitle = if (firstPin == null) subtitle else "Введите тот же код ещё раз"

    fun pushDigit(digit: String) {
        if (pin.length >= PIN_LENGTH) return
        val next = pin + digit
        pin = next
        if (next.length != PIN_LENGTH) return

        val stored = firstPin
        when {
            stored == null -> {
                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                firstPin = next
                pin = ""
                mismatch = false
            }

            stored == next -> {
                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                onComplete(next)
            }

            else -> {
                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                mismatch = true
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
                .padding(horizontal = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = currentTitle,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = currentSubtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(28.dp))
            PinDots(length = PIN_LENGTH, filled = pin.length, error = mismatch)
            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = if (mismatch) "PIN-коды не совпали, попробуйте снова" else " ",
                style = MaterialTheme.typography.labelLarge,
                color = Palette.Danger,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(12.dp))
            PinKeypad(
                onDigit = { digit ->
                    if (mismatch) mismatch = false
                    pushDigit(digit)
                },
                onBackspace = { pin = pin.dropLast(1) },
                onBiometric = null,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(18.dp))
            if (onCancel != null) {
                TextButton(onClick = onCancel) {
                    Text(
                        text = "Отмена",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}