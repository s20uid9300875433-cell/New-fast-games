package com.hexacolor.quest.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.IntOffset
import com.hexacolor.quest.logic.HexMath
import com.hexacolor.quest.model.BoardTile
import com.hexacolor.quest.model.HexCoord
import com.hexacolor.quest.model.HintSourceType
import com.hexacolor.quest.model.HintSuggestion
import com.hexacolor.quest.model.SparkleParticle
import com.hexacolor.quest.model.TileTier
import com.hexacolor.quest.ui.theme.QuestColors
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt

data class BoardGeometry(
    val rootBounds: Rect = Rect.Zero,
    val radius: Int = 2
) {
    fun computeHexSizePx(): Float {
        val minDim = min(rootBounds.width, rootBounds.height)
        if (minDim <= 0f) return 1f
        val horizontalSpan = sqrt(3f) * (2 * radius + 1)
        return (minDim * 0.93f) / horizontalSpan
    }

    fun localCenter(): Offset {
        return Offset(rootBounds.width / 2f, rootBounds.height / 2f)
    }

    fun rootOffsetToHex(rootPoint: Offset): HexCoord? {
        if (!rootBounds.contains(rootPoint)) return null
        val local = rootPoint - rootBounds.topLeft
        val hexSize = computeHexSizePx()
        return HexMath.pixelToHex(local, hexSize, localCenter())
    }
}

