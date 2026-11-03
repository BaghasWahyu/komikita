package tachiyomi.data.release

import com.sun.net.httpserver.HttpServer
import eu.kanade.tachiyomi.network.NetworkHelper
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import okhttp3.Cache
import okhttp3.OkHttpClient
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import tachiyomi.domain.release.interactor.GetApplicationRelease
import java.io.File
import java.net.InetSocketAddress
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicReference

class ReleaseServiceImplTest {

    @TempDir
    lateinit var cacheDirectory: File

    private lateinit var server: HttpServer
    private lateinit var client: OkHttpClient
    private lateinit var getApplicationRelease: GetApplicationRelease
    private val publishedVersion = AtomicReference("v1.0.4")
    private val requestCount = AtomicInteger()

    @BeforeEach
    fun setUp() {
        server = HttpServer.create(InetSocketAddress("127.0.0.1", 0), 0).apply {
            createContext("/repos/BaghasWahyu/komikita/releases/latest") { exchange ->
                requestCount.incrementAndGet()
                val version = publishedVersion.get()
                val response = """
                    {
                      "tag_name": "$version",
                      "body": "Release notes",
                      "html_url": "https://github.com/BaghasWahyu/komikita/releases/tag/$version",
                      "assets": [
                        {
                          "name": "komikita-$version.apk",
                          "browser_download_url": "https://github.com/BaghasWahyu/komikita/releases/download/$version/komikita-$version.apk"
                        },
                        {
                          "name": "komikita-$version-foss.apk",
                          "browser_download_url": "https://github.com/BaghasWahyu/komikita/releases/download/$version/komikita-$version-foss.apk"
                        }
                      ]
                    }
                """.trimIndent().toByteArray()
                exchange.responseHeaders.add("Content-Type", "application/json")
                exchange.responseHeaders.add("Cache-Control", "public, max-age=60")
                exchange.sendResponseHeaders(200, response.size.toLong())
                exchange.responseBody.use { it.write(response) }
            }
            start()
        }
        client = OkHttpClient.Builder()
            .cache(Cache(cacheDirectory, 1024 * 1024))
            .addInterceptor { chain ->
                val request = chain.request()
                request.url.toString() shouldBe "https://api.github.com/repos/BaghasWahyu/komikita/releases/latest"
                chain.proceed(
                    request.newBuilder()
                        .url(
                            request.url.newBuilder()
                                .scheme("http")
                                .host("127.0.0.1")
                                .port(server.address.port)
                                .build(),
                        )
                        .build(),
                )
            }
            .build()
        val network = mockk<NetworkHelper> {
            every { this@mockk.client } returns this@ReleaseServiceImplTest.client
        }
        getApplicationRelease = GetApplicationRelease(ReleaseServiceImpl(network, Json))
    }

    @AfterEach
    fun tearDown() {
        server.stop(0)
        client.connectionPool.evictAll()
        client.dispatcher.executorService.shutdown()
        client.cache?.close()
    }

    @Test
    fun manualCheckDetectsNewReleaseEvenWhenPreviousResponseIsCached() = runTest {
        val arguments = GetApplicationRelease.Arguments(
            isFoss = true,
            isNightly = false,
            commitCount = 0,
            versionName = "1.0.4",
            repository = "BaghasWahyu/komikita",
        )

        getApplicationRelease.await(arguments) shouldBe GetApplicationRelease.Result.NoNewUpdate
        requestCount.get() shouldBe 1

        publishedVersion.set("v1.0.5")
        getApplicationRelease.await(arguments) shouldBe GetApplicationRelease.Result.NoNewUpdate
        requestCount.get() shouldBe 1

        val result = getApplicationRelease.await(arguments.copy(forceCheck = true))
        val release = (result as GetApplicationRelease.Result.NewUpdate).release
        release.version shouldBe "v1.0.5"
        release.downloadLink shouldBe
            "https://github.com/BaghasWahyu/komikita/releases/download/v1.0.5/komikita-v1.0.5-foss.apk"
        requestCount.get() shouldBe 2

        val cachedResult = getApplicationRelease.await(arguments)
        (cachedResult as GetApplicationRelease.Result.NewUpdate).release shouldBe release
        requestCount.get() shouldBe 2
    }
}
