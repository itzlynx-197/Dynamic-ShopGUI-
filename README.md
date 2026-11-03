
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

- Folia-compatible implementation.
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
| Minecraft | 1.21+ |
| Java | 21+ |

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
| `dynamicshopgui.admin` | `op` | Access administrative functionality |
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
