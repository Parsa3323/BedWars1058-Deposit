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

package com.parsa3323.depositplugin.utils;

import com.andrei1058.bedwars.api.BedWars;
import com.andrei1058.bedwars.api.arena.IArena;
import com.andrei1058.bedwars.api.server.ISetupSession;
import com.cryptomorin.xseries.XSound;
import com.parsa3323.depositapi.Events.PlayerDepositEvent;
import com.parsa3323.depositapi.Types.DepositType;
import com.parsa3323.depositplugin.Configs.ArenaConfig;
import com.parsa3323.depositplugin.DepositPlugin;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class DepositUtils {
    private static final Map<String, String> CHEST_HOLOGRAM_TEXTS = Map.of(
            "ENDER_CHEST", ChatColor.DARK_PURPLE + "Ender Chest" + ChatColor.BOLD + " Deposit Set",
            "CHEST", ChatColor.AQUA + "Team Chest" + ChatColor.BOLD + " Deposit Set"
    );

    private static final Set<Material> BLACKLISTED_ITEMS = EnumSet.of(
            Material.WOOD_SWORD, Material.IRON_SWORD, Material.DIAMOND_SWORD, Material.STONE_SWORD,
            Material.COMPASS, Material.WOOD_PICKAXE, Material.STONE_PICKAXE, Material.IRON_PICKAXE,
            Material.DIAMOND_PICKAXE, Material.GOLD_PICKAXE, Material.WOOD_AXE, Material.STONE_AXE,
            Material.IRON_AXE, Material.DIAMOND_AXE, Material.GOLD_AXE, Material.SHEARS
    );

    public static boolean isBlacklistedItem(ItemStack item) {
        return item == null || item.getType() == Material.AIR || BLACKLISTED_ITEMS.contains(item.getType());
    }

    public static List<String> getArenaChests(World arena) {
        DepositPlugin.debug("Ran a method for api : getArenaChests");
        String path = "worlds." + arena.getName() + ".chestLocations";
        return ArenaConfig.get().getStringList(path);
    }



    public static String serializeLocation(Location location) {
        DepositPlugin.debug("Serializing location: " + location);
        return location.getBlockX() + "," + location.getBlockY() + "," + location.getBlockZ();
    }

    public static Location deserializeLocation(String locString, World world) {
        DepositPlugin.debug("Deserializing location string: " + locString);
        String[] parts = locString.split(",");
        int x = Integer.parseInt(parts[0]);
        int y = Integer.parseInt(parts[1]);
        int z = Integer.parseInt(parts[2]);
        Location location = new Location(world, x, y, z);
        DepositPlugin.debug("Deserialized location: " + location);
        return location;
    }

    public static void sendDepositMessage(Player p, int amount, Material material, String chestType, ChatColor color) {
        String itemName = TextUtils.formatItemName(material);
        p.sendMessage(ChatColor.GRAY + "You deposited x" + amount + " " + color + itemName +
                ChatColor.GRAY + " to the" + chestType);
        p.playSound(p.getLocation(), XSound.BLOCK_CHEST_CLOSE.parseSound(), 1.0f, 1.0f);
    }

    public static void handleDepositWholeStack(Player p, Material itemMat, Inventory targetInventory, String chestType, ChatColor color) {
        int totalCount = 0;
        List<ItemStack> itemsToRemove = new ArrayList<>();

        for (ItemStack itemStack : p.getInventory().getContents()) {
            if (itemStack == null || itemStack.getType() != itemMat) continue;

            totalCount += itemStack.getAmount();
            itemsToRemove.add(itemStack);
            targetInventory.addItem(itemStack.clone());
        }

        if (totalCount > 0) {
            itemsToRemove.forEach(item -> p.getInventory().removeItem(item));
            DepositUtils.sendDepositMessage(p, totalCount, itemMat, chestType, color);
            DepositPlugin.info(p.getName() + " deposited " + totalCount + "x " + itemMat + " to the " + chestType);
        } else {
            p.sendMessage(ChatColor.RED + "You don't have any " + TextUtils.formatItemName(itemMat) + " to deposit!");
        }
    }

    public static void handleDepositSingleStack(Player p, ItemStack item, Inventory targetInventory, String chestType, ChatColor color) {
        if (item == null || item.getType() == Material.AIR) return;

        int amount = item.getAmount();
        targetInventory.addItem(item.clone());
        p.setItemInHand(null);

        DepositUtils.sendDepositMessage(p, amount, item.getType(), chestType, color);
        DepositPlugin.info(p.getName() + " deposited " + amount + "x " + item.getType() + " to the " + chestType);
    }

    public static void handleSetupSession(Player p, Block block) {
        String chestLocation = block.getLocation().getBlockX() + "," +
                block.getLocation().getBlockY() + "," +
                block.getLocation().getBlockZ();
        String path = "worlds." + p.getWorld().getName() + ".chestLocations";
        List<String> chestLocations = ArenaConfig.get().getStringList(path);

        if (!chestLocations.contains(chestLocation)) {
            chestLocations.add(chestLocation);
            ArenaConfig.get().set(path, chestLocations);
            ArenaConfig.save();

            p.sendMessage(ChatColor.GREEN + "Chest location added: " + chestLocation);
            updateSetupSessionMessage(p, chestLocations.size());
            p.playSound(p.getLocation(), XSound.BLOCK_NOTE_BLOCK_HAT.parseSound(), 1, 1);

            String hologramText = CHEST_HOLOGRAM_TEXTS.get(block.getType().name());
            if (hologramText != null) {
                HologramUtils.createHologram(block.getLocation(), hologramText);
            }
        } else {
            p.sendMessage(ChatColor.YELLOW + "This chest is already set!");
        }
        SelectionUtils.removePlayerFromSelectionMode(p);
    }

    public static void updateSetupSessionMessage(Player p, int chestCount) {
        ISetupSession setupSession = DepositPlugin.bedWars.getSetupSession(p.getUniqueId());
        if (setupSession == null) return;

        int maxInTeam = setupSession.getConfig().getInt("maxInTeam");
        int expectedCount = (maxInTeam == 2 || maxInTeam == 1) ? 16 : 8;

        String status;
        if (chestCount == 0) status = "&c&l(NOT SET)";
        else if (chestCount < expectedCount) status = "&e&l(NOT PROPERLY SET)";
        else if (chestCount == expectedCount) status = "&a&l(SET)";
        else status = "&c&l(NOT SET)";

        String message = "§6 ▪ §7ChestLocations: " + status + " §8 - §eShift + Left-Click ";
        p.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
    }

    public static void deposit(Player p, Block block, Material blockType) {
        BedWars bedwarsAPI = Bukkit.getServicesManager().getRegistration(BedWars.class).getProvider();
        if (!bedwarsAPI.getArenaUtil().isPlaying(p)) return;

        DepositPlugin.debug(p.getName() + " left-clicked on a " + blockType.name() + "!");

        ItemStack itemInHand = p.getItemInHand();
        if (DepositUtils.isBlacklistedItem(itemInHand)) return;

        DepositType depositType = blockType == Material.ENDER_CHEST ? DepositType.ENDER_CHEST : DepositType.CHEST;
        Inventory targetInventory = blockType == Material.ENDER_CHEST ? p.getEnderChest() : ((Chest) block.getState()).getInventory();

        if (targetInventory.firstEmpty() == -1) return;

        PlayerDepositEvent depositEvent = new PlayerDepositEvent(p, depositType, block);
        Bukkit.getPluginManager().callEvent(depositEvent);

        if (depositEvent.isCancelled()) {
            DepositPlugin.warn("Player deposit event has been canceled");
            return;
        }

        Material itemMat = itemInHand.getType();
        ChatColor color = (itemMat == Material.GOLDEN_APPLE || itemMat == Material.GOLD_INGOT) ? ChatColor.GOLD : ChatColor.WHITE;
        String chestTypeName = blockType == Material.ENDER_CHEST ?
                ChatColor.LIGHT_PURPLE + " Ender Chest" : ChatColor.AQUA + " Team Chest";

        new BukkitRunnable() {
            @Override
            public void run() {
                if (DepositPlugin.plugin.configuration.getBoolean("deposit-whole-itemstack")) {
                    DepositUtils.handleDepositWholeStack(p, itemMat, targetInventory, chestTypeName, color);
                } else {
                    DepositUtils.handleDepositSingleStack(p, itemInHand, targetInventory, chestTypeName, color);
                }
            }
        }.runTaskAsynchronously(DepositPlugin.plugin);
    }

    public static void setChestLocationsAll() {
        DepositPlugin.debug("Creating HD locations for all arenas");
        for (IArena arena : DepositPlugin.bedWars.getArenaUtil().getArenas()) {
            World world = arena.getWorld();
            if (world == null) {
                Bukkit.getLogger().warning("World is null for arena " + arena.getWorldName());
                continue;
            }
            setChestLocations(world);
        }
        DepositPlugin.info("Finished processing all arenas");
    }

    public static void setChestLocations(World world) {
        String path = "worlds." + world.getName() + ".chestLocations";
        if (ArenaConfig.get().contains(path)) {
            DepositPlugin.debug("Chest locations already exist for world: " + world.getName());
            return;
        }

        DepositPlugin.debug("Scanning world for chests: " + world.getName());
        List<String> chestLocations = new ArrayList<>();

        for (Chunk chunk : world.getLoadedChunks()) {
            for (int x = 0; x < 16; x++) {
                for (int z = 0; z < 16; z++) {
                    for (int y = 0; y < world.getMaxHeight(); y++) {
                        Block block = chunk.getBlock(x, y, z);
                        if (EnumSet.of(Material.ENDER_CHEST, Material.CHEST).contains(block.getType())) {
                            chestLocations.add(DepositUtils.serializeLocation(block.getLocation()));
                        }
                    }
                }
            }
        }

        ArenaConfig.get().set(path, chestLocations);
        ArenaConfig.save();
        DepositPlugin.debug("Saved " + chestLocations.size() + " chest locations for world: " + world.getName());
    }

}
