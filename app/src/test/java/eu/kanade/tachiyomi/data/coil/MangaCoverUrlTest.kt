package eu.kanade.tachiyomi.data.coil

import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

class MangaCoverUrlTest {
    @TempDir
    lateinit var root: File

    @Test
    fun resolvesComixRootRelativeCoverAgainstSource() = runTest {
        val cover = "/images/covers/12127/f0816afb6e53abbfb56defb6c4a3c5c2ad4ec0c2@280.jpg"

        resolveCoverUrl(cover) { "https://comix.to" } shouldBe "https://comix.to$cover"
    }

    @Test
    fun resolvesRelativeCoversAndProtocolRelativeCdnUrls() = runTest {
        resolveCoverUrl("images/cover.jpg") { "https://example.org/catalog/" } shouldBe
            "https://example.org/catalog/images/cover.jpg"
        resolveCoverUrl("//cdn.example.org/cover.jpg") { "https://example.org" } shouldBe
            "https://cdn.example.org/cover.jpg"
    }

    @Test
    fun leavesAbsoluteAndExplicitLocalUrlsWithoutLoadingSource() = runTest {
        listOf(
            "https://static.comix.to/cover.jpg",
            "http://example.org/cover.jpg?size=280#cover",
            "Custom-https://example.org/cover.jpg",
            "file:///storage/emulated/0/cover.jpg",
            "content://media/external/images/media/42",
            "",
        ).forEach { cover ->
            resolveCoverUrl(cover) { error("Source lookup is unnecessary for $cover") } shouldBe cover
        }
    }

    @Test
    fun preservesExistingLocalCoverWithoutLoadingSource() = runTest {
        val cover = File(root, "cover.jpg").apply { writeText("local cover") }.absolutePath

        resolveCoverUrl(cover) { error("Local covers must not use the network") } shouldBe cover
    }

    @Test
    fun preservesCoverWhenHttpSourceIsUnavailable() = runTest {
        val cover = "/storage/emulated/0/cover.jpg"

        resolveCoverUrl(cover) { null } shouldBe cover
        resolveCoverUrl(cover) { "invalid base URL" } shouldBe cover
    }
}
