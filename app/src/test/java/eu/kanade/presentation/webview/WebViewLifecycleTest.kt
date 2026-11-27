package eu.kanade.presentation.webview

import android.view.ViewGroup
import android.webkit.WebView
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.verify
import io.mockk.verifyOrder
import org.junit.jupiter.api.Test

class WebViewLifecycleTest {
    @Test
    fun rendererCallbacksReleaseEachViewAndCloseTheScreenOnce() {
        val parent = mockk<ViewGroup>()
        val main = webView(parent)
        val popup = webView()
        justRun { parent.removeView(main) }
        val lifecycle = WebViewLifecycle()
        lifecycle.register(main, "https://example.org")
        lifecycle.register(popup, "https://example.org/popup")
        var closeCount = 0

        lifecycle.release(main) shouldBe "https://example.org"
        lifecycle.close { closeCount++ }
        lifecycle.release(popup) shouldBe "https://example.org/popup"
        lifecycle.close { closeCount++ }
        lifecycle.dispose()

        verifyOrder {
            parent.removeView(main)
            main.destroy()
        }
        verify(exactly = 1) { main.destroy() }
        verify(exactly = 1) { popup.destroy() }
        closeCount shouldBe 1
    }

    @Test
    fun disposingTheScreenReleasesParkedViewsAndIgnoresLateUrlUpdates() {
        val parked = webView()
        val lifecycle = WebViewLifecycle()
        lifecycle.register(parked, "https://example.org")

        lifecycle.dispose()
        lifecycle.updateUrl(parked, "https://example.org/late")
        lifecycle.release(parked) shouldBe null
        lifecycle.url(parked) shouldBe null
        lifecycle.isActive shouldBe false
        verify(exactly = 1) { parked.destroy() }
    }

    @Test
    fun pauseAndResumeOnlyTouchRegisteredActiveViews() {
        val main = webView()
        val popup = webView()
        val lifecycle = WebViewLifecycle()
        lifecycle.register(main, "https://example.org")
        lifecycle.register(popup, "https://example.org/popup")

        lifecycle.pause(main)
        lifecycle.resume(popup)
        lifecycle.pauseAll()
        lifecycle.release(popup)
        lifecycle.resume(popup)
        lifecycle.close()
        lifecycle.resume(main)

        verify(exactly = 2) { main.onPause() }
        verify(exactly = 0) { main.onResume() }
        verify(exactly = 1) { popup.onPause() }
        verify(exactly = 1) { popup.onResume() }
    }

    private fun webView(parent: ViewGroup? = null) = mockk<WebView> {
        every { this@mockk.parent } returns parent
        justRun { onPause() }
        justRun { onResume() }
        justRun { destroy() }
    }
}
