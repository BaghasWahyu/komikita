package eu.kanade.presentation.more.settings.screen

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLocale
import androidx.core.app.ActivityCompat
import eu.kanade.domain.ui.UiPreferences
import eu.kanade.domain.ui.model.AppearanceStyle
import eu.kanade.domain.ui.model.TabletUiMode
import eu.kanade.domain.ui.model.ThemeMode
import eu.kanade.domain.ui.model.TimeFormat
import eu.kanade.domain.ui.model.setAppCompatDelegateThemeMode
import eu.kanade.presentation.more.settings.Preference
import eu.kanade.presentation.more.settings.widget.AppThemeModePreferenceWidget
import eu.kanade.presentation.more.settings.widget.AppThemePreferenceWidget
import eu.kanade.presentation.more.settings.widget.ListPreferenceWidget
import eu.kanade.presentation.util.rememberIs24HourSystem
import eu.kanade.tachiyomi.util.system.toast
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toJavaLocalDateTime
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.Serializable
import mihon.app.di.appGraph
import mihon.core.navigation.AppLanguageRoute
import mihon.core.navigation.LocalNavigator
import tachiyomi.i18n.MR
import tachiyomi.i18n.action_apply
import tachiyomi.i18n.action_cancel
import tachiyomi.i18n.label_default
import tachiyomi.i18n.pref_app_language
import tachiyomi.i18n.pref_app_theme
import tachiyomi.i18n.pref_appearance_style
import tachiyomi.i18n.pref_appearance_style_glass_unavailable
import tachiyomi.i18n.pref_appearance_style_glass_warning
import tachiyomi.i18n.pref_category_appearance
import tachiyomi.i18n.pref_category_display
import tachiyomi.i18n.pref_category_theme
import tachiyomi.i18n.pref_dark_theme_pure_black
import tachiyomi.i18n.pref_date_format
import tachiyomi.i18n.pref_display_images_description
import tachiyomi.i18n.pref_relative_format
import tachiyomi.i18n.pref_relative_format_summary
import tachiyomi.i18n.pref_tablet_ui_mode
import tachiyomi.i18n.pref_time_format
import tachiyomi.i18n.relative_time_today
import tachiyomi.i18n.requires_app_restart
import tachiyomi.presentation.core.i18n.stringResource
import tachiyomi.presentation.core.util.collectAsState
import kotlin.time.Clock

@Serializable
data object SettingsAppearanceScreen : SearchableSettings {

    @ReadOnlyComposable
    @Composable
    override fun getTitleRes() = MR.strings.pref_category_appearance

    @Composable
    override fun getPreferences(): List<Preference> {
        val context = LocalContext.current
        val uiPreferences = remember { context.appGraph.uiPreferences }

        return listOf(
            getThemeGroup(uiPreferences = uiPreferences),
            getDisplayGroup(uiPreferences = uiPreferences),
        )
    }

    @Composable
    private fun getThemeGroup(
        uiPreferences: UiPreferences,
    ): Preference.PreferenceGroup {
        val context = LocalContext.current

        val themeModePref = uiPreferences.themeMode
        val themeMode by themeModePref.collectAsState()

        val appThemePref = uiPreferences.appTheme
        val appTheme by appThemePref.collectAsState()

        val amoledPref = uiPreferences.themeDarkAmoled
        val amoled by amoledPref.collectAsState()

        return Preference.PreferenceGroup(
            title = stringResource(MR.strings.pref_category_theme),
            preferenceItems = listOf(
                Preference.PreferenceItem.CustomPreference(
                    title = stringResource(MR.strings.pref_app_theme),
                ) {
                    Column {
                        AppThemeModePreferenceWidget(
                            value = themeMode,
                            onItemClick = {
                                themeModePref.set(it)
                                setAppCompatDelegateThemeMode(it)
                            },
                        )

                        AppThemePreferenceWidget(
                            value = appTheme,
                            amoled = amoled,
                            onItemClick = { appThemePref.set(it) },
                        )
                    }
                },
                Preference.PreferenceItem.SwitchPreference(
                    preference = amoledPref,
                    title = stringResource(MR.strings.pref_dark_theme_pure_black),
                    visible = themeMode != ThemeMode.LIGHT,
                    onValueChanged = {
                        (context as? Activity)?.let { ActivityCompat.recreate(it) }
                        true
                    },
                ),
            ),
        )
    }

