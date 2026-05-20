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
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

public class ChestClickListener implements Listener {

    @EventHandler
    public void onPlayerLeftClickEnderChest(PlayerInteractEvent e) {
        Player p = e.getPlayer();
        Block clickedBlock = e.getClickedBlock();

        if (clickedBlock == null || e.getAction() != Action.LEFT_CLICK_BLOCK) return;

        Material blockType = clickedBlock.getType();
        if (blockType != Material.ENDER_CHEST && blockType != Material.CHEST) return;

        if (DepositPlugin.bedWars.isInSetupSession(p.getUniqueId()) && p.isSneaking()) {
            DepositUtils.handleSetupSession(p, clickedBlock);
            e.setCancelled(true);
            return;
        }

        DepositUtils.deposit(p, clickedBlock, blockType);
        e.setCancelled(true);
    }

}