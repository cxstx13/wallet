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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ru.rfdocs.wallet.data.repo.DocRepository
import ru.rfdocs.wallet.data.repo.PhotoStore
import ru.rfdocs.wallet.ui.WalletViewModel
import ru.rfdocs.wallet.ui.components.DocumentRow
import ru.rfdocs.wallet.ui.components.DocumentStyleCard
import ru.rfdocs.wallet.ui.components.FieldRow
import ru.rfdocs.wallet.ui.components.GlassPanel
import ru.rfdocs.wallet.ui.components.SectionTitle
import ru.rfdocs.wallet.ui.components.WalletBackground
import ru.rfdocs.wallet.ui.components.expiryDateOf
import ru.rfdocs.wallet.ui.theme.Palette

@Composable
fun CardDetailScreen(
    viewModel: WalletViewModel,
    cardId: String,
    onBack: () -> Unit,
    onEdit: (String) -> Unit
) {
    val cards by DocRepository.cards.collectAsState()
    val card = cards.firstOrNull { it.id == cardId }

    var revealed by remember(cardId) { mutableStateOf(setOf<String>()) }
    var showNumber by remember(cardId) { mutableStateOf(false) }
    var showQr by remember { mutableStateOf(false) }
    var showShare by remember { mutableStateOf(false) }
    var showDelete by remember { mutableStateOf(false) }

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
                        imageVector = Icons.Filled.Share,
                        contentDescription = "Показать",
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

            Spacer(Modifier.height(16.dp))

            Text(
                text = card.title,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground
            )

            if (expiryDateOf(card) != null) {
                Spacer(Modifier.height(6.dp))
                Text(
                    text = expiryCaption(card),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Palette.Success
                )
            }

            Spacer(Modifier.height(20.dp))
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

            Spacer(Modifier.height(20.dp))

            Row(modifier = Modifier.fillMaxWidth()) {
                PillButton(
                    text = "Показать QR-код",
                    icon = Icons.Filled.QrCode2,
                    onClick = { showQr = true },
                    modifier = Modifier.weight(1f)
                )
                Spacer(Modifier.height(0.dp))
            }

            Spacer(Modifier.height(10.dp))

            Row(modifier = Modifier.fillMaxWidth()) {
                PillButton(
                    text = "Поделиться",
                    icon = Icons.Filled.Share,
                    onClick = { showShare = true },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(Modifier.height(40.dp))
        }
    }
}