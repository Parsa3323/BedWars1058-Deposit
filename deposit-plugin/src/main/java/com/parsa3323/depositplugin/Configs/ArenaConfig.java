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

public class ArenaConfig {
    private static File file;
    private static FileConfiguration fileConfiguration;

    public static void init() {
        File addonsPath = DepositPlugin.bedWars.getAddonsPath();
        File depositFolder = new File(addonsPath, "Deposit");
        File oldFile = new File(depositFolder, "chestLocations.yml");
        File newFile = new File(depositFolder, "data/chestlocations.yml");

        if (oldFile.exists()) {
            if (!newFile.getParentFile().exists()) {
                newFile.getParentFile().mkdirs();
            }

            if (!newFile.exists()) {
                boolean success = oldFile.renameTo(newFile);
                if (success) {
                    DepositPlugin.info("Migrated old chestLocations.yml to data/chestlocations.yml");
                } else {
                    DepositPlugin.error("Failed to migrate chestLocations.yml, moving manually may be required.");
                }
            } else {
                File backup = new File(depositFolder, "chestLocations.yml.old");
                oldFile.renameTo(backup);
                DepositPlugin.warn("chestlocations.yml already exists. Old file backed up as chestLocations.yml.old");
            }
        }

        file = newFile;

        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }

        if (!file.exists()) {
            try {
                file.createNewFile();
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
            DepositPlugin.error("Error while saving: " + e.getMessage());
        }
    }

    public static void reload() {
        fileConfiguration = YamlConfiguration.loadConfiguration(file);
    }
}