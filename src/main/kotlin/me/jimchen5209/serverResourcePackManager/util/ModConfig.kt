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