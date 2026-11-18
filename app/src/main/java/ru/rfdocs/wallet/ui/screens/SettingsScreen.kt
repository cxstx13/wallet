package ru.rfdocs.wallet.ui.screens

import android.app.Activity
import android.net.Uri
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
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
import ru.rfdocs.wallet.ui.components.WalletBackground

private val MimeBackup = arrayOf("application/octet-stream", "*/*")

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

            SettingsContent(
                biometricAvailable = bioOk,
                biometricEnabled = bio,
                onBiometric = { value ->
                    bio = value
                    SecurePrefs.setBiometricEnabled(context, value)
                },
                screenshotBlocked = blockShots,
                onScreenshot = { value -> applyShotPolicy(value) },
                onPin = { changePin = true },
                darkTheme = darkTheme,
                onDark = onToggleDarkTheme,
                onExport = {
                    if (DocRepository.cardCount() == 0) {
                        Toast.makeText(context, "Нет документов", Toast.LENGTH_SHORT).show()
                    } else {
                        exportLauncher.launch(BackupManager.suggestedFileName())
                    }
                },
                onImport = { importLauncher.launch(MimeBackup) },
                onAbout = onAbout,
                onWipe = { wipe = true }
            )
        }
    }
}