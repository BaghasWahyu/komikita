package tachiyomi.domain.history.model

import java.time.LocalDate

data class ReadingActivity(
    val chapterId: Long,
    val mangaId: Long,
    val date: LocalDate,
    val durationMillis: Long,
    val completed: Boolean,
)

data class ReadingRecap(
    val year: Int,
    val chapters: Int,
    val titles: Int,
    val durationMillis: Long,
    val activeDays: Int,
    val longestStreak: Int,
    val monthlyDuration: List<Long>,
    val topMangaIds: List<Long>,
)

fun List<ReadingActivity>.currentReadingStreak(today: LocalDate): Int {
    val dates = filter { it.durationMillis > 0 || it.completed }.map { it.date }.toSet()
    var date = if (today in dates) today else today.minusDays(1)
    var count = 0
    while (date in dates) {
        count++
        date = date.minusDays(1)
    }
    return count
}

fun List<ReadingActivity>.readingRecap(year: Int): ReadingRecap {
    val activity = filter { it.date.year == year && (it.durationMillis > 0 || it.completed) }
    val dates = activity.map { it.date }.distinct().sorted()
    var longest = 0
    var current = 0
    var previous: LocalDate? = null
    dates.forEach { date ->
        current = if (previous?.plusDays(1) == date) current + 1 else 1
        longest = maxOf(longest, current)
        previous = date
    }
    return ReadingRecap(
        year = year,
        chapters = activity.filter { it.completed }.distinctBy { it.chapterId }.size,
        titles = activity.distinctBy { it.mangaId }.size,
        durationMillis = activity.sumOf { it.durationMillis },
        activeDays = dates.size,
        longestStreak = longest,
        monthlyDuration = (1..12).map { month ->
            activity.filter { it.date.monthValue == month }.sumOf { it.durationMillis }
        },
        topMangaIds = activity.groupBy { it.mangaId }.entries
            .sortedWith(
                compareByDescending<Map.Entry<Long, List<ReadingActivity>>> {
                    it.value.sumOf { item -> item.durationMillis }
                }.thenBy { it.key },
            )
            .take(3).map { it.key },
    )
}
