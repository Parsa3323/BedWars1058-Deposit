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

/**
 * O(1) coordinate cache that maps chest block locations to their owning team.
 *
 * <p><b>Problem solved:</b> The original {@code DepositListener} ran a Java Stream
 * over all arena teams on every deposit click, executing world-UID comparisons and
 * {@code distanceSquared()} geometry math each time. In high-action BedWars games
 * this fires dozens of times per second.
 *
 * <p><b>Solution:</b> Since arena chests and team spawns are static during a game,
 * proximity math is executed <em>once per chest location</em> — either eagerly at
 * game start (for pre-registered chests) or lazily on first deposit click. All
 * subsequent clicks become a single {@code HashMap#get()} with zero math.
 *
 * <p><b>3-state result design:</b>
 * <ul>
 *   <li>{@code CACHE_MISS} — location not seen before; caller must fall back to the
 *       stream, then {@link #store(Location, ITeam)} the result.</li>
 *   <li>{@code NO_OWNER} — location is in cache and is <em>not</em> within any
 *       team's island radius (e.g. a neutral chest). Caller should allow deposit.</li>
 *   <li>{@code OWNED} — location is in cache and belongs to a specific team.
 *       Caller must apply the usual security-gate checks (member, bed status).</li>
 * </ul>
 *
 * <p><b>Thread safety:</b> Every method in this class MUST be called from the
 * main server thread. The cache is backed by plain {@code HashMap} instances — no
 * {@code ConcurrentHashMap} overhead is needed because all accesses happen inside
 * Bukkit's single-threaded event system.
 *
 * <p><b>Cache lifecycle:</b> The cache is populated by {@link #populate(IArena)}
 * when a game starts and invalidated by {@link #invalidate(IArena)} when the game
 * ends or the arena restarts. Without proper invalidation, a subsequent game in
 * the same arena world could return stale team ownership data.
 *
 * <p><b>2026-06-07 refactor:</b> Replaced the flat {@code Map<String, Optional<ITeam>>}
 * with a nested {@code Map<UUID, Map<BlockKey, Optional<ITeam>>>}. This eliminates
 * string concatenation on the hot path and makes arena invalidation O(1) instead
 * of O(total cached entries).
 *
 * @see com.parsa3323.depositplugin.Listeners.ArenaLifecycleListener
 */
public final class ChestOwnerCache {

    // -----------------------------------------------------------------------
    // Constructor guard
    // -----------------------------------------------------------------------

    private ChestOwnerCache() {
        throw new UnsupportedOperationException("ChestOwnerCache is a utility class.");
    }

    // -----------------------------------------------------------------------
    // 3-state lookup result
    // -----------------------------------------------------------------------

    public enum Result {
        /** Location has never been seen — caller must compute and {@link #store}. */
        CACHE_MISS,
        /** Location is cached and has no owning team (neutral / out-of-radius chest). */
        NO_OWNER,
        /** Location is cached and belongs to the team returned in {@link Lookup#team}. */
        OWNED
    }

    /**
     * Immutable result of a cache lookup. When {@code result == OWNED},
     * {@code team} is non-null. For all other states {@code team} is null.
     */
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

    // -----------------------------------------------------------------------
    // Lightweight block-coordinate key
    //
    // Replaces the old String concatenation key with a primitive-only object.
    // This avoids StringBuilder + String allocations on every deposit click.
    // hashCode is pre-computed in the constructor so HashMap lookups are fast.
    //
    // NOTE: If you are on Java 16+, you can replace this class with a record:
    //   public record BlockKey(int x, int y, int z) {
    //       public BlockKey(Location loc) {
    //           this(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
    //       }
    //   }
    // -----------------------------------------------------------------------

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

