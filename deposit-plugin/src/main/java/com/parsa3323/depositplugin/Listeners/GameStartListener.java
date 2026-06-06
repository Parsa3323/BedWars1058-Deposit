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

package com.parsa3323.depositplugin.Listeners;

import com.andrei1058.bedwars.api.arena.GameState;
import com.andrei1058.bedwars.api.arena.IArena;
import com.andrei1058.bedwars.api.events.gameplay.GameStateChangeEvent;
import com.andrei1058.bedwars.api.events.gameplay.TeamAssignEvent;
import com.parsa3323.depositplugin.Configs.MainConfig;
import com.parsa3323.depositplugin.DepositPlugin;
import com.parsa3323.depositplugin.utils.DepositUtils;
import com.parsa3323.depositplugin.utils.HologramUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashSet;
import java.util.Set;

public class GameStartListener implements Listener {

    private final Set<String> hologramSpawnedWorlds = new HashSet<>();
    private final Set<String> successfulAssignWorlds  = new HashSet<>();
    private final Set<String> successfulStateWorlds   = new HashSet<>();
    private final Set<String> fallbackScheduledWorlds = new HashSet<>();

    private String resolveWorldName(IArena arena) {
        World world = arena.getWorld();
        return world != null ? world.getName() : arena.getWorldName();
    }

    private void clearWorldState(String worldName) {
        successfulAssignWorlds.remove(worldName);
        successfulStateWorlds.remove(worldName);
        hologramSpawnedWorlds.remove(worldName);
        fallbackScheduledWorlds.remove(worldName);
    }

    @EventHandler
    public void onGameStateChange(GameStateChangeEvent event) {
        IArena arena     = event.getArena();
        GameState state  = event.getNewState();
        String worldName = resolveWorldName(arena);

        if (state == GameState.restarting || state == GameState.waiting) {
            clearWorldState(worldName);
            return;
        }

        if (state != GameState.playing) return;

        World world = arena.getWorld();
        if (world == null) {
            Bukkit.getLogger().warning("[DepositPlugin] World is null for arena: " + arena.getWorldName());
            return;
        }

        String configEvent = MainConfig.get().getString("hologram-register-event");

        if ("GameStateChangeEvent".equalsIgnoreCase(configEvent)) {
            DepositPlugin.debug("GameStateChangeEvent triggered for world: " + worldName);
            successfulStateWorlds.add(worldName);

            if (!hologramSpawnedWorlds.add(worldName)) {
                DepositPlugin.debug("Holograms already spawned for world: " + worldName);
                return;
            }

            DepositUtils.setChestLocations(world);

            if (MainConfig.get().getBoolean("deposit-holograms")) {
                HologramUtils.spawnDepositHolograms(world);
            }

            return;
        }

        if (!fallbackScheduledWorlds.add(worldName)) return;

        new BukkitRunnable() {
            @Override
            public void run() {
                if (!successfulAssignWorlds.contains(worldName) &&
                        "TeamAssignEvent".equalsIgnoreCase(
                                MainConfig.get().getString("hologram-register-event"))) {

                    arena.getPlayers().forEach(player -> {
                        if (player != null && player.isOnline() && player.isOp()) {
                            player.sendMessage(ChatColor.RED +
                                    "Hologram registration failed. Consider switching " +
                                    "hologram-register-event to GameStateChangeEvent.");
                        }
                    });
                }
            }
        }.runTaskLater(DepositPlugin.plugin, 20L);
    }

    @EventHandler
    public void onTeamAssign(TeamAssignEvent event) {
        String configuredEvent = MainConfig.get().getString("hologram-register-event");
        boolean isTeamAssignMode = "TeamAssignEvent".equalsIgnoreCase(configuredEvent);

        IArena arena = event.getArena();
        if (arena == null) return;

        String worldName = resolveWorldName(arena);

        if (!isTeamAssignMode) {
            if (!fallbackScheduledWorlds.add(worldName)) return;

            new BukkitRunnable() {
                @Override
                public void run() {
                    if (!successfulStateWorlds.contains(worldName) &&
                            "GameStateChangeEvent".equalsIgnoreCase(
                                    MainConfig.get().getString("hologram-register-event"))) {

                        arena.getPlayers().forEach(player -> {
                            if (player != null && player.isOnline() && player.isOp()) {
                                player.sendMessage(ChatColor.RED +
                                        "Hologram registration failed. Consider switching " +
                                        "hologram-register-event to TeamAssignEvent.");
                            }
                        });
                    }
                }
            }.runTaskLater(DepositPlugin.plugin, 10L);

            return;
        }

        World world = arena.getWorld();
        if (world == null) {
            Player player = event.getPlayer();
            if (player != null) {
                world = player.getWorld();
            }
        }

        if (world == null) {
            Bukkit.getLogger().warning("[DepositPlugin] Could not resolve world for TeamAssignEvent.");
            return;
        }

        final World resolvedWorld = world;

        DepositPlugin.debug("TeamAssignEvent triggered for world: " + worldName);
        successfulAssignWorlds.add(worldName);

        if (!hologramSpawnedWorlds.add(worldName)) {
            DepositPlugin.debug("Holograms already spawned for world: " + worldName);
            return;
        }

        DepositUtils.setChestLocations(resolvedWorld);

        if (MainConfig.get().getBoolean("deposit-holograms")) {
            HologramUtils.spawnDepositHolograms(resolvedWorld);
        }
    }
}
