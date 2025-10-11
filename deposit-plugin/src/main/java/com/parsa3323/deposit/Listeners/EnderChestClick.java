package com.parsa3323.deposit.Listeners;

import com.andrei1058.bedwars.api.BedWars;
import com.andrei1058.bedwars.api.arena.IArena;
import com.andrei1058.bedwars.api.arena.team.ITeam;
import com.andrei1058.bedwars.api.configuration.ConfigPath;
import com.andrei1058.bedwars.api.language.Messages;
import com.parsa3323.deposit.api.Events.PlayerDepositEvent;
import com.parsa3323.deposit.api.Types.DepositType;
import com.parsa3323.deposit.Configs.MessagesConfig;
import com.parsa3323.deposit.DepositPlugin;
import com.parsa3323.deposit.utils.DepositUtils;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.HashSet;
import java.util.UUID;

import static com.andrei1058.bedwars.api.language.Language.getMsg;

public class EnderChestClick implements Listener {

    private static final HashSet<UUID> selectionModePlayers = new HashSet<>();

    public static void addPlayerToSelectionMode(Player player) {
        selectionModePlayers.add(player.getUniqueId());
    }

    public static final HashSet<Material> BLOCKED_ITEMS = new HashSet<>(Arrays.asList(
            Material.WOOD_SWORD, Material.IRON_SWORD, Material.DIAMOND_SWORD, Material.STONE_SWORD,
            Material.COMPASS, Material.WOOD_PICKAXE, Material.STONE_PICKAXE, Material.IRON_PICKAXE,
            Material.DIAMOND_PICKAXE, Material.GOLD_PICKAXE, Material.WOOD_AXE, Material.STONE_AXE,
            Material.IRON_AXE, Material.DIAMOND_AXE, Material.GOLD_AXE, Material.SHEARS
    ));

    public static void removePlayerFromSelectionMode(Player player) {
        selectionModePlayers.remove(player.getUniqueId());
    }

    private void deleteHologram(Location chestLocation) {

        Location baseLocation = chestLocation.clone().add(0.5, 0.9, 0.5);

        for (Entity entity : chestLocation.getWorld().getEntities()) {
            if (entity instanceof ArmorStand) {
                ArmorStand hologram = (ArmorStand) entity;

                if (hologram.getLocation().distance(baseLocation) < 1) {
                    hologram.remove();
                }
            }
        }
    }

    private boolean isDepositItem(ItemStack item) {
        return item != null && item.getType() != Material.AIR && !BLOCKED_ITEMS.contains(item.getType());
    }

    @EventHandler
    public void onPlayerLeftClickEnderChest(PlayerInteractEvent e) {
        if (e.getAction() != Action.LEFT_CLICK_BLOCK) return;

        Player p = e.getPlayer();
        Block block = e.getClickedBlock();
        if (block == null) return;

        if (DepositPlugin.bedWars.isInSetupSession(p.getUniqueId()) && p.isSneaking()) {
            DepositUtils.handleSetupChestSelection(p, block);
            e.setCancelled(true);
            return;
        }

        BedWars bedWars =  DepositPlugin.bedWars;
        if (!bedWars.getArenaUtil().isPlaying(p)) return;
        if (!isDepositItem(p.getItemInHand())) return;

        if (block.getType() == Material.ENDER_CHEST) {
            PlayerDepositEvent event =  new PlayerDepositEvent(p, DepositType.ENDER_CHEST, block);
            Bukkit.getPluginManager().callEvent(event);
            if (!event.isCancelled()) {
                DepositUtils.depositItems(p, p.getEnderChest(), p.getItemInHand().getType(), MessagesConfig.get().getString("player_deposit_ender_chest"));
            }
            e.setCancelled(true);


        } else if (block.getType() == Material.CHEST) {
            Chest chest = (Chest) block.getState();
            PlayerDepositEvent event =  new PlayerDepositEvent(p, DepositType.CHEST, block);
            Bukkit.getPluginManager().callEvent(event);
            if (!event.isCancelled()) {
                DepositUtils.depositItems(p, chest.getInventory(), p.getItemInHand().getType(),  MessagesConfig.get().getString("player_deposit_chest"));
            }
            e.setCancelled(true);

        }

    }

    @EventHandler
    public void enderChestClick(PlayerDepositEvent event) {
        if (event.getDepositType() == DepositType.CHEST) {
            Player p = event.getPlayer();
            DepositPlugin.debug("Checking deposit event for player: " + p.getName());

            IArena a = DepositPlugin.bedWars.getArenaUtil().getArenaByPlayer(p);
            if (a == null) {
                DepositPlugin.debug("Arena is null for player: " + p.getName());
                return;
            }

            ITeam owner = null;
            int isRad = a.getConfig().getInt(ConfigPath.ARENA_ISLAND_RADIUS);
            DepositPlugin.debug("Arena radius: " + isRad);

            Block block = event.getBlock();
            if (block == null) {
                DepositPlugin.debug("Block is null for player: " + p.getName());
                return;
            }

            Location blockLoc = block.getLocation();
            DepositPlugin.debug("Block location: " + blockLoc);

            for (ITeam t : a.getTeams()) {
                if (t == null || t.getSpawn() == null) {
                    DepositPlugin.debug("Skipping team due to null spawn.");
                    continue;
                }
                DepositPlugin.debug("Checking team: " + t.getName() + " with spawn at " + t.getSpawn());

                if (t.getSpawn().distance(blockLoc) <= isRad) {
                    owner = t;
                    DepositPlugin.debug("Found owner: " + t.getName());
                }
            }

            if (owner != null) {
                if (!owner.isMember(p)) {
                    DepositPlugin.debug("Player " + p.getName() + " is NOT a member of " + owner.getName());

                    if (!(owner.getMembers().isEmpty() && owner.isBedDestroyed())) {
                        DepositPlugin.debug("Team " + owner.getName() + " is still in the game. Cancelling event.");
                        event.setCancelled(true);
                        p.sendMessage(getMsg(p, Messages.INTERACT_CHEST_CANT_OPEN_TEAM_ELIMINATED));
                    } else {
                        DepositPlugin.debug("Team " + owner.getName() + " is eliminated, allowing chest interaction.");
                    }
                } else {
                    DepositPlugin.debug("Player " + p.getName() + " is a member of " + owner.getName() + ", allowing access.");
                }
            } else {
                DepositPlugin.debug("No team owns this chest.");
            }

        }

    }

}
