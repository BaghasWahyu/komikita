package eu.kanade.presentation.reader

import androidx.activity.compose.LocalActivity
import androidx.compose.runtime.Composable
import androidx.lifecycle.compose.LifecycleResumeEffect

/** Requests a lower display refresh rate only while the reader is resumed. */
@Composable
fun ReaderRefreshRatePreference(enabled: Boolean) {
    val activity = LocalActivity.current ?: return
    LifecycleResumeEffect(activity, enabled) {
        val window = activity.window
        val previousMode = window.attributes.preferredDisplayModeId
        val previousRate = window.attributes.preferredRefreshRate
        if (enabled) {
            @Suppress("DEPRECATION")
            val display = activity.windowManager.defaultDisplay
            val current = display.mode
            val modes = display.supportedModes.filter {
                it.physicalWidth == current.physicalWidth && it.physicalHeight == current.physicalHeight
            }
            val rate = chooseReaderRefreshRate(modes.map { it.refreshRate })
            val mode = modes.firstOrNull { it.refreshRate == rate }
            if (mode != null) {
                window.attributes = window.attributes.apply {
                    preferredDisplayModeId = mode.modeId
                    preferredRefreshRate = mode.refreshRate
                }
            }
        }
        onPauseOrDispose {
            if (enabled) {
                window.attributes = window.attributes.apply {
                    preferredDisplayModeId = previousMode
                    preferredRefreshRate = previousRate
                }
            }
        }
    }
}

/** Prefer up to 60 Hz without requesting an unsupported mode or changing resolution. */
internal fun chooseReaderRefreshRate(rates: List<Float>): Float? {
    val valid = rates.filter { it.isFinite() && it > 0f }
    return valid.filter { it <= 60.1f }.maxOrNull() ?: valid.minOrNull()
}
