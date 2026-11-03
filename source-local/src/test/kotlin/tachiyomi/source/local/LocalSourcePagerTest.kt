package tachiyomi.source.local

import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

class LocalSourcePagerTest {
    @Test
    fun reusesStableSnapshotUntilPageOneRefreshes() = runTest {
        val pager = LocalSourcePager<Int>(50)
        var scans = 0
        var contents = (0..<125).toList()
        fun load(): List<Int> {
            scans++
            return contents
        }
        pager.page(1, "a", ::load) shouldBe ((0..<50).toList() to true)
        contents = emptyList()
        pager.page(2, "a", ::load) shouldBe ((50..<100).toList() to true)
        pager.page(3, "a", ::load) shouldBe ((100..<125).toList() to false)
        pager.page(Int.MAX_VALUE, "a", ::load) shouldBe (emptyList<Int>() to false)
        scans shouldBe 1
        pager.page(1, "a", ::load) shouldBe (emptyList<Int>() to false)
        scans shouldBe 2
    }

    @Test
    fun invalidatesSnapshotForDifferentQuerySortOrRoot() = runTest {
        val pager = LocalSourcePager<Int>(2)
        pager.page(1, "old", { listOf(1, 2, 3) })
        pager.page(2, "new", { listOf(4, 5, 6) }) shouldBe (listOf(6) to false)
    }
}
