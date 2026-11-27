package eu.kanade.tachiyomi.ui.home

import android.os.Build
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuite
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteDefaults
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteType
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.BlurEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.addOutline
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.layer.GraphicsLayer
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onPlaced
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import tachiyomi.presentation.core.components.material.LocalScaffoldOverlayInsets
import tachiyomi.presentation.core.theme.GlassDockShape
import tachiyomi.presentation.core.theme.glassBorder
import kotlin.math.roundToInt

@Composable
internal fun HomeNavigationOverlay(
    visible: Boolean,
    navigationItems: @Composable () -> Unit,
    content: @Composable () -> Unit,
) {
    val colorScheme = MaterialTheme.colorScheme
    val blurEnabled = visible && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
    var measuredBarHeight by remember { mutableIntStateOf(0) }
    var overlayCoordinates by remember { mutableStateOf<LayoutCoordinates?>(null) }
    var barBounds by remember { mutableStateOf(Rect.Zero) }
    val barHeight = if (visible) measuredBarHeight else 0
    val overlayInsets = remember(barHeight) {
        if (barHeight > 0) WindowInsets(bottom = barHeight) else null
    }
    val backdrop = if (blurEnabled) {
        Modifier.blurNavigationBackdrop(
            contentLayer = rememberGraphicsLayer(),
            blurLayer = rememberGraphicsLayer(),
            barBounds = barBounds,
            shape = GlassDockShape,
            background = colorScheme.background,
        )
    } else {
        Modifier
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .onPlaced { overlayCoordinates = it },
    ) {
        CompositionLocalProvider(LocalScaffoldOverlayInsets provides overlayInsets) {
            Box(Modifier.matchParentSize().then(backdrop)) { content() }
        }

        if (visible) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .onSizeChanged { measuredBarHeight = it.height }
                    .navigationBarsPadding()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
            ) {
                NavigationSuite(
                    navigationSuiteType = NavigationSuiteType.NavigationBar,
                    modifier = Modifier
                        .onPlaced { coordinates ->
                            val root = overlayCoordinates
                            if (root != null && root.isAttached && coordinates.isAttached) {
                                barBounds = root.localBoundingBoxOf(coordinates, clipBounds = false)
                            }
                        }
                        .shadow(elevation = 12.dp, shape = GlassDockShape, clip = false)
                        .clip(GlassDockShape)
                        .border(glassBorder(), GlassDockShape),
                    colors = NavigationSuiteDefaults.colors(
                        navigationBarContainerColor = colorScheme.surfaceContainerLow.copy(
                            alpha = if (blurEnabled) 0.78f else 0.92f,
                        ),
                        navigationBarContentColor = colorScheme.onSurface,
                    ),
                    content = navigationItems,
                )
            }
        }
    }
}

private fun Modifier.blurNavigationBackdrop(
    contentLayer: GraphicsLayer,
    blurLayer: GraphicsLayer,
    barBounds: Rect,
    shape: Shape,
    background: Color,
): Modifier = drawWithCache {
    val radius = 16.dp.toPx()
    blurLayer.renderEffect = BlurEffect(radius, radius, TileMode.Clamp)
    val clipPath = if (!barBounds.isEmpty) {
        Path().apply {
            addOutline(shape.createOutline(barBounds.size, layoutDirection, this@drawWithCache))
            translate(barBounds.topLeft)
        }
    } else {
        null
    }
    onDrawWithContent {
        if (clipPath == null || size.width <= 0f || size.height <= 0f) {
            drawContent()
            return@onDrawWithContent
        }

        contentLayer.record { this@onDrawWithContent.drawContent() }
        drawLayer(contentLayer)

        // Sample around the floating bar so every edge can blur neighboring content.
        val margin = 2 * radius
        val captureLeft = (barBounds.left - margin).coerceAtLeast(0f).roundToInt()
        val captureTop = (barBounds.top - margin).coerceAtLeast(0f).roundToInt()
        val captureRight = (barBounds.right + margin).coerceAtMost(size.width).roundToInt()
        val captureBottom = (barBounds.bottom + margin).coerceAtMost(size.height).roundToInt()
        val captureWidth = captureRight - captureLeft
        val captureHeight = captureBottom - captureTop
        if (captureWidth <= 0 || captureHeight <= 0) return@onDrawWithContent

        blurLayer.record(size = IntSize(captureWidth, captureHeight)) {
            drawRect(background)
            translate(left = -captureLeft.toFloat(), top = -captureTop.toFloat()) {
                drawLayer(contentLayer)
            }
        }
        clipPath(clipPath) {
            translate(left = captureLeft.toFloat(), top = captureTop.toFloat()) {
                drawLayer(blurLayer)
            }
        }
    }
}
