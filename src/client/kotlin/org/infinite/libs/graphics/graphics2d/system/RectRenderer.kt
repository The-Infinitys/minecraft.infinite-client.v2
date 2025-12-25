package org.infinite.libs.graphics.graphics2d.system

import net.minecraft.client.gui.GuiGraphics
import org.infinite.libs.graphics.graphics2d.minecraft.fill

class RectRenderer(
    private val guiGraphics: GuiGraphics,
) {
    /**
     * 矩形の枠線を描画する (Canvas API 準拠の拡大幅)
     */
    fun strokeRect(
        x: Float,
        y: Float,
        w: Float,
        h: Float,
        color: Int,
        strokeWidth: Float,
    ) {
        val halfWidth = strokeWidth / 2f

        // Canvasの挙動に合わせて、境界線を中心に外側と内側に半分ずつ広げる
        val xMin = x - halfWidth
        val yMin = y - halfWidth
        val xMax = x + w + halfWidth
        val yMax = y + h + halfWidth

        // 上辺
        guiGraphics.fill(xMin, yMin, xMax, yMin + strokeWidth, color)
        // 下辺
        guiGraphics.fill(xMin, yMax - strokeWidth, xMax, yMax, color)
        // 左辺 (上下の角を重複させない場合は yMin + strokeWidth ～ yMax - strokeWidth に調整)
        guiGraphics.fill(xMin, yMin + strokeWidth, xMin + strokeWidth, yMax - strokeWidth, color)
        // 右辺
        guiGraphics.fill(xMax - strokeWidth, yMin + strokeWidth, xMax, yMax - strokeWidth, color)
    }

    fun fillRect(
        x: Float,
        y: Float,
        w: Float,
        h: Float,
        color: Int,
    ) = guiGraphics.fill(x, y, x + w, y + h, color)
}
