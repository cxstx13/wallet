package ru.rfdocs.wallet.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.Share
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ru.rfdocs.wallet.ui.components.PhotoThumbStrip
import ru.rfdocs.wallet.ui.components.PillButton
import ru.rfdocs.wallet.ui.components.SectionTitle

fun dialNumber(context: Context, digits: String) {
    if (digits.isBlank()) return
    val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$digits"))
    runCatching { context.startActivity(intent) }
}

@Composable
fun DetailFooter(
    photoIds: List<String>,
    hasPhone: Boolean,
    onAddPhoto: () -> Unit,
    onOpenPhoto: (String) -> Unit,
    onRemovePhoto: (String) -> Unit,
    onQr: () -> Unit,
    onShare: () -> Unit,
    onCall: () -> Unit
) {
    SectionTitle(text = "Фото документа")
    Spacer(Modifier.height(10.dp))

    PillButton(text = "Добавить фото", icon = Icons.Filled.Add, onClick = onAddPhoto)
    Spacer(Modifier.height(10.dp))

    PhotoThumbStrip(
        photoIds = photoIds,
        onOpen = onOpenPhoto,
        onRemove = onRemovePhoto
    )

    Spacer(Modifier.height(18.dp))

    PillButton(text = "Показать QR-код", icon = Icons.Filled.QrCode2, onClick = onQr)
    Spacer(Modifier.height(10.dp))
    PillButton(text = "Поделиться", icon = Icons.Filled.Share, onClick = onShare)

    if (hasPhone) {
        Spacer(Modifier.height(10.dp))
        PillButton(text = "Позвонить", icon = Icons.Filled.Call, onClick = onCall)
    }

    Spacer(Modifier.height(40.dp))
}