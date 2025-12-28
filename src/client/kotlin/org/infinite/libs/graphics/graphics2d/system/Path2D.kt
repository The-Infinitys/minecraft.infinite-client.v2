package org.infinite.libs.graphics.graphics2d.system

import org.infinite.libs.graphics.graphics2d.structs.StrokeStyle
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.acos
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.math.tan

class Path2D {
    private val currentSegments: MutableList<PathSegment> = mutableListOf()
    private var lastPoint: Pair<Float, Float>? = null
    private var firstPointOfPath: Pair<Float, Float>? = null

    fun beginPath() {
        currentSegments.clear()
        lastPoint = null
        firstPointOfPath = null
    }

    fun moveTo(x: Float, y: Float) {
        lastPoint = x to y
        if (firstPointOfPath == null) firstPointOfPath = x to y
    }

    fun lineTo(x: Float, y: Float, style: StrokeStyle) {
        lastPoint?.let { p0 ->
            currentSegments.add(PathSegment(p0.first, p0.second, x, y, style))
        }
        lastPoint = x to y
    }

    fun closePath(style: StrokeStyle) {
        lastPoint?.let { p0 ->
            firstPointOfPath?.let { pStart ->
                if (p0 != pStart) {
                    currentSegments.add(PathSegment(p0.first, p0.second, pStart.first, pStart.second, style))
                }
            }
        }
        lastPoint = firstPointOfPath
    }

    fun arc(x: Float, y: Float, radius: Float, startAngle: Float, endAngle: Float, counterclockwise: Boolean = false, style: StrokeStyle) {
        // 現在地がない場合は、弧の開始点へ移動
        if (lastPoint == null) moveTo(x + cos(startAngle) * radius, y + sin(startAngle) * radius)

        val twoPi = 2 * PI.toFloat()

        // 角度の差分を計算
        var diff = endAngle - startAngle

        // 方向と角度差の調整
        if (!counterclockwise) {
            if (diff <= 0) diff += twoPi
        } else {
            if (diff >= 0) diff -= twoPi
        }

        // 分割数の決定（角度の絶対値に基づいて決定）
        val segments = (abs(diff) / (PI.toFloat() / 12f)).toInt().coerceAtLeast(2)
        val step = diff / segments

        for (i in 1..segments) {
            val a = startAngle + step * i
            lineTo(x + cos(a) * radius, y + sin(a) * radius, style)
        }

        // 浮動小数点の誤差を避けるため、最後に確実にターゲットの終点をセットする
        lastPoint = (x + cos(endAngle) * radius) to (y + sin(endAngle) * radius)
    }
    fun arcTo(x1: Float, y1: Float, x2: Float, y2: Float, radius: Float, style: StrokeStyle) {
        val p0 = lastPoint ?: return
        val p0x = p0.first
        val p0y = p0.second

        // ベクトル1 (P0 -> P1), ベクトル2 (P1 -> P2)
        val dx1 = x1 - p0x
        val dy1 = y1 - p0y
        val dx2 = x2 - x1
        val dy2 = y2 - y1

        val len1 = sqrt(dx1 * dx1 + dy1 * dy1)
        val len2 = sqrt(dx2 * dx2 + dy2 * dy2)

        if (len1 < 1e-6 || len2 < 1e-6 || radius <= 0f) {
            lineTo(x1, y1, style)
            return
        }

        // 単位ベクトル
        val u1x = dx1 / len1
        val u1y = dy1 / len1
        val u2x = dx2 / len2
        val u2y = dy2 / len2

        // 角度 theta
        val cosTheta = u1x * u2x + u1y * u2y
        val angle = acos(cosTheta.coerceIn(-1f, 1f))

        // 接点までの距離
        val tangentDist = radius / tan(angle / 2f)

        // 接点1 (開始) と 接点2 (終了)
        val t1x = x1 - u1x * tangentDist
        val t1y = y1 - u1y * tangentDist
        val t2x = x1 + u2x * tangentDist
        val t2y = y1 + u2y * tangentDist

        // 円心の計算（接点1から法線方向へradius分移動）
        val cross = u1x * u2y - u1y * u2x
        val isClockwise = cross > 0
        val nx = if (isClockwise) u1y else -u1y
        val ny = if (isClockwise) -u1x else u1x

        val cx = t1x + nx * radius
        val cy = t1y + ny * radius

        val startA = atan2(t1y - cy, t1x - cx)
        val endA = atan2(t2y - cy, t2x - cx)

        // 重要: P0から最初の接点まで直線を引く
        lineTo(t1x, t1y, style)
        // 弧を描画（arc内でlastPointがt2まで更新されるようにする）
        arc(cx, cy, radius, startA, endA, !isClockwise, style)
    }

    fun bezierCurveTo(cp1x: Float, cp1y: Float, cp2x: Float, cp2y: Float, x: Float, y: Float, style: StrokeStyle) {
        val p0 = lastPoint ?: return
        val p0x = p0.first
        val p0y = p0.second

        val segments = 30
        for (i in 1..segments) {
            val t = i.toFloat() / segments
            val it = 1f - t
            val ptx = it * it * it * p0x + 3 * it * it * t * cp1x + 3 * it * t * t * cp2x + t * t * t * x
            val pty = it * it * it * p0y + 3 * it * it * t * cp1y + 3 * it * t * t * cp2y + t * t * t * y
            lineTo(ptx, pty, style)
        }
    }

    fun getSegments(): List<PathSegment> = currentSegments
    fun clearSegments() = beginPath()
}
