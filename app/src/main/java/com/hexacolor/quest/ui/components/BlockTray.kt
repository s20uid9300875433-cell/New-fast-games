package com.hexacolor.quest.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.hexacolor.quest.R
import com.hexacolor.quest.model.TrayBlock
import com.hexacolor.quest.ui.theme.QuestColors

@Composable
fun BlockTray(
    blocks: List<TrayBlock>,
    selectedBlockId: String?,
    draggingBlockId: String?,
    disabled: Boolean,
    onSelectBlock: (String) -> Unit,
    onDragStart: (TrayBlock, Offset) -> Unit,
    onDragMove: (Offset) -> Unit,
    onDragEnd: (Offset) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("block_tray"),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 6.dp, vertical = 2.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.game_tray_title).uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = QuestColors.MutedText,
                fontWeight = FontWeight.ExtraBold
            )
            Text(
                text = stringResource(R.string.game_tray_hint),
                style = MaterialTheme.typography.labelSmall,
                color = QuestColors.SubtleText
            )
        }

        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = QuestColors.FrostedHudCard,
            shape = RoundedCornerShape(24.dp),
            border = BorderStroke(1.dp, QuestColors.GlassWhiteBorder)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                blocks.forEachIndexed { index, block ->
                    TraySlotItem(
                        index = index,
                        block = block,
                        isSelected = selectedBlockId == block.id,
                        isDragging = draggingBlockId == block.id,
                        disabled = disabled,
                        onSelectBlock = onSelectBlock,
                        onDragStart = onDragStart,
                        onDragMove = onDragMove,
                        onDragEnd = onDragEnd,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun TraySlotItem(
    index: Int,
    block: TrayBlock,
    isSelected: Boolean,
    isDragging: Boolean,
    disabled: Boolean,
    onSelectBlock: (String) -> Unit,
    onDragStart: (TrayBlock, Offset) -> Unit,
    onDragMove: (Offset) -> Unit,
    onDragEnd: (Offset) -> Unit,
    modifier: Modifier = Modifier
) {
    var slotRootPos by remember { mutableStateOf(Offset.Zero) }
    var lastDragRootPos by remember { mutableStateOf(Offset.Zero) }

    if (block.isUsed) {
        Box(
            modifier = modifier
                .height(74.dp)
                .alpha(0.35f)
                .background(
                    color = MaterialTheme.colorScheme.background.copy(alpha = 0.45f),
                    shape = RoundedCornerShape(16.dp)
                )
                .border(
                    width = 1.dp,
                    color = QuestColors.GlassWhiteBorder,
                    shape = RoundedCornerShape(16.dp)
                )
                .testTag("tray_slot_used_$index"),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .border(
                        width = 1.dp,
                        color = QuestColors.MutedText.copy(alpha = 0.5f),
                        shape = CircleShape
                    )
            )
        }
        return
    }

    val containerBg = if (isSelected) {
        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.85f)
    } else {
        QuestColors.FrostedHudStrong
    }
    val borderColor = if (isSelected) {
        QuestColors.CyanGlow
    } else {
        QuestColors.GlassWhiteBorder
    }

    Surface(
        modifier = modifier
            .height(74.dp)
            .minimumInteractiveComponentSize()
            .offset(y = if (isSelected) (-3).dp else 0.dp)
            .alpha(if (isDragging) 0.35f else 1f)
            .onGloballyPositioned { coords ->
                slotRootPos = coords.positionInRoot()
            }
            .clickable(enabled = !disabled) {
                onSelectBlock(block.id)
            }
            .pointerInput(block.id, disabled) {
                if (disabled) return@pointerInput
                detectDragGestures(
                    onDragStart = { startLocal ->
                        val rootPt = slotRootPos + startLocal
                        lastDragRootPos = rootPt
                        onDragStart(block, rootPt)
                    },
                    onDrag = { change, _ ->
                        change.consume()
                        val rootPt = slotRootPos + change.position
                        lastDragRootPos = rootPt
                        onDragMove(rootPt)
                    },
                    onDragEnd = {
                        onDragEnd(lastDragRootPos)
                    },
                    onDragCancel = {
                        onDragEnd(Offset.Zero)
                    }
                )
            }
            .testTag("tray_slot_$index")
            .semantics {
                contentDescription = "Tray block Tier ${block.tier.tierNumber} ${block.tier.tierName}"
            },
        color = containerBg,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(if (isSelected) 2.dp else 1.dp, borderColor)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(vertical = 4.dp)
        ) {
            HexTileGraphic(
                tier = block.tier,
                modifier = Modifier.size(44.dp)
            )
            Text(
                text = "T${block.tier.tierNumber} ${block.tier.shortName}",
                style = MaterialTheme.typography.labelSmall,
                color = Color.White,
                fontWeight = FontWeight.Black
            )
        }
    }
}
