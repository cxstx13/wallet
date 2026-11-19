package ru.rfdocs.wallet.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.ChildCare
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.rfdocs.wallet.data.model.DocCard
import ru.rfdocs.wallet.ui.theme.WalletTheme
import java.time.LocalDate

@Composable
fun SectionTitle(
    text: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = text.uppercase(),
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = 1.2.sp,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier
    )
}

@Composable
fun GlassPanel(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    val extras = WalletTheme.extra
    val shape = RoundedCornerShape(20.dp)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(extras.glassFill)
            .border(1.dp, extras.glassBorder, shape)
            .padding(16.dp),
        content = content
    )
}

@Composable
fun TypeBadge(
    icon: ImageVector,
    accent: Color,
    size: Dp = 28.dp
) {
    Icon(
        imageVector = icon,
        contentDescription = null,
        tint = accent,
        modifier = Modifier.size(size)
    )
}

fun documentIcon(typeKey: String): ImageVector {
    val key = typeKey.lowercase()
    return when {
        key.contains("foreign") || key.contains("zagran") -> Icons.Filled.Description
        key.contains("passport") || key.contains("pasport") -> Icons.Filled.Badge
        key.contains("driver") || key.contains("vu") -> Icons.Filled.DirectionsCar
        key.contains("sts") || key.contains("pts") -> Icons.Filled.DirectionsCar
        key.contains("osago") || key.contains("kasko") -> Icons.Filled.Shield
        key.contains("oms") || key.contains("dms") -> Icons.Filled.MedicalServices
        key.contains("snils") -> Icons.Filled.Badge
        key.contains("inn") -> Icons.Filled.Description
        key.contains("birth") -> Icons.Filled.ChildCare
        key.contains("bank") -> Icons.Filled.AccountBalance
        else -> Icons.Filled.CreditCard
    }
}

fun documentIconOf(typeKey: String): ImageVector = documentIcon(typeKey)

fun parseDocumentDate(raw: String): LocalDate? {
    val digits = raw.filter { it.isDigit() }
    val iso = if (digits.length == 8) {
        "${digits.substring(4, 8)}-${digits.substring(2, 4)}-${digits.substring(0, 2)}"
    } else {
        raw.trim()
    }
    return runCatching { LocalDate.parse(iso) }.getOrNull()
}

fun expiryDateOf(card: DocCard): LocalDate? {
    val field = card.fields.firstOrNull { candidate ->
        val key = candidate.key.lowercase()
        val label = candidate.label.lowercase()
        key.contains("valid_until") || key.contains("expiry") || key.contains("valid_to") ||
            label.contains("действителен до") || label.contains("действует по")
    } ?: return null
    if (field.value.isBlank()) return null
    return parseDocumentDate(field.value)
}