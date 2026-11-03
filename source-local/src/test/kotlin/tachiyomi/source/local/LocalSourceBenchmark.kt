package tachiyomi.source.local

import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable
import org.junit.jupiter.api.io.TempDir
import java.io.File
import kotlin.system.measureNanoTime

@EnabledIfEnvironmentVariable(named = "KOMIKITA_BENCHMARK_OUTPUT", matches = ".+")
class LocalSourceBenchmark {
    @TempDir
    lateinit var directory: File

    @Test
    fun firstPageCoverLookups() = runTest {
        repeat(2_500) { index ->
            File(directory, "Comic %05d".format(index)).apply { mkdirs() }
                .resolve("cover.jpg").writeBytes(byteArrayOf(1))
        }
        fun scan() = directory.listFiles().orEmpty().filter { it.isDirectory }.sortedBy { it.name }
        val pager = LocalSourcePager<File>()
        val before = LongArray(15)
        val after = LongArray(15)
        repeat(3) {
            scan().map { File(it, "cover.jpg").exists() }
            pager.page(1, "test", ::scan).first.map { File(it, "cover.jpg").exists() }
        }
        repeat(15) { index ->
            before[index] = measureNanoTime { check(scan().count { File(it, "cover.jpg").exists() } == 2_500) }
            after[index] = measureNanoTime {
                val page = pager.page(1, "test", ::scan)
                check(page.second && page.first.count { File(it, "cover.jpg").exists() } == 50)
            }
        }
        before.sort()
        after.sort()
        val output = File(System.getenv("KOMIKITA_BENCHMARK_OUTPUT"), "jvm-local.csv")
        output.parentFile.mkdirs()
        output.writeText(
            "folders,before_median_ms,after_median_ms,before_cover_lookups,after_cover_lookups\n" +
                "2500,${before[7] / 1_000_000.0},${after[7] / 1_000_000.0},2500,50\n",
        )
    }
}
