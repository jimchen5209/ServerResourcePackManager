# Server ResourcePack Manager

Multi Server ResourcePack Manager from [Galaxy Proxy](https://github.com/OKTW-Network/Galaxy-proxy) on OKTW Network, but for a single dedicated server.

## Features

- Multi ResourcePack introduced in 1.20.3 is supported
- Automatic calculation of ResourcePack hash
- Simple configuration file
- Reload command to update ResourcePacks without server restart
- Send command to re-apply ResourcePacks to player(s)

## Installation

1. Install [Fabric](https://fabricmc.net/use/) modloader on your Minecraft server.
2. Install [Fabric API](https://modrinth.com/mod/fabric-api) and [Fabric Language Kotlin](https://modrinth.com/mod/fabric-language-kotlin).
3. Install the mod from [Releases](https://github.com/jimchen5209/ServerResourcePackManager/releases) or [Modrinth](https://modrinth.com/mod/server-resourcepack-manager).
4. Start server to generate config

## Configuration

The configuration file is located at `config/server-resource-pack-manager.json`. You can add multiple ResourcePacks by specifying their URL.

| Name | Description | Default |
|:--|:--|:--|
| `autoSend` | Automatically send ResourcePack(s) on player join | `true` |
| `promptMessage` | Message displayed to players when prompting for ResourcePack(s) | `""` |
| `required` | Whether to enforce server ResourcePack(s) | `false` |
| `resourcePacks` | List of ResourcePack URLs | `[]` |

### Multi ResourcePack

The order of ResourcePacks in the list determines the order they are applied.

The first ResourcePack will be applied first, followed by the second, and so on. If there are duplicate ResourcePack keys, the first occurrence takes precedence.

#### Example

```json
{
  "autoSend": true,
  "promptMessage": "",
  "required": false,
  "resourcePacks": [
    "https://path/to/resourcepack1.zip",
    "https://path/to/resourcepack2.zip"
  ]
}
```

In this example, `resourcepack1.zip` will be applied first, followed by `resourcepack2.zip`.

Applied resource packs will be shown in the in-game settings menu in this order.

![](https://github.com/user-attachments/assets/18dc5224-7f46-46ce-91e9-562fd8a7b262)

## Commands

- `/resourcePackManager reload` - Reload the configuration file and update ResourcePacks.
- `/resourcePackManager send [player]` - Re-apply ResourcePacks to the specified player, or yourself if no player is specified.

## Development

Recommended IDE: [IntelliJ IDEA](https://www.jetbrains.com/idea/)

This mod is developed using [Fabric](https://fabricmc.net/) modding framework and [Kotlin](https://kotlinlang.org/).

To build the mod, use the Gradle wrapper:

```bash
./gradlew build
```
