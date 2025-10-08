package me.jimchen5209.serverResourcePackManager.util

import me.jimchen5209.serverResourcePackManager.ServerResourcePackManager.Companion.main
import net.minecraft.network.packet.s2c.common.ResourcePackRemoveS2CPacket
import net.minecraft.network.packet.s2c.common.ResourcePackSendS2CPacket
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Text
import java.util.*
import kotlin.math.min

class ResourcePackManager {
    private val appliedPacks: MutableMap<ServerPlayerEntity, List<ResourcePack>> = mutableMapOf()

    fun applyPlayer(player: ServerPlayerEntity) {
        val config = main?.configManager?.config ?: return
        val resourcePacks = main?.configManager?.resourcePacks ?: return

        if (!config.sendOnJoin || config.resourcePacks.isEmpty()) {
            return
        }

        main?.logger?.info("Sending resourcePack to ${player.name.string}")

        updatePlayerResourcePacks(player, resourcePacks, config.resourcePacks, config.required, config.promptMessage)
    }

    // Remove player resource packs state
    fun removePlayer(player: ServerPlayerEntity) {
        appliedPacks.remove(player)
    }

    private fun updatePlayerResourcePacks(
        player: ServerPlayerEntity,
        resourcePacks: MutableMap<String, ResourcePack>,
        packs: MutableList<String>,
        required: Boolean,
        promptMessage: String
    ) {
        val new = packs.mapNotNull { resourcePacks[it] }
        val old = this.appliedPacks.getOrElse(player) { listOf() }

        // Matching new resource packs
        var updateIndex = old.lastIndex
        for (i in 0..min(old.size, new.size)) {
            if (!old.getOrNull(i)?.hash.contentEquals(new.getOrNull(i)?.hash)) {
                updateIndex = i
                break
            }
        }

        // Remove not match resource packs
        old.subList(updateIndex, old.size).forEach { removePack(player, it) }

        // Add new resource packs
        new.subList(updateIndex, new.size).forEach { sendPack(player, it, required, promptMessage) }

        // Save player resource packs state
        appliedPacks[player] = new.toList()
    }

    private fun removePack(player: ServerPlayerEntity, pack: ResourcePack) {
        player.networkHandler.sendPacket(ResourcePackRemoveS2CPacket(Optional.of(pack.uuid)))
    }

    private fun sendPack(
        player: ServerPlayerEntity,
        pack: ResourcePack,
        required: Boolean,
        promptMessage: String
    ) {
        val promptText = if (promptMessage.isNotBlank()) {
            Text.of(promptMessage)
        } else {
            null
        }

        player.networkHandler.sendPacket(
            ResourcePackSendS2CPacket(
                pack.uuid,
                pack.uri.toURL().toString(),
                pack.hash,
                required,
                Optional.ofNullable(promptText)
            )
        )
    }
}