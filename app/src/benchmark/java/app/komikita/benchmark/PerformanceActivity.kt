package app.komikita.benchmark

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.ReportDrawn
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import eu.kanade.presentation.theme.TachiyomiTheme
import tachiyomi.presentation.core.components.VerticalFastScroller
import tachiyomi.presentation.core.components.VerticalGridFastScroller

/** Deterministic, offline fixtures, packaged exclusively in the benchmark variant. */
class PerformanceActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val grid = intent.getStringExtra("mode") == "grid"
        setContent {
            TachiyomiTheme {
                if (grid) GridFixture() else ListFixture()
                ReportDrawn()
            }
        }
    }
}

@Composable
private fun ListFixture() {
    val state = rememberLazyListState()
    VerticalFastScroller(state, Modifier.fillMaxSize()) {
        LazyColumn(state = state, modifier = Modifier.fillMaxSize()) {
            items(5_000, key = { it }) { index ->
                Text(
                    text = "Comic $index",
                    modifier = Modifier.fillParentMaxWidth().height(72.dp).padding(16.dp),
                )
            }
        }
    }
}

@Composable
private fun GridFixture() {
    val state = rememberLazyGridState()
    val columns = GridCells.Fixed(3)
    val arrangement = Arrangement.spacedBy(8.dp)
    val padding = PaddingValues(8.dp)
    VerticalGridFastScroller(state, Modifier.fillMaxSize()) {
        LazyVerticalGrid(
            columns = columns,
            state = state,
            horizontalArrangement = arrangement,
            verticalArrangement = arrangement,
            contentPadding = padding,
            modifier = Modifier.fillMaxSize(),
        ) {
            items(5_000, key = { it }) { index ->
                Text(
                    text = "Comic $index",
                    modifier = Modifier.height(160.dp)
                        .background(MaterialTheme.colorScheme.surfaceContainer)
                        .padding(12.dp),
                )
            }
        }
    }
}
