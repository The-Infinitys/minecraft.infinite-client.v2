package org.infinite.libs.interfaces

import net.minecraft.client.Minecraft
import net.minecraft.client.multiplayer.ClientLevel
import net.minecraft.client.player.LocalPlayer

open class MinecraftInterface {
    protected val minecraft: Minecraft
        get() = Minecraft.getInstance()
    protected val player: LocalPlayer?
        get() = minecraft.player
    protected val level: ClientLevel?
        get() = minecraft.level
}
