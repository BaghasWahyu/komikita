package tachiyomi.domain.release.interactor

import dev.zacsweers.metro.Inject
import tachiyomi.domain.release.model.Release
import tachiyomi.domain.release.service.ReleaseService

@Inject
class GetApplicationRelease(
    private val service: ReleaseService,
) {
    suspend fun await(arguments: Arguments): Result {
        val release = service.latest(arguments) ?: return Result.NoNewUpdate

        // Check if latest version is different from current version
        val isNewVersion = isNewVersion(
            arguments.isNightly,
            arguments.commitCount,
            arguments.versionName,
            release.version,
        )
        return when {
            isNewVersion -> Result.NewUpdate(release)
            else -> Result.NoNewUpdate
        }
    }

    private fun isNewVersion(
        isNightly: Boolean,
        commitCount: Int,
        versionName: String,
        versionTag: String,
    ): Boolean {
        if (isNightly) {
            return versionTag.removePrefix("r").toIntOrNull()?.let { it > commitCount } ?: false
        }
        fun parse(version: String): List<Int>? {
            val parts = version.removePrefix("v").split(".")
            return parts.map { it.toIntOrNull()?.takeIf { value -> value >= 0 } ?: return null }
        }
        val newVersion = parse(versionTag) ?: return false
        val oldVersion = parse(versionName) ?: return false
        for (index in 0 until maxOf(newVersion.size, oldVersion.size)) {
            val comparison = newVersion.getOrElse(index) { 0 }.compareTo(oldVersion.getOrElse(index) { 0 })
            if (comparison != 0) return comparison > 0
        }
        return false
    }

    data class Arguments(
        val isFoss: Boolean,
        val isNightly: Boolean,
        val commitCount: Int,
        val versionName: String,
        val repository: String,
        val forceCheck: Boolean = false,
    )

    sealed interface Result {
        data class NewUpdate(val release: Release) : Result
        data object NoNewUpdate : Result
        data object OsTooOld : Result
    }
}
