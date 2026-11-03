package eu.kanade.tachiyomi.ui.reader.model

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import java.io.IOException

class DownloadStreamTest {
    @Test
    fun readsBytesAndReleasesBudgetOnClose() {
        val budget = StreamMemoryBudget(128 * 1024)
        val stream = DownloadStream(budget, 64 * 1024)
        val reader = stream.reader()
        val bytes = ByteArray(32 * 1024) { (it % 127).toByte() }
        stream.reserve(bytes.size.toLong())
        stream.append(bytes, 0, bytes.size)
        stream.finish()
        reader.readBytes().toList() shouldBe bytes.toList()
        reader.close()
        budget.used shouldBe 0
        reader.close()
        budget.used shouldBe 0
    }

    @Test
    fun exceedingLimitDropsStreamWithoutFailingProducer() {
        val budget = StreamMemoryBudget(128 * 1024)
        val stream = DownloadStream(budget, 16 * 1024)
        val reader = stream.reader()
        val bytes = ByteArray(32 * 1024)
        stream.append(bytes, 0, bytes.size)
        stream.append(bytes, 0, bytes.size)
        stream.finish()
        shouldThrow<IOException> { reader.read() }
        budget.used shouldBe 0
    }

    @Test
    fun streamsShareBudgetAndReleaseForNextPage() {
        val budget = StreamMemoryBudget(64 * 1024)
        val a = DownloadStream(budget, 64 * 1024)
        val b = DownloadStream(budget, 64 * 1024)
        val readerA = a.reader()
        val readerB = b.reader()
        a.append(ByteArray(64 * 1024), 0, 64 * 1024)
        b.append(ByteArray(1), 0, 1)
        budget.used shouldBe 64 * 1024
        shouldThrow<IOException> { readerB.read() }
        readerA.close()
        budget.used shouldBe 0
        val c = DownloadStream(budget, 64 * 1024)
        c.append(ByteArray(1), 0, 1)
        budget.used shouldBe 64 * 1024
        c.close()
        budget.used shouldBe 0
    }

    @Test
    fun completedPrefetchWithoutDecoderReleasesBudget() {
        val budget = StreamMemoryBudget(128 * 1024)
        val stream = DownloadStream(budget, 64 * 1024)
        stream.append(ByteArray(1), 0, 1)
        stream.finish()
        budget.used shouldBe 0
        shouldThrow<IOException> { stream.reader().read() }
    }

    @Test
    fun countsTransientOldAndNewArraysDuringGrowth() {
        val budget = StreamMemoryBudget(64 * 1024)
        val stream = DownloadStream(budget, 64 * 1024)
        stream.reserve(32 * 1024L)
        stream.append(ByteArray(32 * 1024), 0, 32 * 1024)
        stream.append(ByteArray(1), 0, 1)
        budget.used shouldBe 0
        shouldThrow<IOException> { stream.reader().read() }
    }
}
