package ru.rfdocs.wallet.ui

import androidx.compose.runtime.Composable

/**
 * Совместимость: старый экран PIN-кода заменён на LockScreen.
 * PIN_LENGTH / PinDots / PinPad объявлены в ui.components (PinPad.kt)
 * и в LockScreen.kt — здесь их дублировать нельзя.
 */
@Composable
fun PinScreen(onUnlocked: () -> Unit) {
    LockScreen(onUnlocked = onUnlocked)
}