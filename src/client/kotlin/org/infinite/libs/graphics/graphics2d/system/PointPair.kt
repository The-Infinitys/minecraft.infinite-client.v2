package org.infinite.libs.graphics.graphics2d.system

import kotlin.math.sqrt

data class PointPair(val ix: Float, val iy: Float, val ox: Float, val oy: Float) {
    companion object {
        fun calculateForMiter(
            currX: Float,
            currY: Float,
            prevX: Float,
            prevY: Float,
            nextX: Float,
            nextY: Float,
            halfWidth: Float,
        ): PointPair {
            // 1. 方向ベクトル
            val d1x = currX - prevX
            val d1y = currY - prevY
            val len1 = sqrt(d1x * d1x + d1y * d1y)
            val v1x = if (len1 < 1e-4f) 0f else d1x / len1
            val v1y = if (len1 < 1e-4f) 0f else d1y / len1

            val d2x = nextX - currX
            val d2y = nextY - currY
            val len2 = sqrt(d2x * d2x + d2y * d2y)
            val v2x = if (len2 < 1e-4f) 0f else d2x / len2
            val v2y = if (len2 < 1e-4f) 0f else d2y / len2

            // 2. 法線ベクトル (左手系: 進行方向に対し左に90度回転)
            val n1x = -v1y
            val n2x = -v2y

            // 3. Miterベクトル (法線の平均方向)
            val miterX = n1x + n2x
            val miterY = v1x + v2x
            val mLenSq = miterX * miterX + miterY * miterY

            // 4. 特殊ケース：ほぼ一直線または折り返し
            if (mLenSq < 1e-4f) {
                // 折り返し、あるいは方向がない場合は、単純に法線方向にオフセット
                return PointPair(
                    ix = currX - n1x * halfWidth,
                    iy = currY - v1x * halfWidth,
                    ox = currX + n1x * halfWidth,
                    oy = currY + v1x * halfWidth,
                )
            }

            // 5. Miterの長さを計算
            // miter と 法線1 の内積を利用
            val mLen = sqrt(mLenSq)
            val dot = (miterX / mLen) * n1x + (miterY / mLen) * v1x
            val scale = halfWidth / dot.coerceAtLeast(0.1f) // 極端な鋭角での破綻防止

            val finalMiterX = (miterX / mLen) * scale
            val finalMiterY = (miterY / mLen) * scale

            return PointPair(
                ix = currX - finalMiterX,
                iy = currY - finalMiterY,
                ox = currX + finalMiterX,
                oy = currY + finalMiterY,
            )
        }
    }
}
