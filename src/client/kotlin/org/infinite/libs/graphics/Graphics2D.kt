package org.infinite.libs.graphics

import net.minecraft.client.DeltaTracker
import org.infinite.libs.graphics.graphics2d.structs.RenderCommand
import org.infinite.libs.graphics.graphics2d.structs.StrokeStyle
import org.infinite.libs.graphics.graphics2d.system.PointPair
import org.infinite.libs.graphics.graphics2d.system.PointPair.Companion.calculateForMiter
import org.infinite.libs.interfaces.MinecraftInterface
import java.util.*
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * MDN CanvasRenderingContext2D API を Minecraft GuiGraphics 上に再現するクラス。
 * zIndex を排除し、呼び出し順（画家のアルゴリズム）に従って描画コマンドを保持します。
 */
class Graphics2D(
    deltaTracker: DeltaTracker,
) : MinecraftInterface() {
    val gameDelta: Float = deltaTracker.gameTimeDeltaTicks
    val realDelta: Float = deltaTracker.realtimeDeltaTicks
    val width: Int = client?.window?.guiScaledWidth ?: 200
    val height: Int = client?.window?.guiScaledHeight ?: 150
    var strokeStyle: StrokeStyle? = null
    var fillStyle: Int = 0xFFFFFFFF.toInt()

    // zIndexによるソートが不要なため、単純なFIFOキューに変更
    // 100は初期容量ではなく、最大容量の指定になるため、必要に応じて調整してください
    private val commandQueue = LinkedList<RenderCommand>()

    // --- fillRect ---

    fun fillRect(x: Float, y: Float, width: Float, height: Float) {
        commandQueue.add(RenderCommand.FillRect(x, y, width, height, fillStyle, fillStyle, fillStyle, fillStyle))
    }

    // --- fillQuad ---

    fun fillQuad(x0: Float, y0: Float, x1: Float, y1: Float, x2: Float, y2: Float, x3: Float, y3: Float) {
        fillQuad(x0, y0, x1, y1, x2, y2, x3, y3, fillStyle, fillStyle, fillStyle, fillStyle)
    }

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
        // 頂点データと色のペアをリスト化
        val vertices = mutableListOf(
            Vertex(x0, y0, col0),
            Vertex(x1, y1, col1),
            Vertex(x2, y2, col2),
            Vertex(x3, y3, col3),
        )

        // 重心を計算
        val centerX = vertices.map { it.x }.average().toFloat()
        val centerY = vertices.map { it.y }.average().toFloat()

        // 重心からの角度でソート (時計回り)
        // Math.atan2(y, x) は反時計回りなので、マイナスを付けてソート
        vertices.sortBy { atan2((it.y - centerY).toDouble(), (it.x - centerX).toDouble()) }

        commandQueue.add(
            RenderCommand.FillQuad(
                vertices[0].x, vertices[0].y,
                vertices[1].x, vertices[1].y,
                vertices[2].x, vertices[2].y,
                vertices[3].x, vertices[3].y,
                vertices[0].color, vertices[1].color, vertices[2].color, vertices[3].color,
            ),
        )
    }

    // 内部用ヘルパー
    private data class Vertex(val x: Float, val y: Float, val color: Int)
    // --- fillTriangle ---

    fun fillTriangle(x0: Float, y0: Float, x1: Float, y1: Float, x2: Float, y2: Float) {
        fillTriangle(x0, y0, x1, y1, x2, y2, fillStyle, fillStyle, fillStyle)
    }

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
        // 外積 (Vector Cross Product) を利用して回転方向を判定
        // (x1-x0)*(y2-y0) - (y1-y0)*(x2-x0)
        val crossProduct = (x1 - x0) * (y2 - y0) - (y1 - y0) * (x2 - x0)

        // crossProduct > 0 なら反時計回りなので、頂点1と2を入れ替えて時計回りにする
        if (crossProduct > 0) {
            addFillTriangle(x0, y0, x2, y2, x1, y1, col0, col2, col1)
        } else {
            addFillTriangle(x0, y0, x1, y1, x2, y2, col0, col1, col2)
        }
    }

    private fun addFillTriangle(
        x0: Float,
        y0: Float,
        x1: Float,
        y1: Float,
        x2: Float,
        y2: Float,
        c0: Int,
        c1: Int,
        c2: Int,
    ) {
        commandQueue.add(RenderCommand.FillTriangle(x0, y0, x1, y1, x2, y2, c0, c1, c2))
    }

    // --- strokeRect ---
    fun strokeRect(x: Float, y: Float, width: Float, height: Float) {
        val style = strokeStyle ?: return
        strokeRect(x, y, width, height, style.color, style.color, style.color, style.color)
    }

    fun strokeRect(
        x: Float,
        y: Float,
        w: Float,
        h: Float,
        col0: Int, // 左上
        col1: Int, // 右上
        col2: Int, // 右下
        col3: Int, // 左下
    ) {
        val style = strokeStyle ?: return
        val strokeWidth = style.width
        val v = strokeWidth / 2f
        val p1 = PointPair(x + v, y + v, x - v, y - v)
        val p2 = PointPair(x + v, y + h - v, x - v, y + h + v)
        val p3 = PointPair(x + w - v, y + h - v, x + w + v, y + h + v)
        val p4 = PointPair(x + w - v, y + v, x + w + v, y - v)

        // ここで guiGraphics.fillQuad の代わりに commandQueue.add(RenderCommand.FillQuad(...)) を使用
        commandQueue.add(RenderCommand.FillQuad(p1.ix, p1.iy, p1.ox, p1.oy, p2.ox, p2.oy, p2.ix, p2.iy, col0, col0, col1, col1))
        commandQueue.add(RenderCommand.FillQuad(p2.ix, p2.iy, p2.ox, p2.oy, p3.ox, p3.oy, p3.ix, p3.iy, col1, col1, col2, col2))
        commandQueue.add(RenderCommand.FillQuad(p3.ix, p3.iy, p3.ox, p3.oy, p4.ox, p4.oy, p4.ix, p4.iy, col2, col2, col3, col3))
        commandQueue.add(RenderCommand.FillQuad(p4.ix, p4.iy, p4.ox, p4.oy, p1.ox, p1.oy, p1.ix, p1.iy, col3, col3, col0, col0))
    }

    // --- strokeQuad ---
    fun strokeQuad(x0: Float, y0: Float, x1: Float, y1: Float, x2: Float, y2: Float, x3: Float, y3: Float) {
        val style = strokeStyle ?: return
        val color = style.color

        strokeQuad(x0, y0, x1, y1, x2, y2, x3, y3, color, color, color, color)
    }

    fun strokeQuad(
        ix0: Float,
        iy0: Float,
        ix1: Float,
        iy1: Float,
        ix2: Float,
        iy2: Float,
        ix3: Float,
        iy3: Float,
        icol0: Int,
        icol1: Int,
        icol2: Int,
        icol3: Int,
    ) {
        val style = strokeStyle ?: return
        val strokeWidth = style.width

        // 1. 反時計回りに正規化
        val q = normalizeToCCW(ix0, iy0, ix1, iy1, ix2, iy2, ix3, iy3, icol0, icol1, icol2, icol3)

        val hw = strokeWidth / 2f

        // 2. 正規化された座標で計算
        val p0 = calculateForMiter(q.x0, q.y0, q.x3, q.y3, q.x1, q.y1, hw)
        val p1 = calculateForMiter(q.x1, q.y1, q.x0, q.y0, q.x2, q.y2, hw)
        val p2 = calculateForMiter(q.x2, q.y2, q.x1, q.y1, q.x3, q.y3, hw)
        val p3 = calculateForMiter(q.x3, q.y3, q.x2, q.y2, q.x0, q.y0, hw)

        // 3. 内側の色をサンプリング
        val innerCols = if (strokeWidth > 2.0f) {
            QuadColorSampler.sample(
                p0.ix, p0.iy, p1.ix, p1.iy, p2.ix, p2.iy, p3.ix, p3.iy,
                q.x0, q.y0, q.x1, q.y1, q.x2, q.y2, q.x3, q.y3,
                q.c0, q.c1, q.c2, q.c3,
            )
        } else {
            listOf(q.c0, q.c1, q.c2, q.c3)
        }

        // 4. エッジ描画 (色の引数順序を修正)
        // 引数: start, end, outSCol, outECol, inSCol, inECol
        drawColoredEdge(p0, p1, q.c0, q.c1, innerCols[0], innerCols[1])
        drawColoredEdge(p1, p2, q.c1, q.c2, innerCols[1], innerCols[2])
        drawColoredEdge(p2, p3, q.c2, q.c3, innerCols[2], innerCols[3])
        drawColoredEdge(p3, p0, q.c3, q.c0, innerCols[3], innerCols[0])
    }

    private fun drawColoredEdge(
        start: PointPair,
        end: PointPair,
        outSCol: Int,
        outECol: Int,
        inSCol: Int,
        inECol: Int,
    ) {
        // 頂点指定順序:
        // 1: 開始外(ox,oy) -> 2: 終了外(ox,oy) -> 3: 終了内(ix,iy) -> 4: 開始内(ix,iy)
        commandQueue.add(
            RenderCommand.FillQuad(
                start.ox, start.oy,
                end.ox, end.oy,
                end.ix, end.iy,
                start.ix, start.iy,
                outSCol, // 1に対応
                outECol, // 2に対応
                inECol, // 3に対応 (終了地点の内側の色)
                inSCol, // 4に対応 (開始地点の内側の色)
            ),
        )
    }

    // --- strokeTriangle ---
    fun strokeTriangle(x0: Float, y0: Float, x1: Float, y1: Float, x2: Float, y2: Float) {
        val style = strokeStyle ?: return
        val color = style.color

        strokeTriangle(x0, y0, x1, y1, x2, y2, color, color, color)
    }

    fun strokeTriangle(
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
        val style = strokeStyle ?: return
        val strokeWidth = style.width
        val hw = strokeWidth / 2f

        // 1. 各角のオフセット座標を計算
        val p0 = calculateForMiter(x0, y0, x2, y2, x1, y1, hw)
        val p1 = calculateForMiter(x1, y1, x0, y0, x2, y2, hw)
        val p2 = calculateForMiter(x2, y2, x1, y1, x0, y0, hw)

        // 2. 内側の色を決定
        val (inCol0, inCol1, inCol2) = if (strokeWidth > 2.0f) {
            Triple(
                lerpColorInTriangle(p0.ix, p0.iy, x0, y0, x1, y1, x2, y2, col0, col1, col2),
                lerpColorInTriangle(p1.ix, p1.iy, x0, y0, x1, y1, x2, y2, col0, col1, col2),
                lerpColorInTriangle(p2.ix, p2.iy, x0, y0, x1, y1, x2, y2, col0, col1, col2),
            )
        } else {
            // 幅が狭い場合は、元の頂点色をそのまま使う（高速）
            Triple(col0, col1, col2)
        }

        // 3. 描画
        drawColoredEdge(p0, p1, inCol0, inCol1, col0, col1)
        drawColoredEdge(p1, p2, inCol1, inCol2, col1, col2)
        drawColoredEdge(p2, p0, inCol2, inCol0, col2, col0)
    }

    // 2点間に線を描画するヘルパー関数 (線幅を持つ四角形として描画)
    private fun drawLine(
        x1: Float,
        y1: Float,
        x2: Float,
        y2: Float,
        strokeWidth: Float,
        col1: Int,
        col2: Int,
    ) {
        if (strokeWidth <= 0) return

        val dx = x2 - x1
        val dy = y2 - y1
        val length = sqrt(dx * dx + dy * dy)

        if (length == 0f) return // 同じ点なので線は描画しない

        val angle = atan2(dy.toDouble(), dx.toDouble()).toFloat()
        val halfWidth = strokeWidth / 2.0f

        val nx = -sin(angle) // 法線ベクトルのx成分
        val ny = cos(angle) // 法線ベクトルのy成分

        // 線の四隅の座標を計算
        val p1x = x1 + nx * halfWidth
        val p1y = y1 + ny * halfWidth
        val p2x = x2 + nx * halfWidth
        val p2y = y2 + ny * halfWidth
        val p3x = x2 - nx * halfWidth
        val p3y = y2 - ny * halfWidth
        val p4x = x1 - nx * halfWidth
        val p4y = y1 - ny * halfWidth

        // FillQuadとしてコマンドキューに追加
        commandQueue.add(
            RenderCommand.FillQuad(
                p1x, p1y,
                p2x, p2y,
                p3x, p3y,
                p4x, p4y,
                col1, col2, col2, col1, // 線は均一な色になるように
            ),
        )
    }

    /**
     * 登録された順にコマンドを取り出します
     */
    fun commands(): List<RenderCommand> = commandQueue.toList()

    /**
     * 頂点と色のペアを保持するデータ構造
     */
    private data class NormalizedQuad(
        val x0: Float,
        val y0: Float,
        val x1: Float,
        val y1: Float,
        val x2: Float,
        val y2: Float,
        val x3: Float,
        val y3: Float,
        val c0: Int,
        val c1: Int,
        val c2: Int,
        val c3: Int,
    )

    /**
     * 頂点の順序を反時計回り(CCW)に正規化し、対応する色も入れ替える
     */
    private fun normalizeToCCW(
        x0: Float,
        y0: Float,
        x1: Float,
        y1: Float,
        x2: Float,
        y2: Float,
        x3: Float,
        y3: Float,
        c0: Int,
        c1: Int,
        c2: Int,
        c3: Int,
    ): NormalizedQuad {
        // 符号付き面積の計算 (Shoelace formula)
        // MinecraftのGUI座標系（下が正）では、この値が正なら時計回り(CW)
        val area = (x1 - x0) * (y1 + y0) +
            (x2 - x1) * (y2 + y1) +
            (x3 - x2) * (y3 + y2) +
            (x0 - x3) * (y0 + y3)

        return if (area < 0) {
            // 時計回りなので、頂点1と頂点3を入れ替えて反時計回りにする (0 -> 3 -> 2 -> 1)
            NormalizedQuad(x0, y0, x3, y3, x2, y2, x1, y1, c0, c3, c2, c1)
        } else {
            // 既に反時計回り
            NormalizedQuad(x0, y0, x1, y1, x2, y2, x3, y3, c0, c1, c2, c3)
        }
    }

    private object QuadColorSampler {
        fun sample(
            ix0: Float,
            iy0: Float,
            ix1: Float,
            iy1: Float,
            ix2: Float,
            iy2: Float,
            ix3: Float,
            iy3: Float,
            x0: Float,
            y0: Float,
            x1: Float,
            y1: Float,
            x2: Float,
            y2: Float,
            x3: Float,
            y3: Float,
            c0: Int,
            c1: Int,
            c2: Int,
            c3: Int,
        ): List<Int> {
            val pts = arrayOf(ix0 to iy0, ix1 to iy1, ix2 to iy2, ix3 to iy3)
            return pts.map { (px, py) ->
                // 四角形を2つの三角形 (0,1,2) と (0,2,3) に分割して判定
                if (isPointInTriangle(px, py, x0, y0, x1, y1, x2, y2)) {
                    lerpColor(px, py, x0, y0, x1, y1, x2, y2, c0, c1, c2)
                } else {
                    lerpColor(px, py, x0, y0, x2, y2, x3, y3, c0, c2, c3)
                }
            }
        }

        private fun isPointInTriangle(
            px: Float,
            py: Float,
            x0: Float,
            y0: Float,
            x1: Float,
            y1: Float,
            x2: Float,
            y2: Float,
        ): Boolean {
            // 外積を用いた包含判定（方向を一貫させる）
            fun crossProduct(ax: Float, ay: Float, bx: Float, by: Float, cx: Float, cy: Float) =
                (bx - ax) * (cy - ay) - (by - ay) * (cx - ax)

            val d1 = crossProduct(px, py, x0, y0, x1, y1)
            val d2 = crossProduct(px, py, x1, y1, x2, y2)
            val d3 = crossProduct(px, py, x2, y2, x0, y0)

            val hasNeg = (d1 < 0) || (d2 < 0) || (d3 < 0)
            val hasPos = (d1 > 0) || (d2 > 0) || (d3 > 0)
            return !(hasNeg && hasPos)
        }

        private fun lerpColor(
            px: Float,
            py: Float,
            x0: Float,
            y0: Float,
            x1: Float,
            y1: Float,
            x2: Float,
            y2: Float,
            c0: Int,
            c1: Int,
            c2: Int,
        ): Int {
            val denom = (y1 - y2) * (x0 - x2) + (x2 - x1) * (y0 - y2)
            if (abs(denom) < 1e-6f) return c0

            val w0 = ((y1 - y2) * (px - x2) + (x2 - x1) * (py - y2)) / denom
            val w1 = ((y2 - y0) * (px - x2) + (x0 - x2) * (py - y2)) / denom
            val w2 = 1f - w0 - w1

            // 頂点カラーをRGBA成分に分解（安全なLong/Float変換）
            fun extract(c: Int) = floatArrayOf(
                (c shr 24 and 0xFF).toFloat(),
                (c shr 16 and 0xFF).toFloat(),
                (c shr 8 and 0xFF).toFloat(),
                (c and 0xFF).toFloat(),
            )

            val v0 = extract(c0)
            val v1 = extract(c1)
            val v2 = extract(c2)

            val a = (v0[0] * w0 + v1[0] * w1 + v2[0] * w2).toInt().coerceIn(0, 255)
            val r = (v0[1] * w0 + v1[1] * w1 + v2[1] * w2).toInt().coerceIn(0, 255)
            val g = (v0[2] * w0 + v1[2] * w1 + v2[2] * w2).toInt().coerceIn(0, 255)
            val b = (v0[3] * w0 + v1[3] * w1 + v2[3] * w2).toInt().coerceIn(0, 255)

            return (a shl 24) or (r shl 16) or (g shl 8) or b
        }
    }

    private fun lerpColorInTriangle(
        px: Float,
        py: Float,
        x0: Float,
        y0: Float,
        x1: Float,
        y1: Float,
        x2: Float,
        y2: Float,
        c0: Int,
        c1: Int,
        c2: Int,
    ): Int {
        val denom = (y1 - y2) * (x0 - x2) + (x2 - x1) * (y0 - y2)
        // 縮退した三角形（面積0）の場合は安全にc0を返す
        if (abs(denom) < 1e-6f) return c0

        val w0 = ((y1 - y2) * (px - x2) + (x2 - x1) * (py - y2)) / denom
        val w1 = ((y2 - y0) * (px - x2) + (x0 - x2) * (py - y2)) / denom
        val w2 = 1f - w0 - w1

        // 0.0〜1.0にクランプ（浮動小数点の誤差対策）
        val cw0 = w0.coerceIn(0f, 1f)
        val cw1 = w1.coerceIn(0f, 1f)
        val cw2 = w2.coerceIn(0f, 1f)

        // アルファ値を安全に抽出 (0xFFL と Long を使うことで符号付きIntのバグを回避)
        val a0 = (c0 shr 24 and 0xFF).toFloat()
        val r0 = (c0 shr 16 and 0xFF).toFloat()
        val g0 = (c0 shr 8 and 0xFF).toFloat()
        val b0 = (c0 and 0xFF).toFloat()

        val a1 = (c1 shr 24 and 0xFF).toFloat()
        val r1 = (c1 shr 16 and 0xFF).toFloat()
        val g1 = (c1 shr 8 and 0xFF).toFloat()
        val b1 = (c1 and 0xFF).toFloat()

        val a2 = (c2 shr 24 and 0xFF).toFloat()
        val r2 = (c2 shr 16 and 0xFF).toFloat()
        val g2 = (c2 shr 8 and 0xFF).toFloat()
        val b2 = (c2 and 0xFF).toFloat()

        val a = (a0 * cw0 + a1 * cw1 + a2 * cw2).toInt()
        val r = (r0 * cw0 + r1 * cw1 + r2 * cw2).toInt()
        val g = (g0 * cw0 + g1 * cw1 + g2 * cw2).toInt()
        val b = (b0 * cw0 + b1 * cw1 + b2 * cw2).toInt()

        return (a shl 24) or (r shl 16) or (g shl 8) or b
    }
}
