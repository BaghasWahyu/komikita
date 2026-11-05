package eu.kanade.tachiyomi.ui.security

import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import eu.kanade.tachiyomi.util.system.AuthenticatorUtil.authenticate
import kotlinx.coroutines.launch
import mihon.app.di.appGraph
import mihon.icons.materialsymbols.MaterialSymbols
import mihon.icons.materialsymbols.rounded.Security
import tachiyomi.i18n.MR
import tachiyomi.i18n.komikita_device_security
import tachiyomi.i18n.komikita_hidden_locked
import tachiyomi.i18n.komikita_hidden_locked_summary
import tachiyomi.i18n.komikita_unlock_hidden
import tachiyomi.presentation.core.i18n.stringResource
import tachiyomi.presentation.core.screens.LoadingScreen
import tachiyomi.presentation.core.util.collectAsState

/** Call before composing any title, cover, or reader content, including intent entry points. */
@Composable
fun hiddenCategoryAccess(mangaId: Long? = null): Boolean {
    val context = LocalContext.current
    val graph = remember(context) { context.appGraph }
    val enabled by graph.securityPreferences.authenticateHiddenCategories.collectAsState()
    val hidden by graph.libraryPreferences.hiddenCategories.collectAsState()
    val unlocked by graph.hiddenLibrarySession.unlocked.collectAsStateWithLifecycle()
    if (!enabled || hidden.isEmpty() || unlocked) return true

    if (mangaId != null) {
        var protectedIds by remember(hidden, mangaId) { mutableStateOf<Set<Long>?>(null) }
        LaunchedEffect(graph, hidden, mangaId) {
            graph.libraryVisibility.hiddenMangaIdsFlow().collect { protectedIds = it }
        }
        if (protectedIds == null) {
            LoadingScreen()
            return false
        }
        if (mangaId !in protectedIds.orEmpty()) return true
    }

    val scope = rememberCoroutineScope()
    val title = stringResource(MR.strings.komikita_unlock_hidden)
    var authenticating by remember { mutableStateOf(false) }
    Column(
        modifier = Modifier.fillMaxSize().systemBarsPadding().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Column(
            modifier = Modifier.widthIn(max = 360.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            Icon(
                MaterialSymbols.Rounded.Security,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.primary,
            )
            Text(
                stringResource(MR.strings.komikita_hidden_locked),
                style = MaterialTheme.typography.headlineSmall,
                textAlign = TextAlign.Center,
            )
            Text(
                stringResource(MR.strings.komikita_hidden_locked_summary),
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Button(
                modifier = Modifier.fillMaxWidth(),
                enabled = !authenticating,
                onClick = {
                    scope.launch {
                        authenticating = true
                        try {
                            val activity = context as? FragmentActivity ?: return@launch
                            if (activity.authenticate(title = title, subtitle = null, allowUnsupported = false)) {
                                graph.hiddenLibrarySession.unlock()
                            }
                        } finally {
                            authenticating = false
                        }
                    }
                },
            ) { Text(title, textAlign = TextAlign.Center) }
            TextButton(onClick = { context.startActivity(Intent(Settings.ACTION_SECURITY_SETTINGS)) }) {
                Text(stringResource(MR.strings.komikita_device_security))
            }
        }
    }
    return false
}
