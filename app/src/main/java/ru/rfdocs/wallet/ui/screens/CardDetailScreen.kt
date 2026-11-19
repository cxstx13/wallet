package ru.rfdocs.wallet.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Photo
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import ru.rfdocs.wallet.data.model.DocCard
import ru.rfdocs.wallet.data.model.DocField
import ru.rfdocs.wallet.data.repo.DocRepository
import ru.rfdocs.wallet.ui.components.DocStatusChip
import ru.rfdocs.wallet.ui.components.DocumentStyleCard
import ru.rfdocs.wallet.ui.components.PhotoViewerDialog
import ru.rfdocs.wallet.ui.components.WalletBackground
import ru.rfdocs.wallet.ui.components.docStatusOf
import ru.rfdocs.wallet.ui.components.fieldDisplayValue
import ru.rfdocs.wallet.ui.theme.MonoSmall
import ru.rfdocs.wallet.ui.theme.Palette
import ru.rfdocs.wallet.ui.theme.WalletTheme

@Composable
fun CardDetailScreen(
    cardId: String,
    onBack: () -> Unit,
    onEdit: (String) -> Unit,
    onDelete: (DocCard) -> Unit
) {
    val cards by DocRepository.cards.collectAsState()
    val card = remember(cards, cardId) { cards.firstOrNull { it.id == cardId } }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbar = remember { SnackbarHostState() }

    var showQr by remember { mutableStateOf(false) }
    var showShare by remember { mutableStateOf(false) }
    var showDelete by remember { mutableStateOf(false) }
    var menuOpen by remember { mutableStateOf(false) }
    var photoId by remember { mutableStateOf<String?>(null) }

    fun copyValue(value: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.setPrimaryClip(ClipData.newPlainText("Значение", value))
        scope.launch { snackbar.showSnackbar("Скопировано") }
    }

    fun dialPhone(target: DocCard) {
        val phone = target.fields.firstOrNull {
            it.label.lowercase().contains("тел") && it.value.isNotBlank()
        }?.value
        if (phone == null) {
            scope.launch { snackbar.showSnackbar("Телефон не найден") }
            return
        }
        val digits = phone.filter { it.isDigit() || it == '+' }
        runCatching {
            context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:$digits")))
        }.onFailure {
            scope.launch { snackbar.showSnackbar("Не удалось открыть набор") }
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbar) }
    ) { padding ->
        WalletBackground(modifier = Modifier.padding(padding)) {
            if (card == null) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(20.dp)
                ) {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Назад",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = "Документ не найден",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .statusBarsPadding()
                        .padding(horizontal = 20.dp)
                ) {
                    Spacer(Modifier.height(4.dp))

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
                        Box {
                            IconButton(onClick = { menuOpen = true }) {
                                Icon(
                                    imageVector = Icons.Filled.MoreVert,
                                    contentDescription = "Действия",
                                    tint = MaterialTheme.colorScheme.onBackground
                                )
                            }
                            DropdownMenu(
                                expanded = menuOpen,
                                onDismissRequest = { menuOpen = false }
                            ) {
                                if (card.photoIds.isNotEmpty()) {
                                    DropdownMenuItem(
                                        text = { Text("Фото") },
                                        leadingIcon = {
                                            Icon(
                                                imageVector = Icons.Filled.Photo,
                                                contentDescription = null
                                            )
                                        },
                                        onClick = {
                                            menuOpen = false
                                            photoId = card.photoIds.firstOrNull()
                                        }
                                    )
                                }
                                DropdownMenuItem(
                                    text = { Text("Редактировать") },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Filled.Edit,
                                            contentDescription = null
                                        )
                                    },
                                    onClick = {
                                        menuOpen = false
                                        onEdit(card.id)
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Позвонить") },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Filled.Call,
                                            contentDescription = null
                                        )
                                    },
                                    onClick = {
                                        menuOpen = false
                                        dialPhone(card)
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Удалить") },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Filled.Delete,
                                            contentDescription = null,
                                            tint = Palette.Danger
                                        )
                                    },
                                    onClick = {
                                        menuOpen = false
                                        showDelete = true
                                    }
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(4.dp))
                    DocumentStyleCard(card = card)
                    Spacer(Modifier.height(18.dp))

                    Text(
                        text = card.title,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(Modifier.height(10.dp))
                    DocStatusChip(status = docStatusOf(card))

                    Spacer(Modifier.height(22.dp))

                    val filled = card.fields.filter { it.value.isNotBlank() }
                    if (filled.isEmpty()) {
                        Text(
                            text = "Нет заполненных реквизитов",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            filled.forEach { field ->
                                DetailFieldRow(field = field, onCopy = { copyValue(it) })
                            }
                        }
                    }

                    Spacer(Modifier.height(26.dp))

                    PrimaryCapsuleButton(
                        text = "Показать QR-код",
                        onClick = { showQr = true }
                    )
                    Spacer(Modifier.height(10.dp))
                    GlassCapsuleButton(
                        text = "Поделиться",
                        onClick = { showShare = true }
                    )

                    Spacer(Modifier.height(32.dp))
                    Spacer(Modifier.navigationBarsPadding())
                }
            }
        }
    }

    if (card != null) {
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
                    showDelete = false
                    onDelete(card)
                }
            )
        }
        photoId?.let { id ->
            PhotoViewerDialog(
                photoId = id,
                onDismiss = { photoId = null },
                onDelete = { photoId = null }
            )
        }
    }
}

@Composable
private fun DetailFieldRow(
    field: DocField,
    onCopy: (String) -> Unit
) {
    val extras = WalletTheme.extra
    val shape = RoundedCornerShape(18.dp)
    val masked = !field.mask.isNullOrBlank() && field.value.isNotBlank()
    var revealed by remember(field.key, field.value) { mutableStateOf(false) }
    val shown = if (masked && !revealed) fieldDisplayValue(field) else field.value

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(extras.glassFill)
            .border(1.dp, extras.glassBorder, shape)
            .clickable { onCopy(field.value) }
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = field.label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = shown,
                style = MonoSmall.copy(letterSpacing = 0.4.sp),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }

        if (masked) {
            IconButton(onClick = { revealed = !revealed }) {
                Icon(
                    imageVector = if (revealed) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                    contentDescription = if (revealed) "Скрыть" else "Показать",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
        Icon(
            imageVector = Icons.Filled.ContentCopy,
            contentDescription = "Копировать",
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(18.dp)
        )
    }
}

@Composable
private fun PrimaryCapsuleButton(
    text: String,
    onClick: () -> Unit
) {
    val extras = WalletTheme.extra
    val shape = RoundedCornerShape(16.dp)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .clip(shape)
            .background(extras.chipSelectedFill)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = extras.chipSelectedText
        )
    }
}

@Composable
private fun GlassCapsuleButton(
    text: String,
    onClick: () -> Unit
) {
    val extras = WalletTheme.extra
    val shape = RoundedCornerShape(16.dp)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .clip(shape)
            .background(extras.glassFill)
            .border(1.dp, extras.glassBorder, shape)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}