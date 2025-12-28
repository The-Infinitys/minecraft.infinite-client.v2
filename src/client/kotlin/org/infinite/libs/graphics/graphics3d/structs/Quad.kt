package org.infinite.libs.graphics.graphics3d.structs

import net.minecraft.world.phys.Vec3
import org.joml.Vector3f

data class Quad(
    val vertex1: Vec3,
    val vertex2: Vec3,
    val vertex3: Vec3,
    val vertex4: Vec3,
    val color: Int,
    val normal: Vector3f,
)
