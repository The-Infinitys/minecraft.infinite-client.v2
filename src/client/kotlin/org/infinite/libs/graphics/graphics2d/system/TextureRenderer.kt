package org.infinite.libs.graphics.graphics2d.system

import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.renderer.RenderPipelines
import org.infinite.libs.graphics.graphics2d.structs.RenderCommand2D

class TextureRenderer(private val gui: GuiGraphics) {
    fun drawTexture(cmd: RenderCommand2D.DrawTexture) {
        // cmd から値を正しくマッピングして呼び出す
        gui.blit(
            RenderPipelines.GUI_TEXTURED,
            cmd.identifier,
            cmd.x.toInt(),
            cmd.y.toInt(),
            cmd.u,
            cmd.v,
            cmd.width.toInt(), // 画面上の幅
            cmd.height.toInt(), // 画面上の高さ
            cmd.uWidth.toInt(), // UVの幅
            cmd.vHeight.toInt(), // UVの高さ
            cmd.textureWidth.toInt(),
            cmd.textureHeight.toInt(),
            cmd.color, // ここで色（ARGB）を直接渡す
        )
    }
}
