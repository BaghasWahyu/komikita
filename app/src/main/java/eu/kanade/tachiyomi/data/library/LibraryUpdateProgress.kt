package eu.kanade.tachiyomi.data.library

data class LibraryUpdateProgress(
    val completed: Int,
    val total: Int,
    val isCategoryUpdate: Boolean,
)
