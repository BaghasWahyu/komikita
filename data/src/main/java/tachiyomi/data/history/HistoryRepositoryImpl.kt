package tachiyomi.data.history

import app.cash.sqldelight.async.coroutines.awaitAsList
import app.cash.sqldelight.async.coroutines.awaitAsOne
import app.cash.sqldelight.async.coroutines.awaitAsOneOrNull
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import logcat.LogPriority
import tachiyomi.core.common.util.system.logcat
import tachiyomi.data.Database
import tachiyomi.data.subscribeToList
import tachiyomi.domain.history.model.History
import tachiyomi.domain.history.model.HistoryUpdate
import tachiyomi.domain.history.model.HistoryWithRelations
import tachiyomi.domain.history.model.ReadingActivity
import tachiyomi.domain.history.model.activitySlices
import tachiyomi.domain.history.repository.HistoryRepository
import tachiyomi.domain.library.service.LibraryVisibility
import java.time.LocalDate
import java.time.ZoneId
import kotlin.time.Instant

@Inject
@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
class HistoryRepositoryImpl(
    private val database: Database,
    private val visibility: LibraryVisibility,
) : HistoryRepository {

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun getHistory(query: String): Flow<List<HistoryWithRelations>> {
        return visibility.changes().flatMapLatest { hiddenCategories ->
            database.historyViewQueries
                .history(query, hiddenCategories, HistoryMapper::mapHistoryWithRelations)
                .subscribeToList()
        }
    }

    override suspend fun getLastHistory(): HistoryWithRelations? {
        return database.historyViewQueries
            .getLatestHistory(visibility.hiddenCategoryIds(), HistoryMapper::mapHistoryWithRelations)
            .awaitAsOneOrNull()
    }

    override suspend fun getTotalReadDuration(): Long {
        return database.historyQueries
            .getReadDuration(visibility.hiddenMangaIds())
            .awaitAsOne()
    }

    override suspend fun getHistoryByMangaId(mangaId: Long): List<History> {
        return database.historyQueries
            .getHistoryByMangaId(mangaId, HistoryMapper::mapHistory)
            .awaitAsList()
    }

    override suspend fun getHistoryByMangaIds(mangaIds: List<Long>): Map<Long, List<History>> {
        return database.historyQueries
            .getHistoryByMangaIds(mangaIds) { mangaId, id, chapterId, readAt, readDuration ->
                mangaId to HistoryMapper.mapHistory(id, chapterId, readAt, readDuration)
            }
            .awaitAsList()
            .groupBy({ it.first }, { it.second })
    }

    override suspend fun resetHistory(historyId: Long) {
        try {
            database.historyQueries.resetHistoryById(historyId)
        } catch (e: Exception) {
            logcat(LogPriority.ERROR, throwable = e)
        }
    }

    override suspend fun resetHistoryByMangaId(mangaId: Long) {
        try {
            database.historyQueries.resetHistoryByMangaId(mangaId)
        } catch (e: Exception) {
            logcat(LogPriority.ERROR, throwable = e)
        }
    }

    override suspend fun deleteAllHistory(): Boolean {
        return try {
            database.transaction {
                database.historyQueries.removeAllHistory()
                database.readingActivityQueries.deleteAll()
            }
            true
        } catch (e: Exception) {
            logcat(LogPriority.ERROR, throwable = e)
            false
        }
    }

    override suspend fun deleteResetHistory() {
        database.historyQueries.removeResettedHistory()
    }

    override suspend fun upsertHistory(historyUpdate: HistoryUpdate) {
        try {
            database.transaction {
                database.historyQueries.upsert(
                    chapterId = historyUpdate.chapterId,
                    readAt = historyUpdate.readAt,
                    readDuration = historyUpdate.sessionReadDuration.coerceAtLeast(0),
                )
                historyUpdate.activitySlices(ZoneId.systemDefault()).forEach { slice ->
                    database.readingActivityQueries.upsert(
                        chapterId = historyUpdate.chapterId,
                        readDate = slice.date.toString(),
                        readDuration = slice.durationMillis,
                        completed = false,
                    )
                }
            }
        } catch (e: Exception) {
            logcat(LogPriority.ERROR, throwable = e)
        }
    }

    override fun getReadingActivity(): Flow<List<ReadingActivity>> =
        database.readingActivityQueries.getActivity { chapterId, mangaId, date, duration, completed ->
            ReadingActivity(chapterId, mangaId, LocalDate.parse(date), duration, completed)
        }.subscribeToList()

    override suspend fun recordCompletedChapter(chapterId: Long, readAt: Instant) {
        database.readingActivityQueries.upsert(
            chapterId = chapterId,
            readDate = java.time.Instant.ofEpochMilli(readAt.toEpochMilliseconds())
                .atZone(ZoneId.systemDefault()).toLocalDate().toString(),
            readDuration = 0,
            completed = true,
        )
    }
}
