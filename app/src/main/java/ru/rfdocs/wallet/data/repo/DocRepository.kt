package ru.rfdocs.wallet.data.repo

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import ru.rfdocs.wallet.data.model.DocCard
import ru.rfdocs.wallet.security.CryptoManager
import java.io.File
import java.util.UUID

object DocRepository {

    private const val VAULT_DIR = "vault"
    private const val CARDS_FILE = "cards.enc"

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
        isLenient = true
    }

    private val cardsSerializer = ListSerializer(DocCard.serializer())

    private val _cards = MutableStateFlow<List<DocCard>>(emptyList())
    val cards: StateFlow<List<DocCard>> = _cards.asStateFlow()

    @Volatile
    private var appContext: Context? = null

    private val lock = Any()

    fun init(context: Context) {
        synchronized(lock) {
            if (appContext != null) return
            val ctx = context.applicationContext
            appContext = ctx
            _cards.value = readFromDisk(ctx)
        }
    }

    fun refresh() {
        synchronized(lock) {
            val ctx = appContext ?: return
            _cards.value = readFromDisk(ctx)
        }
    }

    private fun requireContext(): Context =
        appContext ?: throw IllegalStateException("DocRepository не инициализирован")

    private fun vaultDir(context: Context): File {
        val dir = File(context.filesDir, VAULT_DIR)
        if (!dir.exists()) dir.mkdirs()
        return dir
    }

    private fun cardsFile(context: Context): File = File(vaultDir(context), CARDS_FILE)

    private fun readFromDisk(context: Context): List<DocCard> {
        val file = cardsFile(context)
        if (!file.exists() || file.length() == 0L) return emptyList()
        return runCatching {
            val decrypted = CryptoManager.decrypt(file.readBytes())
            val parsed = json.decodeFromString(cardsSerializer, String(decrypted, Charsets.UTF_8))
            normalize(parsed)
        }.getOrDefault(emptyList())
    }

    private fun normalize(cards: List<DocCard>): List<DocCard> =
        cards.distinctBy { it.id }.sortedByDescending { it.createdAt }

    private fun writeToDisk(cards: List<DocCard>) {
        val ctx = requireContext()
        val payload = json.encodeToString(cardsSerializer, cards).toByteArray(Charsets.UTF_8)
        val encrypted = CryptoManager.encrypt(payload)
        val dir = vaultDir(ctx)
        val tmp = File(dir, "$CARDS_FILE.tmp")
        tmp.writeBytes(encrypted)
        val target = File(dir, CARDS_FILE)
        if (target.exists()) target.delete()
        if (!tmp.renameTo(target)) {
            target.writeBytes(encrypted)
            tmp.delete()
        }
    }

    fun get(cardId: String): DocCard? = _cards.value.firstOrNull { it.id == cardId }

    fun upsert(card: DocCard) {
        synchronized(lock) {
            val updated = card.withUpdated()
            val current = _cards.value.toMutableList()
            val index = current.indexOfFirst { it.id == updated.id }
            if (index >= 0) current[index] = updated else current.add(0, updated)
            val snapshot = current.toList()
            _cards.value = snapshot
            writeToDisk(snapshot)
        }
    }

    fun delete(cardId: String) {
        synchronized(lock) {
            val card = get(cardId)
            val snapshot = _cards.value.filterNot { it.id == cardId }
            _cards.value = snapshot
            writeToDisk(snapshot)
            card?.photoIds?.forEach { photoId ->
                PhotoStore.delete(requireContext(), photoId)
            }
        }
    }

    fun replaceAll(cards: List<DocCard>) {
        synchronized(lock) {
            val snapshot = normalize(cards)
            _cards.value = snapshot
            writeToDisk(snapshot)
        }
    }

    fun exportCardsJson(): ByteArray {
        synchronized(lock) {
            val snapshot = json.encodeToString(cardsSerializer, _cards.value)
            return snapshot.toByteArray(Charsets.UTF_8)
        }
    }

    fun importCardsJson(bytes: ByteArray): Int {
        val parsed = runCatching {
            json.decodeFromString(cardsSerializer, String(bytes, Charsets.UTF_8))
        }.getOrElse { return -1 }
        if (parsed.isEmpty()) return -1
        replaceAll(parsed)
        refresh()
        return _cards.value.size
    }

    fun wipeAll() {
        synchronized(lock) {
            val ctx = requireContext()
            _cards.value = emptyList()
            val file = cardsFile(ctx)
            if (file.exists()) file.delete()
            val tmp = File(vaultDir(ctx), "$CARDS_FILE.tmp")
            if (tmp.exists()) tmp.delete()
            PhotoStore.deleteAll(ctx)
        }
    }
}

object PhotoStore {

    private const val PHOTO_DIR = "vault/photos"

    private fun dir(context: Context): File {
        val directory = File(context.filesDir, PHOTO_DIR)
        if (!directory.exists()) directory.mkdirs()
        return directory
    }

    fun save(context: Context, bytes: ByteArray): String {
        val id = UUID.randomUUID().toString()
        val encrypted = CryptoManager.encrypt(bytes)
        File(dir(context), "$id.enc").writeBytes(encrypted)
        return id
    }

    fun load(context: Context, photoId: String): ByteArray? {
        val file = File(dir(context), "$photoId.enc")
        if (!file.exists()) return null
        return runCatching { CryptoManager.decrypt(file.readBytes()) }.getOrNull()
    }

    fun loadBitmap(context: Context, photoId: String): Bitmap? {
        val bytes = load(context, photoId) ?: return null
        return runCatching { BitmapFactory.decodeByteArray(bytes, 0, bytes.size) }.getOrNull()
    }

    fun delete(context: Context, photoId: String) {
        val file = File(dir(context), "$photoId.enc")
        if (file.exists()) file.delete()
    }

    fun deleteAll(context: Context) {
        dir(context).listFiles()?.forEach { it.delete() }
    }

    fun exportAll(context: Context): Map<String, ByteArray> {
        val result = LinkedHashMap<String, ByteArray>()
        dir(context).listFiles()?.forEach { file ->
            val id = file.name.removeSuffix(".enc")
            runCatching { CryptoManager.decrypt(file.readBytes()) }
                .onSuccess { bytes -> result[id] = bytes }
        }
        return result
    }

    fun importAll(context: Context, photos: Map<String, ByteArray>) {
        photos.forEach { (id, bytes) ->
            val encrypted = CryptoManager.encrypt(bytes)
            File(dir(context), "$id.enc").writeBytes(encrypted)
        }
    }
}