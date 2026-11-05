package eu.kanade.presentation.more.stats

import androidx.compose.runtime.Immutable
import eu.kanade.presentation.more.stats.data.StatsData
import tachiyomi.domain.history.model.ReadingRecap

sealed interface StatsScreenState {
    @Immutable
    data object Loading : StatsScreenState

    @Immutable
    data class Success(
        val overview: StatsData.Overview,
        val titles: StatsData.Titles,
        val chapters: StatsData.Chapters,
        val trackers: StatsData.Trackers,
        val reading: ReadingRecap? = null,
        val currentStreak: Int = 0,
        val availableYears: List<Int> = emptyList(),
        val topTitles: List<String> = emptyList(),
    ) : StatsScreenState
}
