package org.infinite.libs.graphics.system

import net.minecraft.world.phys.Vec3
import org.joml.Matrix4f

/**
 * 3D空間から2Dスクリーンへの投影に必要な情報を保持する不変クラス
 */
data class ProjectionData(
    val cameraPos: Vec3,
    val modelViewMatrix: Matrix4f,
    val projectionMatrix: Matrix4f,
    val scaledWidth: Int,
    val scaledHeight: Int,
)
