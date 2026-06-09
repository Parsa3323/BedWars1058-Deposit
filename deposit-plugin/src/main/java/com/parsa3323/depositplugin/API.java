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

package com.parsa3323.depositplugin;

import com.parsa3323.depositapi.DepositApi;
import com.parsa3323.depositplugin.Configs.ArenaConfig;
import com.parsa3323.depositplugin.Configs.MainConfig;
import com.parsa3323.depositplugin.Listeners.GameStartListener;
import com.parsa3323.depositplugin.utils.DepositUtils;
import com.parsa3323.depositplugin.utils.HologramUtils;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.List;

public class API implements DepositApi {

    private final GameStartListener gameStartListener;

    public API(GameStartListener gameStartListener) {
        this.gameStartListener = gameStartListener;
    }

    private final ConfigManager configManager = new ConfigManager() {

        @Override
        public List<String> getArenaChests(World arena) {
            return DepositUtils.getArenaChests(arena);
        }

        @Override
        public FileConfiguration getArenaConfig() {
            return ArenaConfig.get();
        }
    };

    private final HologramUtil hologramUtil = new HologramUtil() {

        @Override
        public void reloadHolograms(Player player) {
            if (player == null) return;
            World world = player.getWorld();
            HologramUtils.deleteHolograms(world);
            DepositUtils.setChestLocations(world);
            if (MainConfig.get().getBoolean("deposit-holograms")) {
                HologramUtils.spawnDepositHolograms(world);
            }
        }

        @Override
        public void deleteHolograms(World world) {
            HologramUtils.deleteHolograms(world);
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
