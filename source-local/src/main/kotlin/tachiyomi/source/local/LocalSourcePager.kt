package tachiyomi.source.local

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/** Reuses a stable directory snapshot between pages. Refreshing page one always rescans storage. */
internal class LocalSourcePager<T>(private val pageSize: Int = 50) {
    private val mutex = Mutex()
    private var key: String? = null
    private var snapshot = emptyList<T>()

    suspend fun page(page: Int, searchKey: String, load: () -> List<T>): Pair<List<T>, Boolean> = mutex.withLock {
        require(page >= 1)
        if (page == 1 || key != searchKey) {
            snapshot = load()
            key = searchKey
        }
        val start = ((page - 1).toLong() * pageSize).coerceAtMost(snapshot.size.toLong()).toInt()
        val end = (start + pageSize).coerceAtMost(snapshot.size)
        snapshot.subList(start, end).toList() to (end < snapshot.size)
    }
}
