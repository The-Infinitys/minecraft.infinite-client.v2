package org.infinite.libs.graphics

import net.minecraft.world.phys.AABB
import net.minecraft.world.phys.Vec3
import org.infinite.libs.core.tick.RenderTicks
import org.infinite.libs.graphics.graphics3d.RenderSystem3D
import org.infinite.libs.graphics.graphics3d.structs.ColorBox
import org.infinite.libs.graphics.graphics3d.structs.Line
import org.infinite.libs.graphics.graphics3d.structs.Quad
import org.infinite.libs.graphics.graphics3d.structs.RenderCommand3D
import java.util.LinkedList

class Graphics3D {
    // スナップショット（行列やカメラ位置）へのアクセス
    private val snapshot: RenderSystem3D.RenderSnapshot
        get() = RenderTicks.renderSnapShot ?: throw IllegalStateException("RenderSnapshot is not available.")

    private val commandQueue: LinkedList<RenderCommand3D> = LinkedList()

    fun commands(): LinkedList<RenderCommand3D> = commandQueue

    /**
     * キューをクリアします。各フレームの開始時に呼び出されることを想定しています。
     */
    fun clear() {
        commandQueue.clear()
    }

    // --- 描画コマンドの追加メソッド群 ---

    fun renderLinedBox(box: AABB, color: Int, isOverDraw: Boolean = false) {
        commandQueue.add(RenderCommand3D.LinedBox(box, color, isOverDraw))
    }

    fun renderLinedColorBoxes(boxes: List<ColorBox>, isOverDraw: Boolean = false) {
        commandQueue.add(RenderCommand3D.LinedColorBoxes(boxes, isOverDraw))
    }

    fun renderSolidColorBoxes(boxes: List<ColorBox>, isOverDraw: Boolean = false) {
        commandQueue.add(RenderCommand3D.SolidColorBoxes(boxes, isOverDraw))
    }

    fun renderSolidQuads(quads: List<Quad>, isOverDraw: Boolean = false) {
        commandQueue.add(RenderCommand3D.SolidQuads(quads, isOverDraw))
    }

    fun renderLinedLines(lines: List<Line>, isOverDraw: Boolean = false) {
        commandQueue.add(RenderCommand3D.LinedLines(lines, isOverDraw))
    }

    fun renderLine(start: Vec3, end: Vec3, color: Int, isOverDraw: Boolean = false) {
        commandQueue.add(RenderCommand3D.Line(start, end, color, isOverDraw))
    }

    /**
     * トレーサーの描画。
     * 計算スレッド側で snapshot を使用して、カメラオフセットを考慮した計算を行うことも可能です。
     */
    fun renderTracer(end: Vec3, color: Int, isOverDraw: Boolean = true) {
        commandQueue.add(RenderCommand3D.Tracer(end, color, isOverDraw))
    }

    // マトリックス操作の記録
    fun pushMatrix() = commandQueue.add(RenderCommand3D.PushMatrix)
    fun popMatrix() = commandQueue.add(RenderCommand3D.PopMatrix)
}
