package me.jimchen5209.serverResourcePackManager.util

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import kotlinx.io.IOException
import me.jimchen5209.serverResourcePackManager.ServerResourcePackManager.Companion.main
import net.fabricmc.loader.api.FabricLoader
import kotlin.io.path.exists
import kotlin.io.path.reader
import kotlin.io.path.writer

class ModConfig {
    private val configPath = FabricLoader.getInstance().configDir.resolve("server-resource-pack-manager.json")
    private val gson: Gson = GsonBuilder().setPrettyPrinting().create()
    var config: ResourcePackConfig = createDefaultConfig()
        private set
    val resourcePacks: MutableMap<String, ResourcePack> = mutableMapOf()

    suspend fun loadConfig() {
        if (configPath.exists()) {
            try {
                configPath.reader().use { reader ->
                    config = gson.fromJson(reader, ResourcePackConfig::class.java)
                }
            }catch (e: IOException) {
                main?.logger?.error("Failed to load config", e)
                config = createDefaultConfig()
                saveConfig()
            }
        } else {
            config = createDefaultConfig()
            saveConfig()
        }
        resourcePacks.clear()

        config.resourcePacks.forEach {
            resourcePacks[it] = ResourcePack.new(it)
        }
    }

    fun saveConfig() {
        try {
            configPath.writer().use { writer ->
                gson.toJson(config, writer)
            }
        } catch (e: IOException) {
            main?.logger?.error("Failed to save config", e)
        }
    }

    private fun createDefaultConfig(): ResourcePackConfig {
        return ResourcePackConfig(
            sendOnJoin = true,
            promptMessage = "",
            required = false,
            resourcePacks = mutableListOf()
        )
    }
}