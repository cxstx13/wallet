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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Icon
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ru.rfdocs.wallet.data.model.DocCard
import ru.rfdocs.wallet.data.model.DocCategory
import ru.rfdocs.wallet.data.repo.DocRepository
import ru.rfdocs.wallet.ui.components.DocCardView
import ru.rfdocs.wallet.ui.components.WalletBackground
import ru.rfdocs.wallet.ui.theme.WalletTheme

@Composable
fun HomeScreen(
    onOpenCard: (String) -> Unit,
    onAddCard: () -> Unit,
    onOpenSettings: () -> Unit
) {
    val cards by DocRepository.cards.collectAsState()
    var query by remember { mutableStateOf("") }
    var filter by remember { mutableStateOf<DocCategory?>(null) }
    var chipsVisible by remember { mutableStateOf(true) }
    val focusRequester = remember { FocusRequester() }

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
            Spacer(Modifier.height(18.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Документы",
                        style = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "Ваши документы всегда под рукой",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                RoundIconButton(
                    icon = Icons.Filled.Search,
                    description = "Поиск",
                    onClick = { runCatching { focusRequester.requestFocus() } }
                )
                Spacer(Modifier.width(8.dp))
                RoundIconButton(
                    icon = Icons.Filled.Settings,
                    description = "Настройки",
                    onClick = onOpenSettings
                )
            }

            Spacer(Modifier.height(18.dp))

            WalletSearchField(
                query = query,
                onQueryChange = { query = it },
                focusRequester = focusRequester,
                onFilterClick = { chipsVisible = !chipsVisible }
            )

            if (chipsVisible) {
                Spacer(Modifier.height(12.dp))
                CategoryChips(selected = filter, onSelect = { filter = it })
            }

            Spacer(Modifier.height(20.dp))

            if (visible.isEmpty()) {
                EmptyState(hasCards = cards.isNotEmpty())
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    visible.forEach { card ->
                        DocCardView(card = card, onClick = { onOpenCard(card.id) })
                    }
                }
            }

            Spacer(Modifier.height(150.dp))
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

@Composable
private fun RoundIconButton(
    icon: ImageVector,
    description: String,
    onClick: () -> Unit
) {
    val extras = WalletTheme.extra
    val shape = RoundedCornerShape(14.dp)
    Box(
        modifier = Modifier
            .size(42.dp)
            .clip(shape)
            .background(extras.glassFill)
            .border(1.dp, extras.glassBorder, shape)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = description,
            tint = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
private fun WalletSearchField(
    query: String,
    onQueryChange: (String) -> Unit,
    focusRequester: FocusRequester,
    onFilterClick: () -> Unit
) {
    val extras = WalletTheme.extra
    val shape = RoundedCornerShape(20.dp)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(extras.glassFill)
            .border(1.dp, extras.glassBorder, shape)
            .padding(start = 14.dp, end = 6.dp, top = 4.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Filled.Search,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(20.dp)
        )
        Spacer(Modifier.width(10.dp))
        Box(
            modifier = Modifier
                .weight(1f)
                .padding(vertical = 12.dp)
        ) {
            if (query.isEmpty()) {
                Text(
                    text = "Поиск по документам",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            BasicTextField(
                value = query,
                onValueChange = onQueryChange,
                singleLine = true,
                textStyle = MaterialTheme.typography.bodyMedium.copy(
                    color = MaterialTheme.colorScheme.onSurface
                ),
                cursorBrush = SolidColor(extras.monoAccent),
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester)
            )
        }
        Spacer(Modifier.width(8.dp))
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(RoundedCornerShape(13.dp))
                .background(extras.chipFill)
                .clickable { onFilterClick() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.Tune,
                contentDescription = "Фильтры",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
private fun CategoryChips(
    selected: DocCategory?,
    onSelect: (DocCategory?) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        FilterPill(text = "Все", selected = selected == null) { onSelect(null) }
        DocCategory.entries.forEach { category ->
            FilterPill(text = category.label, selected = selected == category) {
                onSelect(if (selected == category) null else category)
            }
        }
    }
}

@Composable
private fun FilterPill(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val extras = WalletTheme.extra
    val shape = RoundedCornerShape(50)

    Text(
        text = text,
        style = MaterialTheme.typography.labelLarge,
        color = if (selected) extras.chipSelectedText else MaterialTheme.colorScheme.onSurfaceVariant,
        maxLines = 1,
        modifier = Modifier
            .clip(shape)
            .background(if (selected) extras.chipSelectedFill else extras.chipFill)
            .border(1.dp, if (selected) Color.Transparent else extras.pillStroke, shape)
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 9.dp)
    )
}

@Composable
private fun EmptyState(hasCards: Boolean) {
    val extras = WalletTheme.extra
    val shape = RoundedCornerShape(20.dp)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(extras.glassFill)
            .border(1.dp, extras.glassBorder, shape)
            .padding(horizontal = 20.dp, vertical = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = if (hasCards) "Ничего не найдено" else "Пока нет документов",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = if (hasCards) {
                "Попробуйте изменить запрос"
            } else {
                "Нажмите +, чтобы добавить первый документ"
            },
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

private fun categoryOf(card: DocCard): DocCategory {
    val key = card.typeKey.lowercase()
    return when {
        key.contains("osago") || key.contains("kasko") -> DocCategory.AUTO
        key.contains("driver") || key.contains("vu") -> DocCategory.AUTO
        key.contains("sts") || key.contains("pts") -> DocCategory.AUTO
        key.contains("oms") || key.contains("dms") -> DocCategory.MEDICINE
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