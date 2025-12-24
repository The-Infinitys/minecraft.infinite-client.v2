package org.infinite.libs.core.features.categories

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.cancel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import net.minecraft.client.DeltaTracker
import org.infinite.libs.core.features.FeatureCategories
import org.infinite.libs.core.features.categories.category.LocalCategory
import org.infinite.libs.core.features.feature.LocalFeature
import org.infinite.libs.graphics.graphics2d.structs.RenderCommand
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.PriorityBlockingQueue
import kotlin.reflect.KClass

class LocalFeatureCategories(
    categories: List<LocalCategory>,
) : FeatureCategories<KClass<out LocalFeature>, LocalFeature, KClass<out LocalCategory>, LocalCategory>() {
    override val categories: ConcurrentHashMap<KClass<out LocalCategory>, LocalCategory> = ConcurrentHashMap()

    // 接続ごとに作り直すためのスコープ。初期値は null または空のスコープ
    private var connectionScope: CoroutineScope? = null

    init {
        categories.forEach { insert(it) }
    }

    /**
     * サーバー接続時の処理（非同期・並列）
     */
    fun onConnected() {
        // 1. もし前の接続が残っていたらキャンセルして掃除する
        connectionScope?.cancel()

        // 2. 新しい接続用のスコープを作成
        val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
        connectionScope = scope

        // 3. 並列で初期化を実行
        scope.launch {
            try {
                categories.values
                    .map { category ->
                        launch { category.onConnected() }
                    }.joinAll()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    /**
     * サーバー切断時の処理（同期待機）
     */
    fun onDisconnected() {
        // 1. 現在進行中の処理（onConnectedなど）をすべて即座に止める
        connectionScope?.cancel()
        connectionScope = null

        // 2. 終了処理を確実に終わらせるために runBlocking を使用
        runBlocking(Dispatchers.Default) {
            categories.values
                .map { category ->
                    launch {
                        try {
                            category.onDisconnected()
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }.joinAll()
        }
    }

    /**
     * マイクラ終了時
     */
    fun onShutdown() {
        onDisconnected()
    }

    fun onStartTick() {
        connectionScope?.launch {
            try {
                categories.values
                    .map { category ->
                        launch { category.onStartTick() }
                    }.joinAll()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun onEndTick() {
        connectionScope?.launch {
            try {
                categories.values
                    .map { category ->
                        launch { category.onEndTick() }
                    }.joinAll()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    /**
     * UIレンダリング開始時に、その場限りのスコープで全カテゴリーの命令を収集する
     */
    suspend fun onStartUiRendering(deltaTracker: DeltaTracker): List<RenderCommand> {
        val globalCommandQueue = PriorityBlockingQueue<RenderCommand>(512, compareBy { it.zIndex })
        coroutineScope {
            categories.values
                .map { category ->
                    async(Dispatchers.Default) {
                        val queue = category.onStartUiRendering(deltaTracker)
                        while (true) {
                            val cmd = queue.poll() ?: break
                            globalCommandQueue.add(cmd)
                        }
                    }
                }.awaitAll() // 全ての Feature の計算と統合が終わるのを待つ
        }
        return globalCommandQueue.toList()
    }

    suspend fun onEndUiRendering(deltaTracker: DeltaTracker): List<RenderCommand> {
        val globalCommandQueue = PriorityBlockingQueue<RenderCommand>(512, compareBy { it.zIndex })
        coroutineScope {
            categories.values
                .map { category ->
                    async(Dispatchers.Default) {
                        val queue = category.onEndUiRendering(deltaTracker)
                        while (true) {
                            val cmd = queue.poll() ?: break
                            globalCommandQueue.add(cmd)
                        }
                    }
                }.awaitAll() // 全ての Feature の計算と統合が終わるのを待つ
        }
        return globalCommandQueue.toList()
    }
}
