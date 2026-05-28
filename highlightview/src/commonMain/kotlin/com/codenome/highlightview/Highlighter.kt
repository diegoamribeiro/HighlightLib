package com.codenome.highlightview

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

enum class ArrowDirection {
    TOP,
    BOTTOM,
    LEFT,
    RIGHT
}

data class TooltipConfig(
    val title: String,
    val backgroundColor: Color = Color.Black,
    val textColor: Color = Color.White,
    val textSize: TextUnit = 12.sp,
    val textPadding: Dp = 16.dp,
    val lineSpacing: TextUnit = 4.sp,
    val cornerRadius: Dp = 12.dp,
    val margin: Dp = 10.dp,
    val arrowDirection: ArrowDirection? = null,
    val arrowWidth: Dp = 16.dp,
    val arrowHeight: Dp = 12.dp
)

data class HighlighterConfig(
    val overlayColor: Color = Color(0x99000000),
    val overlayRadius: Dp = 16.dp,
    val overlayPadding: Dp = 8.dp,
    val tooltipConfig: TooltipConfig
)

class HighlightState {
    var activeTargetKey by mutableStateOf<Any?>(null)
        private set
    var config by mutableStateOf<HighlighterConfig?>(null)
        internal set
    var onDismissListener by mutableStateOf<(() -> Unit)?>(null)
        internal set

    var coordinatesUpdateTrigger by mutableStateOf(0)
        private set

    private val targets = mutableMapOf<Any, LayoutCoordinates>()

    fun registerTarget(key: Any, coordinates: LayoutCoordinates) {
        targets[key] = coordinates
        if (activeTargetKey == key) {
            coordinatesUpdateTrigger++
        }
    }

    fun unregisterTarget(key: Any) {
        targets.remove(key)
        if (activeTargetKey == key) {
            dismiss()
        }
    }

    fun show(key: Any, config: HighlighterConfig, onDismiss: (() -> Unit)? = null) {
        activeTargetKey = key
        this.config = config
        this.onDismissListener = onDismiss
        coordinatesUpdateTrigger++
    }

    fun dismiss() {
        activeTargetKey = null
        config = null
        onDismissListener?.invoke()
        onDismissListener = null
    }

    val activeTargetCoordinates: LayoutCoordinates?
        get() {
            val _trigger = coordinatesUpdateTrigger
            val key = activeTargetKey ?: return null
            val coords = targets[key] ?: return null
            return if (coords.isAttached) coords else null
        }
}

@Composable
fun rememberHighlightState(): HighlightState {
    return remember { HighlightState() }
}

fun Modifier.highlightTarget(
    key: Any,
    state: HighlightState
): Modifier = this.onGloballyPositioned { coordinates ->
    state.registerTarget(key, coordinates)
}

@Composable
fun HighlightOverlay(
    state: HighlightState,
    modifier: Modifier = Modifier
) {
    val activeCoords = state.activeTargetCoordinates ?: return
    val config = state.config ?: return

    var overlayCoords by remember { mutableStateOf<LayoutCoordinates?>(null) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .onGloballyPositioned { coordinates ->
                overlayCoords = coordinates
            }
            .pointerInput(Unit) {
                detectTapGestures {
                    state.dismiss()
                }
            }
    ) {
        val currentOverlayCoords = overlayCoords ?: return@Box
        if (!activeCoords.isAttached || !currentOverlayCoords.isAttached) return@Box

        val localTopLeft = currentOverlayCoords.localPositionOf(activeCoords, Offset.Zero)
        val localBottomRight = currentOverlayCoords.localPositionOf(
            activeCoords,
            Offset(activeCoords.size.width.toFloat(), activeCoords.size.height.toFloat())
        )
        val bounds = Rect(
            left = localTopLeft.x,
            top = localTopLeft.y,
            right = localBottomRight.x,
            bottom = localBottomRight.y
        )

        // Desenha o fundo preto translúcido com o "cutout" transparente
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer(compositingStrategy = CompositingStrategy.Offscreen)
        ) {
            drawRect(color = config.overlayColor)

            val paddingPx = config.overlayPadding.toPx()
            val cutoutLeft = bounds.left - paddingPx
            val cutoutTop = bounds.top - paddingPx
            val cutoutRight = bounds.right + paddingPx
            val cutoutBottom = bounds.bottom + paddingPx
            
            val cutoutWidth = cutoutRight - cutoutLeft
            val cutoutHeight = cutoutBottom - cutoutTop
            val radiusPx = config.overlayRadius.toPx()

            drawRoundRect(
                color = Color.Transparent,
                topLeft = Offset(cutoutLeft, cutoutTop),
                size = Size(cutoutWidth, cutoutHeight),
                cornerRadius = CornerRadius(radiusPx, radiusPx),
                blendMode = BlendMode.Clear
            )
        }

        // Renderiza o tooltip posicionado próximo ao cutout
        TooltipLayout(
            targetBounds = bounds,
            config = config.tooltipConfig,
            overlayPadding = config.overlayPadding
        )
    }
}

