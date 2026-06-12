package com.savoo.rooterm.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import com.savoo.rooterm.R

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

fun termColorsFor(theme: TermColorTheme, materialAccent: Color, isDark: Boolean): TermColors {
    if (!isDark) return when (theme) {
        TermColorTheme.MATERIAL_MONO -> TermColors(
            background = Color(0xFFFAFAFA), surface = Color(0xFFF0F0F0),
            foreground = Color(0xFF1A1A1A), accent = materialAccent,
            accentSecondary = materialAccent.copy(alpha = 0.75f),
            errorColor = Color(0xFFB00020), dimColor = Color(0xFF999999),
            promptColor = materialAccent,
        )
        TermColorTheme.DRACULA -> TermColors(
            background = Color(0xFFF8F8F2), surface = Color(0xFFEEEEDD),
            foreground = Color(0xFF282A36), accent = Color(0xFF6C3FB5),
            accentSecondary = Color(0xFF1A8F8F), errorColor = Color(0xFFCC2222),
            dimColor = Color(0xFFBFBFBF), promptColor = Color(0xFFB840C0),
        )
        TermColorTheme.NORD -> TermColors(
            background = Color(0xFFECEFF4), surface = Color(0xFFE5E9F0),
            foreground = Color(0xFF2E3440), accent = Color(0xFF4C7A89),
            accentSecondary = Color(0xFF5E81AC), errorColor = Color(0xFFBF616A),
            dimColor = Color(0xFFB8C0D0), promptColor = Color(0xFF6B8C5A),
        )
        TermColorTheme.SOLARIZED -> TermColors(
            background = Color(0xFFFCF6E3), surface = Color(0xFFEEE8D5),
            foreground = Color(0xFF586E75), accent = Color(0xFF268BD2),
            accentSecondary = Color(0xFF2AA198), errorColor = Color(0xFFDC322F),
            dimColor = Color(0xFFB0C4D0), promptColor = Color(0xFFB58900),
        )
        TermColorTheme.GRUVBOX -> TermColors(
            background = Color(0xFFFBF1E7), surface = Color(0xFFEBDBB2),
            foreground = Color(0xFF3C3836), accent = Color(0xFF987409),
            accentSecondary = Color(0xFFB5760F), errorColor = Color(0xFFCC241D),
            dimColor = Color(0xFFBDAE93), promptColor = Color(0xFFD65D0E),
        )
        TermColorTheme.ONE_DARK -> TermColors(
            background = Color(0xFFF5F5F5), surface = Color(0xFFEBEBEB),
            foreground = Color(0xFF383C44), accent = Color(0xFF4078D5),
            accentSecondary = Color(0xFF5FA04E), errorColor = Color(0xFFD44C4C),
            dimColor = Color(0xFFB0B8C8), promptColor = Color(0xFFA626A4),
        )
    }
    return when (theme) {
        TermColorTheme.MATERIAL_MONO -> TermColors(
            background = Color(0xFF080808), surface = Color(0xFF141414),
            foreground = Color(0xFFE2E2E2), accent = materialAccent,
            accentSecondary = materialAccent.copy(alpha = 0.65f),
            errorColor = Color(0xFFCF6679), dimColor = Color(0xFF777777),
            promptColor = materialAccent,
        )
        TermColorTheme.DRACULA -> TermColors(
            background = Color(0xFF1A1B26), surface = Color(0xFF24253A),
            foreground = Color(0xFFF8F8F2), accent = Color(0xFFBD93F9),
            accentSecondary = Color(0xFF8BE9FD), errorColor = Color(0xFFFF5555),
            dimColor = Color(0xFF5E6178), promptColor = Color(0xFFFF79C6),
        )
        TermColorTheme.NORD -> TermColors(
            background = Color(0xFF1E2230), surface = Color(0xFF252B3B),
            foreground = Color(0xFFD8DEE9), accent = Color(0xFF88C0D0),
            accentSecondary = Color(0xFF81A1C1), errorColor = Color(0xFFBF616A),
            dimColor = Color(0xFF4E5668), promptColor = Color(0xFFA3BE8C),
        )
        TermColorTheme.SOLARIZED -> TermColors(
            background = Color(0xFF002B36), surface = Color(0xFF073642),
            foreground = Color(0xFF93A1A1), accent = Color(0xFF268BD2),
            accentSecondary = Color(0xFF2AA198), errorColor = Color(0xFFDC322F),
            dimColor = Color(0xFF4A6E75), promptColor = Color(0xFFB58900),
        )
        TermColorTheme.GRUVBOX -> TermColors(
            background = Color(0xFF1D2021), surface = Color(0xFF282828),
            foreground = Color(0xFFEBDBB2), accent = Color(0xFFB8BB26),
            accentSecondary = Color(0xFFFABD2F), errorColor = Color(0xFFCC241D),
            dimColor = Color(0xFF585550), promptColor = Color(0xFFFE8019),
        )
        TermColorTheme.ONE_DARK -> TermColors(
            background = Color(0xFF1A1D27), surface = Color(0xFF21242E),
            foreground = Color(0xFFABB2BF), accent = Color(0xFF61AFEF),
            accentSecondary = Color(0xFF98C379), errorColor = Color(0xFFE06C75),
            dimColor = Color(0xFF5A6270), promptColor = Color(0xFFC678DD),
        )
    }
}

