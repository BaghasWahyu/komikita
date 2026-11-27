package mihon.feature.library

import android.content.Context
import android.os.Process
import android.util.AtomicFile
import coil3.disk.DiskCache
import com.google.mediapipe.tasks.components.containers.Embedding
import com.google.mediapipe.tasks.text.textembedder.TextEmbedder
import com.google.mediapipe.tasks.text.textembedder.TextEmbedder.EmbeddingType
import com.google.mediapipe.tasks.text.textembedder.TextEmbedder.TextFormatContext
import com.google.mediapipe.tasks.text.textembedder.TextEmbedderResult
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import eu.kanade.tachiyomi.network.awaitSuccess
import eu.kanade.tachiyomi.ui.library.LibraryItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import okio.ByteString.Companion.encodeUtf8
import okio.HashingSink
import okio.Path.Companion.toOkioPath
import okio.buffer
import okio.sink
import java.io.File
import java.io.IOException
import java.util.Optional

@Inject
@SingleIn(AppScope::class)
class LibrarySemanticSearch(private val context: Context) {
    private val mutex = Mutex()
    private val client by lazy { OkHttpClient() }
    private val embeddings by lazy {
        DiskCache.Builder()
            .directory(File(context.cacheDir, "library-embeddings").toOkioPath())
            .maxSizeBytes(16L * 1024 * 1024)
            .build()
    }

    val isSupported: Boolean
        get() = Process.is64Bit()

    suspend fun rank(query: String, items: List<LibraryItem>): List<LibraryItem> {
        if (items.isEmpty()) return emptyList()

        return withContext(Dispatchers.IO) {
            mutex.withLock {
                TextEmbedder.createFromFile(context, getModel()).use { embedder ->
                    val queryFormat = TextFormatContext.builder().setTaskType(EmbeddingType.RETRIEVAL_QUERY).build()
                    val queryEmbedding = embedder.embed(query.take(1024), queryFormat).firstEmbedding()

                    items.map { item ->
                        ensureActive()
                        val manga = item.libraryManga.manga
                        val document = listOfNotNull(manga.genre?.joinToString(", "), manga.description)
                            .filter(String::isNotBlank)
                            .joinToString("\n")
                            .ifBlank { manga.title }
                            .take(1200)
                        val key = "$MODEL_SHA256\u0000${manga.title}\u0000$document".encodeUtf8().sha256().hex()
                        val embedding = readEmbedding(key, queryEmbedding.floatEmbedding().size) ?: run {
                            val format = TextFormatContext.builder()
                                .setTaskType(EmbeddingType.RETRIEVAL_DOCUMENT)
                                .setTitle(manga.title)
                                .build()
                            embedder.embed(document, format).firstEmbedding().also { writeEmbedding(key, it) }
                        }
                        item to TextEmbedder.cosineSimilarity(queryEmbedding, embedding)
                    }.sortedByDescending { it.second }.map { it.first }
                }
            }
        }
    }

    private fun readEmbedding(key: String, size: Int): Embedding? = try {
        embeddings.openSnapshot(key)?.use { snapshot ->
            val values = embeddings.fileSystem.read(snapshot.data) {
                FloatArray(size) { Float.fromBits(readInt()) }
            }
            Embedding.create(values, byteArrayOf(), 0, Optional.empty())
        }
    } catch (_: IOException) {
        null
    }

    private fun writeEmbedding(key: String, embedding: Embedding) {
        val editor = embeddings.openEditor(key) ?: return
        try {
            embeddings.fileSystem.write(editor.metadata) {}
            embeddings.fileSystem.write(editor.data) {
                embedding.floatEmbedding().forEach { writeInt(it.toRawBits()) }
            }
            editor.commit()
        } catch (_: IOException) {
            editor.abort()
        }
    }

    private suspend fun getModel(): File {
        val file = File(context.noBackupFilesDir, "embedding-gemma-$MODEL_SHA256.task")
        if (file.length() == MODEL_SIZE) return file

        val coroutine = currentCoroutineContext()
        val model = AtomicFile(file)
        val request = Request.Builder().url(MODEL_URL).build()
        client.newCall(request).awaitSuccess().use { response ->
            val source = response.body.source()
            val output = model.startWrite()
            try {
                val hash = HashingSink.sha256(output.sink())
                val sink = hash.buffer()
                var remaining = MODEL_SIZE
                while (remaining > 0) {
                    coroutine.ensureActive()
                    val read = source.read(sink.buffer, minOf(remaining, 64 * 1024L))
                    if (read == -1L) throw IOException("Incomplete semantic search model")
                    sink.emitCompleteSegments()
                    remaining -= read
                }
                sink.flush()
                if (!source.exhausted() || hash.hash.hex() != MODEL_SHA256) {
                    throw IOException("Invalid semantic search model")
                }
                coroutine.ensureActive()
                model.finishWrite(output)
            } catch (e: Throwable) {
                model.failWrite(output)
                throw e
            }
        }
        return file
    }

    private fun TextEmbedderResult.firstEmbedding() = embeddingResult().embeddings().first()

    private companion object {
        const val MODEL_URL = "https://huggingface.co/litert-community/MediaPipe-TextEmbedder-EmbeddingGemma-300M" +
            "/resolve/93c96b51d328c85e4cbbfe3039d3cd9b98d8a55d/embedding_gemma.task"
        const val MODEL_SIZE = 183_816_181L
        const val MODEL_SHA256 = "913b7a1edc7c7c3d1da3979ec1d0648ed9e0a370f181bb59ab177ca4b97707ad"
    }
}
