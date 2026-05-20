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

import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class SelectionUtils {
    private static final Set<UUID> selectionModePlayers = Collections.synchronizedSet(new HashSet<>());

    public static Set<UUID> getSelectionModePlayers() {
        return selectionModePlayers;
    }

    public static void addPlayerToSelectionMode(Player player) {
        selectionModePlayers.add(player.getUniqueId());
    }

    public static void removePlayerFromSelectionMode(Player player) {
        selectionModePlayers.remove(player.getUniqueId());
    }
}
