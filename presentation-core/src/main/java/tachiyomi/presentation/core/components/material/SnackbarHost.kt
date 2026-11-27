package tachiyomi.presentation.core.components.material

import androidx.compose.foundation.border
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarData
import androidx.compose.material3.SnackbarDefaults
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import tachiyomi.presentation.core.theme.LocalGlassUiEnabled
import tachiyomi.presentation.core.theme.glassBorder
import tachiyomi.presentation.core.theme.glassSurfaceColor
import androidx.compose.material3.SnackbarHost as MaterialSnackbarHost

@Composable
fun SnackbarHost(
    hostState: SnackbarHostState,
    modifier: Modifier = Modifier,
    snackbar: @Composable (SnackbarData) -> Unit = { data ->
        if (LocalGlassUiEnabled.current) {
            val colors = MaterialTheme.colorScheme
            Snackbar(
                snackbarData = data,
                modifier = Modifier.border(glassBorder(), SnackbarDefaults.shape),
                containerColor = glassSurfaceColor(alpha = 0.90f),
                contentColor = colors.onSurface,
                actionColor = colors.primary,
                actionContentColor = colors.primary,
                dismissActionContentColor = colors.onSurfaceVariant,
            )
        } else {
            Snackbar(data)
        }
    },
) {
    MaterialSnackbarHost(
        hostState = hostState,
        modifier = modifier,
        snackbar = snackbar,
    )
}
