package com.hexacolor.quest.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hexacolor.quest.R
import com.hexacolor.quest.logic.HexMath
import com.hexacolor.quest.model.SymbolType
import com.hexacolor.quest.model.TileTier
import com.hexacolor.quest.ui.theme.QuestColors
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

fun buildHexagonPath(center: Offset, size: Float): Path {
    val corners = HexMath.getCornerPoints(center, size)
    return Path().apply {
        moveTo(corners[0].x, corners[0].y)
        for (i in 1..5) {
            lineTo(corners[i].x, corners[i].y)
        }
        close()
    }
}

fun DrawScope.draw25DHexTile(
    center: Offset,
    hexRadius: Float,
    tier: TileTier,
    isSelected: Boolean = false,
    isHintSource: Boolean = false,
    liftOffsetPx: Float = 0f,
    tileScale: Float = 1f,
    textMeasurer: TextMeasurer? = null
) {
    val liftedCenter = Offset(center.x, center.y + liftOffsetPx)
    val extrusion = if (isSelected) hexRadius * 0.34f else hexRadius * 0.25f

    withTransform({
        if (tileScale != 1f) {
            this.scale(scaleX = tileScale, scaleY = tileScale, pivot = liftedCenter)
        }
    }) {
        // Soft floor drop shadow
        val shadowPath = buildHexagonPath(
            center = Offset(center.x, center.y + extrusion + (if (isSelected) 8f else 4f)),
            size = hexRadius * 0.98f
        )
        drawPath(
            path = shadowPath,
            color = Color.Black.copy(alpha = if (isSelected) 0.55f else 0.42f)
        )

        // Selection or Hint outer aura
        if (isSelected) {
            val auraPath = buildHexagonPath(liftedCenter, hexRadius + 4.5f)
            drawPath(
                path = auraPath,
                color = QuestColors.CyanGlow,
                style = Stroke(width = 3.5f)
            )
        } else if (isHintSource) {
            val hintAura = buildHexagonPath(liftedCenter, hexRadius + 4f)
            drawPath(
                path = hintAura,
                color = QuestColors.GoldStar,
                style = Stroke(
                    width = 3f,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 6f), 0f)
                )
            )
        }

        // Layer 3: Bottom-most extruded ridge
        val ridge3Path = buildHexagonPath(
            Offset(liftedCenter.x, liftedCenter.y + extrusion),
            hexRadius
        )
        drawPath(path = ridge3Path, color = tier.ridgeColor)
        drawPath(
            path = ridge3Path,
            color = Color.Black.copy(alpha = 0.65f),
            style = Stroke(width = 1.4f)
        )

        // Layer 2: Middle ridge with highlight rim
        val ridge2Y = extrusion * 0.67f
        val ridge2Path = buildHexagonPath(
            Offset(liftedCenter.x, liftedCenter.y + ridge2Y),
            hexRadius
        )
        drawPath(path = ridge2Path, color = tier.shadowColor)
        drawPath(
            path = ridge2Path,
            color = tier.ridgeHighlight.copy(alpha = 0.5f),
            style = Stroke(width = 1.1f)
        )

        // Layer 1: Upper ridge
        val ridge1Y = extrusion * 0.34f
        val ridge1Path = buildHexagonPath(
            Offset(liftedCenter.x, liftedCenter.y + ridge1Y),
            hexRadius
        )
        drawPath(path = ridge1Path, color = tier.color)
        drawPath(
            path = ridge1Path,
            color = tier.lightColor.copy(alpha = 0.6f),
            style = Stroke(width = 1.2f)
        )

        // Main Top Glossy Hexagon Face
        val topFacePath = buildHexagonPath(liftedCenter, hexRadius)
        drawPath(
            path = topFacePath,
            brush = Brush.verticalGradient(
                colorStops = arrayOf(
                    0.0f to tier.lightColor,
                    0.28f to tier.color,
                    0.86f to tier.shadowColor,
                    1.0f to tier.ridgeColor
                ),
                startY = liftedCenter.y - hexRadius,
                endY = liftedCenter.y + hexRadius
            )
        )

        // Crisp translucent white bevel border
        drawPath(
            path = topFacePath,
            color = Color.White.copy(alpha = 0.75f),
            style = Stroke(width = 1.8f)
        )

        // Top Specular Highlight Arc
        val specularPath = buildHexagonPath(
            Offset(liftedCenter.x, liftedCenter.y - 1.5f),
            hexRadius * 0.84f
        )
        drawPath(
            path = specularPath,
            color = Color.White.copy(alpha = 0.45f),
            style = Stroke(
                width = 1.4f,
                pathEffect = PathEffect.dashPathEffect(
                    floatArrayOf(hexRadius * 0.9f, hexRadius * 1.4f),
                    0f
                )
            )
        )

        // Embossed Center Symbol
        val symbolCenter = if (textMeasurer != null) {
            Offset(liftedCenter.x, liftedCenter.y - hexRadius * 0.10f)
        } else {
            liftedCenter
        }
        drawEmbossedSymbol(
            symbolType = tier.symbolType,
            center = symbolCenter,
            symbolRadius = hexRadius * 0.36f,
            fillColor = tier.ridgeColor
        )

        // Optional Tier Number Label (T1 - T6)
        if (textMeasurer != null) {
            val label = "T${tier.tierNumber}"
            val fontSizeSp = (hexRadius * 0.25f).coerceIn(8f, 13f).sp
            val measured = textMeasurer.measure(
                text = label,
                style = TextStyle(
                    color = Color.White.copy(alpha = 0.95f),
                    fontSize = fontSizeSp,
                    fontWeight = FontWeight.Black
                )
            )
            drawText(
                textLayoutResult = measured,
                topLeft = Offset(
                    x = liftedCenter.x - measured.size.width / 2f,
                    y = liftedCenter.y + hexRadius * 0.24f
                )
            )
        }
    }
}

