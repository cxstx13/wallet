package ru.rfdocs.wallet.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ru.rfdocs.wallet.data.model.DocField
import ru.rfdocs.wallet.ui.components.CapsuleButton
import ru.rfdocs.wallet.ui.components.GlassTextField
import ru.rfdocs.wallet.ui.components.PhotoThumbStrip
import ru.rfdocs.wallet.ui.components.SectionTitle

@Composable
fun EditForm(
    title: String,
    onTitleChange: (String) -> Unit,
    fields: List<DocField>,
    onFieldChange: (Int, DocField) -> Unit,
    photoIds: List<String>,
    onAddPhoto: () -> Unit,
    onRemovePhoto: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
    ) {
        GlassTextField(
            label = "Название карточки",
            value = title,
            onValueChange = onTitleChange,
            placeholder = "Например, Паспорт РФ"
        )

        Spacer(Modifier.height(16.dp))
        SectionTitle(text = "Реквизиты")
        Spacer(Modifier.height(10.dp))

        fields.forEachIndexed { index, field ->
            GlassTextField(
                label = field.label,
                value = field.value,
                onValueChange = { value ->
                    onFieldChange(index, field.copy(value = value))
                },
                placeholder = "—",
                singleLine = !field.label.lowercase().contains("адрес")
            )
            Spacer(Modifier.height(12.dp))
        }

        Spacer(Modifier.height(4.dp))
        SectionTitle(text = "Фото документа")
        Spacer(Modifier.height(10.dp))
        CapsuleButton(text = "Добавить фото", filled = false, onClick = onAddPhoto)
        Spacer(Modifier.height(10.dp))
        PhotoThumbStrip(
            photoIds = photoIds,
            onOpen = { },
            onRemove = onRemovePhoto
        )
        Spacer(Modifier.height(24.dp))
    }
}