package eu.kanade.presentation.browse.components

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import eu.kanade.presentation.components.SearchToolbar
import eu.kanade.presentation.theme.LocalGlassUiEnabled
import eu.kanade.presentation.theme.glassEdge
import eu.kanade.tachiyomi.ui.browse.source.globalsearch.SourceFilter
import mihon.icons.materialsymbols.MaterialSymbols
import mihon.icons.materialsymbols.rounded.DoneAll
import mihon.icons.materialsymbols.rounded.FilterList
import mihon.icons.materialsymbols.rounded.Keep
import tachiyomi.i18n.MR
import tachiyomi.i18n.all
import tachiyomi.i18n.has_results
import tachiyomi.i18n.pinned_sources
import tachiyomi.presentation.core.components.material.FilterChip
import tachiyomi.presentation.core.components.material.padding
import tachiyomi.presentation.core.i18n.stringResource
import tachiyomi.presentation.core.theme.glassFilterChipBorder
import tachiyomi.presentation.core.theme.glassFilterChipColors

@Composable
fun GlobalSearchToolbar(
    searchQuery: String?,
    progress: Int,
    total: Int,
    navigateUp: () -> Unit,
    onChangeSearchQuery: (String?) -> Unit,
    onSearch: (String) -> Unit,
    hideSourceFilter: Boolean,
    sourceFilter: SourceFilter,
    onChangeSearchFilter: (SourceFilter) -> Unit,
    onlyShowHasResults: Boolean,
    onToggleResults: () -> Unit,
    scrollBehavior: TopAppBarScrollBehavior,
) {
    val useGlassUi = LocalGlassUiEnabled.current
    val colors = MaterialTheme.colorScheme
    val chipColors = if (useGlassUi) glassFilterChipColors() else FilterChipDefaults.filterChipColors()
    Column(
        modifier = Modifier
            .background(
                if (useGlassUi) colors.surfaceContainerLow.copy(alpha = 0.84f) else colors.surface,
            )
            .then(
                if (useGlassUi) Modifier.glassEdge(colors.outlineVariant, atTop = false) else Modifier,
            ),
    ) {
        Box {
            SearchToolbar(
                searchQuery = searchQuery,
                onChangeSearchQuery = onChangeSearchQuery,
                onSearch = onSearch,
                onClickCloseSearch = navigateUp,
                navigateUp = navigateUp,
                scrollBehavior = scrollBehavior,
            )
            if (progress in 1..<total) {
                LinearProgressIndicator(
                    progress = { progress / total.toFloat() },
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .fillMaxWidth(),
                )
            }
        }

        Row(
            modifier = Modifier
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = MaterialTheme.padding.small),
            horizontalArrangement = Arrangement.spacedBy(MaterialTheme.padding.small),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // TODO: make this UX better; it only applies when triggering a new search
            if (!hideSourceFilter) {
                val pinnedSelected = sourceFilter == SourceFilter.PinnedOnly
                FilterChip(
                    selected = pinnedSelected,
                    onClick = { onChangeSearchFilter(SourceFilter.PinnedOnly) },
                    leadingIcon = {
                        Icon(
                            imageVector = MaterialSymbols.Rounded.Keep,
                            contentDescription = null,
                            modifier = Modifier
                                .size(FilterChipDefaults.IconSize),
                        )
                    },
                    label = {
                        Text(text = stringResource(MR.strings.pinned_sources))
                    },
                    colors = chipColors,
                    border = if (useGlassUi) {
                        glassFilterChipBorder(enabled = true, selected = pinnedSelected)
                    } else {
                        FilterChipDefaults.filterChipBorder(enabled = true, selected = pinnedSelected)
                    },
                )
                val allSelected = sourceFilter == SourceFilter.All
                FilterChip(
                    selected = allSelected,
                    onClick = { onChangeSearchFilter(SourceFilter.All) },
                    leadingIcon = {
                        Icon(
                            imageVector = MaterialSymbols.Rounded.DoneAll,
                            contentDescription = null,
                            modifier = Modifier
                                .size(FilterChipDefaults.IconSize),
                        )
                    },
                    label = {
                        Text(text = stringResource(MR.strings.all))
                    },
                    colors = chipColors,
                    border = if (useGlassUi) {
                        glassFilterChipBorder(enabled = true, selected = allSelected)
                    } else {
                        FilterChipDefaults.filterChipBorder(enabled = true, selected = allSelected)
                    },
                )

                VerticalDivider(modifier = Modifier.height(FilterChipDefaults.Height))
            }

            FilterChip(
                selected = onlyShowHasResults,
                onClick = { onToggleResults() },
                leadingIcon = {
                    Icon(
                        imageVector = MaterialSymbols.Rounded.FilterList,
                        contentDescription = null,
                        modifier = Modifier
                            .size(FilterChipDefaults.IconSize),
                    )
                },
                label = {
                    Text(text = stringResource(MR.strings.has_results))
                },
                colors = chipColors,
                border = if (useGlassUi) {
                    glassFilterChipBorder(enabled = true, selected = onlyShowHasResults)
                } else {
                    FilterChipDefaults.filterChipBorder(enabled = true, selected = onlyShowHasResults)
                },
            )
        }

        if (!useGlassUi) {
            HorizontalDivider()
        }
    }
}
