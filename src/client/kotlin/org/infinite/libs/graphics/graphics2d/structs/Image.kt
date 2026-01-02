package org.infinite.libs.graphics.graphics2d.structs

import com.mojang.blaze3d.platform.NativeImage
import net.minecraft.client.Minecraft
import net.minecraft.resources.Identifier
import net.minecraft.server.packs.resources.ResourceManager

class Image(
    val identifier: Identifier,
    customWidth: Int? = null,
    customHeight: Int? = null,
) {
    val width: Int
    val height: Int

    init {
        if (customWidth != null && customHeight != null) {
            width = customWidth
            height = customHeight
        } else {
            val mc: Minecraft = Minecraft.getInstance()
            val resourceManager: ResourceManager = mc.resourceManager
            val resource = resourceManager.getResource(identifier)
            if (resource.isPresent) {
                val iconResource = resource.get()
                val image = NativeImage.read(iconResource.open())
                width = image.width
                height = image.height
            } else {
                throw IllegalArgumentException("Image not found: $identifier")
            }
        }
    }
}
