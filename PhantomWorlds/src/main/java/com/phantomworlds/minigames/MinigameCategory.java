package com.phantomworlds.minigames;

/**
 * Minigame categories for organization
 */
public enum MinigameCategory {
    PVP("PvP", "Player vs Player games"),
    SKILL("Skill", "Games requiring precision and skill"),
    FUN("Fun", "Casual and fun games"),
    STRATEGY("Strategy", "Strategic and tactical games");
    
    private final String displayName;
    private final String description;
    
    MinigameCategory(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public String getDescription() {
        return description;
    }
}
