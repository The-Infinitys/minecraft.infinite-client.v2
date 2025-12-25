package org.infinite.libs.graphics

import net.minecraft.client.DeltaTracker
import org.infinite.libs.graphics.graphics2d.structs.RenderCommand
import org.infinite.libs.graphics.graphics2d.structs.StrokeStyle
import java.util.concurrent.PriorityBlockingQueue

/**
 * MDN CanvasRenderingContext2D API を Minecraft GuiGraphics 上に再現するクラス。
 * すべての座標指定を Float に統一。
 */
class Graphics2D(
    deltaTracker: DeltaTracker,
    var zIndex: Int = 0,
) {
    private val capturedGameDelta: Float = deltaTracker.gameTimeDeltaTicks
    private val capturedRealDelta: Float = deltaTracker.realtimeDeltaTicks

    var strokeStyle: StrokeStyle? = null

    // 塗りつぶしの色（ARGB形式のInt）
    var fillStyle: Int = 0xFFFFFFFF.toInt()

    private val commandQueue = PriorityBlockingQueue<RenderCommand>(100, compareBy { it.zIndex })

    // --- strokeRect ---

    /**
     * 指定された矩形の枠線を描画します。
     */
    fun strokeRect(
        x: Float,
        y: Float,
        width: Float,
        height: Float,
    ) {
        val style = strokeStyle ?: return
        val (strokeColor, strokeWidth) = style
        // RenderCommand側もFloat版に集約
        commandQueue.add(
            RenderCommand.StrokeRect(
                x,
                y,
                width,
                height,
                strokeWidth,
                strokeColor,
                zIndex,
            ),
        )
    }

    // --- fillRect ---

    /**
     * 現在の fillStyle を使用して矩形を塗りつぶします。
     */
    fun fillRect(
        x: Float,
        y: Float,
        width: Float,
        height: Float,
    ) {
        commandQueue.add(RenderCommand.FillRect(x, y, width, height, fillStyle, zIndex))
    }

    // --- fillQuad ---

    /**
     * 現在の fillStyle を使用して四角形を塗りつぶします。
     */
    fun fillQuad(
        x0: Float,
        y0: Float,
        x1: Float,
        y1: Float,
        x2: Float,
        y2: Float,
        x3: Float,
        y3: Float,
    ) {
        fillQuad(x0, y0, x1, y1, x2, y2, x3, y3, fillStyle, fillStyle, fillStyle, fillStyle)
    }

    /**
     * 各頂点に個別の色を指定して四角形を塗りつぶします（グラデーション）。
     */
    fun fillQuad(
        x0: Float,
        y0: Float,
        x1: Float,
        y1: Float,
        x2: Float,
        y2: Float,
        x3: Float,
        y3: Float,
        col0: Int,
        col1: Int,
        col2: Int,
        col3: Int,
    ) {
        commandQueue.add(
            RenderCommand.FillQuad(
                x0, y0, x1, y1, x2, y2, x3, y3,
                col0, col1, col2, col3,
                zIndex,
            ),
        )
    }

    // --- fillTriangle ---

    /**
     * 現在の fillStyle を使用して三角形を塗りつぶします。
     */
    fun fillTriangle(
        x0: Float,
        y0: Float,
        x1: Float,
        y1: Float,
        x2: Float,
        y2: Float,
    ) {
        fillTriangle(x0, y0, x1, y1, x2, y2, fillStyle, fillStyle, fillStyle)
    }

    /**
     * 各頂点に個別の色を指定して三角形を塗りつぶします（グラデーション）。
     */
    fun fillTriangle(
        x0: Float,
        y0: Float,
        x1: Float,
        y1: Float,
        x2: Float,
        y2: Float,
        col0: Int,
        col1: Int,
        col2: Int,
    ) {
        commandQueue.add(
            RenderCommand.FillTriangle(
                x0, y0, x1, y1, x2, y2,
                col0, col1, col2,
                zIndex,
            ),
        )
    }

    fun strokeQuad(
        x0: Float,
        y0: Float,
        x1: Float,
        y1: Float,
        x2: Float,
        y2: Float,
        x3: Float,
        y3: Float,
    ) {
        val style = strokeStyle ?: return
        commandQueue.add(
            RenderCommand.StrokeQuad(
                x0, y0, x1, y1, x2, y2, x3, y3,
                style.width, style.color, zIndex,
            ),
        )
    }

    fun strokeTriangle(x0: Float, y0: Float, x1: Float, y1: Float, x2: Float, y2: Float) {
        val style = strokeStyle ?: return
        commandQueue.add(
            RenderCommand.StrokeTriangle(
                x0, y0, x1, y1, x2, y2,
                style.width, style.color, zIndex,
            ),
        )
    }
    // --- Utilities ---

    fun gameDelta(): Float = capturedGameDelta

    fun realDelta(): Float = capturedRealDelta

    fun poll(): RenderCommand? = commandQueue.poll()
}
