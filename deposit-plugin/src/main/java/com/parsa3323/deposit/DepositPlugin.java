package com.parsa3323.deposit;

import com.andrei1058.bedwars.api.BedWars;
import com.parsa3323.deposit.api.DepositApi;
import com.parsa3323.deposit.Configs.ArenasConfig;
import com.parsa3323.deposit.Configs.MessagesConfig;
import com.parsa3323.deposit.Listeners.EnderChestClick;
import com.parsa3323.deposit.Listeners.GameStartListener;
import com.parsa3323.deposit.Listeners.PlayerDeathListener;
import com.parsa3323.deposit.Listeners.PlayerJoin;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
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
        api= new API();
        plugin = this;

        bedWars = Bukkit.getServicesManager().getRegistration(BedWars.class).getProvider();
        getServer().getServicesManager().register(DepositApi.class, api, this, ServicePriority.Normal);

        status("Enabling plugin");
        status("Loading version v" + getDescription().getVersion());
        status("BedWars1058 - Deposit by Parsa3323");
        status("Loading configs");
        String packageName = getServer().getClass().getPackage().getName();
        String version = packageName.split("\\.")[3];


        ArenasConfig.init();
        ArenasConfig.get().options().copyDefaults(true);
        ArenasConfig.save();
        File configFile = new File(bedWars.getAddonsPath(), "Deposit/config.yml");

        MessagesConfig.init();
        MessagesConfig.get().options().copyDefaults(true);
        MessagesConfig.get().addDefault("player_deposit_chest", "&7You deposited x%amount% %color%%material% to the &bTeamChest");
        MessagesConfig.get().addDefault("player_deposit_ender_chest", "&7You deposited x%amount% %color%%material% to the &dEnderChest");
        MessagesConfig.save();

        if (!configFile.exists()) {
            createConfigWithComments(configFile);
        }

        configuration = YamlConfiguration.loadConfiguration(configFile);

        status("Registering events");
        status("Hooking into bw1085");

        if (Bukkit.getPluginManager().getPlugin("BedWars1058") == null) {
            getLogger().severe("BedWars1058 was not found. Disabling...");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        BedWars bedwarsAPI = Bukkit.getServicesManager().getRegistration(BedWars.class).getProvider();
        getServer().getPluginManager().registerEvents(new EnderChestClick(), this);
        getServer().getPluginManager().registerEvents(new GameStartListener(this, ArenasConfig.get()), this);
        getServer().getPluginManager().registerEvents(new PlayerDeathListener(), this);
        getServer().getPluginManager().registerEvents(new PlayerJoin(), this);
        status("Enabled plugin");
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