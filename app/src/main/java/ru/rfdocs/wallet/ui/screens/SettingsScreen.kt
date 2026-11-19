package ru.rfdocs.wallet.ui.screens

import androidx.compose.runtime.Composable

@Composable
fun SettingsScreen(
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