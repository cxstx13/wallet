package ru.rfdocs.wallet.ui.screens

import android.content.Intent
import android.net.Uri
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.ChevronRight
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
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
    val scope = rememberCoroutineScope()

    var biometricEnabled by remember { mutableStateOf(SecurePrefs.isBiometricEnabled(context)) }
    var showPinChange by remember { mutableStateOf(false) }
    var showWipe by remember { mutableStateOf(false) }
    var pendingExportUri by remember { mutableStateOf<Uri?>(null) }
    var pendingImportUri by remember { mutableStateOf<Uri?>(null) }
    var showExportPassword by remember { mutableStateOf(false) }
    var showImportPassword by remember { mutableStateOf(false) }

    val biometricAvailable = remember { BiometricHelper.isBiometricAvailable(context) }

    val createBackup = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/octet-stream")
    ) { uri: Uri? ->
        if (uri != null) {
            pendingExportUri = uri
            showExportPassword = true
        }
    }

    val openBackup = rememberLauncherForActivityResult(
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
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .statusBarsPadding()
            .padding(horizontal = 20.dp)
            .navigationBarsPadding()
    ) {
        Spacer(modifier = Modifier.height(6.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.Rounded.ArrowBack,
                    contentDescription = "Назад",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
            Text(
                text = "Настройки",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
        SectionTitle(text = "Безопасность")
        Spacer(modifier = Modifier.height(8.dp))

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
                biometricEnabled = value
                SecurePrefs.setBiometricEnabled(context, value)
            }
        )
        Spacer(modifier = Modifier.height(8.dp))
        NavRow(title = "Сменить PIN-код", onClick = { showPinChange = true })

        Spacer(modifier = Modifier.height(18.dp))
        SectionTitle(text = "Оформление")
        Spacer(modifier = Modifier.height(8.dp))
        SwitchRow(
            title = "Тёмная тема",
            subtitle = "OLED-чёрный фон для экономии батареи",
            checked = darkTheme,
            enabled = true,
            onCheckedChange = onToggleDarkTheme
        )

        Spacer(modifier = Modifier.height(18.dp))
        SectionTitle(text = "Резервная копия")
        Spacer(modifier = Modifier.height(8.dp))
        NavRow(
            title = "Экспорт зашифрованного бэкапа",
            subtitle = "Один файл, защищённый паролем",
            onClick = { createBackup.launch(BackupManager.suggestedFileName()) }
        )
        Spacer(modifier = Modifier.height(8.dp))
        NavRow(
            title = "Импорт из бэкапа",
            subtitle = "Заменит текущие документы",
            onClick = { openBackup.launch(arrayOf("application/octet-stream", "*/*")) }
        )

        Spacer(modifier = Modifier.height(18.dp))
        SectionTitle(text = "Прочее")
        Spacer(modifier = Modifier.height(8.dp))
        NavRow(title = "О приложении и конфиденциальность", onClick = onAbout)
        Spacer(modifier = Modifier.height(8.dp))
        NavRow(
            title = "Удалить все документы",
            subtitle = "Безвозвратно, только на этом устройстве",
            danger = true,
            onClick = { showWipe = true }
        )

        Spacer(modifier = Modifier.height(40.dp))
    }

    if (showExportPassword) {
        PasswordDialog(
            title = "Пароль для бэкапа",
            message = "Файл будет зашифрован паролем. Запомните его — восстановить данные без пароля невозможно.",
            confirmText = "Экспортировать",
            onConfirm = { password ->
                val uri = pendingExportUri
                showExportPassword = false
                if (uri != null) {
                    scope.launch {
                        val message = viewModel.exportBackup(uri, password)
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
                        val message = viewModel.importBackup(uri, password)
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
                viewModel.wipeVault()
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
            .clip(RoundedCornerShape(18.dp))
            .background(WalletTheme.extra.glassFill)
            .border(1.dp, WalletTheme.extra.glassBorder, RoundedCornerShape(18.dp))
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
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
            .clip(RoundedCornerShape(18.dp))
            .background(WalletTheme.extra.glassFill)
            .border(1.dp, WalletTheme.extra.glassBorder, RoundedCornerShape(18.dp))
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 14.dp),
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
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Box(modifier = Modifier.size(20.dp), contentAlignment = Alignment.Center) {
            Icon(
                imageVector = Icons.Rounded.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
