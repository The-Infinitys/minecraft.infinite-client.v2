package org.infinite.libs.graphics.graphics3d.structs

import net.minecraft.world.phys.AABB
import net.minecraft.world.phys.Vec3
import org.joml.Matrix4f

/**
 * 3D空間での描画命令をカプセル化するデータ構造
 */
sealed interface RenderCommand3D {

    // --- ボックス関連 ---

    /** 単一のボックスを線で描画 */
    data class LinedBox(
        val box: AABB,
        val color: Int,
        val isOverDraw: Boolean = false,
    ) : RenderCommand3D

    /** 複数の色付きボックスを一括描画（バッチ処理用） */
    data class LinedColorBoxes(
        val boxes: List<ColorBox>,
        val isOverDraw: Boolean = false,
    ) : RenderCommand3D

    /** 複数の塗りつぶしボックスを一括描画 */
    data class SolidColorBoxes(
        val boxes: List<ColorBox>,
        val isOverDraw: Boolean = false,
    ) : RenderCommand3D

    // --- プリミティブ関連 ---

    /** 直線を描画 */
    data class Line(
        val start: Vec3,
        val end: Vec3,
        val color: Int,
        val isOverDraw: Boolean = false,
    ) : RenderCommand3D

    /** 複数の直線を描画（Line構造体を使用） */
    data class LinedLines(
        val lines: List<org.infinite.libs.graphics.graphics3d.structs.Line>,
        val isOverDraw: Boolean = false,
    ) : RenderCommand3D

    /** 四角形（平面）の塗りつぶし */
    data class SolidQuads(
        val quads: List<Quad>,
        val isOverDraw: Boolean = false,
    ) : RenderCommand3D

    // --- 特殊描画 ---

    /** プレイヤーの視点（または特定地点）からのトレーサー線 */
    data class Tracer(
        val end: Vec3,
        val color: Int,
        val isOverDraw: Boolean = true,
    ) : RenderCommand3D

    // --- 状態制御 ---

    /** 行列スタックの直接操作が必要な場合（Push/Popの間で使用） */
    data class SetMatrix(val matrix: Matrix4f) : RenderCommand3D

    object PushMatrix : RenderCommand3D
    object PopMatrix : RenderCommand3D
}
