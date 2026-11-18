package ru.rfdocs.wallet.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
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
    val locked by LockManager.locked.collectAsState()

    WalletTheme(darkTheme = isDark) {
        var pinConfigured by remember { mutableStateOf(PinVault.isPinSet(context)) }
        var screen by remember { mutableStateOf<Screen>(Screen.Home) }

        if (!pinConfigured) {
            PinSetupScreen(
                title = "Придумайте PIN-код",
                subtitle = "Код нужен для входа в приложение и хранится только на устройстве",
                onComplete = { pin ->
                    PinVault.setPin(context, pin)
                    SecurePrefs.setOnboarded(context, true)
                    pinConfigured = true
                    screen = Screen.Home
                    LockManager.unlock()
                }
            )
            return@WalletTheme
        }

        if (locked) {
            LockScreen(
                onUnlocked = { LockManager.unlock() },
                onVaultReset = {
                    screen = Screen.Home
                    pinConfigured = false
                }
            )
            return@WalletTheme
        }

        BackHandler(enabled = screen != Screen.Home) {
            screen = when (val current = screen) {
                is Screen.Detail -> Screen.Home
                is Screen.Edit -> if (current.cardId != null) {
                    Screen.Detail(current.cardId)
                } else {
                    Screen.TemplatePicker
                }

                is Screen.TemplatePicker -> Screen.Home
                is Screen.Settings -> Screen.Home
                is Screen.About -> Screen.Settings
                else -> Screen.Home
            }
        }

        AnimatedContent(
            targetState = screen,
            transitionSpec = {
                (fadeIn(tween(240)) + scaleIn(tween(240), initialScale = 0.96f)) togetherWith
                    (fadeOut(tween(160)) + scaleOut(tween(200), targetScale = 0.98f))
            },
            label = "wallet_screen"
        ) { current ->
            when (current) {
                is Screen.Home -> HomeScreen(
                    onOpenCard = { cardId -> screen = Screen.Detail(cardId) },
                    onAddCard = { screen = Screen.TemplatePicker },
                    onOpenSettings = { screen = Screen.Settings }
                )

                is Screen.TemplatePicker -> TemplatePickerScreen(
                    onPick = { typeKey -> screen = Screen.Edit(null, typeKey) },
                    onBack = { screen = Screen.Home }
                )

                is Screen.Detail -> CardDetailScreen(
                    viewModel = viewModel,
                    cardId = current.cardId,
                    onBack = { screen = Screen.Home },
                    onEdit = { cardId -> screen = Screen.Edit(cardId, null) }
                )

                is Screen.Edit -> CardEditScreen(
                    viewModel = viewModel,
                    cardId = current.cardId,
                    typeKey = current.typeKey,
                    onBack = {
                        screen = if (current.cardId != null) {
                            Screen.Detail(current.cardId)
                        } else {
                            Screen.TemplatePicker
                        }
                    },
                    onDone = { savedId -> screen = Screen.Detail(savedId) }
                )

                is Screen.Settings -> SettingsScreen(
                    viewModel = viewModel,
                    darkTheme = isDark,
                    onToggleDarkTheme = { value ->
                        darkOverride = value
                        SecurePrefs.setDarkTheme(context, value)
                    },
                    onBack = { screen = Screen.Home },
                    onAbout = { screen = Screen.About }
                )

                is Screen.About -> AboutScreen(
                    onBack = { screen = Screen.Settings }
                )
            }
        }
    }
}