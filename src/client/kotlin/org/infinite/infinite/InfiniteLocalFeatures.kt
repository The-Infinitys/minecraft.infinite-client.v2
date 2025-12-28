package org.infinite.infinite

import org.infinite.infinite.features.local.rendering.LocalRenderingCategory
import org.infinite.libs.core.features.categories.LocalFeatureCategories

class InfiniteLocalFeatures : LocalFeatureCategories() {
    val rendering by category(LocalRenderingCategory())
}
