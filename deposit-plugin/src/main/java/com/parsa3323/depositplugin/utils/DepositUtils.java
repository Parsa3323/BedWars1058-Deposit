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

import com.andrei1058.bedwars.api.BedWars;
import com.andrei1058.bedwars.api.arena.IArena;
import com.andrei1058.bedwars.api.server.ISetupSession;
import com.cryptomorin.xseries.XMaterial;
import com.cryptomorin.xseries.XSound;
import com.parsa3323.depositapi.Events.PlayerDepositEvent;
import com.parsa3323.depositapi.Types.DepositType;
import com.parsa3323.depositplugin.Configs.ArenaConfig;
import com.parsa3323.depositplugin.Configs.MainConfig;
import com.parsa3323.depositplugin.Configs.MessageConfig;
import com.parsa3323.depositplugin.DepositPlugin;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Core utility class for the Deposit plugin //REMAKE BY 1xMohameeD. GITHUB: 1xMohameeD0101.
 * This took me 5 days to refactor don't forget the star :3
 **/

public final class DepositUtils {
    private DepositUtils() {
        throw new UnsupportedOperationException("DepositUtils is a utility class.");
    }

    private static final Set<UUID> DEPOSIT_LOCK = ConcurrentHashMap.newKeySet();

    private static final long DEPOSIT_COOLDOWN_MS = 500L;
    private static final Map<UUID, Long> LAST_DEPOSIT_MS = new ConcurrentHashMap<>();

    public static void clearPlayerState(UUID uuid) {
        LAST_DEPOSIT_MS.remove(uuid);
        DEPOSIT_LOCK.remove(uuid);
    }

    private static final Set<Material> BLACKLISTED_ITEMS;

    static {
        final XMaterial[] BLACKLISTED_XMATERIALS = {
                XMaterial.WOODEN_SWORD,    XMaterial.STONE_SWORD,
                XMaterial.IRON_SWORD,      XMaterial.GOLDEN_SWORD,
                XMaterial.DIAMOND_SWORD,   XMaterial.NETHERITE_SWORD,
                XMaterial.WOODEN_PICKAXE,  XMaterial.STONE_PICKAXE,
                XMaterial.IRON_PICKAXE,    XMaterial.GOLDEN_PICKAXE,
                XMaterial.DIAMOND_PICKAXE, XMaterial.NETHERITE_PICKAXE,
                XMaterial.WOODEN_AXE,      XMaterial.STONE_AXE,
                XMaterial.IRON_AXE,        XMaterial.GOLDEN_AXE,
                XMaterial.DIAMOND_AXE,     XMaterial.NETHERITE_AXE,
                XMaterial.SHEARS,          XMaterial.COMPASS,
        };

        final EnumSet<Material> set = EnumSet.noneOf(Material.class);
        for (XMaterial xm : BLACKLISTED_XMATERIALS) {
            final Material resolved = xm.parseMaterial();
            if (resolved != null) {
                set.add(resolved);
            } else {
                DepositPlugin.debug("Blacklist: " + xm.name()
                        + " does not exist on this server version — skipped.");
            }
        }
        BLACKLISTED_ITEMS = Collections.unmodifiableSet(set);
        DepositPlugin.debug("Blacklist built with " + BLACKLISTED_ITEMS.size() + " entries.");
    }

    private static final EnumMap<Material, ChatColor> ITEM_COLOR_MAP = new EnumMap<>(Material.class);

    static {
        mapItemColor(XMaterial.GOLD_INGOT,               ChatColor.GOLD);
        mapItemColor(XMaterial.GOLDEN_APPLE,             ChatColor.GOLD);
        mapItemColor(XMaterial.ENCHANTED_GOLDEN_APPLE,   ChatColor.GOLD);

        mapItemColor(XMaterial.IRON_INGOT,               ChatColor.WHITE);

        mapItemColor(XMaterial.BRICK,                    ChatColor.RED);
        mapItemColor(XMaterial.CLAY_BALL,                ChatColor.GRAY);

        mapItemColor(XMaterial.DIAMOND,                  ChatColor.AQUA);

        mapItemColor(XMaterial.EMERALD,                  ChatColor.GREEN);

        DepositPlugin.debug("ITEM_COLOR_MAP built with " + ITEM_COLOR_MAP.size() + " entries.");
    }

