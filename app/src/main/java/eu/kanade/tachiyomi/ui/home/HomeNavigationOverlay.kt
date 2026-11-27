package eu.kanade.tachiyomi.ui.home

import android.os.Build
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuite
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteDefaults
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteType
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlurEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.layer.GraphicsLayer
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import eu.kanade.domain.ui.model.BottomNavigationStyle
import tachiyomi.presentation.core.components.material.LocalScaffoldOverlayInsets
import kotlin.math.roundToInt

@Composable
internal fun HomeNavigationOverlay(
    style: BottomNavigationStyle,
    visible: Boolean,
    navigationItems: @Composable () -> Unit,
    content: @Composable () -> Unit,
) {
    val colorScheme = MaterialTheme.colorScheme
    val blurEnabled = visible && style == BottomNavigationStyle.GLASS && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
    var measuredBarHeight by remember { mutableIntStateOf(0) }
    val barHeight = if (visible) measuredBarHeight else 0
    val overlayInsets = remember(barHeight) {
        if (barHeight > 0) WindowInsets(bottom = barHeight) else null
    }
    val backdrop = if (blurEnabled) {
        Modifier.blurNavigationBackdrop(
            contentLayer = rememberGraphicsLayer(),
            blurLayer = rememberGraphicsLayer(),
            barHeight = barHeight,
            background = colorScheme.background,
        )
    } else {
        Modifier
    }

    Box(Modifier.fillMaxSize()) {
        CompositionLocalProvider(LocalScaffoldOverlayInsets provides overlayInsets) {
            Box(Modifier.matchParentSize().then(backdrop)) { content() }
        }

        if (visible) {
            val edgeColor = colorScheme.outlineVariant
            NavigationSuite(
                navigationSuiteType = NavigationSuiteType.NavigationBar,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .onSizeChanged { measuredBarHeight = it.height }
                    .drawWithCache {
                        val edgeWidth = 1.dp.toPx()
                        onDrawWithContent {
                            drawContent()
                            drawLine(
                                color = edgeColor,
                                start = Offset(0f, edgeWidth / 2),
                                end = Offset(size.width, edgeWidth / 2),
                                strokeWidth = edgeWidth,
                            )
                        }
                    },
                colors = NavigationSuiteDefaults.colors(
                    navigationBarContainerColor = colorScheme.surfaceContainerLow.copy(
                        alpha = if (blurEnabled) 0.80f else 0.92f,
                    ),
                    navigationBarContentColor = colorScheme.onSurface,
                ),
                content = navigationItems,
            )
        }
    }
}

private fun Modifier.blurNavigationBackdrop(
    contentLayer: GraphicsLayer,
    blurLayer: GraphicsLayer,
    barHeight: Int,
    background: Color,
): Modifier = drawWithCache {
    val radius = 16.dp.toPx()
    blurLayer.renderEffect = BlurEffect(radius, radius, TileMode.Clamp)
    onDrawWithContent {
        if (barHeight <= 0 || size.width <= 0f || size.height <= 0f) {
            drawContent()
            return@onDrawWithContent
        }

        contentLayer.record { this@onDrawWithContent.drawContent() }
        drawLayer(contentLayer)

        val barTop = (size.height - barHeight).coerceAtLeast(0f)
        // Sample above the bar so the top edge can blur neighboring content.
        val captureTop = (barTop - 2 * radius).coerceAtLeast(0f).roundToInt()
        blurLayer.record(size = IntSize(size.width.roundToInt(), size.height.roundToInt() - captureTop)) {
            drawRect(background)
            translate(top = -captureTop.toFloat()) { drawLayer(contentLayer) }
        }
        clipRect(top = barTop) {
            translate(top = captureTop.toFloat()) { drawLayer(blurLayer) }
        }
    }
}
