package org.infinite.libs.graphics.graphics2d.types

import net.minecraft.resources.Identifier
import org.infinite.libs.graphics.graphics2d.CanvasStyle
import org.infinite.libs.graphics.graphics2d.Enums.Repetition

class CanvasPattern(
    val image: Identifier,
    val repetition: Repetition,
) : CanvasStyle
