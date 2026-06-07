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

import com.parsa3323.depositplugin.Configs.MainConfig;
import com.parsa3323.depositplugin.DepositPlugin;
import com.parsa3323.depositplugin.utils.DepositUtils;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

public class ChestClickListener implements Listener {

    private static final boolean HAS_OFF_HAND_API;
    private static final Object HAND_SLOT;

    static {
        boolean hasApi = false;
        Object hand = null;
        try {
            Class<?> equipmentSlotClass = Class.forName("org.bukkit.inventory.EquipmentSlot");
            hand = equipmentSlotClass.getField("HAND").get(null);
            PlayerInteractEvent.class.getMethod("getHand");
            hasApi = true;
        } catch (Exception ignored) {
        }
        HAS_OFF_HAND_API = hasApi;
        HAND_SLOT = hand;
    }

    
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onPlayerLeftClickChest(PlayerInteractEvent e) {
        if (HAS_OFF_HAND_API) {
            try {
                Object slot = PlayerInteractEvent.class.getMethod("getHand").invoke(e);
                if (slot != HAND_SLOT) return;
            } catch (Exception ex) {
                return;
            }
        }
        if (e.getAction() != Action.LEFT_CLICK_BLOCK) return;

        final Block clickedBlock = e.getClickedBlock();
        if (clickedBlock == null) return;

        final Material blockType = clickedBlock.getType();
        if (blockType != Material.CHEST && blockType != Material.ENDER_CHEST) return;

        final Player p = e.getPlayer();
        if (DepositPlugin.bedWars.isInSetupSession(p.getUniqueId())
                && p.isSneaking()
                && MainConfig.get().getBoolean("shift-click-on-chest-to-set", true)) {
            DepositUtils.handleSetupSession(p, clickedBlock);
            e.setCancelled(true);
            return;
        }

        // Cancel before deposit so the vanilla chest-open UI never appears
        // even if deposit() throws mid-execution.
        e.setCancelled(true);
        DepositUtils.deposit(p, clickedBlock, blockType);
    }
}
