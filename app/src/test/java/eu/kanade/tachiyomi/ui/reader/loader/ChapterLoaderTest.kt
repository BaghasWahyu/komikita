package eu.kanade.tachiyomi.ui.reader.loader

import android.content.Context
import com.hippo.unifile.UniFile
import eu.kanade.tachiyomi.data.download.DownloadManager
import eu.kanade.tachiyomi.ui.reader.model.ReaderChapter
import eu.kanade.tachiyomi.ui.reader.model.ReaderPage
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import io.mockk.verify
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import mihon.core.archive.ArchiveReader
import mihon.core.archive.archiveReader
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import tachiyomi.domain.chapter.model.Chapter
import tachiyomi.domain.manga.model.Manga
import tachiyomi.source.local.LocalSource
import tachiyomi.source.local.io.Format
import java.io.IOException
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

class ChapterLoaderTest {
    private val context = mockk<Context>()
    private val archive = mockk<ArchiveReader>(relaxUnitFun = true)
    private val chapter = ReaderChapter(Chapter.create())
    private val file = mockk<UniFile>()
    private val source = mockk<LocalSource> {
        every { getFormat(any()) } returns Format.Archive(file)
    }
    private val downloadManager = mockk<DownloadManager> {
        coEvery { isChapterDownloadedOnDisk(any(), any(), any(), any(), any()) } returns false
    }

    @BeforeEach
    fun mockArchive() {
        mockkStatic(UniFile::archiveReader)
        every { file.archiveReader(context) } returns archive
    }

    @AfterEach
    fun unmockArchive() {
        unmockkStatic(UniFile::archiveReader)
    }

    @Test
    fun closesArchiveWhenReadingPagesFails() = runTest {
        val failure = IOException("Unable to read archive")
        every { archive.useEntries<List<ReaderPage>>(any()) } throws failure

        shouldThrow<IOException> { loader().loadChapter(chapter) } shouldBe failure

        chapter.state shouldBe ReaderChapter.State.Error(failure)
        chapter.pageLoader shouldBe null
        verify(exactly = 1) { archive.close() }
    }

    @Test
    fun closesArchiveWhenLoadingIsCancelledBeforePublishingPages() = runTest {
        val readingStarted = CompletableDeferred<Unit>()
        val finishReading = CountDownLatch(1)
        every { archive.useEntries<List<ReaderPage>>(any()) } answers {
            readingStarted.complete(Unit)
            check(finishReading.await(10, TimeUnit.SECONDS))
            listOf(ReaderPage(0))
        }

        val job = launch { loader().loadChapter(chapter) }
        try {
            readingStarted.await()
            job.cancel()
        } finally {
            finishReading.countDown()
        }
        job.join()

        chapter.state shouldBe ReaderChapter.State.Wait
        chapter.pageLoader shouldBe null
        verify(exactly = 1) { archive.close() }
    }

    private fun TestScope.loader() =
        ChapterLoader(context, downloadManager, mockk(), mockk(), backgroundScope, { false }, Manga.create(), source)
}
