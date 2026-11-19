package ru.rfdocs.wallet.ui.screens

import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import ru.rfdocs.wallet.data.repo.DocRepository
import ru.rfdocs.wallet.security.BackupManager

@Composable
fun SettingsDialogs(
    exportUri: Uri?,
    importUri: Uri?,
    wipeVisible: Boolean,
    onDismissExport: () -> Unit,
    onDismissImport: () -> Unit,
    onDismissWipe: () -> Unit
) {
    val context = LocalContext.current

    if (exportUri != null) {
        PasswordDialog(
            title = "Пароль для бэкапа",
            message = "Файл будет зашифрован. Запомните пароль.",
            confirmText = "Экспортировать",
            onConfirm = { password -> runExport(context, exportUri, password) },
            onDismiss = onDismissExport
        )
    }

    if (importUri != null) {
        PasswordDialog(
            title = "Пароль от бэкапа",
            message = "Введите пароль, с которым создавался файл.",
            confirmText = "Импортировать",
            onConfirm = { password -> runImport(context, importUri, password) },
            onDismiss = onDismissImport
        )
    }

    if (wipeVisible) {
        ConfirmDialog(
            title = "Удалить все документы?",
            message = "Данные будут удалены с устройства без возможности восстановления.",
            confirmText = "Удалить",
            onConfirm = { runWipe(context) },
            onDismiss = onDismissWipe
        )
    }
}

private fun runExport(context: Context, uri: Uri, password: String) {
    val result = runCatching { BackupManager.exportTo(context, uri, password) }
    val message = if (result.isSuccess) "Бэкап сохранён" else "Ошибка экспорта"
    Toast.makeText(context, message, Toast.LENGTH_LONG).show()
}

private fun runImport(context: Context, uri: Uri, password: String) {
    val result = runCatching { BackupManager.importFrom(context, uri, password) }
    val message = if (result.isSuccess) "Документы восстановлены" else "Ошибка импорта"
    Toast.makeText(context, message, Toast.LENGTH_LONG).show()
}

private fun runWipe(context: Context) {
    val result = runCatching { DocRepository.wipeAll() }
    val message = if (result.isSuccess) "Данные удалены" else "Ошибка удаления"
    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
}