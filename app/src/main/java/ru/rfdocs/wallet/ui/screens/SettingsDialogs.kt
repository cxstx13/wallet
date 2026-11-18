package ru.rfdocs.wallet.ui.screens

import android.net.Uri
import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ru.rfdocs.wallet.data.repo.DocRepository
import ru.rfdocs.wallet.security.BackupManager
import ru.rfdocs.wallet.security.CryptoManager

@Composable
fun SettingsDialogs(
    exportUri: Uri?,
    importUri: Uri?,
    wipeVisible: Boolean,
    onDismissExport: () -> Unit,
    onDismissImport: () -> Unit,
    onDismissWipe: () -> Unit
) {
    if (exportUri != null) {
        ExportBackupDialog(uri = exportUri, onDismiss = onDismissExport)
    }
    if (importUri != null) {
        ImportBackupDialog(uri = importUri, onDismiss = onDismissImport)
    }
    if (wipeVisible) {
        WipeVaultDialog(onDismiss = onDismissWipe)
    }
}

@Composable
fun ExportBackupDialog(uri: Uri?, onDismiss: () -> Unit) {
    if (uri == null) return
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    PasswordDialog(
        title = "Пароль для бэкапа",
        message = "Файл будет зашифрован. Запомните пароль.",
        confirmText = "Экспортировать",
        onConfirm = { password ->
            scope.launch {
                val message = withContext(Dispatchers.IO) {
                    BackupManager.exportTo(context, uri, password).fold(
                        onSuccess = { result ->
                            val kb = (result.bytes / 1024).coerceAtLeast(1)
                            "Готово: ${result.cards} док., $kb КБ"
                        },
                        onFailure = { error -> error.message ?: "Ошибка экспорта" }
                    )
                }
                Toast.makeText(context, message, Toast.LENGTH_LONG).show()
            }
        },
        onDismiss = onDismiss
    )
}

@Composable
fun ImportBackupDialog(uri: Uri?, onDismiss: () -> Unit) {
    if (uri == null) return
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    PasswordDialog(
        title = "Пароль от бэкапа",
        message = "Введите пароль, с которым создавался файл.",
        confirmText = "Импортировать",
        onConfirm = { password ->
            scope.launch {
                val message = withContext(Dispatchers.IO) {
                    BackupManager.importFrom(context, uri, password).fold(
                        onSuccess = { count ->
                            DocRepository.refresh()
                            "Восстановлено документов: $count"
                        },
                        onFailure = { error -> error.message ?: "Не удалось восстановить" }
                    )
                }
                Toast.makeText(context, message, Toast.LENGTH_LONG).show()
            }
        },
        onDismiss = onDismiss
    )
}

@Composable
fun WipeVaultDialog(onDismiss: () -> Unit) {
    val context = LocalContext.current

    ConfirmDialog(
        title = "Удалить все документы?",
        message = "Данные будут удалены с устройства.",
        confirmText = "Удалить",
        onConfirm = {
            DocRepository.wipeAll()
            CryptoManager.wipeKey()
            Toast.makeText(context, "Удалено", Toast.LENGTH_SHORT).show()
        },
        onDismiss = onDismiss
    )
}