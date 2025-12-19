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

package me.jimchen5209.serverResourcePackManager.command

import com.mojang.brigadier.CommandDispatcher
import kotlinx.coroutines.launch
import me.jimchen5209.serverResourcePackManager.ServerResourcePackManager.Companion.main
import net.minecraft.command.argument.GameProfileArgumentType
import net.minecraft.server.command.CommandManager
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Text

class ResourcePackManagerCommand : Command {
    override fun register(dispatcher: CommandDispatcher<ServerCommandSource>) {
        dispatcher.register(
            CommandManager.literal("resourcePackManager")
            .requires(CommandManager.requirePermissionLevel(CommandManager.GAMEMASTERS_CHECK))
            .then(CommandManager.literal("reload").executes { context -> reloadConfig(context.source) })
            .then(
                CommandManager.literal("send").then(
                    CommandManager.argument("player", GameProfileArgumentType.gameProfile()).executes { context ->
                            val targetPlayer = GameProfileArgumentType.getProfileArgument(context, "player").first()
                            val player = main?.server?.playerManager?.getPlayer(targetPlayer.id)
                                ?: return@executes com.mojang.brigadier.Command.SINGLE_SUCCESS
                            context.source.sendFeedback({
                                Text.literal("Sending resourcePack to ").append(player.name)
                            }, true)
                            sendPack(player)
                            return@executes com.mojang.brigadier.Command.SINGLE_SUCCESS
                        }).executes { context ->
                    val player = context.source.player
                    if (player == null) {
                        context.source.sendMessage(Text.of("Player is required for this command."))
                        return@executes com.mojang.brigadier.Command.SINGLE_SUCCESS
                    }
                    context.source.sendMessage(Text.of("Reloading resource pack..."))
                    sendPack(player)
                    return@executes com.mojang.brigadier.Command.SINGLE_SUCCESS
                }))
    }

    private fun reloadConfig(source: ServerCommandSource): Int {
        source.sendFeedback({ Text.of("Reloading config...") }, true)
        main?.launch {
            main?.configManager?.loadConfig()
            main?.resourcePackManager?.updateAllPlayer()
            source.sendFeedback({ Text.of("Reload completed!") }, true)
            main?.logger?.info("Loaded ResourcesPacks: ${main?.configManager?.config?.resourcePacks?.size}")
        }
        return com.mojang.brigadier.Command.SINGLE_SUCCESS
    }

    private fun sendPack(player: ServerPlayerEntity) {
        main?.resourcePackManager?.applyPlayer(player, true)
    }
}