fun DrawScope.drawEmbossedSymbol(
    symbolType: SymbolType,
    center: Offset,
    symbolRadius: Float,
    fillColor: Color
) {
    val highlightOffset = Offset(center.x, center.y + symbolRadius * 0.08f)
    when (symbolType) {
        SymbolType.HEART -> {
            val highlightPath = createHeartPath(highlightOffset, symbolRadius)
            val mainPath = createHeartPath(center, symbolRadius)
            drawPath(
                path = highlightPath,
                color = Color.White.copy(alpha = 0.45f),
                style = Stroke(width = 1.5f)
            )
            drawPath(path = mainPath, color = fillColor, style = Fill)
        }
        SymbolType.STAR5 -> {
            val highlightPath = createStar5Path(highlightOffset, symbolRadius)
            val mainPath = createStar5Path(center, symbolRadius)
            drawPath(
                path = highlightPath,
                color = Color.White.copy(alpha = 0.45f),
                style = Stroke(width = 1.5f)
            )
            drawPath(path = mainPath, color = fillColor, style = Fill)
        }
        SymbolType.CLOVER -> {
            val leafR = symbolRadius * 0.38f
            val topLeaf = Offset(center.x, center.y - symbolRadius * 0.36f)
            val leftLeaf = Offset(center.x - symbolRadius * 0.35f, center.y + symbolRadius * 0.06f)
            val rightLeaf = Offset(center.x + symbolRadius * 0.35f, center.y + symbolRadius * 0.06f)

            listOf(topLeaf, leftLeaf, rightLeaf).forEach { leaf ->
                drawCircle(
                    color = Color.White.copy(alpha = 0.4f),
                    radius = leafR,
                    center = Offset(leaf.x, leaf.y + 1.2f),
                    style = Stroke(width = 1.4f)
                )
                drawCircle(color = fillColor, radius = leafR, center = leaf)
            }
            val stemPath = Path().apply {
                moveTo(center.x, center.y + symbolRadius * 0.08f)
                quadraticTo(
                    center.x,
                    center.y + symbolRadius * 0.72f,
                    center.x + symbolRadius * 0.28f,
                    center.y + symbolRadius * 0.85f
                )
            }
            drawPath(
                path = stemPath,
                color = fillColor,
                style = Stroke(width = symbolRadius * 0.2f, cap = StrokeCap.Round)
            )
        }
        SymbolType.STAR6 -> {
            val triUp = createTrianglePath(center, symbolRadius, pointUp = true)
            val triDown = createTrianglePath(center, symbolRadius, pointUp = false)
            val triUpHi = createTrianglePath(highlightOffset, symbolRadius, pointUp = true)
            val triDownHi = createTrianglePath(highlightOffset, symbolRadius, pointUp = false)
            drawPath(triUpHi, Color.White.copy(alpha = 0.42f), style = Stroke(width = 1.4f))
            drawPath(triDownHi, Color.White.copy(alpha = 0.42f), style = Stroke(width = 1.4f))
            drawPath(triUp, fillColor, style = Fill)
            drawPath(triDown, fillColor, style = Fill)
        }
        SymbolType.MOON -> {
            val moonHi = createCrescentPath(highlightOffset, symbolRadius)
            val moonMain = createCrescentPath(center, symbolRadius)
            drawPath(moonHi, Color.White.copy(alpha = 0.45f), style = Stroke(width = 1.5f))
            drawPath(moonMain, fillColor, style = Fill)
        }
        SymbolType.CROWN -> {
            val crownHi = createCrownPath(highlightOffset, symbolRadius)
            val crownMain = createCrownPath(center, symbolRadius)
            drawPath(crownHi, Color.White.copy(alpha = 0.5f), style = Stroke(width = 1.6f))
            drawPath(crownMain, fillColor, style = Fill)
            drawRoundRect(
                color = fillColor,
                topLeft = Offset(center.x - symbolRadius * 0.72f, center.y + symbolRadius * 0.48f),
                size = Size(symbolRadius * 1.44f, symbolRadius * 0.22f),
                cornerRadius = CornerRadius(2f, 2f)
            )
        }
    }
}

