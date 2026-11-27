package tachiyomi.presentation.core.components.material

import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.FloatingActionButtonElevation
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import tachiyomi.presentation.core.theme.LocalGlassUiEnabled
import tachiyomi.presentation.core.theme.glassBorder
import androidx.compose.material3.SmallExtendedFloatingActionButton as MaterialSmallExtendedFloatingActionButton

@Composable
fun SmallExtendedFloatingActionButton(
    text: @Composable () -> Unit,
    icon: @Composable () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    expanded: Boolean = true,
    shape: Shape = FloatingActionButtonDefaults.smallExtendedFabShape,
    containerColor: Color = FloatingActionButtonDefaults.containerColor,
    contentColor: Color = contentColorFor(containerColor),
    elevation: FloatingActionButtonElevation = FloatingActionButtonDefaults.elevation(),
    interactionSource: MutableInteractionSource? = null,
) {
    val useGlassUi = LocalGlassUiEnabled.current
    MaterialSmallExtendedFloatingActionButton(
        text = text,
        icon = icon,
        onClick = onClick,
        modifier = if (useGlassUi) {
            modifier.border(glassBorder(MaterialTheme.colorScheme.outlineVariant), shape)
        } else {
            modifier
        },
        expanded = expanded,
        shape = shape,
        containerColor = if (useGlassUi) containerColor.copy(alpha = 0.88f) else containerColor,
        contentColor = contentColor,
        elevation = elevation,
        interactionSource = interactionSource,
    )
}
