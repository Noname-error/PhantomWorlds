package com.phantomworlds.minigames.events;

/**
 * Event fired when a minigame starts
 */
public class MinigameStartEvent extends MinigameEvent {
    
    public MinigameStartEvent(String instanceId, String gameType) {
        super(instanceId, gameType);
    }
}
