package ru.rfdocs.wallet.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ru.rfdocs.wallet.data.model.DocCard
import ru.rfdocs.wallet.data.model.DocField
import ru.rfdocs.wallet.data.model.DocTemplates
import ru.rfdocs.wallet.data.repo.DocRepository
import ru.rfdocs.wallet.data.repo.PhotoStore
import ru.rfdocs.wallet.ui.WalletViewModel
import ru.rfdocs.wallet.ui.components.PhotoThumbStrip
import ru.rfdocs.wallet.ui.components.PhotoViewerDialog
import ru.rfdocs.wallet.ui.components.SectionTitle
import ru.rfdocs.wallet.ui.theme.Palette
import java.util.UUID

private fun isNumericMask(mask: String?): Boolean {
    if (mask.isNullOrBlank()) return false
    return mask.all { it == '0' || !it.isLetter() }
}

private fun digitCountOf(mask: String): Int = mask.count { it == '0' }

private fun applyMask(raw: String, mask: String?): String {
    if (mask.isNullOrBlank()) return raw
    val digits = raw.filter { it.isDigit() }
    val builder = StringBuilder()
    var index = 0
    for (ch in mask) {
        if (index >= digits.length) break
        if (ch == '0') {
            builder.append(digits[index])
            index++
        } else {
            builder.append(ch)
        }
    }
    return builder.toString()
}

private fun isRequiredLabel(label: String): Boolean {
    val lower = label.lowercase()
    return lower.contains("фам") || lower.contains("имя") ||
        lower.contains("серия") || lower.contains("номер") ||
        lower.contains("инн") || lower.contains("снилс")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CardEditScreen(
    viewModel: WalletViewModel,
    cardId: String?,
    typeKey: String?,
    onBack: () -> Unit,
    onDone: (String) -> Unit
) {
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val existing = remember(cardId) { cardId?.let { DocRepository.get(it) } }
    val template = remember(typeKey, existing) {
        DocTemplates.byKey(existing?.typeKey ?: typeKey.orEmpty())
    }
    val title = template?.title ?: existing?.title ?: "Документ"

    val values = remember(template, existing) {
        mutableStateMapOf<String, String>().apply {
            template?.fields?.forEach { spec ->
                put(spec.key, existing?.fields?.firstOrNull { it.key == spec.key }?.value ?: "")
            }
            existing?.fields?.forEach { field ->
                if (!containsKey(field.key)) put(field.key, field.value)
            }
        }
    }

    val photoIds = remember(existing) {
        mutableStateListOf<String>().apply { addAll(existing?.photoIds ?: emptyList()) }
    }
    var viewingPhoto by remember { mutableStateOf<String?>(null) }
    val errorKeys = remember(template) { mutableStateMapOf<String, String>() }

    val picker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        scope.launch {
            val saved = withContext(Dispatchers.IO) {
                val bytes = runCatching {
                    context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                }.getOrNull()
                if (bytes == null || bytes.isEmpty()) null else PhotoStore.save(context, bytes)
            }
            if (saved == null) {
                snackbarHostState.showSnackbar("Не удалось прочитать фото")
            } else {
                photoIds.add(saved)
            }
        }
    }

    fun validate(): Boolean {
        errorKeys.clear()
        template?.fields?.forEach { spec ->
            val value = values[spec.key].orEmpty().trim()
            val mask = spec.mask
            when {
                value.isEmpty() && isRequiredLabel(spec.label) -> {
                    errorKeys[spec.key] = "Обязательное поле"
                }

                !mask.isNullOrBlank() && value.isNotEmpty() &&
                    digitCountOf(mask) > 0 &&
                    value.filter { it.isDigit() }.length != digitCountOf(mask) -> {
                    errorKeys[spec.key] = "Неполный формат: нужно ${digitCountOf(mask)} цифр"
                }
            }
        }
        return errorKeys.isEmpty()
    }

    fun save() {
        if (!validate()) {
            scope.launch {
                snackbarHostState.showSnackbar("Проверьте выделенные поля")
            }
            return
        }

        val now = System.currentTimeMillis()
        val builtFields = buildList {
            template?.fields?.forEach { spec ->
                add(
                    DocField(
                        key = spec.key,
                        label = spec.label,
                        value = values[spec.key].orEmpty().trim(),
                        mask = spec.mask,
                        secret = spec.secret,
                        phone = spec.phone
                    )
                )
            }
            existing?.fields?.forEach { field ->
                if (none { it.key == field.key }) add(field)
            }
        }

        val card = if (existing != null) {
            existing.copy(
                title = title,
                fields = builtFields,
                photoIds = photoIds.toList(),
                updatedAt = now
            )
        } else {
            DocCard(
                id = UUID.randomUUID().toString(),
                typeKey = template?.key ?: typeKey.orEmpty(),
                title = title,
                fields = builtFields,
                photoIds = photoIds.toList(),
                createdAt = now,
                updatedAt = now
            )
        }

        runCatching { DocRepository.upsert(card) }
            .onSuccess { onDone(card.id) }
            .onFailure { error ->
                scope.launch {
                    snackbarHostState.showSnackbar(error.message ?: "Не удалось сохранить документ")
                }
            }
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .imePadding(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = title,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Назад"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 3.dp,
                shadowElevation = 10.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Button(
                        onClick = { save() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                    ) {
                        Text("Сохранить", style = MaterialTheme.typography.titleMedium)
                    }
                }
            }
        }
    ) { padding ->
        if (template == null && existing == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text("Шаблон документа не найден")
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            template?.fields?.forEach { spec ->
                val error = errorKeys[spec.key]
                OutlinedTextField(
                    value = values[spec.key].orEmpty(),
                    onValueChange = { raw ->
                        values[spec.key] = applyMask(raw, spec.mask)
                        if (errorKeys.containsKey(spec.key)) errorKeys.remove(spec.key)
                    },
                    label = { Text(spec.label) },
                    singleLine = true,
                    isError = error != null,
                    supportingText = if (error != null) {
                        { Text(text = error, color = Palette.Danger) }
                    } else {
                        null
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = if (isNumericMask(spec.mask)) KeyboardType.Number else KeyboardType.Text
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            existing?.fields?.forEach { field ->
                if (template?.fields?.none { it.key == field.key } != false) {
                    OutlinedTextField(
                        value = values[field.key].orEmpty(),
                        onValueChange = { raw -> values[field.key] = raw },
                        label = { Text(field.label) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Spacer(Modifier.height(4.dp))
            SectionTitle(text = "Фото документа")
            Spacer(Modifier.height(2.dp))
            Text(
                text = "Сфотографируйте документ и заполните поля вручную — фото хранится в зашифрованном виде.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = { picker.launch("image/*") },
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("Добавить фото")
                }
                Spacer(Modifier.width(12.dp))
                Text(
                    text = if (photoIds.isEmpty()) "Нет фото" else "Фото: ${photoIds.size}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            PhotoThumbStrip(
                photoIds = photoIds.toList(),
                onOpen = { photoId -> viewingPhoto = photoId },
                onRemove = { photoId -> photoIds.remove(photoId) }
            )

            Spacer(Modifier.height(16.dp))
        }
    }

    viewingPhoto?.let { photoId ->
        PhotoViewerDialog(
            photoId = photoId,
            onDismiss = { viewingPhoto = null },
            onDelete = { removed ->
                photoIds.remove(removed)
                viewingPhoto = null
            }
        )
    }
}