package com.savoo.rooterm.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp

enum class TermColorTheme(val displayName: String) {
    MATERIAL_MONO("Mono"),
    DRACULA("Dracula"),
    NORD("Nord"),
    SOLARIZED("Solarized"),
    GRUVBOX("Gruvbox"),
    ONE_DARK("One Dark"),
}

@Immutable
data class TermColors(
    val background: Color,
    val surface: Color,       
    val foreground: Color,
    val accent: Color,
    val accentSecondary: Color,
    val errorColor: Color,
    val dimColor: Color,
    val promptColor: Color,
)

fun termColorsFor(theme: TermColorTheme, materialAccent: Color): TermColors = when (theme) {
    TermColorTheme.MATERIAL_MONO -> TermColors(
        background      = Color(0xFF080808),
        surface         = Color(0xFF141414),
        foreground      = Color(0xFFE2E2E2),
        accent          = materialAccent,
        accentSecondary = materialAccent.copy(alpha = 0.65f),
        errorColor      = Color(0xFFCF6679),
        dimColor        = Color(0xFF555555),
        promptColor     = materialAccent,
    )
    TermColorTheme.DRACULA -> TermColors(
        background      = Color(0xFF1A1B26),
        surface         = Color(0xFF24253A),
        foreground      = Color(0xFFF8F8F2),
        accent          = Color(0xFFBD93F9),
        accentSecondary = Color(0xFF8BE9FD),
        errorColor      = Color(0xFFFF5555),
        dimColor        = Color(0xFF44475A),
        promptColor     = Color(0xFFFF79C6),
    )
    TermColorTheme.NORD -> TermColors(
        background      = Color(0xFF1E2230),
        surface         = Color(0xFF252B3B),
        foreground      = Color(0xFFD8DEE9),
        accent          = Color(0xFF88C0D0),
        accentSecondary = Color(0xFF81A1C1),
        errorColor      = Color(0xFFBF616A),
        dimColor        = Color(0xFF3B4252),
        promptColor     = Color(0xFFA3BE8C),
    )
    TermColorTheme.SOLARIZED -> TermColors(
        background      = Color(0xFF002B36),
        surface         = Color(0xFF073642),
        foreground      = Color(0xFF93A1A1),
        accent          = Color(0xFF268BD2),
        accentSecondary = Color(0xFF2AA198),
        errorColor      = Color(0xFFDC322F),
        dimColor        = Color(0xFF335056),
        promptColor     = Color(0xFFB58900),
    )
    TermColorTheme.GRUVBOX -> TermColors(
        background      = Color(0xFF1D2021),
        surface         = Color(0xFF282828),
        foreground      = Color(0xFFEBDBB2),
        accent          = Color(0xFFB8BB26),
        accentSecondary = Color(0xFFFABD2F),
        errorColor      = Color(0xFFCC241D),
        dimColor        = Color(0xFF3C3836),
        promptColor     = Color(0xFFFE8019),
    )
    TermColorTheme.ONE_DARK -> TermColors(
        background      = Color(0xFF1A1D27),
        surface         = Color(0xFF21242E),
        foreground      = Color(0xFFABB2BF),
        accent          = Color(0xFF61AFEF),
        accentSecondary = Color(0xFF98C379),
        errorColor      = Color(0xFFE06C75),
        dimColor        = Color(0xFF3E4452),
        promptColor     = Color(0xFFC678DD),
    )
}

val LocalTermColors    = compositionLocalOf { termColorsFor(TermColorTheme.MATERIAL_MONO, Color(0xFF6750A4)) }
val LocalTermFontSize  = compositionLocalOf { 14.sp }

@Composable
fun RooTermTheme(
    termColorTheme: TermColorTheme = TermColorTheme.MATERIAL_MONO,
    fontSize: TextUnit = 14.sp,
    useDynamicColor: Boolean = true,
    content: @Composable () -> Unit,
) {
    val context = LocalContext.current
    val colorScheme: ColorScheme = if (useDynamicColor)
        dynamicDarkColorScheme(context)
    else
        darkColorScheme()

    val termColors = termColorsFor(termColorTheme, colorScheme.primary)

    CompositionLocalProvider(
        LocalTermColors   provides termColors,
        LocalTermFontSize provides fontSize,
    ) {
        MaterialTheme(colorScheme = colorScheme, content = content)
    }
}

object TermTheme {
    val colors: TermColors
        @Composable @ReadOnlyComposable get() = LocalTermColors.current
    val fontSize: TextUnit
        @Composable @ReadOnlyComposable get() = LocalTermFontSize.current
    val fontFamily: FontFamily
        @Composable @ReadOnlyComposable get() = FontFamily.Monospace
}
