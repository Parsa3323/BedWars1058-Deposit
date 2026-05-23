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
                configContent.append("# BedWars1058 - Deposit addon\n");
                configContent.append("# Github: https://github.com/Parsa3323/BedWars1058-Deposit \n\n");


                configContent.append("# Log level:\n");
                configContent.append("# Determines the level of logging that will be shown in the console.\n");
                configContent.append("# Available options:\n");
                configContent.append("# SEVERE   - Shows only critical errors.\n");
                configContent.append("# WARNING  - Displays warnings and serious issues.\n");
                configContent.append("# INFO     - Standard logging (recommended).\n");
                configContent.append("# CONFIG   - Shows additional configuration details.\n");
                configContent.append("# FINE     - Provides debugging information (debug mode).\n");
                configContent.append("# FINER    - Even more detailed debugging logs.\n");
                configContent.append("# FINEST   - Maximum debugging details (may spam the console).\n");
                configContent.append("log-level: INFO\n\n");

                configContent.append("# Determines when the deposit hologram should be registered in the game.\n");
                configContent.append("hologram-register-event: GameStateChangeEvent\n\n");

                configContent.append("# If enabled, depositing an item will move all matching item stacks\n");
                configContent.append("# from the player's inventory to the Ender Chest or Chest.\n");
                configContent.append("deposit-whole-itemstack: false\n\n");

                configContent.append("# If enabled, while in BedWars1058 setup mode,\n");
                configContent.append("# players can shift-click on an Ender Chest or Chest to register it\n");
                configContent.append("# as a valid deposit chest for holograms.\n");
                configContent.append("shift-click-on-chest-to-set: true\n\n");

                configContent.append("# If enabled, all chest locations will be saved when a player joins the server. (may lag the server for the first time)\n");
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