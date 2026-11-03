package eu.kanade.tachiyomi.ui.reader.model

import java.io.Closeable
import java.io.IOException
import java.io.InputStream

/**
 * A download's bytes as they arrive, for one reader. Past its memory budget, or once that reader is
 * closed, it stops keeping them; [append] never throws, so the download itself carries on.
 */
class DownloadStream internal constructor(
    private val budget: StreamMemoryBudget = sharedBudget,
    private val maxBytes: Int = DEFAULT_MAX_BYTES,
) : Closeable {
    // Object exposes wait()/notifyAll(), which are unavailable on Any.
    @Suppress("PLATFORM_CLASS_MAPPED_TO_KOTLIN")
    private val lock = Object()
    private var data = ByteArray(0)
    private var size = 0
    private var done = false
    private var hasReader = false
    private var error: Throwable? = null

    /** Presizes for [bytes], e.g. Content-Length; ignored if implausible. */
    fun reserve(bytes: Long) {
        synchronized(lock) {
            if (error == null && !done && bytes > data.size && bytes <= maxBytes) {
                grow(bytes.toInt())
            }
        }
    }

    fun append(src: ByteArray, offset: Int, length: Int) {
        synchronized(lock) {
            if (error != null || done) return
            val needed = size.toLong() + length
            if (needed > maxBytes) {
                // Too big to decode while downloading; the whole-file decode takes over.
                drop("Download too large to stream")
                return
            }
            if (needed > data.size) {
                val capacity = maxOf(needed, minOf(maxOf(data.size.toLong() * 2, 1 shl 16), maxBytes.toLong()))
                if (!grow(capacity.toInt())) return
            }
            System.arraycopy(src, offset, data, size, length)
            size += length
            lock.notifyAll()
        }
    }

    /** Ends the bytes; readers throw [error] if given. */
    fun finish(error: Throwable? = null) {
        synchronized(lock) {
            done = true
            if (error != null || !hasReader) drop(error?.message ?: "Download complete; use cached file")
            lock.notifyAll()
        }
    }

    /** From the start; [Reader.read] blocks for bytes. Closing it drops what's kept. */
    fun reader(): Reader = synchronized(lock) {
        hasReader = true
        Reader()
    }

    override fun close() {
        synchronized(lock) { drop("Reader released") }
    }

    private fun grow(capacity: Int): Boolean {
        if (!budget.acquire(capacity)) {
            drop("Streaming memory budget exhausted")
            return false
        }
        val replacement = try {
            data.copyOf(capacity)
        } catch (_: OutOfMemoryError) {
            budget.release(capacity)
            drop("Out of memory buffering the download")
            return false
        }
        budget.release(data.size)
        data = replacement
        return true
    }

    // Rather than hold a second copy of the page.
    private fun drop(why: String) {
        if (error == null) error = IOException(why)
        budget.release(data.size)
        data = ByteArray(0)
        size = 0
        lock.notifyAll()
    }

    private companion object {
        const val DEFAULT_MAX_BYTES = 64 shl 20
        val sharedBudget = StreamMemoryBudget(
            (Runtime.getRuntime().maxMemory() / 8).coerceIn(8L shl 20, 128L shl 20),
        )
    }

    inner class Reader : InputStream() {
        private var position = 0

        /** All read; throws if the download failed. */
        val ended: Boolean
            get() = synchronized(lock) {
                checkError()
                done && position >= size
            }

        override fun read(): Int {
            synchronized(lock) {
                while (position >= size && !done && error == null) lock.wait()
                checkError()
                return if (position >= size) -1 else data[position++].toInt() and 0xFF
            }
        }

        override fun read(b: ByteArray, off: Int, len: Int): Int {
            synchronized(lock) {
                if (len == 0) return 0
                while (position >= size && !done && error == null) lock.wait()
                checkError()
                if (position >= size) return -1
                val n = minOf(len, size - position)
                System.arraycopy(data, position, b, off, n)
                position += n
                return n
            }
        }

        override fun available(): Int = synchronized(lock) { if (error != null) 0 else size - position }

        /** Waits up to [timeoutMs] for [min] unread bytes or the end. */
        fun await(min: Int, timeoutMs: Long) {
            synchronized(lock) {
                val until = System.nanoTime() + timeoutMs * 1_000_000
                while (size - position < min && !done && error == null) {
                    val left = (until - System.nanoTime()) / 1_000_000
                    if (left <= 0) return
                    lock.wait(left)
                }
            }
        }

        override fun close() {
            synchronized(lock) { drop("No longer read") }
        }

        private fun checkError() {
            error?.let { throw IOException("Download failed", it) }
        }
    }
}
