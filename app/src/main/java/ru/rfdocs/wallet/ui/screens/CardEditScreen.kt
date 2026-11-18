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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
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
import ru.rfdocs.wallet.ui.components.DocFieldEditor
import ru.rfdocs.wallet.ui.components.PhotoThumbStrip
import ru.rfdocs.wallet.ui.components.PhotoViewerDialog
import ru.rfdocs.wallet.ui.components.SectionTitle
import ru.rfdocs.wallet.ui.components.WalletField
import ru.rfdocs.wallet.ui.components.DocumentStyleCard
import ru.rfdocs.wallet.ui.input.DateMask
import ru.rfdocs.wallet.ui.input.Masks
import ru.rfdocs.wallet.ui.theme.Palette
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
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val existing = remember(cardId) { cardId?.let { DocRepository.get(it) } }
    val template = remember(typeKey, existing) {
        DocTemplates.byKey(existing?.typeKey ?: typeKey.orEmpty())
    }
    val resolvedTypeKey = existing?.typeKey ?: template?.key ?: typeKey.orEmpty()
    val isCustom = resolvedTypeKey.equals("CUSTOM", ignoreCase = true)

    var cardTitle by remember {
        mutableStateOf(existing?.title ?: template?.title ?: "Документ")
    }

    val fields = remember(template, existing) {
        mutableStateListOf<DocField>().apply {
            val specs = template?.fields.orEmpty()
            if (specs.isEmpty()) {
                existing?.fields?.forEach { add(it) }
                if (isEmpty()) {
                    add(DocField(key = "field_1", label = "Поле 1", value = ""))
                }
            } else {
                specs.forEach { spec ->
                    val value = existing?.fields?.firstOrNull { it.key == spec.key }?.value ?: ""
                    add(
                        DocField(
                            key = spec.key,
                            label = spec.label,
                            value = value,
                            mask = spec.mask,
                            secret = spec.secret,
                            phone = spec.phone
                        )
                    )
                }
            }
        }
    }

    val photoIds = remember(existing) {
        mutableStateListOf<String>().apply { addAll(existing?.photoIds ?: emptyList()) }
    }
    var viewingPhoto by remember { mutableStateOf<String?>(null) }

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

    val previewFields = fields.toList()
    val previewCard = DocCard(
        id = existing?.id ?: "preview",
        typeKey = resolvedTypeKey,
        title = cardTitle.ifBlank { "Документ" },
        fields = previewFields
    )

    fun save() {
        val invalidDate = fields.firstOrNull { field ->
            field.mask == Masks.DATE && DateMask.error(field.value) != null
        }
        if (invalidDate != null) {
            scope.launch { snackbarHostState.showSnackbar("Проверьте дату в поле «${invalidDate.label}»") }
            return
        }

        val cleaned = if (isCustom) {
            fields
                .filter { it.label.isNotBlank() || it.value.isNotBlank() }
                .map { field ->
                    field.copy(
                        key = field.key.ifBlank { UUID.randomUUID().toString() },
                        label = field.label.ifBlank { "Поле" },
                        value = field.value.trim()
                    )
                }
        } else {
            fields.map { field -> field.copy(value = field.value.trim()) }
        }

        val now = System.currentTimeMillis()
        val card = if (existing != null) {
            existing.copy(
                title = cardTitle.trim().ifBlank { "Документ" },
                fields = cleaned,
                photoIds = photoIds.toList(),
                updatedAt = now
            )
        } else {
            DocCard(
                id = UUID.randomUUID().toString(),
                typeKey = resolvedTypeKey,
                title = cardTitle.trim().ifBlank { "Документ" },
                fields = cleaned,
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
                        text = if (existing == null) "Новый документ" else "Редактирование",
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            DocumentStyleCard(
                card = previewCard,
                showNumber = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(4.dp))

            if (isCustom || fields.any { it.mask.isNullOrBlank() && it.key.startsWith("field_") }) {
                WalletField(label = "Название карточки") {
                    androidx.compose.foundation.text.BasicTextField(
                        value = cardTitle,
                        onValueChange = { cardTitle = it },
                        singleLine = true,
                        textStyle = MaterialTheme.typography.bodyLarge.copy(
                            color = MaterialTheme.colorScheme.onSurface
                        ),
                        cursorBrush = androidx.compose.ui.graphics.SolidColor(Palette.Gold),
                        modifier = Modifier.fillMaxWidth(),
                        decorationBox = { inner ->
                            if (cardTitle.isEmpty()) {
                                Text(
                                    text = "Например, пропуск или абонемент",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                )
                            }
                            inner()
                        }
                    )
                }
            }

            SectionTitle(text = "Поля документа")
            Spacer(Modifier.height(4.dp))

            fields.forEachIndexed { index, field ->
                if (isCustom) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            WalletField(
                                label = "Название поля",
                                modifier = Modifier.weight(1f)
                            ) {
                                androidx.compose.foundation.text.BasicTextField(
                                    value = field.label,
                                    onValueChange = { value ->
                                        fields[index] = field.copy(label = value)
                                    },
                                    singleLine = true,
                                    textStyle = MaterialTheme.typography.bodyLarge.copy(
                                        color = MaterialTheme.colorScheme.onSurface
                                    ),
                                    cursorBrush = androidx.compose.ui.graphics.SolidColor(Palette.Gold),
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                            IconButton(
                                onClick = { fields.removeAt(index) },
                                modifier = Modifier.padding(start = 4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Delete,
                                    contentDescription = "Удалить поле",
                                    tint = Palette.Danger
                                )
                            }
                        }
                        Spacer(Modifier.height(8.dp))
                        WalletField(label = "Значение") {
                            androidx.compose.foundation.text.BasicTextField(
                                value = field.value,
                                onValueChange = { value ->
                                    fields[index] = field.copy(value = value)
                                },
                                singleLine = true,
                                textStyle = MaterialTheme.typography.bodyLarge.copy(
                                    color = MaterialTheme.colorScheme.onSurface
                                ),
                                cursorBrush = androidx.compose.ui.graphics.SolidColor(Palette.Gold),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                } else {
                    DocFieldEditor(
                        field = field,
                        onFieldChange = { updated -> fields[index] = updated }
                    )
                }
            }

            if (isCustom) {
                OutlinedButton(
                    onClick = {
                        fields.add(
                            DocField(
                                key = "field_" + UUID.randomUUID().toString().take(8),
                                label = "Поле ${fields.size + 1}",
                                value = ""
                            )
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("Добавить поле")
                }
            }

            Spacer(Modifier.height(8.dp))
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