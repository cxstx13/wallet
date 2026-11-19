package ru.rfdocs.wallet.ui

import ru.rfdocs.wallet.data.repo.DocRepository

/**
 * Единственная точка обращения к репозиторию из UI-слоя.
 * Если в DocRepository уже есть метод удаления одной карточки —
 * замените тело на его вызов.
 */
fun deleteCard(cardId: String) {
    val snapshot = DocRepository.cards.value.filterNot { it.id == cardId }
    DocRepository.replaceAll(snapshot)
}