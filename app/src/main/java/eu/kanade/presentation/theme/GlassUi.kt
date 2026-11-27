package eu.kanade.presentation.theme

import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

internal val LocalGlassUiEnabled = compositionLocalOf { false }

internal fun Modifier.glassEdge(color: Color, atTop: Boolean): Modifier = drawWithContent {
    drawContent()
    val width = 1.dp.toPx()
    val edge = if (atTop) width / 2 else size.height - width / 2
    drawLine(color, Offset(0f, edge), Offset(size.width, edge), strokeWidth = width)
}
