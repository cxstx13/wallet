package ru.rfdocs.wallet.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ru.rfdocs.wallet.data.model.DocCard
import ru.rfdocs.wallet.data.model.DocField
import ru.rfdocs.wallet.data.repo.DocRepository
import ru.rfdocs.wallet.ui.components.CapsuleButton
import ru.rfdocs.wallet.ui.components.GlassTextField
import ru.rfdocs.wallet.ui.components.WalletBackground
import ru.rfdocs.wallet.ui.theme.WalletTheme
import java.util.UUID

private data class Preset(
    val key: String,
    val label: String,
    val mask: String? = null
)

private data class FieldState(
    val key: String,
    val label: String,
    val value: String,
    val mask: String?,
    val custom: Boolean
)

private val DATE_MASK = "00.00.0000"

private fun firstNameFields(): List<Preset> = listOf(
    Preset("surname", "Фамилия"),
    Preset("name", "Имя"),
    Preset("patronymic", "Отчество")
)

private fun presetsFor(typeKey: String): List<Preset> {
    val key = typeKey.lowercase()
    return when {
        key.contains("foreign") || key.contains("zagran") -> listOf(
            Preset("surname", "Фамилия"),
            Preset("name", "Имя"),
            Preset("latin_name", "ФИО латиницей"),
            Preset("number", "Номер документа", "00 0000000"),
            Preset("issue_date", "Дата выдачи", DATE_MASK),
            Preset("valid_until", "Действителен до", DATE_MASK),
            Preset("issued_by", "Кем выдан")
        )

        key.contains("passport") -> firstNameFields() + listOf(
            Preset("birth_date", "Дата рождения", DATE_MASK),
            Preset("number", "Серия и номер", "00 00 000000"),
            Preset("issue_date", "Дата выдачи", DATE_MASK),
            Preset("issued_by", "Кем выдан"),
            Preset("department", "Код подразделения", "000-000"),
            Preset("address", "Адрес регистрации")
        )

        key.contains("driver") || key.contains("vu") -> firstNameFields() + listOf(
            Preset("birth_date", "Дата рождения", DATE_MASK),
            Preset("number", "Серия и номер", "00 00 000000"),
            Preset("issue_date", "Дата выдачи", DATE_MASK),
            Preset("valid_until", "Действителен до", DATE_MASK),
            Preset("issued_by", "Кем выдан"),
            Preset("categories", "Категории"),
            Preset("experience", "Стаж")
        )

        key.contains("sts") -> listOf(
            Preset("plate", "Госномер"),
            Preset("vin", "VIN"),
            Preset("model", "Марка и модель"),
            Preset("year", "Год выпуска"),
            Preset("number", "Серия и номер", "00 00 000000"),
            Preset("issue_date", "Дата выдачи", DATE_MASK),
            Preset("owner", "Собственник")
        )

        key.contains("pts") -> listOf(
            Preset("number", "Номер ПТС / ЭПТС"),
            Preset("vin", "VIN"),
            Preset("model", "Марка и модель"),
            Preset("engine", "Двигатель"),
            Preset("power", "Мощность, л.с."),
            Preset("issue_date", "Дата выдачи", DATE_MASK),
            Preset("owner", "Собственник")
        )

        key.contains("osago") || key.contains("kasko") -> listOf(
            Preset("insurer", "Страховая компания"),
            Preset("number", "Серия и номер", "000 0000000"),
            Preset("valid_from", "Действует с", DATE_MASK),
            Preset("valid_until", "Действует по", DATE_MASK),
            Preset("insurer_phone", "Телефон страховой")
        )

        key.contains("oms") -> firstNameFields() + listOf(
            Preset("birth_date", "Дата рождения", DATE_MASK),
            Preset("number", "Номер полиса", "0000000000000000"),
            Preset("insurer", "Страховая компания")
        )

        key.contains("dms") -> listOf(
            Preset("insurer", "Страховая компания"),
            Preset("number", "Номер полиса"),
            Preset("valid_from", "Действует с", DATE_MASK),
            Preset("valid_until", "Действует по", DATE_MASK),
            Preset("assistance_phone", "Телефон ассистанса")
        )

        key.contains("snils") -> firstNameFields() + listOf(
            Preset("number", "Номер СНИЛС", "000-000-000 00"),
            Preset("birth_date", "Дата рождения", DATE_MASK)
        )

        key.contains("inn") -> firstNameFields() + listOf(
            Preset("number", "ИНН", "000000000000"),
            Preset("issue_date", "Дата постановки на учёт", DATE_MASK),
            Preset("fns_code", "Код ФНС", "0000")
        )

        key.contains("birth") -> listOf(
            Preset("surname", "Фамилия"),
            Preset("name", "Имя"),
            Preset("birth_date", "Дата рождения", DATE_MASK),
            Preset("birth_place", "Место рождения"),
            Preset("number", "Серия и номер", "00 000000"),
            Preset("issue_date", "Дата выдачи", DATE_MASK),
            Preset("registry_office", "ЗАГС")
        )

        key.contains("bank") -> listOf(
            Preset("bank", "Банк"),
            Preset("bik", "БИК", "000000000"),
            Preset("account", "Расчётный счёт"),
            Preset("corr_account", "Корр. счёт"),
            Preset("inn", "ИНН", "000000000000"),
            Preset("kpp", "КПП", "000000000"),
            Preset("recipient", "Получатель")
        )

        else -> emptyList()
    }
}

