package com.phantomworlds.minigames.events;

import com.phantomworlds.minigames.regions.MinigameRegionType;
import org.bukkit.entity.Player;

/**
 * Event fired when a player enters a minigame region
 */
public class RegionEnterEvent extends MinigameEvent {
    
    private final Player player;
    private final MinigameRegionType regionType;
    
    public RegionEnterEvent(String instanceId, String gameType, Player player, MinigameRegionType regionType) {
        super(instanceId, gameType);
        this.player = player;
        this.regionType = regionType;
    }
    
    public Player getPlayer() {
        return player;
    }
    
    public MinigameRegionType getRegionType() {
        return regionType;
    }
}
