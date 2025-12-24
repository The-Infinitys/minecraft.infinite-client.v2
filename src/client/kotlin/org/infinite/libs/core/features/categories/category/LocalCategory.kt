package org.infinite.libs.core.features.categories.category

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import net.minecraft.client.DeltaTracker
import org.infinite.libs.core.features.Category
import org.infinite.libs.core.features.feature.LocalFeature
import org.infinite.libs.graphics.Graphics2D
import org.infinite.libs.graphics.graphics2d.structs.RenderCommand
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.PriorityBlockingQueue
import kotlin.reflect.KClass

open class LocalCategory : Category<KClass<out LocalFeature>, LocalFeature>() {
    override val features: ConcurrentHashMap<KClass<out LocalFeature>, LocalFeature> = ConcurrentHashMap()

    /**
     * サーバー接続時：配下の全 LocalFeature を並列初期化
     */
    suspend fun onConnected() =
        coroutineScope {
            features.values
                .map { feature ->
                    launch(Dispatchers.Default) {
                        feature.onConnected()
                    }
                }.joinAll()
        }

    /**
     * サーバー切断時：配下の全 LocalFeature の終了処理を並列実行
     */
    suspend fun onDisconnected() =
        coroutineScope {
            features.values
                .map { feature ->
                    launch(Dispatchers.Default) {
                        feature.onDisconnected()
                    }
                }.joinAll()
        }

    suspend fun onStartTick() =
        coroutineScope {
            features.values
                .map { feature ->
                    launch(Dispatchers.Default) {
                        feature.onStartTick()
                    }
                }.joinAll()
        }

    suspend fun onEndTick() =
        coroutineScope {
            features.values
                .map { feature ->
                    launch(Dispatchers.Default) {
                        feature.onEndTick()
                    }
                }.joinAll()
        }

    suspend fun onStartUiRendering(deltaTracker: DeltaTracker): PriorityBlockingQueue<RenderCommand> {
        val globalCommandQueue = PriorityBlockingQueue<RenderCommand>(256, compareBy { it.zIndex })
        coroutineScope {
            features.values
                .map { feature ->
                    async(Dispatchers.Default) {
                        val graphics2D = Graphics2D(deltaTracker)
                        feature.onStartUiRendering(graphics2D)

                        // 2. この feature の計算が終わったら、統合キューへ全命令を移送する
                        // poll() を使って全件抽出
                        while (true) {
                            val cmd = graphics2D.poll() ?: break
                            globalCommandQueue.add(cmd)
                        }
                    }
                }.awaitAll() // 全ての Feature の計算と統合が終わるのを待つ
        }
        return globalCommandQueue
    }

    suspend fun onEndUiRendering(deltaTracker: DeltaTracker): PriorityBlockingQueue<RenderCommand> {
        val globalCommandQueue = PriorityBlockingQueue<RenderCommand>(256, compareBy { it.zIndex })
        coroutineScope {
            features.values
                .map { feature ->
                    async(Dispatchers.Default) {
                        val graphics2D = Graphics2D(deltaTracker)
                        feature.onEndUiRendering(graphics2D)

                        // 2. この feature の計算が終わったら、統合キューへ全命令を移送する
                        // poll() を使って全件抽出
                        while (true) {
                            val cmd = graphics2D.poll() ?: break
                            globalCommandQueue.add(cmd)
                        }
                    }
                }.awaitAll() // 全ての Feature の計算と統合が終わるのを待つ
        }
        return globalCommandQueue
    }
}
