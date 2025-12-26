package org.infinite

import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents
import org.infinite.features.local.rendering.LocalRenderingCategory
import org.infinite.libs.config.ConfigManager
import org.infinite.libs.core.features.categories.GlobalFeatureCategories
import org.infinite.libs.core.features.categories.LocalFeatureCategories
import org.infinite.libs.core.tick.WorldTicks
import org.infinite.libs.log.LogSystem
import org.infinite.libs.translation.TranslationChecker

object UltimateClient : ClientModInitializer {
    val globalFeatureCategories = GlobalFeatureCategories(listOf())
    val localFeatureCategories = LocalFeatureCategories(listOf(LocalRenderingCategory()))
    val worldTicks = WorldTicks(localFeatureCategories)

    override fun onInitializeClient() {
        LogSystem.init()

        // 1. グローバル設定のロード
        ConfigManager.loadGlobal()
        globalFeatureCategories.onInitialized()
        TranslationChecker.register()
        // --- Server Connection Events ---

        // サーバー接続時 (ログイン成功後)
        ClientPlayConnectionEvents.JOIN.register { _, _, _ ->
            // 2. ローカル設定（サーバー/ワールド別）のロード
            ConfigManager.loadLocal()
            localFeatureCategories.onConnected()
        }

        // サーバー切断時
        ClientPlayConnectionEvents.DISCONNECT.register { _, _ ->
            // 3. ローカル設定の保存（切断時にそのサーバーの状態を保持）
            ConfigManager.saveLocal()
            localFeatureCategories.onDisconnected()
        }

        // --- Tick Events ---
        ClientTickEvents.START_CLIENT_TICK.register { _ ->
            globalFeatureCategories.onStartTick()
        }
        ClientTickEvents.END_CLIENT_TICK.register { _ ->
            globalFeatureCategories.onEndTick()
        }

        worldTicks.register()

        // --- Shutdown (マイクラ終了時) ---
        ClientLifecycleEvents.CLIENT_STOPPING.register { _ ->
            // 4. すべての設定を最終保存
            ConfigManager.saveGlobal()
            ConfigManager.saveLocal() // 念のため現在の接続先も保存

            globalFeatureCategories.onShutdown()
            localFeatureCategories.onShutdown()
        }
    }
}
