package tachiyomi.domain.library.model

/**
 * Keep reading the most recently opened comic until it is caught up, then work through unread
 * comics by their latest chapter upload. Fetch time is a fallback for sources without upload dates.
 * Callers supply only the comics visible in the current collection.
 */
fun Sequence<LibraryManga>.getContinueReadingManga(): LibraryManga? {
    var lastRead: LibraryManga? = null
    var latestUnread: LibraryManga? = null

    for (manga in this) {
        // Include caught-up comics here so an older reading session cannot override newer updates.
        if (manga.lastRead != null && (lastRead == null || lastReadComparator.compare(manga, lastRead) > 0)) {
            lastRead = manga
        }
        if (manga.unreadCount > 0 &&
            (latestUnread == null || latestUpdateComparator.compare(manga, latestUnread) > 0)
        ) {
            latestUnread = manga
        }
    }

    return lastRead?.takeIf { it.unreadCount > 0 } ?: latestUnread
}

/** Keeps the existing first recommendation, followed by recent reads and recent updates. */
fun Sequence<LibraryManga>.getContinueReadingMangaList(limit: Int = 5): List<LibraryManga> {
    if (limit <= 0) return emptyList()
    val candidates = distinctBy { it.id }.toList()
    val first = candidates.asSequence().getContinueReadingManga() ?: return emptyList()
    return (
        listOf(first) + candidates.filter { it.unreadCount > 0 && it.id != first.id }
            .sortedWith(
                compareByDescending<LibraryManga> { it.lastRead != null }
                    .thenByDescending { it.lastRead }
                    .then(latestUpdateComparator.reversed()),
            )
        )
        .take(limit)
}

private val lastReadComparator = compareBy<LibraryManga> { it.lastRead }
    .thenBy { it.id }

private val latestUpdateComparator = compareBy<LibraryManga> { it.latestUpload ?: it.chapterFetchedAt }
    .thenBy { it.chapterFetchedAt }
    .thenBy { it.lastRead }
    .thenBy { it.id }
