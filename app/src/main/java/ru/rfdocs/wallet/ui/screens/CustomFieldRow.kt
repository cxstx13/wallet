package ru.rfdocs.wallet.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ru.rfdocs.wallet.ui.components.GlassTextField

@Composable
fun CustomFieldRow(
    label: String,
    value: String,
    onLabelChange: (String) -> Unit,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        GlassTextField(
            label = "Название поля",
            value = label,
            onValueChange = onLabelChange,
            placeholder = "Например, Логин"
        )
        Spacer(Modifier.height(8.dp))
        GlassTextField(
            label = "Значение",
            value = value,
            onValueChange = onValueChange,
            placeholder = "—"
        )
    }
}