private fun createHeartPath(c: Offset, r: Float): Path = Path().apply {
    moveTo(c.x, c.y + r * 0.82f)
    cubicTo(
        c.x - r * 0.95f, c.y + r * 0.22f,
        c.x - r * 0.95f, c.y - r * 0.72f,
        c.x, c.y - r * 0.25f
    )
    cubicTo(
        c.x + r * 0.95f, c.y - r * 0.72f,
        c.x + r * 0.95f, c.y + r * 0.22f,
        c.x, c.y + r * 0.82f
    )
    close()
}

private fun createStar5Path(c: Offset, r: Float): Path = Path().apply {
    val innerR = r * 0.44f
    for (i in 0 until 10) {
        val rad = ((i * 36.0 - 90.0) * PI / 180.0)
        val currR = if (i % 2 == 0) r else innerR
        val px = (c.x + currR * cos(rad)).toFloat()
        val py = (c.y + currR * sin(rad)).toFloat()
        if (i == 0) moveTo(px, py) else lineTo(px, py)
    }
    close()
}

private fun createTrianglePath(c: Offset, r: Float, pointUp: Boolean): Path = Path().apply {
    val offsetDeg = if (pointUp) -90.0 else 90.0
    for (i in 0..2) {
        val rad = ((i * 120.0 + offsetDeg) * PI / 180.0)
        val px = (c.x + r * 0.92f * cos(rad)).toFloat()
        val py = (c.y + r * 0.92f * sin(rad)).toFloat()
        if (i == 0) moveTo(px, py) else lineTo(px, py)
    }
    close()
}

private fun createCrescentPath(c: Offset, r: Float): Path = Path().apply {
    moveTo(c.x + r * 0.35f, c.y - r * 0.78f)
    cubicTo(
        c.x - r * 0.65f, c.y - r * 0.75f,
        c.x - r * 0.85f, c.y + r * 0.65f,
        c.x + r * 0.45f, c.y + r * 0.82f
    )
    cubicTo(
        c.x - r * 0.12f, c.y + r * 0.45f,
        c.x - r * 0.12f, c.y - r * 0.35f,
        c.x + r * 0.35f, c.y - r * 0.78f
    )
    close()
}

private fun createCrownPath(c: Offset, r: Float): Path = Path().apply {
    moveTo(c.x - r * 0.78f, c.y + r * 0.36f)
    lineTo(c.x - r * 0.68f, c.y - r * 0.35f)
    lineTo(c.x - r * 0.28f, c.y + r * 0.04f)
    lineTo(c.x, c.y - r * 0.68f)
    lineTo(c.x + r * 0.28f, c.y + r * 0.04f)
    lineTo(c.x + r * 0.68f, c.y - r * 0.35f)
    lineTo(c.x + r * 0.78f, c.y + r * 0.36f)
    close()
}

@Composable
fun HexTileGraphic(
    tier: TileTier,
    modifier: Modifier = Modifier,
    showTierLabel: Boolean = false
) {
    val measurer = rememberTextMeasurer()
    Canvas(modifier = modifier) {
        val hexRadius = (size.minDimension * 0.42f).coerceAtLeast(8f)
        val center = Offset(size.width / 2f, size.height * 0.46f)
        draw25DHexTile(
            center = center,
            hexRadius = hexRadius,
            tier = tier,
            textMeasurer = if (showTierLabel) measurer else null
        )
    }
}

