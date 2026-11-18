package ru.rfdocs.wallet.ui.screens

import androidx.compose.runtime.Composable
import ru.rfdocs.wallet.ui.WalletViewModel

@Composable
fun SettingsScreen(
    viewModel: WalletViewModel,
    darkTheme: Boolean,
    onToggleDarkTheme: (Boolean) -> Unit,
    onBack: () -> Unit,
    onAbout: () -> Unit
) {
    SettingsBody(
        darkTheme = darkTheme,
        onToggleDarkTheme = onToggleDarkTheme,
        onBack = onBack,
        onAbout = onAbout
    )
}