package eu.kanade.presentation.more.stats

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLocale
import androidx.compose.ui.unit.dp
import eu.kanade.presentation.more.stats.components.StatsItem
import eu.kanade.presentation.more.stats.components.StatsOverviewItem
import eu.kanade.presentation.more.stats.data.StatsData
import eu.kanade.presentation.util.toDurationString
import mihon.icons.materialsymbols.MaterialSymbols
import mihon.icons.materialsymbols.rounded.CollectionsBookmark
import mihon.icons.materialsymbols.rounded.KeyboardArrowDown
import mihon.icons.materialsymbols.rounded.LocalLibrary
import mihon.icons.materialsymbols.rounded.Schedule
import tachiyomi.i18n.MR
import tachiyomi.i18n.chapters
import tachiyomi.i18n.in_library
import tachiyomi.i18n.komikita_active_days
import tachiyomi.i18n.komikita_completed_chapters
import tachiyomi.i18n.komikita_current_streak
import tachiyomi.i18n.komikita_longest_streak
import tachiyomi.i18n.komikita_monthly_reading
import tachiyomi.i18n.komikita_reading_titles
import tachiyomi.i18n.komikita_reading_wrapped
import tachiyomi.i18n.komikita_top_titles
import tachiyomi.i18n.komikita_wrapped_note
import tachiyomi.i18n.label_completed_titles
import tachiyomi.i18n.label_downloaded
import tachiyomi.i18n.label_local
import tachiyomi.i18n.label_mean_score
import tachiyomi.i18n.label_overview_section
import tachiyomi.i18n.label_read_chapters
import tachiyomi.i18n.label_read_duration
import tachiyomi.i18n.label_started
import tachiyomi.i18n.label_titles_in_global_update
import tachiyomi.i18n.label_titles_section
import tachiyomi.i18n.label_total_chapters
import tachiyomi.i18n.label_tracked_titles
import tachiyomi.i18n.label_tracker_section
import tachiyomi.i18n.label_used
import tachiyomi.i18n.none
import tachiyomi.i18n.not_applicable
import tachiyomi.presentation.core.components.SectionCard
import tachiyomi.presentation.core.components.material.padding
import tachiyomi.presentation.core.i18n.stringResource
import java.util.Locale
import kotlin.time.DurationUnit
import kotlin.time.toDuration

@Composable
fun StatsScreenContent(
    state: StatsScreenState.Success,
    paddingValues: PaddingValues,
    onSelectYear: (Int) -> Unit = {},
) {
    LazyColumn(
        contentPadding = paddingValues,
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.padding.small),
    ) {
        if (state.reading != null) {
            item { ReadingRecapSection(state, onSelectYear) }
        }
        item {
            OverviewSection(state.overview)
        }
        item {
            TitlesStats(state.titles)
        }
        item {
            ChapterStats(state.chapters)
        }
        item {
            TrackerStats(state.trackers)
        }
    }
}

