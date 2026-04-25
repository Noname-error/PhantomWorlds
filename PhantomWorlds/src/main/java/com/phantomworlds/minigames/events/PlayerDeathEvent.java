package com.phantomworlds.minigames.events;

import org.bukkit.entity.Player;

/**
 * Event fired when a player dies in a minigame
 */
public class PlayerDeathEvent extends MinigameEvent {
    
    private final Player player;
    private final String killer;
    
    public PlayerDeathEvent(String instanceId, String gameType, Player player, String killer) {
        super(instanceId, gameType);
        this.player = player;
        this.killer = killer;
    }
    
    public Player getPlayer() {
        return player;
    }
    
    public String getKiller() {
        return killer;
    }
    
    public boolean hasKiller() {
        return killer != null;
    }
}
