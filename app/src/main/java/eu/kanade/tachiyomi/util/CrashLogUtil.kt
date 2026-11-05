package eu.kanade.tachiyomi.util

import android.content.Context
import android.os.Build
import android.os.Process
import dev.zacsweers.metro.Inject
import eu.kanade.domain.base.BasePreferences
import eu.kanade.tachiyomi.BuildConfig
import eu.kanade.tachiyomi.extension.ExtensionManager
import eu.kanade.tachiyomi.extension.model.Extension
import eu.kanade.tachiyomi.util.storage.getUriCompat
import eu.kanade.tachiyomi.util.system.WebViewUtil
import eu.kanade.tachiyomi.util.system.createFileInCacheDir
import eu.kanade.tachiyomi.util.system.toShareIntent
import eu.kanade.tachiyomi.util.system.toast
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.datetime.TimeZone
import kotlinx.datetime.offsetAt
import kotlinx.datetime.toLocalDateTime
import tachiyomi.domain.library.service.LibraryVisibility
import tachiyomi.domain.manga.interactor.GetManga
import tachiyomi.i18n.MR
import tachiyomi.i18n.komikita_export_logs_failed
import java.io.File
import java.util.concurrent.TimeUnit
import kotlin.time.Clock

@Inject
class CrashLogUtil(
    private val context: Context,
    private val extensionManager: ExtensionManager,
    private val preferences: BasePreferences,
    private val visibility: LibraryVisibility,
    private val getManga: GetManga,
) {

    suspend fun dumpLogs(exception: Throwable? = null) = withContext(Dispatchers.IO) {
        val rawLog = File.createTempFile("komikita-logcat-", ".tmp", context.cacheDir)
        try {
            val file = context.createFileInCacheDir("komikita_diagnostics.txt")
            val process = ProcessBuilder(
                "logcat",
                "--pid=${Process.myPid()}",
                "-d",
                "-t",
                "2000",
                "-v",
                "threadtime",
            ).redirectErrorStream(true).redirectOutput(rawLog).start()
            try {
                check(process.waitFor(10, TimeUnit.SECONDS)) { "Log collection timed out" }
                check(process.exitValue() == 0) { "Log collection failed" }
            } finally {
                if (process.isAlive) process.destroyForcibly()
            }
            val hiddenTitles = visibility.hiddenMangaIds().mapNotNull { getManga.await(it)?.title }
            val diagnostics = buildString {
                appendLine(getDebugInfo())
                getExtensionsInfo()?.let { appendLine(it) }
                exception?.let { appendLine(it.stackTraceToString()) }
                appendLine("\nRecent application logs (up to 2000 entries):")
                append(rawLog.inputStream().bufferedReader().use { it.readText().takeLast(2_000_000) })
            }
            file.writeText(LogRedactor.redact(diagnostics, hiddenTitles))

            val uri = file.getUriCompat(context)
            withContext(Dispatchers.Main) { context.startActivity(uri.toShareIntent(context, "text/plain")) }
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) {
            withContext(Dispatchers.Main) { context.toast(MR.strings.komikita_export_logs_failed) }
        } finally {
            rawLog.delete()
        }
    }

    fun getDebugInfo(): String {
        val now = Clock.System.now()
        val tz = TimeZone.currentSystemDefault()
        return """
            App ID: ${BuildConfig.APPLICATION_ID}
            App version: ${BuildConfig.VERSION_NAME} (${BuildConfig.COMMIT_SHA}, ${BuildConfig.VERSION_CODE}, ${BuildConfig.BUILD_TIME})
            Installation ID: ${preferences.installationId.get()}
            Skipped app update: ${preferences.skippedAppUpdate.get().ifEmpty { "None" }}
            Android version: ${Build.VERSION.RELEASE} (SDK ${Build.VERSION.SDK_INT}; build ${Build.DISPLAY})
            Device brand: ${Build.BRAND}
            Device manufacturer: ${Build.MANUFACTURER}
            Device name: ${Build.DEVICE} (${Build.PRODUCT})
            Device model: ${Build.MODEL}
            WebView: ${WebViewUtil.getVersion(context)}
            Current time: ${now.toLocalDateTime(tz)}${tz.offsetAt(now)}
        """.trimIndent()
    }

    private suspend fun getExtensionsInfo(): String? {
        val availableExtensions = extensionManager.availableExtensionsFlow.value.associateBy { it.pkgName }

        val outdatedInfoList = extensionManager.getLoadedExtensions()
            .sortedBy { it.name }
            .mapNotNull {
                val availableExtension = availableExtensions[it.pkgName]
                val hasUpdate = (availableExtension?.versionCode ?: 0) > it.versionCode

                if (!hasUpdate && !it.isObsolete) return@mapNotNull null

                """
                    - ${it.name}
                      Installed: ${it.versionName} / Available: ${availableExtension?.versionName ?: "?"}
                      Orphaned: ${it.isObsolete}
                """.trimIndent()
            }

        val notLoadedInfoList = extensionManager.getNotLoadedExtensions()
            .sortedBy { it.name }
            .map { extension ->
                buildString {
                    appendLine("- ${extension.name}")
                    appendLine("  Installed: ${extension.versionName} (lib ${extension.libVersion ?: "?"})")
                    append("  Not loaded: ${extension.reason.description}")

                    val reason = extension.reason
                    if (reason is Extension.NotLoaded.Reason.Failed) {
                        appendLine()
                        append(reason.stackTrace.trimEnd().prependIndent("  "))
                    }
                }
            }

        val extensionInfoList = outdatedInfoList + notLoadedInfoList

        return if (extensionInfoList.isNotEmpty()) {
            (listOf("Problematic extensions:") + extensionInfoList)
                .joinToString("\n")
        } else {
            null
        }
    }
}

private val Extension.NotLoaded.Reason.description: String
    get() = when (this) {
        is Extension.NotLoaded.Reason.Untrusted -> "Untrusted"
        Extension.NotLoaded.Reason.Filtered -> "Filtered by content warning"
        Extension.NotLoaded.Reason.Unsigned -> "Unsigned"
        Extension.NotLoaded.Reason.UnsupportedLibVersion -> "Unsupported lib version"
        Extension.NotLoaded.Reason.Malformed -> "Malformed"
        is Extension.NotLoaded.Reason.Failed -> "Failed ($message)"
    }
