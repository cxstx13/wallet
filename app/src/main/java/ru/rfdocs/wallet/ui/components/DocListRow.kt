package ru.rfdocs.wallet.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import ru.rfdocs.wallet.data.model.DocCard

@Composable
fun DocListRow(
    card: DocCard,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    DocCardView(card = card, onClick = onClick, modifier = modifier)
}