@Composable
private fun TooltipLayout(
    targetBounds: Rect,
    config: TooltipConfig,
    overlayPadding: Dp
) {
    var resolvedDirection by remember { mutableStateOf(ArrowDirection.TOP) }
    var arrowOffsetX by remember { mutableStateOf(0f) }
    var arrowOffsetY by remember { mutableStateOf(0f) }

    Layout(
        content = {
            TooltipBalloon(
                config = config,
                arrowDirection = resolvedDirection,
                arrowOffsetX = arrowOffsetX,
                arrowOffsetY = arrowOffsetY
            )
        }
    ) { measurables, constraints ->
        val placeable = measurables.first().measure(constraints.copy(minWidth = 0, minHeight = 0))
        val tooltipWidth = placeable.width
        val tooltipHeight = placeable.height

        val density = this
        val screenWidth = constraints.maxWidth
        val screenHeight = constraints.maxHeight

        val paddingPx = with(density) { overlayPadding.toPx() }
        val marginPx = with(density) { config.margin.toPx() }
        val arrowHeightPx = with(density) { config.arrowHeight.toPx() }
        val cornerRadiusPx = with(density) { config.cornerRadius.toPx() }
        val arrowWidthPx = with(density) { config.arrowWidth.toPx() }

        val cutoutLeft = targetBounds.left - paddingPx
        val cutoutTop = targetBounds.top - paddingPx
        val cutoutRight = targetBounds.right + paddingPx
        val cutoutBottom = targetBounds.bottom + paddingPx
        
        val cutoutWidth = cutoutRight - cutoutLeft
        val cutoutHeight = cutoutBottom - cutoutTop

        val targetCenterX = targetBounds.left + targetBounds.width / 2f
        val targetCenterY = targetBounds.top + targetBounds.height / 2f

        // Determina a direção automaticamente se não for especificada
        var direction = config.arrowDirection
        if (direction == null) {
            val spaceTop = cutoutTop - tooltipHeight - arrowHeightPx - marginPx
            direction = if (spaceTop >= 0) {
                ArrowDirection.TOP
            } else {
                val spaceBottom = screenHeight - (cutoutBottom + tooltipHeight + arrowHeightPx + marginPx)
                if (spaceBottom >= 0) {
                    ArrowDirection.BOTTOM
                } else {
                    val spaceRight = screenWidth - (cutoutRight + tooltipWidth + arrowHeightPx + marginPx)
                    if (spaceRight >= 0) {
                        ArrowDirection.RIGHT
                    } else {
                        ArrowDirection.LEFT
                    }
                }
            }
        }

        // Calcula a posição do balão
        var x = 0f
        var y = 0f

        when (direction) {
            ArrowDirection.TOP -> {
                x = targetCenterX - tooltipWidth / 2f
                y = cutoutTop - tooltipHeight - arrowHeightPx
            }
            ArrowDirection.BOTTOM -> {
                x = targetCenterX - tooltipWidth / 2f
                y = cutoutBottom + arrowHeightPx
            }
            ArrowDirection.LEFT -> {
                x = cutoutLeft - tooltipWidth - arrowHeightPx
                y = targetCenterY - tooltipHeight / 2f
            }
            ArrowDirection.RIGHT -> {
                x = cutoutRight + arrowHeightPx
                y = targetCenterY - tooltipHeight / 2f
            }
        }

        // Garante que o balão fique visível na tela
        x = x.coerceIn(marginPx, screenWidth - tooltipWidth - marginPx)
        y = y.coerceIn(marginPx, screenHeight - tooltipHeight - marginPx)

        // Calcula a posição da ponta da seta em relação ao balão
        val localArrowOffsetX = (targetCenterX - x).coerceIn(
            cornerRadiusPx + arrowWidthPx / 2f,
            tooltipWidth - cornerRadiusPx - arrowWidthPx / 2f
        )
        val localArrowOffsetY = (targetCenterY - y).coerceIn(
            cornerRadiusPx + arrowWidthPx / 2f,
            tooltipHeight - cornerRadiusPx - arrowWidthPx / 2f
        )

        resolvedDirection = direction
        arrowOffsetX = localArrowOffsetX
        arrowOffsetY = localArrowOffsetY

        layout(screenWidth, screenHeight) {
            placeable.place(x.toInt(), y.toInt())
        }
    }
}

