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
import com.parsa3323.depositplugin.cache.ChestOwnerCache;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import static com.andrei1058.bedwars.api.language.Language.getMsg;

public class DepositListener implements Listener {

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPlayerDeposit(PlayerDepositEvent event) {
        if (event.getDepositType() != DepositType.CHEST) return;

        final Player p = event.getPlayer();
        DepositPlugin.debug("Checking deposit event for player: " + p.getName());
        final IArena arena = DepositPlugin.bedWars.getArenaUtil().getArenaByPlayer(p);
        if (arena == null) {
            DepositPlugin.debug("Arena is null for player: " + p.getName());
            return;
        }
        final Block block = event.getBlock();
        if (block == null) {
            DepositPlugin.debug("Block is null for player: " + p.getName());
            return;
        }

        final Location blockLoc = block.getLocation();
        final ChestOwnerCache.Lookup lookup = ChestOwnerCache.lookup(blockLoc);
        final ITeam chestOwner;

        switch (lookup.result) {
            case OWNED:
                chestOwner = lookup.team;
                DepositPlugin.debug("Cache HIT — chest at " + blockLoc
                        + " owned by " + chestOwner.getName());
                break;

            case NO_OWNER:
                DepositPlugin.debug("Cache HIT — chest at " + blockLoc
                        + " has no owning team — allowing.");
                return;

            case CACHE_MISS:
            default:
                final int islandRadius     = arena.getConfig().getInt(ConfigPath.ARENA_ISLAND_RADIUS);
                final double radiusSquared = (double) islandRadius * islandRadius;

                chestOwner = ChestOwnerCache.computeOwner(blockLoc, arena, radiusSquared);
                ChestOwnerCache.store(blockLoc, chestOwner);

                if (chestOwner == null) {
                    DepositPlugin.debug("Cache MISS (computed) — no team owns chest at "
                            + blockLoc + " — allowing and storing.");
                    return;
                }

                DepositPlugin.debug("Cache MISS (computed) — chest at " + blockLoc
                        + " owned by " + chestOwner.getName() + " — stored for future clicks.");
                break;
        }

        final boolean notMember       = !chestOwner.isMember(p);
        final boolean teamHasMembers  = !chestOwner.getMembers().isEmpty();

        if (notMember && teamHasMembers) {
            DepositPlugin.debug("Player " + p.getName()
                    + " is NOT a member of " + chestOwner.getName()
                    + " and that team still has members — cancelling deposit.");
            event.setCancelled(true);
            p.sendMessage(getMsg(p, Messages.INTERACT_CHEST_CANT_OPEN_TEAM_ELIMINATED));
        } else {
            DepositPlugin.debug("Player " + p.getName()
                    + " is allowed to deposit into " + chestOwner.getName() + "'s chest.");
        }
    }
}
