package org.infinite.libs.graphics.graphics2d.structs

sealed interface RenderCommand {
    val zIndex: Int

    // 矩形の枠線
    data class StrokeRect(
        val x: Float,
        val y: Float,
        val width: Float,
        val height: Float,
        val strokeWidth: Float,
        val color: Int,
        override val zIndex: Int,
    ) : RenderCommand

    // 矩形の塗りつぶし
    data class FillRect(
        val x: Float,
        val y: Float,
        val width: Float,
        val height: Float,
        val color: Int,
        override val zIndex: Int,
    ) : RenderCommand

    // 四角形の塗りつぶし
    data class FillQuad(
        val x0: Float,
        val y0: Float,
        val x1: Float,
        val y1: Float,
        val x2: Float,
        val y2: Float,
        val x3: Float,
        val y3: Float,
        val col0: Int,
        val col1: Int,
        val col2: Int,
        val col3: Int,
        override val zIndex: Int,
    ) : RenderCommand

    /**
     * 四角形の枠線を描画するコマンド
     */
    data class StrokeQuad(
        val x0: Float,
        val y0: Float,
        val x1: Float,
        val y1: Float,
        val x2: Float,
        val y2: Float,
        val x3: Float,
        val y3: Float,
        val strokeWidth: Float,
        val color: Int,
        override val zIndex: Int,
    ) : RenderCommand

    // 三角形の塗りつぶし
    data class FillTriangle(
        val x0: Float,
        val y0: Float,
        val x1: Float,
        val y1: Float,
        val x2: Float,
        val y2: Float,
        val col0: Int,
        val col1: Int,
        val col2: Int,
        override val zIndex: Int,
    ) : RenderCommand

    /**
     * 三角形の枠線を描画するコマンド
     */
    data class StrokeTriangle(
        val x0: Float,
        val y0: Float,
        val x1: Float,
        val y1: Float,
        val x2: Float,
        val y2: Float,
        val strokeWidth: Float,
        val color: Int,
        override val zIndex: Int,
    ) : RenderCommand
}