    // -----------------------------------------------------------------------
    // Core storage
    //
    // Outer key: World UUID (immutable, never clashes between arenas).
    // Inner key: BlockKey (block coordinates — chests are always aligned to
    //            the block grid, so sub-block precision is wasted).
    // Value:    Optional<ITeam>
    //   • Optional.empty()  → this location was computed and has NO owning team
    //   • Optional.of(team) → this location belongs to 'team'
    //   • missing key       → CACHE_MISS (not computed yet)
    //
    // Using Optional<ITeam> as a map value lets us distinguish "known to have
    // no owner" from "not in cache yet" without a secondary data structure.
    //
    // Nested structure makes invalidate() O(1): CACHE.remove(worldUUID) drops
    // the entire inner map instantly. The old flat String-key map required an
    // O(N) scan with startsWith() prefix matching.
    // -----------------------------------------------------------------------

    private static final Map<UUID, Map<BlockKey, java.util.Optional<ITeam>>> CACHE = new HashMap<>();

    // -----------------------------------------------------------------------
    // Public API
    // -----------------------------------------------------------------------

    /**
     * Looks up the owning team for a chest block location.
     *
     * <p><b>O(1)</b> — a UUID map lookup, a BlockKey map lookup, and a few
     * object allocations (the BlockKey itself).
     *
     * @param loc the block location (typically from {@code event.getBlock().getLocation()})
     * @return a {@link Lookup} describing the cache state; never null
     */
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

    /**
     * Stores the result of a stream computation in the cache.
     * Pass {@code null} for {@code team} to record "no owner".
     *
     * <p>Idempotent — calling store() twice with the same location is safe.
     *
     * @param loc  the chest block location
     * @param team the owning team, or null if the chest is neutral
     */
    public static void store(Location loc, ITeam team) {
        CACHE.computeIfAbsent(loc.getWorld().getUID(), k -> new HashMap<>())
                .put(new BlockKey(loc), java.util.Optional.ofNullable(team));
    }

    /**
     * Eagerly pre-computes ownership for all registered chest locations in the
     * arena's world and stores them in the cache.
     *
     * <p>This is called by {@link com.parsa3323.depositplugin.Listeners.ArenaLifecycleListener}
     * when a game transitions to PLAYING state. Pre-population eliminates even the
     * first-cache-miss penalty for the arena's standard chests.
     *
     * <p>Chests placed mid-game (e.g. by players) are not in the registered list;
     * they are handled by lazy loading via the stream fallback in
     * {@link com.parsa3323.depositplugin.Listeners.DepositListener}.
     *
     * @param arena the arena whose registered chests should be pre-cached
     */
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

    /**
     * Removes all cache entries belonging to the given arena's world.
     * Called when a game ends or the arena restarts/reloads.
     *
     * <p><b>Without this,</b> a subsequent game in the same arena world would
     * reuse stale team mappings, causing deposit security checks to evaluate
     * against the wrong teams silently.
     *
     * <p><b>Performance:</b> O(1) — a single {@code HashMap#remove()} on the
     * outer map. The old flat-map implementation required an O(N) scan.
     *
     * @param arena the arena whose cache entries should be purged
     */
    public static void invalidate(IArena arena) {
        final World world = arena.getWorld();
        if (world == null) return;

        CACHE.remove(world.getUID());

        DepositPlugin.debug("ChestOwnerCache.invalidate: removed all entries for arena " + arena.getWorldName());
    }

    /**
     * Clears the entire cache. Use with caution — intended for plugin reload
     * or shutdown scenarios.
     */
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

    // -----------------------------------------------------------------------
    // Internal helpers
    // -----------------------------------------------------------------------

    /**
     * Computes the owning team for a chest location using the exact same stream
     * logic that {@link com.parsa3323.depositplugin.Listeners.DepositListener}
     * used originally. This ensures cache hits and cache misses produce
     * <em>identical</em> results.
     *
     * @param blockLoc      the chest block location
     * @param arena         the arena containing the teams
     * @param radiusSquared pre-computed squared island radius
     * @return the owning team, or null if the chest is not within any team's island
     */
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
