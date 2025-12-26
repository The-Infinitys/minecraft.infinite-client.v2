package org.infinite.libs.graphics.graphics2d.elements;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.gui.render.state.GuiElementRenderState;
import net.minecraft.client.gui.render.state.GuiTextRenderState;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.util.FormattedCharSequence;
import org.joml.Matrix3x2fc;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

@Environment(EnvType.CLIENT)
public final class StringRenderState extends GuiTextRenderState implements GuiElementRenderState {
  public final Font font;
  public final FormattedCharSequence text;
  public final Matrix3x2fc pose;

  // 基底クラスに合わせて float から int へ戻すか、計算時にキャストします
  // コンストラクタ引数の型に厳密に合わせます
  public final float x;
  public final float y;

  public final int color;
  public final int backgroundColor;
  public final boolean dropShadow;
  public final boolean includeEmpty;

  @Nullable public final ScreenRectangle scissor;

  private Font.PreparedText preparedText;
  @Nullable private ScreenRectangle bounds;

  public StringRenderState(
      Font font,
      FormattedCharSequence text,
      Matrix3x2fc pose,
      float x,
      float y,
      int color,
      int backgroundColor,
      boolean dropShadow,
      boolean includeEmpty,
      @Nullable ScreenRectangle scissor) {
    // 親クラスのシグネチャ: (Font, FormattedCharSequence, Matrix3x2fc, int, int, int, int, boolean, boolean,
    // ScreenRectangle)
    super(
        font,
        text,
        pose,
        (int) x,
        (int) y,
        color,
        backgroundColor,
        dropShadow,
        includeEmpty,
        scissor);

    this.font = font;
    this.text = text;
    this.pose = pose;
    this.x = x;
    this.y = y;
    this.color = color;
    this.backgroundColor = backgroundColor;
    this.dropShadow = dropShadow;
    this.includeEmpty = includeEmpty;
    this.scissor = scissor;
  }

  @Nullable
  @Override
  public ScreenRectangle bounds() {
    this.ensurePrepared();
    return this.bounds;
  }

  @Override
  public void buildVertices(@NonNull VertexConsumer vertexConsumer) {}

  @Override
  public @NonNull RenderPipeline pipeline() {
    return RenderPipelines.GUI_TEXT;
  }

  @Override
  public @NonNull TextureSetup textureSetup() {
    return TextureSetup.noTexture();
  }

  @Override
  @Nullable
  public ScreenRectangle scissorArea() {
    return this.scissor;
  }
}
