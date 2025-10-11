package com.parsa3323.deposit.Listeners;

import com.andrei1058.bedwars.api.arena.GameState;
import com.andrei1058.bedwars.api.arena.IArena;
import com.andrei1058.bedwars.api.events.gameplay.GameStateChangeEvent;
import com.andrei1058.bedwars.api.events.gameplay.TeamAssignEvent;
import com.parsa3323.deposit.Configs.ArenasConfig;
import com.parsa3323.deposit.DepositPlugin;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;

public class GameStartListener implements Listener {

    private final Plugin plugin;
    private final FileConfiguration config;
    public boolean successGameAssgin = false;
    public boolean succesGameState = false;
    public boolean isReloaded = false;

    public GameStartListener(Plugin plugin, FileConfiguration config) {
        this.plugin = plugin;
        this.config = config;
    }

    @EventHandler
    public void onGameStart(GameStateChangeEvent event) {
        if (event.getNewState() == GameState.playing) {
            if (DepositPlugin.plugin.configuration.getString("hologram-register-event").equalsIgnoreCase("GameStateChangeEvent")) {
                DepositPlugin.debug("TeamAssignEvent triggered");
                succesGameState = true;

                DepositPlugin.debug("Player: ");
                World world = event.getArena().getWorld();
                DepositPlugin.debug("World: " + (world != null ? world.getName() : "null"));

                if (world == null) {
                    Bukkit.getLogger().warning("World is null for player ");
                    return;
                }

                String worldName = world.getName();
                DepositPlugin.debug("World Name: " + worldName);
                String path = "worlds." + worldName + ".chestLocations";
                DepositPlugin.debug("Config path: " + path);

                if (!config.contains(path)) {
                    DepositPlugin.debug("First time loading the map, searching for chests");
                    List<String> chestLocations = new ArrayList<>();

                    for (Chunk chunk : world.getLoadedChunks()) {
                        DepositPlugin.debug("Checking chunk: " + chunk.getX() + ", " + chunk.getZ());
                        for (int x = 0; x < 16; x++) {
                            for (int y = 0; y < world.getMaxHeight(); y++) {
                                for (int z = 0; z < 16; z++) {
                                    Block block = chunk.getBlock(x, y, z);
                                    if (block.getType() == Material.ENDER_CHEST || block.getType() == Material.CHEST) {
                                        DepositPlugin.debug("Chest found at: " + block.getLocation());
                                        chestLocations.add(serializeLocation(block.getLocation()));
                                    }
                                }
                            }
                        }
                    }

                    config.set(path, chestLocations);
                    ArenasConfig.save();
                    DepositPlugin.debug("Chest locations saved to config");
                }

                List<String> chestLocations = config.getStringList(path);
                DepositPlugin.debug("Loaded " + chestLocations.size() + " chest locations from config");

                String[] lines = {
                        ChatColor.GRAY + "DEPOSIT.",
                        ChatColor.GRAY + "PUNCH TO"
                };

                for (String locString : chestLocations) {
                    DepositPlugin.debug("Processing chest location: " + locString);
                    Location chestLocation = deserializeLocation(locString, world);
                    Location baseLocation = chestLocation.add(0.5, 0.9, 0.5);

                    for (int i = 0; i < lines.length; i++) {
                        Location hologramLocation = baseLocation.clone().add(0, 0.3 * i, 0);
                        DepositPlugin.debug("Spawning hologram at: " + hologramLocation);

                        ArmorStand hologram = (ArmorStand) world.spawnEntity(hologramLocation, EntityType.ARMOR_STAND);
                        hologram.setVisible(false);
                        hologram.setMarker(true);
                        hologram.setCustomName(lines[i]);
                        hologram.setCustomNameVisible(true);
                        hologram.setGravity(false);
                        DepositPlugin.debug("Hologram created with text: " + lines[i]);
                    }
                }
            } else {
                new BukkitRunnable(){
                    @Override
                    public void run() {
                        if (!successGameAssgin && DepositPlugin.plugin.getConfig().getString("hologram-register-event").equalsIgnoreCase("TeamAssignEvent")) {
                            event.getArena().getPlayers().forEach(player -> {
                                if (player.isOp()) {
                                    player.sendMessage(ChatColor.RED + "It looks like your event for registering holograms didn't work, you can make it work with changing hologram-register-event value to GameStateChangeEvent");
                                }
                            });
                        }
                    }
                }.runTaskLaterAsynchronously(DepositPlugin.plugin, 20L);
            }

        }

    }

    private String serializeLocation(Location location) {
        DepositPlugin.debug("Serializing location: " + location);
        return location.getBlockX() + "," + location.getBlockY() + "," + location.getBlockZ();
    }

