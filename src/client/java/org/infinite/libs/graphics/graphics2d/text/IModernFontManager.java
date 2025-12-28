package org.infinite.libs.graphics.graphics2d.text;

import net.minecraft.client.gui.font.FontSet;
import net.minecraft.network.chat.Style;

public interface IModernFontManager {
  @SuppressWarnings("unused")
  FontSet infinite$fontSetFromStyle(Style style);

  @SuppressWarnings("unused")
  FontSet infinite$fontSetFromIdentifier(String name);
}
