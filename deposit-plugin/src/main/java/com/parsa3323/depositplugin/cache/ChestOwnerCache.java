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

package com.parsa3323.depositplugin.cache;

import com.andrei1058.bedwars.api.arena.IArena;
import com.andrei1058.bedwars.api.arena.team.ITeam;
import com.andrei1058.bedwars.api.configuration.ConfigPath;
import com.parsa3323.depositplugin.Configs.ArenaConfig;
import com.parsa3323.depositplugin.DepositPlugin;
import com.parsa3323.depositplugin.utils.DepositUtils;
import org.bukkit.Location;
import org.bukkit.World;

import java.util.*;

public final class ChestOwnerCache {

    private ChestOwnerCache() {
        throw new UnsupportedOperationException("ChestOwnerCache is a utility class.");
    }
    public enum Result {
        CACHE_MISS,
        NO_OWNER,
        OWNED
    }
    public static final class Lookup {
        public final Result result;
        public final ITeam team;

        private Lookup(Result result, ITeam team) {
            this.result = result;
            this.team   = team;
        }

        static Lookup miss()      { return new Lookup(Result.CACHE_MISS, null); }
        static Lookup noOwner()   { return new Lookup(Result.NO_OWNER,   null); }
        static Lookup owned(ITeam t) { return new Lookup(Result.OWNED, t); }
    }

    public static final class BlockKey {
        private final int x, y, z;
        private final int hash;

        public BlockKey(Location loc) {
            this(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
        }

        public BlockKey(int x, int y, int z) {
            this.x = x;
            this.y = y;
            this.z = z;
            int h = x;
            h = 31 * h + y;
            h = 31 * h + z;
            this.hash = h;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof BlockKey)) return false;
            BlockKey other = (BlockKey) o;
            return x == other.x && y == other.y && z == other.z;
        }

        @Override
        public int hashCode() {
            return hash;
        }

        @Override
        public String toString() {
            return x + "," + y + "," + z;
        }
    }

    private static final Map<UUID, Map<BlockKey, java.util.Optional<ITeam>>> CACHE = new HashMap<>();
    public static Lookup lookup(Location loc) {
        final Map<BlockKey, java.util.Optional<ITeam>> worldCache = CACHE.get(loc.getWorld().getUID());
        if (worldCache == null) {
            return Lookup.miss();
        }

        final java.util.Optional<ITeam> cached = worldCache.get(new BlockKey(loc));
        if (cached == null) {
            return Lookup.miss();
        }

        final ITeam team = cached.orElse(null);
        return (team == null) ? Lookup.noOwner() : Lookup.owned(team);
    }
    public static void store(Location loc, ITeam team) {
        CACHE.computeIfAbsent(loc.getWorld().getUID(), k -> new HashMap<>())
                .put(new BlockKey(loc), java.util.Optional.ofNullable(team));
    }
    public static void populate(IArena arena) {
        final World world = arena.getWorld();
        if (world == null) {
            DepositPlugin.warn("ChestOwnerCache.populate: null world for arena '" + arena.getWorldName() + "'");
            return;
        }

        final String configPath = "worlds." + world.getName() + ".chestLocations";
        final List<String> registered = ArenaConfig.get().getStringList(configPath);
        if (registered.isEmpty()) {
            DepositPlugin.debug("ChestOwnerCache.populate: no registered chests for " + world.getName());
            return;
        }

        final int islandRadius    = arena.getConfig().getInt(ConfigPath.ARENA_ISLAND_RADIUS);
        final double radiusSquared = (double) islandRadius * islandRadius;
        int cached = 0;

        final Map<BlockKey, java.util.Optional<ITeam>> worldCache =
                CACHE.computeIfAbsent(world.getUID(), k -> new HashMap<>());

        for (String serialized : registered) {
            try {
                final Location loc = DepositUtils.deserializeLocation(serialized, world);
                final ITeam owner  = computeOwner(loc, arena, radiusSquared);
                worldCache.put(new BlockKey(loc), java.util.Optional.ofNullable(owner));
                cached++;
            } catch (IllegalArgumentException ex) {
                DepositPlugin.warn("ChestOwnerCache.populate: malformed location '" + serialized
                        + "' in arena " + arena.getWorldName() + " — skipped.");
            }
        }

        DepositPlugin.debug("ChestOwnerCache.populate: pre-cached " + cached
                + " chest location(s) for arena " + arena.getWorldName());
    }
    public static void invalidate(IArena arena) {
        final World world = arena.getWorld();
        if (world == null) return;

        CACHE.remove(world.getUID());

        DepositPlugin.debug("ChestOwnerCache.invalidate: removed all entries for arena " + arena.getWorldName());
    }
    public static void clear() {
        int totalEntries = 0;
        for (Map<BlockKey, java.util.Optional<ITeam>> worldCache : CACHE.values()) {
            totalEntries += worldCache.size();
        }
        final int worlds = CACHE.size();
        CACHE.clear();
        DepositPlugin.debug("ChestOwnerCache.clear: purged " + totalEntries
                + " entries across " + worlds + " world(s)");
    }
    public static ITeam computeOwner(Location blockLoc, IArena arena, double radiusSquared) {
        return arena.getTeams().stream()
                .filter(Objects::nonNull)
                .filter(team -> team.getSpawn() != null)
                .filter(team -> {
                    final World spawnWorld = team.getSpawn().getWorld();
                    final World blockWorld = blockLoc.getWorld();
                    return spawnWorld != null
                            && blockWorld != null
                            && spawnWorld.getUID().equals(blockWorld.getUID());
                })
                .filter(team -> team.getSpawn().distanceSquared(blockLoc) <= radiusSquared)
                .findFirst()
                .orElse(null);
    }
}
