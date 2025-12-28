package org.infinite.libs.core.tick

import kotlinx.coroutines.runBlocking
import net.minecraft.client.DeltaTracker
import net.minecraft.client.gui.GuiGraphics
import org.infinite.InfiniteClient
import org.infinite.libs.graphics.graphics2d.RenderSystem2D

object RenderTicks {
    fun onStartUiRendering(
        guiGraphics: GuiGraphics,
        deltaTracker: DeltaTracker,
    ) {
        val commands =
            runBlocking {
                return@runBlocking InfiniteClient.localFeatures.onStartUiRendering(deltaTracker)
            }
        val renderSystem2D = RenderSystem2D(guiGraphics)
        renderSystem2D.render(commands)
    }

    fun onEndUiRendering(
        guiGraphics: GuiGraphics,
        deltaTracker: DeltaTracker,
    ) {
        val commands =
            runBlocking {
                return@runBlocking InfiniteClient.localFeatures.onEndUiRendering(deltaTracker)
            }
        val renderSystem2D = RenderSystem2D(guiGraphics)
        renderSystem2D.render(commands)
    }

    fun onStartWorldRendering() {}

    fun onEndWorldRendering() {}
}
