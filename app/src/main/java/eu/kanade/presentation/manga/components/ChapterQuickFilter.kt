package eu.kanade.presentation.manga.components

import eu.kanade.presentation.util.formatChapterNumber
import eu.kanade.tachiyomi.ui.manga.ChapterList

enum class ChapterQuickFilter {
    ALL,
    UNREAD,
    DOWNLOADED,
}

internal fun filterChapterItems(
    chapters: List<ChapterList>,
    query: String,
    quickFilter: ChapterQuickFilter,
    isLocalManga: Boolean,
): List<ChapterList> {
    val search = query.trim()
    if (search.isEmpty() && quickFilter == ChapterQuickFilter.ALL) return chapters

    // Missing-chapter separators describe the full list, not gaps created by search or quick filters.
    return chapters.filterIsInstance<ChapterList.Item>().filter { item ->
        val matchesFilter = when (quickFilter) {
            ChapterQuickFilter.ALL -> true
            ChapterQuickFilter.UNREAD -> !item.chapter.read
            ChapterQuickFilter.DOWNLOADED -> item.isDownloaded || isLocalManga
        }
        matchesFilter && (
            search.isEmpty() ||
                item.chapter.name.contains(search, ignoreCase = true) ||
                item.chapter.scanlator.orEmpty().contains(search, ignoreCase = true) ||
                formatChapterNumber(item.chapter.chapterNumber).contains(search)
            )
    }
}
