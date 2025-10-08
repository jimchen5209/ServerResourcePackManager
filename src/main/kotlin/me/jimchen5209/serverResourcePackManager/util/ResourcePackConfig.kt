package me.jimchen5209.serverResourcePackManager.util

data class ResourcePackConfig(
    var sendOnJoin: Boolean = true,
    var promptMessage: String = "",
    var required: Boolean = false,
    var resourcePacks: MutableList<String> = mutableListOf()
)
