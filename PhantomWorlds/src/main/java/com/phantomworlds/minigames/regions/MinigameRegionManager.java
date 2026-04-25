package com.phantomworlds.minigames.regions;

import com.phantomworlds.models.templates.TemplateRegion;
import org.bukkit.Location;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages minigame regions and handles region events
 */
public class MinigameRegionManager {
    
    private final Map<String, Map<MinigameRegionType, TemplateRegion>> instanceRegions;
    private final Map<String, Map<String, MinigameRegionType>> playerCurrentRegions;
    
    public MinigameRegionManager() {
        this.instanceRegions = new ConcurrentHashMap<>();
        this.playerCurrentRegions = new ConcurrentHashMap<>();
    }
    
    /**
     * Register regions for an instance
     */
    public void registerInstanceRegions(String instanceId, Map<MinigameRegionType, TemplateRegion> regions) {
        instanceRegions.put(instanceId, regions);
    }
    
    /**
     * Get all regions for an instance
     */
    public Map<MinigameRegionType, TemplateRegion> getInstanceRegions(String instanceId) {
        return instanceRegions.getOrDefault(instanceId, new HashMap<>());
    }
    
    /**
     * Get a specific region type for an instance
     */
    public TemplateRegion getRegion(String instanceId, MinigameRegionType regionType) {
        Map<MinigameRegionType, TemplateRegion> regions = instanceRegions.get(instanceId);
        return regions != null ? regions.get(regionType) : null;
    }
    
    /**
     * Check if a location is in a specific region type
     */
    public boolean isInRegion(String instanceId, Location location, MinigameRegionType regionType) {
        TemplateRegion region = getRegion(instanceId, regionType);
        return region != null && region.contains(location);
    }
    
    /**
     * Get the region type at a location
     */
    public MinigameRegionType getRegionAt(String instanceId, Location location) {
        Map<MinigameRegionType, TemplateRegion> regions = instanceRegions.get(instanceId);
        if (regions == null) return null;
        
        for (Map.Entry<MinigameRegionType, TemplateRegion> entry : regions.entrySet()) {
            if (entry.getValue().contains(location)) {
                return entry.getKey();
            }
        }
        return null;
    }
    
    /**
     * Handle player movement and region changes
     */
    public void handlePlayerMove(String instanceId, String playerId, Location location) {
        MinigameRegionType currentRegion = getRegionAt(instanceId, location);
        MinigameRegionType previousRegion = getCurrentRegion(instanceId, playerId);
        
        // Update current region tracking
        setCurrentRegion(instanceId, playerId, currentRegion);
        
        // Trigger region events
        if (previousRegion != currentRegion) {
            if (previousRegion != null && previousRegion.triggersEvents()) {
                onRegionLeave(instanceId, playerId, previousRegion);
            }
            if (currentRegion != null && currentRegion.triggersEvents()) {
                onRegionEnter(instanceId, playerId, currentRegion);
            }
        }
    }
    
    /**
     * Get a player's current region
     */
    public MinigameRegionType getCurrentRegion(String instanceId, String playerId) {
        Map<String, MinigameRegionType> instancePlayerRegions = playerCurrentRegions.get(instanceId);
        return instancePlayerRegions != null ? instancePlayerRegions.get(playerId) : null;
    }
    
    /**
     * Set a player's current region
     */
    private void setCurrentRegion(String instanceId, String playerId, MinigameRegionType regionType) {
        playerCurrentRegions.computeIfAbsent(instanceId, k -> new ConcurrentHashMap<>())
            .put(playerId, regionType);
    }
    
    /**
     * Remove player from region tracking
     */
    public void removePlayer(String instanceId, String playerId) {
        Map<String, MinigameRegionType> instancePlayerRegions = playerCurrentRegions.get(instanceId);
        if (instancePlayerRegions != null) {
            instancePlayerRegions.remove(playerId);
        }
    }
    
    /**
     * Clean up instance data
     */
    public void cleanupInstance(String instanceId) {
        instanceRegions.remove(instanceId);
        playerCurrentRegions.remove(instanceId);
    }
    
    /**
     * Region event handlers - to be implemented by game types
     */
    public void onRegionEnter(String instanceId, String playerId, MinigameRegionType regionType) {
        // This will be handled by the specific game type implementation
    }
    
    public void onRegionLeave(String instanceId, String playerId, MinigameRegionType regionType) {
        // This will be handled by the specific game type implementation
    }
    
    /**
     * Get spawn location for a team or player
     */
    public Location getSpawnLocation(String instanceId, String spawnType, org.bukkit.World world) {
        Map<MinigameRegionType, TemplateRegion> regions = instanceRegions.get(instanceId);
        if (regions == null) return null;
        
        // Check for team spawn regions first
        for (Map.Entry<MinigameRegionType, TemplateRegion> entry : regions.entrySet()) {
            if (entry.getKey() == MinigameRegionType.TEAM_SPAWN) {
                TemplateRegion region = entry.getValue();
                // Check if this region matches the spawn type
                if (region.getRegionId().equalsIgnoreCase(spawnType)) {
                    return region.getCenter(world);
                }
            }
        }
        
        // Fallback to any spawn region
        TemplateRegion spawnRegion = regions.get(MinigameRegionType.TEAM_SPAWN);
        return spawnRegion != null ? spawnRegion.getCenter(world) : null;
    }
}
