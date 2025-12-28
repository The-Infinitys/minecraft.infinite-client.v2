package org.infinite.libs.graphics.graphics3d.system

import com.mojang.blaze3d.vertex.PoseStack
import com.mojang.blaze3d.vertex.VertexConsumer
import net.minecraft.client.Minecraft
import net.minecraft.world.phys.AABB
import net.minecraft.world.phys.Vec3
import org.infinite.libs.graphics.graphics3d.structs.ColorBox
import org.infinite.libs.graphics.graphics3d.structs.Line
import org.infinite.libs.graphics.graphics3d.structs.Quad
import org.joml.Vector3f

object RenderUtils {
    private const val DEFAULT_LINE_WIDTH = 2f

    /**
     * 単一のボックスを塗りつぶし描画します。
     */
    fun renderSolidBox(
        matrix: PoseStack,
        box: AABB,
        color: Int,
        buffer: VertexConsumer,
    ) {
        val entry = matrix.last()
        val x1 = box.minX.toFloat()
        val y1 = box.minY.toFloat()
        val z1 = box.minZ.toFloat()
        val x2 = box.maxX.toFloat()
        val y2 = box.maxY.toFloat()
        val z2 = box.maxZ.toFloat()

        // 頂点順序を整理 (反時計回り/時計回りの一貫性)
        // Bottom
        drawQuad(buffer, entry, x1, y1, z1, x2, y1, z1, x2, y1, z2, x1, y1, z2, color, 0f, -1f, 0f)
        // Top
        drawQuad(buffer, entry, x1, y2, z1, x1, y2, z2, x2, y2, z2, x2, y2, z1, color, 0f, 1f, 0f)
        // North
        drawQuad(buffer, entry, x1, y1, z1, x1, y2, z1, x2, y2, z1, x2, y1, z1, color, 0f, 0f, -1f)
        // East
        drawQuad(buffer, entry, x2, y1, z1, x2, y2, z1, x2, y2, z2, x2, y1, z2, color, 1f, 0f, 0f)
        // South
        drawQuad(buffer, entry, x1, y1, z2, x2, y1, z2, x2, y2, z2, x1, y2, z2, color, 0f, 0f, 1f)
        // West
        drawQuad(buffer, entry, x1, y1, z1, x1, y1, z2, x1, y2, z2, x1, y2, z1, color, -1f, 0f, 0f)
    }

    private fun drawQuad(
        buffer: VertexConsumer,
        entry: PoseStack.Pose,
        x1: Float,
        y1: Float,
        z1: Float,
        x2: Float,
        y2: Float,
        z2: Float,
        x3: Float,
        y3: Float,
        z3: Float,
        x4: Float,
        y4: Float,
        z4: Float,
        color: Int,
        nx: Float,
        ny: Float,
        nz: Float,
    ) {
        buffer.addVertex(entry, x1, y1, z1).setColor(color).setNormal(entry, nx, ny, nz)
        buffer.addVertex(entry, x2, y2, z2).setColor(color).setNormal(entry, nx, ny, nz)
        buffer.addVertex(entry, x3, y3, z3).setColor(color).setNormal(entry, nx, ny, nz)
        buffer.addVertex(entry, x4, y4, z4).setColor(color).setNormal(entry, nx, ny, nz)
    }

    /**
     * ラインを描画します。Minecraftのラインシェーダーバグ（カメラ付近の消失）の対策を含みます。
     */
    fun drawLine(
        entry: PoseStack.Pose,
        buffer: VertexConsumer,
        x1: Float,
        y1: Float,
        z1: Float,
        x2: Float,
        y2: Float,
        z2: Float,
        color: Int,
    ) {
        val start = Vector3f(x1, y1, z1)
        val end = Vector3f(x2, y2, z2)
        val diff = Vector3f(end).sub(start)
        val length = diff.length()
        if (length < 0.0001f) return

        val normal = diff.normalize()

        // 頂点1
        buffer.addVertex(entry, x1, y1, z1).setColor(color).setNormal(entry, normal).setLineWidth(DEFAULT_LINE_WIDTH)

        // シェーダーバグ対策: ラインがカメラ(0,0,0)の至近を通る場合、中間頂点を挿入
        val t = -Vector3f(start).dot(normal)
        if (t > 0 && t < length) {
            val closeToCam = Vector3f(normal).mul(t).add(start)
            buffer.addVertex(entry, closeToCam).setColor(color).setNormal(entry, normal).setLineWidth(DEFAULT_LINE_WIDTH)
            buffer.addVertex(entry, closeToCam).setColor(color).setNormal(entry, normal).setLineWidth(DEFAULT_LINE_WIDTH)
        }

        // 頂点2
        buffer.addVertex(entry, x2, y2, z2).setColor(color).setNormal(entry, normal).setLineWidth(DEFAULT_LINE_WIDTH)
    }

