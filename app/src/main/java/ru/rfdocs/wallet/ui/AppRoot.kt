package ru.rfdocs.wallet.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import ru.rfdocs.wallet.ui.screens.AboutScreen
import ru.rfdocs.wallet.ui.screens.CardDetailScreen
import ru.rfdocs.wallet.ui.screens.CardEditScreen
import ru.rfdocs.wallet.ui.screens.HomeScreen
import ru.rfdocs.wallet.ui.screens.SettingsBody
import ru.rfdocs.wallet.ui.screens.TemplatePickerScreen
import ru.rfdocs.wallet.ui.theme.WalletTheme

sealed interface Screen {
    data object Home : Screen
    data object Picker : Screen
    data class Detail(val cardId: String) : Screen
    data class Edit(val cardId: String?, val typeKey: String?) : Screen
    data object Settings : Screen
    data object About : Screen
}

@Composable
fun AppRoot() {
    val systemDark = isSystemInDarkTheme()
    var darkTheme by remember { mutableStateOf(systemDark) }
    var unlocked by remember { mutableStateOf(false) }
    var screen by remember { mutableStateOf<Screen>(Screen.Home) }

    WalletTheme(darkTheme = darkTheme) {
        if (!unlocked) {
            LockScreen(onUnlocked = { unlocked = true })
            return@WalletTheme
        }

        BackHandler(enabled = screen != Screen.Home) {
            screen = when (val current = screen) {
                is Screen.Detail -> Screen.Home
                is Screen.Edit -> if (current.cardId != null) {
                    Screen.Detail(current.cardId)
                } else {
                    Screen.Picker
                }

                is Screen.Picker -> Screen.Home
                is Screen.Settings -> Screen.Home
                is Screen.About -> Screen.Settings
                else -> Screen.Home
            }
        }

        when (val current = screen) {
            is Screen.Home -> HomeScreen(
                onOpenCard = { cardId -> screen = Screen.Detail(cardId) },
                onAddCard = { screen = Screen.Picker },
                onOpenSettings = { screen = Screen.Settings }
            )

            is Screen.Picker -> TemplatePickerScreen(
                onPick = { typeKey -> screen = Screen.Edit(null, typeKey) },
                onBack = { screen = Screen.Home }
            )

            is Screen.Detail -> CardDetailScreen(
                cardId = current.cardId,
                onBack = { screen = Screen.Home },
                onEdit = { cardId -> screen = Screen.Edit(cardId, null) },
                onDelete = { card ->
                    deleteCard(card.id)
                    screen = Screen.Home
                }
            )

            is Screen.Edit -> CardEditScreen(
                cardId = current.cardId,
                typeKey = current.typeKey,
                onBack = {
                    val id = current.cardId
                    screen = if (id != null) Screen.Detail(id) else Screen.Picker
                },
                onDone = { cardId -> screen = Screen.Detail(cardId) }
            )

            is Screen.Settings -> SettingsBody(
                darkTheme = darkTheme,
                onToggleDarkTheme = { value -> darkTheme = value },
                onBack = { screen = Screen.Home },
                onAbout = { screen = Screen.About }
            )

            is Screen.About -> AboutScreen(onBack = { screen = Screen.Settings })
        }
    }
}