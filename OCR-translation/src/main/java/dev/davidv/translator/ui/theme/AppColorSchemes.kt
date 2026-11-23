package dev.davidv.translator.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

/**
 * Fixed app colours (no Material You): a Material 3 tonal palette seeded from the
 * logo's orange (#F97316). Dark mode gets the light orange accent; the surfaces stay
 * near neutral so only the few accents read as orange. Shared by the app, the
 * translator screens and the overlay chrome so everything matches.
 */
object AppColorSchemes {
  val Light: ColorScheme =
    lightColorScheme(
      primary = Color(0xFF9A4600),
      onPrimary = Color(0xFFFFFFFF),
      primaryContainer = Color(0xFFFFDBC8),
      onPrimaryContainer = Color(0xFF321200),
      inversePrimary = Color(0xFFFFB68A),
      secondary = Color(0xFF765848),
      onSecondary = Color(0xFFFFFFFF),
      secondaryContainer = Color(0xFFFFDBC8),
      onSecondaryContainer = Color(0xFF2B160A),
      tertiary = Color(0xFF645F30),
      onTertiary = Color(0xFFFFFFFF),
      tertiaryContainer = Color(0xFFEBE3A9),
      onTertiaryContainer = Color(0xFF1F1C00),
      background = Color(0xFFFFF8F5),
      onBackground = Color(0xFF221A15),
      surface = Color(0xFFFFF8F5),
      onSurface = Color(0xFF221A15),
      surfaceVariant = Color(0xFFF4DED4),
      onSurfaceVariant = Color(0xFF52443C),
      surfaceTint = Color(0xFF9A4600),
      inverseSurface = Color(0xFF382E29),
      inverseOnSurface = Color(0xFFFFEDE5),
      outline = Color(0xFF85746B),
      outlineVariant = Color(0xFFD7C2B9),
      surfaceBright = Color(0xFFFFF8F5),
      surfaceDim = Color(0xFFE7D7CF),
      surfaceContainerLowest = Color(0xFFFFFFFF),
      surfaceContainerLow = Color(0xFFFFF1EB),
      surfaceContainer = Color(0xFFFCEAE3),
      surfaceContainerHigh = Color(0xFFF6E5DD),
      surfaceContainerHighest = Color(0xFFF0DFD7),
    )

  val Dark: ColorScheme =
    darkColorScheme(
      primary = Color(0xFFFFB68A),
      onPrimary = Color(0xFF532200),
      primaryContainer = Color(0xFF763300),
      onPrimaryContainer = Color(0xFFFFDBC8),
      inversePrimary = Color(0xFF9A4600),
      secondary = Color(0xFFE6BEAB),
      onSecondary = Color(0xFF432B1E),
      secondaryContainer = Color(0xFF5C4033),
      onSecondaryContainer = Color(0xFFFFDBC8),
      tertiary = Color(0xFFCEC78F),
      onTertiary = Color(0xFF353107),
      tertiaryContainer = Color(0xFF4C471B),
      onTertiaryContainer = Color(0xFFEBE3A9),
      background = Color(0xFF1A120D),
      onBackground = Color(0xFFF0DFD7),
      surface = Color(0xFF1A120D),
      onSurface = Color(0xFFF0DFD7),
      surfaceVariant = Color(0xFF52443C),
      onSurfaceVariant = Color(0xFFD7C2B9),
      surfaceTint = Color(0xFFFFB68A),
      inverseSurface = Color(0xFFF0DFD7),
      inverseOnSurface = Color(0xFF382E29),
      outline = Color(0xFFA08D84),
      outlineVariant = Color(0xFF52443C),
      surfaceBright = Color(0xFF423732),
      surfaceDim = Color(0xFF1A120D),
      surfaceContainerLowest = Color(0xFF140D08),
      surfaceContainerLow = Color(0xFF221A15),
      surfaceContainer = Color(0xFF271E19),
      surfaceContainerHigh = Color(0xFF322823),
      surfaceContainerHighest = Color(0xFF3D332D),
    )

  fun forDark(dark: Boolean): ColorScheme = if (dark) Dark else Light
}
