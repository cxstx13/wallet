package ru.rfdocs.wallet.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ru.rfdocs.wallet.ui.components.CapsuleButton
import ru.rfdocs.wallet.ui.components.PhotoThumbStrip
import ru.rfdocs.wallet.ui.components.SectionTitle

fun dialNumber(context: Context, digits: String) {
    if (digits.isBlank()) return
    val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$digits"))
    runCatching { context.startActivity(intent) }
}

@Composable
fun DetailFooter(
    photoIds: List<String>,
    onAddPhoto: () -> Unit,
    onOpenPhoto: (String) -> Unit,
    onRemovePhoto: (String) -> Unit,
    onQr: () -> Unit,
    onShare: () -> Unit
) {
    SectionTitle(text = "Фото документа")
    Spacer(Modifier.height(10.dp))

    CapsuleButton(text = "Добавить фото", filled = false, onClick = onAddPhoto)
    Spacer(Modifier.height(10.dp))

    PhotoThumbStrip(
        photoIds = photoIds,
        onOpen = onOpenPhoto,
        onRemove = onRemovePhoto
    )

    Spacer(Modifier.height(18.dp))

    CapsuleButton(text = "Показать QR-код", filled = true, onClick = onQr)
    Spacer(Modifier.height(10.dp))
    CapsuleButton(text = "Поделиться", filled = false, onClick = onShare)

    Spacer(Modifier.height(40.dp))
}