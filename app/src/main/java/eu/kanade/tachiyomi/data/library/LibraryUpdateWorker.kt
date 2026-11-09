package eu.kanade.tachiyomi.data.library

import android.content.Context
import android.content.pm.ServiceInfo
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Build
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.ForegroundInfo
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import dev.zacsweers.metro.Inject
import eu.kanade.tachiyomi.data.download.DownloadManager
import eu.kanade.tachiyomi.data.notification.Notifications
import eu.kanade.tachiyomi.source.model.SManga
import eu.kanade.tachiyomi.source.model.UpdateStrategy
import eu.kanade.tachiyomi.util.storage.getUriCompat
import eu.kanade.tachiyomi.util.system.createFileInCacheDir
import eu.kanade.tachiyomi.util.system.isConnectedToWifi
import eu.kanade.tachiyomi.util.system.isRunning
import eu.kanade.tachiyomi.util.system.setForegroundSafely
import eu.kanade.tachiyomi.util.system.workManager
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.guava.await
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.withContext
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import logcat.LogPriority
import mihon.app.di.AppGraph
import mihon.app.di.appGraph
import mihon.core.metro.metroGraph
import mihon.domain.chapter.interactor.FilterChaptersForDownload
import mihon.domain.source.interactor.UpdateMangaFromRemote
import tachiyomi.core.common.i18n.stringResource
import tachiyomi.core.common.preference.getAndSet
import tachiyomi.core.common.util.system.logcat
import tachiyomi.domain.category.model.Category
import tachiyomi.domain.chapter.model.Chapter
import tachiyomi.domain.chapter.model.NoChaptersException
import tachiyomi.domain.library.model.LibraryManga
import tachiyomi.domain.library.service.LibraryPreferences
import tachiyomi.domain.library.service.LibraryPreferences.Companion.DEVICE_CHARGING
import tachiyomi.domain.library.service.LibraryPreferences.Companion.DEVICE_NETWORK_NOT_METERED
import tachiyomi.domain.library.service.LibraryPreferences.Companion.DEVICE_ONLY_ON_WIFI
import tachiyomi.domain.library.service.LibraryPreferences.Companion.MANGA_HAS_UNREAD
import tachiyomi.domain.library.service.LibraryPreferences.Companion.MANGA_NON_COMPLETED
import tachiyomi.domain.library.service.LibraryPreferences.Companion.MANGA_NON_READ
import tachiyomi.domain.library.service.LibraryPreferences.Companion.MANGA_OUTSIDE_RELEASE_PERIOD
import tachiyomi.domain.library.service.LibraryVisibility
import tachiyomi.domain.manga.interactor.FetchInterval
import tachiyomi.domain.manga.interactor.GetLibraryManga
import tachiyomi.domain.manga.interactor.GetManga
import tachiyomi.domain.manga.model.Manga
import tachiyomi.domain.source.model.SourceNotInstalledException
import tachiyomi.domain.source.service.SourceManager
import tachiyomi.i18n.MR
import tachiyomi.i18n.komikita_update_removed
import tachiyomi.i18n.library_errors_help
import tachiyomi.i18n.loader_not_implemented_error
import tachiyomi.i18n.no_chapters_error
import tachiyomi.i18n.skipped_reason_completed
import tachiyomi.i18n.skipped_reason_not_always_update
import tachiyomi.i18n.skipped_reason_not_caught_up
import tachiyomi.i18n.skipped_reason_not_in_release_period
import tachiyomi.i18n.skipped_reason_not_started
import java.io.File
import java.util.Collections
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.TimeUnit
import kotlin.concurrent.atomics.AtomicBoolean
import kotlin.concurrent.atomics.AtomicInt
import kotlin.concurrent.atomics.ExperimentalAtomicApi
import kotlin.concurrent.atomics.incrementAndFetch
import kotlin.time.Clock
import kotlin.time.Instant

