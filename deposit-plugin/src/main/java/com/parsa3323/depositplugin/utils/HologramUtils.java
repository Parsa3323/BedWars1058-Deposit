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

import com.parsa3323.depositplugin.Configs.ArenaConfig;
import com.parsa3323.depositplugin.DepositPlugin;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.List;

public class HologramUtils {
    private static final String[] HOLOGRAM_LINES = {
            ChatColor.GRAY + "DEPOSIT.",
            ChatColor.GRAY + "PUNCH TO"
    };

    public static void createHologram(Location chestLocation, String... lines) {
        Location baseLocation = chestLocation.clone().add(0.5, 0.9, 0.5);

        for (int i = 0; i < lines.length; i++) {
            Location hologramLocation = baseLocation.clone().add(0, 0.3 * i, 0);
            ArmorStand hologram = chestLocation.getWorld().spawn(hologramLocation, ArmorStand.class);

            hologram.setVisible(false);
            hologram.setMarker(true);
            hologram.setCustomName(lines[i]);
            hologram.setCustomNameVisible(true);
            hologram.setGravity(false);
        }
    }

    public static void deleteHolograms(World world) {
        if (world == null) {
            Bukkit.getLogger().warning("World is null");
            return;
        }

        FileConfiguration config = ArenaConfig.get();
        String worldName = world.getName();
        String path = "worlds." + worldName + ".chestLocations";

        if (!config.contains(path)) {
            DepositPlugin.debug("No chest locations found in config for world: " + worldName);
            return;
        }

        List<String> chestLocations = config.getStringList(path);

        for (String locString : chestLocations) {
            Location chestLocation = DepositUtils.deserializeLocation(locString, world);
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

    public static void createHolograms(Player player) {
        if (player == null) return;
        World world = player.getWorld();
        if (world == null) {
            Bukkit.getLogger().warning("World is null for player " + player.getName());
            return;
        }
        DepositPlugin.debug("Manually spawning holograms for world: " + world.getName());
        DepositUtils.setChestLocations(world);
        HologramUtils.spawnDepositHolograms(world);
    }

    public static void deleteHologram(Location chestLocation) {
        Location baseLocation = chestLocation.clone().add(0.5, 0.9, 0.5);

        chestLocation.getWorld().getEntitiesByClass(ArmorStand.class).stream()
                .filter(hologram -> hologram.getLocation().distance(baseLocation) < 1)
                .forEach(Entity::remove);
    }

    public static void spawnDepositHolograms(World world) {
        String path = "worlds." + world.getName() + ".chestLocations";
        List<String> chestLocations = ArenaConfig.get().getStringList(path);
        if (chestLocations.isEmpty()) {
            DepositPlugin.debug("No chest locations found for world: " + world.getName());
            return;
        }

        DepositPlugin.debug("Spawning holograms for " + chestLocations.size() + " chests in world: " + world.getName());
        for (String locString : chestLocations) {
            Location chestLoc = DepositUtils.deserializeLocation(locString, world);
            if (chestLoc == null) continue;

            Location baseLoc = chestLoc.clone().add(0.5, 0.9, 0.5);
            for (int i = 0; i < HOLOGRAM_LINES.length; i++) {
                Location hologramLoc = baseLoc.clone().add(0, 0.3 * i, 0);
                ArmorStand hologram = world.spawn(hologramLoc, ArmorStand.class);
                hologram.setVisible(false);
                hologram.setMarker(true);
                hologram.setCustomName(HOLOGRAM_LINES[i]);
                hologram.setCustomNameVisible(true);
                hologram.setGravity(false);
            }
        }
    }
}
