package ru.rfdocs.wallet.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ru.rfdocs.wallet.ui.components.GlassPanel
import ru.rfdocs.wallet.ui.components.SectionTitle

@Composable
fun AboutScreen(onBack: () -> Unit) {
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
                text = "О приложении",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        Spacer(Modifier.height(12.dp))

        GlassPanel(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "Мои документы",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "Офлайн‑кошелёк документов РФ · v2.0",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(Modifier.height(20.dp))
        SectionTitle(text = "Что внутри")
        Spacer(Modifier.height(10.dp))
        GlassPanel(modifier = Modifier.fillMaxWidth()) {
            listOf(
                "Паспорт РФ, загранпаспорт, СНИЛС, ИНН",
                "Водительское удостоверение, СТС, ПТС / ЭПТС",
                "ОСАГО, КАСКО, полисы ОМС и ДМС",
                "Свидетельство о рождении, банковские реквизиты",
                "Своя карточка с произвольными полями"
            ).forEach { line ->
                Text(
                    text = "•  $line",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(vertical = 3.dp)
                )
            }
        }

        Spacer(Modifier.height(20.dp))
        SectionTitle(text = "Безопасность")
        Spacer(Modifier.height(10.dp))
        GlassPanel(modifier = Modifier.fillMaxWidth()) {
            listOf(
                "Данные не покидают устройство: разрешения INTERNET нет",
                "Шифрование AES‑GCM, ключ в Android Keystore",
                "Вход по PIN‑коду и отпечатку пальца",
                "Скриншоты блокируются FLAG_SECURE (отключается в настройках)",
                "Резервная копия — отдельный файл, защищённый паролем"
            ).forEach { line ->
                Text(
                    text = "•  $line",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(vertical = 3.dp)
                )
            }
        }

        Spacer(Modifier.height(20.dp))
        SectionTitle(text = "Важно")
        Spacer(Modifier.height(10.dp))
        GlassPanel(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "Приложение — личный справочник реквизитов. Оно не является официальным " +
                    "документом и не заменяет бумажный или государственный цифровой документ. " +
                    "В приложении нет герба РФ, флага РФ и логотипов государственных сервисов.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        Spacer(Modifier.height(32.dp))
    }
}