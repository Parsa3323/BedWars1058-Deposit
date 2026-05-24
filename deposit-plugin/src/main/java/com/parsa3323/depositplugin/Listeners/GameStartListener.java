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
import com.parsa3323.depositplugin.Configs.ArenaConfig;
import com.parsa3323.depositplugin.Configs.MainConfig;
import com.parsa3323.depositplugin.DepositPlugin;
import com.parsa3323.depositplugin.utils.DepositUtils;
import com.parsa3323.depositplugin.utils.HologramUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;

public class GameStartListener implements Listener {

    private final FileConfiguration config = ArenaConfig.get();
    public boolean successGameAssign = false;
    public boolean successGameState = false;

    @EventHandler
    public void onGameStart(GameStateChangeEvent event) {
        if (event.getNewState() != GameState.playing) return;

        String configEvent = MainConfig.get().getString("hologram-register-event");
        IArena arena = event.getArena();
        World world = arena.getWorld();

        if (world == null) {
            Bukkit.getLogger().warning("World is null for arena " + arena.getWorldName());
            return;
        }

        if ("GameStateChangeEvent".equalsIgnoreCase(configEvent)) {
            DepositPlugin.debug("GameStateChangeEvent triggered for world: " + world.getName());
            successGameState = true;

            DepositUtils.setChestLocations(world);

            if (MainConfig.get().getBoolean("deposit-holograms")) {
                HologramUtils.spawnDepositHolograms(world);
            }

            return;
        }

        new BukkitRunnable(){
            @Override
            public void run() {
                if (!successGameAssign && MainConfig.get().getString("hologram-register-event").equalsIgnoreCase("TeamAssignEvent")) {
                    event.getArena().getPlayers().forEach(player -> {
                        if (player.isOp()) {
                            player.sendMessage(ChatColor.RED + "It looks like your event for registering holograms didn't work, you can make it work with changing hologram-register-event value to GameStateChangeEvent");
                        }
                    });
                }
            }
        }.runTaskLaterAsynchronously(DepositPlugin.plugin, 20L);
    }

    @EventHandler
    public void onGameAssign(TeamAssignEvent event) {
        String configuredEvent = MainConfig.get().getString("hologram-register-event");
        if ("TeamAssignEvent".equalsIgnoreCase(configuredEvent)) {
            DepositPlugin.debug("TeamAssignEvent triggered");
            successGameAssign = true;

            Player player = event.getPlayer();
            World world = player.getWorld();
            if (world == null) {
                Bukkit.getLogger().warning("World is null for player " + player.getName());
                return;
            }

            DepositUtils.setChestLocations(world);

            if (MainConfig.get().getBoolean("deposit-holograms")) {
                HologramUtils.spawnDepositHolograms(world);
            }

            return;
        }



        new BukkitRunnable(){
            @Override
            public void run() {
                if (!successGameState && MainConfig.get().getString("hologram-register-event").equalsIgnoreCase("GameStateChangeEvent")) {
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