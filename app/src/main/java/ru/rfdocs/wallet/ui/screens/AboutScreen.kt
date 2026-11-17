package ru.rfdocs.wallet.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.statusBarsPadding
import ru.rfdocs.wallet.BuildConfig
import ru.rfdocs.wallet.ui.components.GlassPanel
import ru.rfdocs.wallet.ui.components.SectionTitle

@Composable
fun AboutScreen(onBack: () -> Unit) {
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
                text = "О приложении",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
        GlassPanel(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "Мои документы",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Версия ${BuildConfig.VERSION_NAME}",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.height(20.dp))
        SectionTitle(text = "Конфиденциальность")
        Spacer(modifier = Modifier.height(8.dp))
        GlassPanel(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "Все данные хранятся только на этом устройстве в зашифрованном виде. " +
                    "Приложение не имеет доступа к сети: в нём нет разрешения INTERNET и сетевых библиотек, " +
                    "поэтому документы не могут быть переданы третьим лицам. " +
                    "Обработка персональных данных полностью локальная (152-ФЗ).",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        Spacer(modifier = Modifier.height(20.dp))
        SectionTitle(text = "Дисклеймер")
        Spacer(modifier = Modifier.height(8.dp))
        GlassPanel(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "Приложение является личным офлайн-справочником реквизитов. " +
                    "Оно не является официальным документом, не заменяет бумажный документ " +
                    "или государственный цифровой документ и не имеет юридической силы. " +
                    "Не содержит герба РФ, флага РФ и логотипов государственных сервисов.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        Spacer(modifier = Modifier.height(20.dp))
        SectionTitle(text = "Как защищены данные")
        Spacer(modifier = Modifier.height(8.dp))
        GlassPanel(modifier = Modifier.fillMaxWidth()) {
            listOf(
                "PIN-код и отпечаток пальца для входа",
                "Шифрование AES-256-GCM с ключом в Android Keystore",
                "Запрет скриншотов и записи экрана (FLAG_SECURE)",
                "Резервные копии шифруются отдельным паролем",
                "Тексты документов не покидают устройство"
            ).forEach { line ->
                Text(
                    text = "•  $line",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(vertical = 3.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(40.dp))
    }
}
