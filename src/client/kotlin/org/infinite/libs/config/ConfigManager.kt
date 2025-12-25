package org.infinite.libs.config

import org.infinite.UltimateClient
import org.infinite.libs.interfaces.MinecraftInterface
import org.infinite.libs.log.LogSystem
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
        LogSystem.info("Global config saving...")
        val data = UltimateClient.globalFeatureCategories.data()
        save(File(baseDir, "global.yaml"), data)
        LogSystem.info("Global config saved to global.yaml")
    }

    fun saveLocal() {
        LogSystem.info("Local config saving...")
        val data = UltimateClient.localFeatureCategories.data()
        val path = getLocalPath()
        if (path == null) {
            LogSystem.warn("Local config save skipped: path not available.")
            return
        }
        save(File(baseDir, "local/$path/local.yaml"), data)
        LogSystem.info("Local config saved to local/$path/local.yaml")
    }

    private fun save(file: File, data: Map<String, *>) {
        try {
            if (!file.parentFile.exists()) file.parentFile.mkdirs()
            FileWriter(file).use { writer ->
                yaml.dump(data, writer)
            }
            LogSystem.info("Successfully saved config to ${file.absolutePath}")
        } catch (e: Exception) {
            LogSystem.error("Failed to save config to ${file.absolutePath}: ${e.message}")
            e.printStackTrace()
        }
    }

    // --- Load ---

    fun loadGlobal() {
        LogSystem.info("Loading global config...")
        val file = File(baseDir, "global.yaml")
        if (!file.exists()) {
            LogSystem.warn("Global config file global.yaml not found.")
            return
        }
        val data = load(file)
        applyData(UltimateClient.globalFeatureCategories, data)
        LogSystem.info("Global config loaded from global.yaml")
    }

    fun loadLocal() {
        LogSystem.info("Loading local config...")
        val path = getLocalPath()
        if (path == null) {
            LogSystem.warn("Local config load skipped: path not available.")
            return
        }
        val file = File(baseDir, "local/$path/local.yaml")
        if (!file.exists()) {
            LogSystem.warn("Local config file local/$path/local.yaml not found.")
            return
        }
        val data = load(file)
        applyData(UltimateClient.localFeatureCategories, data)
        LogSystem.info("Local config loaded from local/$path/local.yaml")
    }

    private fun load(file: File): Map<String, Any?> {
        return try {
            FileInputStream(file).use { input ->
                LogSystem.info("Successfully loaded config from ${file.absolutePath}")
                yaml.load<Map<String, Any?>>(input) ?: emptyMap()
            }
        } catch (e: Exception) {
            LogSystem.error("Failed to load config from ${file.absolutePath}: ${e.message}")
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
        val client = client ?: return null
    val isLocalServer = client.isLocalServer
        val serverName = if (isLocalServer) {
            val server = client.singleplayerServer ?: return null
            server.storageSource.levelId
        } else {
            val server = client.currentServer ?: return null
            server.ip
        }
        val prefix = if (isLocalServer) "sp" else "mp"
        return "$prefix/$serverName"
    }
}
