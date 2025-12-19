/*
 * Server ResourcePack Manager
 * Copyright (C) 2025-2025
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package me.jimchen5209.serverResourcePackManager.util

import me.jimchen5209.serverResourcePackManager.ServerResourcePackManager.Companion.main
import net.minecraft.network.chat.Component
import net.minecraft.network.protocol.common.ClientboundResourcePackPopPacket
import net.minecraft.network.protocol.common.ClientboundResourcePackPushPacket
import net.minecraft.server.level.ServerPlayer
import java.util.*
import kotlin.math.min

class ResourcePackManager {
    private val appliedPacks: MutableMap<ServerPlayer, List<ResourcePack>> = mutableMapOf()

    fun updateAllPlayer() {
        val config = main?.configManager?.config ?: return
        val resourcePacks = main?.configManager?.resourcePacks ?: return

        if (!config.autoSend || config.resourcePacks.isEmpty()) {
            return
        }

        appliedPacks.forEach { (player, _) ->
            main?.logger?.info("Reloading resourcePack to ${player.name.string}")

            updatePlayerResourcePacks(
                player,
                resourcePacks,
                config.resourcePacks,
                config.required,
                config.promptMessage
            )
        }
    }

    fun applyPlayer(player: ServerPlayer, isByCommand: Boolean = false) {
        val config = main?.configManager?.config ?: return
        val resourcePacks = main?.configManager?.resourcePacks ?: return

        if ((!config.autoSend && !isByCommand) || config.resourcePacks.isEmpty()) {
            return
        }

        main?.logger?.info("Sending resourcePack to ${player.name.string}")

        updatePlayerResourcePacks(
            player,
            resourcePacks,
            config.resourcePacks,
            config.required,
            config.promptMessage,
            isByCommand
        )
    }

    // Remove player resource packs state
    fun removePlayer(player: ServerPlayer) {
        appliedPacks.remove(player)
    }

    private fun updatePlayerResourcePacks(
        player: ServerPlayer,
        resourcePacks: MutableMap<String, ResourcePack>,
        packs: MutableList<String>,
        required: Boolean,
        promptMessage: String,
        resend: Boolean = false
    ) {
        val new = packs.mapNotNull { resourcePacks[it] }
        val old = this.appliedPacks.getOrElse(player) { listOf() }

        // Matching new resource packs
        var updateIndex = old.size
        if (!resend) {
            for (i in 0..min(old.size, new.size)) {
                if (old.getOrNull(i)?.hash != new.getOrNull(i)?.hash) {
                    updateIndex = i
                    break
                }
            }
        } else {
            updateIndex = 0
        }

        // Remove not match resource packs
        old.subList(updateIndex, old.size).forEach { removePack(player, it) }

        // Add new resource packs
        new.subList(updateIndex, new.size).forEach { sendPack(player, it, required, promptMessage) }

        // Save player resource packs state
        appliedPacks[player] = new.toList()
    }

    private fun removePack(player: ServerPlayer, pack: ResourcePack) {
        player.connection.send(ClientboundResourcePackPopPacket(Optional.of(pack.uuid)))
    }

    private fun sendPack(
        player: ServerPlayer,
        pack: ResourcePack,
        required: Boolean,
        promptMessage: String
    ) {
        val promptText = if (promptMessage.isNotBlank()) {
            Component.literal(promptMessage)
        } else {
            null
        }

        player.connection.send(
            ClientboundResourcePackPushPacket(
                pack.uuid,
                pack.uri.toURL().toString(),
                pack.hash,
                required,
                Optional.ofNullable(promptText)
            )
        )
    }
}