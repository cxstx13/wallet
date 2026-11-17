package ru.rfdocs.wallet.ui.components

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.MultiFormatWriter

fun generateQrBitmap(content: String, sizePx: Int = 720): Bitmap? {
    if (content.isBlank()) return null
    return runCatching {
        val hints = mapOf(
            EncodeHintType.MARGIN to 1,
            EncodeHintType.CHARACTER_SET to "UTF-8",
            EncodeHintType.ERROR_CORRECTION to com.google.zxing.qrcode.decoder.ErrorCorrectionLevel.M
        )
        val matrix = MultiFormatWriter().encode(
            content,
            BarcodeFormat.QR_CODE,
            sizePx,
            sizePx,
            hints
        )
        val pixels = IntArray(sizePx * sizePx)
        for (y in 0 until sizePx) {
            val offset = y * sizePx
            for (x in 0 until sizePx) {
                pixels[offset + x] = if (matrix.get(x, y)) {
                    android.graphics.Color.BLACK
                } else {
                    android.graphics.Color.WHITE
                }
            }
        }
        Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888).apply {
            setPixels(pixels, 0, sizePx, 0, 0, sizePx, sizePx)
        }
    }.getOrNull()
}

@Composable
fun QrCodeView(
    content: String,
    modifier: Modifier = Modifier,
    sizePx: Int = 720
) {
    val bitmap = remember(content, sizePx) { generateQrBitmap(content, sizePx) }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(Color.White)
            .padding(12.dp),
        contentAlignment = Alignment.Center
    ) {
        if (bitmap != null) {
            Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = "QR-код документа",
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
            )
        } else {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Нет данных для QR-кода",
                    style = MaterialTheme.typography.labelLarge,
                    color = Color(0xFF6B7280),
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Заполните поля документа",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF9AA3B2),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}