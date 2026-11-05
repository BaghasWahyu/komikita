package tachiyomi.domain.library.service

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/** An authenticated reveal lasts only while the application is in the foreground. */
@Inject
@SingleIn(AppScope::class)
class HiddenLibrarySession {
    private val mutableUnlocked = MutableStateFlow(false)
    val unlocked = mutableUnlocked.asStateFlow()

    fun unlock() {
        mutableUnlocked.value = true
    }

    fun lock() {
        mutableUnlocked.value = false
    }
}