    private Location deserializeLocation(String locString, World world) {
        DepositPlugin.debug("Deserializing location string: " + locString);
        String[] parts = locString.split(",");
        int x = Integer.parseInt(parts[0]);
        int y = Integer.parseInt(parts[1]);
        int z = Integer.parseInt(parts[2]);
        Location location = new Location(world, x, y, z);
        DepositPlugin.debug("Deserialized location: " + location);
        return location;
    }

    @EventHandler
    public void onGameAssgin(TeamAssignEvent event) {
        if (DepositPlugin.plugin.configuration.getString("hologram-register-event").equalsIgnoreCase("TeamAssignEvent")) {
            DepositPlugin.debug("TeamAssignEvent triggered");
            successGameAssgin = true;
            Player player = event.getPlayer();
            DepositPlugin.debug("Player: " + player.getName());
            World world = player.getWorld();
            DepositPlugin.debug("World: " + (world != null ? world.getName() : "null"));

            if (world == null) {
                Bukkit.getLogger().warning("World is null for player " + player.getName());
                return;
            }

            String worldName = world.getName();
            DepositPlugin.debug("World Name: " + worldName);
            String path = "worlds." + worldName + ".chestLocations";
            DepositPlugin.debug("Config path: " + path);

            if (!config.contains(path)) {
                DepositPlugin.debug("First time loading the map, searching for chests");
                List<String> chestLocations = new ArrayList<>();

                for (Chunk chunk : world.getLoadedChunks()) {
                    DepositPlugin.debug("Checking chunk: " + chunk.getX() + ", " + chunk.getZ());
                    for (int x = 0; x < 16; x++) {
                        for (int y = 0; y < world.getMaxHeight(); y++) {
                            for (int z = 0; z < 16; z++) {
                                Block block = chunk.getBlock(x, y, z);
                                if (block.getType() == Material.ENDER_CHEST || block.getType() == Material.CHEST) {
                                    DepositPlugin.debug("Chest found at: " + block.getLocation());
                                    chestLocations.add(serializeLocation(block.getLocation()));
                                }
                            }
                        }
                    }
                }

                config.set(path, chestLocations);
                ArenasConfig.save();
                DepositPlugin.debug("Chest locations saved to config");
            }

            List<String> chestLocations = config.getStringList(path);
            DepositPlugin.debug("Loaded " + chestLocations.size() + " chest locations from config");

            String[] lines = {
                    ChatColor.GRAY + "DEPOSIT.",
                    ChatColor.GRAY + "PUNCH TO"
            };

            for (String locString : chestLocations) {
                DepositPlugin.debug("Processing chest location: " + locString);
                Location chestLocation = deserializeLocation(locString, world);
                Location baseLocation = chestLocation.add(0.5, 0.9, 0.5);

                for (int i = 0; i < lines.length; i++) {
                    Location hologramLocation = baseLocation.clone().add(0, 0.3 * i, 0);
                    DepositPlugin.debug("Spawning hologram at: " + hologramLocation);

                    ArmorStand hologram = (ArmorStand) world.spawnEntity(hologramLocation, EntityType.ARMOR_STAND);
                    hologram.setVisible(false);
                    hologram.setMarker(true);
                    hologram.setCustomName(lines[i]);
                    hologram.setCustomNameVisible(true);
                    hologram.setGravity(false);
                    DepositPlugin.debug("Hologram created with text: " + lines[i]);
                }
            }
        } else {
            new BukkitRunnable(){
                @Override
                public void run() {
                    if (!succesGameState && DepositPlugin.plugin.configuration.getString("hologram-register-event").equalsIgnoreCase("GameStateChangeEvent")) {
                        event.getArena().getPlayers().forEach(player -> {
                            if (player.isOp()) {
                                player.sendMessage(ChatColor.RED + "It looks like your event for registering holograms didn't work, you can make it work with changing hologram-register-event value to TeamAssignEvent");
                            }
                        });
                    }
                }
            }.runTaskLaterAsynchronously(DepositPlugin.plugin, 10L);
        }

    }
    public void deleteHolograms(Player player) {
        DepositPlugin.debug("Deleting holograms for player: " + player.getName());
        World world = player.getWorld();

        if (world == null) {
            Bukkit.getLogger().warning("World is null for player " + player.getName());
            return;
        }

        String worldName = world.getName();
        String path = "worlds." + worldName + ".chestLocations";

        if (!config.contains(path)) {
            DepositPlugin.debug("No chest locations found in config for world: " + worldName);
            return;
        }

        List<String> chestLocations = config.getStringList(path);

        for (String locString : chestLocations) {
            Location chestLocation = deserializeLocation(locString, world);
            Location baseLocation = chestLocation.clone().add(0.5, 0.9, 0.5);

            for (Entity entity : world.getNearbyEntities(baseLocation, 1, 1, 1)) {
                if (entity instanceof ArmorStand) {
                    ArmorStand armorStand = (ArmorStand) entity;
                    String name = armorStand.getCustomName();

                    if (name != null && (name.equals(ChatColor.GRAY + "DEPOSIT.") || name.equals(ChatColor.GRAY + "PUNCH TO"))) {
                        armorStand.remove();
                        DepositPlugin.debug("Deleted hologram at: " + armorStand.getLocation());
                    }
                }
            }
        }
    }
    public void createHDLocationsCustomArenaFile(IArena arena) {
        if (arena == null) {
            DepositPlugin.error("Arena is null, cannot process HD locations.");
            return;
        }

        DepositPlugin.info("Processing HD locations for arena: " + arena.getWorldName());

        World world = arena.getWorld();
        if (world == null) {
            Bukkit.getLogger().warning("World is null for arena " + arena.getArenaName());
            return;
        }

        DepositPlugin.debug("World found: " + world.getName());
        String worldName = world.getName();
        String path = "worlds." + worldName + ".chestLocations";
        DepositPlugin.debug("Config path: " + path);

        if (!config.contains(path)) {
            DepositPlugin.debug("First time loading the map, searching for chests in arena " + arena.getArenaName());
            List<String> chestLocations = new ArrayList<>();

            for (Chunk chunk : world.getLoadedChunks()) {
                DepositPlugin.debug("Checking chunk: " + chunk.getX() + ", " + chunk.getZ());
                for (int x = 0; x < 16; x++) {
                    for (int y = 0; y < world.getMaxHeight(); y++) {
                        for (int z = 0; z < 16; z++) {
                            Block block = chunk.getBlock(x, y, z);
                            if (block.getType() == Material.ENDER_CHEST || block.getType() == Material.CHEST) {
                                DepositPlugin.debug("Chest found at: " + block.getLocation());
                                chestLocations.add(serializeLocation(block.getLocation()));
                            }
                        }
                    }
                }
            }

            DepositPlugin.debug("Total chests found: " + chestLocations.size());
            config.set(path, chestLocations);
            ArenasConfig.save();
            DepositPlugin.debug("Chest locations saved for arena " + arena.getArenaName());
        } else {
            DepositPlugin.debug("Chest locations already exist for arena " + arena.getArenaName());
        }

        DepositPlugin.info("Finished processing arena: " + arena.getArenaName());
    }
    public void createHDLocationsCustomArena(World arena) {
        if (arena == null) {
            DepositPlugin.error("Arena is null, cannot process HD locations.");
            return;
        }

        DepositPlugin.info("Processing HD locations for arena: " + arena.getName());

        World world = arena;
        if (world == null) {
            Bukkit.getLogger().warning("World is null for arena " + arena.getName());
            return;
        }

        DepositPlugin.debug("World found: " + world.getName());
        String worldName = world.getName();
        String path = "worlds." + worldName + ".chestLocations";
        DepositPlugin.debug("Config path: " + path);

        if (!config.contains(path)) {
            DepositPlugin.debug("First time loading the map, searching for chests in arena " + arena.getName());
            List<String> chestLocations = new ArrayList<>();

            for (Chunk chunk : world.getLoadedChunks()) {
                DepositPlugin.debug("Checking chunk: " + chunk.getX() + ", " + chunk.getZ());
                for (int x = 0; x < 16; x++) {
                    for (int y = 0; y < world.getMaxHeight(); y++) {
                        for (int z = 0; z < 16; z++) {
                            Block block = chunk.getBlock(x, y, z);
                            if (block.getType() == Material.ENDER_CHEST || block.getType() == Material.CHEST) {
                                DepositPlugin.debug("Chest found at: " + block.getLocation());
                                chestLocations.add(serializeLocation(block.getLocation()));
                            }
                        }
                    }
                }
            }

            DepositPlugin.debug("Total chests found: " + chestLocations.size());
            config.set(path, chestLocations);
            ArenasConfig.save();
            DepositPlugin.debug("Chest locations saved for arena " + arena.getName());
        } else {
            DepositPlugin.debug("Chest locations already exist for arena " + arena.getName());
        }

        DepositPlugin.info("Finished processing arena: " + arena.getName());
    }

