package org.infinite.libs.graphics.graphics2d.system

import net.minecraft.client.gui.GuiGraphics

class RectRenderer(
    private val guiGraphics: GuiGraphics,
) {
    fun strokeRect(
        x: Int,
        y: Int,
        width: Int,
        height: Int,
        strokeColor: Int,
        strokeWidth: Int,
    ) {
        guiGraphics.fill(x, y, strokeWidth, height - strokeWidth, strokeColor)
        guiGraphics.fill(x + strokeWidth, y + height - strokeWidth, width - strokeWidth, strokeWidth, strokeColor)
        guiGraphics.fill(
            x + width - strokeWidth,
            y + strokeWidth,
            strokeWidth,
            height - strokeWidth,
            strokeColor,
        )
        guiGraphics.fill(x + strokeWidth, y, width - strokeWidth, strokeWidth, strokeColor)
    }
}
