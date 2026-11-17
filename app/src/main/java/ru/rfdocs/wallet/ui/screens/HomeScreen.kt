package ru.rfdocs.wallet.ui.screens

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ru.rfdocs.wallet.ui.WalletViewModel
import ru.rfdocs.wallet.ui.components.CategoryChips
import ru.rfdocs.wallet.ui.components.GlassPanel
import ru.rfdocs.wallet.ui.components.PlasticCard
import ru.rfdocs.wallet.ui.components.WalletSearchField
import ru.rfdocs.wallet.ui.theme.Palette
import ru.rfdocs.wallet.ui.theme.WalletTheme

@Composable
fun HomeScreen(
    viewModel: WalletViewModel,
    onOpenCard: (String) -> Unit,
    onAddCard: () -> Unit,
    onOpenSettings: () -> Unit
) {
    val cards by viewModel.visibleCards.collectAsStateWithLifecycle()
    val query by viewModel.query.collectAsStateWithLifecycle()
    val filter by viewModel.filter.collectAsStateWithLifecycle()
    val allCards by viewModel.cards.collectAsStateWithLifecycle()

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
            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Мои документы",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "Офлайн · ${allCards.size} карточек",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(20.dp))
            WalletSearchField(query = query, onQueryChange = viewModel::setQuery)
            Spacer(modifier = Modifier.height(12.dp))
            CategoryChips(selected = filter, onSelect = viewModel::setFilter)
            Spacer(modifier = Modifier.height(22.dp))

            if (cards.isEmpty()) {
                EmptyState(
                    hasAnyCards = allCards.isNotEmpty(),
                    onAddCard = onAddCard
                )
            } else {
                Column(
                    verticalArrangement = Arrangement.spacedBy(if (cards.size > 1) (-42).dp else 10.dp)
                ) {
                    cards.forEachIndexed { index, card ->
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

            Spacer(modifier = Modifier.height(160.dp))
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
                imageVector = Icons.Rounded.Add,
                contentDescription = null,
                tint = Palette.NightBase,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.size(8.dp))
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
                imageVector = Icons.Rounded.Settings,
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
        Spacer(modifier = Modifier.height(6.dp))
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
            Spacer(modifier = Modifier.height(16.dp))
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