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
import com.parsa3323.depositplugin.cache.ChestOwnerCache;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

/**
 * Manages the {@link ChestOwnerCache} lifecycle across BedWars arena state transitions.
 *
 * <p><b>Why this is critical:</b> The chest-owner cache stores static team→chest
 * mappings that are valid only for the duration of a single game. If a game ends
 * and a new one starts in the same arena world (common on shared BedWars servers),
 * stale cache entries would silently return the wrong team for chest locations,
 * causing the ownership security gate in {@link DepositListener} to evaluate
 * against incorrect teams.
 *
 * <p><b>Lifecycle rules:</b>
 * <ul>
 *   <li><b>PLAYING</b> → eagerly {@link ChestOwnerCache#populate populate} the cache
 *       with all registered chest locations for this arena. Future deposits are O(1).</li>
 *   <li><b>Any other state</b> (WAITING, STARTING, RESTARTING, DISABLED) →
 *       {@link ChestOwnerCache#invalidate invalidate} all entries for this arena.
 *       This is defensive: we invalidate on any non-PLAYING transition because an
 *       arena reload or restart can change team spawn locations.</li>
 * </ul>
 *
 * <p><b>Event priority = MONITOR</b> — this listener only observes; it does not
 * modify event outcomes. MONITOR ensures we see the final state after all other
 * plugins have processed the transition.
 */
public class ArenaLifecycleListener implements Listener {

    /**
     * Reacts to arena state changes to keep the chest-owner cache consistent.
     *
     * @param event the BedWars arena state-change event
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onGameStateChange(GameStateChangeEvent event) {
        final IArena arena    = event.getArena();
        final GameState state = event.getNewState();

        if (state == GameState.playing) {
            // Game is now active — team spawns and island radii are final.
            // Pre-compute ownership for all registered chests so deposits
            // are O(1) from the first click.
            ChestOwnerCache.populate(arena);
        } else {
            // Arena is leaving the PLAYING state (RESTARTING, WAITING, DISABLED, etc.).
            // Invalidate immediately so the next game starts with a clean cache.
            ChestOwnerCache.invalidate(arena);
        }
    }
}
