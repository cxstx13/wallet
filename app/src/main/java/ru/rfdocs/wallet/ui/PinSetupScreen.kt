package ru.rfdocs.wallet.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import ru.rfdocs.wallet.ui.components.PinDots
import ru.rfdocs.wallet.ui.components.PinPad
import ru.rfdocs.wallet.ui.components.WalletBackground
import ru.rfdocs.wallet.ui.theme.Palette

private const val PIN_LENGTH = 4

@Composable
fun PinSetupScreen(
    title: String,
    subtitle: String,
    onComplete: (String) -> Unit,
    onCancel: () -> Unit
) {
    val haptics = LocalHapticFeedback.current

    var stage by remember { mutableStateOf(0) }
    var first by remember { mutableStateOf("") }
    var pin by remember { mutableStateOf("") }
    var mismatch by remember { mutableStateOf(false) }

    LaunchedEffect(pin, stage) {
        if (pin.length < PIN_LENGTH) return@LaunchedEffect
        if (stage == 0) {
            first = pin
            pin = ""
            stage = 1
            mismatch = false
        } else {
            if (pin == first) {
                haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                onComplete(pin)
            } else {
                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                mismatch = true
                delay(700)
                pin = ""
                first = ""
                stage = 0
            }
        }
    }

    WalletBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 28.dp)
        ) {
            Spacer(Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onCancel) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Отмена",
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = if (stage == 0) subtitle else "Повторите PIN-код",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(Modifier.height(26.dp))
                PinDots(
                    count = pin.length,
                    length = PIN_LENGTH,
                    error = mismatch
                )
                Spacer(Modifier.height(12.dp))
                Text(
                    text = if (mismatch) "PIN-коды не совпадают" else " ",
                    style = MaterialTheme.typography.labelMedium,
                    color = Palette.Danger
                )

                Spacer(Modifier.height(18.dp))
                PinPad(
                    onDigit = { digit -> if (pin.length < PIN_LENGTH) pin = pin + digit },
                    onBackspace = { pin = pin.dropLast(1) }
                )

                Spacer(Modifier.height(20.dp))
            }
        }
    }
}