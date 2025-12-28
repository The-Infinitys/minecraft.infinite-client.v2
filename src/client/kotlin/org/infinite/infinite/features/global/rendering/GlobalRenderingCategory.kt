package org.infinite.infinite.features.global.rendering

import org.infinite.infinite.features.global.rendering.font.InfiniteFontFeature
import org.infinite.infinite.features.global.rendering.theme.ThemeFeature
import org.infinite.libs.core.features.categories.category.GlobalCategory

class GlobalRenderingCategory : GlobalCategory() {
    val infiniteFontFeature by feature(InfiniteFontFeature())
    val themeFeature by feature(ThemeFeature())
}
