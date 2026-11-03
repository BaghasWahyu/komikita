package tachiyomi.domain.library.service

import dev.zacsweers.metro.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import tachiyomi.domain.library.model.LibraryManga
import tachiyomi.domain.manga.repository.MangaRepository

/** Shared visibility rules for history, updates, widgets, notifications and update reports. */
@Inject
class LibraryVisibility(
    private val preferences: LibraryPreferences,
    private val mangaRepository: MangaRepository,
) {
    fun hiddenCategoryIds(): List<Long> = categoryIds(
        preferences.hiddenCategories.get(),
        preferences.hideHiddenCategoriesEverywhere.get(),
    )

    fun changes(): Flow<List<Long>> = combine(
        preferences.hiddenCategories.changes(),
        preferences.hideHiddenCategoriesEverywhere.changes(),
        ::categoryIds,
    ).distinctUntilChanged()

    suspend fun hiddenMangaIds(): Set<Long> {
        val categories = hiddenCategoryIds()
        if (categories.isEmpty()) return emptySet()
        return mangaRepository.getHiddenLibraryMangaIds(categories)
    }

    fun hiddenMangaIdsFlow(): Flow<Set<Long>> = changes().flatMapLatest { categories ->
        if (categories.isEmpty()) {
            flowOf(emptySet())
        } else {
            mangaRepository.getHiddenLibraryMangaIdsAsFlow(categories)
        }
    }.distinctUntilChanged()

    private fun categoryIds(hidden: Set<String>, everywhere: Boolean): List<Long> =
        if (everywhere) hidden.mapNotNull(String::toLongOrNull) else emptyList()
}

fun LibraryManga.isHiddenInLibrary(hiddenCategories: Set<String>): Boolean =
    categories.any { it.toString() in hiddenCategories }
