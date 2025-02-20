package me.parsa.depositplugin;

import com.andrei1058.bedwars.api.BedWars;
import me.parsa.depositapi.DepositApi;
import me.parsa.depositplugin.Configs.ArenasConfig;
import me.parsa.depositplugin.Listeners.EnderChestClick;
import me.parsa.depositplugin.Listeners.GameStartListener;
import me.parsa.depositplugin.Listeners.PlayerDeathListener;
import me.parsa.depositplugin.Listeners.PlayerJoin;
import org.bukkit.Bukkit;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Level;
import java.util.logging.Logger;

public final class DepositPlugin extends JavaPlugin {

    private static Logger logger;
    public static Level logLevel;
    public static DepositApi api;

    public static DepositPlugin plugin;
    //Main

    public static BedWars bedWars;

    @Override
    public void onEnable() {
        logger = getLogger();
        api= new API();
        plugin = this;

        bedWars = Bukkit.getServicesManager().getRegistration(BedWars.class).getProvider();
        getServer().getServicesManager().register(DepositApi.class, api, this, ServicePriority.Normal);

        Bukkit.getConsoleSender().sendMessage("[Deposit] Enabling plugin");
        Bukkit.getConsoleSender().sendMessage("[Deposit] Loading version v" + getDescription().getVersion());
        Bukkit.getConsoleSender().sendMessage("[Deposit] Loading configs");


        ArenasConfig.setup();
        ArenasConfig.get().options().copyDefaults(true);
        ArenasConfig.save();
        saveDefaultConfig();
        Bukkit.getConsoleSender().sendMessage("[Deposit] Registering events");
        Bukkit.getConsoleSender().sendMessage("[Deposit] Hooking into bw1085");
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
        Bukkit.getConsoleSender().sendMessage("[Deposit] Enabled plugin");
        String levelName = getConfig().getString("log-level", "INFO").toUpperCase();
        logLevel = Level.parse(levelName);
        logger.setLevel(logLevel);

        debug("Log level set to: " + logLevel);

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
}