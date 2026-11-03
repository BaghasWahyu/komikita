package eu.kanade.tachiyomi.data.download

import com.hippo.unifile.UniFile
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Test

class ChapterFileIndexTest {
    private fun file(name: String): UniFile = mockk {
        every { this@mockk.name } returns name
        every { exists() } returns true
    }

    @Test
    fun matchesCompleteImagesAndFirstSplitOnly() {
        val full = file("001.webp")
        val split = file("002__001.jpg")
        val files = ChapterFileIndex(arrayOf(full, split, file("003.tmp"), file("004__002.jpg"), file("ComicInfo.xml")))
        files.image("001") shouldBe full
        files.image("002") shouldBe split
        files.image("003") shouldBe null
        files.image("004") shouldBe null
        files.image("000") shouldBe null
    }

    @Test
    fun detectsPartialFilesRenamedOrDeletedOutsideIndex() {
        val partial = file("001.tmp")
        val files = ChapterFileIndex(arrayOf(partial))
        files.temporary("001") shouldBe partial
        every { partial.name } returns "001.jpg"
        files.temporary("001") shouldBe null
        files.add(partial)
        files.image("001") shouldBe partial
        every { partial.exists() } returns false
        files.image("001") shouldBe null
    }

    @Test
    fun distinguishesPagePrefixes() {
        val files = ChapterFileIndex(arrayOf(file("0010.jpg"), file("001__001.jpg.tmp")))
        files.image("001") shouldBe null
        files.image("0010")?.name shouldBe "0010.jpg"
    }
}
