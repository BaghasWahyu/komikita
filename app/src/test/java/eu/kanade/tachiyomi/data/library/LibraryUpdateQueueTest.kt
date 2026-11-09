package eu.kanade.tachiyomi.data.library

import android.content.Context
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequest
import androidx.work.Operation
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.SettableFuture
import eu.kanade.tachiyomi.data.library.LibraryUpdateWorker.StartResult
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkObject
import io.mockk.verify
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import tachiyomi.domain.category.model.Category
import java.util.UUID

class LibraryUpdateQueueTest {

    @Test
    fun `another category is appended while the first category is running`() = runTest {
        val queue = Queue(listOf(work(WorkInfo.State.RUNNING, categoryId = 1)))

        LibraryUpdateWorker.enqueue(queue.manager, category(2)) shouldBe StartResult.QUEUED

        queue.requests.single().let { request ->
            request.tags.contains(categoryTag(2)) shouldBe true
            request.workSpec.input.getLong("category", -1) shouldBe 2L
        }
        verify(exactly = 1) {
            queue.manager.enqueueUniqueWork(MANUAL, ExistingWorkPolicy.APPEND_OR_REPLACE, any<OneTimeWorkRequest>())
        }
        verify(exactly = 0) { queue.manager.cancelUniqueWork(any()) }
    }

    @ParameterizedTest
    @EnumSource(value = WorkInfo.State::class, names = ["RUNNING", "ENQUEUED", "BLOCKED"])
    fun `the same running or pending category cannot be added twice`(state: WorkInfo.State) = runTest {
        val queue = Queue(listOf(work(state, categoryId = 0)))

        LibraryUpdateWorker.enqueue(queue.manager, category(0)) shouldBe StartResult.ALREADY_RUNNING

        queue.requests.size shouldBe 0
    }

    @Test
    fun `rapid requests wait for persistence and enqueue the category only once`() = runTest {
        val queue = Queue()
        val persisted = SettableFuture.create<Operation.State.SUCCESS>()
        queue.nextResult = persisted

        val first = async { LibraryUpdateWorker.enqueue(queue.manager, category(1)) }
        runCurrent()
        val second = async { LibraryUpdateWorker.enqueue(queue.manager, category(1)) }
        runCurrent()

        first.isCompleted shouldBe false
        second.isCompleted shouldBe false
        queue.requests.size shouldBe 1
        persisted.set(Operation.SUCCESS)

        first.await() shouldBe StartResult.STARTED
        second.await() shouldBe StartResult.ALREADY_RUNNING
        queue.requests.size shouldBe 1
    }

    @Test
    fun `multiple categories are appended in the order requested`() = runTest {
        val queue = Queue(listOf(work(WorkInfo.State.RUNNING, categoryId = 1)))

        LibraryUpdateWorker.enqueue(queue.manager, category(2)) shouldBe StartResult.QUEUED
        LibraryUpdateWorker.enqueue(queue.manager, category(3)) shouldBe StartResult.QUEUED

        queue.requests.map { it.workSpec.input.getLong("category", -1) } shouldBe listOf(2L, 3L)
    }

    @Test
    fun `a category can be queued behind a running full library update`() = runTest {
        val queue = Queue(listOf(work(WorkInfo.State.RUNNING)))

        LibraryUpdateWorker.enqueue(queue.manager, category(2)) shouldBe StartResult.QUEUED

        queue.requests.size shouldBe 1
    }

    @Test
    fun `a category can be queued during an automatic update`() = runTest {
        val queue = Queue(listOf(work(WorkInfo.State.RUNNING, automatic = true)))

        LibraryUpdateWorker.enqueue(queue.manager, category(2)) shouldBe StartResult.QUEUED

        queue.requests.size shouldBe 1
    }

    @Test
    fun `a future automatic update does not prevent starting a category`() = runTest {
        val queue = Queue(listOf(work(WorkInfo.State.ENQUEUED, automatic = true)))

        LibraryUpdateWorker.enqueue(queue.manager, category(0)) shouldBe StartResult.STARTED

        queue.requests.size shouldBe 1
    }

    @ParameterizedTest
    @EnumSource(value = WorkInfo.State::class, names = ["SUCCEEDED", "FAILED", "CANCELLED"])
    fun `a finished category can be requested again`(state: WorkInfo.State) = runTest {
        val queue = Queue(listOf(work(state, categoryId = 1)))

        LibraryUpdateWorker.enqueue(queue.manager, category(1)) shouldBe StartResult.STARTED

        queue.requests.size shouldBe 1
    }

