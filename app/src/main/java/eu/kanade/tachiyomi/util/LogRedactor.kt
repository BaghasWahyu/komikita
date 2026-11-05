package eu.kanade.tachiyomi.util

/** Export only diagnostics; URLs, credentials, and hidden titles have no place in a bug attachment. */
object LogRedactor {
    fun redact(text: String, hiddenTitles: List<String> = emptyList()): String {
        var result = text
            .replace(Regex("(?i)https?://[^\\s<>]+"), "[URL redacted]")
            .replace(
                Regex(
                    "(?im)^.*(?:authorization|cookie|password|access[_ -]?token|refresh[_ -]?token|api[_ -]?key|installation id)[\"']?\\s*[:=].*$",
                ),
                "[Credential redacted]",
            )
            .replace(Regex("(?i)Bearer\\s+[^\\s,;]+"), "Bearer [redacted]")
        hiddenTitles.filter { it.isNotBlank() }.sortedByDescending { it.length }.forEach { title ->
            result = result.replace(title, "[Hidden title]", ignoreCase = true)
        }
        return result
    }
}
