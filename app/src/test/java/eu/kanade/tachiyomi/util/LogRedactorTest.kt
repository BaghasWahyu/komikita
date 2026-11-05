package eu.kanade.tachiyomi.util

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class LogRedactorTest {
    @Test
    fun removesCredentialsUrlsAndHiddenTitlesButPreservesUsefulStackTraces() {
        val result = LogRedactor.redact(
            "Authorization: Bearer secret\n{\"password\":\"secret\"}\nhttps://example.test/private?token=secret\n" +
                "Loading Secret Comic\njava.io.IOException: failed\n    at reader.load(Reader.kt:42)",
            listOf("Secret Comic"),
        )
        result.contains("secret", ignoreCase = true) shouldBe false
        result.contains("example.test") shouldBe false
        result.contains("reader.load(Reader.kt:42)") shouldBe true
    }
}
