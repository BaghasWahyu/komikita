package tachiyomi.presentation.core.components.material

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import tachiyomi.presentation.core.theme.LocalGlassUiEnabled
import tachiyomi.presentation.core.theme.glassBorder
import tachiyomi.presentation.core.theme.glassSurfaceColor
import androidx.compose.material3.AlertDialog as MaterialAlertDialog

@Composable
fun AlertDialog(
    onDismissRequest: () -> Unit,
    confirmButton: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    dismissButton: @Composable (() -> Unit)? = null,
    icon: @Composable (() -> Unit)? = null,
    title: @Composable (() -> Unit)? = null,
    text: @Composable (() -> Unit)? = null,
    shape: Shape = AlertDialogDefaults.shape,
    containerColor: Color = AlertDialogDefaults.containerColor,
    iconContentColor: Color = AlertDialogDefaults.iconContentColor,
    titleContentColor: Color = AlertDialogDefaults.titleContentColor,
    textContentColor: Color = AlertDialogDefaults.textContentColor,
    tonalElevation: Dp = AlertDialogDefaults.TonalElevation,
    properties: DialogProperties = DialogProperties(),
) {
    val useGlassUi = LocalGlassUiEnabled.current
    val effectiveProperties = if (useGlassUi && properties.blurBehindRadius == 0.dp) {
        DialogProperties(
            dismissOnBackPress = properties.dismissOnBackPress,
            dismissOnClickOutside = properties.dismissOnClickOutside,
            securePolicy = properties.securePolicy,
            usePlatformDefaultWidth = properties.usePlatformDefaultWidth,
            decorFitsSystemWindows = properties.decorFitsSystemWindows,
            blurBehindRadius = 8.dp,
        )
    } else {
        properties
    }
    MaterialAlertDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = confirmButton,
        modifier = if (useGlassUi) modifier.border(glassBorder(), shape) else modifier,
        dismissButton = dismissButton,
        icon = icon,
        title = title,
        text = text,
        shape = shape,
        containerColor = if (useGlassUi) glassSurfaceColor(alpha = 0.90f) else containerColor,
        iconContentColor = iconContentColor,
        titleContentColor = titleContentColor,
        textContentColor = textContentColor,
        tonalElevation = if (useGlassUi) 0.dp else tonalElevation,
        properties = effectiveProperties,
    )
}

@Composable
fun AlertDialogContent(
    buttons: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    icon: (@Composable () -> Unit)? = null,
    title: (@Composable () -> Unit)? = null,
    text: @Composable () -> Unit,
) {
    AlertDialogContent(
        modifier = modifier,
        icon = icon,
        title = title,
        content = {
            Column {
                CompositionLocalProvider(
                    LocalContentColor provides MaterialTheme.colorScheme.onSurfaceVariant,
                ) {
                    val textStyle = MaterialTheme.typography.bodyMedium
                    ProvideTextStyle(textStyle) {
                        Box(
                            Modifier
                                .weight(weight = 1f, fill = false)
                                .padding(horizontal = DialogPadding)
                                .padding(TextPadding)
                                .align(Alignment.Start),
                        ) {
                            text()
                        }
                    }
                }

                Box(
                    modifier = Modifier
                        .padding(
                            start = DialogPadding,
                            end = DialogPadding,
                            bottom = DialogPadding,
                        )
                        .align(Alignment.End),
                ) {
                    CompositionLocalProvider(
                        LocalContentColor provides MaterialTheme.colorScheme.primary,
                    ) {
                        val textStyle = MaterialTheme.typography.labelLarge
                        ProvideTextStyle(value = textStyle, content = buttons)
                    }
                }
            }
        },
    )
}

@Composable
fun AlertDialogContent(
    modifier: Modifier = Modifier,
    icon: (@Composable () -> Unit)? = null,
    title: (@Composable () -> Unit)? = null,
    content: @Composable (ColumnScope.() -> Unit)? = null,
) {
    Column(
        modifier = modifier
            .sizeIn(minWidth = MinWidth, maxWidth = MaxWidth),
    ) {
        if (icon != null || title != null) {
            Column(
                modifier = Modifier
                    .padding(
                        start = DialogPadding,
                        top = DialogPadding,
                        end = DialogPadding,
                    )
                    .fillMaxWidth(),
            ) {
                icon?.let {
                    CompositionLocalProvider(
                        LocalContentColor provides MaterialTheme.colorScheme.secondary,
                    ) {
                        Box(
                            Modifier
                                .padding(IconPadding)
                                .align(Alignment.CenterHorizontally),
                        ) {
                            icon()
                        }
                    }
                }
                title?.let {
                    CompositionLocalProvider(
                        LocalContentColor provides MaterialTheme.colorScheme.onSurface,
                    ) {
                        val textStyle = MaterialTheme.typography.headlineSmall
                        ProvideTextStyle(textStyle) {
                            Box(
                                // Align the title to the center when an icon is present.
                                Modifier
                                    .padding(TitlePadding)
                                    .align(
                                        if (icon == null) {
                                            Alignment.Start
                                        } else {
                                            Alignment.CenterHorizontally
                                        },
                                    ),
                            ) {
                                title()
                            }
                        }
                    }
                }
            }
        }
        content?.invoke(this)
    }
}

// Paddings for each of the dialog's parts.
private val DialogPadding = 24.dp
private val IconPadding = PaddingValues(bottom = 16.dp)
private val TitlePadding = PaddingValues(bottom = 16.dp)
private val TextPadding = PaddingValues(bottom = 24.dp)

private val MinWidth = 280.dp
private val MaxWidth = 560.dp
