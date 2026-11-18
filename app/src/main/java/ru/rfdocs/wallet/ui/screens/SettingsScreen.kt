package ru.rfdocs.wallet.ui.screens

import android.app.Activity
import android.net.Uri
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
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
    val haptics = LocalHapticFeedback.current

    val activity = view.context as? Activity

    var biometricEnabled by remember { mutableStateOf(SecurePrefs.isBiometricEnabled(context)) }
    var screenshotBlocked by remember { mutableStateOf(SecurePrefs.isScreenshotBlocked(context)) }
    var showPinChange by remember { mutableStateOf(false) }
    var showWipe by remember { mutableStateOf(false) }
    var pendingExportUri by remember { mutableStateOf<Uri?>(null) }
    var pendingImportUri by remember { mutableStateOf<Uri?>(null) }

    val biometricAvailable = remember { BiometricHelper.isBiometricAvailable(context) }

    fun applyScreenshotPolicy(enabled: Boolean) {
        SecurePrefs.setScreenshotBlocked(context, enabled)
        screenshotBlocked = enabled
        val window = activity?.window
        if (window != null) {
            if (enabled) {
                window.setFlags(
                    WindowManager.LayoutParams.FLAG_SECURE,
                    WindowManager.LayoutParams.FLAG_SECURE
                )
            } else {
                window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
            }
        }
    }

    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/octet-stream")
    ) { uri: Uri? ->
        if (uri != null) {
            pendingExportUri = uri
        }
    }

    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            pendingImportUri = uri
        }
    }

    if (showPinChange) {
        PinSetupScreen(
            title = "Новый PIN-код",
            subtitle = "Придумайте новый код для входа",
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
        exportUri = pendingExportUri,
        importUri = pendingImportUri,
        wipeVisible = showWipe,
        onDismissExport = { pendingExportUri = null },
        onDismissImport = { pendingImportUri = null },
        onDismissWipe = { showWipe = false }
    )

    WalletBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .statusBarsPadding()
                .padding(horizontal = 16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Назад"
                    )
                }
                Text(
                    text = "Настройки",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            Spacer(Modifier.height(8.dp))
            SectionTitle(text = "Безопасность")
            Spacer(Modifier.height(10.dp))

            SettingsSwitchRow(
                title = "Вход по отпечатку",
                subtitle = if (biometricAvailable) {
                    "Быстрый вход вместо ввода PIN-кода"
                } else {
                    "Отпечаток не настроен на устройстве"
                },
                checked = biometricEnabled,
                enabled = biometricAvailable,
                onCheckedChange = { value ->
                    haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    biometricEnabled = value
                    SecurePrefs.setBiometricEnabled(context, value)
                }
            )

            Spacer(Modifier.height(10.dp))
            SettingsSwitchRow(
                title = "Запрет скриншотов",
                subtitle = if (screenshotBlocked) {
                    "Снимки экрана блокируются"
                } else {
                    "Снимки экрана разрешены"
                },
                checked = screenshotBlocked,
                enabled = true,
                onCheckedChange = { value ->
                    haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    applyScreenshotPolicy(value)
                }
            )

            Spacer(Modifier.height(10.dp))
            SettingsNavRow(
                title = "Сменить PIN-код",
                onClick = {
                    haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    showPinChange = true
                }
            )

            Spacer(Modifier.height(22.dp))
            SectionTitle(text = "Оформление")
            Spacer(Modifier.height(10.dp))
            SettingsSwitchRow(
                title = "Тёмная тема",
                subtitle = "AMOLED-фон",
                checked = darkTheme,
                enabled = true,
                onCheckedChange = { value ->
                    haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onToggleDarkTheme(value)
                }
            )

            Spacer(Modifier.height(22.dp))
            SectionTitle(text = "Резервная копия")
            Spacer(Modifier.height(10.dp))
            SettingsNavRow(
                title = "Экспорт бэкапа",
                subtitle = "Файл, защищённый паролем",
                onClick = {
                    haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    if (DocRepository.cardCount() == 0) {
                        Toast.makeText(context, "Нет документов для экспорта", Toast.LENGTH_SHORT).show()
                    } else {
                        exportLauncher.launch(BackupManager.suggestedFileName())
                    }
                }
            )

            Spacer(Modifier.height(10.dp))
            SettingsNavRow(
                title = "Импорт из бэкапа",
                subtitle = "Заменит текущие документы",
                onClick = {
                    haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    importLauncher.launch(arrayOf("application/octet-stream", "*/*"))
                }
            )

            Spacer(Modifier.height(22.dp))
            SectionTitle(text = "Прочее")
            Spacer(Modifier.height(10.dp))
            SettingsNavRow(
                title = "О приложении",
                onClick = {
                    haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onAbout()
                }
            )

            Spacer(Modifier.height(10.dp))
            SettingsNavRow(
                title = "Удалить все документы",
                subtitle = "Безвозвратно, только на этом устройстве",
                danger = true,
                onClick = {
                    haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    showWipe = true
                }
            )

            Spacer(Modifier.height(32.dp))
        }
    }
}