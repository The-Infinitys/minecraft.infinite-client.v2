package org.infinite.libs.graphics.graphics3d.structs

import net.minecraft.world.phys.AABB

data class ColorBox(
    val color: Int,
    val box: AABB,
)
