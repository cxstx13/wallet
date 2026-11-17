package ru.rfdocs.wallet.ui

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.withContext
import ru.rfdocs.wallet.data.model.DocCard
import ru.rfdocs.wallet.data.model.DocCategory
import ru.rfdocs.wallet.data.model.DocTemplates
import ru.rfdocs.wallet.data.repo.DocRepository
import ru.rfdocs.wallet.security.BackupManager

sealed interface Screen {
    data object Home : Screen
    data object TemplatePicker : Screen
    data class Edit(val cardId: String?, val typeKey: String?) : Screen
    data class Detail(val cardId: String) : Screen
    data object Settings : Screen
    data object About : Screen
}

class WalletViewModel(application: Application) : AndroidViewModel(application) {

    private val app: Application = application

    init {
        DocRepository.init(application)
    }

    val cards: StateFlow<List<DocCard>> = DocRepository.cards

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    private val _filter = MutableStateFlow<DocCategory?>(null)
    val filter: StateFlow<DocCategory?> = _filter.asStateFlow()

    val visibleCards: StateFlow<List<DocCard>> =
        combine(DocRepository.cards, _query, _filter) { list, query, filter ->
            val byCategory = if (filter == null) list else list.filter { it.category == filter }
            val term = query.trim().lowercase()
            if (term.isEmpty()) {
                byCategory
            } else {
                byCategory.filter { it.searchHaystack().contains(term) }
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun setQuery(value: String) {
        _query.value = value
    }

    fun setFilter(value: DocCategory?) {
        _filter.value = value
    }

    fun getCard(cardId: String?): DocCard? = if (cardId == null) null else DocRepository.get(cardId)

    fun blankCard(typeKey: String): DocCard? = DocTemplates.createCard(typeKey)

    fun saveCard(card: DocCard) {
        DocRepository.upsert(card)
    }

    fun deleteCard(cardId: String) {
        DocRepository.delete(cardId)
    }

    suspend fun exportBackup(uri: Uri, password: String): String = withContext(Dispatchers.IO) {
        val result = BackupManager.exportTo(app, uri, password)
        result.fold(
            onSuccess = { count -> "Экспортировано карточек: $count" },
            onFailure = { error -> "Ошибка экспорта: ${error.message ?: "неизвестная"}" }
        )
    }

    suspend fun importBackup(uri: Uri, password: String): String = withContext(Dispatchers.IO) {
        val result = BackupManager.importFrom(app, uri, password)
        result.fold(
            onSuccess = { count -> "Импортировано карточек: $count" },
            onFailure = { error -> "Ошибка импорта: ${error.message ?: "неверный пароль"}" }
        )
    }

    fun wipeVault() {
        DocRepository.wipeAll()
    }
}
