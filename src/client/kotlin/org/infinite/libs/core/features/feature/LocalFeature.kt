package org.infinite.libs.core.features.feature

import org.infinite.libs.core.TickInterface
import org.infinite.libs.core.features.Feature
import org.infinite.libs.graphics.Graphics2D

open class LocalFeature :
    Feature(),
    TickInterface {
    open fun onConnected() {
    }

    open fun onDisconnected() {
    }

    override fun onStartTick() {
    }

    override fun onEndTick() {
    }

    open fun onStartUiRendering(graphics2D: Graphics2D): Graphics2D = graphics2D

    open fun onEndUiRendering(graphics2D: Graphics2D): Graphics2D = graphics2D
}
