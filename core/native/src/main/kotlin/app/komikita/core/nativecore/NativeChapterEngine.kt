package app.komikita.core.nativecore

/** Optional native batch processing; failure always leaves the Kotlin implementation available. */
object NativeChapterEngine {
    @Volatile
    private var available = try {
        System.loadLibrary("komikita_native")
        true
    } catch (_: LinkageError) {
        false
    } catch (_: SecurityException) {
        false
    }

    fun missingChaptersCount(numbers: DoubleArray): Int? {
        if (!available) return null
        return try {
            nativeMissingChaptersCount(numbers).takeIf { it >= 0 }
        } catch (_: LinkageError) {
            available = false
            null
        }
    }

    @JvmStatic
    private external fun nativeMissingChaptersCount(numbers: DoubleArray): Int
}
