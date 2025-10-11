package com.parsa3323.deposit.Listeners;

import com.parsa3323.deposit.Configs.ArenasConfig;
import com.parsa3323.deposit.DepositPlugin;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scheduler.BukkitRunnable;

public class PlayerJoin implements Listener {
    @EventHandler
    public void playerJoinEvent(PlayerJoinEvent event) {
        if (DepositPlugin.plugin.configuration.getBoolean("set-chest-locations-on-join")) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    DepositPlugin.debug("WorldLoadEvent ran");
                    new GameStartListener(DepositPlugin.plugin, ArenasConfig.get()).createHDLocations();
                    DepositPlugin.debug("WorldLoadEvent done");
                }
            }.runTaskAsynchronously(DepositPlugin.plugin);
        }
    }
}