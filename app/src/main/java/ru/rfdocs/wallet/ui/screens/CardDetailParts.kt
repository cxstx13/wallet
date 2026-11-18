package ru.rfdocs.wallet.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import ru.rfdocs.wallet.data.model.DocCard
import ru.rfdocs.wallet.data.repo.PhotoStore
import ru.rfdocs.wallet.ui.components.SectionTitle
import ru.rfdocs.wallet.ui.theme.Palette
import ru.rfdocs.wallet.ui.theme.WalletTheme

data class CardAction(
    val label: String,
    val icon: ImageVector,
    val onClick: () -> Unit
)

fun dialPhone(context: Context, dial: String) {
    if (dial.isBlank()) return
    val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$dial"))
    runCatching { context.startActivity(intent) }
}

fun shareText(context: Context, text: String) {
    if (text.isBlank()) return
    val intent = Intent(Intent.ACTION_SEND)
    intent.type = "text/plain"
    intent.putExtra(Intent.EXTRA_TEXT, text)
    runCatching { context.startActivity(Intent.createChooser(intent, "Поделиться")) }
}

@Composable
fun CardActionsFab(
    actions: List<CardAction>,
    modifier: Modifier = Modifier
) {
    val extras = WalletTheme.extra
    val haptics = LocalHapticFeedback.current
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        FloatingActionButton(
            onClick = {
                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
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
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        expanded = false
                        action.onClick()
                    }
                )
            }
        }
    }
}

@Composable
fun CardPhotosSection(
    card: DocCard,
    onRemovePhoto: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    if (card.photoIds.isEmpty()) return
    val context = LocalContext.current
    val haptics = LocalHapticFeedback.current

    Column(modifier = modifier.fillMaxWidth()) {
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
                            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
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