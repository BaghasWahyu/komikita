package tachiyomi.presentation.core.theme

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SelectableChipColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

val LocalGlassUiEnabled = compositionLocalOf { false }

val GlassCoverShape = RoundedCornerShape(12.dp)
val GlassCardShape = RoundedCornerShape(20.dp)
val GlassDockShape = RoundedCornerShape(28.dp)

fun Modifier.glassEdge(color: Color, atTop: Boolean): Modifier = drawWithContent {
    drawContent()
    val width = 1.dp.toPx()
    val edge = if (atTop) width / 2 else size.height - width / 2
    drawLine(color, Offset(0f, edge), Offset(size.width, edge), strokeWidth = width)
}

@Composable
fun glassSurfaceColor(
    baseColor: Color = MaterialTheme.colorScheme.surfaceContainerHigh,
    alpha: Float = 0.84f,
): Color = baseColor.copy(alpha = alpha)

@Composable
fun glassBorder(
    color: Color = MaterialTheme.colorScheme.outlineVariant,
): BorderStroke = BorderStroke(1.dp, color)

@Composable
fun glassCardColors(
    containerColor: Color = MaterialTheme.colorScheme.surfaceContainerHigh,
    contentColor: Color = MaterialTheme.colorScheme.onSurface,
    alpha: Float = 0.78f,
): CardColors = CardDefaults.cardColors(
    containerColor = containerColor.copy(alpha = alpha),
    contentColor = contentColor,
)

@Composable
fun glassFilterChipColors(
    selectedContainerColor: Color = MaterialTheme.colorScheme.secondaryContainer,
    selectedLabelColor: Color = MaterialTheme.colorScheme.onSecondaryContainer,
): SelectableChipColors {
    val colors = MaterialTheme.colorScheme
    return FilterChipDefaults.filterChipColors(
        containerColor = colors.surfaceContainerHigh.copy(alpha = 0.68f),
        labelColor = colors.onSurface,
        iconColor = colors.onSurfaceVariant,
        selectedContainerColor = selectedContainerColor.copy(alpha = 0.82f),
        selectedLabelColor = selectedLabelColor,
        selectedLeadingIconColor = selectedLabelColor,
    )
}

@Composable
fun glassFilterChipBorder(
    enabled: Boolean,
    selected: Boolean,
): BorderStroke = FilterChipDefaults.filterChipBorder(
    enabled = enabled,
    selected = selected,
    borderColor = MaterialTheme.colorScheme.outlineVariant,
    selectedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.6f),
    selectedBorderWidth = 1.dp,
)