    fun renderLinedBox(matrix: PoseStack, box: AABB, color: Int, buffer: VertexConsumer) {
        val entry = matrix.last()
        val x1 = box.minX.toFloat()
        val y1 = box.minY.toFloat()
        val z1 = box.minZ.toFloat()
        val x2 = box.maxX.toFloat()
        val y2 = box.maxY.toFloat()
        val z2 = box.maxZ.toFloat()

        // 12本の辺を描画
        drawLine(entry, buffer, x1, y1, z1, x2, y1, z1, color)
        drawLine(entry, buffer, x1, y1, z1, x1, y1, z2, color)
        drawLine(entry, buffer, x2, y1, z1, x2, y1, z2, color)
        drawLine(entry, buffer, x1, y1, z2, x2, y1, z2, color)
        drawLine(entry, buffer, x1, y2, z1, x2, y2, z1, color)
        drawLine(entry, buffer, x1, y2, z1, x1, y2, z2, color)
        drawLine(entry, buffer, x2, y2, z1, x2, y2, z2, color)
        drawLine(entry, buffer, x1, y2, z2, x2, y2, z2, color)
        drawLine(entry, buffer, x1, y1, z1, x1, y2, z1, color)
        drawLine(entry, buffer, x2, y1, z1, x2, y2, z1, color)
        drawLine(entry, buffer, x1, y1, z2, x1, y2, z2, color)
        drawLine(entry, buffer, x2, y1, z2, x2, y2, z2, color)
    }

    // ---------------------------------------------------------------------------------------------
    // バッチ処理用 (カメラオフセット適用)

    fun renderSolidColorBoxes(matrix: PoseStack, boxes: List<ColorBox>, buffer: VertexConsumer) {
        val offset = cameraPos().reverse()
        boxes.forEach { renderSolidBox(matrix, it.box.move(offset), it.color, buffer) }
    }

    fun renderLinedColorBoxes(matrix: PoseStack, boxes: List<ColorBox>, buffer: VertexConsumer) {
        val offset = cameraPos().reverse()
        boxes.forEach { renderLinedBox(matrix, it.box.move(offset), it.color, buffer) }
    }

    fun renderLinedLines(matrix: PoseStack, lines: List<Line>, buffer: VertexConsumer) {
        val offset = cameraPos().reverse()
        val entry = matrix.last()
        lines.forEach { line ->
            val s = line.start.add(offset)
            val e = line.end.add(offset)
            drawLine(entry, buffer, s.x.toFloat(), s.y.toFloat(), s.z.toFloat(), e.x.toFloat(), e.y.toFloat(), e.z.toFloat(), line.color)
        }
    }

    /**
     * 複数の Quad（四角形面）をワールド座標基準で描画します。
     */
    fun renderSolidQuads(
        matrix: PoseStack,
        quads: List<Quad>,
        buffer: VertexConsumer,
    ) {
        val offset = cameraPos().reverse()
        val entry = matrix.last()

        quads.forEach { quad ->
            // カメラ相対座標への変換
            val v1 = quad.vertex1.add(offset)
            val v2 = quad.vertex2.add(offset)
            val v3 = quad.vertex3.add(offset)
            val v4 = quad.vertex4.add(offset)

            // 各頂点を追加
            // WurstのdrawSolidBoxの実装に合わせ、法線(Normal)はquadから取得
            buffer.addVertex(entry, v1.x.toFloat(), v1.y.toFloat(), v1.z.toFloat())
                .setColor(quad.color)
                .setNormal(entry, quad.normal.x, quad.normal.y, quad.normal.z)

            buffer.addVertex(entry, v2.x.toFloat(), v2.y.toFloat(), v2.z.toFloat())
                .setColor(quad.color)
                .setNormal(entry, quad.normal.x, quad.normal.y, quad.normal.z)

            buffer.addVertex(entry, v3.x.toFloat(), v3.y.toFloat(), v3.z.toFloat())
                .setColor(quad.color)
                .setNormal(entry, quad.normal.x, quad.normal.y, quad.normal.z)

            buffer.addVertex(entry, v4.x.toFloat(), v4.y.toFloat(), v4.z.toFloat())
                .setColor(quad.color)
                .setNormal(entry, quad.normal.x, quad.normal.y, quad.normal.z)
        }
    }

    /**
     * 2つのワールド座標間に直線を描画します。
     * カメラ位置によるオフセット計算と、シェーダーバグ対策が含まれています。
     */
    fun renderLine(
        matrix: PoseStack,
        start: Vec3,
        end: Vec3,
        color: Int,
        buffer: VertexConsumer,
    ) {
        val entry = matrix.last()
        val offset = cameraPos().reverse()

        // ワールド座標からカメラ相対座標へ変換
        val s = start.add(offset)
        val e = end.add(offset)

        // 共通の描画ロジック（drawLine）を呼び出し
        drawLine(
            entry,
            buffer,
            s.x.toFloat(), s.y.toFloat(), s.z.toFloat(),
            e.x.toFloat(), e.y.toFloat(), e.z.toFloat(),
            color,
        )
    }
    fun cameraPos(): Vec3 = Minecraft.getInstance().gameRenderer.mainCamera.position()
}
