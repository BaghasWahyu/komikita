package eu.kanade.presentation.theme.colorscheme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

/**
 * KomiKita Warm identity color scheme inspired by the terracotta-and-amber folded K mark.
 */
internal object KomiKitaWarmColorScheme : BaseColorScheme() {

    override val darkScheme = darkColorScheme(
        primary = Color(0xFFE06D53),
        onPrimary = Color(0xFF2B0B05),
        primaryContainer = Color(0xFF762A20),
        onPrimaryContainer = Color(0xFFFFF1D6),
        inversePrimary = Color(0xFF9E3828),
        secondary = Color(0xFFF7B957),
        onSecondary = Color(0xFF2B1B00),
        secondaryContainer = Color(0xFF563911),
        onSecondaryContainer = Color(0xFFFFE4B5),
        tertiary = Color(0xFF7AD9A8),
        onTertiary = Color(0xFF06291A),
        tertiaryContainer = Color(0xFF1A4D36),
        onTertiaryContainer = Color(0xFFC2F5DA),
        background = Color(0xFF121011),
        onBackground = Color(0xFFF6EFEA),
        surface = Color(0xFF121011),
        onSurface = Color(0xFFF6EFEA),
        surfaceVariant = Color(0xFF241F21),
        onSurfaceVariant = Color(0xFFB6A9A3),
        surfaceTint = Color(0xFFE06D53),
        inverseSurface = Color(0xFFF6EFEA),
        inverseOnSurface = Color(0xFF1B1718),
        error = Color(0xFFFFB4AB),
        onError = Color(0xFF690005),
        errorContainer = Color(0xFF93000A),
        onErrorContainer = Color(0xFFFFDAD6),
        outline = Color(0xFF8C7D77),
        outlineVariant = Color(0xFF3E3538),
        surfaceContainerLowest = Color(0xFF0E0C0D),
        surfaceContainerLow = Color(0xFF1B1718),
        surfaceContainer = Color(0xFF201B1D),
        surfaceContainerHigh = Color(0xFF241F21),
        surfaceContainerHighest = Color(0xFF2E282B),
    )

    override val lightScheme = lightColorScheme(
        primary = Color(0xFF9E3828),
        onPrimary = Color(0xFFFFFFFF),
        primaryContainer = Color(0xFFFFDAD2),
        onPrimaryContainer = Color(0xFF3B0903),
        inversePrimary = Color(0xFFE06D53),
        secondary = Color(0xFFA8640A),
        onSecondary = Color(0xFFFFFFFF),
        secondaryContainer = Color(0xFFFFE0B2),
        onSecondaryContainer = Color(0xFF331B00),
        tertiary = Color(0xFF1F7A4D),
        onTertiary = Color(0xFFFFFFFF),
        tertiaryContainer = Color(0xFFC2F5DA),
        onTertiaryContainer = Color(0xFF002112),
        background = Color(0xFFFAF6F2),
        onBackground = Color(0xFF1E1918),
        surface = Color(0xFFFAF6F2),
        onSurface = Color(0xFF1E1918),
        surfaceVariant = Color(0xFFF3ECE6),
        onSurfaceVariant = Color(0xFF6A5D58),
        surfaceTint = Color(0xFF9E3828),
        inverseSurface = Color(0xFF322C2A),
        inverseOnSurface = Color(0xFFF6EFEA),
        error = Color(0xFFBA1A1A),
        onError = Color(0xFFFFFFFF),
        errorContainer = Color(0xFFFFDAD6),
        onErrorContainer = Color(0xFF410002),
        outline = Color(0xFF847570),
        outlineVariant = Color(0xFFD8CCC4),
        surfaceContainerLowest = Color(0xFFFFFFFF),
        surfaceContainerLow = Color(0xFFF7F1EC),
        surfaceContainer = Color(0xFFF3ECE6),
        surfaceContainerHigh = Color(0xFFEDE4DC),
        surfaceContainerHighest = Color(0xFFE7DDD5),
    )
}
