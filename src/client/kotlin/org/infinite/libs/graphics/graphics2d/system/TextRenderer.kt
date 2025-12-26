package org.infinite.libs.graphics.graphics2d.system

import net.minecraft.client.gui.Font
import net.minecraft.client.gui.GuiGraphics
import org.infinite.libs.interfaces.MinecraftInterface

class TextRenderer(private val guiGraphics: GuiGraphics) : MinecraftInterface() {
    fun text(font: Font, text: String, x: Float, y: Float, color: Int, size: Float = 8.0f, shadow: Boolean = false) {
        val poseStack = guiGraphics.pose()
        poseStack.pushMatrix()

        poseStack.translate(x, y)
        val fontSize = size / client.font.lineHeight
        poseStack.scale(fontSize, fontSize)

        // 描画（座標は0, 0でOK）
        guiGraphics.drawString(font, text, 0, 0, color, shadow)

        poseStack.popMatrix()
    }

    fun textCentered(
        font: Font,
        text: String,
        x: Float,
        y: Float,
        color: Int,
        size: Float = 8.0f,
        shadow: Boolean = false,
    ) {
        val scale = size / client.font.lineHeight
        val width = font.width(text) * scale
        text(font, text, x - width / 2, y - size / 2, color, size, shadow)
    }
}
