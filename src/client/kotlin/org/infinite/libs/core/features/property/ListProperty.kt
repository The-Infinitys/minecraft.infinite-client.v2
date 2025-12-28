package org.infinite.libs.core.features.property

import org.infinite.libs.core.features.Property

/**
 * リスト形式の設定プロパティ
 * @param T リスト内の要素の型
 * @param default デフォルトのリスト内容
 */
open class ListProperty<T>(
    default: List<T>,
) : Property<List<T>>(default.toList()) {

    // 内部的なリスト操作もスレッド安全にする
    private val internalList = java.util.concurrent.CopyOnWriteArrayList<T>(default)

    override fun filterValue(newValue: List<T>): List<T> {
        // リスト全体が差し替えられた場合、内部リストを同期する
        internalList.clear()
        internalList.addAll(newValue)
        return internalList.toList()
    }

    /**
     * 要素を追加し、Propertyの値を更新して通知を飛ばす
     */
    fun add(element: T): Boolean {
        return if (!internalList.contains(element)) {
            internalList.add(element)
            sync() // 値を更新して通知
            true
        } else {
            false
        }
    }

    fun remove(element: T): Boolean {
        return if (internalList.remove(element)) {
            sync()
            true
        } else {
            false
        }
    }

    fun contains(element: T): Boolean = internalList.contains(element)

    /**
     * 現在の internalList の状態を Property.value に反映させ、通知を発生させる
     */
    private fun sync() {
        value = internalList.toList()
    }
}