@OptIn(ExperimentalAtomicApi::class)
class LibraryUpdateWorker(private val context: Context, workerParams: WorkerParameters) :
    CoroutineWorker(context, workerParams) {

    private val graph: AppGraph = context.metroGraph()

    @Inject private lateinit var sourceManager: SourceManager

    @Inject private lateinit var libraryPreferences: LibraryPreferences

    @Inject private lateinit var downloadManager: DownloadManager

    @Inject private lateinit var getLibraryManga: GetLibraryManga

    @Inject private lateinit var getManga: GetManga

    @Inject private lateinit var fetchInterval: FetchInterval

    @Inject private lateinit var filterChaptersForDownload: FilterChaptersForDownload

    @Inject private lateinit var updateMangaFromRemote: UpdateMangaFromRemote

    @Inject private lateinit var notifier: LibraryUpdateNotifier

    @Inject private lateinit var reportStore: LibraryUpdateReportStore

    @Inject private lateinit var visibility: LibraryVisibility

    private val reportEntries = Collections.synchronizedList(mutableListOf<LibraryUpdateReport.Entry>())

    private var mangaToUpdate: List<LibraryManga> = mutableListOf()

    override suspend fun doWork(): Result {
        // Scheduled updates have their own WorkManager chain. Never let them overlap the manual queue.
        if (!executionMutex.tryLock()) return Result.retry()
        return try {
            doUpdate()
        } finally {
            executionMutex.unlock()
        }
    }

    private suspend fun doUpdate(): Result {
        graph.inject(this)

        if (tags.contains(WORK_NAME_AUTO)) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) {
                val restrictions = libraryPreferences.autoUpdateDeviceRestrictions.get()
                if ((DEVICE_ONLY_ON_WIFI in restrictions) && !context.isConnectedToWifi()) {
                    return Result.retry()
                }
            }

            // Find a running manual worker. If exists, try again later
            if (context.workManager.isRunning(WORK_NAME_MANUAL)) {
                return Result.retry()
            }
        }

        setForegroundSafely()

        libraryPreferences.lastUpdatedTimestamp.set(Clock.System.now().toEpochMilliseconds())

        return withContext(Dispatchers.IO) {
            val retryReport = inputData.getLong(KEY_RETRY_REPORT, -1L).takeIf { it != -1L }
            val retryIds = if (retryReport != null) {
                reportStore.get()?.takeIf { it.startedAt == retryReport }?.failedMangaIds
                    ?: return@withContext Result.failure()
            } else {
                null
            }
            val startedAt = Clock.System.now().toEpochMilliseconds()
            var outcome = LibraryUpdateReport.Outcome.COMPLETED
            reportStore.save(LibraryUpdateReport(startedAt = startedAt))
            try {
                val categoryId = inputData.getLong(KEY_CATEGORY, -1L)
                publishProgress(0)
                addMangaToQueue(categoryId, retryIds)
                updateChapterList()
                Result.success()
            } catch (e: CancellationException) {
                outcome = LibraryUpdateReport.Outcome.CANCELLED
                throw e
            } catch (e: Exception) {
                outcome = LibraryUpdateReport.Outcome.FAILED
                logcat(LogPriority.ERROR, e)
                Result.failure()
            } finally {
                withContext(NonCancellable) {
                    reportStore.save(
                        LibraryUpdateReport(
                            startedAt = startedAt,
                            finishedAt = Clock.System.now().toEpochMilliseconds(),
                            outcome = outcome,
                            entries = synchronized(reportEntries) {
                                reportEntries.toList()
                            }.sortedBy { it.title.lowercase() },
                        ),
                    )
                    notifier.cancelProgressNotification()
                }
            }
        }
    }

    override suspend fun getForegroundInfo(): ForegroundInfo {
        return ForegroundInfo(
            Notifications.ID_LIBRARY_PROGRESS,
            notifier.progressNotificationBuilder.build(),
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
            } else {
                0
            },
        )
    }

    /**
     * Adds list of manga to be updated.
     *
     * @param categoryId the ID of the category to update, or -1 if no category specified.
     */
    private suspend fun addMangaToQueue(categoryId: Long, retryIds: List<Long>? = null) {
        val libraryManga = getLibraryManga.await()

        val listToUpdate = if (retryIds != null) {
            libraryManga.filter { it.id in retryIds }
        } else if (categoryId != -1L) {
            libraryManga.filter { categoryId in it.categories }
        } else {
            val includedCategories = libraryPreferences.updateCategories.get().map { it.toLong() }
            val excludedCategories = libraryPreferences.updateCategoriesExclude.get().map { it.toLong() }

            libraryManga.filter {
                val included = includedCategories.isEmpty() || it.categories.intersect(includedCategories).isNotEmpty()
                val excluded = it.categories.intersect(excludedCategories).isNotEmpty()
                included && !excluded
            }
        }

        val restrictions = libraryPreferences.autoUpdateMangaRestrictions.get()
        val skippedUpdates = mutableListOf<Pair<Manga, String?>>()
        val timeZone = TimeZone.currentSystemDefault()
        val fetchWindowUpperBound = fetchInterval.getWindow(
            Clock.System.now().toLocalDateTime(timeZone).date,
            timeZone,
        ).endInclusive

        mangaToUpdate = listToUpdate
            .filter {
                when {
                    it.manga.updateStrategy == UpdateStrategy.ONLY_FETCH_ONCE && it.totalChapters > 0L -> {
                        skippedUpdates.add(
                            it.manga to context.stringResource(MR.strings.skipped_reason_not_always_update),
                        )
                        false
                    }

                    MANGA_NON_COMPLETED in restrictions && it.manga.status.toInt() == SManga.COMPLETED -> {
                        skippedUpdates.add(it.manga to context.stringResource(MR.strings.skipped_reason_completed))
                        false
                    }

                    MANGA_HAS_UNREAD in restrictions && it.unreadCount != 0L -> {
                        skippedUpdates.add(it.manga to context.stringResource(MR.strings.skipped_reason_not_caught_up))
                        false
                    }

                    MANGA_NON_READ in restrictions && it.totalChapters > 0L && !it.hasStarted -> {
                        skippedUpdates.add(it.manga to context.stringResource(MR.strings.skipped_reason_not_started))
                        false
                    }

                    MANGA_OUTSIDE_RELEASE_PERIOD in restrictions &&
                        it.manga.nextUpdate.let { next -> next != null && next > fetchWindowUpperBound } -> {
                        skippedUpdates.add(
                            it.manga to context.stringResource(MR.strings.skipped_reason_not_in_release_period),
                        )
                        false
                    }

                    else -> true
                }
            }
            .sortedBy { it.manga.title }

        notifier.showQueueSizeWarningNotificationIfNeeded(mangaToUpdate)

        reportEntries.addAll(
            skippedUpdates.map { (manga, reason) ->
                manga.reportEntry(LibraryUpdateReport.Status.SKIPPED, reason)
            },
        )
    }

    /**
     * Method that updates manga in [mangaToUpdate]. It's called in a background thread, so it's safe
     * to do heavy operations or network calls here.
     * For each manga it calls [updateManga] and updates the notification showing the current
     * progress.
     *
     * @return an observable delivering the progress of each update.
     */
    private suspend fun updateChapterList() {
        val semaphore = Semaphore(5)
        val progressCount = AtomicInt(0)
        val progressMutex = Mutex()
        val currentlyUpdatingManga = CopyOnWriteArrayList<Manga>()
        val newUpdates = Collections.synchronizedList(mutableListOf<Pair<Manga, Array<Chapter>>>())
        val failedUpdates = Collections.synchronizedList(mutableListOf<Pair<Manga, String?>>())
        val hasDownloads = AtomicBoolean(false)
        val timeZone = TimeZone.currentSystemDefault()
        val fetchWindow = fetchInterval.getWindow(Clock.System.now().toLocalDateTime(timeZone).date, timeZone)

        publishProgress(0)

        coroutineScope {
            mangaToUpdate.groupBy { it.manga.source }.values
                .sortedByDescending { it.size }
                .map { mangaInSource ->
                    async {
                        semaphore.withPermit {
                            mangaInSource.forEach { libraryManga ->
                                val manga = libraryManga.manga
                                ensureActive()

                                // Don't continue to update if manga is not in library
                                if (getManga.await(manga.id)?.favorite != true) {
                                    reportEntries.add(
                                        manga.reportEntry(
                                            LibraryUpdateReport.Status.SKIPPED,
                                            context.stringResource(MR.strings.komikita_update_removed),
                                        ),
                                    )
                                    progressMutex.withLock {
                                        publishProgress(progressCount.incrementAndFetch())
                                    }
                                    return@forEach
                                }

                                withUpdateNotification(
                                    currentlyUpdatingManga,
                                    progressCount,
                                    progressMutex,
                                    manga,
                                ) {
                                    try {
                                        val newChapters = updateManga(manga, fetchWindow)
                                            .sortedByDescending { it.sourceOrder }

                                        if (newChapters.isNotEmpty()) {
                                            val chaptersToDownload = filterChaptersForDownload.await(manga, newChapters)

                                            if (chaptersToDownload.isNotEmpty()) {
                                                downloadChapters(manga, chaptersToDownload)
                                                hasDownloads.store(true)
                                            }

                                            // Sources update concurrently, and getAndSet is a separate read and write
                                            synchronized(libraryPreferences.newUpdatesCount) {
                                                libraryPreferences.newUpdatesCount.getAndSet { it + newChapters.size }
                                            }

                                            // Convert to the manga that contains new chapters
                                            newUpdates.add(manga to newChapters.toTypedArray())
                                        }
                                        reportEntries.add(
                                            manga.reportEntry(LibraryUpdateReport.Status.SUCCESS)
                                                .copy(newChapters = newChapters.size),
                                        )
                                    } catch (e: CancellationException) {
                                        throw e
                                    } catch (e: Exception) {
                                        val errorMessage = when (e) {
                                            is NoChaptersException -> context.stringResource(
                                                MR.strings.no_chapters_error,
                                            )
                                            // failedUpdates will already have the source, don't need to copy it into the message
                                            is SourceNotInstalledException -> context.stringResource(
                                                MR.strings.loader_not_implemented_error,
                                            )
                                            else -> e.message
                                        }
                                        failedUpdates.add(manga to errorMessage)
                                        reportEntries.add(
                                            manga.reportEntry(LibraryUpdateReport.Status.FAILED, errorMessage),
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                .awaitAll()
        }

        notifier.cancelProgressNotification()

        val updatesSnapshot = synchronized(newUpdates) { newUpdates.toList() }
        if (updatesSnapshot.isNotEmpty()) {
            notifier.showUpdateNotifications(updatesSnapshot)
            if (hasDownloads.load()) {
                downloadManager.startDownloads()
            }
        }

        val failuresSnapshot = synchronized(failedUpdates) { failedUpdates.toList() }
        if (failuresSnapshot.isEmpty()) return
        val hiddenIds = visibility.hiddenMangaIds()
        val visibleFailures = failuresSnapshot.filterNot { it.first.id in hiddenIds }
        if (visibleFailures.isNotEmpty()) {
            val errorFile = writeErrorFile(visibleFailures)
            notifier.showUpdateErrorNotification(
                visibleFailures.size,
                errorFile.getUriCompat(context),
            )
        }
    }

    private fun Manga.reportEntry(status: LibraryUpdateReport.Status, reason: String? = null) =
        LibraryUpdateReport.Entry(id, source, title, status, reason)

    private suspend fun downloadChapters(manga: Manga, chapters: List<Chapter>) {
        // We don't want to start downloading while the library is updating, because websites
        // may don't like it and they could ban the user.
        downloadManager.downloadChapters(manga, chapters, false)
    }

    /**
     * Updates the chapters for the given manga and adds them to the database.
     *
     * @param manga the manga to update.
     * @return a pair of the inserted and removed chapters.
     */
    private suspend fun updateManga(manga: Manga, fetchWindow: ClosedRange<Instant>): List<Chapter> {
        val source = sourceManager.getOrStub(manga.source)

        val update = updateMangaFromRemote(
            source = source,
            manga = manga,
            fetchDetails = libraryPreferences.autoUpdateMetadata.get(),
            fetchChapters = true,
            fetchWindow = fetchWindow,
        )
            .getOrThrow()

        return if (update.manga.favorite) update.newChapters else emptyList()
    }

    private suspend fun withUpdateNotification(
        updatingManga: CopyOnWriteArrayList<Manga>,
        completed: AtomicInt,
        progressMutex: Mutex,
        manga: Manga,
        block: suspend () -> Unit,
    ) = coroutineScope {
        ensureActive()

        updatingManga.add(manga)
        notifier.showProgressNotification(
            completed.load(),
            mangaToUpdate.size,
        )

        block()

        ensureActive()

        updatingManga.remove(manga)
        progressMutex.withLock {
            val current = completed.incrementAndFetch()
            publishProgress(current)
            notifier.showProgressNotification(current, mangaToUpdate.size)
        }
    }

    private suspend fun publishProgress(completed: Int) {
        setProgress(
            workDataOf(
                KEY_PROGRESS_COMPLETED to completed,
                KEY_PROGRESS_TOTAL to mangaToUpdate.size,
                KEY_PROGRESS_CATEGORY to (inputData.getLong(KEY_CATEGORY, -1L) != -1L),
            ),
        )
    }

    /**
     * Writes basic file of update errors to cache dir.
     */
    private suspend fun writeErrorFile(errors: List<Pair<Manga, String?>>): File {
        try {
            if (errors.isNotEmpty()) {
                val file = context.createFileInCacheDir("mihon_update_errors.txt")
                file.bufferedWriter().use { out ->
                    out.write(context.stringResource(MR.strings.library_errors_help, ERROR_LOG_HELP_URL) + "\n\n")
                    // Error file format:
                    // ! Error
                    //   # Source
                    //     - Manga
                    errors.groupBy({ it.second }, { it.first }).forEach { (error, mangas) ->
                        out.write("\n! ${error}\n")
                        mangas.groupBy { it.source }.forEach { (srcId, mangas) ->
                            val source = sourceManager.getOrStub(srcId)
                            out.write("  # $source\n")
                            mangas.forEach { manga ->
                                out.write("    - ${manga.title}\n")
                            }
                        }
                    }
                }
                return file
            }
        } catch (_: Exception) {}
        return File("")
    }

    companion object {
        private const val TAG = "LibraryUpdate"
        private const val WORK_NAME_AUTO = "LibraryUpdate-auto"
        private const val WORK_NAME_MANUAL = "LibraryUpdate-manual"
        private const val CATEGORY_TAG_PREFIX = "LibraryUpdate-category-"

        private val queueMutex = Mutex()
        private val executionMutex = Mutex()

        private const val ERROR_LOG_HELP_URL = "https://mihon.app/docs/guides/troubleshooting/"

        private const val MANGA_PER_SOURCE_QUEUE_WARNING_THRESHOLD = 60

        /**
         * Key for category to update.
         */
        private const val KEY_CATEGORY = "category"
        private const val KEY_RETRY_REPORT = "retry_report"
        private const val KEY_PROGRESS_COMPLETED = "progress_completed"
        private const val KEY_PROGRESS_TOTAL = "progress_total"
        private const val KEY_PROGRESS_CATEGORY = "progress_category"

        fun observeProgress(workManager: WorkManager): Flow<LibraryUpdateProgress?> =
            workManager.getWorkInfosByTagFlow(TAG)
                .map { workInfos ->
                    val running = workInfos.firstOrNull { it.state == WorkInfo.State.RUNNING }
                    val pending = workInfos.filter {
                        WORK_NAME_MANUAL in it.tags && !it.state.isFinished && it.state != WorkInfo.State.RUNNING
                    }
                    (running ?: pending.firstOrNull())?.let { workInfo ->
                        LibraryUpdateProgress(
                            completed = workInfo.progress.getInt(KEY_PROGRESS_COMPLETED, 0),
                            total = workInfo.progress.getInt(KEY_PROGRESS_TOTAL, 0),
                            isCategoryUpdate = workInfo.tags.any { it.startsWith(CATEGORY_TAG_PREFIX) } ||
                                workInfo.progress.getBoolean(KEY_PROGRESS_CATEGORY, false),
                            queuedCategories = pending.count { work ->
                                work.tags.any { it.startsWith(CATEGORY_TAG_PREFIX) }
                            },
                        )
                    }
                }
                .distinctUntilChanged()

        fun setupTask(
            context: Context,
            prefInterval: Int? = null,
        ) {
            val preferences = context.appGraph.libraryPreferences
            val interval = prefInterval ?: preferences.autoUpdateInterval.get()
            if (interval > 0) {
                val restrictions = preferences.autoUpdateDeviceRestrictions.get()
                val networkType = if (DEVICE_NETWORK_NOT_METERED in restrictions) {
                    NetworkType.UNMETERED
                } else {
                    NetworkType.CONNECTED
                }
                val networkRequest = NetworkRequest.Builder().apply {
                    removeCapability(NetworkCapabilities.NET_CAPABILITY_NOT_VPN)
                    if (DEVICE_ONLY_ON_WIFI in restrictions) {
                        addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
                    }
                    if (DEVICE_NETWORK_NOT_METERED in restrictions) {
                        addCapability(NetworkCapabilities.NET_CAPABILITY_NOT_METERED)
                    }
                }
                    .build()
                val constraints = Constraints.Builder()
                    // 'networkRequest' only applies to Android 9+, otherwise 'networkType' is used
                    .setRequiredNetworkRequest(networkRequest, networkType)
                    .setRequiresCharging(DEVICE_CHARGING in restrictions)
                    .setRequiresBatteryNotLow(true)
                    .build()

                val request = PeriodicWorkRequestBuilder<LibraryUpdateWorker>(
                    interval.toLong(),
                    TimeUnit.HOURS,
                    10,
                    TimeUnit.MINUTES,
                )
                    .addTag(TAG)
                    .addTag(WORK_NAME_AUTO)
                    .setConstraints(constraints)
                    .setBackoffCriteria(BackoffPolicy.LINEAR, 10, TimeUnit.MINUTES)
                    .build()

                context.workManager.enqueueUniquePeriodicWork(
                    WORK_NAME_AUTO,
                    ExistingPeriodicWorkPolicy.UPDATE,
                    request,
                )
            } else {
                context.workManager.cancelUniqueWork(WORK_NAME_AUTO)
            }
        }

        suspend fun startNow(
            workManager: WorkManager,
            category: Category? = null,
            retryReport: Long? = null,
        ): Boolean = enqueue(workManager, category, retryReport) != StartResult.ALREADY_RUNNING

        suspend fun enqueue(
            workManager: WorkManager,
            category: Category? = null,
            retryReport: Long? = null,
        ): StartResult = queueMutex.withLock {
            val activeWork = workManager.getWorkInfosByTagFlow(TAG).first().filter {
                !it.state.isFinished && (WORK_NAME_MANUAL in it.tags || it.state == WorkInfo.State.RUNNING)
            }
            val categoryTag = category?.let { "$CATEGORY_TAG_PREFIX${it.id}" }
            if ((category == null && activeWork.isNotEmpty()) ||
                (categoryTag != null && activeWork.any { categoryTag in it.tags })
            ) {
                return@withLock StartResult.ALREADY_RUNNING
            }

            val inputData = workDataOf(
                KEY_CATEGORY to category?.id,
                KEY_RETRY_REPORT to retryReport,
            )
            val request = OneTimeWorkRequestBuilder<LibraryUpdateWorker>()
                .addTag(TAG)
                .addTag(WORK_NAME_MANUAL)
                .apply { if (categoryTag != null) addTag(categoryTag) }
                .setInputData(inputData)
                .setBackoffCriteria(BackoffPolicy.LINEAR, 10, TimeUnit.SECONDS)
                .build()
            val policy = if (category != null) ExistingWorkPolicy.APPEND_OR_REPLACE else ExistingWorkPolicy.KEEP
            // Wait for persistence before releasing the lock so rapid taps cannot enqueue duplicates.
            withContext(NonCancellable) {
                workManager.enqueueUniqueWork(WORK_NAME_MANUAL, policy, request).result.await()
            }

            if (activeWork.isEmpty()) StartResult.STARTED else StartResult.QUEUED
        }

        suspend fun stop(context: Context) = queueMutex.withLock {
            val workManager = context.workManager
            val runningAutomaticWork = workManager.getWorkInfosByTagFlow(WORK_NAME_AUTO).first()
                .filter { it.state == WorkInfo.State.RUNNING }
            withContext(NonCancellable) {
                // Cancel the whole chain, including categories that have not started yet.
                workManager.cancelUniqueWork(WORK_NAME_MANUAL).result.await()
                runningAutomaticWork.forEach {
                    workManager.cancelWorkById(it.id).result.await()
                }
                if (runningAutomaticWork.isNotEmpty()) {
                    setupTask(context)
                }
            }
        }
    }

    enum class StartResult { STARTED, QUEUED, ALREADY_RUNNING }
}
