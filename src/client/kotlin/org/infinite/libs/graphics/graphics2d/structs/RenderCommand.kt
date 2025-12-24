package org.infinite.libs.graphics.graphics2d.structs

sealed interface RenderCommand {
    val zIndex: Int

    data class DrawRectInt(
        val x: Int,
        val y: Int,
        val width: Int,
        val height: Int,
        val strokeWidth: Int,
        val color: Int,
        override val zIndex: Int,
    ) : RenderCommand
}
