package tachiyomi.domain.release.interactor

import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import tachiyomi.domain.release.model.Release
import tachiyomi.domain.release.service.ReleaseService

class GetApplicationReleaseTest {

    private lateinit var getApplicationRelease: GetApplicationRelease
    private lateinit var releaseService: ReleaseService

    @BeforeEach
    fun beforeEach() {
        releaseService = mockk()

        getApplicationRelease = GetApplicationRelease(releaseService)
    }

    @Test
    fun `When has update but is nightly expect new update`() = runTest {
        val release = Release(
            "r2000",
            "info",
            "http://example.com/release_link",
            "http://example.com/release_link.apk",
        )

        coEvery { releaseService.latest(any()) } returns release

        val result = getApplicationRelease.await(
            GetApplicationRelease.Arguments(
                isFoss = false,
                isNightly = true,
                commitCount = 1000,
                versionName = "",
                repository = "test",
            ),
        )

        (result as GetApplicationRelease.Result.NewUpdate).release shouldBe GetApplicationRelease.Result.NewUpdate(
            release,
        ).release
    }

    @Test
    fun `When has update expect new update`() = runTest {
        val release = Release(
            "v2.0.0",
            "info",
            "http://example.com/release_link",
            "http://example.com/release_link.apk",
        )

        coEvery { releaseService.latest(any()) } returns release

        val result = getApplicationRelease.await(
            GetApplicationRelease.Arguments(
                isFoss = false,
                isNightly = false,
                commitCount = 0,
                versionName = "v1.0.0",
                repository = "test",
            ),
        )

        (result as GetApplicationRelease.Result.NewUpdate).release shouldBe GetApplicationRelease.Result.NewUpdate(
            release,
        ).release
    }

    @Test
    fun `When has no update expect no new update`() = runTest {
        val release = Release(
            "v1.0.0",
            "info",
            "http://example.com/release_link",
            "http://example.com/release_link.apk",
        )

        coEvery { releaseService.latest(any()) } returns release

        val result = getApplicationRelease.await(
            GetApplicationRelease.Arguments(
                isFoss = false,
                isNightly = false,
                commitCount = 0,
                versionName = "v2.0.0",
                repository = "test",
            ),
        )

        result shouldBe GetApplicationRelease.Result.NoNewUpdate
    }

    @Test
    fun rejectsOlderMajorOrMinorEvenWithHigherPatch() = runTest {
        for ((current, offered) in listOf("1.0.0" to "v0.99.9", "0.21.3" to "v0.20.4")) {
            coEvery { releaseService.latest(any()) } returns Release(offered, "", "", "")
            getApplicationRelease.await(
                GetApplicationRelease.Arguments(false, false, 0, current, "test"),
            ) shouldBe GetApplicationRelease.Result.NoNewUpdate
        }
    }

    @Test
    fun handlesShortOrMalformedVersionTags() = runTest {
        for (offered in listOf("v1.0", "invalid", "v1..2")) {
            coEvery { releaseService.latest(any()) } returns Release(offered, "", "", "")
            getApplicationRelease.await(
                GetApplicationRelease.Arguments(false, false, 0, "1.0.0", "test"),
            ) shouldBe GetApplicationRelease.Result.NoNewUpdate
        }
    }

    @Test
    fun detectsLatestKomiKitaReleaseForOlderStableAndFossBuilds() = runTest {
        val release = Release("v1.0.5", "", "", "")
        coEvery { releaseService.latest(any()) } returns release

        for (isFoss in listOf(false, true)) {
            getApplicationRelease.await(
                GetApplicationRelease.Arguments(isFoss, false, 0, "1.0.4", "BaghasWahyu/komikita", true),
            ) shouldBe GetApplicationRelease.Result.NewUpdate(release)
        }
    }

    @Test
    fun doesNotOfferLatestReleaseToEqualOrNewerInstalledVersions() = runTest {
        coEvery { releaseService.latest(any()) } returns Release("v1.0.5", "", "", "")

        for (current in listOf("1.0.5", "1.0.6")) {
            getApplicationRelease.await(
                GetApplicationRelease.Arguments(false, false, 0, current, "BaghasWahyu/komikita", true),
            ) shouldBe GetApplicationRelease.Result.NoNewUpdate
        }
    }
}
