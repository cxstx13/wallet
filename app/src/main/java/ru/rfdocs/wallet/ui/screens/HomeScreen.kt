package ru.rfdocs.wallet.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Settings
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import ru.rfdocs.wallet.data.model.DocCard
import ru.rfdocs.wallet.data.model.DocCategory
import ru.rfdocs.wallet.data.repo.DocRepository
import ru.rfdocs.wallet.ui.components.CategoryChips
import ru.rfdocs.wallet.ui.components.DocumentStyleCard
import ru.rfdocs.wallet.ui.components.GlassPanel
import ru.rfdocs.wallet.ui.components.WalletBackground
import ru.rfdocs.wallet.ui.components.WalletSearchField
import ru.rfdocs.wallet.ui.theme.Palette
import ru.rfdocs.wallet.ui.theme.WalletTheme

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

            Spacer(Modifier.height(18.dp))
            WalletSearchField(query = query, onQueryChange = { query = it })
            Spacer(Modifier.height(12.dp))
            CategoryChips(selected = filter, onSelect = { filter = it })
            Spacer(Modifier.height(24.dp))

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
                            "Добавьте первый документ — он останется на устройстве"
                        } else {
                            "Измените запрос или категорию"
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                Column(
                    verticalArrangement = Arrangement.spacedBy(
                        if (visible.size > 1) (-24).dp else 14.dp
                    )
                ) {
                    visible.forEachIndexed { index, card ->
                        CardInStack(
                            card = card,
                            index = index,
                            onClick = { onOpenCard(card.id) }
                        )
                    }
                }
            }

            Spacer(Modifier.height(160.dp))
        }

        BottomBar(
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
private fun CardInStack(
    card: DocCard,
    index: Int,
    onClick: () -> Unit
) {
    val appear by animateFloatAsState(
        targetValue = 1f,
        animationSpec = spring(dampingRatio = 0.8f, stiffness = 300f),
        label = "cardAppear"
    )
    DocumentStyleCard(
        card = card,
        compact = true,
        modifier = Modifier
            .fillMaxWidth()
            .zIndex(index.toFloat())
            .graphicsLayer {
                alpha = appear
                translationY = (1f - appear) * 40f
            },
        onClick = onClick
    )
}

@Composable
private fun BottomBar(
    onAddCard: () -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val extras = WalletTheme.extra

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(CircleShape)
            .background(extras.glassFill)
            .border(1.dp, extras.glassBorder, CircleShape)
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier
                .weight(1f)
                .clip(CircleShape)
                .background(Palette.Gold)
                .clickable { onAddCard() }
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
            Text(
                text = "  Добавить",
                style = MaterialTheme.typography.labelLarge,
                color = Palette.NightBase
            )
        }
        Spacer(Modifier.size(8.dp))
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .clickable { onOpenSettings() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.Settings,
                contentDescription = "Настройки",
                tint = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.size(21.dp)
            )
        }
    }
}