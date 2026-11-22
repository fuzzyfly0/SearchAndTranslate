package dev.davidv.translator.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

/**
 * Colours of the Search & Translate logo (orange to pink) instead of the wallpaper's
 * dynamic colours, with warm neutral surfaces. Shared by the app and this module so
 * settings, overlay and translator look the same.
 */
fun brandColorScheme(dark: Boolean): ColorScheme = if (dark) BrandDark else BrandLight

private val BrandLight =
  lightColorScheme(
    primary = Color(0xFFE8590C),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFFFDBCA),
    onPrimaryContainer = Color(0xFF351000),
    inversePrimary = Color(0xFFFFB590),
    secondary = Color(0xFF765849),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFFFDBCA),
    onSecondaryContainer = Color(0xFF2C160B),
    tertiary = Color(0xFFBE185D),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFFFD9E2),
    onTertiaryContainer = Color(0xFF3E001D),
    background = Color(0xFFFFF8F6),
    onBackground = Color(0xFF231A16),
    surface = Color(0xFFFFF8F6),
    onSurface = Color(0xFF231A16),
    surfaceVariant = Color(0xFFF5DED5),
    onSurfaceVariant = Color(0xFF53443D),
    surfaceTint = Color(0xFFE8590C),
    inverseSurface = Color(0xFF392E2A),
    inverseOnSurface = Color(0xFFFFEDE6),
    outline = Color(0xFF85736C),
    outlineVariant = Color(0xFFD8C2BA),
    surfaceBright = Color(0xFFFFF8F6),
    surfaceDim = Color(0xFFE8D6D0),
    surfaceContainerLowest = Color(0xFFFFFFFF),
    surfaceContainerLow = Color(0xFFFFF1EC),
    surfaceContainer = Color(0xFFFCEAE4),
    surfaceContainerHigh = Color(0xFFF6E5DE),
    surfaceContainerHighest = Color(0xFFF0DFD8),
  )

private val BrandDark =
  darkColorScheme(
    primary = Color(0xFFFB923C),
    onPrimary = Color(0xFF3B1500),
    primaryContainer = Color(0xFF7A3000),
    onPrimaryContainer = Color(0xFFFFDBCA),
    inversePrimary = Color(0xFFE8590C),
    secondary = Color(0xFFE7BEAC),
    onSecondary = Color(0xFF442A1E),
    secondaryContainer = Color(0xFF5D4033),
    onSecondaryContainer = Color(0xFFFFDBCA),
    tertiary = Color(0xFFF472B6),
    onTertiary = Color(0xFF5E1133),
    tertiaryContainer = Color(0xFF8A1B4A),
    onTertiaryContainer = Color(0xFFFFD9E2),
    background = Color(0xFF1A120E),
    onBackground = Color(0xFFF0DFD8),
    surface = Color(0xFF1A120E),
    onSurface = Color(0xFFF0DFD8),
    surfaceVariant = Color(0xFF53443D),
    onSurfaceVariant = Color(0xFFD8C2BA),
    surfaceTint = Color(0xFFFB923C),
    inverseSurface = Color(0xFFF0DFD8),
    inverseOnSurface = Color(0xFF392E2A),
    outline = Color(0xFFA08D85),
    outlineVariant = Color(0xFF53443D),
    surfaceBright = Color(0xFF42372F),
    surfaceDim = Color(0xFF1A120E),
    surfaceContainerLowest = Color(0xFF140C09),
    surfaceContainerLow = Color(0xFF231A16),
    surfaceContainer = Color(0xFF271E1A),
    surfaceContainerHigh = Color(0xFF322824),
    surfaceContainerHighest = Color(0xFF3D332E),
  )
