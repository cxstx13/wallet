package ru.rfdocs.wallet.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import ru.rfdocs.wallet.data.model.DocCard
import ru.rfdocs.wallet.data.model.DocCategory
import ru.rfdocs.wallet.data.repo.DocRepository
import ru.rfdocs.wallet.ui.components.CategoryChips
import ru.rfdocs.wallet.ui.components.GlassPanel
import ru.rfdocs.wallet.ui.components.PlasticCard
import ru.rfdocs.wallet.ui.components.WalletSearchField
import ru.rfdocs.wallet.ui.theme.Palette
import ru.rfdocs.wallet.ui.theme.WalletTheme

private enum class SortMode(val label: String) {
    DATE("По дате добавления"),
    CATEGORY("По категории"),
    TITLE("По названию")
}

private fun categoryOf(card: DocCard): DocCategory {
    val key = card.typeKey.lowercase()
    return when {
        key.contains("osago") || key.contains("kasko") ||
            key.contains("oms") || key.contains("dms") -> DocCategory.MEDICINE
        key.contains("driver") || key.contains("vu") || key.contains("sts") ||
            key.contains("pts") -> DocCategory.AUTO
        key.contains("birth") || key.contains("family") -> DocCategory.FAMILY
        key.contains("passport") || key.contains("zagran") ||
            key.contains("snils") || key.contains("inn") -> DocCategory.IDENTITY
        else -> DocCategory.OTHER
    }
}

private fun matchesQuery(card: DocCard, query: String): Boolean {
    val term = query.trim().lowercase()
    if (term.isEmpty()) return true
    if (card.title.lowercase().contains(term)) return true
    return card.fields.any { field -> field.value.lowercase().contains(term) }
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
    var sortMode by remember { mutableStateOf(SortMode.DATE) }
    var sortMenu by remember { mutableStateOf(false) }
    var revealed by remember { mutableStateOf(false) }

    val visible = remember(cards, query, filter, sortMode) {
        val filtered = cards.filter { card ->
            (filter == null || categoryOf(card) == filter) && matchesQuery(card, query)
        }
        when (sortMode) {
            SortMode.DATE -> filtered.sortedByDescending { it.createdAt }
            SortMode.CATEGORY -> filtered.sortedWith(
                compareBy({ categoryOf(it).label }, { it.title })
            )

            SortMode.TITLE -> filtered.sortedBy { it.title }
        }
    }

    LaunchedEffect(Unit) { revealed = true }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .statusBarsPadding()
                .padding(horizontal = 20.dp)
        ) {
            Spacer(Modifier.height(20.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Мои документы",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = "Офлайн · ${cards.size} карточек · ${sortMode.label.lowercase()}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Box {
                    IconButton(onClick = { sortMenu = true }) {
                        Icon(
                            imageVector = Icons.Filled.List,
                            contentDescription = "Сортировка",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    DropdownMenu(
                        expanded = sortMenu,
                        onDismissRequest = { sortMenu = false }
                    ) {
                        SortMode.entries.forEach { mode ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = mode.label,
                                        color = if (mode == sortMode) {
                                            WalletTheme.extra.monoAccent
                                        } else {
                                            MaterialTheme.colorScheme.onSurface
                                        }
                                    )
                                },
                                onClick = {
                                    sortMode = mode
                                    sortMenu = false
                                }
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(20.dp))
            WalletSearchField(query = query, onQueryChange = { query = it })
            Spacer(Modifier.height(12.dp))
            CategoryChips(selected = filter, onSelect = { filter = it })
            Spacer(Modifier.height(24.dp))

            AnimatedVisibility(
                visible = revealed,
                enter = fadeIn(tween(260)) + slideInVertically(tween(260)) { height -> height / 10 }
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    if (visible.isEmpty()) {
                        EmptyState(
                            hasAnyCards = cards.isNotEmpty(),
                            onAddCard = onAddCard
                        )
                    } else {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(
                                if (visible.size > 1) (-24).dp else 14.dp
                            )
                        ) {
                            visible.forEachIndexed { index, card ->
                                PlasticCard(
                                    card = card,
                                    compact = true,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .zIndex(index.toFloat()),
                                    onClick = { onOpenCard(card.id) }
                                )
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(160.dp))
        }

        HomeBottomBar(
            onAddCard = onAddCard,
            onOpenSettings = onOpenSettings,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(horizontal = 20.dp, vertical = 16.dp)
        )
    }
}

@Composable
private fun HomeBottomBar(
    onAddCard: () -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val extras = WalletTheme.extra
    val haptics = LocalHapticFeedback.current

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(CircleShape)
            .background(extras.glassFill)
            .border(1.dp, extras.glassBorder, CircleShape)
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier
                .weight(1f)
                .clip(CircleShape)
                .background(Palette.Gold)
                .clickable {
                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    onAddCard()
                }
                .padding(vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Filled.Add,
                contentDescription = null,
                tint = Palette.NightBase,
                modifier = Modifier.size(20.dp)
            )
            Spacer(Modifier.size(8.dp))
            Text(
                text = "Добавить документ",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = Palette.NightBase
            )
        }

        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(extras.pillFill)
                .border(1.dp, extras.pillStroke, CircleShape)
                .clickable {
                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    onOpenSettings()
                },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.Settings,
                contentDescription = "Настройки",
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(21.dp)
            )
        }
    }
}

@Composable
private fun EmptyState(
    hasAnyCards: Boolean,
    onAddCard: () -> Unit
) {
    val extras = WalletTheme.extra
    val haptics = LocalHapticFeedback.current

    GlassPanel(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = if (hasAnyCards) "Ничего не найдено" else "Пока нет документов",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = if (hasAnyCards) {
                "Измените запрос или выберите другую категорию"
            } else {
                "Добавьте паспорт, СНИЛС, полисы и другие документы — всё хранится только на устройстве"
            },
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        if (!hasAnyCards) {
            Spacer(Modifier.height(16.dp))
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(extras.chipSelectedFill)
                    .clickable {
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        onAddCard()
                    }
                    .padding(horizontal = 18.dp, vertical = 11.dp)
            ) {
                Text(
                    text = "Добавить документ",
                    style = MaterialTheme.typography.labelLarge,
                    color = extras.chipSelectedText
                )
            }
        }
    }
}