val LocalTermColors = compositionLocalOf { termColorsFor(TermColorTheme.MATERIAL_MONO, Color(0xFF6750A4), true) }
val LocalTermFontSize = compositionLocalOf { 14.sp }

val GoogleSansFlex = FontFamily(
    Font(R.font.google_sans_flex_regular, FontWeight.Normal),
    Font(R.font.google_sans_flex_medium, FontWeight.Medium),
    Font(R.font.google_sans_flex_bold, FontWeight.Bold),
)

@Composable
fun RooTermTheme(
    termColorTheme: TermColorTheme = TermColorTheme.MATERIAL_MONO,
    fontSize: TextUnit = 14.sp,
    useDynamicColor: Boolean = true,
    isDarkMode: Boolean = true,
    content: @Composable () -> Unit,
) {
    val context = LocalContext.current
    val colorScheme: ColorScheme = when {
        useDynamicColor && isDarkMode -> dynamicDarkColorScheme(context)
        useDynamicColor && !isDarkMode -> dynamicLightColorScheme(context)
        isDarkMode -> darkColorScheme()
        else -> lightColorScheme()
    }

    val termColors = termColorsFor(termColorTheme, colorScheme.primary, isDarkMode)

    val gsf = GoogleSansFlex
    val typography = Typography(
        displayLarge    = TextStyle(fontFamily = gsf, fontWeight = FontWeight.Bold),
        displayMedium   = TextStyle(fontFamily = gsf, fontWeight = FontWeight.Bold),
        displaySmall    = TextStyle(fontFamily = gsf, fontWeight = FontWeight.Bold),
        headlineLarge   = TextStyle(fontFamily = gsf, fontWeight = FontWeight.Bold),
        headlineMedium  = TextStyle(fontFamily = gsf, fontWeight = FontWeight.Bold),
        headlineSmall   = TextStyle(fontFamily = gsf, fontWeight = FontWeight.Bold),
        titleLarge      = TextStyle(fontFamily = gsf, fontWeight = FontWeight.Bold),
        titleMedium     = TextStyle(fontFamily = gsf, fontWeight = FontWeight.Medium),
        titleSmall      = TextStyle(fontFamily = gsf, fontWeight = FontWeight.Medium),
        bodyLarge       = TextStyle(fontFamily = gsf, fontWeight = FontWeight.Medium),
        bodyMedium      = TextStyle(fontFamily = gsf, fontWeight = FontWeight.Medium),
        bodySmall       = TextStyle(fontFamily = gsf, fontWeight = FontWeight.Normal),
        labelLarge      = TextStyle(fontFamily = gsf, fontWeight = FontWeight.Medium),
        labelMedium     = TextStyle(fontFamily = gsf, fontWeight = FontWeight.Medium),
        labelSmall      = TextStyle(fontFamily = gsf, fontWeight = FontWeight.Normal),
    )

    CompositionLocalProvider(
        LocalTermColors   provides termColors,
        LocalTermFontSize provides fontSize,
    ) {
        MaterialTheme(colorScheme = colorScheme, typography = typography, content = content)
    }
}

object TermTheme {
    val colors: TermColors
        @Composable @ReadOnlyComposable get() = LocalTermColors.current
    val fontSize: TextUnit
        @Composable @ReadOnlyComposable get() = LocalTermFontSize.current
    val fontFamily: FontFamily
        @Composable @ReadOnlyComposable get() = GoogleSansFlex
    val monoFamily: FontFamily
        @Composable @ReadOnlyComposable get() = FontFamily.Monospace
}
