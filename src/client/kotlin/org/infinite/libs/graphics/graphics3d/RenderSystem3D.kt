package org.infinite.libs.graphics.graphics3d

import com.mojang.blaze3d.buffers.GpuBufferSlice
import com.mojang.blaze3d.resource.GraphicsResourceAllocator
import net.minecraft.client.Camera
import net.minecraft.client.DeltaTracker
import net.minecraft.world.phys.Vec3
import org.infinite.libs.graphics.graphics3d.structs.RenderCommand3D
import org.infinite.libs.interfaces.MinecraftInterface
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
) : MinecraftInterface() {
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
        val window = minecraft.window
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

    fun render(commands: List<RenderCommand3D>) {
    }
}