    @ParameterizedTest
    @EnumSource(value = WorkInfo.State::class, names = ["RUNNING", "ENQUEUED", "BLOCKED"])
    fun `full library updates remain blocked while the manual queue is active`(state: WorkInfo.State) = runTest {
        val queue = Queue(listOf(work(state, categoryId = 1)))

        LibraryUpdateWorker.startNow(queue.manager) shouldBe false

        queue.requests.size shouldBe 0
    }

    @Test
    fun `full library updates remain blocked during an automatic update`() = runTest {
        val queue = Queue(listOf(work(WorkInfo.State.RUNNING, automatic = true)))

        LibraryUpdateWorker.startNow(queue.manager) shouldBe false
    }

    @Test
    fun `progress includes only pending categories alongside the active update`() = runTest {
        val queue = Queue(
            listOf(
                work(WorkInfo.State.SUCCEEDED, categoryId = 5),
                work(WorkInfo.State.BLOCKED, categoryId = 2),
                work(WorkInfo.State.ENQUEUED, automatic = true),
                work(
                    WorkInfo.State.RUNNING,
                    categoryId = 1,
                    progress = workDataOf("progress_completed" to 3, "progress_total" to 10),
                ),
                work(WorkInfo.State.BLOCKED, categoryId = 3),
                work(WorkInfo.State.CANCELLED, categoryId = 4),
            ),
        )

        LibraryUpdateWorker.observeProgress(queue.manager).first() shouldBe LibraryUpdateProgress(
            completed = 3,
            total = 10,
            isCategoryUpdate = true,
            queuedCategories = 2,
        )
    }

    @Test
    fun `pending categories stay visible between workers`() = runTest {
        val queue = Queue(listOf(work(WorkInfo.State.ENQUEUED, categoryId = 0)))

        LibraryUpdateWorker.observeProgress(queue.manager).first() shouldBe LibraryUpdateProgress(
            completed = 0,
            total = 0,
            isCategoryUpdate = true,
            queuedCategories = 1,
        )
    }

    @Test
    fun `scheduled idle work does not show update progress`() = runTest {
        val queue = Queue(
            listOf(
                work(WorkInfo.State.SUCCEEDED, categoryId = 1),
                work(WorkInfo.State.ENQUEUED, automatic = true),
            ),
        )

        LibraryUpdateWorker.observeProgress(queue.manager).first() shouldBe null
    }

    @Test
    fun `stop cancels the manual queue even between workers`() = runTest {
        val queue = Queue(listOf(work(WorkInfo.State.ENQUEUED, categoryId = 1)))
        val context = mockk<Context>()
        mockkObject(WorkManager.Companion)
        try {
            every { WorkManager.getInstance(context) } returns queue.manager

            LibraryUpdateWorker.stop(context)

            verify(exactly = 1) { queue.manager.cancelUniqueWork(MANUAL) }
        } finally {
            unmockkObject(WorkManager.Companion)
        }
    }

    private class Queue(initial: List<WorkInfo> = emptyList()) {
        val manager = mockk<WorkManager>()
        val requests = mutableListOf<OneTimeWorkRequest>()
        var nextResult: ListenableFuture<Operation.State.SUCCESS> = Futures.immediateFuture(Operation.SUCCESS)
        private val workInfos = MutableStateFlow(initial)

        init {
            every { manager.getWorkInfosByTagFlow(any()) } answers {
                val tag = firstArg<String>()
                workInfos.map { infos -> infos.filter { tag in it.tags } }
            }
            every { manager.enqueueUniqueWork(any(), any(), any<OneTimeWorkRequest>()) } answers {
                val request = thirdArg<OneTimeWorkRequest>()
                requests += request
                workInfos.value += WorkInfo(request.id, WorkInfo.State.ENQUEUED, request.tags)
                operation(nextResult)
            }
            every { manager.cancelUniqueWork(any()) } returns operation(Futures.immediateFuture(Operation.SUCCESS))
        }

        private fun operation(result: ListenableFuture<Operation.State.SUCCESS>): Operation = mockk {
            every { this@mockk.result } returns result
        }
    }

    companion object {
        private const val TAG = "LibraryUpdate"
        private const val MANUAL = "LibraryUpdate-manual"
        private const val AUTO = "LibraryUpdate-auto"

        private fun categoryTag(id: Long) = "LibraryUpdate-category-$id"

        private fun category(id: Long) = Category(id, "Category $id", id, 0)

        private fun work(
            state: WorkInfo.State,
            categoryId: Long? = null,
            automatic: Boolean = false,
            progress: Data = Data.EMPTY,
        ) = WorkInfo(
            id = UUID.randomUUID(),
            state = state,
            tags = buildSet {
                add(TAG)
                add(if (automatic) AUTO else MANUAL)
                if (categoryId != null) add(categoryTag(categoryId))
            },
            progress = progress,
        )
    }
}
