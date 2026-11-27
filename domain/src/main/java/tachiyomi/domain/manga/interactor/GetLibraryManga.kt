package tachiyomi.domain.manga.interactor

import dev.zacsweers.metro.Inject
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.retry
import logcat.LogPriority
import tachiyomi.core.common.util.system.logcat
import tachiyomi.domain.library.model.LibraryManga
import tachiyomi.domain.manga.repository.MangaRepository
import kotlin.time.Duration.Companion.seconds

@Inject
class GetLibraryManga(
    private val mangaRepository: MangaRepository,
) {

    suspend fun await(): List<LibraryManga> {
        return mangaRepository.getLibraryManga()
    }

    suspend fun awaitForUpdate(
        includedCategories: List<Long>,
        excludedCategories: List<Long>,
    ): List<LibraryManga> {
        return mangaRepository.getLibraryMangaForUpdate(includedCategories, excludedCategories)
    }

    suspend fun awaitForUpdateByCategory(categoryId: Long): List<LibraryManga> {
        return mangaRepository.getLibraryMangaForUpdateByCategory(categoryId)
    }

    suspend fun awaitForUpdateByIds(mangaIds: List<Long>): List<LibraryManga> {
        return mangaRepository.getLibraryMangaForUpdateByIds(mangaIds)
    }

    fun subscribe(): Flow<List<LibraryManga>> {
        return mangaRepository.getLibraryMangaAsFlow()
            .retry {
                if (it is NullPointerException) {
                    delay(0.5.seconds)
                    true
                } else {
                    false
                }
            }.catch {
                this@GetLibraryManga.logcat(LogPriority.ERROR, it)
            }
    }
}
