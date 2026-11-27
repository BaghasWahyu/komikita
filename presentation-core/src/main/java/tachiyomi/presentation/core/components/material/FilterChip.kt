package tachiyomi.presentation.core.components.material

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.SelectableChipColors
import androidx.compose.material3.SelectableChipElevation
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import tachiyomi.presentation.core.theme.LocalGlassUiEnabled
import tachiyomi.presentation.core.theme.glassFilterChipBorder
import tachiyomi.presentation.core.theme.glassFilterChipColors
import androidx.compose.material3.FilterChip as MaterialFilterChip

@Composable
fun FilterChip(
    selected: Boolean,
    onClick: () -> Unit,
    label: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    shape: Shape = FilterChipDefaults.shape,
    colors: SelectableChipColors = if (LocalGlassUiEnabled.current) {
        glassFilterChipColors()
    } else {
        FilterChipDefaults.filterChipColors()
    },
    elevation: SelectableChipElevation? = FilterChipDefaults.filterChipElevation(),
    border: BorderStroke? = if (LocalGlassUiEnabled.current) {
        glassFilterChipBorder(enabled = enabled, selected = selected)
    } else {
        FilterChipDefaults.filterChipBorder(enabled = enabled, selected = selected)
    },
    interactionSource: MutableInteractionSource? = null,
) {
    MaterialFilterChip(
        selected = selected,
        onClick = onClick,
        label = label,
        modifier = modifier,
        enabled = enabled,
        leadingIcon = leadingIcon,
        trailingIcon = trailingIcon,
        shape = shape,
        colors = colors,
        elevation = elevation,
        border = border,
        interactionSource = interactionSource,
    )
}
