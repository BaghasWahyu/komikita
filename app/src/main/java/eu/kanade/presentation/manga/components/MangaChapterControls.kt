package eu.kanade.presentation.manga.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import mihon.icons.materialsymbols.MaterialSymbols
import mihon.icons.materialsymbols.rounded.Close
import mihon.icons.materialsymbols.rounded.Search
import tachiyomi.i18n.MR
import tachiyomi.presentation.core.i18n.stringResource

@Composable
fun MangaDetailTabs(
    informationSelected: Boolean,
    enabled: Boolean,
    onSelectInformation: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    SingleChoiceSegmentedButtonRow(
        modifier = modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
    ) {
        listOf(MR.strings.chapters, MR.strings.komikita_manga_information).forEachIndexed { index, label ->
            SegmentedButton(
                selected = informationSelected == (index == 1),
                onClick = { onSelectInformation(index == 1) },
                enabled = enabled,
                shape = SegmentedButtonDefaults.itemShape(index = index, count = 2),
                icon = {},
                label = { Text(stringResource(label)) },
            )
        }
    }
}

@Composable
fun MangaChapterControls(
    query: String,
    onQueryChange: (String) -> Unit,
    quickFilter: ChapterQuickFilter,
    onQuickFilterChange: (ChapterQuickFilter) -> Unit,
    enabled: Boolean,
    modifier: Modifier = Modifier,
) {
    val focusManager = LocalFocusManager.current
    Column(modifier = modifier) {
        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            enabled = enabled,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            placeholder = { Text(stringResource(MR.strings.komikita_search_chapters)) },
            leadingIcon = { Icon(MaterialSymbols.Rounded.Search, contentDescription = null) },
            trailingIcon = if (query.isNotEmpty()) {
                {
                    IconButton(onClick = { onQueryChange("") }, enabled = enabled) {
                        Icon(MaterialSymbols.Rounded.Close, stringResource(MR.strings.komikita_clear_search))
                    }
                }
            } else {
                null
            },
            shape = MaterialTheme.shapes.extraLarge,
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() }),
        )
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(ChapterQuickFilter.entries) { filter ->
                FilterChip(
                    selected = quickFilter == filter,
                    enabled = enabled,
                    onClick = { onQuickFilterChange(filter) },
                    label = {
                        Text(
                            stringResource(
                                when (filter) {
                                    ChapterQuickFilter.ALL -> MR.strings.all
                                    ChapterQuickFilter.UNREAD -> MR.strings.action_filter_unread
                                    ChapterQuickFilter.DOWNLOADED -> MR.strings.label_downloaded
                                },
                            ),
                        )
                    },
                )
            }
        }
    }
}
