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

package me.jimchen5209.serverResourcePackManager

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.launch
import me.jimchen5209.serverResourcePackManager.command.ResourcePackManagerCommand
import me.jimchen5209.serverResourcePackManager.util.ModConfig
import me.jimchen5209.serverResourcePackManager.util.ResourcePackManager
import net.fabricmc.api.DedicatedServerModInitializer
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents
import net.minecraft.server.dedicated.DedicatedServer
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import kotlin.coroutines.CoroutineContext

class ServerResourcePackManager : DedicatedServerModInitializer, CoroutineScope {
    private val job = SupervisorJob()
    lateinit var resourcePackManager: ResourcePackManager
        private set
    lateinit var server: DedicatedServer
        private set
    override val coroutineContext: CoroutineContext
        get() = job + server.asCoroutineDispatcher()

    companion object {
        var main: ServerResourcePackManager? = null
            private set
    }

    val logger: Logger = LoggerFactory.getLogger("server-resource-pack-manager")

    lateinit var configManager: ModConfig

    override fun onInitializeServer() {
        main = this
        logger.info("Initializing Server ResourcePack Manager")

        registerEvents()
    }

    private fun registerEvents() {
        CommandRegistrationCallback.EVENT.register(CommandRegistrationCallback { dispatcher, _, _ ->
            listOf(ResourcePackManagerCommand()).forEach { dispatcher.let(it::register) }
        })

        ServerPlayConnectionEvents.JOIN.register { handler, _, _ ->
            resourcePackManager.applyPlayer(handler.player)
        }

        ServerPlayConnectionEvents.DISCONNECT.register { handler, _ ->
            resourcePackManager.removePlayer(handler.player)
        }

        ServerLifecycleEvents.SERVER_STARTING.register {
            server = it as DedicatedServer
            resourcePackManager = ResourcePackManager()
            configManager = ModConfig()

            launch {
                configManager.loadConfig()
            }
        }

        ServerLifecycleEvents.SERVER_STARTED.register {
            val config = configManager.config
            logger.info("Loaded ResourcesPacks: ${config.resourcePacks.size}")
        }

        ServerLifecycleEvents.SERVER_STOPPING.register {
            job.complete()
        }
    }
}
