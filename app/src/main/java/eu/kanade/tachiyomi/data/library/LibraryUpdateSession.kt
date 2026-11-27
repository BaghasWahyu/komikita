package eu.kanade.tachiyomi.data.library

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.launch
import tachiyomi.domain.library.model.LibraryManga
import tachiyomi.domain.manga.model.Manga

internal class LibraryUpdateSession(initialCategoryId: Long?) {
    private val lock = Any()
    private val categories = mutableSetOf<Long>().apply { initialCategoryId?.let(::add) }
    private val events = Channel<Event>(Channel.UNLIMITED)
    private var accepting = true

    fun addCategory(categoryId: Long): Addition = synchronized(lock) {
        when {
            !accepting -> Addition.CLOSED
            !categories.add(categoryId) -> Addition.DUPLICATE
            else -> {
                events.trySend(Event.CategoryAdded(categoryId))
                Addition.ADDED
            }
        }
    }

    fun close() = synchronized(lock) { accepting = false }

    suspend fun run(
        initialManga: List<LibraryManga>,
        loadCategory: suspend (Long) -> List<LibraryManga>,
        filterManga: suspend (List<LibraryManga>) -> List<LibraryManga>,
        onProgress: suspend (updatingManga: List<Manga>, completed: Int, total: Int) -> Unit,
        isUnmeteredSource: suspend (Long) -> Boolean = { false },
        update: suspend (Manga) -> Unit,
    ) = coroutineScope {
        val seenManga = mutableSetOf<Long>()
        val pending = mutableMapOf<Long, ArrayDeque<Manga>>()
        val activeManga = mutableMapOf<Long, Manga>()
        val unmeteredSources = mutableMapOf<Long, Boolean>()
        var activeMeteredCount = 0
        var completed = 0
        var total = 0

        suspend fun addManga(candidates: List<LibraryManga>) {
            val manga = filterManga(candidates.filter { seenManga.add(it.id) })
            manga.groupBy { it.manga.source }.values.sortedByDescending { it.size }.forEach { group ->
                val source = group.first().manga.source
                if (source !in unmeteredSources) {
                    unmeteredSources[source] = isUnmeteredSource(source)
                }
                pending.getOrPut(source, ::ArrayDeque).addAll(group.map { it.manga })
            }
            total += manga.size
        }

        fun isUnmetered(source: Long): Boolean = unmeteredSources[source] == true

        fun startSource(source: Long, queue: ArrayDeque<Manga>) {
            val manga = queue.removeFirst()
            activeManga[source] = manga
            if (!isUnmetered(source)) activeMeteredCount++
            if (queue.isEmpty()) pending.remove(source)
            launch {
                try {
                    update(manga)
                    ensureActive()
                    events.send(Event.MangaCompleted(source))
                } catch (e: CancellationException) {
                    this@coroutineScope.cancel(e)
                    throw e
                }
            }
        }

        try {
            addManga(initialManga)
            while (true) {
                pending.entries
                    .filter { (source, _) -> source !in activeManga && isUnmetered(source) }
                    .toList()
                    .forEach { (source, queue) -> startSource(source, queue) }

                while (activeMeteredCount < MAX_ACTIVE_SOURCES) {
                    val next = pending.entries
                        .filter { (source, _) -> source !in activeManga && !isUnmetered(source) }
                        .maxByOrNull { it.value.size }
                        ?: break
                    startSource(next.key, next.value)
                }

                onProgress(activeManga.values.toList(), completed, total)

                val event = if (activeManga.isEmpty()) {
                    // A category accepted just before the last title finishes still belongs to this session.
                    synchronized(lock) {
                        events.tryReceive().getOrNull().also { if (it == null) accepting = false }
                    } ?: break
                } else {
                    events.receive()
                }

                when (event) {
                    is Event.CategoryAdded -> addManga(loadCategory(event.id))
                    is Event.MangaCompleted -> {
                        activeManga.remove(event.source)
                        if (!isUnmetered(event.source)) activeMeteredCount--
                        completed++
                    }
                }
            }
        } finally {
            close()
        }
    }

    enum class Addition { ADDED, DUPLICATE, CLOSED }

    private sealed interface Event {
        data class CategoryAdded(val id: Long) : Event
        data class MangaCompleted(val source: Long) : Event
    }

    private companion object {
        const val MAX_ACTIVE_SOURCES = 5
    }
}
