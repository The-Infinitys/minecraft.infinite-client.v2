package org.infinite.libs.core.features.property

import org.infinite.libs.core.features.Property

open class NumberProperty<T>(
    default: T,
    val min: T,
    val max: T,
    val suffix: String = "",
) : Property<T>(default) where T : Number, T : Comparable<T> {

    // value を書く必要がない（書けない）
    override fun filterValue(newValue: T): T {
        return when {
            newValue < min -> min
            newValue > max -> max
            else -> newValue
        }
    }

    fun display(): String = "${value}$suffix"
}
