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

import com.parsa3323.depositplugin.DepositPlugin;
import com.parsa3323.depositplugin.utils.DepositUtils;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scheduler.BukkitRunnable;

public class PlayerJoinListener implements Listener {
    @EventHandler
    public void playerJoinEvent(PlayerJoinEvent event) {
        if (DepositPlugin.plugin.configuration.getBoolean("set-chest-locations-on-join")) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    DepositPlugin.debug("WorldLoadEvent ran");
                    DepositUtils.setChestLocationsAll();
                    DepositPlugin.debug("WorldLoadEvent done");
                }
            }.runTaskAsynchronously(DepositPlugin.plugin);
        }
    }
}