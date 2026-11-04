package eu.kanade.presentation.library.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconToggleButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import eu.kanade.presentation.manga.components.MangaCover
import eu.kanade.tachiyomi.ui.library.LibraryItem
import mihon.icons.materialsymbols.MaterialSymbols
import mihon.icons.materialsymbols.automirroredrounded.ArrowForward
import mihon.icons.materialsymbols.automirroredroundedfilled.ViewList
import mihon.icons.materialsymbols.roundedfilled.ViewModule
import tachiyomi.domain.library.model.LibraryDisplayMode
import tachiyomi.domain.library.model.LibraryManga
import tachiyomi.domain.library.model.getContinueReadingManga
import tachiyomi.i18n.MR
import tachiyomi.i18n.komikita_continue_reading
import tachiyomi.i18n.komikita_grid_view
import tachiyomi.i18n.komikita_list_view
import tachiyomi.i18n.komikita_reading_progress
import tachiyomi.i18n.komikita_your_collection
import tachiyomi.presentation.core.i18n.stringResource

@Composable
internal fun LibraryCollectionHeader(
    items: List<LibraryItem>,
    selectionMode: Boolean,
    showContinueReading: Boolean,
    displayMode: LibraryDisplayMode,
    onDisplayModeChange: (LibraryDisplayMode) -> Unit,
    onContinueReading: (LibraryManga) -> Unit,
    categories: @Composable () -> Unit,
) {
    // Only inspect this page's visible, filtered items, including for hidden categories.
    val continueReading = remember(items) {
        items.asSequence()
            .map { it.libraryManga }
            .getContinueReadingManga()
    }

    Column(
        modifier = Modifier.fillMaxWidth().padding(top = 8.dp, bottom = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        if (!selectionMode && showContinueReading && continueReading != null) {
            ContinueReadingCard(continueReading, onClick = { onContinueReading(continueReading) })
        }
        if (!selectionMode) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = stringResource(MR.strings.komikita_your_collection),
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = items.size.toString(),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                LibraryViewToggle(
                    selected = displayMode != LibraryDisplayMode.List,
                    onClick = { onDisplayModeChange(LibraryDisplayMode.ComfortableGrid) },
                    label = stringResource(MR.strings.komikita_grid_view),
                ) { Icon(MaterialSymbols.RoundedFilled.ViewModule, contentDescription = null) }
                LibraryViewToggle(
                    selected = displayMode == LibraryDisplayMode.List,
                    onClick = { onDisplayModeChange(LibraryDisplayMode.List) },
                    label = stringResource(MR.strings.komikita_list_view),
                ) { Icon(MaterialSymbols.AutoMirroredRoundedFilled.ViewList, contentDescription = null) }
            }
        }
        categories()
    }
}

@Composable
private fun LibraryViewToggle(
    selected: Boolean,
    onClick: () -> Unit,
    label: String,
    icon: @Composable () -> Unit,
) {
    IconToggleButton(
        checked = selected,
        onCheckedChange = { onClick() },
        modifier = Modifier
            .size(48.dp)
            .background(
                color = if (selected) MaterialTheme.colorScheme.secondaryContainer else Color.Transparent,
                shape = MaterialTheme.shapes.medium,
            )
            .semantics { contentDescription = label },
    ) {
        CompositionLocalProvider(
            LocalContentColor provides if (selected) {
                MaterialTheme.colorScheme.onSecondaryContainer
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            },
        ) {
            icon()
        }
    }
}

@Composable
private fun ContinueReadingCard(
    manga: LibraryManga,
    onClick: () -> Unit,
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        ),
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            MangaCover.Book(
                data = manga.manga,
                modifier = Modifier.height(88.dp),
                shape = MaterialTheme.shapes.small,
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text(
                    text = stringResource(MR.strings.komikita_continue_reading),
                    style = MaterialTheme.typography.labelMedium,
                )
                Text(
                    text = manga.manga.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = stringResource(MR.strings.komikita_reading_progress, manga.readCount, manga.totalChapters),
                    style = MaterialTheme.typography.labelSmall,
                )
                LinearProgressIndicator(
                    progress = { manga.readingProgress },
                    modifier = Modifier.fillMaxWidth().height(4.dp).clip(MaterialTheme.shapes.small),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.12f),
                )
            }
            Icon(MaterialSymbols.AutoMirroredRounded.ArrowForward, contentDescription = null)
        }
    }
}

internal val LibraryManga.readingProgress: Float
    get() = if (totalChapters > 0) (readCount.toFloat() / totalChapters).coerceIn(0f, 1f) else 0f
