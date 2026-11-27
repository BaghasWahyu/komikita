package eu.kanade.domain.ui.model

import dev.icerock.moko.resources.StringResource
import tachiyomi.i18n.MR
import tachiyomi.i18n.label_default
import tachiyomi.i18n.pref_bottom_navigation_style_glass
import tachiyomi.i18n.pref_bottom_navigation_style_power_saving

enum class BottomNavigationStyle(val titleRes: StringResource) {
    DEFAULT(MR.strings.label_default),
    GLASS(MR.strings.pref_bottom_navigation_style_glass),
    POWER_SAVING(MR.strings.pref_bottom_navigation_style_power_saving),
}
