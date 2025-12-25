package org.infinite.libs.graphics.graphics2d.system

import kotlin.math.sqrt

data class PointPair(val ix: Float, val iy: Float, val ox: Float, val oy: Float) {
    companion object {
        fun calculateOffsets(vx: Float, vy: Float, cx: Float, cy: Float, halfWidth: Float): PointPair {
            val dx = vx - cx
            val dy = vy - cy
            val dist = sqrt(dx * dx + dy * dy)

            if (dist == 0f) return PointPair(vx, vy, vx, vy)

            val nx = dx / dist
            val ny = dy / dist

            return PointPair(
                ix = vx - nx * halfWidth,
                iy = vy - ny * halfWidth, // 内側へ
                ox = vx + nx * halfWidth,
                oy = vy + ny * halfWidth, // 外側へ
            )
        }
    }
}
