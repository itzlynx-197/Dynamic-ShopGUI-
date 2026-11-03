

package com.siliconstudios.dynamicshopguiplus.util;

import com.siliconstudios.dynamicshopguiplus.util.EcoType;

public enum EconomyType {
    EXP("EXP"),
    ITEM("Item"),
    VAULT("Vault"),
    VAULTUNLOCKED("VaultUnlocked"),
    LEVELS("Levels"),
    ECOBITS("EcoBits"),
    EXCELLENT_ECONOMY("ExcellentEconomy"),
    PLAYER_POINTS("PlayerPoints"),
    GEMS_ECONOMY("GemsEconomy"),
    ULTRA_ECONOMY("UltraEconomy"),
    VOTING_PLUGIN("VotingPlugin"),
    ZESSENTIALS("zEssentials");

    private final String name;

    private EconomyType(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    public static EcoType getFromString(String type) {
        EcoType economyType;
        if (type == null) {
            return null;
        }
        switch (type.split(":")[0]) {
            case "PLAYER_POINTS": {
                economyType = new EcoType(PLAYER_POINTS);
                break;
            }
            case "VAULT": {
                economyType = new EcoType(VAULT);
                break;
            }
            case "VAULTUNLOCKED": {
                if (type.contains(":")) {
                    economyType = new EcoType(VAULTUNLOCKED, type.split(":")[1]);
                    break;
                }
                economyType = new EcoType(VAULTUNLOCKED);
                break;
            }
            case "EXCELLENT_ECONOMY": {
                if (type.contains(":")) {
                    economyType = new EcoType(EXCELLENT_ECONOMY, type.split(":")[1]);
                    break;
                }
                economyType = new EcoType(EXCELLENT_ECONOMY);
                break;
            }
            case "GEMS_ECONOMY": {
                if (type.contains(":")) {
                    economyType = new EcoType(GEMS_ECONOMY, type.split(":")[1]);
                    break;
                }
                economyType = new EcoType(GEMS_ECONOMY);
                break;
            }
            case "ULTRA_ECONOMY": {
                if (type.contains(":")) {
                    economyType = new EcoType(ULTRA_ECONOMY, type.split(":")[1]);
                    break;
                }
                economyType = new EcoType(ULTRA_ECONOMY);
                break;
            }
            case "VOTING_PLUGIN": {
                economyType = new EcoType(VOTING_PLUGIN);
                break;
            }
            case "EXP": {
                economyType = new EcoType(EXP);
                break;
            }
            case "ZESSENTIALS": {
                if (type.contains(":")) {
                    economyType = new EcoType(ZESSENTIALS, type.split(":")[1]);
                    break;
                }
                economyType = new EcoType(ZESSENTIALS);
                break;
            }
            default: {
                economyType = null;
            }
        }
        return economyType;
    }
}

