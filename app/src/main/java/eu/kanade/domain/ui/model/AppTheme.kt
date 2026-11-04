package eu.kanade.domain.ui.model

import dev.icerock.moko.resources.StringResource
import tachiyomi.i18n.MR
import tachiyomi.i18n.label_default
import tachiyomi.i18n.theme_catppuccin
import tachiyomi.i18n.theme_greenapple
import tachiyomi.i18n.theme_lavender
import tachiyomi.i18n.theme_midnightdusk
import tachiyomi.i18n.theme_monet
import tachiyomi.i18n.theme_monochrome
import tachiyomi.i18n.theme_nord
import tachiyomi.i18n.theme_strawberrydaiquiri
import tachiyomi.i18n.theme_tako
import tachiyomi.i18n.theme_tealturquoise
import tachiyomi.i18n.theme_tidalwave
import tachiyomi.i18n.theme_tokyonight
import tachiyomi.i18n.theme_yinyang
import tachiyomi.i18n.theme_yotsuba

enum class AppTheme(val titleRes: StringResource?) {
    DEFAULT(MR.strings.label_default),
    MONET(MR.strings.theme_monet),
    CATPPUCCIN(MR.strings.theme_catppuccin),
    GREEN_APPLE(MR.strings.theme_greenapple),
    LAVENDER(MR.strings.theme_lavender),
    MIDNIGHT_DUSK(MR.strings.theme_midnightdusk),
    NORD(MR.strings.theme_nord),
    STRAWBERRY_DAIQUIRI(MR.strings.theme_strawberrydaiquiri),
    TAKO(MR.strings.theme_tako),
    TEALTURQUOISE(MR.strings.theme_tealturquoise),
    TIDAL_WAVE(MR.strings.theme_tidalwave),
    YINYANG(MR.strings.theme_yinyang),
    YOTSUBA(MR.strings.theme_yotsuba),
    TOKYONIGHT(MR.strings.theme_tokyonight),
    MONOCHROME(MR.strings.theme_monochrome),

    // Deprecated
    DARK_BLUE(null),
    HOT_PINK(null),
    BLUE(null),
}
