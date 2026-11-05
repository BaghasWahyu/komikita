package eu.kanade.tachiyomi.ui.reader.loader

import eu.kanade.tachiyomi.data.download.DownloadManager
import eu.kanade.tachiyomi.source.model.Page
import eu.kanade.tachiyomi.source.online.HttpSource
import eu.kanade.tachiyomi.ui.reader.model.ReaderChapter
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import mihon.data.cache.ChapterCache
import org.junit.jupiter.api.Test
import tachiyomi.domain.chapter.model.Chapter
import tachiyomi.domain.manga.model.Manga

class HttpPageLoaderSchedulingTest {
    private val source = mockk<HttpSource> {
        every { id } returns 7537715367149829912L
    }
    private val cache = mockk<ChapterCache>(relaxed = true)

    @Test
    fun startsWithTheLastReadPageThroughChapterLoader() = runTest {
        val chapter = ReaderChapter(Chapter.create().copy(lastPageRead = 15))
        val firstUrlRequest = CompletableDeferred<Int>()
        coEvery { cache.getPageList(any()) } returns List(30) { Page(it, url = "/chapter") }
        coEvery { source.getImageUrl(any()) } coAnswers {
            firstUrlRequest.complete(firstArg<Page>().index)
            awaitCancellation()
        }
        val downloadManager = mockk<DownloadManager>()
        coEvery { downloadManager.isChapterDownloadedOnDisk(any(), any(), any(), any(), any()) } returns false
        val loader =
            ChapterLoader(mockk(), downloadManager, mockk(), cache, backgroundScope, { false }, Manga.create(), source)

        try {
            loader.loadChapter(chapter)

            firstUrlRequest.await() shouldBe 15
            chapter.requestedPage shouldBe 15
        } finally {
            chapter.pageLoader?.recycle()
        }
    }

    @Test
    fun serializesComixUrlDiscoveryWhileAllowingDownloadsToOverlap() = runTest {
        val chapter = ReaderChapter(Chapter.create())
        val firstUrlStarted = CompletableDeferred<Unit>()
        val releaseFirstUrl = CompletableDeferred<Unit>()
        val nextUrlStarted = CompletableDeferred<Unit>()
        val firstDownloadStarted = CompletableDeferred<Unit>()
        coEvery { cache.getPageList(any()) } returns List(3) { Page(it, url = "/chapter") }
        coEvery { cache.acquireImage(any()) } returns null
        coEvery { source.getImageUrl(any()) } coAnswers {
            val index = firstArg<Page>().index
            if (index == 0) {
                firstUrlStarted.complete(Unit)
                releaseFirstUrl.await()
            } else {
                nextUrlStarted.complete(Unit)
            }
            "image-$index"
        }
        coEvery { source.getImage(any(), any()) } coAnswers {
            if (firstArg<Page>().index == 0) firstDownloadStarted.complete(Unit)
            awaitCancellation()
        }
        val loader = HttpPageLoader(chapter, source, cache, backgroundScope) { true }

        try {
            val pages = loader.getPages()
            chapter.state = ReaderChapter.State.Loaded(pages)
            firstUrlStarted.await()
            loader.loadPage(pages[0])
            pages[1].statusFlow.first { it == Page.State.LoadPage }

            coVerify(exactly = 1) { source.getImageUrl(any()) }
            releaseFirstUrl.complete(Unit)
            firstDownloadStarted.await()
            nextUrlStarted.await()
            pages[0].status shouldBe Page.State.DownloadImage
        } finally {
            loader.recycle()
        }
    }
}
