package ru.rfdocs.wallet.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ru.rfdocs.wallet.data.repo.DocRepository
import ru.rfdocs.wallet.data.repo.PhotoStore
import ru.rfdocs.wallet.ui.WalletViewModel
import ru.rfdocs.wallet.ui.components.CardAction
import ru.rfdocs.wallet.ui.components.CardActionsFab
import ru.rfdocs.wallet.ui.components.CardPhotosSection
import ru.rfdocs.wallet.ui.components.DocumentStyleCard
import ru.rfdocs.wallet.ui.components.ExpiryBanner
import ru.rfdocs.wallet.ui.components.FieldRow
import ru.rfdocs.wallet.ui.components.GlassPanel
import ru.rfdocs.wallet.ui.components.SectionTitle
import ru.rfdocs.wallet.ui.components.expiryDateOf
import ru.rfdocs.wallet.ui.input.PhoneMask

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CardDetailScreen(
    viewModel: WalletViewModel,
    cardId: String,
    onBack: () -> Unit,
    onEdit: (String) -> Unit
) {
    val context = LocalContext.current
    val haptics = LocalHapticFeedback.current
    val scope = rememberCoroutineScope()
    val cards by DocRepository.cards.collectAsState()
    val card = cards.firstOrNull { it.id == cardId }

    var revealedKeys by remember(cardId) { mutableStateOf(setOf<String>()) }
    var showNumber by remember(cardId) { mutableStateOf(false) }
    var showQr by remember { mutableStateOf(false) }
    var showShare by remember { mutableStateOf(false) }
    var showDelete by remember { mutableStateOf(false) }

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
                DocRepository.upsert(
                    current.copy(
                        photoIds = current.photoIds + saved,
                        updatedAt = System.currentTimeMillis()
                    )
                )
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

    val phoneField = card.fields.firstOrNull { it.phone && it.value.isNotBlank() }

    val actions = buildList {
        add(
            CardAction(
                label = "Поделиться",
                icon = Icons.Filled.Share,
                onClick = { showShare = true }
            )
        )
        add(
            CardAction(
                label = "QR-код",
                icon = Icons.Filled.QrCode2,
                onClick = { showQr = true }
            )
        )
        add(
            CardAction(
                label = "Фото документа",
                icon = Icons.Filled.PhotoCamera,
                onClick = { picker.launch("image/*") }
            )
        )
        if (phoneField != null) {
            add(
                CardAction(
                    label = "Позвонить",
                    icon = Icons.Filled.Call,
                    onClick = { dialPhone(context, PhoneMask.toDialable(phoneField.value)) }
                )
            )
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = card.title,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Назад"
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            showNumber = !showNumber
                        }
                    ) {
                        Icon(
                            imageVector = if (showNumber) {
                                Icons.Filled.VisibilityOff
                            } else {
                                Icons.Filled.Visibility
                            },
                            contentDescription = if (showNumber) "Скрыть номер" else "Показать номер"
                        )
                    }
                    IconButton(
                        onClick = {
                            haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onEdit(card.id)
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Edit,
                            contentDescription = "Изменить"
                        )
                    }
                    IconButton(
                        onClick = {
                            haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            showDelete = true
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Delete,
                            contentDescription = "Удалить"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        floatingActionButton = {
            CardActionsFab(
                actions = actions,
                modifier = Modifier.padding(8.dp)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {
            DocumentStyleCard(
                card = card,
                modifier = Modifier.fillMaxWidth(),
                showNumber = showNumber
            )

            if (expiryDateOf(card) != null) {
                Spacer(Modifier.height(16.dp))
                ExpiryBanner(card = card)
            }

            Spacer(Modifier.height(22.dp))
            SectionTitle(text = "Данные документа")
            Spacer(Modifier.height(12.dp))
            GlassPanel(modifier = Modifier.fillMaxWidth()) {
                if (card.fields.isEmpty()) {
                    Text(
                        text = "Поля не заполнены",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    card.fields.forEach { field ->
                        FieldRow(
                            field = field,
                            revealed = revealedKeys.contains(field.key),
                            onToggleReveal = {
                                revealedKeys = if (revealedKeys.contains(field.key)) {
                                    revealedKeys - field.key
                                } else {
                                    revealedKeys + field.key
                                }
                            }
                        )
                    }
                }
            }

            CardPhotosSection(
                card = card,
                onRemovePhoto = { photoId ->
                    DocRepository.get(card.id)?.let { current ->
                        DocRepository.upsert(
                            current.copy(
                                photoIds = current.photoIds - photoId,
                                updatedAt = System.currentTimeMillis()
                            )
                        )
                    }
                }
            )

            Spacer(Modifier.height(120.dp))
        }
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
}