package tachiyomi.data.updates

import app.cash.sqldelight.Query
import app.cash.sqldelight.async.coroutines.awaitAsList
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import tachiyomi.core.common.util.lang.toLong
import tachiyomi.data.Database
import tachiyomi.data.subscribeToList
import tachiyomi.domain.manga.model.MangaCover
import tachiyomi.domain.library.service.LibraryVisibility
import tachiyomi.domain.updates.model.UpdatesWithRelations
import tachiyomi.domain.updates.repository.UpdatesRepository
import kotlin.time.Instant

@Inject
@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
class UpdatesRepositoryImpl(
    private val database: Database,
    private val visibility: LibraryVisibility,
) : UpdatesRepository {

    override suspend fun awaitWithRead(
        read: Boolean,
        after: Instant,
        limit: Long,
    ): List<UpdatesWithRelations> {
        return getUpdatesByReadStatus(read = read, after = after, limit = limit).awaitAsList()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun subscribeAll(
        after: Instant,
        limit: Long,
        unread: Boolean?,
        started: Boolean?,
        bookmarked: Boolean?,
        hideExcludedScanlators: Boolean,
        includedCategories: List<Long>,
        excludedCategories: List<Long>,
    ): Flow<List<UpdatesWithRelations>> {
        return visibility.changes().flatMapLatest { hiddenCategories ->
            database.updatesViewQueries
            .getRecentUpdatesWithFilters(
                after = after,
                limit = limit,
                read = unread?.let { !it },
                started = started?.toLong(),
                bookmarked = bookmarked,
                hideExcludedScanlators = hideExcludedScanlators.toLong(),
                includedEmpty = includedCategories.isEmpty(),
                excludedEmpty = excludedCategories.isEmpty(),
                includedCategories = includedCategories,
                excludedCategories = excludedCategories,
                hiddenCategories = hiddenCategories,
                mapper = ::mapUpdatesWithRelations,
            )
            .subscribeToList()
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun subscribeWithRead(
        read: Boolean,
        after: Instant,
        limit: Long,
    ): Flow<List<UpdatesWithRelations>> {
        return visibility.changes().flatMapLatest { hiddenCategories ->
            getUpdatesByReadStatus(read, after, limit, hiddenCategories).subscribeToList()
        }
    }

    private fun getUpdatesByReadStatus(
        read: Boolean,
        after: Instant,
        limit: Long,
        hiddenCategories: List<Long> = visibility.hiddenCategoryIds(),
    ): Query<UpdatesWithRelations> {
        return if (read) {
            database.updatesViewQueries.getReadUpdates(
                after = after,
                limit = limit,
                hiddenCategories = hiddenCategories,
                mapper = ::mapUpdatesWithRelations,
            )
        } else {
            database.updatesViewQueries.getUnreadUpdates(
                after = after,
                limit = limit,
                hiddenCategories = hiddenCategories,
                mapper = ::mapUpdatesWithRelations,
            )
        }
    }

    @Suppress("UNUSED_PARAMETER")
    private fun mapUpdatesWithRelations(
        mangaId: Long,
        mangaTitle: String,
        chapterId: Long,
        chapterName: String,
        scanlator: String?,
        chapterUrl: String,
        read: Boolean,
        bookmark: Boolean,
        lastPageRead: Int,
        sourceId: Long,
        favorite: Boolean,
        thumbnailUrl: String?,
        coverLastModified: Instant?,
        dateUpload: Instant?,
        dateFetch: Instant,
        excludedScanlator: String?,
    ): UpdatesWithRelations = UpdatesWithRelations(
        mangaId = mangaId,
        mangaTitle = mangaTitle,
        chapterId = chapterId,
        chapterName = chapterName,
        scanlator = scanlator,
        chapterUrl = chapterUrl,
        read = read,
        bookmark = bookmark,
        lastPageRead = lastPageRead,
        sourceId = sourceId,
        dateFetch = dateFetch,
        coverData = MangaCover(
            mangaId = mangaId,
            sourceId = sourceId,
            isMangaFavorite = favorite,
            url = thumbnailUrl,
            lastModified = coverLastModified,
        ),
    )
}
