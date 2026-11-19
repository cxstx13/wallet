package ru.rfdocs.wallet.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import ru.rfdocs.wallet.data.repo.DocRepository

class WalletViewModel(application: Application) : AndroidViewModel(application) {

    init {
        DocRepository.init(application)
    }
}