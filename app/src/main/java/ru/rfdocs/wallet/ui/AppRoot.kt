package ru.rfdocs.wallet.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import ru.rfdocs.wallet.security.LockManager
import ru.rfdocs.wallet.security.PinVault
import ru.rfdocs.wallet.security.SecurePrefs
import ru.rfdocs.wallet.ui.screens.AboutScreen
import ru.rfdocs.wallet.ui.screens.CardDetailScreen
import ru.rfdocs.wallet.ui.screens.CardEditScreen
import ru.rfdocs.wallet.ui.screens.HomeScreen
import ru.rfdocs.wallet.ui.screens.SettingsScreen
import ru.rfdocs.wallet.ui.screens.TemplatePickerScreen
import ru.rfdocs.wallet.ui.theme.WalletTheme

@Composable
fun AppRoot(viewModel: WalletViewModel = viewModel()) {
    val context = LocalContext.current
    var darkOverride by remember { mutableStateOf(SecurePrefs.darkTheme(context)) }
    val isDark = darkOverride ?: isSystemInDarkTheme()

    WalletTheme(darkTheme = isDark) {
        var pinConfigured by remember { mutableStateOf(PinVault.isPinSet(context)) }
        val locked by LockManager.isLocked.collectAsStateWithLifecycle()
        var screen by remember { mutableStateOf<Screen>(Screen.Home) }

        if (!pinConfigured) {
            PinSetupScreen(
                title = "Придумайте PIN-код",
                subtitle = "Код нужен для входа в приложение и хранится только на устройстве",
                onComplete = { pin ->
                    PinVault.setPin(context, pin)
                    SecurePrefs.setOnboarded(context, true)
                    pinConfigured = true
                    LockManager.unlock()
                }
            )
            return@WalletTheme
        }

        if (locked) {
            LockScreen(
                onUnlocked = { LockManager.unlock() },
                onVaultReset = {
                    pinConfigured = false
                    screen = Screen.Home
                }
            )
            return@WalletTheme
        }

        when (val current = screen) {
            is Screen.Home -> {
                HomeScreen(
                    viewModel = viewModel,
                    onOpenCard = { cardId -> screen = Screen.Detail(cardId) },
                    onAddCard = { screen = Screen.TemplatePicker },
                    onOpenSettings = { screen = Screen.Settings }
                )
            }

            is Screen.TemplatePicker -> {
                BackHandler { screen = Screen.Home }
                TemplatePickerScreen(
                    onPick = { typeKey -> screen = Screen.Edit(null, typeKey) },
                    onBack = { screen = Screen.Home }
                )
            }

            is Screen.Detail -> {
                BackHandler { screen = Screen.Home }
                CardDetailScreen(
                    viewModel = viewModel,
                    cardId = current.cardId,
                    onBack = { screen = Screen.Home },
                    onEdit = { cardId -> screen = Screen.Edit(cardId, null) }
                )
            }

            is Screen.Edit -> {
                BackHandler {
                    screen = if (current.cardId != null) Screen.Detail(current.cardId) else Screen.Home
                }
                CardEditScreen(
                    viewModel = viewModel,
                    cardId = current.cardId,
                    typeKey = current.typeKey,
                    onBack = {
                        screen = if (current.cardId != null) Screen.Detail(current.cardId) else Screen.Home
                    },
                    onDone = { savedId ->
                        screen = if (current.cardId != null) Screen.Detail(savedId) else Screen.Detail(savedId)
                    }
                )
            }

            is Screen.Settings -> {
                BackHandler { screen = Screen.Home }
                SettingsScreen(
                    viewModel = viewModel,
                    darkTheme = isDark,
                    onToggleDarkTheme = { value ->
                        darkOverride = value
                        SecurePrefs.setDarkTheme(context, value)
                    },
                    onBack = { screen = Screen.Home },
                    onAbout = { screen = Screen.About }
                )
            }

            is Screen.About -> {
                BackHandler { screen = Screen.Settings }
                AboutScreen(onBack = { screen = Screen.Settings })
            }
        }
    }
}
