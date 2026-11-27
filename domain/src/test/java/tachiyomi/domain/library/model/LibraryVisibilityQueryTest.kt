package tachiyomi.domain.library.model

import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import tachiyomi.core.common.preference.InMemoryPreferenceStore
import tachiyomi.domain.category.interactor.GetCategories
import tachiyomi.domain.category.model.Category
import tachiyomi.domain.category.repository.CategoryRepository
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

    @Test
    fun visibleCategoriesFollowChangesWithoutFilteringTheManagementList() = runTest {
        val hidden = MutableStateFlow(emptySet<String>())
        val preferences = mockk<LibraryPreferences>()
        every { preferences.hiddenCategories.changes() } returns hidden
        val category = Category(id = 4L, name = "Private", order = 1L, flags = 0L)
        val repository = mockk<CategoryRepository>()
        every { repository.getAllAsFlow() } returns MutableStateFlow(listOf(category))
        val getCategories = GetCategories(repository, preferences)
        val visible = mutableListOf<List<Category>>()
        val all = mutableListOf<List<Category>>()
        backgroundScope.launch { getCategories.subscribeVisible().collect { visible.add(it) } }
        backgroundScope.launch { getCategories.subscribe().collect { all.add(it) } }

        runCurrent()
        visible.last() shouldBe listOf(category)

        hidden.value = setOf("4")
        runCurrent()
        visible.last() shouldBe emptyList()
        all.last() shouldBe listOf(category)

        hidden.value = emptySet()
        runCurrent()
        visible.last() shouldBe listOf(category)
    }

    @Test
    fun sourceVisibilityFollowsTheSettingAndCategoryMembership() = runTest {
        val enabled = MutableStateFlow(false)
        val categories = MutableStateFlow(setOf("0", "4", "invalid"))
        val sources = MutableStateFlow(setOf(12L, 24L))
        val preferences = mockk<LibraryPreferences>()
        every { preferences.hideSourcesInHiddenCategories.changes() } returns enabled
        every { preferences.hiddenCategories.changes() } returns categories
        val repository = mockk<MangaRepository>()
        every { repository.getHiddenLibrarySourceIdsAsFlow(listOf(0L, 4L)) } returns sources
        val visibility = LibraryVisibility(preferences, repository)
        val emissions = mutableListOf<Set<Long>>()
        backgroundScope.launch { visibility.hiddenSourceIdsFlow().collect { emissions.add(it) } }

        runCurrent()
        emissions shouldBe listOf(emptySet())
        verify(exactly = 0) { repository.getHiddenLibrarySourceIdsAsFlow(any()) }

        enabled.value = true
        runCurrent()
        emissions.last() shouldBe setOf(12L, 24L)

        sources.value = setOf(24L)
        runCurrent()
        emissions.last() shouldBe setOf(24L)

        categories.value = emptySet()
        runCurrent()
        emissions.last() shouldBe emptySet()
        verify(exactly = 1) { repository.getHiddenLibrarySourceIdsAsFlow(any()) }
    }
}
