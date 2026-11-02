package eu.kanade.tachiyomi.data.library

import kotlinx.serialization.Serializable

@Serializable
data class LibraryUpdateReport(
    val startedAt: Long,
    val finishedAt: Long? = null,
    val outcome: Outcome = Outcome.RUNNING,
    val entries: List<Entry> = emptyList(),
) {
    @Serializable
    enum class Outcome { RUNNING, COMPLETED, CANCELLED, FAILED }

    @Serializable
    enum class Status { SUCCESS, FAILED, SKIPPED }

    @Serializable
    data class Entry(
        val mangaId: Long,
        val sourceId: Long,
        val title: String,
        val status: Status,
        val reason: String? = null,
        val newChapters: Int = 0,
    )

    fun withoutHiddenManga(hiddenIds: Set<Long>): LibraryUpdateReport =
        copy(entries = entries.filterNot { it.mangaId in hiddenIds })

    val failedMangaIds: List<Long>
        get() = entries.filter { it.status == Status.FAILED }.map { it.mangaId }.distinct()
}
