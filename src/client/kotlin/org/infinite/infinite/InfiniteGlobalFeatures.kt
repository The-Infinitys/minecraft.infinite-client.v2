package org.infinite.infinite

import org.infinite.infinite.features.global.rendering.GlobalRenderingCategory
import org.infinite.libs.core.features.categories.GlobalFeatureCategories

class InfiniteGlobalFeatures : GlobalFeatureCategories() {
    val rendering by category(GlobalRenderingCategory())
}
