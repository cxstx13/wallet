package ru.rfdocs.wallet.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import ru.rfdocs.wallet.data.repo.DocRepository

sealed interface Screen {
    data object Home : Screen
    data object TemplatePicker : Screen
    data class Edit(val cardId: String?, val typeKey: String?) : Screen
    data class Detail(val cardId: String) : Screen
    data object Settings : Screen
    data object About : Screen
}

class WalletViewModel(application: Application) : AndroidViewModel(application) {

    init {
        DocRepository.init(application)
    }
}