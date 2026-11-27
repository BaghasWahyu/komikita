package eu.kanade.presentation.reader

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import eu.kanade.presentation.theme.LocalGlassUiEnabled

@Composable
internal fun readerControlSurfaceColor(): Color {
    val opacity = when {
        LocalGlassUiEnabled.current -> 0.84f
        isSystemInDarkTheme() -> 0.90f
        else -> 0.95f
    }
    return MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp).copy(alpha = opacity)
}
