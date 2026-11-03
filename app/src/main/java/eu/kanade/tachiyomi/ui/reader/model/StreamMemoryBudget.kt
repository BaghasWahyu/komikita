package eu.kanade.tachiyomi.ui.reader.model

/** Counts live buffers, including both arrays while a stream grows. */
internal class StreamMemoryBudget(val limit: Long) {
    var used: Long = 0
        private set

    @Synchronized
    fun acquire(bytes: Int): Boolean {
        if (bytes.toLong() > limit - used) return false
        used += bytes
        return true
    }

    @Synchronized
    fun release(bytes: Int) {
        used -= bytes
        check(used >= 0)
    }
}
