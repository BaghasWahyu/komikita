package tachiyomi.presentation.core.components

import android.view.ViewConfiguration
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsDraggedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.systemGestureExclusion
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastFirstOrNull
import androidx.compose.ui.util.fastForEach
import androidx.compose.ui.util.fastLastOrNull
import androidx.compose.ui.util.fastMaxBy
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlin.time.Duration.Companion.seconds

/** The position is estimated from visible item sizes, including lists with sticky headers. */
@Composable
fun VerticalFastScroller(
    listState: LazyListState,
    modifier: Modifier = Modifier,
    thumbAllowed: () -> Boolean = { true },
    thumbColor: Color = MaterialTheme.colorScheme.primary,
    topContentPadding: Dp = Dp.Hairline,
    bottomContentPadding: Dp = Dp.Hairline,
    endContentPadding: Dp = Dp.Hairline,
    content: @Composable () -> Unit,
) {
    FastScrollerLayout(modifier, content) { height, _ ->
        val top = with(LocalDensity.current) { topContentPadding.toPx() }
        val bottom = with(LocalDensity.current) { bottomContentPadding.toPx() }
        val thumb = with(LocalDensity.current) { ThumbLength.toPx() }
        val metrics = remember(listState, height, top, bottom, thumb) {
            derivedStateOf {
                val layout = listState.layoutInfo
                val viewport = (height - top - bottom - layout.afterContentPadding).coerceAtLeast(0f)
                val first = layout.visibleItemsInfo.fastFirstOrNull {
                    (it.key as? String)?.startsWith(Scroller.STICKY_HEADER_KEY_PREFIX) != true &&
                        it.offset + it.size > 0
                }
                val last = layout.visibleItemsInfo.fastLastOrNull {
                    (it.key as? String)?.startsWith(Scroller.STICKY_HEADER_KEY_PREFIX) != true && it.offset < viewport
                }
                if (first == null || last == null) {
                    ScrollMetrics()
                } else {
                    val before = first.index - first.offset.toFloat() / first.size.coerceAtLeast(1)
                    val remaining = layout.totalItemsCount - last.index - 1 +
                        (last.offset + last.size - viewport) / last.size.coerceAtLeast(1)
                    ScrollMetrics(
                        position = before,
                        range = (before + remaining).coerceAtLeast(0f),
                        itemSize = first.size.toFloat().coerceAtLeast(1f),
                        totalItems = layout.totalItemsCount,
                        trackHeight = (viewport - thumb).coerceAtLeast(0f),
                    )
                }
            }
        }
        FastScrollThumb(
            metrics = { metrics.value },
            isScrolling = listState.isScrollInProgress,
            thumbAllowed = thumbAllowed,
            thumbColor = thumbColor,
            top = top,
            endContentPadding = endContentPadding,
            horizontalPadding = 8.dp,
        ) { proportion ->
            val value = metrics.value
            if (value.totalItems > 0) {
                val section = proportion * value.range
                val index = section.toInt().coerceIn(0, value.totalItems - 1)
                listState.scrollToItem(index, ((section - index) * value.itemSize).roundToInt())
            }
        }
    }
}

/** A grid uses the visible rows to estimate the height of the remaining rows. */
@Composable
fun VerticalGridFastScroller(
    state: LazyGridState,
    columns: GridCells,
    arrangement: Arrangement.Horizontal,
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier,
    thumbAllowed: () -> Boolean = { true },
    thumbColor: Color = MaterialTheme.colorScheme.primary,
    topContentPadding: Dp = Dp.Hairline,
    bottomContentPadding: Dp = Dp.Hairline,
    endContentPadding: Dp = Dp.Hairline,
    content: @Composable () -> Unit,
) {
    val density = LocalDensity.current
    FastScrollerLayout(modifier, content) { height, width ->
        val top = with(density) { topContentPadding.toPx() }
        val bottom = with(density) { bottomContentPadding.toPx() }
        val thumb = with(density) { ThumbLength.toPx() }
        val columnCount = remember(columns, arrangement, contentPadding, density, width) {
            val horizontalPadding = contentPadding.calculateStartPadding(LayoutDirection.Ltr) +
                contentPadding.calculateEndPadding(LayoutDirection.Ltr)
            with(density) {
                with(columns) {
                    calculateCrossAxisCellSizes(
                        (width - horizontalPadding.roundToPx()).coerceAtLeast(0),
                        arrangement.spacing.roundToPx(),
                    ).size.coerceAtLeast(1)
                }
            }
        }
        val metrics = remember(state, columnCount, height, top, bottom, thumb) {
            derivedStateOf {
                val layout = state.layoutInfo
                val visible = layout.visibleItemsInfo
                val first = visible.firstOrNull()
                val last = visible.lastOrNull()
                if (first == null || last == null) {
                    ScrollMetrics()
                } else {
                    val rows = 1 + abs(last.index - first.index) / columnCount
                    val area = last.offset.y + last.size.height - first.offset.y
                    val rowSize = (area.toFloat() / rows).coerceAtLeast(1f)
                    val totalRows = (layout.totalItemsCount + columnCount - 1) / columnCount
                    val viewport = (height - top - bottom - layout.afterContentPadding).coerceAtLeast(0f)
                    ScrollMetrics(
                        position = first.index / columnCount * rowSize - first.offset.y,
                        range = (rowSize * totalRows - viewport).coerceAtLeast(0f),
                        itemSize = rowSize,
                        totalItems = layout.totalItemsCount,
                        columns = columnCount,
                        trackHeight = (viewport - thumb).coerceAtLeast(0f),
                    )
                }
            }
        }
        FastScrollThumb(
            metrics = { metrics.value },
            isScrolling = state.isScrollInProgress,
            thumbAllowed = thumbAllowed,
            thumbColor = thumbColor,
            top = top,
            endContentPadding = endContentPadding,
        ) { proportion ->
            val value = metrics.value
            if (value.totalItems > 0) {
                val offset = proportion * value.range
                val row = (offset / value.itemSize).toInt()
                state.scrollToItem(
                    (row * value.columns).coerceIn(0, value.totalItems - 1),
                    (offset - row * value.itemSize).roundToInt(),
                )
            }
        }
    }
}

