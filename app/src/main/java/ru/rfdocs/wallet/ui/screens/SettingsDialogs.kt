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
fun ExportBackupDialog(uri: Uri?, onDismiss: () -> Unit) {
    if (uri == null) return
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    PasswordDialog(
        title = "Пароль для бэкапа",
        message = "Файл будет зашифрован паролем. Запомните его — без пароля данные не восстановить.",
        confirmText = "Экспортировать",
        onConfirm = { password ->
            scope.launch {
                val message = withContext(Dispatchers.IO) {
                    BackupManager.exportTo(context, uri, password).fold(
                        onSuccess = { count -> "Экспортировано документов: $count" },
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
        message = "Введите пароль, с которым создавался файл резервной копии.",
        confirmText = "Импортировать",
        onConfirm = { password ->
            scope.launch {
                val message = withContext(Dispatchers.IO) {
                    BackupManager.importFrom(context, uri, password).fold(
                        onSuccess = { count ->
                            DocRepository.refresh()
                            "Восстановлено документов: $count"
                        },
                        onFailure = { error -> error.message ?: "Не удалось восстановить бэкап" }
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
        message = "Все карточки и фотографии будут удалены с устройства. Действие необратимо.",
        confirmText = "Удалить",
        onConfirm = {
            DocRepository.wipeAll()
            CryptoManager.wipeKey()
            Toast.makeText(context, "Все документы удалены", Toast.LENGTH_SHORT).show()
        },
        onDismiss = onDismiss
    )
}