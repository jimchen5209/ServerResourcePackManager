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

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonParseException
import kotlinx.io.IOException
import me.jimchen5209.serverResourcePackManager.ServerResourcePackManager.Companion.main
import net.fabricmc.loader.api.FabricLoader
import kotlin.io.path.exists
import kotlin.io.path.reader
import kotlin.io.path.writer

class ModConfig {
    private data class ConfigState(val config: ResourcePackConfig, val resourcePacks: Map<String, ResourcePack>)

    @Volatile
    private var state: ConfigState = ConfigState(createDefaultConfig(), emptyMap())
    val config: ResourcePackConfig get() = state.config

    private val configPath = FabricLoader.getInstance().configDir.resolve("server-resource-pack-manager.json")
    private val gson: Gson = GsonBuilder().setPrettyPrinting().create()

    suspend fun loadConfig() {
        val loadedConfig = if (configPath.exists()) {
            try {
                configPath.reader().use { reader ->
                    gson.fromJson(reader, ResourcePackConfig::class.java)
                }
            } catch (e: JsonParseException) {
                main?.logger?.error("Failed to parse config file, using default config. If you want to start over, remove the corrupted file.", e)
                createDefaultConfig()
            } catch (e: IOException) {
                main?.logger?.error("Failed to read config file, using default config. If you want to start over, remove the corrupted file.", e)
                createDefaultConfig()
            }
        } else {
            createDefaultConfig().also { saveConfig(it) }
        }

        main?.logger?.info("Processing resource packs, please wait...")

        val newResourcePacks = mutableMapOf<String, ResourcePack>()
        loadedConfig.resourcePacks.forEach { url ->
            try {
                newResourcePacks[url] = ResourcePack.new(url)
            } catch (e: IOException) {
                main?.logger?.error("Failed to process resource pack: $url", e)
            }
        }

        this.state = ConfigState(loadedConfig, newResourcePacks)
        main?.logger?.info("Loaded ResourcePacks: ${getMappedResourcePacks().size}")
    }

    fun saveConfig(config: ResourcePackConfig) {
        try {
            configPath.writer().use { writer ->
                gson.toJson(config, writer)
            }
        } catch (e: IOException) {
            main?.logger?.error("Failed to save config", e)
        }
    }

    fun getMappedResourcePacks(): List<ResourcePack> {
        val currentState = this.state // Read volatile field once
        return currentState.config.resourcePacks
            .mapNotNull { currentState.resourcePacks[it] }
            .distinctBy { pack -> pack.hash }
    }

    private fun createDefaultConfig(): ResourcePackConfig {
        return ResourcePackConfig(
            autoSend = true,
            promptMessage = "",
            required = false,
            resourcePacks = mutableListOf()
        )
    }
}
