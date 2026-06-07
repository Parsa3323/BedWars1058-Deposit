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

import com.andrei1058.bedwars.api.arena.IArena;
import com.parsa3323.depositplugin.Configs.ArenaConfig;
import com.parsa3323.depositplugin.Configs.MainConfig;
import com.parsa3323.depositplugin.DepositPlugin;
import com.parsa3323.depositplugin.utils.DepositUtils;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerJoinListener implements Listener {
    @EventHandler
    public void playerJoinEvent(PlayerJoinEvent event) {
        if (!MainConfig.get().getBoolean("set-chest-locations-on-join", false)) {
            return;
        }
        boolean needsScan = false;
        for (IArena arena : DepositPlugin.bedWars.getArenaUtil().getArenas()) {
            final World world = arena.getWorld();
            if (world == null) continue;
            final String path = "worlds." + world.getName() + ".chestLocations";
            if (!ArenaConfig.get().contains(path)) {
                needsScan = true;
                break;
            }
        }

        if (!needsScan) {
            DepositPlugin.debug("PlayerJoinEvent: all arena worlds already indexed — skipping scan.");
            return;
        }

        Bukkit.getScheduler().runTask(DepositPlugin.plugin, () -> {
            DepositPlugin.debug("PlayerJoinEvent: unindexed arena worlds detected — scanning chest locations");
            DepositUtils.setChestLocationsAll();
            DepositPlugin.debug("PlayerJoinEvent: chest location scan done");
        });
    }
}
