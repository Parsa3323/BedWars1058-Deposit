/*
 * Copyright (C)  2026 BedWars1058-Deposit, Inc.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.parsa3323.depositplugin;

import com.andrei1058.bedwars.api.BedWars;
import com.parsa3323.depositapi.DepositApi;
import com.parsa3323.depositplugin.Configs.ArenaConfig;
import com.parsa3323.depositplugin.Configs.MainConfig;
import com.parsa3323.depositplugin.Configs.MessageConfig;
import com.parsa3323.depositplugin.Listeners.ArenaLifecycleListener;
import com.parsa3323.depositplugin.Listeners.ChestClickListener;
import com.parsa3323.depositplugin.Listeners.DepositListener;
import com.parsa3323.depositplugin.Listeners.GameStartListener;
import com.parsa3323.depositplugin.Listeners.PlayerJoinListener;
import com.parsa3323.depositplugin.Listeners.PlayerCleanupListener;
import com.parsa3323.depositplugin.cache.ChestOwnerCache;
import com.parsa3323.depositplugin.utils.HologramUtils;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.event.Listener;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Level;
import java.util.logging.Logger;

public final class DepositPlugin extends JavaPlugin {

    private static Logger logger;

    public static Level logLevel = Level.INFO;
    public static DepositApi api;
    public static DepositPlugin plugin;
    public static BedWars bedWars;
    public static GameStartListener gameStartListener;

    private boolean fullyEnabled = false;

    @Override
    public void onEnable() {
        logger = getLogger();

        getLogger().info("Loading " + getDescription().getName() + " v" + getDescription().getVersion());
        status("BedWars1058 - Deposit v2.0.0 by Parsa3323 & 1xMohameeD | Security Patches");

        status("Hooking into BedWars1058...");

        if (Bukkit.getPluginManager().getPlugin("BedWars1058") == null) {
            getLogger().severe("BedWars1058 was not found. Disabling...");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        RegisteredServiceProvider<BedWars> rsp = Bukkit.getServicesManager().getRegistration(BedWars.class);

        if (rsp == null) {
            getLogger().severe("BedWars1058 service registration not found. Disabling...");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        bedWars = rsp.getProvider();
        if (bedWars == null) {
            getLogger().severe("BedWars1058 service provider is null. Disabling...");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        status("Loading configs...");

        ArenaConfig.init();
        ArenaConfig.get().options().copyDefaults(true);
        ArenaConfig.save();

        MessageConfig.init();
        MessageConfig.get().addDefault("player_deposit_chest", "&7You deposited x%amount% %color%%material%&7 to the &bTeamChest");
        MessageConfig.get().addDefault("player_deposit_ender_chest", "&7You deposited x%amount% %color%%material%&7 to the &dEnderChest");
        MessageConfig.get().addDefault("hologram_text", "&7PUNCH TO\n&7DEPOSIT.");
        MessageConfig.get().options().copyDefaults(true);
        MessageConfig.save();

        MainConfig.init();

        String levelName = MainConfig.get().getString("log-level", "INFO").toUpperCase();
        try {
            logLevel = Level.parse(levelName);
            logger.setLevel(logLevel);
        } catch (IllegalArgumentException e) {
            logger.warning("Invalid log-level '" + levelName + "' in config. Defaulting to INFO.");
            logLevel = Level.INFO;
            logger.setLevel(logLevel);
        }

        debug("Log level set to: " + logLevel);

        gameStartListener = new GameStartListener();
        api = new API(gameStartListener);
        getServer().getServicesManager().register(DepositApi.class, api, this, ServicePriority.Normal);

        plugin = this;

        status("Registering events...");
        PluginManager pm = getServer().getPluginManager();

        Listener[] listeners = {
                new ChestClickListener(),
                new DepositListener(),
                new ArenaLifecycleListener(),
                gameStartListener,
                new PlayerJoinListener(),
                new PlayerCleanupListener()
        };

        for (Listener listener : listeners) {
            pm.registerEvents(listener, this);
            getLogger().info("Loaded listener: " + listener.getClass().getSimpleName());
        }

        fullyEnabled = true;
        status("Successfully loaded " + getDescription().getName() + " v" + getDescription().getVersion() + "!");
    }

    @Override
    public void onDisable() {
        if (fullyEnabled) {
            for (World world : Bukkit.getWorlds()) {
                HologramUtils.deleteHolograms(world);
            }
        }

        ChestOwnerCache.clear();

        plugin = null;
        api = null;
        bedWars = null;
        gameStartListener = null;
        logger = null;
    }

    public static void debug(String message) {
        if (logger == null) return;
        if (logLevel.intValue() <= Level.FINE.intValue()) {
            logger.info("[DEBUG] " + message);
        }
    }

    public static void info(String message) {
        if (logger == null) return;
        if (logLevel.intValue() <= Level.INFO.intValue()) {
            logger.info("[INFO] " + message);
        }
    }

    public static void warn(String message) {
        if (logger == null) return;
        if (logLevel.intValue() <= Level.WARNING.intValue()) {
            logger.warning("[WARN] " + message);
        }
    }

    public static void error(String message) {
        if (logger == null) return;
        if (logLevel.intValue() <= Level.SEVERE.intValue()) {
            logger.severe("[ERROR] " + message);
        }
    }

    public static void status(String message) {
        if (logger != null) {
            logger.info(message);
        } else {
            Bukkit.getLogger().info("[DepositPlugin] " + message);
        }
    }
}
