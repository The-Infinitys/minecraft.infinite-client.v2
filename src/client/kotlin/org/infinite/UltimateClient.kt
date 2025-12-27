package org.infinite

import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents
import org.infinite.libs.config.ConfigManager
import org.infinite.libs.core.tick.WorldTicks
import org.infinite.libs.log.LogSystem
import org.infinite.libs.translation.TranslationChecker
import org.infinite.libs.ui.theme.Theme
import org.infinite.libs.ui.theme.ThemeManager
import org.infinite.ultimate.UltimateGlobalFeatures
import org.infinite.ultimate.UltimateLocalFeatures
import org.infinite.ultimate.theme.DefaultTheme

object UltimateClient : ClientModInitializer {
    val globalFeatures = UltimateGlobalFeatures()
    val localFeatures = UltimateLocalFeatures()
    val worldTicks = WorldTicks(localFeatures)
    val themeManager: ThemeManager = ThemeManager(DefaultTheme())
    val theme: Theme
        get() {
            val themeName = globalFeatures.rendering.themeFeature.currentTheme.value
            return themeManager.getTheme(themeName)
        }

    override fun onInitializeClient() {
        LogSystem.init()

        // 1. グローバル設定のロード
        ConfigManager.loadGlobal()
        globalFeatures.onInitialized()
        TranslationChecker.register()
        localFeatures.registerAllActions()
        // サーバー接続時 (ログイン成功後)
        ClientPlayConnectionEvents.JOIN.register { _, _, _ ->
            // 2. ローカル設定（サーバー/ワールド別）のロード
            ConfigManager.loadLocal()
            localFeatures.onConnected()
        }

        // サーバー切断時
        ClientPlayConnectionEvents.DISCONNECT.register { _, _ ->
            // 3. ローカル設定の保存（切断時にそのサーバーの状態を保持）
            ConfigManager.saveLocal()
            localFeatures.onDisconnected()
        }

        // --- Tick Events ---
        ClientTickEvents.START_CLIENT_TICK.register { _ ->
            globalFeatures.onStartTick()
        }
        ClientTickEvents.END_CLIENT_TICK.register { _ ->
            globalFeatures.onEndTick()
        }

        worldTicks.register()

        // --- Shutdown (マイクラ終了時) ---
        ClientLifecycleEvents.CLIENT_STOPPING.register { _ ->
            // 4. すべての設定を最終保存
            ConfigManager.saveGlobal()
            ConfigManager.saveLocal() // 念のため現在の接続先も保存

            globalFeatures.onShutdown()
            localFeatures.onShutdown()
        }
    }
}
