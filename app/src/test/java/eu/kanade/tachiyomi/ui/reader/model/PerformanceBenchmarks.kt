package eu.kanade.tachiyomi.ui.reader.model

import app.komikita.core.nativecore.NativeChapterEngine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.withContext
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable
import tachiyomi.domain.chapter.service.missingChaptersCount
import java.io.File
import java.lang.management.ManagementFactory
import java.util.Collections
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.atomic.AtomicInteger
import kotlin.system.measureNanoTime

/** Opt-in component measurements; timing is reported, never asserted. */
@EnabledIfEnvironmentVariable(named = "KOMIKITA_BENCHMARK_OUTPUT", matches = ".+")
class PerformanceBenchmarks {
    @Test
    fun collectionAppendAndStreamBuffers() {
        val output = File(System.getenv("KOMIKITA_BENCHMARK_OUTPUT"), "jvm-components.csv")
        output.parentFile.mkdirs()
        output.writeText(
            "component,size,before_median_ms,after_median_ms,before_allocated_bytes,after_allocated_bytes\n",
        )
        for (size in listOf(1_000, 10_000)) {
            val entries = Array(size) { Any() }
            fun collect(copyOnWrite: Boolean) {
                val list = if (copyOnWrite) {
                    CopyOnWriteArrayList<Any>()
                } else {
                    Collections.synchronizedList(
                        mutableListOf(),
                    )
                }
                entries.forEach(list::add)
                sink = synchronized(list) { list.toTypedArray() }
                check(list.size == size)
            }
            val (before, after) = measurePair({ collect(true) }, { collect(false) })
            output.appendText("report_entries,$size,${before.first},${after.first},${before.second},${after.second}\n")
        }
        val chunk = ByteArray(64 * 1024) { (it % 127).toByte() }
        fun oldStream() {
            val stream = LegacyDownloadStream()
            val reader = stream.reader()
            repeat(16) { stream.append(chunk, 0, chunk.size) }
            stream.finish()
            check(reader.readBytes().size == 1024 * 1024)
            reader.close()
        }
        fun newStream() {
            val budget = StreamMemoryBudget(16L shl 20)
            val stream = DownloadStream(budget)
            val reader = stream.reader()
            repeat(16) { stream.append(chunk, 0, chunk.size) }
            stream.finish()
            check(reader.readBytes().size == 1024 * 1024)
            reader.close()
            check(budget.used == 0L)
        }
        val (before, after) = measurePair(::oldStream, ::newStream)
        output.appendText("stream_1MiB,1048576,${before.first},${after.first},${before.second},${after.second}\n")
        // Host unit tests have no Android JNI library; measure the original unavailable-native route too.
        check(NativeChapterEngine.missingChaptersCount(doubleArrayOf(1.0)) == null)
        val chapters = List(10_000) { it * 2.0 }
        fun originalMissingCount() {
            NativeChapterEngine.missingChaptersCount(chapters.toDoubleArray())?.let {
                sink = it
                return
            }
            val primitive = IntArray(chapters.size)
            var count = 0
            for (number in chapters) {
                if (number.isFinite() && number >= 0.0 &&
                    number <= Int.MAX_VALUE.toDouble()
                ) {
                    primitive[count++] = number.toInt()
                }
            }
            java.util.Arrays.sort(primitive, 0, count)
            var previous = 0L
            var missing = 0L
            for (index in 0 until count) {
                val current = primitive[index].toLong()
                if (current > previous + 1) missing += current - previous - 1
                previous = current
            }
            sink = missing.coerceAtMost(Int.MAX_VALUE.toLong()).toInt()
        }
        val (fallbackBefore, fallbackAfter) = measurePair(::originalMissingCount) {
            sink =
                chapters.missingChaptersCount()
        }
        output.appendText(
            "native_unavailable_10000,10000,${fallbackBefore.first},${fallbackAfter.first}," +
                "${fallbackBefore.second},${fallbackAfter.second}\n",
        )
        val oldData = LegacyDownloadStream::class.java.getDeclaredField("data").apply { isAccessible = true }
        val newData = DownloadStream::class.java.getDeclaredField("data").apply { isAccessible = true }
        val legacy = LegacyDownloadStream()
        val bounded = DownloadStream(StreamMemoryBudget(128L shl 20))
        val readerOld = legacy.reader()
        val readerNew = bounded.reader()
        var oldPeak = 0
        var newPeak = 0
        repeat(100 * 16) {
            legacy.append(chunk, 0, chunk.size)
            bounded.append(chunk, 0, chunk.size)
            oldPeak = maxOf(oldPeak, (oldData.get(legacy) as ByteArray).size)
            newPeak = maxOf(newPeak, (newData.get(bounded) as ByteArray).size)
        }
        File(output.parentFile, "stream-memory.csv").writeText(
            "input_bytes,before_peak_retained_bytes,after_peak_retained_bytes\n104857600,$oldPeak,$newPeak\n",
        )
        readerOld.close()
        readerNew.close()
    }

    @Test
    fun suspendingSourceConcurrency() = runBlocking {
        val output = File(System.getenv("KOMIKITA_BENCHMARK_OUTPUT"), "search-concurrency.csv")
        output.parentFile.mkdirs()
        output.writeText("strategy,sources,max_in_flight,elapsed_ms\n")
        for (limited in listOf(false, true)) {
            val active = AtomicInteger()
            val maximum = AtomicInteger()
            val dispatcher = Dispatchers.IO.limitedParallelism(5)
            val slots = Semaphore(5)
            val elapsed = measureNanoTime {
                coroutineScope {
                    List(20) {
                        async {
                            suspend fun request() {
                                maximum.accumulateAndGet(active.incrementAndGet(), ::maxOf)
                                try {
                                    delay(50)
                                } finally {
                                    active.decrementAndGet()
                                }
                            }
                            if (limited) {
                                slots.withPermit { withContext(Dispatchers.IO) { request() } }
                            } else {
                                withContext(dispatcher) { request() }
                            }
                        }
                    }.awaitAll()
                }
            }
            output.appendText(
                "${if (limited) "semaphore" else "dispatcher"},20,${maximum.get()},${elapsed / 1_000_000.0}\n",
            )
            check(active.get() == 0)
            if (limited) check(maximum.get() <= 5)
        }
    }

    private fun measurePair(before: () -> Unit, after: () -> Unit): Pair<Pair<Double, Long>, Pair<Double, Long>> {
        repeat(10) {
            before()
            after()
        }
        val bean = ManagementFactory.getThreadMXBean() as com.sun.management.ThreadMXBean
        bean.isThreadAllocatedMemoryEnabled = true
        val times = Array(2) { LongArray(15) }
        val allocations = Array(2) { LongArray(15) }
        repeat(15) { index ->
            for (candidate in if (index % 2 == 0) listOf(0, 1) else listOf(1, 0)) {
                val allocated = bean.getThreadAllocatedBytes(Thread.currentThread().threadId())
                times[candidate][index] = measureNanoTime(if (candidate == 0) before else after)
                allocations[candidate][index] =
                    bean.getThreadAllocatedBytes(Thread.currentThread().threadId()) - allocated
            }
        }
        times.forEach { it.sort() }
        allocations.forEach { it.sort() }
        return (times[0][7] / 1_000_000.0 to allocations[0][7]) to (times[1][7] / 1_000_000.0 to allocations[1][7])
    }

    companion object {
        @Volatile
        private var sink: Any? = null
    }
}
