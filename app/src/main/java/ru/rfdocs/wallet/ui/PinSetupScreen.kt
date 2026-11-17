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
    var firstPin by remember { mutableStateOf<String?>(null) }
    var pin by remember { mutableStateOf("") }
    var error by remember { mutableStateOf(false) }

    val currentTitle = when {
        firstPin == null -> title
        else -> "Повторите PIN-код"
    }
    val currentSubtitle = when {
        firstPin == null -> subtitle
        else -> "Введите тот же код ещё раз"
    }

    fun handleComplete(value: String) {
        if (firstPin == null) {
            firstPin = value
            pin = ""
        } else if (firstPin == value) {
            onComplete(value)
        } else {
            error = true
            pin = ""
            firstPin = null
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
            Spacer(modifier = Modifier.height(26.dp))
            PinDots(length = PIN_LENGTH, filled = pin.length, error = error)
            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = if (error) "PIN-коды не совпали, попробуйте снова" else " ",
                style = MaterialTheme.typography.labelLarge,
                color = Palette.Danger,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(10.dp))
            PinKeypad(
                onDigit = { digit ->
                    if (pin.length < PIN_LENGTH) {
                        pin += digit
                        if (pin.length == PIN_LENGTH) {
                            handleComplete(pin)
                        }
                        if (pin.length == PIN_LENGTH) error = error && false
                    }
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
