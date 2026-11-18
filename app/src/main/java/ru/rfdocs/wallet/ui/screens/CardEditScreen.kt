package ru.rfdocs.wallet.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Close
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
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.text.KeyboardOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ru.rfdocs.wallet.data.model.DocCard
import ru.rfdocs.wallet.data.model.DocField
import ru.rfdocs.wallet.data.model.DocTemplates
import ru.rfdocs.wallet.data.repo.DocRepository
import ru.rfdocs.wallet.data.repo.PhotoStore
import ru.rfdocs.wallet.ui.WalletViewModel
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CardEditScreen(
    viewModel: WalletViewModel,
    cardId: String?,
    typeKey: String?,
    onBack: () -> Unit,
    onDone: (String) -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
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

    val picker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        scope.launch {
            val bytes = withContext(Dispatchers.IO) {
                runCatching {
                    context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                }.getOrNull()
            }
            if (bytes == null || bytes.isEmpty()) {
                snackbarHostState.showSnackbar("Не удалось прочитать фото")
                return@launch
            }
            val saved = withContext(Dispatchers.IO) { PhotoStore.save(context, bytes) }
            photoIds.add(saved)
        }
    }

    fun save() {
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
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
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
                OutlinedTextField(
                    value = values[spec.key].orEmpty(),
                    onValueChange = { raw ->
                        values[spec.key] = applyMask(raw, spec.mask)
                    },
                    label = { Text(spec.label) },
                    singleLine = true,
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

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = { picker.launch("image/*") },
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(
                        Icons.Filled.AddPhotoAlternate,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("Добавить фото")
                }
                Spacer(Modifier.width(12.dp))
                Text(
                    text = if (photoIds.isEmpty()) "Фото не добавлены" else "Фото: ${photoIds.size}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            photoIds.forEach { photoId ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Фото ${photoId.take(8)}",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.weight(1f),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    IconButton(onClick = { photoIds.remove(photoId) }) {
                        Icon(
                            Icons.Filled.Close,
                            contentDescription = "Удалить фото",
                            tint = Color(0xFFE05252)
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}

private fun isNumericMask(mask: String?): Boolean {
    if (mask.isNullOrBlank()) return false
    return mask.all { it == '0' || !it.isLetter() }
}

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