    public void createHDLocations() {
        DepositPlugin.debug("Creating HD locations for all arenas");

        for (IArena arena : DepositPlugin.bedWars.getArenaUtil().getArenas()) {
            DepositPlugin.info("Processing arena: " + arena.getWorldName());
            World world = arena.getWorld();
            if (world == null) {
                Bukkit.getLogger().warning("World is null for arena " + arena.getWorldName());
                continue;
            }

            DepositPlugin.debug("World found: " + world.getName());
            String worldName = world.getName();
            String path = "worlds." + worldName + ".chestLocations";
            DepositPlugin.debug("Config path: " + path);

            if (!config.contains(path)) {
                DepositPlugin.debug("First time loading the map, searching for chests in arena " + arena.getWorldName());
                List<String> chestLocations = new ArrayList<>();

                for (Chunk chunk : world.getLoadedChunks()) {
                    DepositPlugin.debug("Checking chunk: " + chunk.getX() + ", " + chunk.getZ());
                    for (int x = 0; x < 16; x++) {
                        for (int y = 0; y < world.getMaxHeight(); y++) {
                            for (int z = 0; z < 16; z++) {
                                Block block = chunk.getBlock(x, y, z);
                                if (block.getType() == Material.ENDER_CHEST || block.getType() == Material.CHEST) {
                                    DepositPlugin.debug("Chest found at: " + block.getLocation());
                                    chestLocations.add(serializeLocation(block.getLocation()));
                                }
                            }
                        }
                    }
                }

                DepositPlugin.debug("Total chests found: " + chestLocations.size());
                config.set(path, chestLocations);
                ArenasConfig.save();
                DepositPlugin.debug("Chest locations saved for arena " + arena.getWorldName());
            } else {
                DepositPlugin.debug("Chest locations already exist for arena " + arena.getWorldName());
            }
        }
        DepositPlugin.info("Finished processing all arenas");
    }

