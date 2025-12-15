package org.infinite.libs.core.features

open class Feature {
    open val properties: HashMap<String, Property> = HashMap.newHashMap(0)

    init {
        val kClass = this::class
        val qualifiedName: String? = kClass.qualifiedName
    }
}
