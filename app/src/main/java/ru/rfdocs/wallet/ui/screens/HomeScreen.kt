package ru.rfdocs.wallet.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ru.rfdocs.wallet.data.model.DocCard
import ru.rfdocs.wallet.data.model.DocCategory
import ru.rfdocs.wallet.data.repo.DocRepository
import ru.rfdocs.wallet.ui.components.CategoryChips
import ru.rfdocs.wallet.ui.components.DocumentRow
import ru.rfdocs.wallet.ui.components.GlassPanel
import ru.rfdocs.wallet.ui.components.WalletBackground
import ru.rfdocs.wallet.ui.components.WalletSearchField

private fun categoryOf(card: DocCard): DocCategory {
    val key = card.typeKey.lowercase()
    return when {
        key.contains("osago") || key.contains("kasko") -> DocCategory.MEDICINE
        key.contains("oms") || key.contains("dms") -> DocCategory.MEDICINE
        key.contains("driver") || key.contains("vu") -> DocCategory.AUTO
        key.contains("sts") || key.contains("pts") -> DocCategory.AUTO
        key.contains("birth") -> DocCategory.FAMILY
        key.contains("passport") || key.contains("zagran") -> DocCategory.IDENTITY
        key.contains("snils") || key.contains("inn") -> DocCategory.IDENTITY
        else -> DocCategory.OTHER
    }
}

private fun matches(card: DocCard, query: String): Boolean {
    val term = query.trim().lowercase()
    if (term.isEmpty()) return true
    if (card.title.lowercase().contains(term)) return true
    return card.fields.any { it.value.lowercase().contains(term) }
}

@Composable
fun HomeScreen(
    onOpenCard: (String) -> Unit,
    onAddCard: () -> Unit,
    onOpenSettings: () -> Unit
) {
    val cards by DocRepository.cards.collectAsState()
    var query by remember { mutableStateOf("") }
    var filter by remember { mutableStateOf<DocCategory?>(null) }

    val visible = remember(cards, query, filter) {
        cards
            .filter { filter == null || categoryOf(it) == filter }
            .filter { matches(it, query) }
            .sortedByDescending { it.createdAt }
    }

    WalletBackground {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .statusBarsPadding()
                .padding(horizontal = 20.dp)
        ) {
            Spacer(Modifier.height(24.dp))
            Text(
                text = "Документы",
                style = MaterialTheme.typography.displaySmall,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "Всегда под рукой",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(18.dp))
            WalletSearchField(query = query, onQueryChange = { query = it })
            Spacer(Modifier.height(12.dp))
            CategoryChips(selected = filter, onSelect = { filter = it })
            Spacer(Modifier.height(20.dp))

            if (visible.isEmpty()) {
                GlassPanel(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = if (cards.isEmpty()) "Пока нет документов" else "Ничего не найдено",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = if (cards.isEmpty()) {
                            "Добавьте первый документ"
                        } else {
                            "Измените запрос или категорию"
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    visible.forEach { card ->
                        DocumentRow(card = card, onClick = { onOpenCard(card.id) })
                    }
                }
            }

            Spacer(Modifier.height(160.dp))
        }

        HomeBottomBar(
            onAdd = onAddCard,
            onSettings = onOpenSettings,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(horizontal = 20.dp, vertical = 16.dp)
        )
    }
}