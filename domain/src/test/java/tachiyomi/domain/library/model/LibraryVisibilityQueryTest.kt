package tachiyomi.domain.library.model

import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import tachiyomi.core.common.preference.InMemoryPreferenceStore
import tachiyomi.domain.library.service.LibraryPreferences
import tachiyomi.domain.library.service.LibraryVisibility
import tachiyomi.domain.manga.repository.MangaRepository

class LibraryVisibilityQueryTest {
    @Test
    fun skipsDatabaseWhenNothingIsHiddenAndUsesDedicatedProjectionOtherwise() = runTest {
        val preferences = LibraryPreferences(InMemoryPreferenceStore())
        val repository = mockk<MangaRepository>()
        val visibility = LibraryVisibility(preferences, repository)
        visibility.hiddenMangaIds() shouldBe emptySet()
        preferences.hiddenCategories.set(setOf("0", "3", "invalid"))
        coEvery { repository.getHiddenLibraryMangaIds(listOf(0L, 3L)) } returns setOf(1L, 5L)
        visibility.hiddenMangaIds() shouldBe setOf(1L, 5L)
        coVerify(exactly = 1) { repository.getHiddenLibraryMangaIds(listOf(0L, 3L)) }
        coVerify(exactly = 0) { repository.getLibraryManga() }
    }

    @Test
    fun hiddenTitlesStayPrivateEvenWhenLegacyVisibilityOptOutWasRestored() = runTest {
        val store = InMemoryPreferenceStore()
        val preferences = LibraryPreferences(store)
        preferences.hiddenCategories.set(setOf("4"))
        store.getBoolean("komikita_hide_categories_everywhere", true).set(false)
        val visibility = LibraryVisibility(preferences, mockk())
        visibility.hiddenCategoryIds() shouldBe listOf(4L)
    }
}
