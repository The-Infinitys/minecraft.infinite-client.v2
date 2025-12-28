package org.infinite.libs.core.features.property

import org.infinite.libs.core.features.Property

/**
 * 複数の選択肢から1つを選択するプロパティ
 */
open class SelectionProperty<T>(
    default: T,
    opts: List<T>,
) : Property<T>(default) {
    open val options: List<T> = opts
    override fun filterValue(newValue: T): T {
        // 選択肢にない場合は現在の値を維持（変更を拒否）
        return if (options.contains(newValue)) newValue else value
    }

    fun next() {
        val currentIndex = options.indexOf(value)
        if (currentIndex != -1) {
            value = options[(currentIndex + 1) % options.size]
        }
    }
}
