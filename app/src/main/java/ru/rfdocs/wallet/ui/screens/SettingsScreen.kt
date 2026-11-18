package ru.rfdocs.wallet.ui.screens

import android.app.Activity
import android.net.Uri
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ru.rfdocs.wallet.data.repo.DocRepository
import ru.rfdocs.wallet.security.BackupManager
import ru.rfdocs.wallet.security.BiometricHelper
import ru.rfdocs.wallet.security.PinVault
import ru.rfdocs.wallet.security.SecurePrefs
import ru.rfdocs.wallet.ui.PinSetupScreen
import ru.rfdocs.wallet.ui.WalletViewModel
import ru.rfdocs.wallet.ui.components.SectionTitle
import ru.rfdocs.wallet.ui.components.WalletBackground

private val BackupMimeTypes = arrayOf("application/octet-stream", "*/*")

@Composable
private fun ToggleLine(
    title: String,
    subtitle: String,
    checked: Boolean,
    enabled: Boolean,
    onToggle: (Boolean) -> Unit
) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp)) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Switch(checked = checked, onCheckedChange = onToggle, enabled = enabled)
    }
}

@Composable
private fun ActionLine(title: String, onClick: () -> Unit) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.onBackground,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 14.dp)
    )
}

@Composable
fun SettingsScreen(
    viewModel: WalletViewModel,
    darkTheme: Boolean,
    onToggleDarkTheme: (Boolean) -> Unit,
    onBack: () -> Unit,
    onAbout: () -> Unit
) {
    val context = LocalContext.current
    val view = LocalView.current
    val activity = view.context as? Activity

    var biometricEnabled by remember { mutableStateOf(SecurePrefs.isBiometricEnabled(context)) }
    var screenBlocked by remember { mutableStateOf(SecurePrefs.isScreenshotBlocked(context)) }
    var showPinChange by remember { mutableStateOf(false) }
    var showWipe by remember { mutableStateOf(false) }
    var exportUri by remember { mutableStateOf<Uri?>(null) }
    var importUri by remember { mutableStateOf<Uri?>(null) }

    val biometricAvailable = remember { BiometricHelper.isBiometricAvailable(context) }

    fun applyScreenPolicy(enabled: Boolean) {
        SecurePrefs.setScreenshotBlocked(context, enabled)
        screenBlocked = enabled
        val window = activity?.window ?: return
        if (enabled) {
            window.setFlags(
                WindowManager.LayoutParams.FLAG_SECURE,
                WindowManager.LayoutParams.FLAG_SECURE
            )
        } else {
            window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
        }
    }

    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/octet-stream")
    ) { uri: Uri? ->
        exportUri = uri
    }

    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        importUri = uri
    }

    if (showPinChange) {
        PinSetupScreen(
            title = "Новый PIN-код",
            subtitle = "Придумайте новый код",
            onComplete = { pin ->
                PinVault.setPin(context, pin)
                showPinChange = false
                Toast.makeText(context, "PIN-код обновлён", Toast.LENGTH_SHORT).show()
            },
            onCancel = { showPinChange = false }
        )
        return
    }

    SettingsDialogs(
        exportUri = exportUri,
        importUri = importUri,
        wipeVisible = showWipe,
        onDismissExport = { exportUri = null },
        onDismissImport = { importUri = null },
        onDismissWipe = { showWipe = false }
    )

    WalletBackground {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .statusBarsPadding()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Назад",
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }
                Text(
                    text = "Настройки",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            Spacer(Modifier.height(16.dp))
            SectionTitle(text = "Безопасность")
            Spacer(Modifier.height(8.dp))

            ToggleLine(
                title = "Вход по отпечатку",
                subtitle = if (biometricAvailable) "Быстрый вход" else "Не настроен",
                checked = biometricEnabled,
                enabled = biometricAvailable,
                onToggle = { value ->
                    biometricEnabled = value
                    SecurePrefs.setBiometricEnabled(context, value)
                }
            )

            ToggleLine(
                title = "Запрет скриншотов",
                subtitle = if (screenBlocked) "Заблокировано" else "Разрешено",
                checked = screenBlocked,
                enabled = true,
                onToggle = { value -> applyScreenPolicy(value) }
            )

            ActionLine(title = "Сменить PIN-код") { showPinChange = true }

            Spacer(Modifier.height(20.dp))
            SectionTitle(text = "Оформление")
            Spacer(Modifier.height(8.dp))

            ToggleLine(
                title = "Тёмная тема",
                subtitle = "AMOLED",
                checked = darkTheme,
                enabled = true,
                onToggle = { value -> onToggleDarkTheme(value) }
            )

            Spacer(Modifier.height(20.dp))
            SectionTitle(text = "Резервная копия")
            Spacer(Modifier.height(8.dp))

            ActionLine(title = "Экспорт бэкапа") {
                if (DocRepository.cardCount() == 0) {
                    Toast.makeText(context, "Нет документов", Toast.LENGTH_SHORT).show()
                } else {
                    exportLauncher.launch(BackupManager.suggestedFileName())
                }
            }

            ActionLine(title = "Импорт из бэкапа") {
                importLauncher.launch(BackupMimeTypes)
            }

            Spacer(Modifier.height(20.dp))
            SectionTitle(text = "Прочее")
            Spacer(Modifier.height(8.dp))

            ActionLine(title = "О приложении") { onAbout() }
            ActionLine(title = "Удалить все документы") { showWipe = true }

            Spacer(Modifier.height(32.dp))
        }
    }
}