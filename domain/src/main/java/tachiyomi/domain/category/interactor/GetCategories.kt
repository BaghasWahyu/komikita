package tachiyomi.domain.category.interactor

import dev.zacsweers.metro.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import tachiyomi.domain.category.model.Category
import tachiyomi.domain.category.repository.CategoryRepository
import tachiyomi.domain.library.service.LibraryPreferences

@Inject
class GetCategories(
    private val categoryRepository: CategoryRepository,
    private val libraryPreferences: LibraryPreferences,
) {

    fun subscribe(): Flow<List<Category>> {
        return categoryRepository.getAllAsFlow()
    }

    fun subscribeVisible(): Flow<List<Category>> =
        combine(subscribe(), libraryPreferences.hiddenCategories.changes()) { categories, hidden ->
            categories.filterNot { it.id.toString() in hidden }
        }

    fun subscribe(mangaId: Long): Flow<List<Category>> {
        return categoryRepository.getCategoriesByMangaIdAsFlow(mangaId)
    }

    suspend fun await(): List<Category> {
        return categoryRepository.getAll()
    }

    suspend fun await(mangaId: Long): List<Category> {
        return categoryRepository.getCategoriesByMangaId(mangaId)
    }
}
