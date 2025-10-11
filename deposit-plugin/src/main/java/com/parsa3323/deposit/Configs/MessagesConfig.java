package com.parsa3323.deposit.Configs;

import com.parsa3323.deposit.DepositPlugin;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;

public class MessagesConfig {
    private static File file;

    private static FileConfiguration fileConfiguration;

    public static void init() {
        file = new File(DepositPlugin.bedWars.getAddonsPath(), "Deposit/messages.yml");

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
            System.out.println("Error while saving : " + e.getMessage());
        }

    }

    public static void reload(){
        fileConfiguration = YamlConfiguration.loadConfiguration(file);
    }

}
