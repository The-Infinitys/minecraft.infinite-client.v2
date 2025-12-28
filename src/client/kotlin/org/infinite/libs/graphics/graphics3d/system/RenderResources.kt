package org.infinite.libs.graphics.graphics3d.system

import net.minecraft.client.renderer.RenderPipelines
import net.minecraft.client.renderer.rendertype.LayeringTransform
import net.minecraft.client.renderer.rendertype.OutputTarget
import net.minecraft.client.renderer.rendertype.RenderSetup
import net.minecraft.client.renderer.rendertype.RenderType

object RenderResources {

    // --- Lines (線描画) ---

    /** 通常の線描画 (デプステストあり) */
    val LINES: RenderType = RenderType.create(
        "infinite:lines",
        RenderSetup.builder(RenderPipelines.LINES)
            .setLayeringTransform(LayeringTransform.VIEW_OFFSET_Z_LAYERING)
            .setOutputTarget(OutputTarget.ITEM_ENTITY_TARGET)
            .createRenderSetup(),
    )

    /** ESP用の線描画 (デプステストなし/壁越し) */
    val ESP_LINES: RenderType = RenderType.create(
        "infinite:esp_lines",
        // Wurstにならい、デプステストがないPipelineや設定を想定
        RenderSetup.builder(RenderPipelines.LINES_TRANSLUCENT)
            .setLayeringTransform(LayeringTransform.VIEW_OFFSET_Z_LAYERING)
            .setOutputTarget(OutputTarget.ITEM_ENTITY_TARGET)
            .createRenderSetup(),
    )

    // --- Quads / Solid (面描画) ---

    /** 通常の面描画 (カリングあり) */
    val QUADS: RenderType = RenderType.create(
        "infinite:quads",
        RenderSetup.builder(RenderPipelines.DEBUG_QUADS) // または適切なSolid Pipeline
            .sortOnUpload()
            .createRenderSetup(),
    )

    /** ESP用の面描画 (デプステストなし) */
    val ESP_QUADS: RenderType = RenderType.create(
        "infinite:esp_quads",
        RenderSetup.builder(RenderPipelines.DEBUG_QUADS)
            .sortOnUpload()
            .createRenderSetup(),
    )

    /** ESP用、かつ裏面も描画するもの */
    val ESP_QUADS_NO_CULLING: RenderType = RenderType.create(
        "infinite:esp_quads_no_culling",
        RenderSetup.builder(RenderPipelines.DEBUG_QUADS)
            .sortOnUpload()
            .useLightmap()
            .createRenderSetup(),
    )

    // --- Getter Methods ---

    /**
     * デプステストの有無(isOverDraw)に応じて、面描画用のRenderTypeを返します。
     */
    fun quads(isOverDraw: Boolean): RenderType = if (isOverDraw) ESP_QUADS else QUADS

    /**
     * デプステストの有無(isOverDraw)に応じて、線描画用のRenderTypeを返します。
     */
    fun lines(isOverDraw: Boolean): RenderType = if (isOverDraw) ESP_LINES else LINES
}
