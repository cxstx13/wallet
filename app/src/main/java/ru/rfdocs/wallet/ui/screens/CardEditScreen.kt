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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import ru.rfdocs.wallet.data.model.DocCard
import ru.rfdocs.wallet.data.model.DocField
import ru.rfdocs.wallet.data.model.GradientKeys
import ru.rfdocs.wallet.data.model.applyInputMask
import ru.rfdocs.wallet.ui.WalletViewModel
import ru.rfdocs.wallet.ui.components.SectionTitle
import ru.rfdocs.wallet.ui.theme.CardGradients
import ru.rfdocs.wallet.ui.theme.Palette
import ru.rfdocs.wallet.ui.theme.WalletTheme
import java.util.UUID

private val EXPIRY_LABELS = listOf(
    "Действует по", "Действителен до", "Действительно до", "Срок действия", "Действует до"
)

@Composable
fun CardEditScreen(
    viewModel: WalletViewModel,
    cardId: String?,
    typeKey: String?,
    onBack: () -> Unit,
    onDone: (String) -> Unit
) {
    val initial = remember(cardId, typeKey) {
        viewModel.getCard(cardId) ?: typeKey?.let { viewModel.blankCard(it) }
    }

    if (initial == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                text = "Не удалось открыть карточку",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        return
    }

    var draft by remember { mutableStateOf(initial) }

    fun updateField(fieldId: String, transform: (DocField) -> DocField) {
        draft = draft.copy(
            fields = draft.fields.map { field ->
                if (field.id == fieldId) transform(field) else field
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
        val expiryField = cleaned.firstOrNull { field ->
            EXPIRY_LABELS.any { label -> field.label.contains(label, ignoreCase = true) }
        }
        val phoneField = cleaned.firstOrNull { field ->
            field.label.contains("Телефон", ignoreCase = true)
        }
        val card = draft.copy(
            title = draft.title.trim().ifEmpty { "Документ" },
            subtitle = draft.category.label,
            fields = cleaned,
            validUntil = expiryField?.let { DocCard.normalizeDate(it.value) } ?: "",
            phone = phoneField?.value ?: ""
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
            TextButton(onClick = { save() }) {
                Text(text = "Сохранить", color = Palette.Gold, fontWeight = FontWeight.SemiBold)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))
        OutlinedTextField(
            value = draft.title,
            onValueChange = { draft = draft.copy(title = it) },
            label = { Text("Название карточки") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(18.dp))
        SectionTitle(text = "Цвет карточки")
        Spacer(modifier = Modifier.height(8.dp))
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
                        .size(if (selected) 42.dp else 34.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                listOf(gradient.start, gradient.middle, gradient.end)
                            )
                        )
                        .border(
                            width = if (selected) 2.dp else 1.dp,
                            color = if (selected) Palette.Gold else Color.White.copy(alpha = 0.35f),
                            shape = CircleShape
                        )
                        .clickable { draft = draft.copy(gradientKey = key) }
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
        SectionTitle(text = "Поля документа")
        Spacer(modifier = Modifier.height(10.dp))

        draft.fields.forEach { field ->
            FieldEditor(
                field = field,
                editableLabel = true,
                onLabelChange = { newLabel ->
                    updateField(field.id) { it.copy(label = newLabel) }
                },
                onValueChange = { newValue ->
                    updateField(field.id) {
                        it.copy(value = applyInputMask(newValue, it.inputMask))
                    }
                },
                onRemove = {
                    draft = draft.copy(fields = draft.fields.filterNot { it.id == field.id })
                }
            )
            Spacer(modifier = Modifier.height(10.dp))
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(WalletTheme.extra.glassFill)
                .border(1.dp, WalletTheme.extra.glassBorder, RoundedCornerShape(16.dp))
                .clickable {
                    draft = draft.copy(
                        fields = draft.fields + DocField(
                            id = UUID.randomUUID().toString(),
                            label = "Новое поле",
                            placeholder = "Значение"
                        )
                    )
                }
                .padding(horizontal = 14.dp, vertical = 12.dp),
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

        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = draft.note,
            onValueChange = { draft = draft.copy(note = it) },
            label = { Text("Заметка (необязательно)") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 2
        )

        Spacer(modifier = Modifier.height(40.dp))
    }
}

@Composable
private fun FieldEditor(
    field: DocField,
    editableLabel: Boolean,
    onLabelChange: (String) -> Unit,
    onValueChange: (String) -> Unit,
    onRemove: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(WalletTheme.extra.glassFill)
            .border(1.dp, WalletTheme.extra.glassBorder, RoundedCornerShape(18.dp))
            .padding(12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = field.label,
                onValueChange = onLabelChange,
                label = { Text("Название поля") },
                singleLine = true,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = onRemove) {
                Icon(
                    imageVector = Icons.Rounded.Close,
                    contentDescription = "Удалить поле",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = field.value,
            onValueChange = onValueChange,
            label = { Text(if (field.placeholder.isBlank()) "Значение" else field.placeholder) },
            singleLine = !field.multiline,
            minLines = if (field.multiline) 3 else 1,
            keyboardOptions = if (field.inputMask.isNotBlank()) {
                KeyboardOptions(keyboardType = KeyboardType.Number)
            } else {
                KeyboardOptions.Default
            },
            visualTransformation = androidx.compose.ui.text.input.VisualTransformation.None,
            modifier = Modifier.fillMaxWidth()
        )
        if (field.inputMask.isNotBlank()) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Маска ввода: ${field.inputMask}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
