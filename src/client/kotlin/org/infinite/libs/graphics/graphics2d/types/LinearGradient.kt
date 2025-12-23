package org.infinite.libs.graphics.graphics2d.types

import org.infinite.libs.graphics.graphics2d.CanvasStyle

class LinearGradient(
    val x0: Double,
    val y0: Double,
    val x1: Double,
    val y1: Double,
) : CanvasStyle {
    private val colorStops = mutableListOf<Pair<Double, Int>>()

    fun addColorStop(
        offset: Double,
        color: Int,
    ) {
        colorStops.add(offset to color)
    }
}
