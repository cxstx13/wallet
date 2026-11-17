package ru.rfdocs.wallet.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ru.rfdocs.wallet.data.model.DocCard
import ru.rfdocs.wallet.data.model.DocCategory
import ru.rfdocs.wallet.data.model.DocField
import ru.rfdocs.wallet.data.model.FieldKind
import ru.rfdocs.wallet.data.model.GradientKeys
import ru.rfdocs.wallet.ui.WalletViewModel
import ru.rfdocs.wallet.ui.components.DocFieldEditor
import ru.rfdocs.wallet.ui.components.FieldLabel
import ru.rfdocs.wallet.ui.components.GlassPanel
import ru.rfdocs.wallet.ui.components.SectionTitle
import ru.rfdocs.wallet.ui.components.WalletField
import ru.rfdocs.wallet.ui.input.DateMask
import ru.rfdocs.wallet.ui.theme.CardGradients
import ru.rfdocs.wallet.ui.theme.Palette
import ru.rfdocs.wallet.ui.theme.WalletTheme
import java.util.UUID

private val EXPIRY_LABELS = listOf(
    "Действует по",
    "Действителен до",
    "Действительно до"
)

@Composable
fun CardEditScreen(
    viewModel: WalletViewModel,
    cardId: String?,
    typeKey: String?,
    onBack: () -> Unit,
    onDone: (String) -> Unit
) {
    val haptics = LocalHapticFeedback.current
    val loaded = remember(cardId, typeKey) {
        viewModel.getCard(cardId) ?: typeKey?.let { viewModel.blankCard(it) }
    }
    val startCard: DocCard = loaded ?: DocCard(
        typeKey = typeKey ?: "",
        title = "Документ",
        category = DocCategory.OTHER
    )
    var draft by remember { mutableStateOf(startCard) }

    val isCustom = draft.typeKey == "CUSTOM"

    fun updateField(fieldId: String, updated: DocField) {
        draft = draft.copy(
            fields = draft.fields.map { field ->
                if (field.id == fieldId) updated else field
            }
        )
    }

    fun save() {
        val cleaned = draft.fields.mapNotNull { field ->
            val label = field.label.trim()
            val value = field.value.trim()
            if (label.isEmpty() && value.isEmpty()) {
                null
            } else {
                field.copy(label = label.ifEmpty { "Поле" }, value = value)
            }
        }
        val expiry = EXPIRY_LABELS.firstNotNullOfOrNull { label ->
            cleaned.firstOrNull { it.label.contains(label, ignoreCase = true) }
                ?.let { DateMask.toIso(it.value) }
        }.orEmpty()
        val phoneValue = cleaned.firstOrNull { field ->
            field.kind == FieldKind.PHONE || field.label.contains("Телефон", ignoreCase = true)
        }?.value.orEmpty()

        val card = draft.copy(
            title = draft.title.trim().ifEmpty { "Документ" },
            subtitle = draft.category.label,
            fields = cleaned,
            validUntil = expiry,
            phone = phoneValue
        )
        viewModel.saveCard(card)
        onDone(card.id)
    }

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
                text = if (cardId == null) "Новый документ" else "Редактирование",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.weight(1f)
            )
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(WalletTheme.extra.chipSelectedFill)
                    .clickable {
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        save()
                    }
                    .padding(horizontal = 18.dp, vertical = 10.dp)
            ) {
                Text(
                    text = "Сохранить",
                    style = MaterialTheme.typography.labelLarge,
                    color = WalletTheme.extra.chipSelectedText,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        if (isCustom) {
            DocFieldEditor(
                field = DocField(
                    label = "Название карточки",
                    value = draft.title,
                    kind = FieldKind.TEXT,
                    placeholder = "Например, пропуск на работу",
                    capitalizeWords = true
                ),
                onFieldChange = { updated -> draft = draft.copy(title = updated.value) }
            )
        } else {
            GlassPanel(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = draft.title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = draft.category.label,
                    style = MaterialTheme.typography.labelSmall,
                    color = WalletTheme.extra.fieldLabel
                )
            }
        }

        Spacer(modifier = Modifier.height(22.dp))
        SectionTitle(text = "Цвет карточки")
        Spacer(modifier = Modifier.height(10.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            GradientKeys.all.forEach { key ->
                val gradient = CardGradients.of(key)
                val selected = draft.gradientKey == key
                Box(
                    modifier = Modifier
                        .size(if (selected) 44.dp else 34.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                listOf(gradient.start, gradient.middle, gradient.end)
                            )
                        )
                        .border(
                            width = if (selected) 2.dp else 1.dp,
                            color = if (selected) Palette.Gold else Color.White.copy(alpha = 0.28f),
                            shape = CircleShape
                        )
                        .clickable {
                            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                            draft = draft.copy(gradientKey = key)
                        }
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        SectionTitle(text = "Данные документа")
        Spacer(modifier = Modifier.height(12.dp))

        draft.fields.forEach { field ->
            Column(modifier = Modifier.fillMaxWidth()) {
                if (field.editableLabel || isCustom) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.weight(1f)) {
                            LabelEditor(
                                value = field.label,
                                onValueChange = { newLabel ->
                                    updateField(field.id, field.copy(label = newLabel))
                                }
                            )
                        }
                        IconButton(
                            onClick = {
                                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                draft = draft.copy(fields = draft.fields.filterNot { it.id == field.id })
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Close,
                                contentDescription = "Удалить поле",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                }

                DocFieldEditor(
                    field = field,
                    onFieldChange = { updated -> updateField(field.id, updated) }
                )

                if (field.kind == FieldKind.DATE &&
                    field.value.length == DateMask.MAX_DIGITS &&
                    !DateMask.isValid(field.value)
                ) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Проверьте дату",
                        style = MaterialTheme.typography.labelSmall,
                        color = Palette.Danger
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }

        if (isCustom) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(WalletTheme.extra.fieldFill)
                    .border(1.dp, WalletTheme.extra.fieldStroke, RoundedCornerShape(14.dp))
                    .clickable {
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        draft = draft.copy(
                            fields = draft.fields + DocField(
                                id = UUID.randomUUID().toString(),
                                label = "Новое поле",
                                kind = FieldKind.TEXT,
                                placeholder = "Значение",
                                editableLabel = true
                            )
                        )
                    }
                    .padding(horizontal = 14.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Add,
                    contentDescription = null,
                    tint = Palette.Gold,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = "Добавить поле",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Spacer(modifier = Modifier.height(22.dp))
        }

        SectionTitle(text = "Заметка")
        Spacer(modifier = Modifier.height(10.dp))
        DocFieldEditor(
            field = DocField(
                label = "Личная заметка",
                value = draft.note,
                kind = FieldKind.MULTILINE,
                multiline = true,
                placeholder = "Необязательно"
            ),
            onFieldChange = { updated -> draft = draft.copy(note = updated.value) }
        )

        Spacer(modifier = Modifier.height(48.dp))
    }
}

@Composable
private fun LabelEditor(
    value: String,
    onValueChange: (String) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        FieldLabel(text = "Название поля")
        Spacer(modifier = Modifier.height(4.dp))
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = true,
            textStyle = MaterialTheme.typography.bodyMedium.copy(
                color = MaterialTheme.colorScheme.onSurface
            ),
            cursorBrush = SolidColor(Palette.Gold),
            modifier = Modifier.fillMaxWidth()
        )
    }
}