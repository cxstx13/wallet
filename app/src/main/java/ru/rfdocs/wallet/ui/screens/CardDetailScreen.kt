package ru.rfdocs.wallet.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ru.rfdocs.wallet.data.repo.DocRepository
import ru.rfdocs.wallet.data.repo.PhotoStore
import ru.rfdocs.wallet.ui.WalletViewModel
import ru.rfdocs.wallet.ui.components.DocumentStyleCard
import ru.rfdocs.wallet.ui.components.FieldRow
import ru.rfdocs.wallet.ui.components.GlassPanel
import ru.rfdocs.wallet.ui.components.PhotoViewerDialog
import ru.rfdocs.wallet.ui.components.SectionTitle
import ru.rfdocs.wallet.ui.components.WalletBackground
import ru.rfdocs.wallet.ui.components.expiryDateOf
import ru.rfdocs.wallet.ui.input.PhoneMask
import ru.rfdocs.wallet.ui.theme.Palette
import java.time.format.DateTimeFormatter

@Composable
fun CardDetailScreen(
    viewModel: WalletViewModel,
    cardId: String,
    onBack: () -> Unit,
    onEdit: (String) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val cards by DocRepository.cards.collectAsState()
    val card = cards.firstOrNull { it.id == cardId }

    var revealed by remember(cardId) { mutableStateOf(setOf<String>()) }
    var showNumber by remember(cardId) { mutableStateOf(false) }
    var showQr by remember { mutableStateOf(false) }
    var showShare by remember { mutableStateOf(false) }
    var showDelete by remember { mutableStateOf(false) }
    var viewingPhoto by remember { mutableStateOf<String?>(null) }

    val picker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        scope.launch {
            val saved = withContext(Dispatchers.IO) {
                val bytes = runCatching {
                    context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                }.getOrNull()
                if (bytes == null || bytes.isEmpty()) null else PhotoStore.save(context, bytes)
            } ?: return@launch

            DocRepository.get(cardId)?.let { current ->
                DocRepository.upsert(current.copy(photoIds = current.photoIds + saved))
            }
        }
    }

    if (card == null) {
        WalletBackground {
            Text(
                text = "Документ не найден",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(24.dp)
            )
        }
        return
    }

    if (showQr) {
        CardQrDialog(card = card, onDismiss = { showQr = false })
    }
    if (showShare) {
        CardShareDialog(card = card, onDismiss = { showShare = false })
    }
    if (showDelete) {
        CardDeleteDialog(
            card = card,
            onDismiss = { showDelete = false },
            onConfirm = {
                DocRepository.delete(card.id)
                onBack()
            }
        )
    }

    viewingPhoto?.let { photoId ->
        PhotoViewerDialog(
            photoId = photoId,
            onDismiss = { viewingPhoto = null },
            onDelete = { removed ->
                DocRepository.get(card.id)?.let { current ->
                    DocRepository.upsert(current.copy(photoIds = current.photoIds - removed))
                }
                viewingPhoto = null
            }
        )
    }

    val phone = card.fields.firstOrNull { it.phone && it.value.isNotBlank() }
    val expiry = expiryDateOf(card)

    WalletBackground {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Назад",
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }
                Spacer(Modifier.weight(1f))
                IconButton(onClick = { showNumber = !showNumber }) {
                    Icon(
                        imageVector = if (showNumber) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                        contentDescription = "Показать номер",
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }
                IconButton(onClick = { onEdit(card.id) }) {
                    Icon(
                        imageVector = Icons.Filled.Edit,
                        contentDescription = "Изменить",
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }
                IconButton(onClick = { showDelete = true }) {
                    Icon(
                        imageVector = Icons.Filled.Delete,
                        contentDescription = "Удалить",
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }
            }

            DocumentStyleCard(
                card = card,
                showNumber = showNumber,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(18.dp))

            Text(
                text = card.title,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground
            )

            if (expiry != null) {
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "Действителен до " + DateTimeFormatter.ofPattern("dd.MM.yyyy").format(expiry),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Palette.Success
                )
            }

            Spacer(Modifier.height(18.dp))
            SectionTitle(text = "Данные документа")
            Spacer(Modifier.height(10.dp))

            GlassPanel(modifier = Modifier.fillMaxWidth()) {
                card.fields.forEach { field ->
                    FieldRow(
                        field = field,
                        revealed = revealed.contains(field.key),
                        onToggleReveal = {
                            revealed = if (revealed.contains(field.key)) {
                                revealed - field.key
                            } else {
                                revealed + field.key
                            }
                        }
                    )
                }
            }

            Spacer(Modifier.height(18.dp))

            DetailFooter(
                photoIds = card.photoIds,
                hasPhone = phone != null,
                onAddPhoto = { picker.launch("image/*") },
                onOpenPhoto = { photoId -> viewingPhoto = photoId },
                onRemovePhoto = { photoId ->
                    DocRepository.get(card.id)?.let { current ->
                        DocRepository.upsert(current.copy(photoIds = current.photoIds - photoId))
                    }
                },
                onQr = { showQr = true },
                onShare = { showShare = true },
                onCall = {
                    if (phone != null) {
                        dialNumber(context, PhoneMask.toDialable(phone.value))
                    }
                }
            )
        }
    }
}