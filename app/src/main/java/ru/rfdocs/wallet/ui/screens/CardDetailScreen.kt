package ru.rfdocs.wallet.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AddPhotoAlternate
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Call
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.QrCode2
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ru.rfdocs.wallet.data.model.FieldKind
import ru.rfdocs.wallet.data.repo.PhotoStore
import ru.rfdocs.wallet.ui.WalletViewModel
import ru.rfdocs.wallet.ui.components.ActionPill
import ru.rfdocs.wallet.ui.components.ExpiryBanner
import ru.rfdocs.wallet.ui.components.FieldRow
import ru.rfdocs.wallet.ui.components.GlassPanel
import ru.rfdocs.wallet.ui.components.PlasticCard
import ru.rfdocs.wallet.ui.components.SectionTitle
import ru.rfdocs.wallet.ui.components.copyText
import ru.rfdocs.wallet.ui.components.fieldDisplayValue
import ru.rfdocs.wallet.ui.input.PhoneMask
import ru.rfdocs.wallet.ui.theme.Palette
import ru.rfdocs.wallet.ui.theme.WalletTheme

@Composable
fun CardDetailScreen(
    viewModel: WalletViewModel,
    cardId: String,
    onBack: () -> Unit,
    onEdit: (String) -> Unit
) {
    val context = LocalContext.current
    val haptics = LocalHapticFeedback.current
    val allCards by viewModel.cards.collectAsStateWithLifecycle()
    val card = allCards.firstOrNull { it.id == cardId }

    var revealedIds by remember(cardId) { mutableStateOf(setOf<String>()) }
    var showNumber by remember(cardId) { mutableStateOf(false) }
    var showQr by remember { mutableStateOf(false) }
    var showShare by remember { mutableStateOf(false) }
    var showDelete by remember { mutableStateOf(false) }

    val pickImage = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null && card != null) {
            val bytes = runCatching {
                context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
            }.getOrNull()
            if (bytes != null) {
                val photoId = PhotoStore.save(context, bytes)
                viewModel.saveCard(card.copy(photoIds = card.photoIds + photoId))
            }
        }
    }

    if (card == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                text = "Документ не найден",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        return
    }

    val primaryNumber = card.primaryMaskedField?.let { fieldDisplayValue(it) }.orEmpty()
    val phoneField = card.fields.firstOrNull {
        it.kind == FieldKind.PHONE || it.label.contains("Телефон", ignoreCase = true)
    }
    val phoneDigits = card.phone.ifBlank { phoneField?.value.orEmpty() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .statusBarsPadding()
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.Rounded.ArrowBack,
                        contentDescription = "Назад",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
                Text(
                    text = card.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onBackground,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = {
                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    showDelete = true
                }) {
                    Icon(
                        imageVector = Icons.Rounded.Delete,
                        contentDescription = "Удалить",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(onClick = {
                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    onEdit(card.id)
                }) {
                    Icon(
                        imageVector = Icons.Rounded.Edit,
                        contentDescription = "Изменить",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            PlasticCard(card = card, showNumber = showNumber)
            Spacer(modifier = Modifier.height(16.dp))

            if (card.validUntil.isNotBlank()) {
                ExpiryBanner(card = card)
                Spacer(modifier = Modifier.height(16.dp))
            }

            SectionTitle(text = "Данные документа")
            Spacer(modifier = Modifier.height(12.dp))
            GlassPanel(modifier = Modifier.fillMaxWidth()) {
                card.fields.forEach { field ->
                    FieldRow(
                        field = field,
                        revealed = revealedIds.contains(field.id),
                        onToggleReveal = {
                            revealedIds = if (revealedIds.contains(field.id)) {
                                revealedIds - field.id
                            } else {
                                revealedIds + field.id
                            }
                        }
                    )
                }
                if (card.fields.isEmpty()) {
                    Text(
                        text = "Поля не заполнены",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (card.note.isNotBlank()) {
                Spacer(modifier = Modifier.height(20.dp))
                SectionTitle(text = "Заметка")
                Spacer(modifier = Modifier.height(10.dp))
                GlassPanel(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = card.note,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            if (card.photoIds.isNotEmpty()) {
                Spacer(modifier = Modifier.height(20.dp))
                SectionTitle(text = "Фотографии")
                Spacer(modifier = Modifier.height(10.dp))
                card.photoIds.forEach { photoId ->
                    val bitmap = remember(photoId) { PhotoStore.loadBitmap(context, photoId) }
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp)
                            .clip(RoundedCornerShape(18.dp))
                            .background(WalletTheme.extra.glassFill)
                            .border(1.dp, WalletTheme.extra.glassBorder, RoundedCornerShape(18.dp))
                    ) {
                        if (bitmap != null) {
                            Image(
                                bitmap = bitmap.asImageBitmap(),
                                contentDescription = "Фотография документа",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp)
                                    .clip(RoundedCornerShape(12.dp))
                            )
                        } else {
                            Text(
                                text = "Фото недоступно",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(10.dp)
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(Palette.Danger.copy(alpha = 0.9f))
                                .clickable {
                                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                    PhotoStore.delete(context, photoId)
                                    viewModel.saveCard(card.copy(photoIds = card.photoIds - photoId))
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Delete,
                                contentDescription = "Удалить фото",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(150.dp))
        }

        DetailBottomBar(
            showNumber = showNumber,
            hasPhone = phoneDigits.isNotBlank(),
            onToggleNumber = { showNumber = !showNumber },
            onCopy = {
                if (primaryNumber.isNotBlank()) {
                    copyText(context, primaryNumber, "Номер скопирован")
                }
            },
            onShare = { showShare = true },
            onQr = { showQr = true },
            onPhoto = {
                pickImage.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                )
            },
            onCall = {
                val dial = PhoneMask.forDial(phoneDigits)
                if (dial.isNotBlank()) {
                    val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$dial"))
                    runCatching { context.startActivity(intent) }
                }
            },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(horizontal = 20.dp, vertical = 16.dp)
        )
    }

    if (showQr) {
        val qrContent = buildShareText(
            card = card,
            selectedFieldIds = card.fields.filter { it.isFilled }.map { it.id }.toSet()
        )
        QrDialog(title = card.title, content = qrContent, onDismiss = { showQr = false })
    }

    if (showShare) {
        ShareFieldsDialog(
            card = card,
            onDismiss = { showShare = false },
            onShare = { text ->
                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, text)
                }
                runCatching { context.startActivity(Intent.createChooser(intent, "Поделиться")) }
            }
        )
    }

    if (showDelete) {
        ConfirmDialog(
            title = "Удалить документ?",
            message = "Карточка «${card.title}» и её фотографии будут удалены с устройства.",
            confirmText = "Удалить",
            onConfirm = {
                viewModel.deleteCard(card.id)
                onBack()
            },
            onDismiss = { showDelete = false }
        )
    }
}

@Composable
private fun DetailBottomBar(
    showNumber: Boolean,
    hasPhone: Boolean,
    onToggleNumber: () -> Unit,
    onCopy: () -> Unit,
    onShare: () -> Unit,
    onQr: () -> Unit,
    onPhoto: () -> Unit,
    onCall: () -> Unit,
    modifier: Modifier = Modifier
) {
    val extras = WalletTheme.extra

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(CircleShape)
            .background(extras.glassFill)
            .border(1.dp, extras.glassBorder, CircleShape)
            .padding(horizontal = 10.dp, vertical = 10.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ActionPill(
                text = if (showNumber) "Скрыть" else "Показать",
                icon = if (showNumber) Icons.Rounded.VisibilityOff else Icons.Rounded.Visibility,
                onClick = onToggleNumber
            )
            ActionPill(
                text = "Копировать",
                icon = Icons.Rounded.ContentCopy,
                onClick = onCopy
            )
            ActionPill(
                text = "Поделиться",
                icon = Icons.Rounded.Share,
                onClick = onShare
            )
            ActionPill(
                text = "QR",
                icon = Icons.Rounded.QrCode2,
                onClick = onQr
            )
            ActionPill(
                text = "Фото",
                icon = Icons.Rounded.AddPhotoAlternate,
                onClick = onPhoto
            )
            if (hasPhone) {
                ActionPill(
                    text = "Позвонить",
                    icon = Icons.Rounded.Call,
                    filled = true,
                    onClick = onCall
                )
            }
        }
    }
}