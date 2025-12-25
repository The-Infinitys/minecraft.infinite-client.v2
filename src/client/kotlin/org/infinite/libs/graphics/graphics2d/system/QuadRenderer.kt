package org.infinite.libs.graphics.graphics2d.system

import net.minecraft.client.gui.GuiGraphics
import org.infinite.libs.graphics.graphics2d.minecraft.fillQuad
import org.infinite.libs.graphics.graphics2d.system.PointPair.Companion.calculateOffsets

class QuadRenderer(
    private val guiGraphics: GuiGraphics,
) {
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
        guiGraphics.fillQuad(x0, y0, x1, y1, x2, y2, x3, y3, col0, col1, col2, col3)
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
        color: Int,
        strokeWidth: Float,
    ) {
        val halfWidth = strokeWidth / 2f

        // 1. 中心座標（重心）を計算
        val cx = (x0 + x1 + x2 + x3) / 4f
        val cy = (y0 + y1 + x2 + x3) / 4f // ※y2, y3 のタイポ修正

        // 2. 各頂点について、中心からの方向ベクトルを正規化して内外の点を求める
        // (innerX, innerY), (outerX, outerY)
        val p0 = calculateOffsets(x0, y0, cx, cy, halfWidth)
        val p1 = calculateOffsets(x1, y1, cx, cy, halfWidth)
        val p2 = calculateOffsets(x2, y2, cx, cy, halfWidth)
        val p3 = calculateOffsets(x3, y3, cx, cy, halfWidth)

        // 3. 4つの「辺」をそれぞれ fillQuad で描画 (計8座標を使用)
        // 辺 0-1
        fillQuad(p0.ox, p0.oy, p0.ix, p0.iy, p1.ix, p1.iy, p1.ox, p1.oy, color)
        // 辺 1-2
        fillQuad(p1.ox, p1.oy, p1.ix, p1.iy, p2.ix, p2.iy, p2.ox, p2.oy, color)
        // 辺 2-3
        fillQuad(p2.ox, p2.oy, p2.ix, p2.iy, p3.ix, p3.iy, p3.ox, p3.oy, color)
        // 辺 3-0
        fillQuad(p3.ox, p3.oy, p3.ix, p3.iy, p0.ix, p0.iy, p0.ox, p0.oy, color)
    }

    private fun fillQuad(
        x0: Float,
        y0: Float,
        x1: Float,
        y1: Float,
        x2: Float,
        y2: Float,
        x3: Float,
        y3: Float,
        color: Int,
    ) {
        fillQuad(x0, y0, x1, y1, x2, y2, x3, y3, color, color, color, color)
    }
}
