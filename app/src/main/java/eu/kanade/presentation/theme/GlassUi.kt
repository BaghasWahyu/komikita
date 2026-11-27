package eu.kanade.presentation.theme

import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import tachiyomi.presentation.core.theme.glassEdge as coreGlassEdge

internal val LocalGlassUiEnabled
    get() = tachiyomi.presentation.core.theme.LocalGlassUiEnabled

internal fun Modifier.glassEdge(color: Color, atTop: Boolean): Modifier = coreGlassEdge(color, atTop)
