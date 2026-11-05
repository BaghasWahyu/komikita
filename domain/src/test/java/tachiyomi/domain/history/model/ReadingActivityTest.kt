package tachiyomi.domain.history.model

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.ZoneId
import kotlin.time.Instant

class ReadingActivityTest {
    private fun day(date: String, chapter: Long = 1, duration: Long = 60_000, completed: Boolean = true) =
        ReadingActivity(chapter, chapter / 10, LocalDate.parse(date), duration, completed)

    @Test
    fun streakIncludesYesterdayButExpiresAfterMissedDayAndIgnoresEmptyActivity() {
        val activity = listOf(day("2026-11-02"), day("2026-11-03"), day("2026-11-05", duration = 0, completed = false))
        activity.currentReadingStreak(LocalDate.parse("2026-11-04")) shouldBe 2
        activity.currentReadingStreak(LocalDate.parse("2026-11-05")) shouldBe 0
    }

    @Test
    fun recapUsesSelectedYearUniqueChaptersAndConsecutiveCalendarDays() {
        val activity = listOf(
            day("2025-12-31"),
            day("2026-01-01", 10),
            day("2026-01-02", 10),
            day("2026-01-04", 20),
            day("2026-02-01", 30, completed = false),
        )
        val recap = activity.readingRecap(2026)
        recap.chapters shouldBe 2
        recap.titles shouldBe 3
        recap.activeDays shouldBe 4
        recap.longestStreak shouldBe 2
        recap.durationMillis shouldBe 240_000
        recap.monthlyDuration.take(2) shouldBe listOf(180_000L, 60_000L)
        recap.topMangaIds shouldBe listOf(1L, 2L, 3L)
        emptyList<ReadingActivity>().readingRecap(2026).longestStreak shouldBe 0
    }

    @Test
    fun splitsNewYearSessionUsingLocalTimezone() {
        val update = HistoryUpdate(1, Instant.parse("2026-12-31T17:05:00Z"), 600_000)
        update.activitySlices(ZoneId.of("Asia/Jakarta")) shouldBe listOf(
            ReadingActivitySlice(LocalDate.parse("2026-12-31"), 300_000),
            ReadingActivitySlice(LocalDate.parse("2027-01-01"), 300_000),
        )
    }

    @Test
    fun handlesDaylightSavingAndNegativeDurationsWithoutAddingTime() {
        val update = HistoryUpdate(1, Instant.parse("2026-03-09T04:30:00Z"), 7_200_000)
        update.activitySlices(ZoneId.of("America/New_York")).sumOf { it.durationMillis } shouldBe 7_200_000
        update.copy(sessionReadDuration = -1).activitySlices(ZoneId.of("UTC")) shouldBe emptyList()
    }
}
