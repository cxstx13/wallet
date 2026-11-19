package ru.rfdocs.wallet.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.rfdocs.wallet.data.model.DocCard
import ru.rfdocs.wallet.ui.theme.DocumentGradients

private fun coverTitles(typeKey: String): Pair<String, String> {
    val key = typeKey.lowercase()
    return when {
        key.contains("foreign") || key.contains("zagran") ->
            "РОССИЙСКАЯ ФЕДЕРАЦИЯ" to "ЗАГРАНПАСПОРТ"

        key.contains("passport") || key.contains("pasport") ->
            "РОССИЙСКАЯ ФЕДЕРАЦИЯ" to "ПАСПОРТ"

        key.contains("driver") || key.contains("vu") ->
            "РОССИЙСКАЯ ФЕДЕРАЦИЯ" to "ВОДИТЕЛЬСКОЕ УДОСТОВЕРЕНИЕ"

        key.contains("sts") ->
            "РОССИЙСКАЯ ФЕДЕРАЦИЯ" to "СВИДЕТЕЛЬСТВО О РЕГИСТРАЦИИ"

        key.contains("pts") ->
            "РОССИЙСКАЯ ФЕДЕРАЦИЯ" to "ПАСПОРТ ТРАНСПОРТНОГО СРЕДСТВА"

        key.contains("osago") || key.contains("kasko") ->
            "СТРАХОВОЙ ПОЛИС" to "ОСАГО"

        key.contains("oms") -> "ПОЛИС" to "ОМС"
        key.contains("dms") -> "ПОЛИС" to "ДМС"
        key.contains("snils") -> "ПЕНСИОННЫЙ ФОНД" to "СНИЛС"
        key.contains("inn") -> "ФЕДЕРАЛЬНАЯ НАЛОГОВАЯ СЛУЖБА" to "ИНН"
        key.contains("birth") -> "СВИДЕТЕЛЬСТВО" to "О РОЖДЕНИИ"
        key.contains("bank") -> "БАНКОВСКАЯ КАРТА" to "КАРТА"
        else -> "ЛИЧНЫЙ ДОКУМЕНТ" to "ДОКУМЕНТ"
    }
}

@Composable
fun DocumentStyleCard(
    card: DocCard,
    modifier: Modifier = Modifier
) {
    val gradient = DocumentGradients.forType(card.typeKey)
    val key = card.typeKey.lowercase()
    val isPassport = key.contains("passport") || key.contains("pasport")
    val (caption, title) = coverTitles(card.typeKey)
    val shape = RoundedCornerShape(22.dp)
    val gold = Color(0xFFE8C675)
    val borderColor = if (isPassport) gold.copy(alpha = 0.55f) else Color.White.copy(alpha = 0.12f)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1.62f)
            .clip(shape)
            .background(Brush.linearGradient(gradient.colors))
            .border(1.dp, borderColor, shape)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawRect(
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.12f),
                        Color.Transparent,
                        Color.Black.copy(alpha = 0.22f)
                    ),
                    start = Offset(0f, 0f),
                    end = Offset(size.width, size.height)
                )
            )
            if (isPassport) {
                val inset = size.minDimension * 0.055f
                drawRect(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            gold.copy(alpha = 0.55f),
                            gold.copy(alpha = 0.15f),
                            gold.copy(alpha = 0.45f)
                        )
                    ),
                    topLeft = Offset(inset, inset),
                    size = Size(
                        size.width - inset * 2f,
                        size.height - inset * 2f
                    ),
                    style = Stroke(width = 2f)
                )
            }
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(20.dp)
        ) {
            Text(
                text = caption,
                style = MaterialTheme.typography.labelSmall.copy(
                    letterSpacing = 2.4.sp,
                    fontWeight = FontWeight.SemiBold
                ),
                color = gradient.accent.copy(alpha = 0.9f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.headlineMedium.copy(
                    letterSpacing = 1.6.sp,
                    fontWeight = FontWeight.Bold
                ),
                color = Color.White,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }

        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp)
                .size(30.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.14f))
                .border(1.dp, Color.White.copy(alpha = 0.22f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.Check,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}