private fun titleForType(typeKey: String): String {
    val key = typeKey.lowercase()
    return when {
        key.contains("foreign") || key.contains("zagran") -> "Загранпаспорт"
        key.contains("passport") -> "Паспорт РФ"
        key.contains("driver") || key.contains("vu") -> "Водительское удостоверение"
        key.contains("sts") -> "СТС"
        key.contains("pts") -> "ПТС / ЭПТС"
        key.contains("osago") -> "ОСАГО"
        key.contains("kasko") -> "КАСКО"
        key.contains("oms") -> "Полис ОМС"
        key.contains("dms") -> "ДМС"
        key.contains("snils") -> "СНИЛС"
        key.contains("inn") -> "ИНН"
        key.contains("birth") -> "Свидетельство о рождении"
        key.contains("bank") -> "Банковские реквизиты"
        else -> "Документ"
    }
}

@Composable
fun CardEditScreen(
    cardId: String?,
    typeKey: String?,
    onBack: () -> Unit,
    onDone: (String) -> Unit
) {
    val cards by DocRepository.cards.collectAsState()
    val existing = remember(cardId, cards) {
        cardId?.let { id -> cards.firstOrNull { it.id == id } }
    }
    val resolvedType = remember(existing, typeKey) {
        existing?.typeKey ?: typeKey.orEmpty()
    }

    var title by remember(existing?.id, resolvedType) {
        mutableStateOf(existing?.title ?: titleForType(resolvedType))
    }

    val fields = remember(existing?.id, resolvedType) {
        mutableStateListOf<FieldState>().apply {
            val source = existing?.fields
            if (source != null) {
                source.forEach { field ->
                    add(
                        FieldState(
                            key = field.key,
                            label = field.label,
                            value = field.value,
                            mask = field.mask,
                            custom = false
                        )
                    )
                }
            } else {
                presetsFor(resolvedType).forEach { preset ->
                    add(
                        FieldState(
                            key = preset.key,
                            label = preset.label,
                            value = "",
                            mask = preset.mask,
                            custom = false
                        )
                    )
                }
            }
        }
    }

    fun save() {
        val now = System.currentTimeMillis()
        val docFields = fields
            .filter { it.label.isNotBlank() || it.value.isNotBlank() }
            .map { state ->
                DocField(
                    key = state.key,
                    label = state.label.ifBlank { "Поле" },
                    value = state.value.trim(),
                    mask = state.mask
                )
            }

        val card = if (existing != null) {
            existing.copy(
                title = title.trim().ifBlank { "Документ" },
                fields = docFields,
                updatedAt = now
            )
        } else {
            DocCard(
                id = UUID.randomUUID().toString(),
                typeKey = resolvedType,
                title = title.trim().ifBlank { "Документ" },
                fields = docFields,
                photoIds = emptyList(),
                createdAt = now,
                updatedAt = now
            )
        }

        DocRepository.upsert(card)
        onDone(card.id)
    }

    WalletBackground {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp)
        ) {
            Spacer(Modifier.height(8.dp))

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
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (existing == null) "Новый документ" else "Редактирование",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Заполните реквизиты",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                SavePill(onClick = { save() })
            }

            Spacer(Modifier.height(18.dp))

            GlassTextField(
                label = "Название карточки",
                value = title,
                onValueChange = { title = it },
                placeholder = "Например, Паспорт РФ"
            )

            Spacer(Modifier.height(16.dp))

            fields.forEachIndexed { index, state ->
                Column(modifier = Modifier.fillMaxWidth()) {
                    if (state.custom) {
                        GlassTextField(
                            label = "Название поля",
                            value = state.label,
                            onValueChange = { value ->
                                fields[index] = fields[index].copy(label = value)
                            }
                        )
                        Spacer(Modifier.height(8.dp))
                        GlassTextField(
                            label = "Значение",
                            value = state.value,
                            onValueChange = { value ->
                                fields[index] = fields[index].copy(value = value)
                            }
                        )
                    } else {
                        GlassTextField(
                            label = state.label,
                            value = state.value,
                            onValueChange = { value ->
                                fields[index] = fields[index].copy(value = value)
                            },
                            singleLine = !state.label.lowercase().contains("адрес")
                        )
                    }
                    Spacer(Modifier.height(12.dp))
                }
            }

            CapsuleButton(
                text = "Добавить поле",
                filled = false,
                onClick = {
                    fields.add(
                        FieldState(
                            key = "custom_" + UUID.randomUUID().toString().take(8),
                            label = "",
                            value = "",
                            mask = null,
                            custom = true
                        )
                    )
                }
            )

            Spacer(Modifier.height(28.dp))
            CapsuleButton(text = "Сохранить", filled = true, onClick = { save() })
            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
private fun SavePill(onClick: () -> Unit) {
    val extras = WalletTheme.extra
    val shape = RoundedCornerShape(14.dp)

    Box(
        modifier = Modifier
            .clip(shape)
            .background(extras.chipSelectedFill)
            .border(1.dp, androidx.compose.ui.graphics.Color.Transparent, shape)
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        Text(
            text = "Сохранить",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            color = extras.chipSelectedText
        )
    }
}