@Composable
fun HeroCrownHexLogo(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "heroFloat")
    val floatOffset by infiniteTransition.animateFloat(
        initialValue = -5f,
        targetValue = 5f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "floatY"
    )

    Box(
        modifier = modifier
            .size(136.dp)
            .offset(y = floatOffset.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2f, size.height * 0.47f)
            val hexR = size.minDimension * 0.41f
            val extrusion = hexR * 0.18f

            // Extruded base bevel
            val baseHex = buildHexagonPath(Offset(center.x, center.y + extrusion), hexR)
            drawPath(
                path = baseHex,
                brush = Brush.verticalGradient(
                    colors = listOf(QuestColors.HeroHexDeep, QuestColors.HeroHexDark)
                )
            )
            drawPath(baseHex, Color.Black.copy(alpha = 0.6f), style = Stroke(width = 2f))

            // Glossy Rose-Ruby Top Face
            val topHex = buildHexagonPath(center, hexR)
            drawPath(
                path = topHex,
                brush = Brush.linearGradient(
                    colors = listOf(
                        QuestColors.HeroHexLight,
                        QuestColors.HeroHexMid,
                        QuestColors.HeroHexDeep,
                        QuestColors.HeroHexDark
                    ),
                    start = Offset(center.x - hexR, center.y - hexR),
                    end = Offset(center.x + hexR, center.y + hexR)
                )
            )
            drawPath(topHex, Color.White.copy(alpha = 0.6f), style = Stroke(width = 3f))

            // Inner Specular Rim
            val innerHex = buildHexagonPath(center, hexR * 0.86f)
            drawPath(
                path = innerHex,
                color = Color.White.copy(alpha = 0.4f),
                style = Stroke(
                    width = 2f,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(50f, 60f), 0f)
                )
            )

            // Golden Crown Emblem
            drawEmbossedSymbol(
                symbolType = SymbolType.CROWN,
                center = center,
                symbolRadius = hexR * 0.44f,
                fillColor = QuestColors.GoldLight
            )
        }

        // Top-right Amber Satellite Gem
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .size(34.dp)
                .rotate(12f)
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(QuestColors.GoldLight, MaterialTheme.colorScheme.secondary)
                    ),
                    shape = RoundedCornerShape(10.dp)
                )
                .border(1.dp, Color.White.copy(alpha = 0.6f), RoundedCornerShape(10.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "▲",
                color = MaterialTheme.colorScheme.onSecondary,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Black
            )
        }

        // Bottom-left Emerald Satellite Gem
        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .size(34.dp)
                .rotate(-12f)
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(QuestColors.EmeraldLight, QuestColors.EmeraldAccent)
                    ),
                    shape = RoundedCornerShape(10.dp)
                )
                .border(1.dp, Color.White.copy(alpha = 0.6f), RoundedCornerShape(10.dp)),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .background(MaterialTheme.colorScheme.onPrimary, CircleShape)
            )
        }
    }
}

@Composable
fun OceanBackdrop(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    Box(modifier = modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.bg_ocean_quest),
            contentDescription = stringResource(R.string.cd_ocean_background),
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            QuestColors.SkySunburstTop,
                            Color.Transparent,
                            QuestColors.OceanWashBottom
                        )
                    )
                )
        )
        content()
    }
}

@Composable
fun DifficultyPill(
    difficulty: String,
    modifier: Modifier = Modifier
) {
    val (bgColor, borderColor, textColor) = when (difficulty) {
        "Normal", "Tutorial" -> Triple(
            QuestColors.EmeraldContainer,
            QuestColors.EmeraldAccent.copy(alpha = 0.5f),
            QuestColors.EmeraldLight
        )
        "Hard" -> Triple(
            QuestColors.AmberContainer,
            QuestColors.GoldStar.copy(alpha = 0.5f),
            QuestColors.GoldLight
        )
        "Hard+" -> Triple(
            QuestColors.OrangeContainer,
            QuestColors.OrangeAccent.copy(alpha = 0.5f),
            QuestColors.OrangeAccent
        )
        "Very Hard" -> Triple(
            QuestColors.RoseContainer,
            QuestColors.RoseVivid.copy(alpha = 0.5f),
            QuestColors.RoseAccent
        )
        "Expert" -> Triple(
            QuestColors.PurpleContainer,
            QuestColors.PurpleAccent.copy(alpha = 0.5f),
            QuestColors.PurpleAccent
        )
        "Master" -> Triple(
            QuestColors.AmberContainer,
            QuestColors.GoldLight.copy(alpha = 0.65f),
            QuestColors.GoldLight
        )
        else -> Triple(
            QuestColors.FrostedHudCard,
            MaterialTheme.colorScheme.outline,
            MaterialTheme.colorScheme.onSurfaceVariant
        )
    }

    Surface(
        modifier = modifier,
        color = bgColor,
        shape = CircleShape,
        border = BorderStroke(1.dp, borderColor)
    ) {
        Text(
            text = difficulty.uppercase(),
            color = textColor,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Black,
            modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
        )
    }
}
