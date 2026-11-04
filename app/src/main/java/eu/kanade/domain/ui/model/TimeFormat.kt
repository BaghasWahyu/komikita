package eu.kanade.domain.ui.model

import dev.icerock.moko.resources.StringResource
import tachiyomi.i18n.MR
import tachiyomi.i18n.follow_language
import tachiyomi.i18n.follow_system
import tachiyomi.i18n.time_12_hours
import tachiyomi.i18n.time_24_hours

enum class TimeFormat(val titleRes: StringResource) {
    FOLLOW_LANGUAGE(MR.strings.follow_language),
    FOLLOW_SYSTEM(MR.strings.follow_system),
    HOURS_24(MR.strings.time_24_hours),
    HOURS_12(MR.strings.time_12_hours),
}
