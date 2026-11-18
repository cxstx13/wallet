package ru.rfdocs.wallet.ui.screens

import android.app.Activity
import android.net.Uri
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ru.rfdocs.wallet.data.repo.DocRepository
import ru.rfdocs.wallet.security.BackupManager
import ru.rfdocs.wallet.security.BiometricHelper
import ru.rfdocs.wallet.security.CryptoManager
import ru.rfdocs.wallet.security.PinVault
import ru.rfdocs.wallet.security.SecurePrefs
import ru.rfdocs.wallet.ui.PinSetupScreen
import ru.rfdocs.wallet.ui.WalletViewModel
import ru.rfdocs.wallet.ui.components.SectionTitle
import ru.rfdocs.wallet.ui.theme.Palette
import ru.rfdocs.wallet.ui.theme.WalletTheme

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
    val scope = rememberCoroutineScope()

    val activity = view.context as? Activity

    var biometricEnabled by remember { mutableStateOf(SecurePrefs.isBiometricEnabled(context)) }
    var screenshotProtected by remember { mutableStateOf(true) }
    var showPinChange by remember { mutableStateOf(false) }
    var showWipe by remember { mutableStateOf(false) }
    var showExportPassword by remember { mutableStateOf(false) }
    var showImportPassword by remember { mutableStateOf(false) }
    var pendingExportUri by remember { mutableStateOf<Uri?>(null) }
    var pendingImportUri by remember { mutableStateOf<Uri?>(null) }

    val biometricAvailable = remember { BiometricHelper.isBiometricAvailable(context) }

    fun applyScreenshotProtection(enabled: Boolean) {
        screenshotProtected = enabled
        val window = activity?.window
        if (window != null) {
            if (enabled) {
                window.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
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
            showExportPassword = true
        }
    }

    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            pendingImportUri = uri
            showImportPassword = true
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
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
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        Spacer(Modifier.height(8.dp))
        SectionTitle(text = "Безопасность")
        Spacer(Modifier.height(10.dp))

        SwitchRow(
            title = "Вход по отпечатку",
            subtitle = if (biometricAvailable) {
                "Быстрый вход вместо ввода PIN-кода"
            } else {
                "Отпечаток не настроен на устройстве"
            },
            checked = biometricEnabled,
            enabled = biometricAvailable,
            onCheckedChange = { value ->
                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                biometricEnabled = value
                SecurePrefs.setBiometricEnabled(context, value)
            }
        )

        Spacer(Modifier.height(10.dp))
        SwitchRow(
            title = "Запрет скриншотов",
            subtitle = if (screenshotProtected) {
                "FLAG_SECURE включён: снимки экрана блокируются"
            } else {
                "Снимки экрана разрешены — данные видны при захвате"
            },
            checked = screenshotProtected,
            enabled = true,
            onCheckedChange = { value ->
                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                applyScreenshotProtection(value)
            }
        )

        Spacer(Modifier.height(10.dp))
        NavRow(
            title = "Сменить PIN-код",
            onClick = {
                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                showPinChange = true
            }
        )

        Spacer(Modifier.height(22.dp))
        SectionTitle(text = "Оформление")
        Spacer(Modifier.height(10.dp))
        SwitchRow(
            title = "Тёмная тема",
            subtitle = "Глубокий графитовый фон",
            checked = darkTheme,
            enabled = true,
            onCheckedChange = { value ->
                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                onToggleDarkTheme(value)
            }
        )

        Spacer(Modifier.height(22.dp))
        SectionTitle(text = "Резервная копия")
        Spacer(Modifier.height(10.dp))
        NavRow(
            title = "Экспорт зашифрованного бэкапа",
            subtitle = "Один файл, защищённый паролем",
            onClick = {
                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                if (DocRepository.cardCount() == 0) {
                    Toast.makeText(context, "Нет документов для экспорта", Toast.LENGTH_SHORT).show()
                } else {
                    exportLauncher.launch(BackupManager.suggestedFileName())
                }
            }
        )

        Spacer(Modifier.height(10.dp))
        NavRow(
            title = "Импорт из бэкапа",
            subtitle = "Заменит текущие документы",
            onClick = {
                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                importLauncher.launch(arrayOf("application/octet-stream", "*/*"))
            }
        )

        Spacer(Modifier.height(22.dp))
        SectionTitle(text = "Прочее")
        Spacer(Modifier.height(10.dp))
        NavRow(
            title = "О приложении и конфиденциальность",
            onClick = {
                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                onAbout()
            }
        )

        Spacer(Modifier.height(10.dp))
        NavRow(
            title = "Удалить все документы",
            subtitle = "Безвозвратно, только на этом устройстве",
            danger = true,
            onClick = {
                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                showWipe = true
            }
        )

        Spacer(Modifier.height(32.dp))
    }

    if (showExportPassword) {
        PasswordDialog(
            title = "Пароль для бэкапа",
            message = "Файл будет зашифрован паролем. Запомните его — без пароля данные не восстановить.",
            confirmText = "Экспортировать",
            onConfirm = { password ->
                val uri = pendingExportUri
                showExportPassword = false
                if (uri != null) {
                    scope.launch {
                        val message = withContext(Dispatchers.IO) {
                            BackupManager.exportTo(context, uri, password).fold(
                                onSuccess = { count -> "Экспортировано документов: $count" },
                                onFailure = { error -> error.message ?: "Ошибка экспорта" }
                            )
                        }
                        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                    }
                }
            },
            onDismiss = { showExportPassword = false }
        )
    }

    if (showImportPassword) {
        PasswordDialog(
            title = "Пароль от бэкапа",
            message = "Введите пароль, с которым создавался файл резервной копии.",
            confirmText = "Импортировать",
            onConfirm = { password ->
                val uri = pendingImportUri
                showImportPassword = false
                if (uri != null) {
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
                }
            },
            onDismiss = { showImportPassword = false }
        )
    }

    if (showWipe) {
        ConfirmDialog(
            title = "Удалить все документы?",
            message = "Все карточки и фотографии будут удалены с устройства. Действие необратимо.",
            confirmText = "Удалить",
            onConfirm = {
                DocRepository.wipeAll()
                CryptoManager.wipeKey()
                Toast.makeText(context, "Все документы удалены", Toast.LENGTH_SHORT).show()
            },
            onDismiss = { showWipe = false }
        )
    }
}

@Composable
private fun SwitchRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    enabled: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(WalletTheme.extra.glassFill)
            .border(1.dp, WalletTheme.extra.glassBorder, RoundedCornerShape(16.dp))
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = if (enabled) {
                    MaterialTheme.colorScheme.onSurface
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(Modifier.size(8.dp))
        Switch(checked = checked, onCheckedChange = onCheckedChange, enabled = enabled)
    }
}

@Composable
private fun NavRow(
    title: String,
    subtitle: String = "",
    danger: Boolean = false,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(WalletTheme.extra.glassFill)
            .border(1.dp, WalletTheme.extra.glassBorder, RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = if (danger) Palette.Danger else MaterialTheme.colorScheme.onSurface
            )
            if (subtitle.isNotBlank()) {
                Spacer(Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}