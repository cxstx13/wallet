package ru.rfdocs.wallet.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ru.rfdocs.wallet.ui.components.SectionTitle

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
fun SettingsContent(
    biometricAvailable: Boolean,
    biometricEnabled: Boolean,
    onBiometric: (Boolean) -> Unit,
    screenshotBlocked: Boolean,
    onScreenshot: (Boolean) -> Unit,
    onPin: () -> Unit,
    darkTheme: Boolean,
    onDark: (Boolean) -> Unit,
    onExport: () -> Unit,
    onImport: () -> Unit,
    onAbout: () -> Unit,
    onWipe: () -> Unit
) {
    SectionTitle(text = "Безопасность")
    Spacer(Modifier.height(8.dp))

    Toggle(
        title = "Вход по отпечатку",
        subtitle = if (biometricAvailable) "Быстрый вход" else "Не настроен",
        checked = biometricEnabled,
        enabled = biometricAvailable,
        onChange = onBiometric
    )

    Toggle(
        title = "Запрет скриншотов",
        subtitle = if (screenshotBlocked) "Заблокировано" else "Разрешено",
        checked = screenshotBlocked,
        enabled = true,
        onChange = onScreenshot
    )

    Action(title = "Сменить PIN-код", onClick = onPin)

    Spacer(Modifier.height(20.dp))
    SectionTitle(text = "Оформление")
    Spacer(Modifier.height(8.dp))

    Toggle(
        title = "Тёмная тема",
        subtitle = "AMOLED",
        checked = darkTheme,
        enabled = true,
        onChange = onDark
    )

    Spacer(Modifier.height(20.dp))
    SectionTitle(text = "Резервная копия")
    Spacer(Modifier.height(8.dp))

    Action(title = "Экспорт бэкапа", onClick = onExport)
    Action(title = "Импорт из бэкапа", onClick = onImport)

    Spacer(Modifier.height(20.dp))
    SectionTitle(text = "Прочее")
    Spacer(Modifier.height(8.dp))

    Action(title = "О приложении", onClick = onAbout)
    Action(title = "Удалить все документы", onClick = onWipe)

    Spacer(Modifier.height(32.dp))
}