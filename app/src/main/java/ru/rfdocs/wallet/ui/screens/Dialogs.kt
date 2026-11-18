package ru.rfdocs.wallet.ui.screens

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import ru.rfdocs.wallet.data.model.DocCard
import ru.rfdocs.wallet.ui.components.QrCodeView
import ru.rfdocs.wallet.ui.input.FieldMasks
import ru.rfdocs.wallet.ui.theme.Palette
import ru.rfdocs.wallet.ui.theme.WalletTheme

@Composable
private fun AnimatedDialog(
    onDismissRequest: () -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {
    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(usePlatformDefaultWidth = true)
    ) {
        var visible by remember { mutableStateOf(false) }
        LaunchedEffect(Unit) { visible = true }

        val progress by animateFloatAsState(
            targetValue = if (visible) 1f else 0f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioLowBouncy,
                stiffness = Spring.StiffnessMediumLow
            ),
            label = "dialogProgress"
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .graphicsLayer {
                    val scale = 0.92f + 0.08f * progress
                    scaleX = scale
                    scaleY = scale
                    alpha = progress
                }
                .clip(RoundedCornerShape(26.dp))
                .background(MaterialTheme.colorScheme.surface)
                .border(1.dp, WalletTheme.extra.glassBorder, RoundedCornerShape(26.dp))
                .padding(20.dp),
            content = content
        )
    }
}

@Composable
private fun DialogTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurface
    )
}

@Composable
private fun DialogButton(
    text: String,
    onClick: () -> Unit,
    accent: Boolean = false,
    enabled: Boolean = true
) {
    val haptics = LocalHapticFeedback.current
    val color = when {
        !enabled -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
        accent -> WalletTheme.extra.monoAccent
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    Box(
        modifier = Modifier
            .clip(CircleShape)
            .clickable(enabled = enabled) {
                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                onClick()
            }
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            color = color
        )
    }
}

@Composable
fun QrDialog(
    title: String,
    content: String,
    onDismiss: () -> Unit
) {
    AnimatedDialog(onDismissRequest = onDismiss) {
        DialogTitle(text = title)
        Spacer(Modifier.height(16.dp))
        QrCodeView(
            content = content,
            modifier = Modifier
                .fillMaxWidth()
                .height(250.dp)
        )
        Spacer(Modifier.height(16.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            DialogButton(text = "Закрыть", onClick = onDismiss, accent = true)
        }
    }
}

fun buildShareText(card: DocCard, selectedKeys: Set<String>): String {
    val builder = StringBuilder()
    builder.append(card.title).append('\n')
    card.fields.forEach { field ->
        if (field.value.isNotBlank() && selectedKeys.contains(field.key)) {
            val value = if (field.mask.isNullOrBlank()) {
                field.value
            } else {
                FieldMasks.display(field.value, field.mask)
            }
            builder.append(field.label).append(": ").append(value).append('\n')
        }
    }
    return builder.toString().trim()
}

@Composable
fun ShareFieldsDialog(
    card: DocCard,
    onDismiss: () -> Unit,
    onShare: (String) -> Unit
) {
    val filled = remember(card.id, card.updatedAt) {
        card.fields.filter { it.value.isNotBlank() }
    }
    var selected by remember(card.id, card.updatedAt) {
        mutableStateOf(filled.map { it.key }.toSet())
    }

    AnimatedDialog(onDismissRequest = onDismiss) {
        DialogTitle(text = "Поделиться текстом")
        Spacer(Modifier.height(6.dp))
        Text(
            text = "Выберите поля, которые попадут в сообщение",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(12.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .height(260.dp)
                .verticalScroll(rememberScrollState())
        ) {
            filled.forEach { field ->
                val checked = selected.contains(field.key)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable {
                            selected = if (checked) {
                                selected - field.key
                            } else {
                                selected + field.key
                            }
                        }
                        .padding(vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = checked,
                        onCheckedChange = { value ->
                            selected = if (value) selected + field.key else selected - field.key
                        }
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = field.label,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = if (field.mask.isNullOrBlank()) {
                                field.value
                            } else {
                                FieldMasks.display(field.value, field.mask)
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            if (filled.isEmpty()) {
                Text(
                    text = "Нет заполненных полей",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            DialogButton(text = "Отмена", onClick = onDismiss)
            DialogButton(
                text = "Отправить",
                enabled = selected.isNotEmpty(),
                accent = true,
                onClick = {
                    onShare(buildShareText(card, selected))
                    onDismiss()
                }
            )
        }
    }
}

@Composable
fun ConfirmDialog(
    title: String,
    message: String,
    confirmText: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AnimatedDialog(onDismissRequest = onDismiss) {
        DialogTitle(text = title)
        Spacer(Modifier.height(8.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(16.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            DialogButton(text = "Отмена", onClick = onDismiss)
            DialogButton(
                text = confirmText,
                accent = false,
                onClick = {
                    onDismiss()
                    onConfirm()
                }
            )
        }
    }
}

@Composable
fun PasswordDialog(
    title: String,
    message: String,
    confirmText: String,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var password by remember { mutableStateOf("") }

    AnimatedDialog(onDismissRequest = onDismiss) {
        DialogTitle(text = title)
        Spacer(Modifier.height(8.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(14.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(WalletTheme.extra.fieldFill)
                .border(1.dp, WalletTheme.extra.fieldStroke, RoundedCornerShape(14.dp))
                .padding(horizontal = 14.dp, vertical = 14.dp)
        ) {
            BasicTextField(
                value = password,
                onValueChange = { password = it },
                singleLine = true,
                textStyle = MaterialTheme.typography.bodyLarge.copy(
                    color = MaterialTheme.colorScheme.onSurface
                ),
                cursorBrush = SolidColor(Palette.Gold),
                modifier = Modifier.fillMaxWidth(),
                decorationBox = { innerTextField ->
                    if (password.isEmpty()) {
                        Text(
                            text = "Пароль",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                    }
                    innerTextField()
                }
            )
        }

        Spacer(Modifier.height(8.dp))
        Text(
            text = "Минимум 6 символов. Восстановить пароль невозможно",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(Modifier.height(12.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            DialogButton(text = "Отмена", onClick = onDismiss)
            DialogButton(
                text = confirmText,
                enabled = password.length >= 6,
                accent = true,
                onClick = {
                    val value = password
                    onDismiss()
                    onConfirm(value)
                }
            )
        }
    }
}