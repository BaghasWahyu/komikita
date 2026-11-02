package tachiyomi.domain.library.model

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import tachiyomi.domain.manga.model.Manga

class LibraryVisibilityTest {
    private fun comic(vararg categories: Long) = LibraryManga(
        manga = Manga.create(),
        categories = categories.toList(),
        totalChapters = 1,
        readCount = 0,
        bookmarkCount = 0,
        latestUpload = null,
        chapterFetchedAt = null,
        lastRead = null,
    )

    @Test
    fun hiddenCategoryAlsoHidesComicsInOtherVisibleCategories() {
        comic(1, 2).isVisibleInLibrary(setOf(2)) shouldBe false
        comic(1).isVisibleInLibrary(setOf(2)) shouldBe true
    }

    @Test
    fun showingCategoryRestoresComicsWithoutChangingMembership() {
        val comic = comic(1, 2)
        listOf(comic).filter { it.isVisibleInLibrary(setOf(1)) } shouldBe emptyList()
        listOf(comic).filter { it.isVisibleInLibrary(emptySet()) } shouldBe listOf(comic)
        comic.categories shouldBe listOf(1L, 2L)
    }

    @Test
    fun unrelatedOrDeletedCategoryIdsDoNotHideUncategorizedComics() {
        comic(0).isVisibleInLibrary(setOf(1, 999)) shouldBe true
    }
}
