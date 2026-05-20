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
import com.andrei1058.bedwars.api.arena.team.ITeam;
import com.andrei1058.bedwars.api.configuration.ConfigPath;
import com.andrei1058.bedwars.api.language.Messages;
import com.parsa3323.depositapi.Events.PlayerDepositEvent;
import com.parsa3323.depositapi.Types.DepositType;
import com.parsa3323.depositplugin.DepositPlugin;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.Objects;

import static com.andrei1058.bedwars.api.language.Language.getMsg;

public class DepositListener implements Listener {

    @EventHandler
    public void onPlayerDeposit(PlayerDepositEvent event) {
        if (event.getDepositType() != DepositType.CHEST) return;

        Player p = event.getPlayer();
        DepositPlugin.debug("Checking deposit event for player: " + p.getName());

        IArena arena = DepositPlugin.bedWars.getArenaUtil().getArenaByPlayer(p);
        if (arena == null) {
            DepositPlugin.debug("Arena is null for player: " + p.getName());
            return;
        }

        Block block = event.getBlock();
        if (block == null) {
            DepositPlugin.debug("Block is null for player: " + p.getName());
            return;
        }

        int islandRadius = arena.getConfig().getInt(ConfigPath.ARENA_ISLAND_RADIUS);
        Location blockLoc = block.getLocation();

        ITeam chestOwner = arena.getTeams().stream()
                .filter(Objects::nonNull)
                .filter(team -> team.getSpawn() != null)
                .filter(team -> team.getSpawn().distance(blockLoc) <= islandRadius)
                .findFirst()
                .orElse(null);

        if (chestOwner == null) {
            DepositPlugin.debug("No team owns this chest.");
            return;
        }

        if (!chestOwner.isMember(p) && !chestOwner.getMembers().isEmpty() && !chestOwner.isBedDestroyed()) {
            DepositPlugin.debug("Player " + p.getName() + " is NOT a member of " + chestOwner.getName());
            event.setCancelled(true);
            p.sendMessage(getMsg(p, Messages.INTERACT_CHEST_CANT_OPEN_TEAM_ELIMINATED));
        } else {
            DepositPlugin.debug("Player " + p.getName() + " is a member of " + chestOwner.getName() + ", allowing access.");
        }
    }
}
