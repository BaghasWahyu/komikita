package eu.kanade.presentation.library.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import eu.kanade.presentation.components.AppBar
import eu.kanade.presentation.components.AppBarActions
import eu.kanade.presentation.theme.TachiyomiPreviewTheme
import eu.kanade.tachiyomi.data.library.LibraryUpdateProgress
import mihon.icons.materialsymbols.MaterialSymbols
import mihon.icons.materialsymbols.rounded.Close
import mihon.icons.materialsymbols.rounded.Download
import mihon.icons.materialsymbols.rounded.FilterList
import mihon.icons.materialsymbols.rounded.FlipToBack
import mihon.icons.materialsymbols.rounded.Search
import mihon.icons.materialsymbols.rounded.SelectAll
import tachiyomi.i18n.MR
import tachiyomi.i18n.action_filter
import tachiyomi.i18n.action_open_random_manga
import tachiyomi.i18n.action_select_all
import tachiyomi.i18n.action_select_inverse
import tachiyomi.i18n.action_update_category
import tachiyomi.i18n.action_update_library
import tachiyomi.i18n.komikita_clear_search
import tachiyomi.i18n.komikita_library_tagline
import tachiyomi.i18n.komikita_library_update_progress
import tachiyomi.i18n.komikita_search_library
import tachiyomi.i18n.label_download_queue
import tachiyomi.i18n.updating_category
import tachiyomi.i18n.updating_library
import tachiyomi.presentation.core.i18n.stringResource

@Composable
fun LibraryToolbar(
    hasActiveFilters: Boolean,
    selectedCount: Int,
    title: LibraryToolbarTitle,
    onClickUnselectAll: () -> Unit,
    onClickSelectAll: () -> Unit,
    onClickInvertSelection: () -> Unit,
    onClickFilter: () -> Unit,
    onClickDownloads: () -> Unit,
    onClickRefresh: () -> Unit,
    onClickGlobalUpdate: () -> Unit,
    onClickOpenRandomManga: () -> Unit,
    searchQuery: String?,
    onSearchQueryChange: (String?) -> Unit,
    scrollBehavior: TopAppBarScrollBehavior?,
    updateProgress: LibraryUpdateProgress? = null,
) {
    Column {
        when {
            selectedCount > 0 -> LibrarySelectionToolbar(
                selectedCount = selectedCount,
                onClickUnselectAll = onClickUnselectAll,
                onClickSelectAll = onClickSelectAll,
                onClickInvertSelection = onClickInvertSelection,
            )
            else -> LibraryRegularToolbar(
                title = title,
                hasFilters = hasActiveFilters,
                searchQuery = searchQuery,
                onSearchQueryChange = onSearchQueryChange,
                onClickFilter = onClickFilter,
                onClickDownloads = onClickDownloads,
                onClickRefresh = onClickRefresh,
                onClickGlobalUpdate = onClickGlobalUpdate,
                onClickOpenRandomManga = onClickOpenRandomManga,
                scrollBehavior = scrollBehavior,
            )
        }
        if (updateProgress != null) {
            LibraryUpdateProgressIndicator(updateProgress)
        }
    }
}

@Composable
private fun LibraryUpdateProgressIndicator(progress: LibraryUpdateProgress) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, bottom = 12.dp)
            .semantics(mergeDescendants = true) {},
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(
                    if (progress.isCategoryUpdate) MR.strings.updating_category else MR.strings.updating_library,
                ),
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (progress.total > 0) {
                Text(
                    text = stringResource(
                        MR.strings.komikita_library_update_progress,
                        progress.completed,
                        progress.total,
                        (progress.completed.toLong() * 100 / progress.total).toInt(),
                    ),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        if (progress.total > 0) {
            LinearProgressIndicator(
                progress = { (progress.completed.toFloat() / progress.total).coerceIn(0f, 1f) },
                modifier = Modifier.fillMaxWidth(),
            )
        } else {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
        }
    }
}

