package app.komikita.benchmark

import android.app.Activity
import android.os.Build
import android.os.Bundle
import android.widget.TextView
import app.komikita.core.nativecore.NativeChapterEngine
import java.io.File
import kotlin.system.measureNanoTime

/** Measures the complete List -> DoubleArray -> JNI call, exclusively in benchmark builds. */
class NativeBenchmarkActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val label = TextView(this).apply { text = "Native benchmark running" }
        setContentView(label)
        Thread {
            try {
                val csv = StringBuilder("chapters,kotlin_median_ns,jni_including_conversion_median_ns\n")
                for (size in listOf(128, 512, 1_000, 10_000)) {
                    val numbers = List(size) { it * 2.0 }
                    val expected = kotlinCount(numbers)
                    check(NativeChapterEngine.missingChaptersCount(numbers.toDoubleArray()) == expected) {
                        "Native library unavailable or result differs"
                    }
                    fun native() {
                        sink = NativeChapterEngine.missingChaptersCount(numbers.toDoubleArray())!!
                        check(sink == expected)
                    }
                    fun kotlin() {
                        sink = kotlinCount(numbers)
                    }
                    repeat(100) {
                        native()
                        kotlin()
                    }
                    val old = LongArray(21)
                    val jni = LongArray(21)
                    repeat(21) { index ->
                        fun oldRun() {
                            old[index] = measureNanoTime { repeat(100) { kotlin() } } / 100
                        }
                        fun nativeRun() {
                            jni[index] = measureNanoTime { repeat(100) { native() } } / 100
                        }
                        if (index % 2 == 0) {
                            oldRun()
                            nativeRun()
                        } else {
                            nativeRun()
                            oldRun()
                        }
                    }
                    old.sort()
                    jni.sort()
                    csv.appendLine("$size,${old[10]},${jni[10]}")
                }
                val directory = requireNotNull(getExternalFilesDir(null))
                File(directory, "native-boundary.csv").writeText(csv.toString())
                File(directory, "native-environment.txt").writeText(
                    "API=${Build.VERSION.SDK_INT}\nABI=${Build.SUPPORTED_ABIS.joinToString()}\n" +
                        "device=${Build.MODEL}\nheap=${Runtime.getRuntime().maxMemory()}\n",
                )
                runOnUiThread { label.text = "Native benchmark complete" }
            } catch (e: Throwable) {
                File(requireNotNull(getExternalFilesDir(null)), "native-error.txt").writeText(e.stackTraceToString())
                runOnUiThread { label.text = "Native benchmark failed" }
            }
        }.start()
    }

    // Same primitive-array fallback as domain/chapter/service/MissingChapters.kt.
    private fun kotlinCount(numbers: List<Double>): Int {
        val chapters = IntArray(numbers.size)
        var count = 0
        for (number in numbers) {
            if (number.isFinite() && number >= 0.0 &&
                number <= Int.MAX_VALUE.toDouble()
            ) {
                chapters[count++] = number.toInt()
            }
        }
        java.util.Arrays.sort(chapters, 0, count)
        var previous = 0L
        var missing = 0L
        for (index in 0 until count) {
            val current = chapters[index].toLong()
            if (current > previous + 1) missing += current - previous - 1
            previous = current
        }
        return missing.coerceAtMost(Int.MAX_VALUE.toLong()).toInt()
    }

    companion object {
        @Volatile
        private var sink = 0
    }
}
