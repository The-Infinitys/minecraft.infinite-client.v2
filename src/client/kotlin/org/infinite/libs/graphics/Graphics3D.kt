package org.infinite.libs.graphics

import org.infinite.libs.core.tick.RenderTicks
import org.infinite.libs.graphics.graphics3d.RenderSystem3D
import org.infinite.libs.graphics.graphics3d.structs.RenderCommand3D
import org.joml.Matrix4f
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

    fun setMatrix(matrix: Matrix4f) = commandQueue.add(RenderCommand3D.SetMatrix(matrix))
    fun pushMatrix() = commandQueue.add(RenderCommand3D.PushMatrix)
    fun popMatrix() = commandQueue.add(RenderCommand3D.PopMatrix)
}