    private static void mapItemColor(XMaterial xm, ChatColor color) {
        final Material mat = xm.parseMaterial();
        if (mat != null) {
            ITEM_COLOR_MAP.put(mat, color);
        } else {
            DepositPlugin.debug("ITEM_COLOR_MAP: " + xm.name()
                    + " does not exist on this server version — skipped.");
        }
    }

    private static final class CachedTemplate {
        final String raw;
        final String translated;

        CachedTemplate(String raw, String translated) {
            this.raw = raw;
            this.translated = translated;
        }
    }

    private static final Map<String, CachedTemplate> MESSAGE_TEMPLATE_CACHE = new HashMap<>();

    private static String getCachedTemplate(String key) {
        final String raw = MessageConfig.get().getString(key);
        if (raw == null) {
            return null;
        }

        final CachedTemplate cached = MESSAGE_TEMPLATE_CACHE.get(key);
        if (cached != null && cached.raw.equals(raw)) {
            return cached.translated;
        }

        final String translated = ChatColor.translateAlternateColorCodes('&', raw);
        MESSAGE_TEMPLATE_CACHE.put(key, new CachedTemplate(raw, translated));
        return translated;
    }

    public static void clearMessageCache() {
        MESSAGE_TEMPLATE_CACHE.clear();
        DepositPlugin.debug("MESSAGE_TEMPLATE_CACHE cleared.");
    }

    public static boolean isBlacklistedItem(ItemStack item) {
        return item == null
                || item.getType() == Material.AIR
                || BLACKLISTED_ITEMS.contains(item.getType());
    }

    public static List<String> getArenaChests(World world) {
        DepositPlugin.debug("getArenaChests() called for world: " + world.getName());
        return ArenaConfig.get().getStringList("worlds." + world.getName() + ".chestLocations");
    }

    public static String serializeLocation(Location location) {
        return location.getBlockX() + "," + location.getBlockY() + "," + location.getBlockZ();
    }

    public static Location deserializeLocation(String locString, World world) {
        final String[] parts = locString.split(",");
        if (parts.length != 3) {
            throw new IllegalArgumentException(
                    "Malformed location string (expected x,y,z): " + locString);
        }
        try {
            return new Location(world,
                    Integer.parseInt(parts[0].trim()),
                    Integer.parseInt(parts[1].trim()),
                    Integer.parseInt(parts[2].trim()));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(
                    "Non-integer coordinate in location string: " + locString, e);
        }
    }


    private static final Method SET_ITEM_IN_MAIN_HAND;

    static {
        Method m = null;
        try {
            m = org.bukkit.inventory.PlayerInventory.class.getMethod("setItemInMainHand", ItemStack.class);
        } catch (NoSuchMethodException ignored) {
            // 1.8 — method does not exist.
        }
        SET_ITEM_IN_MAIN_HAND = m;
    }


    // deposit() — MAIN THREAD ONLY
    public static void deposit(Player player, Block block, Material blockType) {
        assertMainThread("deposit()");

        final UUID uid = player.getUniqueId();

        if (DEPOSIT_LOCK.contains(uid)) {
            DepositPlugin.debug("deposit() same-tick re-entrance blocked for " + player.getName());
            return;
        }
        DEPOSIT_LOCK.add(uid);
        // Released at end of current tick — never leaks even if we throw.
        Bukkit.getScheduler().runTask(DepositPlugin.plugin, () -> DEPOSIT_LOCK.remove(uid));

        final long now     = System.currentTimeMillis();
        final Long lastMs  = LAST_DEPOSIT_MS.get(uid);
        if (lastMs != null && (now - lastMs) < DEPOSIT_COOLDOWN_MS) {
            DepositPlugin.debug("deposit() cross-tick cooldown active for " + player.getName()
                    + " (gap=" + (now - lastMs) + "ms, required=" + DEPOSIT_COOLDOWN_MS + "ms)");
            return;
        }
        LAST_DEPOSIT_MS.put(uid, now);

        final BedWars bw = Bukkit.getServicesManager()
                .getRegistration(BedWars.class).getProvider();
        if (!bw.getArenaUtil().isPlaying(player)) return;
        DepositPlugin.debug(player.getName() + " left-clicked " + blockType.name());

        final ItemStack rawHeld = player.getInventory().getItemInHand();
        if (isBlacklistedItem(rawHeld)) return;
        final ItemStack heldSnapshot = rawHeld.clone();
        final boolean    isEnderChest = blockType == Material.ENDER_CHEST;
        final DepositType depositType = isEnderChest
                ? DepositType.ENDER_CHEST
                : DepositType.CHEST;

        final Inventory targetInventory = resolveInventory(player, block, isEnderChest);
        if (targetInventory == null) {
            DepositPlugin.warn("Could not resolve inventory for block at "
                    + serializeLocation(block.getLocation()));
            return;
        }

        final PlayerDepositEvent event = new PlayerDepositEvent(player, depositType, block);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            DepositPlugin.debug("PlayerDepositEvent cancelled for " + player.getName());
            return;
        }