@Composable
private fun LazyItemScope.ReadingRecapSection(state: StatsScreenState.Success, onSelectYear: (Int) -> Unit) {
    val recap = state.reading ?: return
    val context = LocalContext.current
    val locale = LocalLocale.current.platformLocale
    val none = stringResource(MR.strings.none)
    var yearMenu by remember { mutableStateOf(false) }
    SectionCard(MR.strings.komikita_reading_wrapped) {
        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Box {
                TextButton(onClick = { yearMenu = true }) {
                    Text(recap.year.toString())
                    Icon(MaterialSymbols.Rounded.KeyboardArrowDown, contentDescription = null)
                }
                DropdownMenu(expanded = yearMenu, onDismissRequest = { yearMenu = false }) {
                    state.availableYears.forEach { year ->
                        DropdownMenuItem(
                            text = { Text(year.toString()) },
                            onClick = {
                                onSelectYear(year)
                                yearMenu = false
                            },
                        )
                    }
                }
            }
            Row {
                StatsItem(recap.chapters.toString(), stringResource(MR.strings.komikita_completed_chapters))
                StatsItem(
                    recap.durationMillis.toDuration(
                        DurationUnit.MILLISECONDS,
                    ).toDurationString(context, fallback = none),
                    stringResource(MR.strings.label_read_duration),
                )
                StatsItem(recap.titles.toString(), stringResource(MR.strings.komikita_reading_titles))
            }
            Row {
                StatsItem(state.currentStreak.toString(), stringResource(MR.strings.komikita_current_streak))
                StatsItem(recap.longestStreak.toString(), stringResource(MR.strings.komikita_longest_streak))
                StatsItem(recap.activeDays.toString(), stringResource(MR.strings.komikita_active_days))
            }
            if (state.topTitles.isNotEmpty()) {
                Text(stringResource(MR.strings.komikita_top_titles), style = MaterialTheme.typography.titleSmall)
                state.topTitles.forEachIndexed { index, title -> Text("${index + 1}. $title") }
            }
            Text(stringResource(MR.strings.komikita_monthly_reading), style = MaterialTheme.typography.titleSmall)
            recap.monthlyDuration.forEachIndexed { index, duration ->
                if (duration > 0) {
                    val month = java.time.Month.of(
                        index + 1,
                    ).getDisplayName(java.time.format.TextStyle.FULL, locale)
                    Text(
                        "$month · ${duration.toDuration(
                            DurationUnit.MILLISECONDS,
                        ).toDurationString(context, fallback = none)}",
                    )
                }
            }
            Text(
                stringResource(MR.strings.komikita_wrapped_note),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun LazyItemScope.OverviewSection(
    data: StatsData.Overview,
) {
    val none = stringResource(MR.strings.none)
    val context = LocalContext.current
    val readDurationString = remember(data.totalReadDuration) {
        data.totalReadDuration
            .toDuration(DurationUnit.MILLISECONDS)
            .toDurationString(context, fallback = none)
    }
    SectionCard(MR.strings.label_overview_section) {
        Row(
            modifier = Modifier.height(IntrinsicSize.Min),
        ) {
            StatsOverviewItem(
                title = data.libraryMangaCount.toString(),
                subtitle = stringResource(MR.strings.in_library),
                icon = MaterialSymbols.Rounded.CollectionsBookmark,
            )
            StatsOverviewItem(
                title = readDurationString,
                subtitle = stringResource(MR.strings.label_read_duration),
                icon = MaterialSymbols.Rounded.Schedule,
            )
            StatsOverviewItem(
                title = data.completedMangaCount.toString(),
                subtitle = stringResource(MR.strings.label_completed_titles),
                icon = MaterialSymbols.Rounded.LocalLibrary,
            )
        }
    }
}

@Composable
private fun LazyItemScope.TitlesStats(
    data: StatsData.Titles,
) {
    SectionCard(MR.strings.label_titles_section) {
        Row {
            StatsItem(
                data.globalUpdateItemCount.toString(),
                stringResource(MR.strings.label_titles_in_global_update),
            )
            StatsItem(
                data.startedMangaCount.toString(),
                stringResource(MR.strings.label_started),
            )
            StatsItem(
                data.localMangaCount.toString(),
                stringResource(MR.strings.label_local),
            )
        }
    }
}

@Composable
private fun LazyItemScope.ChapterStats(
    data: StatsData.Chapters,
) {
    SectionCard(MR.strings.chapters) {
        Row {
            StatsItem(
                data.totalChapterCount.toString(),
                stringResource(MR.strings.label_total_chapters),
            )
            StatsItem(
                data.readChapterCount.toString(),
                stringResource(MR.strings.label_read_chapters),
            )
            StatsItem(
                data.downloadCount.toString(),
                stringResource(MR.strings.label_downloaded),
            )
        }
    }
}

@Composable
private fun LazyItemScope.TrackerStats(
    data: StatsData.Trackers,
) {
    val notApplicable = stringResource(MR.strings.not_applicable)
    val meanScoreStr = remember(data.trackedTitleCount, data.meanScore) {
        if (data.trackedTitleCount > 0 && !data.meanScore.isNaN()) {
            // All other numbers are localized in English
            "%.2f ★".format(Locale.ENGLISH, data.meanScore)
        } else {
            notApplicable
        }
    }
    SectionCard(MR.strings.label_tracker_section) {
        Row {
            StatsItem(
                data.trackedTitleCount.toString(),
                stringResource(MR.strings.label_tracked_titles),
            )
            StatsItem(
                meanScoreStr,
                stringResource(MR.strings.label_mean_score),
            )
            StatsItem(
                data.trackerCount.toString(),
                stringResource(MR.strings.label_used),
            )
        }
    }
}
