package ru.rfdocs.wallet.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import ru.rfdocs.wallet.data.model.DocCard

// Диалог QR-кода документа
@Composable
fun CardQrDialog(card: DocCard, onDismiss: () -> Unit) {
    val ids = card.fields.filter { it.isFilled }.map { it.id }.toSet()
    val text = buildShareText(card, ids)
    QrDialog(title = card.title, content = text, onDismiss = onDismiss)
}

// Диалог выбора полей для отправки
@Composable
fun CardShareDialog(card: DocCard, onDismiss: () -> Unit) {
    val context = LocalContext.current
    ShareFieldsDialog(
        card = card,
        onDismiss = onDismiss,
        onShare = { text -> shareText(context, text) }
    )
}

// Подтверждение удаления карточки
@Composable
fun CardDeleteDialog(card: DocCard, onDismiss: () -> Unit, onConfirm: () -> Unit) {
    ConfirmDialog(
        title = "Удалить документ?",
        message = "Карточка «${card.title}» и её фотографии будут удалены.",
        confirmText = "Удалить",
        onConfirm = onConfirm,
        onDismiss = onDismiss
    )
}