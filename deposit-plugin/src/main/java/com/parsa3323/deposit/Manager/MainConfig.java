package com.parsa3323.deposit.Manager;

import com.parsa3323.deposit.Configs.ArenasConfig;
import com.parsa3323.deposit.DepositPlugin;
import org.bukkit.World;

import java.util.List;

public class MainConfig {
    public static List<String> getArenaChests(World arena) {
        DepositPlugin.debug("Ran a method for api : getArenaChests");
        String path = "worlds." + arena.getName() + ".chestLocations";
        return ArenasConfig.get().getStringList(path);
    }
}