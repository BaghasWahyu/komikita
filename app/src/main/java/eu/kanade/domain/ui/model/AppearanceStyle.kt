package eu.kanade.domain.ui.model

import dev.icerock.moko.resources.StringResource
import tachiyomi.i18n.MR
import tachiyomi.i18n.label_default
import tachiyomi.i18n.pref_appearance_style_glass

enum class AppearanceStyle(val titleRes: StringResource) {
    DEFAULT(MR.strings.label_default),
    GLASS(MR.strings.pref_appearance_style_glass),
}
