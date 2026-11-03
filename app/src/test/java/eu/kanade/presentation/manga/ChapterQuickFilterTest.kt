package eu.kanade.presentation.manga

import eu.kanade.presentation.manga.components.ChapterQuickFilter
import eu.kanade.presentation.manga.components.filterChapterItems
import eu.kanade.tachiyomi.data.download.model.Download
import eu.kanade.tachiyomi.ui.manga.ChapterList
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import tachiyomi.domain.chapter.model.Chapter

class ChapterQuickFilterTest {
    private val unread = ChapterList.Item(
        chapter = Chapter.create().copy(id = 1, name = "New beginning", chapterNumber = 12.5, scanlator = "Team A"),
        downloadState = Download.State.NOT_DOWNLOADED,
    )
    private val downloaded = ChapterList.Item(
        chapter = Chapter.create().copy(id = 2, name = "Return", chapterNumber = 10.0, read = true),
        downloadState = Download.State.DOWNLOADED,
    )
    private val chapters = listOf(downloaded, ChapterList.MissingCount("gap", 1), unread)

    @Test
    fun preservesExistingOrderAndMissingChapterSeparatorsWhenUnfiltered() {
        filterChapterItems(chapters, "  ", ChapterQuickFilter.ALL, false) shouldBe chapters
    }

    @Test
    fun searchesNamesScanlatorsAndFractionalChapterNumbers() {
        for (query in listOf(" BEGINNING ", "team a", "12.5")) {
            filterChapterItems(chapters, query, ChapterQuickFilter.ALL, false) shouldBe listOf(unread)
        }
        filterChapterItems(chapters, "unmatched", ChapterQuickFilter.ALL, false) shouldBe emptyList()
    }

    @Test
    fun combinesSearchWithQuickFiltersWithoutFalseMissingChapterGaps() {
        filterChapterItems(chapters, "", ChapterQuickFilter.UNREAD, false) shouldBe listOf(unread)
        filterChapterItems(chapters, "return", ChapterQuickFilter.DOWNLOADED, false) shouldBe listOf(downloaded)
        filterChapterItems(chapters, "beginning", ChapterQuickFilter.DOWNLOADED, false) shouldBe emptyList()
    }

    @Test
    fun treatsLocalChaptersAsDownloadedAndPreservesInputOrder() {
        filterChapterItems(chapters, "", ChapterQuickFilter.DOWNLOADED, true) shouldBe listOf(downloaded, unread)
    }
}
