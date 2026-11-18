package ru.rfdocs.wallet.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ru.rfdocs.wallet.data.model.DocCard
import ru.rfdocs.wallet.data.repo.DocRepository
import ru.rfdocs.wallet.data.repo.PhotoStore
import ru.rfdocs.wallet.ui.WalletViewModel
import ru.rfdocs.wallet.ui.components.DocumentStyleCard
import ru.rfdocs.wallet.ui.components.ExpiryBanner
import ru.rfdocs.wallet.ui.components.FieldRow
import ru.rfdocs.wallet.ui.components.GlassPanel
import ru.rfdocs.wallet.ui.components.SectionTitle
import ru.rfdocs.wallet.ui.components.expiryDateOf
import ru.rfdocs.wallet.ui.input.PhoneMask
import ru.rfdocs.wallet.ui.theme.Palette
import ru.rfdocs.wallet.ui.theme.WalletTheme

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
            DetailAction(
                label = "Поделиться",
                icon = Icons.Filled.Share,
                onClick = { showShare = true }
            )
        )
        add(
            DetailAction(
                label = "QR-код",
                icon = Icons.Filled.QrCode2,
                onClick = { showQr = true }
            )
        )
        add(
            DetailAction(
                label = "Фото документа",
                icon = Icons.Filled.PhotoCamera,
                onClick = { picker.launch("image/*") }
            )
        )
        if (phoneField != null) {
            add(
                DetailAction(
                    label = "Позвонить",
                    icon = Icons.Filled.Call,
                    onClick = { dialNumber(context, PhoneMask.toDialable(phoneField.value)) }
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
            DetailActionsFab(
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

            DetailPhotosSection(
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

private data class DetailAction(
    val label: String,
    val icon: ImageVector,
    val onClick: () -> Unit
)

private fun dialNumber(context: Context, dial: String) {
    if (dial.isBlank()) return
    val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$dial"))
    runCatching { context.startActivity(intent) }
}

@Composable
private fun DetailActionsFab(
    actions: List<DetailAction>,
    modifier: Modifier = Modifier
) {
    val extras = WalletTheme.extra
    val haptics = LocalHapticFeedback.current
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        FloatingActionButton(
            onClick = {
                haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                expanded = true
            },
            containerColor = extras.monoAccent,
            contentColor = Palette.NightBase,
            shape = CircleShape
        ) {
            Icon(
                imageVector = Icons.Filled.MoreVert,
                contentDescription = "Действия с документом"
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .background(extras.glassFill)
                .border(1.dp, extras.glassBorder, RoundedCornerShape(16.dp))
        ) {
            actions.forEach { action ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = action.label,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = action.icon,
                            contentDescription = null,
                            tint = extras.monoAccent,
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    onClick = {
                        haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        expanded = false
                        action.onClick()
                    }
                )
            }
        }
    }
}

@Composable
private fun DetailPhotosSection(
    card: DocCard,
    onRemovePhoto: (String) -> Unit
) {
    if (card.photoIds.isEmpty()) return
    val context = LocalContext.current
    val haptics = LocalHapticFeedback.current

    Column(modifier = Modifier.fillMaxWidth()) {
        Spacer(Modifier.height(20.dp))
        SectionTitle(text = "Фотографии")
        Spacer(Modifier.height(10.dp))
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
                            haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            PhotoStore.delete(context, photoId)
                            onRemovePhoto(photoId)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Delete,
                        contentDescription = "Удалить фото",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}