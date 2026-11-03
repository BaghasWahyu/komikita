package mihon.baselineprofile

import androidx.benchmark.macro.junit4.BaselineProfileRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.uiautomator.By
import androidx.test.uiautomator.BySelector
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@LargeTest
class BaselineProfileGenerator {

    @get:Rule
    val rule = BaselineProfileRule()

    @Test
    fun generate() {
        rule.collect(TARGET_PACKAGE_NAME) {
            pressHome()
            startActivityAndWait()

            device.waitAndClick(By.text("Updates"))
            device.waitForIdle()

            device.waitAndClick(By.text("History"))
            device.waitForIdle()

            device.waitAndClick(By.text("Browse"))
            device.waitForIdle()
            device.waitAndClick(By.text("Extensions"))
            device.waitForIdle()

            device.waitAndClick(By.text("More"))
            device.waitForIdle()

            // The benchmark variant adds deterministic offline runtime fixtures.
            if (TARGET_PACKAGE_NAME.endsWith(".benchmark")) {
                for (mode in listOf("list", "grid")) {
                    startActivityAndWait(fixtureIntent(mode))
                    device.wait(Until.hasObject(By.text("Comic 0")), 10_000)
                    repeat(6) {
                        device.swipe(device.displayWidth / 2, device.displayHeight * 4 / 5,
                            device.displayWidth / 2, device.displayHeight / 5, 30)
                    }
                }
            }
        }
    }
}

private fun UiDevice.waitAndClick(by: BySelector) {
    wait(Until.findObject(by), 60_000).click()
}
