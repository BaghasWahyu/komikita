package tachiyomi.domain.library.model

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import tachiyomi.domain.manga.model.Manga
import kotlin.time.Instant

class ContinueReadingTest {

    @Test
    fun continuesLastReadComicBeforeMoreRecentUpdates() {
        val reading = comic(1, lastRead = 100, uploaded = 10)
        val updated = comic(2, lastRead = 90, uploaded = 200)

        sequenceOf(updated, reading).getContinueReadingManga() shouldBe reading
    }

    @Test
    fun caughtUpLastReadSwitchesToLatestUpdateInsteadOfOlderReadingSession() {
        val caughtUp = comic(1, lastRead = 100, read = 10)
        val olderSession = comic(2, lastRead = 90, uploaded = 10)
        val updated = comic(3, lastRead = 80, uploaded = 200)

        sequenceOf(olderSession, caughtUp, updated).getContinueReadingManga() shouldBe updated
    }

    @Test
    fun canRecommendAnUnreadComicWithNoHistory() {
        val caughtUp = comic(1, lastRead = 100, read = 10)
        val updated = comic(2, uploaded = 200)

        sequenceOf(caughtUp, updated).getContinueReadingManga() shouldBe updated
    }

    @Test
    fun choosesLatestUpdateWhenThereIsNoReadingHistory() {
        val older = comic(1, uploaded = 10)
        val newer = comic(2, uploaded = 20)

        sequenceOf(older, newer).getContinueReadingManga() shouldBe newer
    }

    @Test
    fun ignoresFullyReadAndEmptyComicsInUpdateFallback() {
        val unread = comic(1, uploaded = 10)
        val caughtUp = comic(2, uploaded = 200, read = 10)
        val empty = comic(3, uploaded = 300, total = 0)

        sequenceOf(caughtUp, empty, unread).getContinueReadingManga() shouldBe unread
    }

    @Test
    fun staysWithSelectedUpdateUntilCaughtUpThenAdvancesToNextUpdate() {
        val previous = comic(1, lastRead = 100, read = 10)
        val newest = comic(2, uploaded = 200)
        val next = comic(3, uploaded = 150)

        sequenceOf(previous, newest, next).getContinueReadingManga() shouldBe newest

        val inProgress = newest.copy(lastRead = instant(110), readCount = 9)
        sequenceOf(previous, inProgress, next).getContinueReadingManga() shouldBe inProgress

        val caughtUp = inProgress.copy(lastRead = instant(120), readCount = 10)
        sequenceOf(previous, caughtUp, next).getContinueReadingManga() shouldBe next
    }

    @Test
    fun resumesLastReadWhenNewChaptersArrive() {
        val caughtUp = comic(1, lastRead = 100, read = 10)
        val other = comic(2, uploaded = 200)
        val continued = caughtUp.copy(totalChapters = 11)

        sequenceOf(continued, other).getContinueReadingManga() shouldBe continued
    }

    @Test
    fun usesFetchTimeWhenUploadDateIsMissing() {
        val uploaded = comic(1, uploaded = 10)
        val fetched = comic(2, fetched = 20)

        sequenceOf(uploaded, fetched).getContinueReadingManga() shouldBe fetched
    }

    @Test
    fun fetchingOldChaptersDoesNotOverrideTheirUploadDate() {
        val oldImport = comic(1, uploaded = 10, fetched = 100)
        val recentRelease = comic(2, uploaded = 20, fetched = 50)

        sequenceOf(oldImport, recentRelease).getContinueReadingManga() shouldBe recentRelease
    }

    @Test
    fun breaksEqualUploadDatesByFetchTime() {
        val olderFetch = comic(1, uploaded = 10, fetched = 20)
        val newerFetch = comic(2, uploaded = 10, fetched = 30)

        sequenceOf(olderFetch, newerFetch).getContinueReadingManga() shouldBe newerFetch
    }

    @Test
    fun recommendationIsStableAcrossCollectionSortOrders() {
        val first = comic(1, uploaded = 10)
        val second = comic(2, uploaded = 10)

        sequenceOf(first, second).getContinueReadingManga() shouldBe second
        sequenceOf(second, first).getContinueReadingManga() shouldBe second
    }

    @Test
    fun handlesComicsWithoutAnyDates() {
        val unknown = comic(1)
        val dated = comic(2, fetched = 10)

        sequenceOf(unknown).getContinueReadingManga() shouldBe unknown
        sequenceOf(unknown, dated).getContinueReadingManga() shouldBe dated
    }

    @Test
    fun hidesRecommendationWhenNothingIsUnread() {
        emptySequence<LibraryManga>().getContinueReadingManga() shouldBe null
        sequenceOf(comic(1, lastRead = 100, read = 10)).getContinueReadingManga() shouldBe null
        sequenceOf(comic(1, total = 0)).getContinueReadingManga() shouldBe null
    }

    private fun comic(
        id: Long,
        lastRead: Long? = null,
        uploaded: Long? = null,
        fetched: Long? = null,
        total: Long = 10,
        read: Long = 0,
    ) = LibraryManga(
        manga = Manga.create().copy(id = id),
        categories = emptyList(),
        totalChapters = total,
        readCount = read,
        bookmarkCount = 0,
        latestUpload = uploaded?.let(::instant),
        chapterFetchedAt = fetched?.let(::instant),
        lastRead = lastRead?.let(::instant),
    )

    private fun instant(value: Long) = Instant.fromEpochMilliseconds(value)
}
