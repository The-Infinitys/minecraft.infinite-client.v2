package org.infinite.libs.config

import kotlinx.serialization.*
import kotlinx.serialization.descriptors.*
import kotlinx.serialization.encoding.*
import kotlinx.serialization.json.*
import org.infinite.UltimateClient
import org.infinite.libs.interfaces.MinecraftInterface
import org.infinite.libs.log.LogSystem
import org.infinite.utils.toLowerSnakeCase
import java.io.File

object ConfigManager : MinecraftInterface() {
    private val baseDir = File(client?.run { gameDirectory } ?: File("."), "ultimate/config")

    private val json: Json by lazy {
        Json {
            prettyPrint = true
            isLenient = true
            encodeDefaults = true
            ignoreUnknownKeys = true
        }
    }

    // カスタムシリアライザー: ネストしたMap<String, Any?>を扱う
    object GenericMapSerializer : KSerializer<Map<String, Any?>> {

        override val descriptor: SerialDescriptor = buildClassSerialDescriptor("GenericMap")

        override fun serialize(encoder: Encoder, value: Map<String, Any?>) {
            val jsonObject = JsonObject(value.mapValues { it.value.toJsonElement() })
            val jsonObjectSerializer = JsonObject.serializer()
            jsonObjectSerializer.serialize(encoder, jsonObject)
        }

        override fun deserialize(decoder: Decoder): Map<String, Any?> {
            val jsonDecoder = decoder as? JsonDecoder ?: throw SerializationException("Can only deserialize Json content to generic Map")
            val root = jsonDecoder.decodeJsonElement()
            return if (root is JsonObject) root.toMap() else throw SerializationException("Cannot deserialize Json content to generic Map")
        }

        private fun Any?.toJsonElement(): JsonElement = when (this) {
            null -> JsonNull
            is String -> JsonPrimitive(this)
            is Number -> JsonPrimitive(this)
            is Boolean -> JsonPrimitive(this)
            is Map<*, *> -> toJsonObject()
            is Iterable<*> -> toJsonArray()
            else -> throw SerializationException("Cannot serialize value type $this")
        }

        private fun Map<*, *>.toJsonObject(): JsonObject = JsonObject(
            this.entries.associate {
                it.key.toString() to it.value.toJsonElement()
            },
        )

        private fun Iterable<*>.toJsonArray(): JsonArray = JsonArray(this.map { it.toJsonElement() })

        private fun JsonElement.toAnyNullableValue(): Any? = when (this) {
            is JsonPrimitive -> toScalarOrNull()
            is JsonObject -> toMap()
            is JsonArray -> toList()
        }

        private fun JsonObject.toMap(): Map<String, Any?> = entries.associate {
            when (val jsonElement = it.value) {
                is JsonPrimitive -> it.key to jsonElement.toScalarOrNull()
                is JsonObject -> it.key to jsonElement.toMap()
                is JsonArray -> it.key to jsonElement.toAnyNullableValueList()
            }
        }

        private fun JsonPrimitive.toScalarOrNull(): Any? = when {
            this is JsonNull -> null
            this.isString -> this.content
            else -> listOfNotNull(booleanOrNull, longOrNull, doubleOrNull).firstOrNull()
        }

        private fun JsonArray.toAnyNullableValueList(): List<Any?> = this.map {
            it.toAnyNullableValue()
        }
    }

    // --- Save ---

    fun saveGlobal() {
        LogSystem.info("Global config saving...")
        val data = UltimateClient.globalFeatureCategories.data()
        save(File(baseDir, "global.json"), data)
        LogSystem.info("Global config saved to global.json")
    }

    fun saveLocal() {
        LogSystem.info("Local config saving...")
        val data = UltimateClient.localFeatureCategories.data()
        val path = getLocalPath()
        if (path == null) {
            LogSystem.warn("Local config save skipped: path not available.")
            return
        }
        save(File(baseDir, "local/$path/local.json"), data)
        LogSystem.info("Local config saved to local/$path/local.json")
    }

    private fun save(file: File, data: Map<String, *>) {
        try {
            if (!file.parentFile.exists()) file.parentFile.mkdirs()
            val jsonString = json.encodeToString(GenericMapSerializer, data)
            file.writeText(jsonString)
            LogSystem.info("Successfully saved config to ${file.absolutePath}")
        } catch (e: Exception) {
            LogSystem.error("Failed to save config to ${file.absolutePath}: ${e.message}")
            e.printStackTrace()
        }
    }

    // --- Load ---

    fun loadGlobal() {
        LogSystem.info("Loading global config...")
        val file = File(baseDir, "global.json")
        if (!file.exists()) {
            LogSystem.warn("Global config file global.json not found.")
            return
        }
        val data = load(file)
        applyData(UltimateClient.globalFeatureCategories, data)
        LogSystem.info("Global config loaded from global.json")
    }

    fun loadLocal() {
        LogSystem.info("Loading local config...")
        val path = getLocalPath()
        if (path == null) {
            LogSystem.warn("Local config load skipped: path not available.")
            return
        }
        val file = File(baseDir, "local/$path/local.json")
        if (!file.exists()) {
            LogSystem.warn("Local config file local/$path/local.json not found.")
            return
        }
        val data = load(file)
        applyData(UltimateClient.localFeatureCategories, data)
        LogSystem.info("Local config loaded from local/$path/local.json")
    }

    private fun load(file: File): Map<String, Any?> {
        return try {
            val jsonString = file.readText()
            val data = json.decodeFromString(GenericMapSerializer, jsonString)
            LogSystem.info("Successfully loaded config from ${file.absolutePath}")
            data
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
                    it::class.simpleName?.toLowerSnakeCase() == featureName.toString()
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
            server.name
        }
        val prefix = if (isLocalServer) "sp" else "mp"
        return "$prefix/$serverName"
    }
}
