package com.parsa3323.deposit;

import com.parsa3323.deposit.api.DepositApi;
import com.parsa3323.deposit.Configs.ArenasConfig;
import com.parsa3323.deposit.Listeners.GameStartListener;
import com.parsa3323.deposit.Manager.MainConfig;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.List;

public class API implements DepositApi {

    ConfigManager configManager = new ConfigManager() {
        @Override
        public List<String> getArenaChests(World arena) {
            return MainConfig.getArenaChests(arena);
        }

        @Override
        public FileConfiguration getArenasConfig() {
            return ArenasConfig.get();
        }

    };

    HologramUtil hologramUtil = new HologramUtil() {
        @Override
        public void reloadHolograms(Player player) {
            new GameStartListener(DepositPlugin.plugin, ArenasConfig.get()).createHolograms(player);
        }

        @Override
        public boolean doesHologramsWorked() {
            GameStartListener gameStartListener = new GameStartListener(DepositPlugin.plugin, ArenasConfig.get());
            return gameStartListener.succesGameState && gameStartListener.successGameAssgin || gameStartListener.isReloaded;
        }

        @Override
        public void deleteHolograms(Player player) {
            GameStartListener gameStartListener = new GameStartListener(DepositPlugin.plugin, ArenasConfig.get());
            DepositPlugin.debug("Trying to delete hologram for " + player.getName());
            gameStartListener.deleteHolograms(player);
        }
    };

    @Override
    public ConfigManager getConfigManager() {
        return configManager;
    }

    @Override
    public HologramUtil getHologramUtil() {
        return hologramUtil;
    }

}