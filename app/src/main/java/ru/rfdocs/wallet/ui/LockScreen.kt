package ru.rfdocs.wallet.ui

import android.content.Context
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.delay
import ru.rfdocs.wallet.data.repo.DocRepository
import ru.rfdocs.wallet.security.BiometricHelper
import ru.rfdocs.wallet.security.CryptoManager
import ru.rfdocs.wallet.security.LockManager
import ru.rfdocs.wallet.security.PinVault
import ru.rfdocs.wallet.security.SecurePrefs
import ru.rfdocs.wallet.ui.components.WalletBackground
import ru.rfdocs.wallet.ui.theme.Palette

fun Context.findFragmentActivity(): FragmentActivity? {
    var ctx: Context? = this
    while (ctx is android.content.ContextWrapper) {
        if (ctx is FragmentActivity) return ctx
        ctx = ctx.baseContext
    }
    return null
}

fun wipeVault(context: Context) {
    runCatching { DocRepository.wipeAll() }
    CryptoManager.wipeKey()
    PinVault.clear(context)
    SecurePrefs.clearAll(context)
    LockManager.lock()
}

@Composable
fun LockScreen(
    onUnlocked: () -> Unit,
    onVaultReset: () -> Unit
) {
    val context = LocalContext.current
    val haptics = LocalHapticFeedback.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val activity = remember(context) { context.findFragmentActivity() }

    var pin by remember { mutableStateOf("") }
    var errorCount by remember { mutableIntStateOf(0) }
    var showReset by remember { mutableStateOf(false) }
    var lastPromptAt by remember { mutableLongStateOf(0L) }

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
        if (PinVault.verify(context, pin)) {
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

    if (showReset) {
        LockResetDialog(
            onDismiss = { showReset = false },
            onConfirm = { wipeVault(context); onVaultReset() }
        )
    }

    WalletBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .navigationBarsPadding()
                .padding(horizontal = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.weight(1f))

            PinHeader(
                title = "Введите PIN-код",
                subtitle = "Данные хранятся только на этом устройстве"
            )

            Spacer(Modifier.height(30.dp))
            PinDots(length = PIN_LENGTH, filled = pin.length, errorCount = errorCount)
            Spacer(Modifier.height(12.dp))
            Text(
                text = if (errorCount > 0) "Неверный PIN-код" else " ",
                style = MaterialTheme.typography.labelLarge,
                color = if (errorCount > 0) Palette.Danger else Palette.Success,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.weight(1f))

            PinKeypad(
                onDigit = { digit -> if (pin.length < PIN_LENGTH) pin += digit },
                onBackspace = { pin = pin.dropLast(1) },
                onBiometric = if (biometricEnabled && biometricAvailable) {
                    { tryBiometric() }
                } else {
                    null
                }
            )

            Spacer(Modifier.height(16.dp))
            TextButton(onClick = { showReset = true }) {
                Text(
                    text = "Забыли код доступа?",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}