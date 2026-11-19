package ru.rfdocs.wallet.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import ru.rfdocs.wallet.data.model.DocCard
import ru.rfdocs.wallet.data.model.DocField
import ru.rfdocs.wallet.ui.components.DocFieldEditor
import ru.rfdocs.wallet.ui.components.DocumentStyleCard
import ru.rfdocs.wallet.ui.components.PhotoThumbStrip
import ru.rfdocs.wallet.ui.components.SectionTitle
import ru.rfdocs.wallet.ui.components.WalletField
import ru.rfdocs.wallet.ui.theme.Palette

@Composable
fun EditForm(
    title: String,
    onTitle: (String) -> Unit,
    isCustom: Boolean,
    isNew: Boolean,
    preview: DocCard,
    fields: List<DocField>,
    onField: (Int, DocField) -> Unit,
    onRemoveField: (Int) -> Unit,
    onAddField: () -> Unit,
    photoIds: List<String>,
    onAddPhoto: () -> Unit,
    onOpenPhoto: (String) -> Unit,
    onRemovePhoto: (String) -> Unit,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Назад",
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }
            Text(
                text = if (isNew) "Новый документ" else "Редактирование",
                style = MaterialTheme.typography.headlineSmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        DocumentStyleCard(
            card = preview,
            showNumber = true,
            modifier = Modifier.fillMaxWidth()
        )

        if (isCustom) {
            WalletField(label = "Название карточки") {
                BasicTextField(
                    value = title,
                    onValueChange = onTitle,
                    singleLine = true,
                    textStyle = MaterialTheme.typography.bodyLarge.copy(
                        color = MaterialTheme.colorScheme.onSurface
                    ),
                    cursorBrush = SolidColor(Palette.Gold),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        SectionTitle(text = "Поля документа")

        fields.forEachIndexed { index, field ->
            if (isCustom) {
                CustomFieldRow(
                    field = field,
                    onLabelChange = { value -> onField(index, field.copy(label = value)) },
                    onValueChange = { value -> onField(index, field.copy(value = value)) },
                    onRemove = { onRemoveField(index) }
                )
            } else {
                DocFieldEditor(
                    field = field,
                    onFieldChange = { updated -> onField(index, updated) }
                )
            }
        }

        if (isCustom) {
            OutlinedButton(
                onClick = onAddField,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Добавить поле")
            }
        }

        SectionTitle(text = "Фотографии")

        Button(onClick = onAddPhoto) {
            Text("Добавить фото")
        }

        PhotoThumbStrip(
            photoIds = photoIds,
            onOpen = onOpenPhoto,
            onRemove = onRemovePhoto
        )

        Spacer(Modifier.height(24.dp))
    }
}