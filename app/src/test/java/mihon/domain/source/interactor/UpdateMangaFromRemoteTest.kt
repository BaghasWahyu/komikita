package mihon.domain.source.interactor

import eu.kanade.domain.chapter.interactor.SyncChaptersWithSource
import eu.kanade.tachiyomi.source.Source
import eu.kanade.tachiyomi.source.model.SChapter
import eu.kanade.tachiyomi.source.model.SManga
import eu.kanade.tachiyomi.source.model.SMangaUpdate
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import tachiyomi.domain.chapter.model.Chapter
import tachiyomi.domain.chapter.repository.ChapterRepository
import tachiyomi.domain.manga.model.Manga
import tachiyomi.domain.manga.model.MangaRemoteUpdate
import tachiyomi.domain.manga.repository.MangaRepository
import java.io.IOException

class UpdateMangaFromRemoteTest {
    private val manga = Manga.create().copy(id = 1, url = "/manga", title = "Manga")
    private var storedManga = manga
    private val source = mockk<Source>()
    private val chapterRepository = mockk<ChapterRepository>()
    private val mangaRepository = mockk<MangaRepository>()
    private val syncChapters = mockk<SyncChaptersWithSource>()
    private val update = UpdateMangaFromRemote(
        sourceManager = mockk(),
        chapterRepository = chapterRepository,
        mangaRepository = mangaRepository,
        syncChaptersWithSource = syncChapters,
        coverCache = mockk(),
        libraryPreferences = mockk(),
        downloadManager = mockk(relaxed = true),
    )

    init {
        coEvery { chapterRepository.getChapterByMangaId(manga.id) } returns emptyList()
        coEvery { mangaRepository.getMangaById(manga.id) } coAnswers { storedManga }
        coEvery { mangaRepository.updateRemote(any()) } coAnswers {
            val change = firstArg<MangaRemoteUpdate>()
            storedManga = storedManga.copy(description = change.description, initialized = change.initialized)
            true
        }
    }

    @Test
    fun savesDetailsBeforeRequestingChaptersAndKeepsThemIfChaptersFail() = runTest {
        val failure = IOException("Chapter request failed")
        coEvery { source.getMangaUpdate(any(), emptyList(), true, false) } coAnswers {
            SMangaUpdate(firstArg<SManga>().apply { description = "Synopsis" }, emptyList())
        }
        coEvery { source.getMangaUpdate(any(), emptyList(), false, true) } coAnswers {
            storedManga.description shouldBe "Synopsis"
            storedManga.initialized shouldBe true
            firstArg<SManga>().description shouldBe "Synopsis"
            throw failure
        }

        val result = update(source, manga, fetchDetails = true, fetchChapters = true, fetchDetailsFirst = true)

        result.exceptionOrNull() shouldBe failure
        storedManga.description shouldBe "Synopsis"
        coVerify(exactly = 0) { syncChapters.await(any(), any(), any(), any(), any()) }
    }

    @Test
    fun fallsBackToCombinedFetchIfEarlyDetailsFail() = runTest {
        val sourceChapter = SChapter.create().apply { url = "/chapter/1" }
        val chapter = Chapter.create().copy(url = sourceChapter.url)
        coEvery { source.getMangaUpdate(any(), emptyList(), true, false) } throws IOException("Details request failed")
        coEvery { source.getMangaUpdate(any(), emptyList(), true, true) } coAnswers {
            SMangaUpdate(firstArg<SManga>().apply { description = "Synopsis" }, listOf(sourceChapter))
        }
        coEvery { syncChapters.await(listOf(sourceChapter), manga, source, false, null) } returns listOf(chapter)

        val result = update(
            source,
            manga,
            fetchDetails = true,
            fetchChapters = true,
            fetchDetailsFirst = true,
        ).getOrThrow()

        result.manga.description shouldBe "Synopsis"
        result.newChapters shouldBe listOf(chapter)
    }
}
