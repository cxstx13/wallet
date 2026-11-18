package ru.rfdocs.wallet

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.fragment.app.FragmentActivity
import ru.rfdocs.wallet.security.LockManager
import ru.rfdocs.wallet.ui.AppRoot
import ru.rfdocs.wallet.ui.theme.WalletTheme

class MainActivity : FragmentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        window.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            WalletTheme {
                AppRoot()
            }
        }
    }

    override fun onStop() {
        LockManager.lock()
        super.onStop()
    }
}