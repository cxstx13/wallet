package ru.rfdocs.wallet.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import ru.rfdocs.wallet.ui.components.DocumentStyleCard
import ru.rfdocs.wallet.ui.components.PhotoThumbStrip
import ru.rfdocs.wallet.ui.components.PhotoViewerDialog
import ru.rfdocs.wallet.ui.components.SectionTitle
import ru.rfdocs.wallet.ui.components.WalletBackground
import ru.rfdocs.wallet.ui.components.WalletField
import ru.rfdocs.wallet.ui.input.DateMask
import ru.rfdocs.wallet.ui.input.Masks
import ru.rfdocs.wallet.ui.theme.Palette
import java.util.UUID

private fun maskOrNull(mask: String?): String? =
    if (mask.isNullOrBlank()) null else mask

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
                existing?.fields?.forEach { add(it.copy(mask = maskOrNull(it.mask))) }
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
                            mask = maskOrNull(spec.mask),
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

    val previewCard = DocCard(
        id = existing?.id ?: "preview",
        typeKey = resolvedTypeKey,
        title = cardTitle.ifBlank { "Документ" },
        fields = fields.toList()
    )

    fun save() {
        val invalidDate = fields.firstOrNull { field ->
            field.mask == Masks.DATE && DateMask.error(field.value) != null
        }
        if (invalidDate != null) {
            scope.launch {
                snackbarHostState.showSnackbar("Проверьте дату: «${invalidDate.label}»")
            }
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
                    snackbarHostState.showSnackbar(error.message ?: "Не удалось сохранить")
                }
            }
    }

    Scaffold(
        modifier = Modifier.imePadding(),
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 3.dp,
                shadowElevation = 12.dp
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
                            .height(52.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text("Сохранить", style = MaterialTheme.typography.titleMedium)
                    }
                }
            }
        }
    ) { padding ->
        WalletBackground(modifier = Modifier.padding(padding)) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
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
                        text = if (existing == null) "Новый документ" else "Редактирование",
                        style = MaterialTheme.typography.headlineSmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }

                DocumentStyleCard(
                    card = previewCard,
                    showNumber = true,
                    modifier = Modifier.fillMaxWidth()
                )

                if (isCustom) {
                    WalletField(label = "Название карточки") {
                        androidx.compose.foundation.text.BasicTextField(
                            value = cardTitle,
                            onValueChange = { cardTitle = it },
                            singleLine = true,
                            textStyle = MaterialTheme.typography.bodyLarge.copy(
                                color = MaterialTheme.colorScheme.onSurface
                            ),
                            cursorBrush = androidx.compose.ui.graphics.SolidColor(Palette.Gold),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                SectionTitle(text = "Поля документа")

                fields.forEachIndexed { index, field ->
                    if (isCustom) {
                        CustomFieldRow(
                            field = field,
                            onLabelChange = { value -> fields[index] = field.copy(label = value) },
                            onValueChange = { value -> fields[index] = field.copy(value = value) },
                            onRemove = { fields.removeAt(index) }
                        )
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
                        shape = RoundedCornerShape(16.dp)
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

                SectionTitle(text = "Фотографии")

                Button(
                    onClick = { picker.launch("image/*") },
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("Добавить фото")
                }

                PhotoThumbStrip(
                    photoIds = photoIds.toList(),
                    onOpen = { photoId -> viewingPhoto = photoId },
                    onRemove = { photoId -> photoIds.remove(photoId) }
                )

                Spacer(Modifier.height(24.dp))
            }
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