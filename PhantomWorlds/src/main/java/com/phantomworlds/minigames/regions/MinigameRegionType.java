package com.phantomworlds.minigames.regions;

/**
 * Region types for minigames
 */
public enum MinigameRegionType {
    LOBBY("Lobby", "Waiting area before game starts"),
    GAME("Game", "Main gameplay area"),
    SPECTATOR("Spectator", "Area for eliminated players"),
    TEAM_SPAWN("Team Spawn", "Spawn points for teams"),
    OBJECTIVE("Objective", "Objective areas like flags, hills"),
    SAFE_ZONE("Safe Zone", "Areas where players cannot take damage"),
    DANGER_ZONE("Danger Zone", "Areas with environmental hazards"),
    RESPAWN("Respawn", "Player respawn points");
    
    private final String displayName;
    private final String description;
    
    MinigameRegionType(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public String getDescription() {
        return description;
    }
    
    /**
     * Check if this region type triggers events
     */
    public boolean triggersEvents() {
        return this != LOBBY && this != SAFE_ZONE;
    }
    
    /**
     * Get the region identifier used in templates
     */
    public String getIdentifier() {
        return name().toLowerCase();
    }
}
