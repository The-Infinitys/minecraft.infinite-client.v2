package org.infinite.libs.config

import org.infinite.UltimateClient
import org.infinite.libs.interfaces.MinecraftInterface
import org.infinite.utils.toLowerSnakeCase
import org.yaml.snakeyaml.DumperOptions
import org.yaml.snakeyaml.Yaml
import java.io.File
import java.io.FileInputStream
import java.io.FileWriter

object ConfigManager : MinecraftInterface() {
    private val baseDir = File(client?.run { gameDirectory } ?: File("."), "ultimate/config")

    private val yaml: Yaml by lazy {
        val options = DumperOptions().apply {
            defaultFlowStyle = DumperOptions.FlowStyle.BLOCK // 読みやすいブロック形式
            isPrettyFlow = true
        }
        Yaml(options)
    }

    // --- Save ---

    fun saveGlobal() {
        val data = UltimateClient.globalFeatureCategories.data()
        save(File(baseDir, "global.yaml"), data)
    }

    fun saveLocal() {
        val data = UltimateClient.localFeatureCategories.data()
        val path = getLocalPath() ?: return
        save(File(baseDir, "local/$path/local.yaml"), data)
    }

    private fun save(file: File, data: Map<String, *>) {
        try {
            if (!file.parentFile.exists()) file.parentFile.mkdirs()
            FileWriter(file).use { writer ->
                yaml.dump(data, writer)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // --- Load ---

    fun loadGlobal() {
        val file = File(baseDir, "global.yaml")
        if (!file.exists()) return
        val data = load(file)
        applyData(UltimateClient.globalFeatureCategories, data)
    }

    fun loadLocal() {
        val path = getLocalPath() ?: return
        val file = File(baseDir, "local/$path/local.yaml")
        if (!file.exists()) return
        val data = load(file)
        applyData(UltimateClient.localFeatureCategories, data)
    }

    private fun load(file: File): Map<String, Any?> {
        return try {
            FileInputStream(file).use { input ->
                yaml.load<Map<String, Any?>>(input) ?: emptyMap()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyMap()
        }
    }

    // --- Helpers ---

    /**
     * ロードしたデータを FeatureCategories -> Category -> Feature -> Property へ反映
     */
    private fun applyData(
        categoriesObj: org.infinite.libs.core.features.FeatureCategories<*, *, *, *>,
        data: Map<String, Any?>,
    ) {
        data.forEach { (categoryName, featuresData) ->
            if (featuresData !is Map<*, *>) return@forEach

            // カテゴリを取得 (名前の比較ロジックは必要に応じて調整)
            val category = categoriesObj.categories.values.find {
                it::class.qualifiedName?.split(".")
                    ?.let { p -> if (p.size >= 2) p[p.size - 2].toLowerSnakeCase() else null } == categoryName
            } ?: return@forEach

            featuresData.forEach { (featureName, propData) ->
                if (propData !is Map<*, *>) return@forEach

                // Featureを取得
                val feature = category.features.values.find {
                    it::class.simpleName?.toLowerSnakeCase() == featureName
                } ?: return@forEach

                // プロパティをセット
                propData.forEach { (propName, value) ->
                    if (propName !is String) return@forEach
                    feature.set(propName, value)
                }
            }
        }
    }

    private fun getLocalPath(): String? {
        val c = client ?: return null
        return if (c.isLocalServer) {
            val server = c.level?.server ?: return null
            val serverName = server.name() ?: return null
            "sp/$serverName"
        } else {
            // マルチプレイヤー: サーバーIP/名前
            val server = c.currentServer ?: return null
            val serverName = server.name
            "mp/$serverName"
        }
    }
}
