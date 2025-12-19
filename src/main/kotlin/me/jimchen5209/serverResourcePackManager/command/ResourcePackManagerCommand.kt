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
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands
import net.minecraft.commands.arguments.GameProfileArgument
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer

class ResourcePackManagerCommand : Command {
    override fun register(dispatcher: CommandDispatcher<CommandSourceStack>) {
        dispatcher.register(
            Commands.literal("resourcePackManager")
            .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
            .then(Commands.literal("reload").executes { context -> reloadConfig(context.source) })
            .then(
                Commands.literal("send").then(
                    Commands.argument("player", GameProfileArgument.gameProfile()).executes { context ->
                            val targetPlayer = GameProfileArgument.getGameProfiles(context, "player").first()
                            val player = main?.server?.playerList?.getPlayer(targetPlayer.id)
                                ?: return@executes com.mojang.brigadier.Command.SINGLE_SUCCESS
                            context.source.sendSuccess({
                                Component.literal("Sending resourcePack to ").append(player.name)
                            }, true)
                            sendPack(player)
                            return@executes com.mojang.brigadier.Command.SINGLE_SUCCESS
                        }).executes { context ->
                    val player = context.source.player
                    if (player == null) {
                        context.source.sendSystemMessage(Component.literal("Player is required for this command."))
                        return@executes com.mojang.brigadier.Command.SINGLE_SUCCESS
                    }
                    context.source.sendSystemMessage(Component.literal("Reloading resource pack..."))
                    sendPack(player)
                    return@executes com.mojang.brigadier.Command.SINGLE_SUCCESS
                }))
    }

    private fun reloadConfig(source: CommandSourceStack): Int {
        source.sendSuccess({ Component.literal("Reloading config...") }, true)
        main?.launch {
            main?.configManager?.loadConfig()
            main?.resourcePackManager?.updateAllPlayer()
            source.sendSuccess({ Component.literal("Reload completed!") }, true)
            main?.logger?.info("Loaded ResourcesPacks: ${main?.configManager?.config?.resourcePacks?.size}")
        }
        return com.mojang.brigadier.Command.SINGLE_SUCCESS
    }

    private fun sendPack(player: ServerPlayer) {
        main?.resourcePackManager?.applyPlayer(player, true)
    }
}
