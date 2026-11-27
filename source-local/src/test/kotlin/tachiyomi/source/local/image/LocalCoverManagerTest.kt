package tachiyomi.source.local.image

import com.hippo.unifile.UniFile
import eu.kanade.tachiyomi.source.model.SManga
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import tachiyomi.source.local.io.LocalSourceFileSystem
import java.io.IOException
import java.io.InputStream

class LocalCoverManagerTest {
    @Test
    fun closesInputWhenCreatingCoverFileFails() {
        val manga = SManga.create().apply { url = "manga" }
        val failure = IOException("Unable to create cover")
        val directory = mockk<UniFile> {
            every { createFile("cover.jpg") } throws failure
        }
        val fileSystem = mockk<LocalSourceFileSystem> {
            every { getMangaDirectory(manga.url) } returns directory
            every { getFilesInMangaDirectory(manga.url) } returns emptyList()
        }
        val input = mockk<InputStream>(relaxUnitFun = true)

        shouldThrow<IOException> { LocalCoverManager(mockk(), fileSystem).update(manga, input) } shouldBe failure

        verify(exactly = 1) { input.close() }
    }
}
