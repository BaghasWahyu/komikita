package eu.kanade.presentation.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import eu.kanade.presentation.theme.LocalGlassUiEnabled
import mihon.presentation.core.util.isTabletUi
import tachiyomi.presentation.core.components.AdaptiveSheet as AdaptiveSheetImpl

/**
 * Sheet with adaptive position aligned to bottom on small screen, otherwise aligned to center
 * and will not be able to dismissed with swipe gesture.
 *
 * Max width of the content is set to 460 dp.
 */
@Composable
fun AdaptiveSheet(
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    enableImplicitDismiss: Boolean = true,
    useGlassUi: Boolean = LocalGlassUiEnabled.current,
    content: @Composable () -> Unit,
) {
    val isTabletUi = isTabletUi()
    val colors = MaterialTheme.colorScheme
    val sheetColor = colors.surfaceContainerHigh

    Dialog(
        onDismissRequest = onDismissRequest,
        properties = if (useGlassUi) glassDialogProperties else dialogProperties,
    ) {
        AdaptiveSheetImpl(
            isTabletUi = isTabletUi,
            enableImplicitDismiss = enableImplicitDismiss,
            onDismissRequest = onDismissRequest,
            modifier = modifier,
            containerColor = if (useGlassUi) sheetColor.copy(alpha = 0.90f) else sheetColor,
            contentColor = colors.onSurface,
            border = if (useGlassUi) BorderStroke(1.dp, colors.outlineVariant) else null,
        ) {
            content()
        }
    }
}

private val dialogProperties = DialogProperties(
    usePlatformDefaultWidth = false,
    decorFitsSystemWindows = true,
    blurBehindRadius = 0.dp,
)

private val glassDialogProperties = DialogProperties(
    usePlatformDefaultWidth = false,
    decorFitsSystemWindows = true,
    blurBehindRadius = 8.dp,
)
