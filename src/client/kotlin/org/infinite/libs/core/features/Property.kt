package org.infinite.libs.core.features

import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.atomic.AtomicReference

open class Property<T>(
    val default: T,
) {
    private val _value = AtomicReference<T>(default)
    private val listeners = CopyOnWriteArrayList<(oldValue: T, newValue: T) -> Unit>()

    // finalにする（オーバーライド禁止）
    // これにより、すべての子クラスで必ずこの setter (Atomic + Notify) が走る
    var value: T
        get() = _value.get()
        set(newValue) {
            // 子クラスで定義したフィルタを通す
            val filtered = filterValue(newValue)
            val oldValue = _value.getAndSet(filtered)
            if (oldValue != filtered) {
                notifyListeners(oldValue, filtered)
            }
        }

    /**
     * 子クラスで値を制限したい場合にオーバーライドする。
     * デフォルトではそのままの値を返す。
     */
    protected open fun filterValue(newValue: T): T = newValue

    fun reset() {
        value = default
    }

    // --- 以下、addListener / notifyListeners は前回と同じ ---
    fun addListener(listener: (oldValue: T, newValue: T) -> Unit) = listeners.add(listener)
    fun removeListener(listener: (oldValue: T, newValue: T) -> Unit) = listeners.remove(listener)
    protected fun notifyListeners(oldValue: T, newValue: T) {
        listeners.forEach { it(oldValue, newValue) }
    }
}
