package org.infinite.libs.graphics.graphics3d

import com.mojang.blaze3d.buffers.GpuBufferSlice
import com.mojang.blaze3d.resource.GraphicsResourceAllocator
import com.mojang.blaze3d.vertex.PoseStack
import net.minecraft.client.Camera
import net.minecraft.client.CameraType
import net.minecraft.client.DeltaTracker
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.MultiBufferSource
import net.minecraft.world.phys.AABB
import net.minecraft.world.phys.Vec3
import org.infinite.libs.graphics.graphics3d.structs.CameraRoll
import org.infinite.libs.graphics.graphics3d.structs.ColorBox
import org.infinite.libs.graphics.graphics3d.structs.Line
import org.infinite.libs.graphics.graphics3d.structs.Quad
import org.infinite.libs.graphics.graphics3d.structs.RenderCommand3D
import org.infinite.libs.graphics.graphics3d.system.RenderResources
import org.infinite.libs.graphics.graphics3d.system.RenderUtils
import org.joml.Matrix4f
import org.joml.Vector4f

class RenderSystem3D(
    private val graphicsResourceAllocator: GraphicsResourceAllocator,
    private val deltaTracker: DeltaTracker,
    private val renderBlockOutline: Boolean,
    private val camera: Camera,
    private val positionMatrix: Matrix4f,
    private val projectionMatrix: Matrix4f,
    private val frustumMatrix: Matrix4f,
    private val gpuBufferSlice: GpuBufferSlice,
    private val vector4f: Vector4f,
    private val bl2: Boolean,
) {
    /**
     * レンダリングスレッドから計算スレッドへ渡すための安全なスナップショット
     */
    data class RenderSnapshot(
        val posMatrix: Matrix4f, // Matrix4f(positionMatrix) でコピー済み
        val projMatrix: Matrix4f, // Matrix4f(projectionMatrix) でコピー済み
        val cameraPos: Vec3, // camera.position
        val partialTicks: Float, // deltaTracker.gameTimeDeltaTicks
        val scaledWidth: Int,
        val scaledHeight: Int,
        val isOutlineEnabled: Boolean, // renderBlockOutline
    )

    fun snapShot(): RenderSnapshot {
        val window = client.window
        return RenderSnapshot(
            posMatrix = Matrix4f(positionMatrix),
            projMatrix = Matrix4f(projectionMatrix),
            cameraPos = Vec3(camera.position().x, camera.position().y, camera.position().z),
            partialTicks = deltaTracker.gameTimeDeltaTicks,
            scaledWidth = window.guiScaledWidth,
            scaledHeight = window.guiScaledHeight,
            isOutlineEnabled = renderBlockOutline,
        )
    }

    val client: Minecraft = Minecraft.getInstance()

    val immediate: MultiBufferSource.BufferSource =
        client.renderBuffers().bufferSource()

    val matrixStack = PoseStack()

    val tickProgress: Float = deltaTracker.gameTimeDeltaTicks

    init {
        matrixStack.mulPose(positionMatrix)
    }

    // ----------------------------------------------------------------------
    // MatrixStack操作メソッド (変更なし)
    // ----------------------------------------------------------------------
    fun translate(
        x: Double,
        y: Double,
        z: Double,
    ) {
        matrixStack.translate(x, y, z)
    }

    fun pushMatrix() {
        matrixStack.pushPose()
    }

    fun popMatrix() {
        matrixStack.popPose()
    }

    // ----------------------------------------------------------------------
    // 描画ヘルパーメソッド (RenderUtilsのコア機能を呼び出すラッパー)
    // ----------------------------------------------------------------------

    /**
     * 単一の Box を線で描画します。
     * Graphics3Dが自動で VertexConsumer を取得し、RenderUtilsのコア関数に渡します。
     */
    fun renderLinedBox(
        box: AABB,
        color: Int,
        isOverDraw: Boolean = false,
    ) {
        val layer = RenderResources.lines(isOverDraw)
        val buffer = immediate.getBuffer(layer).setLineWidth(1f)

        RenderUtils.renderLinedBox(matrixStack, box, color, buffer)
    }

    /**
     * 複数の Box をそれぞれ異なる色で線描画します。
     */
    fun renderLinedColorBoxes(
        boxes: List<ColorBox>,
        isOverDraw: Boolean = false,
    ) {
        val layer = RenderResources.lines(isOverDraw)
        val buffer = immediate.getBuffer(layer).setLineWidth(1f)
        RenderUtils.renderLinedColorBoxes(matrixStack, boxes, buffer)
    }

    /**
     * 複数の Box をそれぞれ異なる色で塗りつぶし描画します。
     */
    fun renderSolidColorBoxes(
        boxes: List<ColorBox>, // 仮定: 塗りつぶし用の色付きBoxは RenderUtils.ColorBox 型とします。
        isOverDraw: Boolean = false,
    ) {
        val layer = RenderResources.quads(isOverDraw)
        val buffer = immediate.getBuffer(layer).setLineWidth(1f)
        RenderUtils.renderSolidColorBoxes(matrixStack, boxes, buffer)
    }

    fun renderSolidQuads(
        quads: List<Quad>,
        isOverDraw: Boolean = false,
    ) {
        val layer = RenderResources.quads(isOverDraw)
        val buffer = immediate.getBuffer(layer).setLineWidth(1f)
        RenderUtils.renderSolidQuads(matrixStack, quads, buffer)
    }

    fun renderLinedLines(
        lines: List<Line>,
        isOverDraw: Boolean = false,
    ) {
        val layer = RenderResources.lines(isOverDraw)
        val buffer = immediate.getBuffer(layer).setLineWidth(1f)
        RenderUtils.renderLinedLines(matrixStack, lines, buffer)
    }

    /**
     * 2点間に直線を描画します (ワールド座標基準)。
     */
    fun renderLine(
        start: Vec3,
        end: Vec3,
        color: Int,
        isOverDraw: Boolean = false,
    ) {
        val layer = RenderResources.lines(isOverDraw)
        val buffer = immediate.getBuffer(layer).setLineWidth(1f)
        RenderUtils.renderLine(matrixStack, start, end, color, buffer)
    }

    /**
     * ワールド座標 (Vec3d) を画面座標 (DisplayPos) に変換します。
     * ターゲットがカメラの後ろにある場合や、画面外にある場合は null を返します。
     */

    private fun tracerOrigin(partialTicks: Float): Vec3? {
        val yaw: Double = client.player?.getViewYRot(partialTicks)?.toDouble() ?: return null
        val pitch: Double = client.player?.getViewXRot(partialTicks)?.toDouble() ?: return null
        var start: Vec3 =
            CameraRoll(yaw, pitch).vec().scale(5.0)
        if (client.options
                .cameraType == CameraType.THIRD_PERSON_FRONT
        ) {
            start = start.reverse()
        }

        return start
    }

    fun renderTracer(
        end: Vec3,
        color: Int,
        isOverDraw: Boolean,
    ) {
        val layer = RenderResources.lines(isOverDraw)
        val buffer = immediate.getBuffer(layer).setLineWidth(1f)
        val start = tracerOrigin(tickProgress) ?: return
        val offset: Vec3 = RenderUtils.cameraPos().reverse()
        RenderUtils.renderLine(matrixStack, start, end.add(offset), color, buffer)
    }

    fun render(commands: List<RenderCommand3D>) {
        for (command in commands) {
            when (command) {
                is RenderCommand3D.LinedBox -> renderLinedBox(command.box, command.color, command.isOverDraw)
                is RenderCommand3D.LinedColorBoxes -> renderLinedColorBoxes(command.boxes, command.isOverDraw)
                is RenderCommand3D.SolidColorBoxes -> renderSolidColorBoxes(command.boxes, command.isOverDraw)
                is RenderCommand3D.Line -> renderLine(command.start, command.end, command.color, command.isOverDraw)
                is RenderCommand3D.LinedLines -> renderLinedLines(command.lines, command.isOverDraw)
                is RenderCommand3D.SolidQuads -> renderSolidQuads(command.quads, command.isOverDraw)
                is RenderCommand3D.Tracer -> renderTracer(command.end, command.color, command.isOverDraw)
                is RenderCommand3D.PushMatrix -> pushMatrix()
                is RenderCommand3D.PopMatrix -> popMatrix()
                is RenderCommand3D.SetMatrix -> matrixStack.last().pose().set(command.matrix)
            }
        }
        // 最後にまとめてバッチを終了
        flush()
    }

    fun flush() {
        immediate.endBatch()
    }
}
