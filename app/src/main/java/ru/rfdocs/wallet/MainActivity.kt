package ru.rfdocs.wallet

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.fragment.app.FragmentActivity
import ru.rfdocs.wallet.security.LockManager
import ru.rfdocs.wallet.security.SecurePrefs
import ru.rfdocs.wallet.ui.AppRoot
import ru.rfdocs.wallet.ui.theme.WalletTheme

class MainActivity : FragmentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        applyScreenshotPolicy()
        enableEdgeToEdge()
        setContent {
            WalletTheme {
                AppRoot()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        applyScreenshotPolicy()
    }

    override fun onStop() {
        LockManager.lock()
        super.onStop()
    }

    private fun applyScreenshotPolicy() {
        val blocked = SecurePrefs.isScreenshotBlocked(this)
        if (blocked) {
            window.setFlags(
                WindowManager.LayoutParams.FLAG_SECURE,
                WindowManager.LayoutParams.FLAG_SECURE
            )
        } else {
            window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
        }
    }
}