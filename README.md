
# Dynamic ShopGUI+

**A powerful, feature-rich and lightweight alternative to premium Minecraft shop plugins.**

Dynamic ShopGUI+ is a fully featured, highly configurable in-game shop system designed for modern Minecraft servers. It provides everything needed to build and manage an advanced server economy, from traditional buy/sell shops to dynamic pricing, stock management and multi-server synchronization.

Built with performance, flexibility and compatibility in mind, Dynamic ShopGUI+ brings premium-level shop functionality without the premium price tag.

> Free. Feature-rich. Lightweight. Built for everyone.

---

## ✨ Features

Dynamic ShopGUI+ offers an extensive collection of features for server owners, administrators and players.

### 🛒 Shop System

- Fully customizable in-game shop GUI.
- Categories, sub-sections and multi-page support.
- Configurable buy and sell prices.
- Custom item support.
- Enchanted item variants.
- Damaged item pricing.
- Item requirements based on time, quests and regions.
- In-game GUI configuration editor.
- Physical shop stands for direct purchases.

### 💰 Economy & Pricing

- Full buy/sell economy integration.
- Dynamic pricing based on supply and demand.
- Permission-based discounts.
- Permission-based sell multipliers.
- Advanced multi-price modifiers.
- Condition-based pricing.
- Seasonal pricing.
- Per-player and global stock limits.
- Configurable stock reset periods and time windows.

### 📦 Selling & Inventory Management

- `/sellall` with inventory, hand and specific-item support.
- Dedicated bulk-selling GUI.
- Configurable sell restrictions.
- Support for custom and third-party items.
- Flexible selling conditions.

### ⚙️ Advanced Integrations

- Command execution on item purchase.
- Delayed and scheduled command execution.
- Custom item integrations.
- Spawner support across multiple popular spawner plugins.
- PlaceholderAPI support.
- Discord transaction logging.
- Bedrock player support through Geyser and Floodgate.

### 🌐 Network & Compatibility

- Shared MySQL storage for multi-server networks.
- Synchronization of shop data across backend servers.
- Folia-compatible implementation.
- Dedicated proxy companion plugin for Velocity and BungeeCord.
- Support for multiple Bukkit-based server implementations.

---

## 🖥️ Compatibility

| Component | Support |
|---|---|
| Paper | ✅ |
| Purpur | ✅ |
| Spigot | ✅ |
| Bukkit | ✅ |
| Folia | ✅ |
| Velocity | Via Proxy companion |
| BungeeCord | Via Proxy companion |
| Minecraft | 1.21+ |
| Java | 21+ |

**Note:** The backend plugin must be installed on each Minecraft backend server. The proxy companion is installed separately on the network's proxy server.

Platform-specific implementations are used where necessary to maintain compatibility.

---

## 📥 Installation

### 1. Standalone Server

1. Download the latest Dynamic ShopGUI+ release.
2. Place the JAR inside your server's `plugins` directory.
3. Ensure Vault and a compatible economy provider are installed.
4. Restart your server.
5. Configure your shop using the generated configuration files.

That's it! Your shop is ready to configure.

### 2. Proxy Network (Velocity / BungeeCord)

For a multi-server network:

1. Install Dynamic ShopGUI+ on every backend server.
2. Install Dynamic ShopGUI+ Proxy on your Velocity or BungeeCord proxy.
3. Configure the proxy companion as documented in its repository.
4. Restart the proxy and backend servers.
5. Verify that proxy commands are correctly forwarded.

The proxy companion forwards supported commands to the backend server the player is currently connected to. The backend plugin handles the actual shop functionality.

### 3. Shared Shop Data Across Multiple Backends

If you want all backend servers to use the same shop configuration and data:

1. Set up a reachable MySQL database.
2. Create a dedicated database and user for Dynamic ShopGUI+.
3. Configure the same MySQL connection details on every participating backend.
4. Enable `proxy: true` in the backend configuration.
5. Configure the shared storage settings.
6. Restart the backend servers.

All participating backends will connect directly to the same MySQL database.

**Important:** Shared storage is optional. A proxy network can use independent shop configurations on each backend without enabling shared MySQL storage.

---

## 🔌 Related Plugin: Dynamic ShopGUI+ Proxy

Dynamic ShopGUI+ Proxy is the companion plugin for proxy-based Minecraft networks.

It provides lightweight command forwarding between the proxy and backend instances.

### How it works

1. A player executes a supported shop command on the proxy.
2. The proxy companion identifies the backend server the player is connected to.
3. The command is forwarded through plugin messaging.
4. The backend plugin processes the command.
5. The backend handles the requested functionality normally.

### Supported commands

- `/shop`
- `/sellall`
- `/sellgui`
- Supported administrative commands

### When do you need it?

| Setup | Backend Plugin | Proxy Plugin | Shared MySQL |
|---|---|---|---|
| Single server | Required | Not needed | Not needed |
| Proxy network, independent shops | Required on each backend | Required | Not needed |
| Proxy network, shared shops | Required on each backend | Required | Required |

The proxy companion does not host or manage the shared database. Each backend connects directly to MySQL when shared storage is enabled.

**Repository:** Dynamic ShopGUI+ Proxy (separate project)

---

