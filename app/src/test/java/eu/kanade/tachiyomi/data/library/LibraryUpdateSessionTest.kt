package eu.kanade.tachiyomi.data.library

import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequest
import androidx.work.Operation
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.google.common.util.concurrent.Futures
import eu.kanade.tachiyomi.data.library.LibraryUpdateWorker.StartResult
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import tachiyomi.domain.category.model.Category
import tachiyomi.domain.library.model.LibraryManga
import tachiyomi.domain.manga.model.Manga

class LibraryUpdateSessionTest {
    @Test
    fun `adding a category expands the total and starts its new source while the first source is busy`() = runTest {
        val session = LibraryUpdateSession(initialCategoryId = 1)
        val initial = (1L..100L).map { manga(it, source = 1) }
        val added = (101L..200L).map { manga(it, source = 2) }
        val gate = CompletableDeferred<Unit>()
        val started = mutableListOf<Long>()
        val progress = mutableListOf<Pair<Int, Int>>()
        var updatingIds = emptyList<Long>()
        val update = launch {
            session.run(initial, { added }, { it }, { updating, done, total ->
                updatingIds = updating.map { it.id }
                progress.add(done to total)
            }) { manga ->
                started.add(manga.id)
                gate.await()
            }
        }
        runCurrent()
        started shouldBe listOf(1L)
        progress.last() shouldBe (0 to 100)
        updatingIds shouldBe listOf(1L)

        session.addCategory(2) shouldBe LibraryUpdateSession.Addition.ADDED
        runCurrent()
        progress.last() shouldBe (0 to 200)
        started shouldBe listOf(1L, 101L)
        updatingIds shouldBe listOf(1L, 101L)

        gate.complete(Unit)
        update.join()
        progress.last() shouldBe (200 to 200)
        updatingIds shouldBe emptyList()
    }

    @Test
    fun `shared titles are counted once and titles on the same source run one at a time`() = runTest {
        val session = LibraryUpdateSession(initialCategoryId = 1)
        val shared = manga(1, source = 1)
        val gate = CompletableDeferred<Unit>()
        val started = mutableListOf<Long>()
        var progress = 0 to 0
        val update = launch {
            session.run(
                initialManga = listOf(shared, manga(2, source = 1)),
                loadCategory = { listOf(shared, manga(3, source = 1)) },
                filterManga = { it },
                onProgress = { _, done, total -> progress = done to total },
            ) { manga ->
                started.add(manga.id)
                gate.await()
            }
        }
        runCurrent()
        session.addCategory(1) shouldBe LibraryUpdateSession.Addition.DUPLICATE
        session.addCategory(2) shouldBe LibraryUpdateSession.Addition.ADDED
        session.addCategory(2) shouldBe LibraryUpdateSession.Addition.DUPLICATE
        runCurrent()
        progress shouldBe (0 to 3)
        started shouldBe listOf(1L)

        gate.complete(Unit)
        update.join()
        started shouldBe listOf(1L, 2L, 3L)
        progress shouldBe (3 to 3)
        session.addCategory(3) shouldBe LibraryUpdateSession.Addition.CLOSED
    }

    @Test
    fun `additional sources respect the five source limit`() = runTest {
        val session = LibraryUpdateSession(initialCategoryId = 1)
        val gates = (1L..6L).associateWith { CompletableDeferred<Unit>() }
        val started = mutableListOf<Long>()
        val update = launch {
            session.run(
                initialManga = (1L..5L).map { manga(it, source = it) },
                loadCategory = { listOf(manga(6, source = 6)) },
                filterManga = { it },
                onProgress = { _, _, _ -> },
            ) { manga ->
                started.add(manga.source)
                gates.getValue(manga.source).await()
            }
        }
        runCurrent()
        session.addCategory(2) shouldBe LibraryUpdateSession.Addition.ADDED
        runCurrent()
        started shouldBe (1L..5L).toList()

        gates.getValue(1).complete(Unit)
        runCurrent()
        started shouldBe (1L..6L).toList()
        gates.values.forEach { it.complete(Unit) }
        update.join()
    }

    @Test
    fun `adding a category before the worker starts does not schedule another worker`() = runTest {
        val workInfos = MutableStateFlow(emptyList<WorkInfo>())
        val manager = mockk<WorkManager>()
        every { manager.getWorkInfosByTagFlow("LibraryUpdate") } returns workInfos
        every {
            manager.enqueueUniqueWork("LibraryUpdate-manual", ExistingWorkPolicy.KEEP, any<OneTimeWorkRequest>())
        } answers {
            val request = thirdArg<OneTimeWorkRequest>()
            workInfos.value = listOf(WorkInfo(request.id, WorkInfo.State.ENQUEUED, request.tags))
            mockk<Operation> { every { result } returns Futures.immediateFuture(Operation.SUCCESS) }
        }

        LibraryUpdateWorker.startOrAdd(manager, Category(1, "A", 1, 0)) shouldBe StartResult.STARTED
        LibraryUpdateWorker.startOrAdd(manager, Category(2, "B", 2, 0)) shouldBe StartResult.ADDED
        LibraryUpdateWorker.startOrAdd(manager, Category(2, "B", 2, 0)) shouldBe StartResult.CATEGORY_INCLUDED
        LibraryUpdateWorker.startNow(manager) shouldBe false
        verify(exactly = 1) { manager.enqueueUniqueWork(any(), any(), any<OneTimeWorkRequest>()) }
    }

    private fun manga(id: Long, source: Long): LibraryManga = LibraryManga(
        manga = Manga.create().copy(id = id, source = source),
        categories = emptyList(),
        totalChapters = 0,
        readCount = 0,
        bookmarkCount = 0,
        latestUpload = null,
        chapterFetchedAt = null,
        lastRead = null,
    )
}
