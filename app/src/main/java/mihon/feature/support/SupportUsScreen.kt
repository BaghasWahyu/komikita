package mihon.feature.support

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import eu.kanade.presentation.components.AppBar
import eu.kanade.presentation.more.settings.widget.TextPreferenceWidget
import eu.kanade.presentation.util.Screen
import mihon.icons.materialsymbols.MaterialSymbols
import mihon.icons.materialsymbols.automirroredrounded.OpenInNew
import mihon.icons.simpleicons.Github
import mihon.icons.simpleicons.SimpleIcons
import tachiyomi.core.common.Constants
import tachiyomi.i18n.MR
import tachiyomi.i18n.komikita_contribute
import tachiyomi.i18n.komikita_report_issue
import tachiyomi.i18n.komikita_support_description
import tachiyomi.i18n.label_support_us
import tachiyomi.presentation.core.components.material.padding
import tachiyomi.presentation.core.i18n.stringResource

class SupportUsScreen : Screen() {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val uriHandler = LocalUriHandler.current

        Scaffold(
            topBar = {
                AppBar(
                    title = stringResource(MR.strings.label_support_us),
                    navigateUp = navigator::pop,
                )
            },
        ) { paddingValues ->
            Column(
                verticalArrangement = Arrangement.spacedBy(MaterialTheme.padding.small),
                modifier = Modifier.verticalScroll(rememberScrollState()).padding(
                    remember(paddingValues) {
                        object : PaddingValues {
                            override fun calculateLeftPadding(layoutDirection: LayoutDirection): Dp {
                                return paddingValues.calculateLeftPadding(layoutDirection) +
                                    MaterialTheme.padding.medium
                            }

                            override fun calculateTopPadding(): Dp {
                                return 0.dp
                            }

                            override fun calculateRightPadding(layoutDirection: LayoutDirection): Dp {
                                return paddingValues.calculateRightPadding(layoutDirection) +
                                    MaterialTheme.padding.medium
                            }

                            override fun calculateBottomPadding(): Dp {
                                return 0.dp
                            }
                        }
                    },
                ),
            ) {
                Spacer(modifier = Modifier.height(paddingValues.calculateTopPadding()))

                Text(
                    text = stringResource(MR.strings.komikita_support_description),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(horizontal = MaterialTheme.padding.medium),
                )

                SupportItem(
                    icon = SimpleIcons.Github,
                    title = stringResource(MR.strings.komikita_report_issue),
                    onClick = { uriHandler.openUri(Constants.URL_ISSUES) },
                )
                SupportItem(
                    icon = SimpleIcons.Github,
                    title = stringResource(MR.strings.komikita_contribute),
                    onClick = { uriHandler.openUri(Constants.URL_PROJECT) },
                )

                Spacer(modifier = Modifier.height(paddingValues.calculateBottomPadding()))
            }
        }
    }

    @Composable
    private fun SupportItem(
        icon: ImageVector,
        title: String,
        onClick: () -> Unit,
    ) {
        Card {
            TextPreferenceWidget(
                title = title,
                icon = icon,
                widget = {
                    Icon(
                        imageVector = MaterialSymbols.AutoMirroredRounded.OpenInNew,
                        contentDescription = null,
                    )
                },
                onPreferenceClick = onClick,
            )
        }
    }
}
