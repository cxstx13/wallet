package ru.rfdocs.wallet.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import ru.rfdocs.wallet.data.model.DocCategory
import ru.rfdocs.wallet.data.model.DocTemplate
import ru.rfdocs.wallet.data.model.DocTemplates
import ru.rfdocs.wallet.ui.components.SectionTitle
import ru.rfdocs.wallet.ui.components.TypeBadge
import ru.rfdocs.wallet.ui.theme.CardGradients
import ru.rfdocs.wallet.ui.theme.DocIcons
import ru.rfdocs.wallet.ui.theme.WalletTheme

@Composable
fun TemplatePickerScreen(
    onPick: (String) -> Unit,
    onBack: () -> Unit
) {
    val grouped = remember { DocTemplates.all.groupBy { it.category } }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .statusBarsPadding()
            .padding(horizontal = 20.dp)
            .navigationBarsPadding()
    ) {
        Spacer(modifier = Modifier.height(6.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.Rounded.ArrowBack,
                    contentDescription = "Назад",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
            Text(
                text = "Новый документ",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Выберите тип документа — поля и маски ввода настроятся автоматически",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        DocCategory.entries.forEach { category ->
            val templates = grouped[category] ?: return@forEach
            Spacer(modifier = Modifier.height(20.dp))
            SectionTitle(text = category.label)
            Spacer(modifier = Modifier.height(10.dp))
            templates.forEach { template ->
                TemplateRow(template = template, onClick = { onPick(template.typeKey) })
                Spacer(modifier = Modifier.height(10.dp))
            }
        }

        Spacer(modifier = Modifier.height(36.dp))
    }
}

@Composable
private fun TemplateRow(template: DocTemplate, onClick: () -> Unit) {
    val gradient = CardGradients.of(template.gradientKey)
    val extras = WalletTheme.extra
    val haptics = LocalHapticFeedback.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(extras.glassFill)
            .border(1.dp, extras.glassBorder, RoundedCornerShape(16.dp))
            .clickable {
                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                onClick()
            }
            .padding(horizontal = 14.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(
                    androidx.compose.ui.graphics.Brush.linearGradient(
                        listOf(gradient.start, gradient.middle, gradient.end)
                    )
                )
                .border(1.dp, androidx.compose.ui.graphics.Color.White.copy(alpha = 0.16f), RoundedCornerShape(14.dp)),
            contentAlignment = Alignment.Center
        ) {
            TypeBadge(
                icon = DocIcons.of(template.iconKey),
                accent = gradient.accent,
                size = 28.dp
            )
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = template.title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "Полей: ${template.fields.size}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Box(modifier = Modifier.size(22.dp), contentAlignment = Alignment.Center) {
            Icon(
                imageVector = Icons.Rounded.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}