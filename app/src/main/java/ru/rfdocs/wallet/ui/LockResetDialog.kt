package ru.rfdocs.wallet.ui

import androidx.compose.runtime.Composable
import ru.rfdocs.wallet.ui.screens.ConfirmDialog

@Composable
fun LockResetDialog(
    onDismiss: () -> Unit,
    onReset: () -> Unit
) {
    ConfirmDialog(
        title = "Сбросить PIN-код?",
        message = "Старый код будет удалён. Новый код задаётся при следующем запуске.",
        confirmText = "Сбросить",
        onConfirm = onReset,
        onDismiss = onDismiss
    )
}