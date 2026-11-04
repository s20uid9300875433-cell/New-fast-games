package com.hexacolor.quest.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

private val HexaDarkColorScheme = darkColorScheme(
    primary = OceanPrimary,
    onPrimary = OceanOnPrimary,
    primaryContainer = OceanPrimaryContainer,
    onPrimaryContainer = OceanOnPrimaryContainer,
    secondary = AmberSecondary,
    onSecondary = AmberOnSecondary,
    secondaryContainer = AmberSecondaryContainer,
    onSecondaryContainer = AmberOnSecondaryContainer,
    tertiary = RoseTertiary,
    onTertiary = RoseOnTertiary,
    tertiaryContainer = RoseTertiaryContainer,
    onTertiaryContainer = RoseOnTertiaryContainer,
    background = DeepNavyBackground,
    onBackground = DeepNavyOnBackground,
    surface = FrostedSlateSurface,
    onSurface = FrostedSlateOnSurface,
    surfaceVariant = FrostedSlateSurfaceVariant,
    onSurfaceVariant = FrostedSlateOnSurfaceVariant,
    outline = SlateOutline,
    outlineVariant = SlateOutlineVariant,
    error = DangerRed,
    errorContainer = DangerContainer,
    onErrorContainer = OnDangerContainer
)

val HexaShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

@Composable
fun HexaColorQuestTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = HexaDarkColorScheme,
        typography = HexaTypography,
        shapes = HexaShapes,
        content = content
    )
}