@Composable
private fun TooltipBalloon(
    config: TooltipConfig,
    arrowDirection: ArrowDirection,
    arrowOffsetX: Float,
    arrowOffsetY: Float,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current
    val cornerRadiusPx = with(density) { config.cornerRadius.toPx() }
    val arrowWidthPx = with(density) { config.arrowWidth.toPx() }
    val arrowHeightPx = with(density) { config.arrowHeight.toPx() }
    val balloonColor = config.backgroundColor

    Box(
        modifier = modifier
            .drawBehind {
                // Desenha o corpo principal do balão
                drawRoundRect(
                    color = balloonColor,
                    cornerRadius = CornerRadius(cornerRadiusPx, cornerRadiusPx)
                )

                // Desenha a seta apontando para o alvo
                val arrowPath = Path().apply {
                    when (arrowDirection) {
                        ArrowDirection.TOP -> {
                            moveTo(arrowOffsetX - arrowWidthPx / 2f, size.height)
                            lineTo(arrowOffsetX, size.height + arrowHeightPx)
                            lineTo(arrowOffsetX + arrowWidthPx / 2f, size.height)
                            close()
                        }
                        ArrowDirection.BOTTOM -> {
                            moveTo(arrowOffsetX - arrowWidthPx / 2f, 0f)
                            lineTo(arrowOffsetX, -arrowHeightPx)
                            lineTo(arrowOffsetX + arrowWidthPx / 2f, 0f)
                            close()
                        }
                        ArrowDirection.LEFT -> {
                            moveTo(size.width, arrowOffsetY - arrowWidthPx / 2f)
                            lineTo(size.width + arrowHeightPx, arrowOffsetY)
                            lineTo(size.width, arrowOffsetY + arrowWidthPx / 2f)
                            close()
                        }
                        ArrowDirection.RIGHT -> {
                            moveTo(0f, arrowOffsetY - arrowWidthPx / 2f)
                            lineTo(-arrowHeightPx, arrowOffsetY)
                            lineTo(0f, arrowOffsetY + arrowWidthPx / 2f)
                            close()
                        }
                    }
                }
                drawPath(arrowPath, color = balloonColor)
            }
            .padding(config.textPadding)
    ) {
        Text(
            text = config.title,
            color = config.textColor,
            fontSize = config.textSize,
            lineHeight = (config.textSize.value + config.lineSpacing.value).sp,
            textAlign = TextAlign.Center,
            style = TextStyle.Default
        )
    }
}