## 🗄️ Shared MySQL Storage

Dynamic ShopGUI+ optionally supports centralized shop storage for multi-server networks.

When enabled, multiple backend servers can use the same shop data instead of maintaining independent configurations.

### How it works

- A central MySQL database stores shared shop data.
- Every participating backend connects directly to that database.
- Each backend maintains its own in-memory cache.
- Changes are synchronized between backend instances.
- No backend needs to be designated as the primary server.

### Database hosting

The database can be hosted:

- On an existing Minecraft server machine.
- On a separate VPS.
- Through an external MySQL hosting provider.

The database host does not need to be the proxy server.

### Storage modes

| Mode | Behaviour |
|---|---|
| Standalone (`proxy: false`) | Existing independent storage behaviour |
| Shared (`proxy: true`) | Centralized MySQL storage |

Shared storage is disabled by default and does not affect standalone installations.

---

## 🔗 Dependencies

### Required

| Dependency | Purpose |
|---|---|
| Vault | Economy integration |

A compatible economy provider must also be installed.

### Optional Integrations

The following integrations are supported when their respective plugins are installed.

| Integration | Supported Plugins |
|---|---|
| Placeholders | PlaceholderAPI |
| Region protection | WorldGuard |
| Quests | Quests, BeautyQuests, BetonQuest |
| Seasons | RealisticSeasons |
| Discord logging | DiscordSRV |
| Bedrock support | Floodgate, Geyser-Spigot |
| Custom items | ItemsAdder, Oraxen, Nexo |
| Spawners | SilkSpawners, SilkSpawners_v2, WildStacker, UltimateStacker, RoseStacker, MineableSpawners, SpawnerMeta, SpawnerLegacy, SmartSpawner |
| Protocol compatibility | ViaVersion, ProtocolSupport |
| Essentials integration | Essentials, EssentialsChat |
| Economy providers | GemsEconomy, PlayerPoints, CoinsEngine, ExcellentEconomy, UltraEconomy, VotingPlugin, zEssentials, VaultUnlocked |

Optional integrations are detected when available. You do not need to install every listed plugin.

---

## ⌨️ Commands

| Command | Aliases | Description | Permission |
|---|---|---|---|
| `/shop` | `market` | Opens the shop GUI | `dynamicshopgui.shop` |
| `/sellall` | — | Sells items from inventory | `dynamicshopgui.sellall` |
| `/sellgui` | — | Opens the bulk-selling GUI | `dynamicshopgui.sellgui` |
| `/shopreload` | `sreload` | Reloads plugin configuration and shops | `dynamicshopgui.reload` |
| `/shopgive` | — | Gives a shop item to a player | `dynamicshopgui.shopgive` |
| `/editshop` | `eshop` | Opens the in-game shop editor | `dynamicshopgui.admin` |
| `/shopmigrate` | — | Migrates standalone data to shared MySQL | `dynamicshopgui.admin` |

---

## 🔐 Permissions

### Player Permissions

| Permission | Default | Description |
|---|---|---|
| `dynamicshopgui.shop` | `true` | Access the shop |
| `dynamicshopgui.shop.<section>` | `true` | Access a specific shop section |
| `dynamicshopgui.sellall` | `true` | Use `/sellall` |
| `dynamicshopgui.sellall.<section>` | `true` | Sell items from a specific section |
| `dynamicshopgui.sellallitem` | `true` | Use `/sellall <item>` |
| `dynamicshopgui.sellallhand` | `true` | Use `/sellall hand` |
| `dynamicshopgui.sellgui` | `true` | Access the selling GUI |

### Administrative Permissions

| Permission | Default | Description |
|---|---|---|
| `dynamicshopgui.reload` | `op` | Reload plugin configuration |
| `dynamicshopgui.shopgive` | `op` | Give shop items |
| `dynamicshopgui.admin` | `op` | Access administrative functionality and migration |
| `dynamicshopgui.bypassgamemode` | `op` | Bypass banned gamemode restrictions |

### Advanced Permissions

| Permission | Description |
|---|---|
| `dynamicshopgui.discounts.<group>` | Grants eligibility for a configured discount group |
| `dynamicshopgui.sell-multipliers.<group>` | Grants eligibility for a configured sell multiplier group |

---

## 📊 Metrics & Analytics

Dynamic ShopGUI+ supports bStats for anonymous plugin usage statistics.

Metrics help track general plugin adoption and usage, allowing development decisions to be guided by real-world usage.

---

## 🛠️ Development & Contributions

Dynamic ShopGUI+ is an open-source project.

Contributions, bug reports, suggestions and improvements are welcome.

If you encounter a problem:

1. Check whether the issue is reproducible.
2. Include your Minecraft server version.
3. Include your Java version.
4. Mention the server platform (Paper, Purpur, Spigot, Folia, etc.).
5. Provide relevant console errors and logs.

Please avoid reporting issues without sufficient information to reproduce them.

---

## 📜 License

Dynamic ShopGUI+ is an open-source project.

See the repository's [MIT LICENSE](https://github.com/itzlynx-197/Dynamic-ShopGUI-/blob/main/LICENSE) file for licensing terms.

---

**Dynamic ShopGUI+ — Premium-level shop functionality, without the premium price tag.**
