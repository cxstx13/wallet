package ru.rfdocs.wallet.ui

import androidx.compose.runtime.Composable
import ru.rfdocs.wallet.ui.screens.ConfirmDialog

@Composable
fun LockResetDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    ConfirmDialog(
        title = "Сбросить всё?",
        message = "Данные будут удалены безвозвратно.",
        confirmText = "Удалить",
        onConfirm = onConfirm,
        onDismiss = onDismiss
    )
}