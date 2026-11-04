package eu.kanade.tachiyomi.util

import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody
import okhttp3.ResponseBody.Companion.toResponseBody
import okio.Buffer
import okio.Source
import okio.Timeout
import okio.buffer
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import java.io.IOException

class JsoupExtensionsTest {

    @Test
    fun `response parsing honors the HTTP charset and URL`() {
        val html = "<a href='cover.png'>Caf\u00e9</a>"
        val body = html.toByteArray(Charsets.ISO_8859_1)
            .toResponseBody("text/html; charset=iso-8859-1".toMediaType())

        val link = response(body).asJsoup().selectFirst("a")!!

        assertEquals("Caf\u00e9", link.text())
        assertEquals("https://example.org/manga/cover.png", link.absUrl("href"))
    }

    @Test
    fun `response parsing gives the byte order mark precedence`() {
        val html = "<p>Caf\u00e9</p>"
        val bytes = byteArrayOf(0xff.toByte(), 0xfe.toByte()) + html.toByteArray(Charsets.UTF_16LE)
        val body = bytes.toResponseBody("text/html; charset=utf-8".toMediaType())

        assertEquals("Caf\u00e9", response(body).asJsoup().selectText("p"))
    }

    @Test
    fun `provided HTML leaves the response body available`() {
        val response = response("original".toResponseBody())

        assertEquals("override", response.asJsoup("<p>override</p>").selectText("p"))
        assertEquals("original", response.body.string())
    }

    @Test
    fun `response parsing propagates the original IO failure`() {
        val failure = IOException("Connection closed")
        val body = object : ResponseBody() {
            private val source = object : Source {
                override fun read(sink: Buffer, byteCount: Long): Long = throw failure
                override fun close() = Unit
                override fun timeout(): Timeout = Timeout.NONE
            }.buffer()

            override fun contentType() = null
            override fun contentLength() = -1L
            override fun source() = source
        }

        assertSame(failure, assertThrows(IOException::class.java) { response(body).asJsoup() })
    }

    private fun response(body: ResponseBody): Response = Response.Builder()
        .request(Request.Builder().url("https://example.org/manga/chapter").build())
        .protocol(Protocol.HTTP_1_1)
        .code(200)
        .message("OK")
        .body(body)
        .build()
}
