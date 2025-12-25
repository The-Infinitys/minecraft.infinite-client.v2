package org.infinite.libs.graphics.graphics2d.system

import net.minecraft.client.gui.GuiGraphics
import org.infinite.libs.graphics.graphics2d.minecraft.fillQuad
import org.infinite.libs.graphics.graphics2d.minecraft.fillTriangle
import org.infinite.libs.graphics.graphics2d.system.PointPair.Companion.calculateOffsets

class TriangleRenderer(
    private val guiGraphics: GuiGraphics,
) {
    /**
     * 三角形を塗りつぶす（各頂点の色を指定可能）
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
        guiGraphics.fillTriangle(x0, y0, x1, y1, x2, y2, col0, col1, col2)
    }

    /**
     * 単一色で三角形を塗りつぶす
     */
    fun fillTriangle(
        x0: Float,
        y0: Float,
        x1: Float,
        y1: Float,
        x2: Float,
        y2: Float,
        color: Int,
    ) = fillTriangle(x0, y0, x1, y1, x2, y2, color, color, color)

    /**
     * 中心点から内外にオフセットして、三角形の枠線を描画する
     */
    fun strokeTriangle(
        x0: Float,
        y0: Float,
        x1: Float,
        y1: Float,
        x2: Float,
        y2: Float,
        color: Int,
        strokeWidth: Float,
    ) {
        val halfWidth = strokeWidth / 2f

        // 1. 重心 (Centroid) を計算
        val cx = (x0 + x1 + x2) / 3f
        val cy = (y0 + y1 + y2) / 3f

        // 2. 各頂点について、重心からの向きに沿って内外のペア（計6点）を生成
        val p0 = calculateOffsets(x0, y0, cx, cy, halfWidth)
        val p1 = calculateOffsets(x1, y1, cx, cy, halfWidth)
        val p2 = calculateOffsets(x2, y2, cx, cy, halfWidth)

        // 3. 3枚の Quad で枠線の各辺を描画
        // 辺 0-1
        drawStrokeEdge(p0, p1, color)
        // 辺 1-2
        drawStrokeEdge(p1, p2, color)
        // 辺 2-0
        drawStrokeEdge(p2, p0, color)
    }

    /**
     * 2つの頂点ペア（内・外）を繋いで1つの辺を描画する
     */
    private fun drawStrokeEdge(start: PointPair, end: PointPair, color: Int) {
        guiGraphics.fillQuad(
            start.ox, start.oy, // 外側開始
            start.ix, start.iy, // 内側開始
            end.ix, end.iy, // 内側終了
            end.ox, end.oy, // 外側終了
            color, color, color, color,
        )
    }
}
