package eu.kanade.presentation.library.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.PagerState
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import eu.kanade.presentation.category.visualName
import tachiyomi.domain.category.model.Category

@Composable
internal fun LibraryTabs(
    categories: List<Category>,
    pagerState: PagerState,
    getItemCountForCategory: (Category) -> Int?,
    onTabItemClick: (Int) -> Unit,
) {
    val currentPageIndex = pagerState.currentPage.coerceAtMost(categories.lastIndex)
    val scrollState = rememberLazyListState()
    LaunchedEffect(currentPageIndex) {
        if (currentPageIndex >= 0) scrollState.animateScrollToItem(currentPageIndex)
    }
    LazyRow(
        state = scrollState,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        itemsIndexed(categories, key = { _, category -> category.id }) { index, category ->
            FilterChip(
                selected = currentPageIndex == index,
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
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                    selectedLabelColor = MaterialTheme.colorScheme.onSecondaryContainer,
                ),
            )
        }
    }
}
