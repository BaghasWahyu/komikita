package tachiyomi.domain.history.model

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

data class ReadingActivitySlice(val date: LocalDate, val durationMillis: Long)

/** Attribute time to each local calendar day, including sessions crossing midnight or DST. */
fun HistoryUpdate.activitySlices(zone: ZoneId): List<ReadingActivitySlice> {
    if (sessionReadDuration <= 0) return emptyList()
    val end = Instant.ofEpochMilli(readAt.toEpochMilliseconds())
    var cursor = end.minusMillis(sessionReadDuration)
    return buildList {
        while (cursor < end) {
            val date = cursor.atZone(zone).toLocalDate()
            val midnight = date.plusDays(1).atStartOfDay(zone).toInstant()
            val until = minOf(end, midnight)
            add(ReadingActivitySlice(date, java.time.Duration.between(cursor, until).toMillis()))
            cursor = until
        }
    }
}
