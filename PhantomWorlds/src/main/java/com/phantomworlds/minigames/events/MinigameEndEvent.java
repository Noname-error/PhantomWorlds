package com.phantomworlds.minigames.events;

import java.util.List;

/**
 * Event fired when a minigame ends
 */
public class MinigameEndEvent extends MinigameEvent {
    
    private final List<String> winners;
    private final String endReason;
    
    public MinigameEndEvent(String instanceId, String gameType, List<String> winners, String endReason) {
        super(instanceId, gameType);
        this.winners = winners;
        this.endReason = endReason;
    }
    
    public List<String> getWinners() {
        return winners;
    }
    
    public String getEndReason() {
        return endReason;
    }
}
