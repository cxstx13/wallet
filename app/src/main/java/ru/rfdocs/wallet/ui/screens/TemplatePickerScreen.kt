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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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
import ru.rfdocs.wallet.ui.components.WalletBackground
import ru.rfdocs.wallet.ui.components.documentIcon
import ru.rfdocs.wallet.ui.theme.DocumentGradients
import ru.rfdocs.wallet.ui.theme.WalletTheme

@Composable
fun TemplatePickerScreen(
    onPick: (String) -> Unit,
    onBack: () -> Unit
) {
    val grouped = remember { DocTemplates.all.groupBy { it.category } }

    WalletBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .statusBarsPadding()
                .padding(horizontal = 16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Назад"
                    )
                }
                Text(
                    text = "Новый документ",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            Spacer(Modifier.height(8.dp))

            DocCategory.entries.forEach { category ->
                val templates = grouped[category] ?: return@forEach
                Spacer(Modifier.height(18.dp))
                SectionTitle(text = category.label)
                Spacer(Modifier.height(10.dp))
                templates.forEach { template ->
                    TemplateRow(template = template, onClick = { onPick(template.key) })
                    Spacer(Modifier.height(10.dp))
                }
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
private fun TemplateRow(
    template: DocTemplate,
    onClick: () -> Unit
) {
    val gradient = DocumentGradients.forType(template.key)
    val extras = WalletTheme.extra
    val haptics = LocalHapticFeedback.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(extras.glassFill)
            .border(1.dp, extras.glassBorder, RoundedCornerShape(18.dp))
            .clickable {
                haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                onClick()
            }
            .padding(horizontal = 14.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(15.dp))
                .background(Brush.linearGradient(gradient.colors))
                .border(1.dp, Color.White.copy(alpha = 0.14f), RoundedCornerShape(15.dp)),
            contentAlignment = Alignment.Center
        ) {
            TypeBadge(
                icon = documentIcon(template.key),
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
            Spacer(Modifier.height(2.dp))
            Text(
                text = if (template.fields.isEmpty()) {
                    "Произвольные поля"
                } else {
                    "Полей: ${template.fields.size}"
                },
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}