@Composable
private fun LibraryRegularToolbar(
    title: LibraryToolbarTitle,
    hasFilters: Boolean,
    searchQuery: String?,
    onSearchQueryChange: (String?) -> Unit,
    onClickFilter: () -> Unit,
    onClickDownloads: () -> Unit,
    onClickRefresh: () -> Unit,
    onClickGlobalUpdate: () -> Unit,
    onClickOpenRandomManga: () -> Unit,
    scrollBehavior: TopAppBarScrollBehavior?,
) {
    val focusManager = LocalFocusManager.current
    Column {
        AppBar(
            titleContent = {
                Column {
                    Text(
                        text = title.text,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = stringResource(MR.strings.komikita_library_tagline),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            },
            actions = {
                AppBarActions(
                    listOf(
                        AppBar.Action(
                            title = stringResource(MR.strings.label_download_queue),
                            icon = MaterialSymbols.Rounded.Download,
                            onClick = onClickDownloads,
                        ),
                        AppBar.OverflowAction(
                            title = stringResource(MR.strings.action_update_library),
                            onClick = onClickGlobalUpdate,
                        ),
                        AppBar.OverflowAction(
                            title = stringResource(MR.strings.action_update_category),
                            onClick = onClickRefresh,
                        ),
                        AppBar.OverflowAction(
                            title = stringResource(MR.strings.action_open_random_manga),
                            onClick = onClickOpenRandomManga,
                        ),
                    ),
                )
            },
            scrollBehavior = scrollBehavior,
        )
        Row(
            modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, bottom = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            OutlinedTextField(
                value = searchQuery.orEmpty(),
                onValueChange = { onSearchQueryChange(it.takeIf(String::isNotEmpty)) },
                modifier = Modifier.weight(1f),
                placeholder = { Text(stringResource(MR.strings.komikita_search_library)) },
                leadingIcon = { Icon(MaterialSymbols.Rounded.Search, contentDescription = null) },
                trailingIcon = {
                    if (!searchQuery.isNullOrEmpty()) {
                        IconButton(onClick = { onSearchQueryChange(null) }) {
                            Icon(MaterialSymbols.Rounded.Close, stringResource(MR.strings.komikita_clear_search))
                        }
                    }
                },
                singleLine = true,
                shape = MaterialTheme.shapes.medium,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() }),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                ),
            )
            FilledTonalIconButton(
                onClick = onClickFilter,
                modifier = Modifier.size(56.dp),
                shape = MaterialTheme.shapes.medium,
                colors = IconButtonDefaults.filledTonalIconButtonColors(
                    containerColor = if (hasFilters) {
                        MaterialTheme.colorScheme.primaryContainer
                    } else {
                        MaterialTheme.colorScheme.surfaceContainerHigh
                    },
                    contentColor = if (hasFilters) {
                        MaterialTheme.colorScheme.onPrimaryContainer
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                ),
            ) {
                Icon(MaterialSymbols.Rounded.FilterList, stringResource(MR.strings.action_filter))
            }
        }
    }
}

@Composable
private fun LibrarySelectionToolbar(
    selectedCount: Int,
    onClickUnselectAll: () -> Unit,
    onClickSelectAll: () -> Unit,
    onClickInvertSelection: () -> Unit,
) {
    AppBar(
        titleContent = { Text(text = "$selectedCount") },
        actions = {
            AppBarActions(
                listOf(
                    AppBar.Action(
                        title = stringResource(MR.strings.action_select_all),
                        icon = MaterialSymbols.Rounded.SelectAll,
                        onClick = onClickSelectAll,
                    ),
                    AppBar.Action(
                        title = stringResource(MR.strings.action_select_inverse),
                        icon = MaterialSymbols.Rounded.FlipToBack,
                        onClick = onClickInvertSelection,
                    ),
                ),
            )
        },
        isActionMode = true,
        onCancelActionMode = onClickUnselectAll,
    )
}

@Immutable
data class LibraryToolbarTitle(
    val text: String,
    val numberOfManga: Int? = null,
)

@PreviewLightDark
@Preview(name = "Indonesian", locale = "in")
@Composable
private fun LibraryToolbarUpdatePreview() {
    TachiyomiPreviewTheme {
        LibraryToolbar(
            hasActiveFilters = false,
            selectedCount = 0,
            title = LibraryToolbarTitle("Library"),
            onClickUnselectAll = {},
            onClickSelectAll = {},
            onClickInvertSelection = {},
            onClickFilter = {},
            onClickDownloads = {},
            onClickRefresh = {},
            onClickGlobalUpdate = {},
            onClickOpenRandomManga = {},
            searchQuery = null,
            onSearchQueryChange = {},
            scrollBehavior = null,
            updateProgress = LibraryUpdateProgress(completed = 12, total = 60, isCategoryUpdate = true),
        )
    }
}
