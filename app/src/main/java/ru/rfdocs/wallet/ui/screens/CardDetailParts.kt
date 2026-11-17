package ru.rfdocs.wallet.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import ru.rfdocs.wallet.data.model.DocCard
import ru.rfdocs.wallet.data.repo.PhotoStore
import ru.rfdocs.wallet.ui.components.ActionPill
import ru.rfdocs.wallet.ui.components.SectionTitle
import ru.rfdocs.wallet.ui.theme.Palette
import ru.rfdocs.wallet.ui.theme.WalletTheme

data class PillSpec(
    val text: String,
    val icon: ImageVector,
    val filled: Boolean,
    val onClick: () -> Unit
)

fun dialPhone(context: Context, dial: String) {
    if (dial.isBlank()) return
    val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$dial"))
    runCatching { context.startActivity(intent) }
}

@Composable
fun CardDetailBottomBar(
    pills: List<PillSpec>,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(CircleShape)
            .background(WalletTheme.extra.glassFill)
            .border(1.dp, WalletTheme.extra.glassBorder, CircleShape)
            .padding(horizontal = 10.dp, vertical = 10.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            pills.forEach { pill ->
                ActionPill(
                    text = pill.text,
                    icon = pill.icon,
                    filled = pill.filled,
                    onClick = pill.onClick
                )
            }
        }
    }
}

@Composable
fun CardPhotosSection(
    card: DocCard,
    onRemovePhoto: (String) -> Unit
) {
    if (card.photoIds.isEmpty()) return
    val context = LocalContext.current
    val haptics = LocalHapticFeedback.current

    Column(modifier = Modifier.fillMaxWidth()) {
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
                            onRemovePhoto(photoId)
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
}

@Composable
fun CardQrDialog(card: DocCard, onDismiss: () -> Unit) {
    val content = buildShareText(
        card = card,
        selectedFieldIds = card.fields.filter { it.isFilled }.map { it.id }.toSet()
    )
    QrDialog(title = card.title, content = content, onDismiss = onDismiss)
}

@Composable
fun CardShareDialog(card: DocCard, onDismiss: () -> Unit) {
    val context = LocalContext.current
    ShareFieldsDialog(
        card = card,
        onDismiss = onDismiss,
        onShare = { text ->
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, text)
            }
            runCatching {
                context.startActivity(Intent.createChooser(intent, "Поделиться"))
            }
        }
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