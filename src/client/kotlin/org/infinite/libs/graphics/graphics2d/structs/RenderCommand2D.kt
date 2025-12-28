package org.infinite.libs.graphics.graphics2d.structs

import net.minecraft.resources.Identifier
import net.minecraft.world.item.ItemStack
import org.joml.Matrix3x2f

sealed interface RenderCommand2D {
    // 矩形の塗りつぶし
    data class FillRect(
        val x: Float,
        val y: Float,
        val width: Float,
        val height: Float,
        val col0: Int, // 左上
        val col1: Int = col0, // 右上
        val col2: Int = col0, // 右下
        val col3: Int = col0, // 左下
    ) : RenderCommand2D

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
        val col1: Int = col0,
        val col2: Int = col0,
        val col3: Int = col0,
    ) : RenderCommand2D

    // 三角形の塗りつぶし
    data class FillTriangle(
        val x0: Float,
        val y0: Float,
        val x1: Float,
        val y1: Float,
        val x2: Float,
        val y2: Float,
        val col0: Int,
        val col1: Int = col0,
        val col2: Int = col0,
    ) : RenderCommand2D

    data class Text(
        val font: String,
        val text: String,
        val x: Float,
        val y: Float,
        val color: Int,
        val shadow: Boolean,
        val size: Float,
    ) : RenderCommand2D

    data class TextCentered(
        val font: String,
        val text: String,
        val x: Float,
        val y: Float,
        val color: Int,
        val shadow: Boolean,
        val size: Float,
    ) : RenderCommand2D

    data class SetTransform(val matrix: Matrix3x2f) : RenderCommand2D
    data class EnableScissor(val x: Int, val y: Int, val width: Int, val height: Int) : RenderCommand2D
    object DisableScissor : RenderCommand2D
    data class DrawTexture(
        val identifier: Identifier,
        val x: Float,
        val y: Float,
        val width: Float,
        val height: Float,
        val u: Float,
        val v: Float,
        val uWidth: Float,
        val vHeight: Float,
        val textureWidth: Float,
        val textureHeight: Float,
        val color: Int, // アルファ値や乗算色の制御用
    ) : RenderCommand2D

    /**
     * アイテム描画コマンド
     * @param stack アイテム情報
     * @param x 描画座標
     * @param y 描画座標
     * @param scale 描画スケール（デフォルト1f）。
     */
    data class DrawItem(
        val stack: ItemStack,
        val x: Float,
        val y: Float,
        val scale: Float,
        val alpha: Float,
    ) : RenderCommand2D
}
