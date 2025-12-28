package org.infinite.libs.level

import net.minecraft.network.protocol.game.ClientboundBlockUpdatePacket
import net.minecraft.network.protocol.game.ClientboundLevelChunkPacketData
import net.minecraft.network.protocol.game.ClientboundSectionBlocksUpdatePacket

/**
 * ワールド（Level）内の更新イベント（チャンク読み込み、ブロック更新）を管理するクラス
 */
class LevelManager {
    sealed class Chunk {
        /**
         * 新規チャンクデータの受信
         */
        class Data(
            val x: Int,
            val z: Int,
            val data: ClientboundLevelChunkPacketData,
        ) : Chunk()

        /**
         * 単一ブロックの更新
         */
        class BlockUpdate(
            val packet: ClientboundBlockUpdatePacket,
        ) : Chunk()

        /**
         * チャンクセクション（16x16x16）内の複数ブロック更新
         */
        class DeltaUpdate(
            val packet: ClientboundSectionBlocksUpdatePacket,
        ) : Chunk()
    }

    // 更新イベントを保持するキュー
    val queue: ArrayDeque<Chunk> = ArrayDeque()

    /**
     * チャンクデータ受信時の処理
     */
    fun handleChunkLoad(
        x: Int,
        z: Int,
        chunkData: ClientboundLevelChunkPacketData,
    ) {
        queue.addLast(Chunk.Data(x, z, chunkData))
    }

    /**
     * 複数ブロックの更新パケットをキューに追加
     */
    fun handleDeltaUpdate(packet: ClientboundSectionBlocksUpdatePacket) {
        queue.addLast(Chunk.DeltaUpdate(packet))
    }

    /**
     * 単一ブロックの更新パケットをキューに追加
     */
    fun handleBlockUpdate(packet: ClientboundBlockUpdatePacket) {
        queue.addLast(Chunk.BlockUpdate(packet))
    }
}
