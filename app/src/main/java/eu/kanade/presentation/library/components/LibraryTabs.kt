package eu.kanade.presentation.library.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.PagerState
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import eu.kanade.presentation.category.visualName
import eu.kanade.presentation.theme.LocalGlassUiEnabled
import tachiyomi.domain.category.model.Category
import tachiyomi.presentation.core.components.material.FilterChip
import tachiyomi.presentation.core.theme.glassFilterChipBorder
import tachiyomi.presentation.core.theme.glassFilterChipColors

@Composable
internal fun LibraryTabs(
    categories: List<Category>,
    pagerState: PagerState,
    getItemCountForCategory: (Category) -> Int?,
    onTabItemClick: (Int) -> Unit,
) {
    val currentPageIndex = pagerState.currentPage.coerceAtMost(categories.lastIndex)
    val useGlassUi = LocalGlassUiEnabled.current
    val scrollState = rememberLazyListState()
    LaunchedEffect(currentPageIndex) {
        if (currentPageIndex >= 0) scrollState.animateScrollToItem(currentPageIndex)
    }
    LazyRow(
        state = scrollState,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        itemsIndexed(categories, key = { _, category -> category.id }) { index, category ->
            val selected = currentPageIndex == index
            FilterChip(
                selected = selected,
                onClick = { onTabItemClick(index) },
                shape = MaterialTheme.shapes.medium,
                label = {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(text = category.visualName, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        getItemCountForCategory(category)?.let {
                            Text(text = it.toString(), style = MaterialTheme.typography.labelSmall)
                        }
                    }
                },
                colors = if (useGlassUi) {
                    glassFilterChipColors()
                } else {
                    FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                        selectedLabelColor = MaterialTheme.colorScheme.onSecondaryContainer,
                    )
                },
                border = if (useGlassUi) {
                    glassFilterChipBorder(enabled = true, selected = selected)
                } else {
                    FilterChipDefaults.filterChipBorder(enabled = true, selected = selected)
                },
            )
        }
    }
}
