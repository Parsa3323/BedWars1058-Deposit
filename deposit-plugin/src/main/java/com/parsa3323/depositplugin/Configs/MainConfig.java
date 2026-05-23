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

package com.parsa3323.depositplugin.Configs;

import com.parsa3323.depositplugin.DepositPlugin;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

public class MainConfig {

    private static File file;
    private static FileConfiguration fileConfiguration;

    public static void init() {
        file = new File(DepositPlugin.bedWars.getAddonsPath(), "Deposit/config.yml");

        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }

        if (!file.exists()) {
            try {
                StringBuilder configContent = new StringBuilder();
                configContent.append("# ──────────────────────────────────────────────────────────────\n");
                configContent.append("#   Punch to deposit - By Parsa3323\n");
                configContent.append("# ──────────────────────────────────────────────────────────────\n");
                configContent.append("#   This configuration file controls various aspects of the addon.\n");
                configContent.append("#   Make sure to read the comments carefully before changing any settings.\n");
                configContent.append("# ──────────────────────────────────────────────────────────────\n\n");

                configContent.append("# LOG LEVEL:\n");
                configContent.append("# Determines the level of logging that will be shown in the console.\n");
                configContent.append("# Available options:\n");
                configContent.append("# - SEVERE   → Shows only critical errors.\n");
                configContent.append("# - WARNING  → Displays warnings and serious issues.\n");
                configContent.append("# - INFO     → Standard logging (recommended for most cases).\n");
                configContent.append("# - CONFIG   → Shows additional configuration details.\n");
                configContent.append("# - FINE     → Provides debugging information (useful for developers).\n");
                configContent.append("# - FINER    → Even more detailed debugging logs.\n");
                configContent.append("# - FINEST   → Maximum debugging details (may spam the console).\n");
                configContent.append("# Default is INFO. Change only if needed for debugging purposes.\n");
                configContent.append("log-level: INFO\n\n");

                configContent.append("# ──────────────────────────────────────────────────────────────\n\n");

                configContent.append("# DISABLE HOLOGRAM AFTER DEATH:\n");
                configContent.append("# If enabled (true), the deposit hologram will be removed after the player dies.\n");
                configContent.append("disable-hologram-after-death: false\n\n");

                configContent.append("# ──────────────────────────────────────────────────────────────\n\n");

                configContent.append("# HOLOGRAM REGISTER EVENT:\n");
                configContent.append("# Determines when the deposit hologram should be registered in the game.\n");
                configContent.append("hologram-register-event: GameStateChangeEvent\n\n");

                configContent.append("# ──────────────────────────────────────────────────────────────\n\n");

                configContent.append("# DEPOSIT WHOLE ITEMSTACK:\n");
                configContent.append("# If enabled (true), depositing an item will move all matching item stacks\n");
                configContent.append("# (same type as the item in hand) from the player's inventory to the Ender Chest.\n");
                configContent.append("deposit-whole-itemstack: false\n\n");

                configContent.append("# ──────────────────────────────────────────────────────────────\n\n");

                configContent.append("# SHIFT-CLICK ON CHEST TO SET:\n");
                configContent.append("# If enabled (true), while in BedWars1058 setup mode,\n");
                configContent.append("# players can shift-click on an Ender Chest or Chest to register it\n");
                configContent.append("# as a valid deposit chest for holograms.\n");
                configContent.append("shift-click-on-chest-to-set: true\n\n");

                configContent.append("# ──────────────────────────────────────────────────────────────\n\n");

                configContent.append("# SET CHEST LOCATIONS ON PLAYER JOIN:\n");
                configContent.append("# If enabled (true), all chest locations will be saved when a player joins the server.\n");
                configContent.append("set-chest-locations-on-join: true\n");

                Files.write(file.toPath(), configContent.toString().getBytes(StandardCharsets.UTF_8));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        fileConfiguration = YamlConfiguration.loadConfiguration(file);
    }

    public static FileConfiguration get() {
        return fileConfiguration;
    }

    public static void save() {
        try {
            fileConfiguration.save(file);
        } catch (IOException e) {
            e.printStackTrace();
            DepositPlugin.error("Error while saving : " + e.getMessage());
        }
    }

    public static void reload() {
        fileConfiguration = YamlConfiguration.loadConfiguration(file);
    }
}