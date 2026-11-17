package ru.rfdocs.wallet

import android.app.Application
import android.content.Context

class WalletApp : Application() {

    override fun onCreate() {
        super.onCreate()
        appContext = applicationContext
    }

    companion object {
        lateinit var appContext: Context
            private set
    }
}
