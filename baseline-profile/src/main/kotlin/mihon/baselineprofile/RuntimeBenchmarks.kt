package mihon.baselineprofile

import android.content.ComponentName
import android.content.Intent
import androidx.benchmark.macro.CompilationMode
import androidx.benchmark.macro.FrameTimingMetric
import androidx.benchmark.macro.StartupMode
import androidx.benchmark.macro.StartupTimingMetric
import androidx.benchmark.macro.junit4.MacrobenchmarkRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.uiautomator.By
import androidx.test.uiautomator.Until
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RuntimeBenchmarks {
    @get:Rule
    val rule = MacrobenchmarkRule()

    @Test
    fun listScroll() = scroll("list")

    @Test
    fun gridScroll() = scroll("grid")

    @Test
    fun fixtureStartup() {
        rule.measureRepeated(
            packageName = TARGET_PACKAGE_NAME,
            metrics = listOf(StartupTimingMetric()),
            compilationMode = CompilationMode.Full(),
            startupMode = StartupMode.COLD,
            iterations = 10,
            setupBlock = { pressHome() },
            measureBlock = { startActivityAndWait(fixtureIntent("list")) },
        )
    }

    private fun scroll(mode: String) {
        rule.measureRepeated(
            packageName = TARGET_PACKAGE_NAME,
            metrics = listOf(FrameTimingMetric()),
            compilationMode = CompilationMode.Full(),
            iterations = 10,
            setupBlock = {
                startActivityAndWait(fixtureIntent(mode))
                check(device.wait(Until.hasObject(By.text("Comic 0")), 10_000))
            },
            measureBlock = {
                repeat(6) {
                    device.swipe(
                        device.displayWidth / 2,
                        device.displayHeight * 4 / 5,
                        device.displayWidth / 2,
                        device.displayHeight / 5,
                        30,
                    )
                }
                device.waitForIdle()
            },
        )
    }
}

internal fun fixtureIntent(mode: String) = Intent().apply {
    component = ComponentName(TARGET_PACKAGE_NAME, "app.komikita.benchmark.PerformanceActivity")
    putExtra("mode", mode)
    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
}
