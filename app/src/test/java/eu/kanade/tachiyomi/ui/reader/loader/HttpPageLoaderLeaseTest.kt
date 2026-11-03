package eu.kanade.tachiyomi.ui.reader.loader

import eu.kanade.tachiyomi.source.model.Page
import eu.kanade.tachiyomi.source.online.HttpSource
import eu.kanade.tachiyomi.ui.reader.model.ReaderChapter
import eu.kanade.tachiyomi.ui.reader.model.ReaderPage
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import mihon.core.common.cache.TtlDiskCache
import mihon.data.cache.ChapterCache
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import tachiyomi.domain.chapter.model.Chapter
import java.io.File

class HttpPageLoaderLeaseTest {
    @TempDir
    lateinit var root: File

    @Test
    fun protectsReadingWindowAndReleasesItWhenChapterIsRecycled() = runTest {
        val chapter = ReaderChapter(Chapter.create())
        val pages = List(30) { index -> ReaderPage(index, imageUrl = "image-$index").also { it.chapter = chapter } }
        chapter.state = ReaderChapter.State.Loaded(pages)
        val leases = List(30) { index ->
            val file = File(root, "$index.jpg").apply { writeText("page-$index") }
            mockk<TtlDiskCache.Lease>(relaxed = true) { every { this@mockk.file } returns file }
        }
        val cache = mockk<ChapterCache>(relaxed = true)
        coEvery { cache.acquireImage(any()) } answers { leases[firstArg<String>().substringAfter('-').toInt()] }
        coEvery { cache.isImageInCache(any()) } returns true
        val loader = HttpPageLoader(chapter, mockk<HttpSource>(), cache, backgroundScope) { false }
        try {
            loader.loadPage(pages[0])
            pages[0].statusFlow.first { it == Page.State.Ready }
            pages[0].stream!!.invoke().use { it.readBytes().decodeToString() } shouldBe "page-0"
            verify(exactly = 0) { leases[0].close() }
            loader.loadPage(pages[20])
            pages[20].statusFlow.first { it == Page.State.Ready }
            verify(exactly = 1) { leases[0].close() }
        } finally {
            loader.recycle()
        }
        verify(exactly = 1) { leases[20].close() }
    }
}
