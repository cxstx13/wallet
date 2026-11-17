package ru.rfdocs.wallet

import android.os.Build
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
        applyScreenshotProtection()
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

    override fun onResume() {
        super.onResume()
        applyScreenshotProtection()
    }

    override fun onStop() {
        LockManager.lock()
        super.onStop()
    }

    private fun applyScreenshotProtection() {
        if (SecurePrefs.isScreenshotProtected(this)) {
            window.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
        } else {
            window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
        }
    }
}