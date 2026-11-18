package ru.rfdocs.wallet.data.model

import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class DocCard(
    val id: String = UUID.randomUUID().toString(),
    val typeKey: String = "",
    val title: String = "",
    val fields: List<DocField> = emptyList(),
    val photoIds: List<String> = emptyList(),
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {

    val isEmpty: Boolean
        get() = fields.all { it.value.isBlank() }

    val filledFields: List<DocField>
        get() = fields.filter { it.isFilled }

    fun fieldOf(key: String): DocField? = fields.firstOrNull { it.key == key }

    fun valueOf(key: String): String = fieldOf(key)?.value.orEmpty()

    fun withUpdated(): DocCard = copy(updatedAt = System.currentTimeMillis())

    fun withPhotos(photoIds: List<String>): DocCard = copy(
        photoIds = photoIds,
        updatedAt = System.currentTimeMillis()
    )

    fun searchHaystack(): String {
        val builder = StringBuilder()
        builder.append(title).append(' ')
        builder.append(typeKey).append(' ')
        fields.forEach { field ->
            builder.append(field.label).append(' ')
            builder.append(field.value).append(' ')
        }
        return builder.toString().lowercase()
    }
}