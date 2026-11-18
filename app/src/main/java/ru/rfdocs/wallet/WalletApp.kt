package ru.rfdocs.wallet

import android.app.Application
import ru.rfdocs.wallet.data.repo.DocRepository

class WalletApp : Application() {

    override fun onCreate() {
        super.onCreate()
        DocRepository.init(this)
    }
}