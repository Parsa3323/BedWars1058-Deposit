package com.parsa3323.deposit.api.Events;

import com.parsa3323.deposit.api.Types.DepositType;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class PlayerDepositEvent extends Event {

    private static final HandlerList handlers = new HandlerList();

    private boolean cancelled;

    Player player;

    DepositType depositType;

    Block block;

    @Deprecated
    public Block getBlock() {
        return block;
    }

    public PlayerDepositEvent(Player player, DepositType depositType, Block block) {
        this.player = player;
        this.depositType = depositType;
        this.block = block;
    }

    public DepositType getDepositType() {
        return depositType;
    }

    public Player getPlayer() {
        return player;
    }

    public boolean isCancelled() {
        return cancelled;
    }

    public void setCancelled(boolean cancel) {
        cancelled = cancel;
    }

    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;

    }
}
