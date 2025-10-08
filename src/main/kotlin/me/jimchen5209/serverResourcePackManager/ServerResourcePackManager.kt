package me.jimchen5209.serverResourcePackManager

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.launch
import me.jimchen5209.serverResourcePackManager.util.ModConfig
import me.jimchen5209.serverResourcePackManager.util.ResourcePackManager
import net.fabricmc.api.DedicatedServerModInitializer
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents
import net.minecraft.server.dedicated.MinecraftDedicatedServer
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import kotlin.coroutines.CoroutineContext

class ServerResourcePackManager : DedicatedServerModInitializer, CoroutineScope {
    private val job = SupervisorJob()
    private lateinit var resourcePackManager: ResourcePackManager
    lateinit var server: MinecraftDedicatedServer
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
        ServerPlayConnectionEvents.JOIN.register { handler, _, _ ->
            resourcePackManager.applyPlayer(handler.player)
        }

        ServerPlayConnectionEvents.DISCONNECT.register { handler, _ ->
            resourcePackManager.removePlayer(handler.player)
        }

        ServerLifecycleEvents.SERVER_STARTING.register {
            server = it as MinecraftDedicatedServer
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
