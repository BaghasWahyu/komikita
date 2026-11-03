package app.komikita.benchmark

import android.app.Activity
import android.os.Bundle
import android.widget.TextView
import com.hippo.unifile.UniFile
import eu.kanade.tachiyomi.data.download.ChapterFileIndex
import java.io.File
import kotlin.system.measureNanoTime

/** Actual UniFile/local-storage resume lookups, using the original lookup and production index. */
class DownloadIndexBenchmarkActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val label = TextView(this).apply { text = "Download index benchmark running" }
        setContentView(label)
        Thread {
            try {
                val csv = StringBuilder("pages,before_median_ms,after_median_ms,before_scans,after_scans\n")
                for (size in listOf(50, 200, 1_000)) {
                    val directory = File(cacheDir, "download-index-$size").apply { mkdirs() }
                    val prefixes = List(size) { "%04d".format(it + 1) }
                    prefixes.forEach { File(directory, "$it.jpg").writeBytes(byteArrayOf(1)) }
                    val files = requireNotNull(UniFile.fromFile(directory))
                    fun original() {
                        for (prefix in prefixes) {
                            check(
                                files.listFiles().orEmpty().firstOrNull {
                                    val name = it.name.orEmpty()
                                    !name.endsWith(".tmp") &&
                                        (name.startsWith("$prefix.") || name.startsWith("${prefix}__001."))
                                } != null,
                            )
                        }
                    }
                    fun indexed() {
                        val index = ChapterFileIndex(files.listFiles().orEmpty())
                        prefixes.forEach { check(index.image(it) != null) }
                    }
                    original()
                    indexed()
                    val before = LongArray(7)
                    val after = LongArray(7)
                    repeat(7) { index ->
                        if (index % 2 == 0) {
                            before[index] = measureNanoTime(::original)
                            after[index] = measureNanoTime(::indexed)
                        } else {
                            after[index] = measureNanoTime(::indexed)
                            before[index] = measureNanoTime(::original)
                        }
                    }
                    before.sort()
                    after.sort()
                    csv.appendLine("$size,${before[3] / 1_000_000.0},${after[3] / 1_000_000.0},$size,1")
                }
                File(requireNotNull(getExternalFilesDir(null)), "download-index.csv").writeText(csv.toString())
                runOnUiThread { label.text = "Download index benchmark complete" }
            } catch (e: Throwable) {
                File(
                    requireNotNull(getExternalFilesDir(null)),
                    "download-index-error.txt",
                ).writeText(e.stackTraceToString())
                runOnUiThread { label.text = "Download index benchmark failed" }
            }
        }.start()
    }
}
