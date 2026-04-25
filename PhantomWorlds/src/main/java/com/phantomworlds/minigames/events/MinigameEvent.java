package com.phantomworlds.minigames.events;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Base event for all minigame events
 */
public abstract class MinigameEvent extends Event {
    
    private static final HandlerList handlers = new HandlerList();
    
    private final String instanceId;
    private final String gameType;
    
    public MinigameEvent(String instanceId, String gameType) {
        this.instanceId = instanceId;
        this.gameType = gameType;
    }
    
    public String getInstanceId() {
        return instanceId;
    }
    
    public String getGameType() {
        return gameType;
    }
    
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
    
    public static HandlerList getHandlerList() {
        return handlers;
    }
}