@Composable
fun HexagonBoard(
    radius: Int,
    blockedCoords: List<HexCoord>,
    tiles: List<BoardTile>,
    selectedCoord: HexCoord?,
    validTargets: List<HexCoord>,
    hint: HintSuggestion?,
    isMerging: Boolean,
    shakeTrigger: Int,
    sparkles: List<SparkleParticle>,
    hoverDropCoord: HexCoord?,
    previewTier: TileTier?,
    onCellTapped: (HexCoord) -> Unit,
    onGeometryUpdated: (BoardGeometry) -> Unit,
    modifier: Modifier = Modifier
) {
    val gridCoords = remember(radius, blockedCoords) {
        HexMath.generateGrid(radius, blockedCoords)
    }
    val gridCoordsSet = remember(gridCoords) { gridCoords.toSet() }
    val tilesMap = remember(tiles) { tiles.associateBy { it.coord } }
    val validTargetsSet = remember(validTargets) { validTargets.toSet() }
    val textMeasurer = rememberTextMeasurer()

    // Gentle shake animation on invalid moves
    val shakeOffset = remember { Animatable(0f) }
    LaunchedEffect(shakeTrigger) {
        if (shakeTrigger > 0) {
            val keyframes = listOf(-14f, 14f, -9f, 9f, -4f, 0f)
            for (target in keyframes) {
                shakeOffset.animateTo(target, tween(durationMillis = 48))
            }
        }
    }

    // Pop-merge scale animation
    val mergeScale = remember { Animatable(1f) }
    LaunchedEffect(isMerging) {
        if (isMerging) {
            mergeScale.snapTo(0.75f)
            mergeScale.animateTo(
                targetValue = 1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMedium
                )
            )
        } else {
            mergeScale.snapTo(1f)
        }
    }

    // Sparkle burst expansion progress
    val sparkleProgress = remember { Animatable(0f) }
    LaunchedEffect(sparkles) {
        if (sparkles.isNotEmpty()) {
            sparkleProgress.snapTo(0f)
            sparkleProgress.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 520, easing = FastOutSlowInEasing)
            )
        }
    }

    // Pulsing target socket animation
    val infiniteTransition = rememberInfiniteTransition(label = "boardPulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.45f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .offset { IntOffset(shakeOffset.value.roundToInt(), 0) }
            .onGloballyPositioned { coords ->
                onGeometryUpdated(
                    BoardGeometry(
                        rootBounds = coords.boundsInRoot(),
                        radius = radius
                    )
                )
            }
            .testTag("hexagon_board")
            .semantics {
                contentDescription = "Hexagon puzzle board with ${gridCoords.size} cells"
            }
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(gridCoordsSet, radius) {
                    detectTapGestures { tapOffset ->
                        val minDim = min(size.width, size.height)
                        val horizontalSpan = sqrt(3f) * (2 * radius + 1)
                        val hexSize = (minDim * 0.93f) / horizontalSpan
                        val center = Offset(size.width / 2f, size.height / 2f)
                        val tappedHex = HexMath.pixelToHex(tapOffset, hexSize, center)
                        if (tappedHex in gridCoordsSet) {
                            onCellTapped(tappedHex)
                        }
                    }
                }
        ) {
            val minDim = min(size.width, size.height)
            val horizontalSpan = sqrt(3f) * (2 * radius + 1)
            val hexSize = (minDim * 0.93f) / horizontalSpan
            val center = Offset(size.width / 2f, size.height / 2f)
            val socketRadius = hexSize - 3.5f

            // 1. Base Grid Sockets
            gridCoords.forEach { coord ->
                val pos = HexMath.hexToPixel(coord, hexSize, center)
                val isTarget = coord in validTargetsSet
                val isHintTarget = hint?.to == coord
                val isHoverDrop = hoverDropCoord == coord
                val hasTile = coord in tilesMap

                // Recessed Socket Shadow
                val shadowSocket = buildHexagonPath(Offset(pos.x, pos.y + 3f), socketRadius)
                drawPath(path = shadowSocket, color = QuestColors.SocketRecessShadow)

                // Frosted Ocean Socket Surface
                val socketPath = buildHexagonPath(pos, socketRadius)
                val socketFill = when {
                    isHoverDrop -> QuestColors.SocketFillHover
                    isTarget -> QuestColors.SocketFillTarget
                    else -> QuestColors.SocketFillDefault
                }
                drawPath(path = socketPath, color = socketFill)

                val borderColor = when {
                    isHintTarget -> QuestColors.GoldStar
                    isHoverDrop || isTarget -> QuestColors.CyanGlow
                    else -> QuestColors.SocketBorderDefault
                }
                val strokeWidth = when {
                    isHintTarget || isHoverDrop -> 3.2f
                    isTarget -> 2.4f
                    else -> 1.5f
                }
                drawPath(
                    path = socketPath,
                    color = borderColor,
                    style = Stroke(
                        width = strokeWidth,
                        pathEffect = if (isTarget && !isHoverDrop) {
                            PathEffect.dashPathEffect(floatArrayOf(10f, 6f), 0f)
                        } else {
                            null
                        }
                    )
                )

                // Pulsing Valid Target Dot
                if (isTarget && !hasTile) {
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                QuestColors.CyanGlow.copy(alpha = 0.42f * pulseAlpha),
                                Color.Transparent
                            ),
                            center = pos,
                            radius = socketRadius * 0.8f
                        ),
                        radius = socketRadius * 0.8f,
                        center = pos
                    )
                    drawCircle(
                        color = QuestColors.CyanGlow.copy(alpha = pulseAlpha),
                        radius = 6f,
                        center = pos
                    )
                    drawCircle(
                        color = QuestColors.CyanLightDot,
                        radius = 3.5f,
                        center = pos
                    )
                }

                // Golden Hint Target Ring
                if (isHintTarget) {
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                QuestColors.GoldLight.copy(alpha = 0.7f * pulseAlpha),
                                QuestColors.GoldStar.copy(alpha = 0.25f * pulseAlpha),
                                Color.Transparent
                            ),
                            center = pos,
                            radius = socketRadius * 0.85f
                        ),
                        radius = socketRadius * 0.85f,
                        center = pos
                    )
                }

                // Ghost Tile Preview when dragging over an empty socket
                if (isHoverDrop && previewTier != null && !hasTile) {
                    drawPath(
                        path = socketPath,
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                previewTier.lightColor.copy(alpha = 0.65f),
                                previewTier.color.copy(alpha = 0.65f),
                                previewTier.shadowColor.copy(alpha = 0.65f)
                            ),
                            startY = pos.y - socketRadius,
                            endY = pos.y + socketRadius
                        )
                    )
                }
            }

            // 2. Layered 2.5D Hexagonal Tiles (sorted top-to-bottom so lower rows overlap upper extrusions cleanly)
            val sortedTiles = tiles.sortedWith(compareBy({ it.coord.r }, { it.coord.q }))
            sortedTiles.forEach { tile ->
                val pos = HexMath.hexToPixel(tile.coord, hexSize, center)
                val isSelected = tile.coord == selectedCoord
                val isHintSource = hint?.sourceType == HintSourceType.BOARD && hint.from == tile.coord
                val tileRadius = if (isSelected) hexSize - 1.5f else hexSize - 3.5f
                val liftY = if (isSelected) -hexSize * 0.18f else 0f
                val scale = if (tile.isMerging) mergeScale.value else 1f

                draw25DHexTile(
                    center = pos,
                    hexRadius = tileRadius,
                    tier = tile.tier,
                    isSelected = isSelected,
                    isHintSource = isHintSource,
                    liftOffsetPx = liftY,
                    tileScale = scale,
                    textMeasurer = textMeasurer
                )
            }

            // 3. Sparkle Particle Burst
            if (sparkles.isNotEmpty()) {
                val p = sparkleProgress.value
                val alpha = (1f - p).coerceIn(0f, 1f)
                sparkles.forEach { sp ->
                    val origin = HexMath.hexToPixel(sp.coord, hexSize, center)
                    val travelDist = hexSize * 1.35f * sp.distanceFactor * p
                    val sx = origin.x + cos(sp.angleRad) * travelDist
                    val sy = origin.y + sin(sp.angleRad) * travelDist
                    drawCircle(
                        color = Color.White.copy(alpha = alpha * 0.85f),
                        radius = sp.radiusDp * (1.2f - 0.5f * p) + 2f,
                        center = Offset(sx, sy)
                    )
                    drawCircle(
                        color = sp.color.copy(alpha = alpha),
                        radius = sp.radiusDp * (1.2f - 0.4f * p),
                        center = Offset(sx, sy)
                    )
                }
            }
        }
    }
}
