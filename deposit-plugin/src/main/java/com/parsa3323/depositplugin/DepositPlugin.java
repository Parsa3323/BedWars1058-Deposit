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
import com.parsa3323.depositplugin.Listeners.ChestClickListener;
import com.parsa3323.depositplugin.Listeners.DepositListener;
import com.parsa3323.depositplugin.Listeners.GameStartListener;
import com.parsa3323.depositplugin.Listeners.PlayerJoinListener;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.Listener;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class DepositPlugin extends JavaPlugin {

    private static Logger logger;
    public static Level logLevel;
    public YamlConfiguration configuration;

    public static DepositApi api;
    public static DepositPlugin plugin;
    public static BedWars bedWars;

    @Override
    public void onEnable() {
        logger = getLogger();
        getLogger().info("Loading " + getDescription().getName() + " v" + getDescription().getVersion());

        status("BedWars1058 - Deposit by Parsa3323");


        status("Hooking into BedWars1058...");

        if (Bukkit.getPluginManager().getPlugin("BedWars1058") == null) {
            getLogger().severe("BedWars1058 was not found. Disabling...");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        bedWars = Bukkit.getServicesManager().getRegistration(BedWars.class).getProvider();


        status("Loading configs...");

        ArenaConfig.setup();
        ArenaConfig.get().options().copyDefaults(true);
        ArenaConfig.save();
        File configFile = new File(bedWars.getAddonsPath(), "Deposit/config.yml");

        if (!configFile.exists()) {
            createConfigWithComments(configFile);
        }

        configuration = YamlConfiguration.loadConfiguration(configFile);

        api = new API();
        getServer().getServicesManager().register(DepositApi.class, api, this, ServicePriority.Normal);

        plugin = this;

        status("Registering events...");
        PluginManager pm = getServer().getPluginManager();
        Listener[] listeners = new Listener[] {
                new ChestClickListener(),
                new DepositListener(),
                new GameStartListener(),
                new PlayerJoinListener()
        };

        for (Listener listener : listeners) {
            pm.registerEvents(listener, this);
            getLogger().info("Loaded listener: " + listener.getClass().getSimpleName());
        }

        status("Successfully loaded plugin!");
        String levelName = getConfig().getString("log-level", "INFO").toUpperCase();
        logLevel = Level.parse(levelName);
        logger.setLevel(logLevel);

        debug("Log level set to: " + logLevel);

    }
    private void createConfigWithComments(File configFile) {
        try {

            configFile.getParentFile().mkdirs();
            if (configFile.createNewFile()) {
                StringBuilder configContent = new StringBuilder();


                configContent.append("# ──────────────────────────────────────────────────────────────\n");
                configContent.append("#   Punch to deposit - By Parsa3323\n");
                configContent.append("# ──────────────────────────────────────────────────────────────\n");
                configContent.append("#   This configuration file controls various aspects of the addon.\n");
                configContent.append("#   Make sure to read the comments carefully before changing any settings.\n");
                configContent.append("# ──────────────────────────────────────────────────────────────\n\n");

                configContent.append("# LOG LEVEL:\n");
                configContent.append("# Determines the level of logging that will be shown in the console.\n");
                configContent.append("# Available options:\n");
                configContent.append("# - SEVERE   → Shows only critical errors.\n");
                configContent.append("# - WARNING  → Displays warnings and serious issues.\n");
                configContent.append("# - INFO     → Standard logging (recommended for most cases).\n");
                configContent.append("# - CONFIG   → Shows additional configuration details.\n");
                configContent.append("# - FINE     → Provides debugging information (useful for developers).\n");
                configContent.append("# - FINER    → Even more detailed debugging logs.\n");
                configContent.append("# - FINEST   → Maximum debugging details (may spam the console).\n");
                configContent.append("# Default is INFO. Change only if needed for debugging purposes.\n");
                configContent.append("log-level: INFO\n\n");

                configContent.append("# ──────────────────────────────────────────────────────────────\n\n");

                configContent.append("# DISABLE HOLOGRAM AFTER DEATH:\n");
                configContent.append("# If enabled (true), the deposit hologram will be removed after the player dies.\n");
                configContent.append("disable-hologram-after-death: false\n\n");

                configContent.append("# ──────────────────────────────────────────────────────────────\n\n");

                configContent.append("# HOLOGRAM REGISTER EVENT:\n");
                configContent.append("# Determines when the deposit hologram should be registered in the game.\n");
                configContent.append("hologram-register-event: GameStateChangeEvent\n\n");

                configContent.append("# ──────────────────────────────────────────────────────────────\n\n");

                configContent.append("# DEPOSIT WHOLE ITEMSTACK:\n");
                configContent.append("# If enabled (true), depositing an item will move all matching item stacks\n");
                configContent.append("# (same type as the item in hand) from the player's inventory to the Ender Chest.\n");
                configContent.append("deposit-whole-itemstack: false\n\n");

                configContent.append("# ──────────────────────────────────────────────────────────────\n\n");

                configContent.append("# SHIFT-CLICK ON CHEST TO SET:\n");
                configContent.append("# If enabled (true), while in BedWars1058 setup mode,\n");
                configContent.append("# players can shift-click on an Ender Chest or Chest to register it\n");
                configContent.append("# as a valid deposit chest for holograms.\n");
                configContent.append("shift-click-on-chest-to-set: true\n\n");

                configContent.append("# ──────────────────────────────────────────────────────────────\n\n");

                configContent.append("# SET CHEST LOCATIONS ON PLAYER JOIN:\n");
                configContent.append("# If enabled (true), all chest locations will be saved when the a player joins the server.\n");
                configContent.append("set-chest-locations-on-join: true\n");

                java.nio.file.Files.write(configFile.toPath(), configContent.toString().getBytes());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    @Override
    public void onDisable() {

    }
    public static void debug(String message) {
        if (logLevel.intValue() <= Level.FINE.intValue()) {
            logger.info("[DEBUG] " + message);
        }
    }

    public static void info(String message) {
        if (logLevel.intValue() <= Level.INFO.intValue()) {
            logger.info("[INFO] " + message);
        }
    }

    public static void warn(String message) {
        if (logLevel.intValue() <= Level.WARNING.intValue()) {
            logger.warning("[WARNING] " + message);
        }
    }

    public static void error(String message) {
        if (logLevel.intValue() <= Level.SEVERE.intValue()) {
            logger.severe("[ERROR] " + message);
        }
    }

    public static void status(String message) {
        logger.info(message);
    }

}