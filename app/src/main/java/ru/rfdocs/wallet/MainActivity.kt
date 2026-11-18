package ru.rfdocs.wallet

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.WindowManager
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.fragment.app.FragmentActivity
import ru.rfdocs.wallet.security.LockManager
import ru.rfdocs.wallet.security.SecurePrefs
import ru.rfdocs.wallet.ui.AppRoot
import ru.rfdocs.wallet.ui.theme.WalletTheme

class MainActivity : FragmentActivity() {

    private val lockHandler = Handler(Looper.getMainLooper())
    private val lockRunnable = Runnable { LockManager.lock() }

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

    override fun onStart() {
        super.onStart()
        lockHandler.removeCallbacks(lockRunnable)
    }

    override fun onResume() {
        super.onResume()
        lockHandler.removeCallbacks(lockRunnable)
        applyScreenshotPolicy()
    }

    override fun onStop() {
        lockHandler.removeCallbacks(lockRunnable)
        lockHandler.postDelayed(lockRunnable, LOCK_DELAY_MS)
        super.onStop()
    }

    override fun onDestroy() {
        lockHandler.removeCallbacks(lockRunnable)
        super.onDestroy()
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

    private companion object {
        private const val LOCK_DELAY_MS = 15_000L
    }
}