    public void createHolograms(Player player) {
        DepositPlugin.debug("Reload Hologram triggered");
        DepositPlugin.debug("Player: " + player.getName());
        World world = player.getWorld();
        isReloaded = true;
        DepositPlugin.debug("World: " + (world != null ? world.getName() : "null"));

        if (world == null) {
            Bukkit.getLogger().warning("World is null for player " + player.getName());
            return;
        }

        String worldName = world.getName();
        DepositPlugin.debug("World Name: " + worldName);
        String path = "worlds." + worldName + ".chestLocations";
        DepositPlugin.debug("Config path: " + path);

        if (!config.contains(path)) {
            DepositPlugin.debug("First time loading the map, searching for chests");
            List<String> chestLocations = new ArrayList<>();

            for (Chunk chunk : world.getLoadedChunks()) {
                DepositPlugin.debug("Checking chunk: " + chunk.getX() + ", " + chunk.getZ());
                for (int x = 0; x < 16; x++) {
                    for (int y = 0; y < world.getMaxHeight(); y++) {
                        for (int z = 0; z < 16; z++) {
                            Block block = chunk.getBlock(x, y, z);
                            if (block.getType() == Material.ENDER_CHEST || block.getType() == Material.CHEST) {
                                DepositPlugin.debug("Chest found at: " + block.getLocation());
                                chestLocations.add(block.getLocation().getBlockX() + "," + block.getLocation().getBlockY() + "," + block.getLocation().getBlockZ());
                            }
                        }
                    }
                }
            }

            config.set(path, chestLocations);
            ArenasConfig.save();
            DepositPlugin.debug("Chest locations saved to config");
        }

        List<String> chestLocations = config.getStringList(path);
        DepositPlugin.debug("Loaded " + chestLocations.size() + " chest locations from config");

        String[] lines = {
                ChatColor.GRAY + "DEPOSIT.",
                ChatColor.GRAY + "PUNCH TO"
        };

        for (String locString : chestLocations) {
            DepositPlugin.debug("Processing chest location: " + locString);
            Location chestLocation = deserializeLocation(locString, world);
            Location baseLocation = chestLocation.add(0.5, 0.9, 0.5);

            for (int i = 0; i < lines.length; i++) {
                Location hologramLocation = baseLocation.clone().add(0, 0.3 * i, 0);
                DepositPlugin.debug("Spawning hologram at: " + hologramLocation);

                ArmorStand hologram = (ArmorStand) world.spawnEntity(hologramLocation, EntityType.ARMOR_STAND);
                hologram.setVisible(false);
                hologram.setMarker(true);
                hologram.setCustomName(lines[i]);
                hologram.setCustomNameVisible(true);
                hologram.setGravity(false);
                DepositPlugin.debug("Hologram created with text: " + lines[i]);
            }
        }
    }

}