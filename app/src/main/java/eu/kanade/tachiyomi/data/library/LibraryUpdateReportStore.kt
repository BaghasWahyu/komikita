package eu.kanade.tachiyomi.data.library

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json
import tachiyomi.core.common.preference.Preference
import tachiyomi.core.common.preference.PreferenceStore

/** Only the latest run is retained. App-state preferences are excluded from backups. */
@Inject
@SingleIn(AppScope::class)
class LibraryUpdateReportStore(
    preferenceStore: PreferenceStore,
    private val json: Json,
) {
    private val report = preferenceStore.getString(Preference.appStateKey("komikita_library_update_report"), "")

    fun get(): LibraryUpdateReport? = decode(report.get())

    fun changes(): Flow<LibraryUpdateReport?> = report.changes().map(::decode)

    fun save(value: LibraryUpdateReport) {
        report.set(json.encodeToString(LibraryUpdateReport.serializer(), value))
    }

    private fun decode(value: String): LibraryUpdateReport? = runCatching {
        json.decodeFromString(LibraryUpdateReport.serializer(), value)
    }.getOrNull()
}