        final ItemStack handNow = player.getInventory().getItemInHand();
        if (!inventoryConsistent(heldSnapshot, handNow)) {
            DepositPlugin.warn("Inventory consistency check FAILED for " + player.getName()
                    + " — hand changed after event. Before=" + describeItem(heldSnapshot)
                    + " After=" + describeItem(handNow)
                    + ". Aborting deposit and resyncing inventory.");
            player.updateInventory(); // force client inventory resync
            return;
        }
        final ChatColor color = resolveItemColor(heldSnapshot.getType());

        if (MainConfig.get().getBoolean("deposit-whole-itemstack")) {
            depositWholeStack(player, heldSnapshot.getType(), targetInventory, depositType, color);
        } else {
            depositSingleStack(player, heldSnapshot, targetInventory, depositType, color);
        }
    }


    public static void depositSingleStack(
            Player player,
            ItemStack heldItem,
            Inventory targetInventory,
            DepositType depositType,
            ChatColor color) {

        assertMainThread("depositSingleStack()");
        if (isBlacklistedItem(heldItem)) return;


        final Map<Integer, ItemStack> leftover = targetInventory.addItem(heldItem.clone());

        int rejected = 0;
        for (ItemStack item : leftover.values()) {
            rejected += item.getAmount();
        }
        final int accepted = heldItem.getAmount() - rejected;

        if (accepted <= 0) {
            player.sendMessage(ChatColor.RED + "That chest doesn't have enough space!");
            return;
        }

        if (rejected == 0) {
            clearHand(player);
        } else {

            final ItemStack remainder = heldItem.clone();
            remainder.setAmount(rejected);
            setHandItem(player, remainder);
        }

        sendDepositMessage(player, accepted, heldItem.getType(), depositType, color);
        DepositPlugin.info(player.getName() + " deposited "
                + accepted + "x " + heldItem.getType()
                + " → " + depositType.name()
                + (rejected > 0 ? " (partial; " + rejected + " returned to hand)" : ""));
    }

    // depositWholeStack — MAIN THREAD ONLY
    private static void depositWholeStack(
            Player player,
            Material targetMat,
            Inventory targetInventory,
            DepositType depositType,
            ChatColor color) {

        assertMainThread("depositWholeStack()");

        int totalDeposited = 0;
        final int inventorySize = player.getInventory().getSize();

        for (int slot = 0; slot < inventorySize; slot++) {

            // Read live — never from a cached snapshot.
            final ItemStack stack = player.getInventory().getItem(slot);
            if (stack == null || stack.getType() != targetMat) continue;
            final int amountBeforeAdd = stack.getAmount();
            final Map<Integer, ItemStack> leftover = targetInventory.addItem(stack.clone());

            int rejected = 0;
            for (ItemStack item : leftover.values()) {
                rejected += item.getAmount();
            }

            final int accepted = amountBeforeAdd - rejected;
            if (accepted <= 0) break; // chest full — player keeps everything

            totalDeposited += accepted;

            final ItemStack liveSlotAfter = player.getInventory().getItem(slot);
            if (liveSlotAfter == null || liveSlotAfter.getType() != targetMat) {
                DepositPlugin.warn("depositWholeStack: slot " + slot + " for "
                        + player.getName() + " changed type during addItem(). "
                        + "Stopping deposit loop to prevent item corruption.");
                break;
            }

            final int currentAmount = liveSlotAfter.getAmount();
            if (accepted >= currentAmount) {
                player.getInventory().setItem(slot, null);
            } else {

                liveSlotAfter.setAmount(currentAmount - accepted);
                player.getInventory().setItem(slot, liveSlotAfter);
                break; // chest is now full
            }
        }

        if (totalDeposited > 0) {
            sendDepositMessage(player, totalDeposited, targetMat, depositType, color);
            DepositPlugin.info(player.getName() + " deposited "
                    + totalDeposited + "x " + targetMat
                    + " → " + depositType.name());
        } else {
            player.sendMessage(ChatColor.RED + "That chest is full!");
        }
    }

    public static void handleSetupSession(Player player, Block block) {
        assertMainThread("handleSetupSession()");

        final String serialized = serializeLocation(block.getLocation());
        final String path       = "worlds." + player.getWorld().getName() + ".chestLocations";

        final List<String> locations = new ArrayList<>(ArenaConfig.get().getStringList(path));

        if (locations.contains(serialized)) {
            player.sendMessage(ChatColor.YELLOW + "This chest is already registered!");
            SelectionUtils.removePlayerFromSelectionMode(player);
            return;
        }

        locations.add(serialized);
        ArenaConfig.get().set(path, locations);
        ArenaConfig.save();

        player.sendMessage(ChatColor.GREEN + "Chest registered at: " + serialized);
        updateSetupSessionMessage(player, locations.size());
        player.playSound(player.getLocation(),
                XSound.BLOCK_NOTE_BLOCK_HAT.parseSound(), 1f, 1f);

        final String label = block.getType() == Material.ENDER_CHEST
                ? ChatColor.DARK_PURPLE + "Ender Chest" + ChatColor.BOLD + " Deposit Set"
                : ChatColor.AQUA        + "Team Chest"  + ChatColor.BOLD + " Deposit Set";
        HologramUtils.createCustomHologram(block.getLocation(), label);

        SelectionUtils.removePlayerFromSelectionMode(player);
    }

    /**
     * Sends the admin a status line showing chest count vs expected for their
     * arena configuration.
     */
    public static void updateSetupSessionMessage(Player player, int chestCount) {
        final ISetupSession session = DepositPlugin.bedWars.getSetupSession(player.getUniqueId());
        if (session == null) return;

        final int maxInTeam = session.getConfig().getInt("maxInTeam");
        final int expected  = (maxInTeam == 1 || maxInTeam == 2) ? 16 : 8;

        final String status;
        if      (chestCount == 0)        status = "&c&l(NOT SET)";
        else if (chestCount < expected)  status = "&e&l(INCOMPLETE — " + chestCount + "/" + expected + ")";
        else if (chestCount == expected) status = "&a&l(SET)";
        else                             status = "&6&l(OVER — " + chestCount + "/" + expected + ")";

        player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                "§6 ▪ §7ChestLocations: " + status + " §8- §eShift + Left-Click to register"));
    }

    public static void setChestLocationsAll() {
        assertMainThread("setChestLocationsAll()");

        DepositPlugin.debug("Scanning chest locations for all arenas...");
        int processed = 0;

        for (IArena arena : DepositPlugin.bedWars.getArenaUtil().getArenas()) {
            final World world = arena.getWorld();
            if (world == null) {
                DepositPlugin.warn("Null world for arena '" + arena.getWorldName() + "' — skipped.");
                continue;
            }
            // FIX: pass shouldSave=false — accumulate in memory only.
            setChestLocations(world, false);
            processed++;
        }

        ArenaConfig.save();
        DepositPlugin.info("Finished scanning " + processed + " arena world(s). Saved once.");
    }
    public static void setChestLocations(World world) {
        setChestLocations(world, true);
    }
    private static void setChestLocations(World world, boolean shouldSave) {
        assertMainThread("setChestLocations()");

        final String path = "worlds." + world.getName() + ".chestLocations";
        if (ArenaConfig.get().contains(path)) {
            DepositPlugin.debug("Already indexed: " + world.getName());
            return;
        }

        //a Hi from 1xMohameeD :) do you enjoy reading the code

        DepositPlugin.debug("Indexing chests in world: " + world.getName());

        final Material chestMat      = XMaterial.CHEST.parseMaterial();
        final Material enderChestMat = XMaterial.ENDER_CHEST.parseMaterial();
        final List<String> locations = new ArrayList<>(64);

        for (Chunk chunk : world.getLoadedChunks()) {
            for (BlockState state : chunk.getTileEntities()) {
                final Material type = state.getType();
                if (type == chestMat || type == enderChestMat) {
                    locations.add(serializeLocation(state.getLocation()));
                }
            }
        }

        ArenaConfig.get().set(path, locations);
        if (shouldSave) {
            ArenaConfig.save();
        }
        DepositPlugin.debug("Indexed " + locations.size() + " chest(s) in: " + world.getName());
    }

    private static Inventory resolveInventory(Player player, Block block, boolean isEnderChest) {
        if (isEnderChest) return player.getEnderChest();

        final BlockState state = block.getState();
        if (state instanceof Chest) {
            return ((Chest) state).getInventory();
        }

        DepositPlugin.warn("Block at " + serializeLocation(block.getLocation())
                + " is flagged as CHEST but BlockState is "
                + state.getClass().getSimpleName());
        return null;
    }

    private static boolean inventoryConsistent(ItemStack snapshot, ItemStack handNow) {
        if (handNow == null || handNow.getType() == Material.AIR) return false;
        return snapshot.getType()   == handNow.getType()
                && snapshot.getAmount() == handNow.getAmount();
    }

    private static String describeItem(ItemStack item) {
        if (item == null) return "null";
        return item.getAmount() + "x" + item.getType().name();
    }

    private static ChatColor resolveItemColor(Material material) {
        return ITEM_COLOR_MAP.getOrDefault(material, ChatColor.WHITE);
    }

    private static void sendDepositMessage(
            Player player,
            int amount,
            Material material,
            DepositType depositType,
            ChatColor color) {

        final String key = depositType == DepositType.ENDER_CHEST
                ? "player_deposit_ender_chest"
                : "player_deposit_chest";

        final String template = getCachedTemplate(key);
        if (template == null || template.isEmpty()) {
            DepositPlugin.warn("Missing message config key: " + key);
            return;
        }

        final String message = template
                .replace("%amount%",   String.valueOf(amount))
                .replace("%color%",    color.toString())
                .replace("%material%", TextUtils.formatItemName(material));

        player.sendMessage(message);
        player.playSound(player.getLocation(),
                XSound.BLOCK_CHEST_CLOSE.parseSound(), 1.0f, 1.0f);
    }

    @SuppressWarnings("deprecation")
    private static void clearHand(Player player) {
        final ItemStack air = new ItemStack(Material.AIR);
        if (SET_ITEM_IN_MAIN_HAND != null) {
            try {
                SET_ITEM_IN_MAIN_HAND.invoke(player.getInventory(), air);
                return;
            } catch (Exception ignored) {
                // Fallback on reflection failure.
            }
        }
        player.getInventory().setItemInHand(air);
    }

    @SuppressWarnings("deprecation")
    private static void setHandItem(Player player, ItemStack item) {
        if (SET_ITEM_IN_MAIN_HAND != null) {
            try {
                SET_ITEM_IN_MAIN_HAND.invoke(player.getInventory(), item);
                return;
            } catch (Exception ignored) {
                // Fallback on reflection failure.
            }
        }
        player.getInventory().setItemInHand(item);
    }

    private static void assertMainThread(String methodName) {
        if (Bukkit.isPrimaryThread()) return;

        final StringWriter sw = new StringWriter();
        final IllegalStateException ex = new IllegalStateException(
                "THREADING VIOLATION — " + methodName + " called off main thread!");
        ex.printStackTrace(new PrintWriter(sw));

        // Log first so the full stack trace reaches the console even if the
        // caller catches the exception.
        DepositPlugin.error(sw.toString());

        // Then throw  a silent log-only assert means execution continues on
        // the wrong thread which is exactly the bug we are preventing.
        throw ex;
    }
}
