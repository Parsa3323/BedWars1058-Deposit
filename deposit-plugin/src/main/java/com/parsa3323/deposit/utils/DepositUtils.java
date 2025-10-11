package com.parsa3323.deposit.utils;

import com.andrei1058.bedwars.api.server.ISetupSession;
import com.cryptomorin.xseries.XSound;
import com.parsa3323.deposit.Configs.ArenasConfig;
import com.parsa3323.deposit.DepositPlugin;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class DepositUtils {
    public static void handleSetupChestSelection(Player player, Block block) {
        String loc = block.getX() + "," + block.getY() + "," + block.getZ();
        String path = "worlds." + player.getWorld().getName() + ".chestLocations";
        List<String> chestLocations = ArenasConfig.get().getStringList(path);

        if (chestLocations.contains(loc)) {
            player.sendMessage(ChatColor.YELLOW + "This chest is already set!");
            return;
        }

        chestLocations.add(loc);
        ArenasConfig.get().set(path, chestLocations);
        ArenasConfig.save();

        createHologram(block.getLocation(),
                (block.getType() == Material.ENDER_CHEST ? ChatColor.DARK_PURPLE + "Ender Chest" : ChatColor.AQUA + "Team Chest")
                        + ChatColor.BOLD + " Deposit Set");

        player.sendMessage(ChatColor.GREEN + "Chest location added: " + loc);

        ISetupSession setupSession = DepositPlugin.bedWars.getSetupSession(player.getUniqueId());
        int size = chestLocations.size();
        String the = null;

        int maxInTeam = setupSession.getConfig().getInt("maxInTeam");
        if (maxInTeam == 1 || maxInTeam == 2) {
            the = "§6 ▪ §7ChestLocations: " + ((size == 0) ? "&c&l(NOT SET) "
                    : (size < 16) ? "&e&l(NOT PROPERLY SET) "
                    : (size == 16) ? "&a&l(SET) " : "&c&l(NOT SET) ")
                    + "§8 - §eShift + Left-Click ";
        } else if (maxInTeam == 3 || maxInTeam == 4) {
            the = "§6 ▪ §7ChestLocations: " + ((size == 0) ? "&c&l(NOT SET) "
                    : (size < 8) ? "&e&l(NOT PROPERLY SET) "
                    : (size == 8) ? "&a&l(SET) " : "&c&l(NOT SET) ")
                    + "§8 - §eShift + Left-Click ";
        }

        if (the != null) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', the));
        }

        player.playSound(player.getLocation(), XSound.BLOCK_NOTE_BLOCK_HAT.parseSound(), 1, 1);
    }

    public static void createHologram(Location chestLocation, String ... lines) {
        Location baseLocation = chestLocation.clone().add(0.5, 0.9, 0.5);

        for (int i = 0; i < lines.length; i++) {
            Location hologramLocation = baseLocation.clone().add(0, 0.3 * i, 0);
            ArmorStand hologram = (ArmorStand) chestLocation.getWorld().spawnEntity(hologramLocation, EntityType.ARMOR_STAND);

            hologram.setVisible(false);
            hologram.setMarker(true);
            hologram.setCustomName(lines[i]);
            hologram.setCustomNameVisible(true);
            hologram.setGravity(false);
        }
    }

    public static void sendDepositMessage(Player p, int amount, Material mat, String fullMessage) {
        if (amount <= 0) {
            p.sendMessage(ChatColor.RED + "You don't have any " + mat + " to deposit!");
            return;
        }
        String itemName = Arrays.stream(mat.name().toLowerCase().split("_"))
                .map(s -> Character.toUpperCase(s.charAt(0)) + s.substring(1))
                .collect(Collectors.joining(" "));
        ChatColor color = (mat == Material.GOLDEN_APPLE || mat == Material.GOLD_INGOT) ? ChatColor.GOLD : ChatColor.WHITE;

        p.sendMessage(
                ChatColor.translateAlternateColorCodes('&',
                        fullMessage
                                .replace("%amount%", String.valueOf(amount))
                                .replace("%color%", color.toString())
                                .replace("%material%", itemName)
                )
        );
        p.playSound(p.getLocation(), XSound.BLOCK_CHEST_CLOSE.parseSound(), 1, 1);
    }


    public static void depositItems(Player p, Inventory target, Material itemMat, String fullMessage) {
        boolean wholeStack = DepositPlugin.plugin.configuration.getBoolean("deposit-whole-itemstack");
        if (wholeStack) {
            int total = 0;
            for (ItemStack itemStack : p.getInventory().getContents()) {
                if (itemStack != null && itemStack.getType() == itemMat) {
                    total += itemStack.getAmount();
                    p.getInventory().remove(itemStack);
                    target.addItem(itemStack);
                }
            }
            sendDepositMessage(p, total, itemMat, fullMessage);
        } else {
            ItemStack inHand = p.getItemInHand();
            int amount = inHand.getAmount();
            target.addItem(inHand);
            p.setItemInHand(null);
            sendDepositMessage(p, amount, itemMat, fullMessage);
        }
    }


}
