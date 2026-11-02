package eu.kanade.presentation.reader

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class ReaderRefreshRateTest {
    @Test
    fun prefers60HzOnHighRefreshDisplays() {
        chooseReaderRefreshRate(listOf(30f, 60f, 90f, 120f)) shouldBe 60f
    }

    @Test
    fun usesLowestSupportedRateWhen60HzIsUnavailable() {
        chooseReaderRefreshRate(listOf(120f, 90f)) shouldBe 90f
    }

    @Test
    fun handlesFractionalRatesAndInvalidData() {
        chooseReaderRefreshRate(listOf(Float.NaN, -1f, 59.94f, 119.88f)) shouldBe 59.94f
        chooseReaderRefreshRate(emptyList()) shouldBe null
    }
}
