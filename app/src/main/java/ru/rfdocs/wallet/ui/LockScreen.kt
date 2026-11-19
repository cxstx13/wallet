package ru.rfdocs.wallet.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Icon
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.delay
import ru.rfdocs.wallet.security.BiometricHelper
import ru.rfdocs.wallet.security.SecurePrefs
import ru.rfdocs.wallet.ui.components.PinDots
import ru.rfdocs.wallet.ui.components.PinPad
import ru.rfdocs.wallet.ui.components.WalletBackground
import ru.rfdocs.wallet.ui.theme.Palette
import ru.rfdocs.wallet.ui.theme.WalletTheme

private const val PIN_LENGTH = 4

@Composable
fun LockScreen(
    onUnlocked: () -> Unit,
    onForgotPin: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val activity = context as? FragmentActivity
    val lifecycleOwner = LocalLifecycleOwner.current
    val haptics = LocalHapticFeedback.current
    val extras = WalletTheme.extra

    var pin by remember { mutableStateOf("") }
    var errorCount by remember { mutableStateOf(0) }
    var lastPromptAt by remember { mutableStateOf(0L) }

    val biometricEnabled = remember { SecurePrefs.isBiometricEnabled(context) }
    val biometricAvailable = remember { BiometricHelper.isBiometricAvailable(context) }

    fun tryBiometric() {
        if (!biometricEnabled || !biometricAvailable || activity == null) return
        val now = System.currentTimeMillis()
        if (now - lastPromptAt < 1500L) return
        lastPromptAt = now
        BiometricHelper.prompt(
            activity = activity,
            title = "Вход в приложение",
            subtitle = "Подтвердите отпечатком пальца",
            cancelText = "Ввести PIN",
            onSuccess = { onUnlocked() },
            onFailure = { }
        )
    }

    LaunchedEffect(lifecycleOwner) {
        lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
            delay(250)
            tryBiometric()
        }
    }

    LaunchedEffect(pin) {
        if (pin.length < PIN_LENGTH) return@LaunchedEffect
        if (PinGate.verify(context, pin)) {
            SecurePrefs.setFailedAttempts(context, 0)
            haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            onUnlocked()
        } else {
            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
            errorCount += 1
            SecurePrefs.setFailedAttempts(context, SecurePrefs.failedAttempts(context) + 1)
            delay(600)
            pin = ""
        }
    }

    WalletBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(74.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            listOf(Palette.LavenderSoft, Palette.LavenderDeep)
                        )
                    )
                    .border(1.dp, Color.White.copy(alpha = 0.18f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Lock,
                    contentDescription = null,
                    tint = Color(0xFF14101F),
                    modifier = Modifier.size(30.dp)
                )
            }

            Spacer(Modifier.height(22.dp))
            Text(
                text = "Введите PIN-код",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = "Доступ к вашим документам",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(28.dp))
            PinDots(
                count = pin.length,
                length = PIN_LENGTH,
                error = errorCount > 0
            )
            Spacer(Modifier.height(12.dp))
            Text(
                text = if (errorCount > 0) "Неверный PIN-код" else " ",
                style = MaterialTheme.typography.labelMedium,
                color = Palette.Danger
            )

            Spacer(Modifier.height(18.dp))
            PinPad(
                onDigit = { digit -> if (pin.length < PIN_LENGTH) pin = pin + digit },
                onBackspace = { pin = pin.dropLast(1) },
                biometric = biometricAvailable && biometricEnabled,
                onBiometric = { tryBiometric() }
            )

            if (onForgotPin != null) {
                Spacer(Modifier.height(18.dp))
                Text(
                    text = "Забыли PIN-код?",
                    style = MaterialTheme.typography.labelLarge,
                    color = extras.monoAccent,
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onForgotPin() }
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                )
            }
        }
    }
}