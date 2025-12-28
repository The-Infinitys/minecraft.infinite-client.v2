package org.infinite.libs.core.features.property

import org.infinite.libs.core.features.Property

/**
 * ON/OFFを管理するプロパティ
 * @param default デフォルト値 (true/false)
 */
class BooleanProperty(
    default: Boolean,
) : Property<Boolean>(default) {
    /**
     * 現在の値を反転させます。
     * 親クラスの setter を通るので、スレッド安全かつ通知も飛びます。
     */
    fun toggle() {
        value = !value
    }
}