private data class ScrollMetrics(
    val position: Float = 0f,
    val range: Float = 0f,
    val itemSize: Float = 1f,
    val totalItems: Int = 0,
    val columns: Int = 1,
    val trackHeight: Float = 0f,
) {
    val proportion: Float get() = if (range > 0f) (position / range).coerceIn(0f, 1f) else 0f
    val scrollable: Boolean get() = range > 0.5f && trackHeight > 0f
}

@Composable
private fun FastScrollerLayout(
    modifier: Modifier,
    content: @Composable () -> Unit,
    scroller: @Composable (height: Int, width: Int) -> Unit,
) {
    SubcomposeLayout(modifier) { constraints ->
        val placeables = subcompose("content", content).map { it.measure(constraints) }
        val height = placeables.fastMaxBy { it.height }?.height ?: 0
        val width = placeables.fastMaxBy { it.width }?.width ?: 0
        val thumbs = subcompose("scroller") { scroller(height, width) }
            .map { it.measure(constraints.copy(minWidth = 0, minHeight = 0)) }
        val thumbWidth = thumbs.fastMaxBy { it.width }?.width ?: 0
        layout(width, height) {
            placeables.fastForEach { it.place(0, 0) }
            thumbs.fastForEach { it.placeRelative(width - thumbWidth, 0) }
        }
    }
}

@Composable
private fun FastScrollThumb(
    metrics: () -> ScrollMetrics,
    isScrolling: Boolean,
    thumbAllowed: () -> Boolean,
    thumbColor: Color,
    top: Float,
    endContentPadding: Dp,
    horizontalPadding: Dp = Dp.Hairline,
    scrollTo: suspend (Float) -> Unit,
) {
    val currentMetrics by rememberUpdatedState(metrics)
    val currentScrollTo by rememberUpdatedState(scrollTo)
    val allowed by rememberUpdatedState(thumbAllowed)
    // Only visibility changes invalidate composition; pixel offsets are read during placement.
    val scrollable by remember { derivedStateOf { currentMetrics().scrollable } }
    if (!scrollable) return

    var draggedOffset by remember { mutableFloatStateOf(0f) }
    val interactionSource = remember { MutableInteractionSource() }
    val dragged by interactionSource.collectIsDraggedAsState()
    val alpha = remember { Animatable(0f) }
    val visible by remember { derivedStateOf { alpha.value > 0f } }

    LaunchedEffect(isScrolling, dragged) {
        if ((isScrolling || dragged) && allowed()) {
            alpha.snapTo(1f)
        } else {
            delay(2.seconds)
            alpha.animateTo(0f, tween(ViewConfiguration.getScrollBarFadeDuration()))
        }
    }
    LaunchedEffect(dragged) {
        if (dragged) {
            snapshotFlow { draggedOffset }.collectLatest { offset ->
                val track = currentMetrics().trackHeight
                if (track > 0f) currentScrollTo(((offset - top) / track).coerceIn(0f, 1f))
            }
        }
    }

    Box(
        Modifier
            .offset {
                val offset = if (dragged) {
                    draggedOffset
                } else {
                    top +
                        currentMetrics().proportion * currentMetrics().trackHeight
                }
                IntOffset(0, offset.roundToInt())
            }
            .draggable(
                enabled = visible && (!isScrolling || dragged),
                interactionSource = interactionSource,
                orientation = Orientation.Vertical,
                onDragStarted = { draggedOffset = top + currentMetrics().proportion * currentMetrics().trackHeight },
                state = rememberDraggableState { delta ->
                    draggedOffset = (draggedOffset + delta).coerceIn(top, top + currentMetrics().trackHeight)
                },
            )
            .then(if (visible && !dragged && !isScrolling) Modifier.systemGestureExclusion() else Modifier)
            .height(ThumbLength)
            .padding(horizontal = horizontalPadding)
            .padding(end = endContentPadding)
            .width(ThumbThickness)
            .graphicsLayer { this.alpha = alpha.value }
            .background(thumbColor, RoundedCornerShape(ThumbThickness / 2)),
    )
}

object Scroller {
    const val STICKY_HEADER_KEY_PREFIX = "sticky:"
}

private val ThumbLength = 48.dp
private val ThumbThickness = 12.dp
