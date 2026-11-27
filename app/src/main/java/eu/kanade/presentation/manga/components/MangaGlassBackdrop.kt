package eu.kanade.presentation.manga.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import tachiyomi.domain.manga.model.Manga

@Composable
internal fun MangaGlassBackdrop(manga: Manga, tint: Brush, modifier: Modifier = Modifier) {
    AsyncImage(
        model = manga,
        contentDescription = null,
        contentScale = ContentScale.Crop,
        modifier = modifier
            .drawWithContent {
                drawContent()
                drawRect(tint)
            }
            .blur(16.dp),
    )
}
