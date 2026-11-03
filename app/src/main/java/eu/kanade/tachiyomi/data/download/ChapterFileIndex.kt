package eu.kanade.tachiyomi.data.download

import com.hippo.unifile.UniFile
import java.util.concurrent.ConcurrentHashMap

/** One directory snapshot per chapter attempt, shared by its parallel page jobs. */
internal class ChapterFileIndex(files: Array<out UniFile>) {
    private val byName = ConcurrentHashMap<String, UniFile>()
    private val downloaded = ConcurrentHashMap<String, UniFile>()

    init {
        files.forEach(::add)
    }

    fun add(file: UniFile) {
        val name = file.name ?: return
        byName[name] = file
        pagePrefix(name)?.let { downloaded.putIfAbsent(it, file) }
    }

    fun image(prefix: String): UniFile? = downloaded[prefix]?.takeIf { it.exists() }

    fun temporary(prefix: String): UniFile? = byName["$prefix.tmp"]
        ?.takeIf { it.name == "$prefix.tmp" && it.exists() }

    private fun pagePrefix(name: String): String? {
        if (name.endsWith(".tmp") || '.' !in name) return null
        val prefix = name.substringBefore('.').removeSuffix("__001")
        return prefix.takeIf { it.isNotEmpty() && it.all(Char::isDigit) }
    }
}
