package ru.rfdocs.wallet.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
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
import ru.rfdocs.wallet.ui.theme.DocumentGradients
import ru.rfdocs.wallet.ui.theme.Palette
import ru.rfdocs.wallet.ui.theme.WalletTheme

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

    val visible = remember(cards, query, filter) {
        cards.filter { card ->
            (filter == null || categoryOf(card) == filter) && matchesQuery(card, query)
        }.sortedByDescending { it.createdAt }
    }

    val isDark = WalletTheme.extra.isDark

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DocumentGradients.screenBrush(isDark))
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                brush = DocumentGradients.glowBrush(isDark),
                radius = size.minDimension * 0.85f,
                center = Offset(size.width * 0.85f, size.height * 0.04f)
            )
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Palette.Mint.copy(alpha = if (isDark) 0.10f else 0.14f),
                        androidx.compose.ui.graphics.Color.Transparent
                    )
                ),
                radius = size.minDimension * 0.7f,
                center = Offset(size.width * 0.05f, size.height * 0.45f)
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .statusBarsPadding()
                .padding(horizontal = 20.dp)
        ) {
            Spacer(Modifier.height(24.dp))

            Text(
                text = "Документы",
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(Modifier.height(18.dp))
            WalletSearchField(query = query, onQueryChange = { query = it })
            Spacer(Modifier.height(12.dp))
            CategoryChips(selected = filter, onSelect = { filter = it })
            Spacer(Modifier.height(24.dp))

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
                        AnimatedVisibility(
                            visible = true,
                            enter = fadeIn(
                                animationSpec = tween(
                                    durationMillis = 280,
                                    delayMillis = index * 70
                                )
                            ) + slideInVertically(
                                animationSpec = tween(
                                    durationMillis = 320,
                                    delayMillis = index * 70
                                )
                            ) { height -> height / 3 }
                        ) {
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
                    haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
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
                text = "Добавить",
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
                    haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
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
                "Добавьте паспорт, СНИЛС, полисы и другие документы"
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
                        haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
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