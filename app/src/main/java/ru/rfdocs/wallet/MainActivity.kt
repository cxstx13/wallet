package ru.rfdocs.wallet

import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.fragment.app.FragmentActivity
import ru.rfdocs.wallet.security.LockManager
import ru.rfdocs.wallet.ui.AppRoot
import ru.rfdocs.wallet.ui.theme.WalletTheme

class MainActivity : FragmentActivity() {

    private var backgroundAt = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        window.setFlags(
            WindowManager.LayoutParams.FLAG_SECURE,
            WindowManager.LayoutParams.FLAG_SECURE
        )
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            window.isNavigationBarContrastEnforced = false
            window.isStatusBarContrastEnforced = false
        }
        setContent {
            WalletTheme {
                AppRoot()
            }
        }
    }

    override fun onStop() {
        backgroundAt = System.currentTimeMillis()
        super.onStop()
    }

    override fun onStart() {
        super.onStart()
        if (backgroundAt != 0L) {
            val away = System.currentTimeMillis() - backgroundAt
            backgroundAt = 0L
            if (away > LOCK_AFTER_MS) {
                LockManager.lock()
            }
        }
    }

    companion object {
        private const val LOCK_AFTER_MS = 20_000L
    }
}