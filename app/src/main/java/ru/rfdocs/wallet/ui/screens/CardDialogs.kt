package ru.rfdocs.wallet.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import ru.rfdocs.wallet.data.model.DocCard

@Composable
fun CardQrDialog(
    card: DocCard,
    onDismiss: () -> Unit
) {
    val keys = card.fields.filter { it.value.isNotBlank() }.map { it.key }.toSet()
    QrDialog(
        title = card.title,
        content = buildShareText(card, keys),
        onDismiss = onDismiss
    )
}

@Composable
fun CardShareDialog(
    card: DocCard,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    ShareFieldsDialog(
        card = card,
        onDismiss = onDismiss,
        onShare = { text -> sharePlainText(context, text) }
    )
}

@Composable
fun CardDeleteDialog(
    card: DocCard,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    ConfirmDialog(
        title = "Удалить документ?",
        message = "Карточка «${card.title}» и её фотографии будут удалены с устройства.",
        confirmText = "Удалить",
        onConfirm = onConfirm,
        onDismiss = onDismiss
    )
}