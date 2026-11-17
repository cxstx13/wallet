package ru.rfdocs.wallet.ui

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Backspace
import androidx.compose.material.icons.rounded.Fingerprint
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import ru.rfdocs.wallet.data.repo.DocRepository
import ru.rfdocs.wallet.security.BiometricHelper
import ru.rfdocs.wallet.security.CryptoManager
import ru.rfdocs.wallet.security.LockManager
import ru.rfdocs.wallet.security.PinVault
import ru.rfdocs.wallet.security.SecurePrefs
import ru.rfdocs.wallet.ui.components.GlassPanel
import ru.rfdocs.wallet.ui.theme.MonoStyle
import ru.rfdocs.wallet.ui.theme.Palette
import ru.rfdocs.wallet.ui.theme.WalletTheme
import kotlinx.coroutines.delay

const val PIN_LENGTH = 4

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
    SecurePrefs.clearAll(context)
    PinVault.clear(context)
    LockManager.lock()
}

@Composable
fun LockScreen(
    onUnlocked: () -> Unit,
    onVaultReset: () -> Unit
) {
    val context = LocalContext.current
    val activity = remember(context) { context.findFragmentActivity() }
    var pin by remember { mutableStateOf("") }
    var error by remember { mutableStateOf(false) }
    var showReset by remember { mutableStateOf(false) }
    val biometricEnabled = remember { SecurePrefs.isBiometricEnabled(context) }
    val biometricAvailable = remember { BiometricHelper.isBiometricAvailable(context) }

    fun tryBiometric() {
        if (!biometricEnabled || !biometricAvailable || activity == null) return
        BiometricHelper.prompt(
            activity = activity,
            title = "Вход в приложение",
            subtitle = "Подтвердите отпечатком пальца",
            cancelText = "Ввести PIN",
            onSuccess = { onUnlocked() },
            onFailure = { }
        )
    }

    LaunchedEffect(Unit) {
        delay(350)
        tryBiometric()
    }

    LaunchedEffect(pin) {
        if (pin.length == PIN_LENGTH) {
            if (PinVault.verify(context, pin)) {
                SecurePrefs.setFailedAttempts(context, 0)
                onUnlocked()
            } else {
                error = true
                SecurePrefs.setFailedAttempts(context, SecurePrefs.failedAttempts(context) + 1)
                delay(650)
                pin = ""
                error = false
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
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(WalletTheme.extra.glassFill)
                    .border(1.dp, WalletTheme.extra.glassBorder, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.Lock,
                    contentDescription = null,
                    tint = Palette.Gold,
                    modifier = Modifier.size(32.dp)
                )
            }
            Spacer(modifier = Modifier.height(18.dp))
            Text(
                text = "Введите PIN-код",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Локальное хранилище документов",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(26.dp))
            PinDots(length = PIN_LENGTH, filled = pin.length, error = error)
            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = if (error) "Неверный PIN-код" else " ",
                style = MaterialTheme.typography.labelLarge,
                color = Palette.Danger,
                modifier = Modifier.alpha(if (error) 1f else 0f)
            )
            Spacer(modifier = Modifier.height(10.dp))
            PinKeypad(
                onDigit = { digit -> if (pin.length < PIN_LENGTH) pin += digit },
                onBackspace = { pin = pin.dropLast(1) },
                onBiometric = if (biometricEnabled && biometricAvailable) {
                    { tryBiometric() }
                } else null,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(18.dp))
            TextButton(onClick = { showReset = true }) {
                Text(
                    text = "Забыли PIN? Сбросить хранилище",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }

    if (showReset) {
        AlertDialog(
            onDismissRequest = { showReset = false },
            title = { Text("Сбросить хранилище?") },
            text = { Text("Все сохранённые документы и PIN-код будут безвозвратно удалены с устройства.") },
            confirmButton = {
                TextButton(onClick = {
                    showReset = false
                    wipeVault(context)
                    onVaultReset()
                }) { Text("Удалить всё", color = Palette.Danger) }
            },
            dismissButton = {
                TextButton(onClick = { showReset = false }) { Text("Отмена") }
            }
        )
    }
}

@Composable
fun PinDots(length: Int, filled: Int, error: Boolean = false) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(length) { index ->
            val isFilled = index < filled
            val color = when {
                error -> Palette.Danger
                isFilled -> Palette.Gold
                else -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f)
            }
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .clip(CircleShape)
                    .background(if (isFilled || error) color else Color.Transparent)
                    .border(1.5.dp, color, CircleShape)
            )
        }
    }
}

@Composable
fun PinKeypad(
    onDigit: (String) -> Unit,
    onBackspace: () -> Unit,
    onBiometric: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val rows = listOf(
        listOf("1", "2", "3"),
        listOf("4", "5", "6"),
        listOf("7", "8", "9")
    )

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        rows.forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                row.forEach { digit ->
                    KeypadButton(
                        label = digit,
                        modifier = Modifier.weight(1f),
                        onClick = { onDigit(digit) }
                    )
                }
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                if (onBiometric != null) {
                    GlassPanel(
                        modifier = Modifier.size(64.dp),
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(20.dp),
                        contentPadding = 0.dp
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Fingerprint,
                                contentDescription = "Вход по отпечатку",
                                tint = Palette.Gold,
                                modifier = Modifier
                                    .size(30.dp)
                                    .clickableNoRipple { onBiometric() }
                            )
                        }
                    }
                }
            }
            KeypadButton(
                label = "0",
                modifier = Modifier.weight(1f),
                onClick = { onDigit("0") }
            )
            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                KeypadButton(
                    label = "",
                    modifier = Modifier.width(64.dp),
                    onClick = onBackspace,
                    icon = {
                        Icon(
                            imageVector = Icons.Rounded.Backspace,
                            contentDescription = "Удалить",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                )
            }
        }
    }
}

@Composable
private fun KeypadButton(
    label: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    icon: (@Composable () -> Unit)? = null
) {
    val extras = WalletTheme.extra
    Box(
        modifier = modifier
            .height(64.dp)
            .clip(androidx.compose.foundation.shape.RoundedCornerShape(20.dp))
            .background(extras.glassFill)
            .border(1.dp, extras.glassBorder, androidx.compose.foundation.shape.RoundedCornerShape(20.dp))
            .clickableNoRipple { onClick() },
        contentAlignment = Alignment.Center
    ) {
        if (icon != null) {
            icon()
        } else {
            Text(
                text = label,
                style = MonoStyle,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

fun Modifier.clickableNoRipple(onClick: () -> Unit): Modifier =
    this.then(
        Modifier.clickableWithIndication(onClick)
    )

private fun Modifier.clickableWithIndication(onClick: () -> Unit): Modifier =
    androidx.compose.foundation.clickable(
        interactionSource = androidx.compose.foundation.interaction.MutableInteractionSource(),
        indication = null,
        onClick = onClick
    ).let { this.then(it) }
