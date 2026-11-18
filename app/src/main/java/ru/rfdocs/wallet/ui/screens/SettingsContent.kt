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
import ru.rfdocs.wallet.ui.components.SectionTitle
import ru.rfdocs.wallet.ui.components.WalletBackground

private val MimeAll = arrayOf("*/*")

@Composable
private fun Toggle(
    title: String,
    subtitle: String,
    checked: Boolean,
    enabled: Boolean,
    onChange: (Boolean) -> Unit
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
        Switch(checked = checked, onCheckedChange = onChange, enabled = enabled)
    }
}

@Composable
private fun Action(title: String, onClick: () -> Unit) {
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
fun SettingsBody(
    darkTheme: Boolean,
    onToggleDarkTheme: (Boolean) -> Unit,
    onBack: () -> Unit,
    onAbout: () -> Unit
) {
    val context = LocalContext.current
    val view = LocalView.current
    val activity = view.context as? Activity

    var bio by remember { mutableStateOf(SecurePrefs.isBiometricEnabled(context)) }
    var blockShots by remember { mutableStateOf(SecurePrefs.isScreenshotBlocked(context)) }
    var changePin by remember { mutableStateOf(false) }
    var wipe by remember { mutableStateOf(false) }
    var exportUri by remember { mutableStateOf<Uri?>(null) }
    var importUri by remember { mutableStateOf<Uri?>(null) }

    val bioOk = remember { BiometricHelper.isBiometricAvailable(context) }

    fun applyShotPolicy(enabled: Boolean) {
        SecurePrefs.setScreenshotBlocked(context, enabled)
        blockShots = enabled
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

    if (changePin) {
        PinSetupScreen(
            title = "Новый PIN-код",
            subtitle = "Придумайте новый код",
            onComplete = { pin ->
                PinVault.setPin(context, pin)
                changePin = false
                Toast.makeText(context, "PIN обновлён", Toast.LENGTH_SHORT).show()
            },
            onCancel = { changePin = false }
        )
        return
    }

    SettingsDialogs(
        exportUri = exportUri,
        importUri = importUri,
        wipeVisible = wipe,
        onDismissExport = { exportUri = null },
        onDismissImport = { importUri = null },
        onDismissWipe = { wipe = false }
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

            Toggle(
                title = "Вход по отпечатку",
                subtitle = if (bioOk) "Быстрый вход" else "Не настроен",
                checked = bio,
                enabled = bioOk,
                onChange = { value ->
                    bio = value
                    SecurePrefs.setBiometricEnabled(context, value)
                }
            )

            Toggle(
                title = "Запрет скриншотов",
                subtitle = if (blockShots) "Заблокировано" else "Разрешено",
                checked = blockShots,
                enabled = true,
                onChange = { value -> applyShotPolicy(value) }
            )

            Action(title = "Сменить PIN-код", onClick = { changePin = true })

            Spacer(Modifier.height(20.dp))
            SectionTitle(text = "Оформление")
            Spacer(Modifier.height(8.dp))

            Toggle(
                title = "Тёмная тема",
                subtitle = "AMOLED",
                checked = darkTheme,
                enabled = true,
                onChange = onToggleDarkTheme
            )

            Spacer(Modifier.height(20.dp))
            SectionTitle(text = "Резервная копия")
            Spacer(Modifier.height(8.dp))

            Action(
                title = "Экспорт бэкапа",
                onClick = {
                    if (DocRepository.cardCount() == 0) {
                        Toast.makeText(context, "Нет документов", Toast.LENGTH_SHORT).show()
                    } else {
                        exportLauncher.launch(BackupManager.suggestedFileName())
                    }
                }
            )

            Action(
                title = "Импорт из бэкапа",
                onClick = { importLauncher.launch(MimeAll) }
            )

            Spacer(Modifier.height(20.dp))
            SectionTitle(text = "Прочее")
            Spacer(Modifier.height(8.dp))

            Action(title = "О приложении", onClick = onAbout)
            Action(title = "Удалить все документы", onClick = { wipe = true })

            Spacer(Modifier.height(32.dp))
        }
    }
}