    @Composable
    private fun getDisplayGroup(
        uiPreferences: UiPreferences,
    ): Preference.PreferenceGroup {
        val context = LocalContext.current
        val navigator = LocalNavigator.current
        val locale = LocalLocale.current
        val is24HourSystem = rememberIs24HourSystem()

        val now = remember { Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).toJavaLocalDateTime() }

        val dateFormat by uiPreferences.dateFormat.collectAsState()
        val formattedDateNow = remember(dateFormat) {
            UiPreferences.dateFormat(dateFormat, locale.platformLocale).format(now)
        }
        val appearanceStyleTitle = stringResource(MR.strings.pref_appearance_style)

        return Preference.PreferenceGroup(
            title = stringResource(MR.strings.pref_category_display),
            preferenceItems = listOf(
                Preference.PreferenceItem.TextPreference(
                    title = stringResource(MR.strings.pref_app_language),
                    onClick = { navigator.push(AppLanguageRoute) },
                ),
                Preference.PreferenceItem.CustomPreference(title = appearanceStyleTitle) {
                    AppearanceStylePreference(uiPreferences, appearanceStyleTitle)
                },
                Preference.PreferenceItem.ListPreference(
                    preference = uiPreferences.tabletUiMode,
                    entries = TabletUiMode.entries
                        .associateWith { stringResource(it.titleRes) },
                    title = stringResource(MR.strings.pref_tablet_ui_mode),
                    onValueChanged = {
                        context.toast(MR.strings.requires_app_restart)
                        true
                    },
                ),
                Preference.PreferenceItem.ListPreference(
                    preference = uiPreferences.dateFormat,
                    entries = DateFormats
                        .associateWith {
                            val formattedDate = UiPreferences.dateFormat(it, locale.platformLocale).format(now)
                            "${it.ifEmpty { stringResource(MR.strings.label_default) }} ($formattedDate)"
                        },
                    title = stringResource(MR.strings.pref_date_format),
                ),
                Preference.PreferenceItem.ListPreference(
                    preference = uiPreferences.timeFormat,
                    entries = TimeFormat.entries
                        .associateWith {
                            val formattedTime = UiPreferences.timeFormat(
                                it,
                                locale.platformLocale,
                                is24HourSystem,
                            ).format(now)
                            "${stringResource(it.titleRes)} ($formattedTime)"
                        },
                    title = stringResource(MR.strings.pref_time_format),
                ),
                Preference.PreferenceItem.SwitchPreference(
                    preference = uiPreferences.relativeTime,
                    title = stringResource(MR.strings.pref_relative_format),
                    subtitle = stringResource(
                        MR.strings.pref_relative_format_summary,
                        stringResource(MR.strings.relative_time_today),
                        formattedDateNow,
                    ),
                ),
                Preference.PreferenceItem.SwitchPreference(
                    preference = uiPreferences.imagesInDescription,
                    title = stringResource(MR.strings.pref_display_images_description),
                ),
            ),
        )
    }
}

@Composable
private fun AppearanceStylePreference(uiPreferences: UiPreferences, title: String) {
    val preference = uiPreferences.appearanceStyle
    val style by preference.collectAsState()
    var showGlassWarning by rememberSaveable { mutableStateOf(false) }

    ListPreferenceWidget(
        value = style,
        title = title,
        subtitle = stringResource(style.titleRes),
        icon = null,
        entries = AppearanceStyle.entries.associateWith { stringResource(it.titleRes) },
        onValueChange = {
            if (it == AppearanceStyle.GLASS) {
                showGlassWarning = true
            } else {
                preference.set(it)
            }
        },
    )

    if (showGlassWarning) {
        val message = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            MR.strings.pref_appearance_style_glass_warning
        } else {
            MR.strings.pref_appearance_style_glass_unavailable
        }
        AlertDialog(
            onDismissRequest = { showGlassWarning = false },
            title = { Text(stringResource(AppearanceStyle.GLASS.titleRes)) },
            text = { Text(stringResource(message)) },
            confirmButton = {
                TextButton(onClick = {
                    preference.set(AppearanceStyle.GLASS)
                    showGlassWarning = false
                }) {
                    Text(stringResource(MR.strings.action_apply))
                }
            },
            dismissButton = {
                TextButton(onClick = { showGlassWarning = false }) {
                    Text(stringResource(MR.strings.action_cancel))
                }
            },
        )
    }
}

private val DateFormats = listOf(
    "", // Default
    "MM/dd/yy",
    "dd/MM/yy",
    "yyyy-MM-dd",
    "dd MMM yyyy",
    "MMM dd, yyyy",
)
