package org.infinite.libs.graphics.graphics2d.system

import net.minecraft.client.gui.Font
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.util.FormattedCharSequence
import org.infinite.libs.graphics.graphics2d.elements.StringRenderState

class TextRenderer(private val guiGraphics: GuiGraphics) {
    fun text(font: Font, text: String, x: Float, y: Float, color: Int, shadow: Boolean = false) {
        val charSequence = FormattedCharSequence.forward(text, net.minecraft.network.chat.Style.EMPTY)
        val state = StringRenderState(
            font,
            charSequence,
            guiGraphics.pose(), // 現在の行列スタックを渡す
            x,
            y,
            color,
            0, // 背景色 (デフォルト透明)
            shadow, // ドロップシャドウ
            false, // 空白を含めるか
            guiGraphics.scissorStack.peek(),
        )

        // 描画キューに送信
        guiGraphics.guiRenderState.submitText(state)
    }
}
