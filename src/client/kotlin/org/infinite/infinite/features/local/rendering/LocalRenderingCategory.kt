package org.infinite.infinite.features.local.rendering

import org.infinite.infinite.features.local.rendering.hello.HelloFeature
import org.infinite.libs.core.features.categories.category.LocalCategory

class LocalRenderingCategory : LocalCategory() {
    val helloFeature by feature(HelloFeature())
}
