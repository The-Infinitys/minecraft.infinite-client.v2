package org.infinite.ultimate.theme.infinite

import org.infinite.libs.graphics.bundle.Graphics2DRenderer
import org.infinite.libs.ui.theme.ColorScheme
import org.infinite.libs.ui.theme.Theme
import org.infinite.utils.alpha
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

class InfiniteTheme : Theme() {
    override val colorScheme: ColorScheme = InfiniteColorScheme()
    private val loopTime = 5000.0 // 5秒で一周

    override fun renderBackGround(
        x: Float,
        y: Float,
        width: Float,
        height: Float,
        graphics2DRenderer: Graphics2DRenderer,
        alpha: Float,
    ) {
        val centerX = x + width / 2f
        val centerY = y + height / 2f

        // 現在の時間に基づくオフセット (0.0 ~ 1.0)
        val t = (System.currentTimeMillis() % loopTime.toLong()) / loopTime

        val baseColors = arrayOf(
            colorScheme.redColor,
            colorScheme.yellowColor,
            colorScheme.greenColor,
            colorScheme.aquaColor,
            colorScheme.blueColor,
            colorScheme.magentaColor,
        )

        val alphaInt = (255 * alpha).toInt()
        val centerColor = colorScheme.blackColor.alpha(alphaInt)

        // 画面全体を覆うのに十分な半径
        val r = sqrt(width.pow(2) + height.pow(2)) * (1 + 4 * alpha)
        val size = baseColors.size

        graphics2DRenderer.enableScissor(x.toInt(), y.toInt(), width.toInt(), height.toInt())

        for (i in 0 until size) {
            // アルファ値を適用した色
            val color = baseColors[i].alpha(alphaInt)
            val nextColor = baseColors[(i + 1) % size].alpha(alphaInt)

            // 角度の計算 (i / size を Double にキャストするのが重要)
            val d1 = 2.0 * PI * ((i.toDouble() / size + t) % 1.0)
            val d2 = 2.0 * PI * (((i + 1).toDouble() / size + t) % 1.0)

            // 頂点座標
            val x1 = centerX + r * cos(d1).toFloat()
            val y1 = centerY + r * sin(d1).toFloat()
            val x2 = centerX + r * cos(d2).toFloat()
            val y2 = centerY + r * sin(d2).toFloat()

            // 三角形の描画 (中心, 点1, 点2)
            // fillTriangle(x1, y1, x2, y2, x3, y3, c1, c2, c3)
            graphics2DRenderer.fillTriangle(
                centerX, centerY, x1, y1, x2, y2,
                centerColor, color, nextColor,
            )
        }

        graphics2DRenderer.disableScissor()
        graphics2DRenderer.flush()
    }
}
