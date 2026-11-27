package eu.kanade.tachiyomi.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.result.ResultEffect
import eu.kanade.presentation.theme.LocalGlassUiEnabled
import eu.kanade.tachiyomi.ui.history.HistoryTab
import eu.kanade.tachiyomi.ui.updates.UpdatesTab
import tachiyomi.i18n.MR
import tachiyomi.i18n.label_recent_manga
import tachiyomi.i18n.label_recent_updates
import tachiyomi.presentation.core.i18n.stringResource
import tachiyomi.presentation.core.theme.glassBorder
import tachiyomi.presentation.core.theme.glassSurfaceColor

enum class ActivitySection {
    Updates,
    History,
}

data class ActivitySectionEvent(val section: ActivitySection)

@Composable
fun ActivityTab() {
    var section by rememberSaveable { mutableStateOf(ActivitySection.Updates) }

    ResultEffect<ActivitySectionEvent> {
        section = it.section
    }

    val switcher: @Composable () -> Unit = {
        ActivitySectionSwitcher(
            selected = section,
            onSelect = { section = it },
        )
    }

    when (section) {
        ActivitySection.Updates -> UpdatesTab(subHeader = switcher)
        ActivitySection.History -> HistoryTab(subHeader = switcher)
    }
}

@Composable
private fun ActivitySectionSwitcher(
    selected: ActivitySection,
    onSelect: (ActivitySection) -> Unit,
) {
    val useGlassUi = LocalGlassUiEnabled.current
    val outerShape = RoundedCornerShape(18.dp)
    val pillShape = RoundedCornerShape(14.dp)
    val containerColor = if (useGlassUi) {
        glassSurfaceColor(MaterialTheme.colorScheme.surfaceContainerHigh, alpha = 0.72f)
    } else {
        MaterialTheme.colorScheme.surfaceContainerHigh
    }
    val borderModifier = if (useGlassUi) {
        Modifier.border(glassBorder(), outerShape)
    } else {
        Modifier
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clip(outerShape)
            .background(containerColor)
            .then(borderModifier)
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        ActivitySegmentButton(
            label = stringResource(MR.strings.label_recent_updates),
            selected = selected == ActivitySection.Updates,
            shape = pillShape,
            onClick = { onSelect(ActivitySection.Updates) },
            modifier = Modifier.weight(1f),
        )
        ActivitySegmentButton(
            label = stringResource(MR.strings.label_recent_manga),
            selected = selected == ActivitySection.History,
            shape = pillShape,
            onClick = { onSelect(ActivitySection.History) },
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun ActivitySegmentButton(
    label: String,
    selected: Boolean,
    shape: RoundedCornerShape,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val backgroundColor = if (selected) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.surface.copy(alpha = 0f)
    }
    val textColor = if (selected) {
        MaterialTheme.colorScheme.onPrimary
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    Box(
        modifier = modifier
            .clip(shape)
            .background(backgroundColor)
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
            color = textColor,
            maxLines = 1,
        )
    }
}
