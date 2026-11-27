package eu.kanade.presentation.theme.colorscheme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

/**
 * KomiKita Mint Ink color scheme combining mint green (#83E8BE) and deep ink (#102D2B).
 */
internal object MintInkColorScheme : BaseColorScheme() {

    override val darkScheme = darkColorScheme(
        primary = Color(0xFF83E8BE),
        onPrimary = Color(0xFF09241D),
        primaryContainer = Color(0xFF144B3E),
        onPrimaryContainer = Color(0xFFC2FCE2),
        inversePrimary = Color(0xFF0F765D),
        secondary = Color(0xFF56CFE1),
        onSecondary = Color(0xFF04262B),
        secondaryContainer = Color(0xFF12464F),
        onSecondaryContainer = Color(0xFFBDF4FC),
        tertiary = Color(0xFFA3E683),
        onTertiary = Color(0xFF112905),
        tertiaryContainer = Color(0xFF274D15),
        onTertiaryContainer = Color(0xFFD5FCB9),
        background = Color(0xFF0B1616),
        onBackground = Color(0xFFECF8F4),
        surface = Color(0xFF0B1616),
        onSurface = Color(0xFFECF8F4),
        surfaceVariant = Color(0xFF162C2B),
        onSurfaceVariant = Color(0xFF98B7B1),
        surfaceTint = Color(0xFF83E8BE),
        inverseSurface = Color(0xFFECF8F4),
        inverseOnSurface = Color(0xFF102120),
        error = Color(0xFFFFB4AB),
        onError = Color(0xFF690005),
        errorContainer = Color(0xFF93000A),
        onErrorContainer = Color(0xFFFFDAD6),
        outline = Color(0xFF6C8C87),
        outlineVariant = Color(0xFF254240),
        surfaceContainerLowest = Color(0xFF071010),
        surfaceContainerLow = Color(0xFF102120),
        surfaceContainer = Color(0xFF132625),
        surfaceContainerHigh = Color(0xFF162C2B),
        surfaceContainerHighest = Color(0xFF1E3937),
    )

    override val lightScheme = lightColorScheme(
        primary = Color(0xFF0F765D),
        onPrimary = Color(0xFFFFFFFF),
        primaryContainer = Color(0xFFA7F3D0),
        onPrimaryContainer = Color(0xFF032118),
        inversePrimary = Color(0xFF83E8BE),
        secondary = Color(0xFF0E7490),
        onSecondary = Color(0xFFFFFFFF),
        secondaryContainer = Color(0xFFCFFAFE),
        onSecondaryContainer = Color(0xFF05242E),
        tertiary = Color(0xFF2F7A29),
        onTertiary = Color(0xFFFFFFFF),
        tertiaryContainer = Color(0xFFCFF7C6),
        onTertiaryContainer = Color(0xFF082104),
        background = Color(0xFFF3FAF7),
        onBackground = Color(0xFF102D2B),
        surface = Color(0xFFF3FAF7),
        onSurface = Color(0xFF102D2B),
        surfaceVariant = Color(0xFFE6F3EE),
        onSurfaceVariant = Color(0xFF4C6B67),
        surfaceTint = Color(0xFF0F765D),
        inverseSurface = Color(0xFF1D3836),
        inverseOnSurface = Color(0xFFECF8F4),
        error = Color(0xFFBA1A1A),
        onError = Color(0xFFFFFFFF),
        errorContainer = Color(0xFFFFDAD6),
        onErrorContainer = Color(0xFF410002),
        outline = Color(0xFF6D8C87),
        outlineVariant = Color(0xFFC4DDD6),
        surfaceContainerLowest = Color(0xFFFFFFFF),
        surfaceContainerLow = Color(0xFFEDF7F3),
        surfaceContainer = Color(0xFFE6F3EE),
        surfaceContainerHigh = Color(0xFFDCEEE7),
        surfaceContainerHighest = Color(0xFFD4EAE2),
    )
}
